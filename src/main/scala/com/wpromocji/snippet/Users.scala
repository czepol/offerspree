package com.wpromocji {
package snippet {
import scala.xml.{NodeSeq,Text}
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full, Empty, Failure}
import net.liftweb.http._
import net.liftweb.mapper._
import com.wpromocji.model.{User,Deal,Comment}
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
  /*
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

    def validateSignup(user: User): List[FieldError] = user.validate 

    def actionsAfterSignup(theUser: User) {  
      theUser.validated(true).uniqueId.reset()  
      theUser.save
      S.notice(S.??("welcome"))  
      User.logUserIn(theUser)  
    }  

    def testSignup() {
      val theUser: User = User.create
      validateSignup(theUser) match {  
        case Nil =>  
          println("Nie ma bledow")
          actionsAfterSignup(theUser)  
          S.redirectTo("/")
          
        case xs => S.error(xs); println("Sa bledy"); //signupFunc(Full(innerSignup _))
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
              
              bind("user", in, 
                "username" -> SHtml.text(userName.toString, parm => userName=parm, ("size","35")),
                "firstname" -> SHtml.text(firstName.toString, parm => firstName=parm, ("size","35")),
                "lastname" -> SHtml.text(lastName.toString, parm => lastName=parm, ("size", "35")),
                "locale" -> SHtml.text(locale.toString, parm => locale=parm, ("type", "hidden")),
                "profile" -> SHtml.text(profile.toString, parm => profile=parm, ("type", "hidden")),
                "email" -> SHtml.text(email.toString, parm => email=parm, ("size","35")),
                "password" -> SHtml.password(password, password=_),
                "passconf" -> SHtml.password(passconf, passconf=_),
                "submit" -> SHtml.submit(?("submit"), testSignup _))
            }
            case _ => in
          }
        }
        case None => in
      }
    } else {
      in
    }
  }*/
  
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
  
  def adminCreate(in: NodeSeq): NodeSeq = { 
    User.create.toForm(Full("Submit"), { _.save })
  }
  
  def adminEdit(in: NodeSeq): NodeSeq = {
    val userId = S.param("userid").map(_.toLong) openOr S.redirectTo("/404.html")
    if(User.withIdExist_?(userId)) {
      User.findAll(By(User.id, userId)).head.toForm(Full("Submit"), { _.save })
    } else {
      S.redirectTo("/404.html")
    }
  }
  
  def adminList(in: NodeSeq): NodeSeq = {
    page.flatMap(
      user => {
        bind("user", in,
          "username" -> user.userName,
          "userid" -> user.id,
          "email" -> user.email,
          "validated" -> user.validated,
          "superuser" -> user.superUser,
          "moderator" -> user.moderator,
          "edit" -> <a href={"/admin/users/edit/"+user.id}>Edit</a>,
          "view" -> <a href={"/admin/users/view/"+user.id}>View</a>,
          "delete" -> <a href={"/admin/users/delete/"+user.id}>Delete</a>
        )
      }
    )
  }
  
  def adminDelete(in: NodeSeq): NodeSeq = {
    Text("User delete")
  }
  
  def adminView(in: NodeSeq): NodeSeq = {
    Text("User view")
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
  
}

}
}
