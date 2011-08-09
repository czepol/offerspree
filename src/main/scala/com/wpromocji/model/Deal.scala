package com.wpromocji.model

import scala.xml.{NodeSeq, Node, Elem}
import net.liftweb.common._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.util.Date

/**
 * Deal model
 * @author Marcin Szepczyński
 * @since 0.1
 */

object Deal extends Deal
            with LongKeyedMetaMapper[Deal] {
  
  override def dbTableName = "deals"

}

class Deal extends LongKeyedMapper[Deal] 
           with IdPK 
           with OneToMany[Long, Deal] {
	
  def getSingleton = Deal

	object title extends MappedString(this,200)
	object text extends MappedText(this) {
    def textareaRows  = 5
    def textareaCols = 50
    override def toForm: Box[Elem] = {  
    S.fmapFunc({s: List[String] => this.setFromAny(s)}){funcName =>  
    Full(<textarea name={funcName}  
     rows={textareaRows.toString}  
     cols={textareaCols.toString} id={fieldId}>{  
       is match {  
         case null => ""  
         case s => s}}</textarea>)} 
    }
	}
	object date extends MappedDateTime(this) {
    override def defaultValue = new java.util.Date
	}
	object url extends MappedString(this, 2000)
	object store extends MappedString(this, 200)
	object price extends MappedString(this, 20)
	
	object value extends MappedInt(this)
		
	object draft extends MappedBoolean(this)
	object expired extends MappedBoolean(this)
  object published extends MappedBoolean(this)

  object merchant extends MappedBoolean(this)
  object online extends MappedBoolean(this)
  
  object start extends MappedDateTime(this)
  object expire extends MappedDateTime(this)
  
	object userid extends LongMappedMapper(this, User) {
    override def validSelectValues = 
      Full(User.findMap(OrderBy(User.id, Ascending)) {
        case u: User => Full(u.id.is -> u.userName.is)
      })
	}
	
	object category extends LongMappedMapper(this, Category) {
    override def validSelectValues = 
      Full(Category.findMap(OrderBy(Category.title, Ascending)) {
        case c: Category => Full(c.id.is -> c.title.is)
      })
	}
	
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
    var input = title 
    input = input.toLowerCase
    input = input.replaceAll("[^a-z0-9-\\s]", "")
    input = input.trim
    input = input.replaceAll("\\s", "-")
    input = input.replaceAll("\\-{2,}", "-")
    input
  }
  
  def toPermalink(id: Long): String =
    Deal.find(By(Deal.id, id)) match {
      case deal: Deal => {
          Deal.toPermalink(deal.title)
      }
      case _ => ""
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
    case name: String => "/image/"+name
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
