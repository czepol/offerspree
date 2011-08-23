package com.wpromocji.model

import scala.xml.{NodeSeq, Node, Elem}
import net.liftweb.common._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import S.?

import java.util.Date
import com.wpromocji.lib._

/**
 * Deal model
 * @author Marcin Szepczyński
 * @since 0.1
 */

object Deal extends Deal with LongKeyedMetaMapper[Deal] 
with CRUDify[Long, Deal] {
  
  override def dbTableName = "deals"

  override def pageWrapper(body: NodeSeq) =
    <lift:surround with="admin" at="content">{body}</lift:surround>
  override def calcPrefix = List("admin",_dbTableNameLC)
  override def showAllMenuLocParams = LocGroup("admin") :: Nil
  override def createMenuLocParams  = LocGroup("admin") :: Nil
  override def viewMenuLocParams    = LocGroup("admin") :: Nil
  override def editMenuLocParams    = LocGroup("admin") :: Nil
  override def deleteMenuLocParams  = LocGroup("admin") :: Nil
  
  override def fieldsForList: List[MappedField[_, Deal]] = 
    List(id,title,date,expired,published,userid)
  
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

class Deal extends LongKeyedMapper[Deal] with IdPK 
with ManyToMany with OneToMany[Long, Deal] {
	
  def getSingleton = Deal

	object title extends MappedString(this,128) {
	  override def displayName = ?("title")
	}
	object text extends MappedText(this) {
	  override def displayName = ?("description")
	}
	object date extends MappedDateTime(this) {
	  override def displayName = ?("deal.date")
    override def defaultValue = new java.util.Date
	}
	object url extends MappedString(this, 256) {
	  override def displayName = ?("url")
	}
	object store extends MappedString(this, 200) {
	  override def displayName = ?("store")
	}
	object price extends MappedString(this, 20) {
	  override def displayName = ?("price")
	}
	
	object value extends MappedInt(this) {
	  override def displayName = ?("deal.value")
  }
		
	object draft extends MappedBoolean(this) {
	  override def displayName = ?("draft")
	}
	object expired extends MappedBoolean(this) {
	  override def displayName = ?("expirde")
	}
  object published extends MappedBoolean(this) {
    override def displayName = ?("published")
  }

  object merchant extends MappedBoolean(this) {
    override def displayName = ?("merchant")
  }
  object online extends MappedBoolean(this) {
    override def displayName = ?("online")
  }
  
  object start extends MappedDateTime(this) {
    override def displayName = ?("start")
  }
  object expire extends MappedDateTime(this) {
    override def displayName = ?("end")
  }
  
	object userid extends LongMappedMapper(this, User) {
	  override def displayName = ?("username")
    override def validSelectValues = 
      Full(User.findMap(OrderBy(User.id, Ascending)) {
        case u: User => Full(u.id.is -> u.userName.is)
      })
	}
	
	object merchantid extends LongMappedMapper(this, Merchant) {
	  override def displayName = ?("merchant")
    override def validSelectValues = 
      Full(Merchant.findMap(OrderBy(Merchant.name, Ascending)) {
        case m: Merchant => Full(m.id.is -> m.name.is)
      })
	}
	
	object category extends LongMappedMapper(this, Category) {
	  override def displayName = ?("category")
    override def validSelectValues = 
      Full(Category.findMap(OrderBy(Category.title, Ascending)) {
        case c: Category => Full(c.id.is -> c.title.is)
      })
	}

	object tags extends MappedManyToMany(
    DealTag, DealTag.dealid, DealTag.tagid, Tag)
	
	object imageOrigin extends MappedString(this, 1024)
	object imageThumb extends MappedString(this, 1024)
	
	def getTitleById(dealId: Long): String = {
	  Deal.find(By(Deal.id, dealId)) match {
	    case Full(deal) => deal.title.toString
	    case _ => "Oferta usunięta"
	  }
	}
	
  def withIdExist_?(dealId: Long): Boolean = {
    Deal.find(By(Deal.id, dealId)) match {
      case Full(deal) => true
      case _ => false
    }
  }
  
  def toPermalink(title: String): String = {
    HtmlHelpers.slugify(title)
  }
  
  def toPermalink(id: Long): String =
    Deal.find(By(Deal.id, id)) match {
      case Full(deal) => {
          Deal.toPermalink(deal.title)
      }
      case _ => ""
    }
  
  def absLink(id: Long): String = {
    "/deal/"+id.toString+"/"+Deal.toPermalink(id)+".html"
  }
    
  def toJson(id: Long, ver: String): JValue = {
    if(ver == "1.0") {
      Deal.find(By(Deal.id, id)) match {
        case Full(deal) => {
            ("deal" -> 
             ("title" -> deal.title.toString) ~
             ("url" -> deal.url.toString) ~
             ("date" -> deal.date.toString))
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
  
  def toXml(id: Long, ver: String): Node = Xml.toXml(Deal.toJson(id,ver)).head
  
  def toLink(title: String, id: Long) = 
    <a href={"/deal/"+id+"/"+Deal.toPermalink(title)+".html"}>{title}</a>
  
  def imageLink(name: String) = name match {
    case url: String if url.startsWith("http") => url
    case name: String if name.length!=0 => "/image/"+name
    case _ => "/img/125x100.png"
  }
  
  def nextDeal(curr: Long): Long = {
    val currDeal = Deal.find(By(Deal.id, curr))
    currDeal match {
      case Full(deal) => {
        var date: Date = deal.date
        Deal.find(By_<(Deal.date, date), By(Deal.published, true)) match {
          case Full(next) => next.id
          case _ => 0L
        }
      }
      case _ => 0L
    }
  }
  
  def prevDeal(curr: Long): Long = {
    val currDeal = Deal.find(By(Deal.id, curr))
    currDeal match {
      case Full(deal) => {
        var date: Date = deal.date
        Deal.find(By_>(Deal.date, date), By(Deal.published, true)) match {
          case Full(prev) => prev.id
          case _ => 0L
        }
      }
      case _ => 0L
    }
  }  
}
