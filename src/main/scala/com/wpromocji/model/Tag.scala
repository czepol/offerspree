package com.wpromocji.model

import scala.xml.{NodeSeq}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._


object Tag extends Tag with LongKeyedMetaMapper[Tag] 
with CRUDify[Long,Tag] {

  override def dbTableName = "tags"
  override def fieldOrder=List(name)

  override def pageWrapper(body: NodeSeq) =
    <lift:surround with="admin" at="content">{body}</lift:surround>
  override def calcPrefix = List("admin",_dbTableNameLC)
  override def showAllMenuLocParams = LocGroup("admin") :: Nil
  override def createMenuLocParams  = LocGroup("admin") :: Nil
  override def viewMenuLocParams    = LocGroup("admin") :: Nil
  override def editMenuLocParams    = LocGroup("admin") :: Nil
  override def deleteMenuLocParams  = LocGroup("admin") :: Nil
  
  def loginAndComeBack = {
    if(User.loggedIn_?) {
      S.error("Brak uprawnień")
      () => RedirectResponse("/")
    } else {
      val uri = S.uri 
      RedirectWithState("/user/login", RedirectState(() => User.loginReferer(uri))) 
    }
  }
  
  val superUserLoggedIn = If(User.superUser_? _, loginAndComeBack _)
  override protected def addlMenuLocParams: List[Loc.AnyLocParam] = superUserLoggedIn :: Nil

}

class Tag extends LongKeyedMapper[Tag] with IdPK {

  def getSingleton = Tag

  object name extends MappedPoliteString(this,120) {
		override def setFilter = List(x=> x.trim)
  }

}
