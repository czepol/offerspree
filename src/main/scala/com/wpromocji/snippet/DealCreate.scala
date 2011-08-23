package com.wpromocji {
package snippet {

import scala.xml.{NodeSeq,Text,Elem}
import java.text.SimpleDateFormat
import java.util.Date
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full,Empty,Box}
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util._
import com.wpromocji.model._
import com.wpromocji.util._
import util._
import S.?

class DealCreate {
  object imageFile extends RequestVar[Box[FileParamHolder]](Empty)

  object dealId extends SessionVar[Long](0L)

  object titleInSession extends SessionVar[String]("")
  object titleValErr extends SessionVar[String]("")
  
  object priceInSession extends SessionVar[String]("")
  object priceValErr extends SessionVar[String]("")
  
  object urlInSession extends SessionVar[String]("")
  object urlValErr extends SessionVar[String]("")
  
  object dealtypeInSession extends SessionVar[String]("")
  object dealtypeValErr extends SessionVar[String]("")
  
  object merchantInSession extends SessionVar[String]("")
  object merchantValErr extends SessionVar[String]("")
  
  object descriptionInSession extends SessionVar[String]("")
  object descriptionValErr extends SessionVar[String]("")
  
  object categoryInSession extends SessionVar[String]("")
  object categoryValErr extends SessionVar[String]("")
  
  object tagsInSession extends SessionVar[String]("")
  
  object startInSession extends SessionVar[String]("")
  object endInSession extends SessionVar[String]("")

  def validationBox(v: SessionVar[String]): NodeSeq = {
    if(v.is=="")
      NodeSeq.Empty
    else
      <span class="help-inline">{v.is}</span>
  }

  var title = titleInSession.is
  val titleField = SHtml.text(title, title=_, ("class","title full"))
  def titleError: NodeSeq = validationBox(titleValErr)
  
  var price = priceInSession.is
  val priceField = SHtml.text(price, price=_, ("class","price small"))
  def priceError: NodeSeq = validationBox(priceValErr)
  
  var url = urlInSession.is
  val urlField = SHtml.text(url, url=_, ("class","url xlarge"))
  def urlError: NodeSeq = validationBox(urlValErr)
  
  var dealtype = ""
  var dealtypeDefault: Box[String] = {
    if(dealtypeInSession.is!="") {
      Full(dealtypeInSession.is)
    } else {
      Empty
    }
  }
  val dealtypes = Map("merchant" -> 0, "online" -> 1)
  val dealtypeField = SHtml.radio(dealtypes.keys.toList, dealtypeDefault, dt=>dealtype=dt.toString).
    flatMap(c => (<li class={c.key.toString}><label>{c.xhtml} <span>{?(c.key.toString)}</span></label></li>))
  def dealtypeError: NodeSeq = validationBox(dealtypeValErr)
  
  var merchant = merchantInSession.is
  val merchantField = SHtml.text(merchant, merchant=_, ("class","merchant"))
  def merchantError: NodeSeq = validationBox(merchantValErr)
  
  var description = descriptionInSession.is
  val descriptionField = SHtml.textarea(description, description=_, ("class","description full"),("rows","7"))
  def descriptionError: NodeSeq = validationBox(descriptionValErr)
  
  var tags = tagsInSession.is
  val tagsField = SHtml.text(tags, tags=_, ("class","tags xlarge"))
  
  val categories = 
    List(("0",?("select.category"))) ::: Category.findAll.map(cat => (cat.id.toString, ?(cat.l10n.toString))).toList
  var category = ""
  val categoryField = SHtml.select(categories, Empty, cat=>category=cat.toString, ("class","merchant"))
  val categoryError = validationBox(categoryValErr)
  
  val imageField = SHtml.fileUpload(img => imageFile(Full(img)))
  
  var startDate = startInSession.is
  val startDateField = SHtml.text(startDate, startDate=_, ("class","startdate datepicker"))
  
  var endDate = endInSession.is
  val endDateField = SHtml.text(endDate, endDate=_, ("class","enddate datepicker"))
  
  val submit = SHtml.submit(?("submit"), finish, ("class","btn primary"))
  val reset = SHtml.submit(?("resetForm"), resetForm, ("class", "btn resetForm"))
  
  lazy val formatter = new java.text.SimpleDateFormat("dd/MM/yyyy")
  
  def finish() {
    if(S.post_?) {
      val deal = Deal.create.date(new java.util.Date)
      titleInSession(title)
      priceInSession(price)
      urlInSession(url)
      merchantInSession(merchant)
      descriptionInSession(description)
      tagsInSession(tags)
      startInSession(startDate)
      endInSession(endDate)
      dealtypeInSession(dealtype)
      
      resetValVars // reset SessionVars for validation
      
      if(title.length <= 3) {
        titleValErr("Tytuł jest za krótki")
      } else if(title.length > 128) {
        titleValErr("Tytuł jest za długi")
      } else {      
        deal.title(title)
      }
      
      if(description.length <= 12) {
        descriptionValErr("Opis oferty jest za krótki")
      } else {
        deal.text(description)
      }
      
      if(price.length == 0) {
        priceValErr("Cena nieprawidłowa")
      } else {
        deal.price(price)
      }
        
      // date parsing
      if(startDate!="") {
        formatter.parse(startDate) match {
          case date: Date => deal.start(date)
          case _ => 
        }
      }
      if(endDate!="") {
        formatter.parse(endDate) match {
          case date: Date => deal.expire(date)
          case _ => 
        }
      }
      if(dealtype=="") {
        dealtypeValErr("Nie wybrano typu oferty")
      } else {
        if(dealtype=="merchant") {
          deal.merchant(true).online(false)
          if(merchant=="") {
            merchantValErr("Nie wpisano nazwy sklepu")
          } else {
            Merchant.find(By(Merchant.name, merchant)) match {
              case Full(merchant) => deal.merchantid(merchant.id)
              case _ => {
                val m = Merchant.create.name(merchant.trim)
                m.save
                deal.merchantid(m.id)
              }
            }
          }
        } else if(dealtype=="online") {
          deal.merchant(false).online(true)
          if(url == "") {
            urlValErr("Nie wpisano adresu URL prowadzącego do oferty")
          } else if(url.length<10) {
            urlValErr("Adres URL wygląda na nieprawidłowy")
          } else {
            deal.url(url)
          }
        }
      }
      
      var catLong = tryo(category.toLong) match {
        case Full(c) => c
        case _ => 0L
      }
      
      if(catLong==0L) {
        categoryValErr("Nie wybrano kategorii")
      } else if(!Category.withIdExist_?(catLong)) {
        categoryValErr("Kategoria nie istnieje")
      } else {
        deal.category(catLong)
      }
      
      deal.validate match {
        case Nil => {
          if(tags!="") {
            Deals.updateTags(deal,tags)
          }
          deal.save
          dealId(deal.id)
          imageFile.is match {
            case Full(img) => {
              if(uploadImage(img, deal)) {
                S.redirectTo(Deal.absLink(deal.id))
              }
            }
            case _ => S.redirectTo(Deal.absLink(deal.id))
          }
        }
        case _ =>
      }
    }
  }
  
  def resetForm() {
    titleInSession("")
    priceInSession("")
    urlInSession("")
    merchantInSession("")
    descriptionInSession("")
    tagsInSession("")
    startInSession("")
    endInSession("")
    dealtypeInSession("")
    resetValVars
  }
  
  def resetValVars() {
    titleValErr("")
    priceValErr("")
    urlValErr("")
    dealtypeValErr("")
    merchantValErr("")
    descriptionValErr("")
    categoryValErr("")
  }
  
  def merchantAutocomplete(q: String) = {
    val input = q.toLowerCase.replaceAll("\"", "").replaceAll("'", "")+"%"
    val query = "SELECT name FROM merchants WHERE lower(name) LIKE '%s' LIMIT 10".format(input)
    val results = Merchant.findAllByInsecureSql(query, IHaveValidatedThisSQL("czepol", "2011-08-22"))
    var output = List[String]()
    for(row <- results) {
      output = output ::: List(row.name.is)
    }
    output
  }

  def uploadImage(img: FileParamHolder, deal: Deal): Boolean = {
    import com.thebuzzmedia.imgscalr.Scalr._
    import java.io.{File,FileOutputStream,InputStream,ByteArrayInputStream}
    import javax.imageio.ImageIO
    
    val imagePath = Props.get("upload.imagepath") openOr "/src/main/webapp/images"
    
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
          val thumbName = deal.id.toString+"_thumb."+ext
          val imageName = deal.id.toString+"."+ext
          
          if(ImageIO.write(ImageIO.read(img.fileStream), ext, new File(imagePath+"/"+imageName))) {
            deal.imageOrigin(imageName)
          }
          if(ImageIO.write(resize(ImageIO.read(img.fileStream), Mode.FIT_TO_WIDTH, thumbMaxWidth, thumbMaxHeight), ext, new File(imagePath+"/"+thumbName))) {
            deal.imageThumb(thumbName)
          }
          deal.save
          return deal.saved_?
        } else {
          S.error(?("notImage"))
        }
      }
    }
    false
  }

  def form(in: NodeSeq): NodeSeq = {    
    bind("form", in,
      "title" -> titleField, 
      "titleError" -> titleError,
      "price" -> priceField, 
      "priceError" -> priceError,
      "url" -> urlField, 
      "urlError" -> urlError,
      "merchant" -> merchantField, 
      "merchantError" -> merchantError,
      "dealtype" -> dealtypeField, 
      "dealtypeError" -> dealtypeError,
      "category" -> categoryField,
      "categoryError" -> categoryError,
      "tags" -> tagsField,
      "description" -> descriptionField, 
      "descriptionError" -> descriptionError,
      "start" -> startDateField,
      "end" -> endDateField,
      "upload" -> imageField,
      "submit" -> submit,
      "reset" -> reset)
  }
}

}
}
