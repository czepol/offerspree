package com.wpromocji.model

import scala.xml.{NodeSeq}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

/**
 * Badge model
 * @author Marcin Szepczyński
 * @since 0.1
 */

object Badge extends Badge with LongKeyedMetaMapper[Badge] 
with CRUDify[Long,Badge] {

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
  val superUserLoggedIn = If(User.superUser_? _, S.error("Nie masz uprawnień"); S.redirectTo("/"))
  override protected def addlMenuLocParams: List[Loc.AnyLocParam] = loggedIn :: superUserLoggedIn :: Nil

  override def dbTableName = "badges"

}

class Badge extends LongKeyedMapper[Badge] with IdPK {

  def getSingleton = Badge

	object title extends MappedString(this,200)
  object description extends MappedText(this)
  object imagePath extends MappedString(this,2048)

}

