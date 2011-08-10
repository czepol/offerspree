package com.wpromocji {
package snippet {

import java.text.SimpleDateFormat
import scala.xml.{ NodeSeq, Text, Unparsed }
import com.wpromocji.model.{User,Deal,Comment}
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.mapper._
import net.liftweb.common._
import Helpers._
import net.liftweb.http.js.{ JE, JsCmd, JsCmds, Jx, jquery }
import JsCmds._
import JE._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.textile.TextileParser
import S.?

import net.liftweb.util.CssSel

class Comments extends PaginatorSnippet[Comment] {

  override def itemsPerPage = 20
    
  override def count = Comment.count 

  override def page = Comment.findAll(
    StartAt(curPage*itemsPerPage), 
    MaxRows(itemsPerPage),
    OrderBy(Comment.date, Descending)
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

  def list = {
    var iterator: Int = 0
    val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
    val comments: List[Comment] = Comment.findAll(By(Comment.dealid, dealId), By(Comment.published, true))
    val count = comments.count(c=>true)
    "#comments *" #> "Komentarze: %s".format(count) & 
    //"li [class]" #> {if(iterator%2==1) { "even" } else { "odd" }} &
    "li *" #> comments.map(comment=>
      "name=comment_id [name]" #> "comment-%s".format(comment.id.toString) & 
      ".author *" #>  Comment.commentAuthor(comment.userid) &
      ".avatar" #> Comment.commentAuthorGravatar(comment.userid) &
      ".date *" #> comment.date &
      ".text" #> <xml:group>{Unparsed(comment.text)}</xml:group>
    )
  }

  def addComment(in: NodeSeq): NodeSeq = {
    val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
    val user = User.currentUser
    
    user match {
      case Full(u) => {
        var text = ""
        // new-comment is element on page inside 
        def onSubmit = {
          val now = new java.util.Date
          val html = TextileParser.toHtml(text).toString
          val c = Comment.create.userid(u.id).text(html).dealid(dealId).date(now).published(true)
          c.validate
          c.save
          AppendHtml("new-comment", (<p class="post-footer align-left">
                                       <p style="margin-bottom: 5px; font-weight: bold;">{ u.niceName } said...<br/></p>
                                       <p>{ Unparsed(html) }</p>
                                       <p>{ now }</p>
                                     </p>))// & clearForm

        }
        //def clearForm = JsRaw("$('#comm-author').val('')") & JsRaw("$('#comm-text').val('')") & JsRaw("$('#comm-website').val('')")
        /*"#addCommentBox" #> ( 
          "#addCommentAuthor" #> SHtml.text("", a => author = a, ("id", "comm-author")) &
          "#addCommentText" #> SHtml.textarea("", t => text = t, ("id", "comm-text")) &
          "type=submit" #> SHtml.ajaxSubmit("Post", () => onSubmit, ("class", "button"))
          )*/
        bind("form",in,
				  "text" -> SHtml.textarea("", parm => text=parm, ("id", "markitup"), "cols"->"60", "rows"-> "5"),
				  "submit" -> SHtml.ajaxSubmit("Submit", () => onSubmit )
			  )
      }
      case _ => <p><a href={User.loginAndRedirectURL}>Zaloguj się</a> aby komentować</p>
    }
  }
}

}
}
