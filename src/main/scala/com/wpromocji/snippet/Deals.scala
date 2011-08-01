package com.wpromocji {
package snippet {
import scala.xml.{NodeSeq,Text}
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full,Empty}
import net.liftweb.http._
import net.liftweb.mapper._
import com.wpromocji.model.{User,Deal,Comment,Vote,Category}
import com.wpromocji.util._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.util.Helpers._
import util._
import S.?


class Deals extends PaginatorSnippet[Deal] {
  
  override def itemsPerPage = 5
    
  override def count = Deal.count 

  override def page = Deal.findAll(
    StartAt(curPage*itemsPerPage), 
    MaxRows(itemsPerPage),
    OrderBy(Deal.date, Descending)
  )
  
  def pageHotDeals = Deal.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    By(Deal.published, true),
    OrderBy(Deal.date, Descending)
  )
  
  def pageSpecialDeals = Deal.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    By(Deal.published, true),
    OrderBy(Deal.date, Descending)
  )
  
  def pageUpcomingDeals = Deal.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    By(Deal.published, false),
    OrderBy(Deal.date, Descending)
  )
  
  def pageMerchantDeals = Deal.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    By(Deal.merchant, true),
    By(Deal.published, true),
    OrderBy(Deal.date, Descending)
  )
  
  def pageOnlineDeals = Deal.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    By(Deal.online, true),
    By(Deal.published, true),
    OrderBy(Deal.date, Descending)
  )
  
  override def prevXml: NodeSeq = Text("« " + ?("previous"))
  override def nextXml: NodeSeq = Text(?("next") + " »")

  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq = {
    if(first==newFirst || newFirst < 0 || newFirst >= count)
    {
      if(ns != Text("« " + ?("previous")) && ns != Text(?("next") + " »"))
      {
        <span>{ns}</span>
      } else {
        <xml:group />
      }
    } else {
      <a href={pageUrl(newFirst)}>{ns}</a>
    }
  }
  
  def submit(in: NodeSeq): NodeSeq = {
    Deal.create.toForm(Full(?("submit")), { _.save })
  }

  def add(in: NodeSeq): NodeSeq = {
   
    var title = ""
    var text  = ""
    var url   = ""
    var price = ""
    var categories = List(("0","-")) ::: Category.findAll.map(cat => (cat.id.toString, ?(cat.l10n.toString))).toList
    var selectedCatId = ""
    var merchant = false
    var online   = false
    var start = ""
    var end   = ""
    var store = ""
    var dealtypes = Map("merchant" -> 0, "online" -> 1)
    var dealtype  = ""
    
    def submit() = {
      println("Deal Type to" + dealtype)
      if(title=="") S.error("Tytuł nie może być pusty")
      //if(text=="")  S.error("Opis nie może być pusty")
      else {
        val deal = Deal.create.date(new java.util.Date).title(title).text(text).url(url).price(price)
        deal.save
        S.redirectTo("/")
      }
    }
    bind("deal",in,
				"title" -> SHtml.text("", parm => title=parm, ("size","55")),
				"dealtype" -> SHtml.radio(dealtypes.keys.toList, Empty, dt => dealtype=dt.toString).
				              flatMap(c => (<label>{c.xhtml} {?(c.key.toString)}</label>)),
				"url" -> SHtml.text("", parm => url=parm, ("size","55")),
				"price" -> SHtml.text("", parm => price=parm, ("size","10")),
				"text" -> SHtml.textarea("", parm => text=parm, ("id", "markitup"), "cols"->"80", "rows"-> "10"),
				"category" -> SHtml.select(categories, Empty, cat => selectedCatId = cat.toString),
				"start" -> SHtml.text("", parm => start=parm, ("class", "datepicker")),
				"end" -> SHtml.text("", parm => end=parm, ("class", "datepicker")),
				"submit" -> SHtml.submit(?("submit"), submit)
			)
  }
  
  def edit(in: NodeSeq): NodeSeq = {
    val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
		var title = ""
		var text = ""
		var deal = Deal.find(By(Deal.id, dealId))
	
		def submit() = {
			if(title=="") S.error("Title musn't be empty") 
			else {
				val html = text 
				deal.open_!.title(title).text(html).save
				deal match {
				  case Full(d) => S.redirectTo("/deal/"+d.id)
				  case _ => S.redirectTo("/404.html")
				}
			}
		}
		deal match {
			case Full(d) => bind("deal",in,
					"title" -> SHtml.text(d.title, parm => title=parm, ("size","55")),
					"text" -> SHtml.textarea(d.text, parm => text=parm, ("id", "markitup"), "cols"->"80", "rows"-> "10"),
					"submit" -> SHtml.submit("Save", submit)
					)
			case _ => S.error("Error occured"); S.redirectTo("/index")
		}
	}
  
  def show(in: NodeSeq): NodeSeq = {
    val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
    val slug = S.param("slug").map(_.toString) openOr ""
    var dealid = dealId.toString
    var userid = ""
    var value  = Vote.getVotes(dealId).toString
    
    def submitUp() = {
      if(S.post_?)
      {
          if(userid.length !=0 && userid.toLong > 0L) {
            Vote.voteUp(dealid.toLong,userid.toLong)
          }
      }
    }
    
    def submitDown() = {
      if(S.post_?)
      {
          if(userid.length !=0 && userid.toLong > 0L) {
            Vote.voteDown(dealid.toLong,userid.toLong)
          }
      }
    }
    
    User.currentUser match {
      case Full(user) => userid = user.id.toString
      case _ => userid = "-1"
    }    
    
    Deal.find(By(Deal.id, dealId)) match {
      case Full(deal) => 
          bind("deal", in, 
            "title" -> deal.title,
            "titleLink" -> <a href={"/deal/"+deal.id+"/"+Deal.toPermalink(deal.title.toString)+".html"}>{deal.title}</a>,
            "text" -> deal.text,
            "expire" -> deal.expire,
            "thumb" -> <img src={Deal.imageLink(deal.imageThumb)} alt="image" title={deal.title} />,
            "image" -> <img src={Deal.imageLink(deal.imageOrigin)} alt="image" title={deal.title} />,
            "dealid" -> SHtml.text(dealid, parm => dealid=parm, ("type","hidden")),
            "userid" -> SHtml.text(userid, parm => userid=parm, ("type","hidden")),
				    "value" -> SHtml.text(value, parm => value=parm, ("readonly", "readonly")),
				    "voteup" -> SHtml.submit("+", submitUp),
				    "votedown" -> SHtml.submit("-", submitDown)
			    )
      case _ => Text(?("Nie znaleziono żadnych ofert"))
    }
  }
  
  def listHot(in: NodeSeq): NodeSeq = list(in, pageHotDeals)
  def listSpecial(in: NodeSeq): NodeSeq = list(in, pageSpecialDeals)
  def listUpcoming(in: NodeSeq): NodeSeq = list(in, pageUpcomingDeals)
  def listMerchant(in: NodeSeq): NodeSeq = list(in, pageMerchantDeals)
  def listOnline(in: NodeSeq): NodeSeq = list(in, pageOnlineDeals)
  
  def list(in: NodeSeq, deals: List[Deal]): NodeSeq = {

    var dealid = ""
    var userid = ""
    var value  = ""

    def submitUp() = {
      if(S.post_?)
      {
          if(userid.length !=0 && userid.toLong > 0L) {
            Vote.voteUp(dealid.toLong,userid.toLong)
          }
      }
    }
    
    def submitDown() = {
      if(S.post_?)
      {
          if(userid.length !=0 && userid.toLong > 0L) {
            Vote.voteDown(dealid.toLong,userid.toLong)
          }
      }
    }
    if(deals.count(c=>true) == 0)
    {
      <p>Brak ofert spełniających kryteria.</p>
    } else {
      deals.flatMap(
        deal => {

          dealid = deal.id.toString
          User.currentUser match {
            case Full(user) => userid = user.id.toString
            case _ => userid = "-1"
          }    
          
          value = Vote.getVotes(deal.id).toString
          
          bind("deal", in, 
           "title" -> <a href={"/deal/"+deal.id+"/"+Deal.toPermalink(deal.title.toString)+".html"}>{deal.title}</a>,
           "text" -> deal.text,
           "expire" -> deal.expire,
           "actions" ->  {
                         <span class="actions">
                           <ul>
                             <li><a href={"/deal/"+deal.id+"/"+Deal.toPermalink(deal.title.toString)+".html#comments"}>{?("Comments")}</a></li>
                             <li>&nbsp;&nbsp;|&nbsp;&nbsp;</li>
                             <li><a href={"/deal/"+deal.id+"/"+Deal.toPermalink(deal.title.toString)+".html"}>{?("More")}</a></li>   
                           </ul>
                         </span>
                         },
            "thumb" -> <img src={Deal.imageLink(deal.imageThumb)} alt="image" title={deal.title} />,
            "image" -> <img src={Deal.imageLink(deal.imageOrigin)} alt="image" title={deal.title} />,
            "dealid" -> SHtml.text(dealid, parm => dealid=parm, ("type","hidden")),
            "userid" -> SHtml.text(userid, parm => userid=parm, ("type","hidden")),
				    "value" -> SHtml.text(value, parm => value=parm, ("readonly", "readonly")),
				    "voteup" -> SHtml.submit("+", submitUp),
				    "votedown" -> SHtml.submit("-", submitDown)
			    )
        })
    }
  }
  
  def navigation(in: NodeSeq): NodeSeq = {
    val dealId: Long = S.param("dealid").map(_.toLong) openOr 0L
    var next: Long = 0L
    var prev: Long = 0L
    val currDeal = Deal.find(By(Deal.id, dealId))
    var isPublished = false
    currDeal match {
      case Full(deal) => isPublished = deal.published
      case _ => isPublished = false
    }
    if(isPublished) {
      next = Deal.nextDeal(dealId)
      prev = Deal.prevDeal(dealId)
      bind("navigation", in, "nav" -> {
      <ul class="nav-deals">
      {if(next!=0L) Deal.toLink(Deal.getTitleById(next), next)}
      {if(prev!=0L) Deal.toLink(Deal.getTitleById(prev), prev)}
      </ul>
      })
    } else {
      println("Nieopublikowany")
      Text("")
    }
  }
   
  def adminCreate(in: NodeSeq): NodeSeq = {
    Deal.create.toForm(Full("Submit"), { _.save })
  }
  
  def adminEdit(in: NodeSeq): NodeSeq = {
    val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
    if(Deal.withIdExist_?(dealId)) {
      Deal.findAll(By(Deal.id, dealId)).head.toForm(Full("Submit"), { _.save })
    } else {
      S.redirectTo("/404.html")
    }
  }
  
  def adminList(in: NodeSeq): NodeSeq = {
    page.flatMap(
      deal => {
        bind("deal", in,
          "dealid" -> deal.id,
          "title" -> deal.title,
          "price" -> deal.price,
          "published" -> deal.published,
          "edit" -> <a href={"/admin/deals/edit/"+deal.id}>Edit</a>,
          "view" -> <a href={"/admin/deals/view/"+deal.id}>View</a>,
          "delete" -> <a href={"/admin/deals/delete/"+deal.id}>Delete</a>
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

