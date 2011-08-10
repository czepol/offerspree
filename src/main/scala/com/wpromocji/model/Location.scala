package com.wpromocji.model

import scala.xml.{NodeSeq}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.common._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._

object Location extends Location with LongKeyedMetaMapper[Location] 
with CRUDify[Long,Location] {
  
  override def dbTableName = "locations"
  
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
  val superUserLoggedIn = If(User.superUser_? _, S.error("Nie masz uprawnień"))
  override protected def addlMenuLocParams: List[Loc.AnyLocParam] = loggedIn :: superUserLoggedIn :: Nil
  
}

class Location extends LongKeyedMapper[Location] with IdPK {

  def getSingleton = Location

  object city extends MappedString(this, 64)
  object country extends MappedCountry(this)
  object latitude extends MappedDouble(this)
  object longitude extends MappedDouble(this)

  def nearestCity(latitude: Double, longitude: Double): Box[String] = {
    val sql = "SELECT city, ((ACOS(SIN(%s*PI()/180)*SIN(latitude*PI()/180)"+
    "+COS(%s*PI()/180)*COS(latitude*PI()/180)*COS((%s-longitude)*PI()/180))"+
    "*180/PI())*111.19*1.1515) AS \"distance\" FROM locations ORDER BY distance ASC LIMIT 1"
    val query = sql.format(latitude, latitude, longitude)
    val result = Location.findAllByInsecureSql(query, IHaveValidatedThisSQL("czepol", "2011-08-10"))
    if(result.count(c =>true)==1)
      Full(result(0).city.toString)
    else
      None
  }

}
