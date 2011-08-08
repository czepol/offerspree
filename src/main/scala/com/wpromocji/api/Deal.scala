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

object DealAPI extends RestHelper {
  serve {
    case Req(List("api", ver, "deal", AsLong(id)), _, GetRequest) => 
      () => Full(Deal.toXml(id,ver))
    case Req(List("api", ver, "deal", AsLong(id), "json"), _, GetRequest) => 
      () => Full(new JsonResponse(Deal.toJson(id,ver),("Content-Type" -> "text/plain") :: Nil, Nil, 200))
    /*case Req(List("api", ver, "deals"), _, GetRequest) =>
      () => Full(*/
  }
}

}
}
