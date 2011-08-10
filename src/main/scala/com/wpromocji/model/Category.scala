package com.wpromocji.model

import scala.xml.{NodeSeq}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.common.{Full}
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._


/**
 * Category model
 * @author Marcin Szepczyński
 * @since 0.1
 */

object Category extends Category with LongKeyedMetaMapper[Category] 
with CRUDify[Long,Category] {

  override def dbTableName = "categories"

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

class Category extends LongKeyedMapper[Category] 
               with IdPK 
               with OneToMany[Long, Category] {

  def getSingleton = Category

  object title extends MappedString(this, 128)
  object l10n extends MappedString(this, 64)
  
  object slug extends MappedString(this, 64) {
    override def validations = valUnique("Slug must be unique") _ :: super.validations
  }
  
  object description extends MappedText(this)
  
  object imagePath extends MappedString(this, 1024) 

  object deals extends MappedOneToMany(Deal, Deal.category, 
    OrderBy(Deal.start, Descending)) with Owned[Deal] with Cascade[Deal] 

  def withIdExist_?(categoryId: Long): Boolean = {
    Category.find(By(Category.id, categoryId)) match {
      case Full(category) => true
      case _ => false
    }
  }

}
