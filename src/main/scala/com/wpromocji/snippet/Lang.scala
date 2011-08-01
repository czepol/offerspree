package com.wpromocji {
package snippet {

import scala.xml.{NodeSeq}
import net.liftweb.common.{Full,Empty,Box}
import net.liftweb.util.Helpers._
import net.liftweb.http._
import net.liftweb.http.provider.HTTPCookie

class Lang {

  private val enabledLocales = List("en", "pl")

  def change: NodeSeq = {
    val whence = S.referer openOr "/"
    val locale = S.param("lang").map(_.toString) openOr S.redirectTo("/")
    if(enabledLocales.contains(locale)) {
      S.deleteCookie("locale")
      S.addCookie(HTTPCookie("locale", locale))
    } 
    S.redirectTo(whence)  
  }

}

}
}
