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

	
	def adminCreate(in: NodeSeq): NodeSeq = {
	  Category.create.toForm(Full("Submit"), { _.save })
	}
	
	def adminEdit(in: NodeSeq): NodeSeq = {
    val categoryId = S.param("categoryid").map(_.toLong) openOr S.redirectTo("/404.html")
    if(Category.withIdExist_?(categoryId)) {
      Category.findAll(By(Category.id, categoryId)).head.toForm(Full("Submit"), { _.save })
    } else {
      S.redirectTo("/404.html")
    }
	}
	
	def adminList(in: NodeSeq): NodeSeq = {
    page.flatMap(
      category => {
        bind("category", in,
          "categoryid" -> category.id,
          "title" -> category.title,
          "slug" -> category.slug,
          "edit" -> <a href={"/admin/categories/edit/"+category.id}>Edit</a>,
          "view" -> <a href={"/admin/categories/view/"+category.id}>View</a>,
          "delete" -> <a href={"/admin/categories/delete/"+category.id}>Delete</a>
        )
      }
    )
	}
	
	def adminDelete(in: NodeSeq): NodeSeq = {
	  Text("")
	}
	
	def adminView(in: NodeSeq): NodeSeq = {
	  Text("")
	}
	
}

}
}
