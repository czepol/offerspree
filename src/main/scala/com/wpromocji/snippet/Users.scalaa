package com.wpromocji {
package snippet {
import scala.xml.{NodeSeq,Text}
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full, Empty, Failure}
import net.liftweb.http._
import net.liftweb.mapper._
import com.wpromocji.model.{User,Deal,Comment}
import net.liftweb.util.Helpers._
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
    var provider = ""
    var userData: Option[Any] = Empty
    var userDataMap = Map.empty[String, String]
    var userName = ""
    var firstName = ""
    var lastName = ""
    var locale = ""
    var email = ""
    var profile = ""
    var submit = ""
    Omniauth.currentAuthMap match {
      case Full(omni) => {
        provider = omni.get(Omniauth.Provider).toString
        userData = omni.get(Omniauth.UserInfo)
      }
      case Empty => NodeSeq.Empty
      case Failure(_,_,_) => NodeSeq.Empty
    }
    println(userData.asInstanceOf[AnyRef].getClass.getSimpleName)
    println(Omniauth.currentAuthMap.asInstanceOf[AnyRef].getClass.getSimpleName)
    /*Full(provider) match {
      case "facebook" => provider = "facebook"
      case "twitter" => provider = "twitter"
      case _ => provider = "error"
    }*/
    /*if(Full(provider) == "facebook") {
      //userData = userData.toMap
      userName  = userData.getOrElse("Nickname", "").toString
      firstName = userData.getOrElse("FirstName", "").toString
      lastName  = userData.getOrElse("LastName", "").toString
      locale    = userData.getOrElse("Locale", "").toString
      email     = userData.getOrElse("Email", "").toString
      profile   = userData.getOrElse("Profile", "").toString
      
      bind("user", in, 
        "username" -> SHtml.text(userName, parm => userName=parm, ("size","55")),
        "email" -> SHtml.text(email, parm => email=parm, ("size","55")),
        "submit" -> SHtml.submit(?("submit"), ()=> submit))
    } else {
      in
    }*/
    in
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
