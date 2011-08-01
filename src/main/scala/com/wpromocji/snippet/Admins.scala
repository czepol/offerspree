package com.wpromocji {
package snippet {

import java.text.SimpleDateFormat
import scala.xml.{NodeSeq,Text}
import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import com.wpromocji.util.Gravatar._
import util._
import js._  
import JsCmds._  

import com.wpromocji.model._

class Admins extends User with MetaMegaProtoUser[User] {

  def profileBoxHTML = {
	  (<div id="profile_info">
	  <user:gravatar />
	  <p>Welcome <strong><user:name /></strong>. <a href="/admin/logout">Log out?</a></p>
	  <p>Last login: <strong><user:lastlogin /></strong></p>
    </div>)  
  }

  def profileBox = {
    currentUser match {
      case Full(user) => {
        val gravatar = Gravatar(user.email, 42)
        val lastlogin = new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy").format(user.lastLogin.is).toString
        bind("user", profileBoxHTML, 
             "gravatar" -> <img src={gravatar} id="avatar" alt="avatar" />, 
             "name" -> user.userName,
             "lastlogin" -> lastlogin
        )
      }
      case _ => S.error("error")
    }
  } 
  
  def login(in: NodeSeq) = {
    currentUser match {
      case Full(user) => {
        if(user.superUser == true)
        {
          S.redirectTo("/admin/")
        }
      }
      case _ => 
    }
    if (S.post_?) {  
      S.param("username").  
      flatMap(username => User.find(By(userName, username),By(superUser,true))) match {
        case Full(user) if user.validated &&  
          user.password.match_?(S.param("password").openOr("*")) =>  
          S.notice(S.??("logged.in"))  
          logUserIn(user)  
          //S.redirectTo(homePage)  
          val redir = "/admin/"
          S.redirectTo(redir)  
        
        // User account is unconfirmed
        case Full(user) if !user.validated =>  
          S.error(S.??("account.validation.error"))  
        
        // Invalid username/passowrd
        case _ => S.error(S.??("invalid.credentials"))  
      }  
    }
    bind("user", in,  
         "username" -> (FocusOnLoad(<input type="text" name="username"/>)),  
         "password" -> (<input type="password" name="password"/>),  
         "submit" -> (<input type="submit" value={S.??("log.in")}/>))  
  }
  
  override def logout = {
    logoutCurrentUser
    S.redirectTo("/admin/login")
  }
  
  def dashboard(in: NodeSeq): NodeSeq = {
    val onlineUsers: List[Long] = User.loggedInUsers
    bind("dash", in,
      "total_deals" -> Deal.count(By(Deal.published, true)).toString,
      "deals_draft" -> Deal.count(By(Deal.draft, true)).toString,
      "total_comments" -> Comment.count(By(Comment.published, true)).toString,
      "comments_waiting" -> Comment.count(By(Comment.published, false)).toString,
      "users_online" -> onlineUsers.count(s => true),
      "comments_waiting" -> Comment.count(By(Comment.published, false)).toString
    )
  }

}

}
}
