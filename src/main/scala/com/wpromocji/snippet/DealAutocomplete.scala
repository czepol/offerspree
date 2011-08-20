package com.wpromocji {
package snippet {

import scala.xml.NodeSeq
import net.liftweb.util.Helpers._
import net.liftweb.widgets.autocomplete.AutoComplete
import net.liftweb.mapper._
import com.wpromocji.model.Deal

class DealAutocomplete {
  private var data = List(
    "Timothy","Derek","Ross","Tyler",
    "Indrajit","Harry","Greg","Debby")

  def sample(xhtml: NodeSeq): NodeSeq = 
    bind("f", xhtml, 
      "find_name" -> AutoComplete("", (current,limit) =>
        data.filter(_.toLowerCase.startsWith(current.toLowerCase)),
        value => println("Submitted: " + value))
    )
    
  def deals(q: String) = {
    val input = q.toLowerCase.replaceAll("\"", "").replaceAll("'", "")+"%"
    val query = "SELECT title FROM deals WHERE lower(title) LIKE '%s'".format(input)
    val results = Deal.findAllByInsecureSql(query, IHaveValidatedThisSQL("czepol", "2011-08-17"))
    var output = List[String]()
    for(row <- results) {
      output = output ::: List(row.title.is)
    }
    output
  }
    
  def adv(in: NodeSeq): NodeSeq = 
    bind("f", in,
      "find_name" -> AutoComplete("", (current,limit) =>
        deals(current).filter(_.toLowerCase.startsWith(current.toLowerCase)),
        value => println("Submitted: " + value))
    )
}

}
}
