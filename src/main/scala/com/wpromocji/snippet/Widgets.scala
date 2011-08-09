package com.wpromocji {
package snippet {
import scala.xml.{NodeSeq,Text}
import net.liftweb.util.Helpers._
import net.liftweb.common.{Full,Empty}
import net.liftweb.http._
import net.liftweb.mapper._
import com.wpromocji.model.{Deal,Vote}
import net.liftweb.util.Helpers._
import util._
import S.?

class Widgets {

  private val count = 5
  private val maxTitleLenght = 25
  private val moreTitleText = " ..."
  
  /*def hotByAll(in: NodeSeq): NodeSeq = {       
    val deals = Deal.findAll(
                  OrderBy(Deal.value, Descending),
                  MaxRows(count)
                )
    deals.flatMap(deal => {
      bind("deal", in,
        "title" -> <a href={"/deal/"+deal.id+"/"+Deal.toPermalink(deal.title.toString)+".html"}>{(deal.title.toString.substring(0, maxTitleLenght) + moreTitleText)}</a>,
        "value" -> deal.value.toString
      )
    })
  }*/

}

}
}
