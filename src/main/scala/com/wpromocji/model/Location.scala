package com.wpromocji.model

import net.liftweb.mapper._

object Location extends Location with LongKeyedMetaMapper[Location] {
  
  override def dbTableName = "locations"
  
}

class Location extends LongKeyedMapper[Location] with IdPK {

  def getSingleton = Location

  object city extends MappedString(this, 64)
  object country extends MappedCountry(this)
  object latitude extends MappedDouble(this)
  object longitude extends MappedDouble(this)

}
