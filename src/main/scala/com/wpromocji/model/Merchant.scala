package com.wpromocji.model

import scala.xml.{NodeSeq, Node}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.common.{Full}
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

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
    if(User.loggedIn_?) {
      S.error("Brak uprawnień")
      RedirectResponse("/")
    } else {
      val uri = S.uri 
      RedirectWithState("/user/login", RedirectState(() => User.loginReferer(uri))) 
    }
  }
  
  val superUserLoggedIn = If(User.superUser_? _, loginAndComeBack _)
  override protected def addlMenuLocParams: List[Loc.AnyLocParam] = superUserLoggedIn :: Nil

}

class Merchant extends LongKeyedMapper[Merchant] 
with CreatedUpdated with IdPK with OneToMany[Long,Merchant] {

  def getSingleton = Merchant

  object name extends MappedString(this, 128)
  object url extends MappedString(this, 128)
  object description extends MappedText(this)
  object networked extends MappedBoolean(this)
  
  def toJson(id: Long, ver: String): JValue = {
    if(ver == "1.0") {
      Merchant.find(By(Merchant.id, id)) match {
        case Full(merchant) => {
            ("merchant" -> 
             ("name" -> merchant.name.toString) ~
             ("url" -> merchant.url.toString) ~
             ("description" -> merchant.description.toString))
        }
        case _ => ("deal" -> 
                   ("errors" -> 
                    ("error" -> "Not found")
                   )
                  )
      }
    } else {
      ("deal" ->
       ("errors" ->
        ("error" -> "Bad API version")
       )
      )
    }
  }

  def toXml(id: Long, ver: String): Node = 
    Xml.toXml(Merchant.toJson(id,ver)).head

}

