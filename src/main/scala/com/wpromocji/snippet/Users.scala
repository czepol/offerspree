package com.wpromocji {
package snippet {
import scala.xml.{NodeSeq,Text,Elem}
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full, Empty, Failure}
import net.liftweb.http._
import net.liftweb.http.S._
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.jquery.JqJsCmds.FadeIn
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.provider.{HTTPRequest,HTTPCookie}
import net.liftweb.mapper._
import com.wpromocji.model.{User,Deal,Comment,Location}
import net.liftweb.util.Helpers._
import net.liftweb.util.{FieldError}
import omniauth.{Omniauth}
import util._
import S.?

/**
 * Users snippet
 * @author Marcin Szepczyński
 * @since 0.1
 */
 
class Users extends PaginatorSnippet[User] {

  override def itemsPerPage = 20

  override def count = User.count
  
  override def page = User.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    OrderBy(User.id, Descending)
  )
	
  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq = {
    if(first==newFirst || newFirst < 0 || newFirst >= count)
    {
      if(ns == Text(?("<")) || ns == Text(?("<<")) || ns == Text(?(">>")) || ns == Text(?(">")))
      {  
        <span class="arrow">{ns}</span>
      } else {
        <span class="current">{ns}</span>
      }
    } else {  
      <a href={pageUrl(newFirst)}>{ns}</a>
    }
  }
  
  def oauthSignIn(in: NodeSeq): NodeSeq = {
    var provider: Any = ""
    var userData: Option[Any] = Empty
    var userName: Any = ""
    var firstName: Any = ""
    var lastName: Any = ""
    var locale: Any = ""
    var email: Any = ""
    var profile: Any = ""
    var submit: Any = ""
    var password = ""
    var passconf = ""
    Omniauth.currentAuthMap match {
      case Full(omni) => {
        provider = omni.get(Omniauth.Provider)
        userData = omni.get(Omniauth.UserInfo)
      }
      case Empty => S.redirectTo("/")
      case Failure(_,_,_) => S.error("Error"); S.redirectTo("/")
    }
    provider match {
      case Some(prov) => provider = prov 
    }
    
    def facebookSignUp() = {
      if(S.post_?) {
        val user = User.create
        
        user.firstName(firstName.toString)
            .lastName(lastName.toString)
            .email(email.toString)
            .locale(locale.toString)
            .facebookProfile(profile.toString)
        
        (password,passconf) match {
          case (pw,pc) if pw == pc && pw.length>5 => user.password(password.toString)
          case (pw,pc) if pw != pc => S.error(?("password-not-match"))
          case (pw,pc) if pw.length<=5 => S.error(?("password-length-error")) 
        }
        
        userName match {
          case userName: String => user.userName(userName)
          case _ => S.error("Error")
        }
        
        if(!User.uniqueEmail_?(email.toString)) {
          S.error(?("unique-email-error"))
        }
        
        if(!User.uniqueUserName_?(userName.toString)) {
          S.error(?("unique-username-error"))
        }
        
        user.validate match {
          case Nil => {
            println("walidacja")
            user.validated(true).uniqueId.reset()
            user.save
            User.logUserIn(user)
            S.redirectTo("/")
          }
          case xs => S.error(xs)
        }
      }
    }
    
    def twitterSignUp() = {
      if(S.post_?) {
        val user = User.create
        
        user.email(email.toString)
            .twitterProfile(profile.toString)
        
        (password,passconf) match {
          case (pw,pc) if pw == pc && pw.length>5 => user.password(password.toString)
          case (pw,pc) if pw != pc => S.error(?("password-not-match"))
          case (pw,pc) if pw.length<=5 => S.error(?("password-length-error")) 
        }
        
        userName match {
          case userName: String => user.userName(userName)
          case _ => S.error("Error")
        }
        
        if(!User.uniqueEmail_?(email.toString)) {
          S.error(?("unique-email-error"))
        }
        
        if(!User.uniqueUserName_?(userName.toString)) {
          S.error(?("unique-username-error"))
        }
        
        user.validate match {
          case Nil => {
            println("walidacja")
            user.validated(true).uniqueId.reset()
            user.save
            User.logUserIn(user)
            S.redirectTo("/")
          }
          case xs => S.error(xs)
        }
      }
    }
    
    if(provider == "facebook") {
      userData match {
        case Some(userData) => {
          userData match {
            case data: Map[String,Any] => {
              userName  = data.getOrElse("Nickname", "")
              firstName = data.getOrElse("FirstName", "")
              lastName  = data.getOrElse("LastName", "")
              locale    = data.getOrElse("Locale", "")
              email     = data.getOrElse("Email", "")
              profile   = data.getOrElse("Profile", "")
              
              bind("user", chooseTemplate("provider", "facebook", in), 
                "username" -> SHtml.text(userName.toString, parm => userName=parm, ("size","35")),
                "firstname" -> SHtml.text(firstName.toString, parm => firstName=parm, ("size","35")),
                "lastname" -> SHtml.text(lastName.toString, parm => lastName=parm, ("size", "35")),
                "locale" -> SHtml.text(locale.toString, parm => locale=parm, ("type", "hidden")),
                "profile" -> SHtml.text(profile.toString, parm => profile=parm, ("type", "hidden")),
                "email" -> SHtml.text(email.toString, parm => email=parm, ("size","35")),
                "password" -> SHtml.password(password, password=_),
                "passconf" -> SHtml.password(passconf, passconf=_),
                "submit" -> SHtml.submit(?("submit"), facebookSignUp _))
            }
            case _ => in
          }
        }
        case None => in
      }
    } else if(provider == "twitter") {
      userData match {
        case Some(userData) => {
          userData match {
            case data: Map[String,Any] => {
              userName = data.getOrElse("Nickname", "")
              email = ""
              if(userName != "") {
                profile  = "http://twitter.com/!#"+userName
              }
              
              bind("user", chooseTemplate("provider", "twitter", in),
                "username" -> SHtml.text(userName.toString, parm => userName=parm, ("size","35")),
                "email" -> SHtml.text(email.toString, parm => email=parm, ("size","35")),
                "profile" -> SHtml.text(profile.toString, parm => profile=parm, ("type", "hidden")),
                "password" -> SHtml.password(password, password=_),
                "passconf" -> SHtml.password(passconf, passconf=_),
                "submit" -> SHtml.submit(?("submit"), twitterSignUp _))
            }
          }
        }
        case _ => in
      }
    } else {
      in
    }
  }
  
  def profile(in: NodeSeq): NodeSeq = {
    val username = S.param("username").map(_.toString) openOr S.redirectTo("/404.html")
    val user = User.find(By(User.userName, username))
    def title(text: NodeSeq) = "Offer Spree :: "+username
    user match {
      case Full(user) => {
        bind("user", in,
          "username" -> user.userName
        )
      }
      case _ => S.error("Nie znaleziono użytkownika " + username.toString); in
    }
  }
  
  def dashboard(in: NodeSeq): NodeSeq = {
    in
  }
  
  object UserLocation extends SessionVar[String]("")
  
  def autodetect(in: NodeSeq): NodeSeq = {
    if(S.post_?) {
      val longitude = S.param("longitude") match { 
        case Full(l) if l.length!=0 => l.toDouble 
        case _ => 999L
      }
      val latitude  = S.param("latitude") match { 
        case Full(l) if l.length!=0 => l.toDouble 
        case _ => 999L
      }
      if(latitude != 999L && longitude != 999L) {
        val city = Location.nearestCity(latitude,longitude) match {
          case Full(city) => city.toString
          case _ => "Nie wykryto"
        }
        UserLocation(city)
        User.currentUser match {
          case Full(user) => User.currentUser.open_!.location(city).save
          case _ => in
        }
      }
    }
    in
  }
  
  def location(in: NodeSeq): NodeSeq = {
    var text = ""
    
    def updateLocationCookie(in: String): HTTPCookie = {
      HTTPCookie("location",Full(in),
        Full(S.hostName),Full(S.contextPath),Empty,Empty,Empty)
    }
    
    def userUpdateLocation(location: String) = {
      text = location
      UserLocation(location)
      User.currentUser.open_!.location(location).save
      S.addCookie(updateLocationCookie(location))
    }
  
    def guestUpdateLocation(location: String) = {
      text = location
      UserLocation(location)
      S.addCookie(updateLocationCookie(location))
    }
    
    if(UserLocation.is == "") {
      text = ?("your.city")
    } else {
      text = UserLocation.is
    }
    if(User.loggedIn_?) {
      var location = ""
      User.currentUser match {
        case Full(user) if user.location.toString != "" => location = user.location.toString 
        case _ => location = ""
      }
      UserLocation(location)
      bind("location", chooseTemplate("guest", "location", in),
        "input" -> SHtml.ajaxEditable(Text(text),
                                      SHtml.text(UserLocation.is, userUpdateLocation(_)),
                                      () => FadeIn("example_two_notice")))
    } else {
      bind("location", chooseTemplate("guest", "location", in),
        "input" -> SHtml.ajaxEditable(Text(text),
                                      SHtml.text(UserLocation.is, guestUpdateLocation(_)),
                                      () => FadeIn("example_two_notice")))
    }
    
  }
  
}

}
}
