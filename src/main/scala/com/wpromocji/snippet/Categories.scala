package com.wpromocji {
package snippet {
import scala.xml.{NodeSeq,Text}
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.http._
import net.liftweb.mapper._
import com.wpromocji.model.{Deal,Category}
import net.liftweb.util.Helpers._
import util._
import S.?

class Categories extends PaginatorSnippet[Category] {

  override def count = Category.count
  
	override def page = Category.findAll(
	   StartAt(curPage*itemsPerPage),
	   MaxRows(itemsPerPage),
	   OrderBy(Category.id, Ascending)
	)
	
  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq = {
    if(first==newFirst || newFirst < 0 || newFirst >= count)
    {
      if(ns == Text(?("<")) || ns == Text(?("<<")) || ns == Text(?(">>")) || ns == Text(?(">")))
      {  
        <span class="arrow">{ns}</span>
      } else {
        <span class="current">{ns}</span>
      }
    } else {  
      <a href={pageUrl(newFirst)}>{ns}</a>
    }
  }
}

}
}
