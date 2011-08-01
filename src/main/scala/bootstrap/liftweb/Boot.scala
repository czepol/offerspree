package bootstrap.liftweb

import java.util.Locale
import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import com.wpromocji.model._
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
      Category, Vote, Comment, CompanyProfile, CompanyAdmins)
    
    
    // where to search snippet
    LiftRules.addToPackages("com.wpromocji")

    val loggedIn = If(() => User.loggedIn_?,
              () => RedirectResponse("/user/login"))
              
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
      Menu.i("User Oauth Signin") / "user" / "oauth" >> Hidden
    ) ::: User.sitemap ::: Omniauth.sitemap
    
    val adminMenus = List(
      Menu.i("Admin") / "admin" / "index" >> superUserLoggedIn >> LocGroup("admin"),
      Menu.i("Admin login") / "admin" / "login",
      Menu.i("Admin logout") / "admin" / "logout" >> superUserLoggedIn,
      Menu.i("Admin Deals") / "admin" / "deals" >> superUserLoggedIn >> LocGroup("admin") 
        submenus(
          Menu.i("Admin List Deals") / "admin" / "deals" / "list" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Create Deals") / "admin" / "deals" / "create" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin View Deal") / "admin" / "deals" / "view" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Edit Deal") / "admin" / "deals" / "edit" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Delete Deal") / "admin" / "deals" / "delete" >> Hidden >> superUserLoggedIn >> LocGroup("admin")
        ),
      Menu.i("Admin Users") / "admin" / "users" >> superUserLoggedIn >> LocGroup("admin")
        submenus(
          Menu.i("Admin List Users") / "admin" / "users" / "list" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Create User") / "admin" / "users" / "create" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin View User") / "admin" / "users" / "view" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Edit User") / "admin" / "users" / "edit" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Delete User") / "admin" / "users" / "delete" >> Hidden >> superUserLoggedIn >> LocGroup("admin")
        ),
      Menu.i("Admin Categories") / "admin" / "categories" >> superUserLoggedIn >> LocGroup("admin")
        submenus(
          Menu.i("Admin List Categories") / "admin" / "categories" / "list" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Create Category") / "admin" / "categories" / "create" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin View Category") / "admin" / "categories" / "view" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Edit Category") / "admin" / "categories" / "edit" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Delete Category") / "admin" / "categories" / "delete" >> Hidden >> superUserLoggedIn >> LocGroup("admin")
        ),
      Menu.i("Admin Comments") / "admin" / "comments" >> superUserLoggedIn >> LocGroup("admin")
        submenus(
          Menu.i("Admin List Comments") / "admin" / "comments" / "list" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Create Comment") / "admin" / "comments" / "create" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin View Comment") / "admin" / "comments" / "view" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Edit Comment") / "admin" / "comments" / "edit" >> Hidden >> superUserLoggedIn >> LocGroup("admin"),
          Menu.i("Admin Delete Comment") / "admin" / "comments" / "delete" >> Hidden >> superUserLoggedIn >> LocGroup("admin")
        )
    )
    
    val sitemaps = entries ++ adminMenus
    
    LiftRules.setSiteMap(SiteMap(sitemaps: _*))
    
    Omniauth.init

    LiftRules.ajaxStart =
          Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
          
    LiftRules.ajaxEnd =
          Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)
          
    LiftRules.localeCalculator = localeCalculator _
    
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
        ParsePath(List("deal", dealid, "index"), "", true, true), GetRequest, _) =>
           RewriteResponse(List("deal", "show"), Map("dealid" -> urlDecode(dealid)))
      
      // Example: #/deal/1523/edit     
      case RewriteRequest(
        ParsePath(List("deal", dealid, "edit"), "", true, false), GetRequest, _) =>
           RewriteResponse(List("deal", "edit"), Map("dealid" -> urlDecode(dealid)))
      
      // Example: #/deal/1523/lorem-ipsum-dolor-sit-amet.html     
      case RewriteRequest(
        ParsePath(List("deal", dealid, slug), "html", true, false), GetRequest, _) => 
            RewriteResponse(List("deal", "show"), Map("dealid" -> urlDecode(dealid), "slug" -> urlDecode(slug)))
      
      // Example #/admin/users/edit/124     
      case RewriteRequest(
        ParsePath(List("admin", "users", "edit", userid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "users", "edit"), Map("userid" -> urlDecode(userid)))
      
      // Example #/admin/users/delete/124      
      case RewriteRequest(
        ParsePath(List("admin", "users", "delete", userid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "users", "delete"), Map("userid" -> urlDecode(userid)))
      
      // Example #/admin/users/view/124
      case RewriteRequest(
        ParsePath(List("admin", "users", "view", userid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "users", "view"), Map("userid" -> urlDecode(userid)))
      
      // Example #/admin/deals/edit/1523      
      case RewriteRequest(
        ParsePath(List("admin", "deals", "edit", dealid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "deals", "edit"), Map("dealid" -> urlDecode(dealid)))

      // Example #/admin/deals/delete/1523
      case RewriteRequest(
        ParsePath(List("admin", "deals", "delete", dealid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "deals", "delete"), Map("dealid" -> urlDecode(dealid)))

      // Example #/admin/deals/view/1523
      case RewriteRequest(
        ParsePath(List("admin", "deals", "view", dealid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "deals", "view"), Map("dealid" -> urlDecode(dealid)))
      
      // Example #/admin/categories/edit/15
      case RewriteRequest(
        ParsePath(List("admin", "categories", "edit", categoryid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "categories", "edit"), Map("categoryid" -> urlDecode(categoryid)))
      
      // Example #/admin/categories/delete/15
      case RewriteRequest(
        ParsePath(List("admin", "categories", "delete", categoryid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "categories", "delete"), Map("categoryid" -> urlDecode(categoryid)))
      
      // Example #/admin/categories/view/15
      case RewriteRequest(
        ParsePath(List("admin", "categories", "view", categoryid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "categories", "view"), Map("categoryid" -> urlDecode(categoryid)))
      
      // Example #/admin/comments/edit/142115
      case RewriteRequest(
        ParsePath(List("admin", "comments", "edit", commentid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "comments", "edit"), Map("commentid" -> urlDecode(commentid)))
      
      // Example #/admin/comments/delete/142115
      case RewriteRequest(
        ParsePath(List("admin", "comments", "delete", commentid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "comments", "delete"), Map("commentid" -> urlDecode(commentid)))
      
      // Example #/admin/comments/view/142115
      case RewriteRequest(
        ParsePath(List("admin", "comments", "view", commentid), _, _, _), _, _) =>
            RewriteResponse(List("admin", "comments", "view"), Map("commentid" -> urlDecode(commentid)))
    }

    // Enable HTML5 mode
	  
	  LiftRules.htmlProperties.default.set((r: Req) =>
      new XHtmlInHtml5OutProperties(r.userAgent))
    /*LiftRules.docType.default.set((r: Req) => r match {
	    case _ if S.skipDocType => Empty
	    case _ if S.getDocType._1 => S.getDocType._2
	    case _ => Full(DocType.html5)
	  })*/
  	
	  // Custom 404 page
    /*LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => 
        NotFoundAsTemplate(ParsePath(List("404"),"html",false,false))
    })*/
    
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

}
