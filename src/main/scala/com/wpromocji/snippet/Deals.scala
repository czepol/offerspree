package com.wpromocji {
package snippet {
import scala.xml.{NodeSeq,Text}
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full,Empty,Box}
import net.liftweb.http._
import net.liftweb.mapper._
import com.wpromocji.model.{User,Deal,Comment,Vote,Category}
import com.wpromocji.util._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.wizard._
import util._
import S.?

class DealsHot extends PaginatorSnippet[Deal] {
  override def itemsPerPage = 5
  override def count = Deal.count
  
  override def page = Deal.findAll(
    StartAt(curPage*itemsPerPage), 
    MaxRows(itemsPerPage),
    OrderBy(Deal.date, Descending)
  )
  
  override def prevXml: NodeSeq = Text("« " + ?("previous"))
  override def nextXml: NodeSeq = Text(?("next") + " »")

  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq = {
    if(first==newFirst || newFirst < 0 || newFirst >= count) {
      if(ns != Text("« " + ?("previous")) && ns != Text(?("next") + " »")) {
        <span>{ns}</span>
      } else {
        NodeSeq.Empty
      }
    } else {
      <a href={pageUrl(newFirst)}>{ns}</a>
    }
  }
  
  def list(in: NodeSeq) = Deals.list(in, page)
}

class DealsSpecial extends PaginatorSnippet[Deal] {
  override def itemsPerPage = 5
  override def count = Deal.count
  
  override def page = Deal.findAll(
    StartAt(curPage*itemsPerPage), 
    MaxRows(itemsPerPage),
    OrderBy(Deal.date, Descending)
  )
  
  override def prevXml: NodeSeq = Text("« " + ?("previous"))
  override def nextXml: NodeSeq = Text(?("next") + " »")

  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq = {
    if(first==newFirst || newFirst < 0 || newFirst >= count) {
      if(ns != Text("« " + ?("previous")) && ns != Text(?("next") + " »")) {
        <span>{ns}</span>
      } else {
        NodeSeq.Empty
      }
    } else {
      <a href={pageUrl(newFirst)}>{ns}</a>
    }
  }
  
  def list(in: NodeSeq) = Deals.list(in, page)
}

class DealsUpcoming extends PaginatorSnippet[Deal] {
  override def itemsPerPage = 5
  override def count = Deal.count(By(Deal.published, false))
  
  override def page = Deal.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    By(Deal.published, false),
    OrderBy(Deal.date, Descending)
  )
  
  override def prevXml: NodeSeq = Text("« " + ?("previous"))
  override def nextXml: NodeSeq = Text(?("next") + " »")

  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq = {
    if(first==newFirst || newFirst < 0 || newFirst >= count) {
      if(ns != Text("« " + ?("previous")) && ns != Text(?("next") + " »")) {
        <span>{ns}</span>
      } else {
        NodeSeq.Empty
      }
    } else {
      <a href={pageUrl(newFirst)}>{ns}</a>
    }
  }
  
  def list(in: NodeSeq) = Deals.list(in, page)
}

class DealsMerchant extends PaginatorSnippet[Deal] {
  override def itemsPerPage = 5
  override def count = 
    Deal.count(By(Deal.published, true),By(Deal.merchant, true))
  
  override def page = Deal.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    By(Deal.merchant, true),
    By(Deal.published, true),
    OrderBy(Deal.date, Descending)
  )
  
  override def prevXml: NodeSeq = Text("« " + ?("previous"))
  override def nextXml: NodeSeq = Text(?("next") + " »")

  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq = {
    if(first==newFirst || newFirst < 0 || newFirst >= count) {
      if(ns != Text("« " + ?("previous")) && ns != Text(?("next") + " »")) {
        <span>{ns}</span>
      } else {
        NodeSeq.Empty
      }
    } else {
      <a href={pageUrl(newFirst)}>{ns}</a>
    }
  }
  
  def list(in: NodeSeq) = Deals.list(in, page)
}

class DealsOnline extends PaginatorSnippet[Deal] {
  override def itemsPerPage = 5
  override def count = 
    Deal.count(By(Deal.online, true),By(Deal.published, true))
  
  override def page = Deal.findAll(
    StartAt(curPage*itemsPerPage),
    MaxRows(itemsPerPage),
    By(Deal.online, true),
    By(Deal.published, true),
    OrderBy(Deal.date, Descending)
  )
  
  override def prevXml: NodeSeq = Text("« " + ?("previous"))
  override def nextXml: NodeSeq = Text(?("next") + " »")

  override def pageXml(newFirst: Long, ns: NodeSeq): NodeSeq = {
    if(first==newFirst || newFirst < 0 || newFirst >= count) {
      if(ns != Text("« " + ?("previous")) && ns != Text(?("next") + " »")) {
        <span>{ns}</span>
      } else {
        NodeSeq.Empty
      }
    } else {
      <a href={pageUrl(newFirst)}>{ns}</a>
    }
  }
  
  def list(in: NodeSeq) = Deals.list(in, page)
}

class Deals {
  
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
      if(S.post_?) {
        if(userid.length !=0 && userid.toLong > 0L) {
          Vote.voteUp(dealid.toLong,userid.toLong)
        }
      }
    }
    
    def submitDown() = {
      if(S.post_?) {
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
  
  def list(in: NodeSeq, deals: List[Deal]): NodeSeq = {

    var dealid = ""
    var userid = ""
    var value  = ""

    def submitUp() = {
      if(S.post_?) {
        if(userid.length !=0 && userid.toLong > 0L) {
          Vote.voteUp(dealid.toLong,userid.toLong)
        }
      }
    }
    
    def submitDown() = {
      if(S.post_?) {
        if(userid.length !=0 && userid.toLong > 0L) {
          Vote.voteDown(dealid.toLong,userid.toLong)
        }
      }
    }
    
    if(deals.count(c=>true) == 0) {
      <p>{?("Brak ofert spełniających kryteria.")}</p>
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
      {if(next!=0L) Deal.toLink(Deal.getTitleById(next)+" →", next)}
      {if(prev!=0L) Deal.toLink("← "+Deal.getTitleById(prev), prev)}
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


object imageFile extends RequestVar[Box[FileParamHolder]](Empty)
object DealSubmit extends Wizard {
  val form = new Screen {
    val title = 
      field(?("title"), "", 
        valMinLen(3, S ? "Title too short"),
        valMaxLen(120, S ? "Title too long"))
        
    val price = 
      field(?("price"), "")
    
    val dealType = 
      radio(?("dealtype"), "", Map(?("merchant")->0,?("online")->1).keys.toList)
    
    val url = 
      field(?("url"), "")
      
    val startDate =
      field(?("start"), "", FormParam("class"->"datepicker"))
      
    val endDate = 
      field(?("end"), "", FormParam("class"->"datepicker"))
          
    var selectedCat = 0L
      
    val category = new Field {
      type ValueType = Long
      def default = 0L
      def name = ?("category")
      lazy val manifest = buildIt[Long]
      val cats = 
        List(("0","-")) ::: Category.findAll.map(cat =>(cat.id.toString, ?(cat.l10n.toString))).toList
      override def toForm = SHtml.select(cats, Empty, cat => selectedCat = cat.toLong)
    }
      
    val description = 
      textarea(?("description"), "")
      
    val imageUpload = new Field {     
      type ValueType = Box[FileParamHolder]
      def default = Empty
      def name = ?("dealimage")
      lazy val manifest = buildIt[Box[FileParamHolder]]
      override def toForm = SHtml.fileUpload(img => imageFile(Full(img)))
    }
    override def hasUploadField = true   
    
  }

  def finish() {
    import java.util.Date
    import java.text.SimpleDateFormat
    def imageSave(img: FileParamHolder, id: Long, deal: Deal): Unit = {
      import com.thebuzzmedia.imgscalr.Scalr._
      import java.io.{File,FileOutputStream,InputStream,ByteArrayInputStream}
      import javax.imageio.ImageIO
      
      val imagePath = Props.get("upload.imagepath") openOr "/src/main/webapp/images"
      val imageMaxHeight = 400
      val imageMaxWidth  = 500
      val thumbMaxHeight = 100
      val thumbMaxWidth  = 125
      
      img.file match {
        case null => println("It is null")
        case x if x.length == 0 => println("File size is 0")
        case x =>{
               
          val mime = img.mimeType
          if(mime.startsWith("image/")) {
            val ext = mime match {
              case mime: String if(mime.endsWith("jpeg"))=> "jpg"
              case mime: String if(mime.endsWith("png")) => "png"
              case mime: String if(mime.endsWith("gif")) => "gif"
              case mime: String => "jpg"
            }
            val thumbName = id.toString+"_thumb."+ext
            val imageName = id.toString+"."+ext
            
            if(ImageIO.write(resize(ImageIO.read(img.fileStream), Mode.FIT_TO_WIDTH, imageMaxWidth, imageMaxHeight), ext, new File(imagePath+"/"+imageName))) {
              deal.imageOrigin(imageName)
            }
            if(ImageIO.write(resize(ImageIO.read(img.fileStream), Mode.FIT_TO_WIDTH, thumbMaxWidth, thumbMaxHeight), ext, new File(imagePath+"/"+thumbName))) {
              deal.imageThumb(thumbName)
            }
            deal.save
          } else {
            S.error(?("notImage"))
          }
        }
      }
    }
    val deal = Deal.create
    val format = new SimpleDateFormat("dd/MM/yyyy")
    if(form.startDate.trim!="") {
      try {
        val startDate: Date = format.parse(form.startDate)
        deal.start(startDate)
      } catch {
        case _ => S.error("Bad date format")
      }
    }
    if(form.endDate.trim!="") {
      try {
        val endDate: Date =  format.parse(form.endDate)
        deal.expire(endDate)
      } catch {
        case _ => S.error("Bad date format")
      }
    }
    deal.date(new java.util.Date).title(form.title).text(form.description)
    if(form.dealType==0) {
      deal.merchant(true).online(false)
    } else {
      deal.merchant(false).online(true)
    }
    if(form.selectedCat != 0L && Category.withIdExist_?(form.selectedCat)) {
      deal.category(form.selectedCat)
    }
    User.currentUserId match {
      case Full(id) => deal.userid(id.toLong)
      case _ => println("not loggedin?")
    }
    deal.url(form.url).price(form.price).published(true)
    deal.validate 
    deal.save
    if(deal.saved_?) {
      val dealId: Long = deal.id
      imageFile.is match {
        case image => imageFile.is.map{ file => imageSave(file, dealId, deal) }   
      }
      val url = Deal.toPermalink(deal.title)
      println("***")
      println("Adres to: "+url)
      println("***")
      S.redirectTo(url)
    }
  }
}

}
}

