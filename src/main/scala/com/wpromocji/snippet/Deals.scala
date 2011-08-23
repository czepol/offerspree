package com.wpromocji {
package snippet {
import scala.xml.{NodeSeq,Text,Elem}
import java.text.SimpleDateFormat
import java.util.Date
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full,Empty,Box}
import net.liftweb.http._
import net.liftweb.mapper._
import com.wpromocji.model._
import com.wpromocji.util._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.wizard._
import net.liftweb.widgets.autocomplete.AutoComplete
import util._
import S.?


class DealsHot extends Deals with PaginatorSnippet[Deal] {
  override def itemsPerPage = 5
  override def count = Deal.count(By(Deal.published, true))
  
  override def page = Deal.findAll(
    StartAt(curPage*itemsPerPage), 
    MaxRows(itemsPerPage),
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
  
  def list(in: NodeSeq): NodeSeq = super.list(in, page)
}

class DealsSpecial extends Deals with PaginatorSnippet[Deal] {
  override def itemsPerPage = 5
  override def count = Deal.count
  
  override def page = Deal.findAll(
    StartAt(curPage*itemsPerPage), 
    MaxRows(itemsPerPage),
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
  
  def list(in: NodeSeq): NodeSeq = super.list(in, page)
}

class DealsUpcoming extends Deals with PaginatorSnippet[Deal] {
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
  
  def list(in: NodeSeq): NodeSeq = super.list(in, page)
}

class DealsMerchant extends Deals with PaginatorSnippet[Deal] {
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
  
  def list(in: NodeSeq): NodeSeq = super.list(in, page)
}

class DealsOnline extends Deals with PaginatorSnippet[Deal] {
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
  
  def list(in: NodeSeq): NodeSeq = super.list(in, page)
}

class Deals {
  
  def edit(in: NodeSeq): NodeSeq = {
    val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
    lazy val formatter = new java.text.SimpleDateFormat("dd/MM/yyyy")
    def submit() {
    
    }
    Deal.find(By(Deal.id, dealId)) match {
      case Full(deal) => {
        var title = deal.title.is
        var price = deal.price.is
        var d: Date = new java.util.Date
        d = deal.start.is
        var startDate = d match {
          case date: Date => formatter.format(date)
          case _ => ""
        }
        d = deal.expire.is
        var endDate = d match {
          case date: Date => formatter.format(date)
          case _ => ""
        }
        var description = deal.text.is
        var url = deal.url.is
        var tags = Tag.tagsToString(deal)
        var selectedCat = 0L
        val cats = 
          List(("0","-")) ::: Category.findAll.map(cat =>(cat.id.toString, ?(cat.l10n.toString))).toList
        
        def imageBox =
          <div class="image" style="text-align:center">
            <img src={Deal.imageLink(deal.imageThumb.is)} alt="image" />
            <p><em>{?("currentImage")}</em></p>
          </div>
        
        bind("deal", in, 
          "title" -> SHtml.text(title, title= _, ("id","title"),("class","title")),
          "price" -> SHtml.text(price, price= _, ("id","price"),("class","price")),
          "url"   -> SHtml.text(url, url= _, ("id","url"),("class","url")),
          "start" -> SHtml.text(startDate, startDate= _, ("id","start"),("class","start datepicker")),
          "end"   -> SHtml.text(endDate, endDate= _, ("id","end"),("class","end datepicker")),
          "tags"  -> SHtml.text(tags, tags = _, ("id","tags"),("class","tags")),
          "cats"  -> SHtml.select(cats, Empty, cat => selectedCat = cat.toLong, ("id","cats"),("class","cats")),
          "upload"-> SHtml.fileUpload(img => imageFile(Full(img))),
          "imagebox" -> imageBox,
          "submit" -> SHtml.submit(?("submit"), submit)
        )
      }
      case _ => in
    }
  }
  
  /*def edit(in: NodeSeq): NodeSeq = {
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
	}*/
  
  def show(in: NodeSeq): NodeSeq = {
    val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
    val slug = S.param("slug").map(_.toString) openOr ""
    var dealid = dealId.toString
    var userid = ""
    var value  = Vote.getVotes(dealId).toString
    var tags: List[NodeSeq] = Nil
    var deal: Deal = Deal.create
    
    Deal.find(By(Deal.id, dealId)) match {
      case Full(curr) => deal=curr
      case _ => S.error(?("Nie znaleziono oferty")); S.redirectTo("/error")
    }
    
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
    
    for(t <- deal.tags) {
      tags = tags ::: List(<a href={"/tag/"+t.slug.is}>{t.name.is}</a>)
    }
    
    val tagged: NodeSeq = {
      <ul>
      {
        tags.map {
          tag => <li>{tag}</li>
        } toSeq
      }
      </ul>
    }

    bind("deal", in, 
      "title" -> deal.title,
      "titleLink" -> <a href={"/deal/"+deal.id+"/"+Deal.toPermalink(deal.title.toString)+".html"}>{deal.title}</a>,
      "text" -> deal.text,
      "tags" -> tagged,
      "expire" -> deal.expire,
      "thumb" -> <img src={Deal.imageLink(deal.imageThumb)} alt="image" title={deal.title} />,
      "image" -> <img src={Deal.imageLink(deal.imageOrigin)} alt="image" title={deal.title} />,
      "dealid" -> SHtml.text(dealid, parm => dealid=parm, ("type","hidden")),
      "userid" -> SHtml.text(userid, parm => userid=parm, ("type","hidden")),
	    "value" -> SHtml.text(value, parm => value=parm, ("readonly", "readonly")),
	    "voteup" -> SHtml.submit("+", submitUp),
	    "votedown" -> SHtml.submit("-", submitDown)
    )    
  }
  
  def storeInfoBox(in: NodeSeq): NodeSeq = {
    val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
    Deal.find(By(Deal.id, dealId)) match {
      case Full(deal) => 
        if(deal.merchant==true) {
          bind("store", chooseTemplate("storetype", "merchant", in), "merchant" -> <h4>To jest test. Merchant store.</h4>)
        } else if(deal.online==true) {
          bind("store", chooseTemplate("storetype", "online", in), "online" -> <h4>To jest test. Online store.</h4>)
        } else {
          in
        }
      case _ => in
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
  
  def nextPrevDeals(in: NodeSeq): NodeSeq = {
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
      <div class="nav-deals">
      {if(next!=0L) <span style="float: right">{Deal.toLink(Deal.getTitleById(next)+" →", next)}</span>}
      {if(prev!=0L) <span style="float: left">{Deal.toLink("← "+Deal.getTitleById(prev), prev)}</span>}
      </div>
      })
    } else {
      println("Nieopublikowany")
      Text("")
    }
  }
  
  def updateTags(deal: Deal, stringTags: String) = {
    var currTagsList: List[Tag] = deal.tags.toList
    println("");
    println("Obecne tagi: "+currTagsList)
    println("");
    for(tag <- currTagsList) {
      deal.tags -= tag
      tag.deals -= deal
      tag.save
    }
    deal.save
    val splited = stringTags.split(",")
    var nTag = Tag.create
    for(tag <- splited) {
      if(tag.trim.length != 0) {
        nTag = Tag.find(By(Tag.name, tag.trim)) match {
          case Full(tagObj) => println("Znalazłem tag: "+ tagObj); tagObj
          case _ => Tag.create.name(tag.trim).slug(tag.trim)
        }
        println("")
        println(nTag)
        println("")
        nTag.save
        deal.tags  += nTag        
      }
    }
    deal.save
  }
  
  def dealTagsForTagIt(in: NodeSeq): NodeSeq = {
    val dealId: Long = S.param("dealid").map(_.toLong) openOr 0L
    Deal.find(By(Deal.id, dealId)) match {
      case Full(deal) => {
        val predefined = deal.tags.toList
        def tags = {
          <ul id="tagit-tags" style="display:none">
          {predefined.map {
              tag => <li>{tag.name}</li>
            } toSeq}
          </ul>
        }
        bind("deal", in, "tags" -> tags)
      }
      case _ => in
    }
  }
}


object imageFile extends RequestVar[Box[FileParamHolder]](Empty)

object DealSubmit extends Wizard {
  val form = new Screen {
  
    override def cancelButton: Elem = <button class="btn">{S.?("Cancel")}</button>  
    override def finishButton: Elem = <button class="btn primary">{S.?("Finish")}</button>  
  
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
    
    val merchant = 
      field(?("merchant"), "", "class"->"autocomplete")
      
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
    
    val tags = 
      field(?("tags"), "")
      
    val description = 
      textarea(?("description"), "")
      
    val imageUpload = new Field {     
      type ValueType = Box[FileParamHolder]
      def default = Empty
      def name = ?("dealimage")
      lazy val manifest = buildIt[Box[FileParamHolder]]
      override def toForm = SHtml.fileUpload(img => imageFile(Full(img)))
    }
    
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
    def saveTags(stringTags: String, deal: Deal) {
      val splited = stringTags.split(",")
      var tags: List[Tag] = Nil
      var tagsList: List[String] = Nil
      for(t <- splited) {
        if(t.trim.length != 0)
          tagsList = tagsList ::: List(t.trim) 
      }
      tagsList = tagsList.removeDuplicates
      
      println("Lista tagów:")
      println(tagsList)
      
      for(t <- tagsList) {
        val tag: Tag = Tag.find(By(Tag.name, t)) match {
          case Full(tag) => tag
          case Empty => Tag.create.name(t)
          case _ => S.error("Error"); S.redirectTo("/error")
        }
        tag.deals += deal
        tag.save
        deal.tags += tag
        deal.save
        
      }
      println("Lista tagów: ")
      println(deal.tags)

    }
    val deal = Deal.create
    val format = new SimpleDateFormat("dd/MM/yyyy")
    // val tags = List[String]
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
      saveTags(form.tags, deal)
      val url = "/deal/"+dealId.toString+"/"+Deal.toPermalink(deal.title)+".html"
      S.redirectTo(url)
    }
  }
}

object DealEdit extends Wizard {
  val dealId = S.param("dealid").map(_.toLong) openOr S.redirectTo("/404.html")
  Deal.find(By(Deal.id, dealId)) match {
    case Full(deal) => {    
      val form = new Screen {
        override def cancelButton: Elem = <button>{S.?("Cancel")}</button>  
        override def finishButton: Elem = <button>{S.?("Finish")}</button>
        
        val title = 
          field(?("title"), deal.title.is, 
            valMinLen(3, S ? "Title too short"),
            valMaxLen(120, S ? "Title too long"))
            
        val price = 
          field(?("price"), deal.price.is)
        
        if(deal.merchant == true) {
          val dealType = 
            radio(?("dealtype"), ?("merchant"), Map(?("merchant")->0,?("online")->1).keys.toList)
        } else if(deal.online == true) {
          val dealType = 
            radio(?("dealtype"), ?("online"), Map(?("merchant")->0,?("online")->1).keys.toList)
        } else {
          val dealType = 
            radio(?("dealtype"), "", Map(?("merchant")->0,?("online")->1).keys.toList)
        }
        
        val url = 
          field(?("url"), deal.url.is)
        
        val merchant = 
          field(?("merchant"), "", "class"->"autocomplete")
                    
        val startDate = new Field {
          type ValueType = String 
          override def name = ?("start") 
          override implicit def manifest = buildIt[String]
          override def default = "" 
          override def toForm: Box[NodeSeq] = SHtml.text(default, set _, ("class","datepicker")) 
        }
          
        val endDate = new Field {
          type ValueType = String 
          override def name = ?("end") 
          override implicit def manifest = buildIt[String]
          override def default = "" 
          override def toForm: Box[NodeSeq] = SHtml.text(default, set _, ("class","datepicker")) 
        }
              
        var selectedCat = 0L
          
        val category = new Field {
          type ValueType = Long
          def default = deal.category
          def name = ?("category")
          lazy val manifest = buildIt[Long]
          val cats = 
            List(("0","-")) ::: Category.findAll.map(cat =>(cat.id.toString, ?(cat.l10n.toString))).toList
          override def toForm = SHtml.select(cats, Empty, cat => selectedCat = cat.toLong)
        }
            
        val tags = new Field {
          type ValueType = String 
          override def name = ?("tags") 
          override implicit def manifest = buildIt[String] 
          override def default = Tag.tagsToString(deal) 
          override def toForm: Box[NodeSeq] = SHtml.text(default, set _, ("class","tags")) 
        }
        
        /*val tags = new Field { 
          type ValueType = String 
          override def name = "tags" 
          override implicit def manifest = buildIt[String] 
          override def default = "" 
          val predefined = deal.tags.toList
          def tags = {
            <ul id="tags" name="tags">
            {predefined.map {
                tag => <li>{tag.name}</li>
              } toSeq}
            </ul>
          }
          override def toForm: Box[NodeSeq] = tags 
        }*/
          
        val description = 
          textarea(?("description"), deal.text)
          
        val imageUpload = new Field {     
          type ValueType = Box[FileParamHolder]
          def default = Empty
          def name = ?("dealimage")
          lazy val manifest = buildIt[Box[FileParamHolder]]
          override def toForm = SHtml.fileUpload(img => imageFile(Full(img)))
        }
        
        val imageBox = new Field {
          type ValueType = String 
          override def name = ?("dealimagecurrent") 
          override implicit def manifest = buildIt[String]
          override def default = Deal.imageLink(deal.imageThumb.is)
          def imageBox: Box[NodeSeq] = 
            <div class="image"><img src={default} alt="image" /></div>
          override def toForm =  imageBox
        }
      }
    }
    case _ => S.redirectTo("/404.html")
  }
  def finish() {
  }
}

object Deals extends Deals

}
}
