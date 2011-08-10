package com.wpromocji.model

import scala.xml.{NodeSeq}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

object Store extends Store with LongKeyedMetaMapper[Store] 
with CRUDify[Long,Store] {
  
  override def dbTableName = "stores"

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
  
  val superUserLoggedIn = If(User.superUser_? _,loginAndComeBack _)
  override protected def addlMenuLocParams: List[Loc.AnyLocParam] = superUserLoggedIn :: Nil

}

class Store extends LongKeyedMapper[Store] with IdPK {

  def getSingleton = Store

  object name extends MappedString(this, 128)
  object description extends MappedText(this)
  object country extends MappedCountry(this)
  object postalcode extends MappedPostalCode(this, country)
  object city extends MappedString(this, 64)
  object address extends MappedString(this, 64)
  object latitude extends MappedDouble(this)
  object longitude extends MappedDouble(this)
  
  //object merchantId extends 
  


}

