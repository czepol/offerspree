package com.wpromocji {
package snippet {

import net.liftweb._
import http._
import common._
import util._
import Helpers._

import java.text.NumberFormat
import scala.xml.{NodeSeq, Text}

object RuntimeStats extends DispatchSnippet {
  @volatile
  var totalMem: Double = (Runtime.getRuntime.totalMemory.toDouble/(1024*1024))
 
  @volatile
  var freeMem: Double = (Runtime.getRuntime.freeMemory.toDouble/(1024*1024))

  @volatile
  var sessions = 1

  @volatile
  var lastUpdate = timeNow

  val startedAt = timeNow
  
  private def toFormat(number: Any): String = number match {
    case number: Double => "%1.2f" format  number
    case _ => number.toString
  }
  
  def dispatch = {
    case "total_mem" => i => Text(toFormat(totalMem) + " MB")
    case "free_mem" => i => Text(toFormat(freeMem) + " MB")
    case "sessions" => i => Text(sessions.toString)
    case "updated_at" => i => Text(lastUpdate.toString)
    case "started_at" => i => Text(startedAt.toString)
  }

}

}
}

