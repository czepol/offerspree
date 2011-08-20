package bootstrap.liftweb

import java.util.Locale
import java.io.{File,ByteArrayInputStream}
import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.http.S._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.widgets.autocomplete.AutoComplete
import com.wpromocji.model._
import com.wpromocji.api._
import com.wpromocji.Helpers._
import net.liftweb.http.provider.{HTTPRequest,HTTPCookie}
import omniauth._
import omniauth.lib._
import Helpers._


/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {

    // define database engine, database and Connection manager
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = new StandardDBVendor(Props.get("db.driver") openOr "org.postgresql.Driver",
			         Props.get("db.url") openOr "jdbc:postgresql:wpromocji",
			         Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)
      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }
  
    // create database tables from models 
    Schemifier.schemify(true, Schemifier.infoF _, 
      User, Deal, Badge, UserBadge, Tag, DealTag, 
      Category, Vote, Comment, CompanyProfile, CompanyAdmins,
      Merchant, Store, OnlineStore, Location)
     
    // where to search snippet
    LiftRules.addToPackages("com.wpromocji")
    
    val loggedIn = If(User.loggedIn_? _, loginAndComeBack _)
            
    val superUserLoggedIn = If(() => User.superUser_?, 
              () => RedirectResponse("/admin/login"))
              
    val moderatorLoggedIn = If(() => User.moderator_?, 
              () => RedirectResponse("/user/login"))
              
    // Build SiteMap
    val entries = List(
      Menu.i("Home") / "index" >> LocGroup("menu") >> LocGroup("sorting"),
      Menu.i("Online deals") / "online" >> LocGroup("sorting"),
      Menu.i("Merchant deals") / "merchant" >> LocGroup("sorting"),
      Menu.i("New deals") / "upcoming" >> LocGroup("sorting"),
      Menu.i("Special deals") / "special" >> LocGroup("sorting"),
      Menu.i("Show Deal") / "deal" / "show" >> Hidden >> LocGroup("menu"),
      Menu.i("Edit Deal") / "deal" / "edit" >> Hidden >> LocGroup("menu"),
      Menu.i("Submit Deal") / "deal" / "submit" >> loggedIn >> LocGroup("menu"),
      Menu.i("Dashboard") / "user" / "dashboard" >> loggedIn,
      Menu.i("User profile") / "user" / "profile" >> Hidden,
      Menu.i("Change language") / "lang" >> Hidden,
      Menu.i("User Oauth Signin") / "user" / "oauth" >> Hidden,
      Menu.i("Admin badges") / "admin" / "badges" >> Hidden submenus(Badge.menus: _*),
      Menu.i("Admin categories") / "admin" / "categories" >> Hidden submenus(Category.menus: _*),
      Menu.i("Admin comments") / "admin" / "comments" >> Hidden submenus(Comment.menus: _*),
      Menu.i("Admin deals") / "admin" / "deals" >> Hidden submenus(Deal.menus: _*),
      Menu.i("Admin locations") / "admin" / "locations" >> Hidden submenus(Location.menus: _*),
      Menu.i("Admin merchants") / "admin" / "merchants" >> Hidden submenus(Merchant.menus: _*),
      Menu.i("Admin online stores") / "admin" / "online-stores" >> Hidden submenus(OnlineStore.menus: _*),
      Menu.i("Admin stores") / "admin" / "stores" >> Hidden submenus(Store.menus: _*),
      Menu.i("Admin tags") / "admin" / "tags" >> Hidden submenus(Tag.menus: _*),
      Menu.i("Admin votes") / "admin" / "votes" >> Hidden submenus(Vote.menus: _*)
    ) ::: User.sitemap ::: Omniauth.sitemap
    
    val sitemaps = entries
    
    LiftRules.setSiteMap(SiteMap(sitemaps: _*))
    
    // Init modules
    Omniauth.init
    AutoComplete.init

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
          
    LiftRules.ajaxEnd   = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
          
    LiftRules.localeCalculator = localeCalculator _
    
    LiftRules.dispatch.append(DealAPI)
    LiftRules.dispatch.append(MerchantAPI)
    LiftRules.dispatch.append(TagAPI)
  
    val imagePath = Props.get("upload.imagepath") openOr "/src/main/webapp/images"
    
    LiftRules.statelessDispatchTable.append {
      case Req("image" :: name :: Nil, "jpg", _) => 
        () =>    
          val file   = new File(imagePath+"/"+name+".jpg")
          val stream = new ByteArrayInputStream(getBytesFromFile(file))
          Full(StreamingResponse(stream,
                            () => stream.close,
                            stream.available,
                            List("Content-Type" -> "image/jpg"),
                            Nil,
                            200))
      case Req("image" :: name :: Nil, "png", _) => 
        () =>    
          val file   = new File(imagePath+"/"+name+".png")
          val stream = new ByteArrayInputStream(getBytesFromFile(file))
          Full(StreamingResponse(stream,
                            () => stream.close,
                            stream.available,
                            List("Content-Type" -> "image/png"),
                            Nil,
                            200))
      case Req("image" :: name :: Nil, "gif", _) => 
        () =>    
          val file   = new File(imagePath+"/"+name+".gif")
          val stream = new ByteArrayInputStream(getBytesFromFile(file))
          Full(StreamingResponse(stream,
                            () => stream.close,
                            stream.available,
                            List("Content-Type" -> "image/gif"),
                            Nil,
                            200))
    }
    
    LiftRules.statelessRewrite.append {
      // Example: #/dashboard
      case RewriteRequest(
        ParsePath(List("dashboard"), "", true, false), GetRequest, _) =>
           RewriteResponse(List("user","dashboard"))
           
      // Example: #/profile/czepol
      case RewriteRequest(
        ParsePath(List("profile", username), "", true, false), GetRequest, _) =>
           RewriteResponse(List("user","profile"), Map("username" -> urlDecode(username)))
    
      // Example: #/lang/en
      case RewriteRequest(
        ParsePath(List("lang", lang), "", true, false), GetRequest, _) =>
           RewriteResponse(List("lang"), Map("lang" -> urlDecode(lang)))
    
      // Example: #/deal/1523/      
      case RewriteRequest(
        ParsePath(List("deal", dealid, "index"), "", true, true), _, _) =>
           RewriteResponse(List("deal", "show"), Map("dealid" -> urlDecode(dealid)))
      
      // Example: #/deal/1523/edit     
      case RewriteRequest(
        ParsePath(List("deal", dealid, "edit"), "", true, false), _, _) =>
           RewriteResponse(List("deal", "edit"), Map("dealid" -> urlDecode(dealid)))
      
      // Example: #/deal/1523/lorem-ipsum-dolor-sit-amet.html     
      case RewriteRequest(
        ParsePath(List("deal", dealid, slug), "html", true, false), _, _) => 
            RewriteResponse(List("deal", "show"), Map("dealid" -> urlDecode(dealid), "slug" -> urlDecode(slug)))

    }

    // Enable HTML5 mode
	  
	  LiftRules.htmlProperties.default.set((r: Req) =>
      new XHtmlInHtml5OutProperties(r.userAgent))
    
    LiftRules.noticesAutoFadeOut.default.set( (notices: NoticeType.Value) => {
        notices match {
          case NoticeType.Notice => Full((2 seconds, 1 seconds))
          case _ => Empty
        }
    }) 
    
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
  }

  def localeCalculator(request : Box[HTTPRequest]): Locale = 
    request.flatMap(r => {
      def localeCookie(in: String): HTTPCookie = 
        HTTPCookie("locale",Full(in),
          Full(S.hostName),Full(S.contextPath),Empty,Empty,Empty)
      def localeFromString(in: String): Locale = {
        val x = in.split("_").toList; new Locale(x.head,x.last)
      }
      def calcLocale: Box[Locale] = 
        S.findCookie("locale").map(
          _.value.map(localeFromString)
        ).openOr(Full(LiftRules.defaultLocaleCalculator(request)))
      S.param("lang") match {
        case Full(null) => calcLocale
        case f@Full(selectedLocale) => 
          S.addCookie(localeCookie(selectedLocale))
          tryo(localeFromString(selectedLocale))
        case _ => calcLocale
      }
    }).openOr(Locale.getDefault())
    
  def loginAndComeBack = {
    val uri = S.uri 
    RedirectWithState("/user/login", RedirectState(() => User.loginReferer(uri))) 
  }
}
