package com.wpromocji.model

import scala.xml.{NodeSeq}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.liftweb.common.Full
import com.wpromocji.lib._

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
      RedirectResponse("/")
    } else {
      val uri = S.uri 
      RedirectWithState("/user/login", RedirectState(() => User.loginReferer(uri))) 
    }
  }
  
  val superUserLoggedIn = If(User.superUser_? _, loginAndComeBack _)
  override protected def addlMenuLocParams: List[Loc.AnyLocParam] = superUserLoggedIn :: Nil

}

class Tag extends LongKeyedMapper[Tag] with IdPK with ManyToMany {

  def getSingleton = Tag

  def toSlug(tag: String): String = {
    var input = tag
    input = input.toLowerCase
    input = input.replaceAll("[^a-z0-9-\\s]", "")
    input = input.trim
    input = input.replaceAll("\\s", "-")
    input = input.replaceAll("\\-{2,}", "-")
    input
  }

  object name extends MappedString(this,128) {
    override def dbIndexed_? = true
		override def dbNotNull_? = true
		override def setFilter = List(x=> x.trim.toLowerCase)
  }
  
  object slug extends MappedPoliteString(this,128) {
    override def dbIndexed_? = true
		override def dbNotNull_? = true
    override def validations = valUnique("Slug must be unique!") _ :: super.validations
		override def setFilter = trim _ :: toLower _ :: HtmlHelpers.slugify _ :: super.setFilter
  }
  
  object deals extends MappedManyToMany(
    DealTag, DealTag.tagid, DealTag.dealid, Deal)
    
  def autocompleteJArray(q: String): JValue = {
    val input = q.toLowerCase.replaceAll("\"", "").replaceAll("'", "")+"%"
    val query = "SELECT name FROM tags WHERE lower(name) LIKE '%s'".format(input)
    val results = Tag.findAllByInsecureSql(query, IHaveValidatedThisSQL("czepol", "2011-08-17"))
    var list = List[String]()
    for(tag <- results) {
      list = list ::: List(tag.name.is)
    }
    val json: JValue = list
    json
  }
  
  def autocompleteJArrayAllTags: JValue = {
    val results = Tag.findAll
    var list = List[String]()
    for(tag <-results) {
      list = list ::: List(tag.name.is)
    }
    val json: JValue = list
    list
  }
  
  def tagsToJArrayString(deal: Deal): String = {
    var tags = deal.tags.toList
    var list: List[String] = Nil
    for(tag <- tags) {
      list = list ::: List(tag.name.is)
    }
    val json: JValue = list
    pretty(render(json))
  }
  
  def tagsToDealJArray(id: Long): JValue = {
    Deal.find(By(Deal.id, id)) match {
      case Full(deal) => {
        var tags = deal.tags.toList
        var list: List[String] = Nil
        for(tag <- tags) {
          list = list ::: List(tag.name.is)
        }
        val json: JValue = list
        json
      }
      case _ => "[]"
    }
  }
  
  def tagsToString(deal: Deal): String = { 
    var str = tagsToJArrayString(deal)
    if(str.startsWith("[") && str.endsWith("]")) {
      str = str.substring(1, str.length-1)
    }
    str = str.replaceAll("\"", "")
    str = str.replaceAll(",", ", ")
    str
  }
}
