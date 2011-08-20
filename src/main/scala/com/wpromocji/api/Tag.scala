package com.wpromocji {
package api {

import java.text.SimpleDateFormat
import scala.xml.{Elem, Node, NodeSeq, Text}
import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.mapper.By
import net.liftweb.common.{Box,Empty,Failure,Full,Logger}
import net.liftweb.http.rest.{RestHelper,XMLApiHelper}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.json._
import com.wpromocji.model._

object TagAPI extends RestHelper {
  serve {
    /*case Req(List("api", ver, "tag", AsLong(id)), _, GetRequest) => 
      () => Full(Merchant.toXml(id,ver))
    case Req(List("api", ver, "merchant", AsLong(id), "json"), _, GetRequest) => 
      () => Full(new JsonResponse(Merchant.toJson(id,ver),("Content-Type" -> "text/plain") :: Nil, Nil, 200))*/
    case Req(List("api", "tags", query), _, _) =>
      () => Full(new JsonResponse(Tag.autocompleteJArray(query), ("Content-Type" -> "text/plain") :: Nil, Nil, 200)) 
    case Req(List("api", "alltags"), _, _) =>
      () => Full(new JsonResponse(Tag.autocompleteJArrayAllTags, ("Content-Type" -> "text/plain") :: Nil, Nil, 200))
    case Req(List("api", "dealtags", AsLong(id)), _, _) =>
      () => Full(new JsonResponse(Tag.tagsToDealJArray(id), ("Content-Type" -> "text/plain") :: Nil, Nil, 200))
  }
}

}
}
