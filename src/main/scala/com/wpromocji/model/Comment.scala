package com.wpromocji.model

import scala.xml.{NodeSeq,Text}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.common._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import com.wpromocji.util.Gravatar

/**
 * Comment model
 * @author Marcin Szepczyński
 * @since 0.1
 */

object Comment extends Comment with LongKeyedMetaMapper[Comment] 
with CRUDify[Long,Comment] {

  override def dbTableName = "comments"

  override def pageWrapper(body: NodeSeq) =
    <lift:surround with="admin" at="content">{body}</lift:surround>
  override def calcPrefix = List("admin",_dbTableNameLC)
  override def showAllMenuLocParams = LocGroup("admin") :: Nil
  override def createMenuLocParams  = LocGroup("admin") :: Nil
  override def viewMenuLocParams    = LocGroup("admin") :: Nil
  override def editMenuLocParams    = LocGroup("admin") :: Nil
  override def deleteMenuLocParams  = LocGroup("admin") :: Nil
  
  def loginAndComeBack = {
    val uri = S.uri 
    RedirectWithState("/user/login", RedirectState(() => User.loginReferer(uri))) 
  }
  
  val loggedIn = If(User.loggedIn_? _, loginAndComeBack _)
  val superUserLoggedIn = If(User.superUser_? _, {S.error("Nie masz uprawnień"); S.redirectTo("/")})
  override protected def addlMenuLocParams: List[Loc.AnyLocParam] = loggedIn :: superUserLoggedIn :: Nil

}

class Comment extends LongKeyedMapper[Comment] 
              with IdPK {

  def getSingleton = Comment

	object userid extends LongMappedMapper(this, User) {
    override def validSelectValues = 
      Full(User.findMap(OrderBy(User.id, Ascending)) {
        case u: User => Full(u.id.is -> u.userName.is)
      })
	}
	
	object dealid extends LongMappedMapper(this, Deal) {
    override def validSelectValues = 
      Full(Deal.findMap(OrderBy(Deal.start, Descending)) {
        case d: Deal => Full(d.id.is -> (d.id.is + " - " + d.title.is))
      })
	}
	
	object userName extends MappedString(this, 32)
  
  object threadid extends MappedLongForeignKey(this, Comment) {
    def defualtValue = 0L
  }
  
	object date extends MappedDateTime(this) {
    override def defaultValue = new java.util.Date
	}
	
	object text extends MappedText(this)
  object published extends MappedBoolean(this)
  
  
  
  def commentAuthor(userId: Long): NodeSeq = {
    User.find(By(User.id, userId)) match {
      case Full(user) => {
        if(user.userSite.length != 0)
          <a href={user.userSite}>{user.userName}</a>
        else
          Text(user.userName)
      }
      case _ => Text("Konto usunięte")
    }
  }
  
  def commentAuthorGravatar(userId: Long): NodeSeq = {
    User.find(By(User.id, userId)) match {
      case Full(user) => {
        <img src={Gravatar(user.email)} class="gravatar" alt="gravatar" />
      }
      case _ => Text("")
    }
  }
  
  def withIdExist_?(commentId: Long): Boolean = {
    Comment.find(By(Comment.id, commentId)) match {
      case Full(comment) => true
      case _ => false
    }
  }
  
}
