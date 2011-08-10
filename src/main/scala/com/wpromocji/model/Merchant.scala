package com.wpromocji.model

import scala.xml.{NodeSeq}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

object Merchant extends Merchant with LongKeyedMetaMapper[Merchant] 
with CRUDify[Long,Merchant] {

  override def dbTableName = "merchants"

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

}

class Merchant extends LongKeyedMapper[Merchant] 
               with CreatedUpdated with IdPK {

  def getSingleton = Merchant

  object name extends MappedString(this, 128)
  object url extends MappedString(this, 128)
  object description extends MappedText(this)
  object networked extends MappedBoolean(this)
  //object meta

}

