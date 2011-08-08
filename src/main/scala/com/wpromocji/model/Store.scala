package com.wpromocji.model

import net.liftweb.mapper._

object Store extends Store with LongKeyedMetaMapper[Store] {
  
  override def dbTableName = "stores"

}

class Store extends LongKeyedMapper[Store] with IdPK {

  def getSingleton = Store

  object name extends MappedString(this, 128)
  object description extends MappedText(this)
  object country extends MappedCountry(this)
  object postalcode extends MappedPostalCode(this, country)
  object city extends MappedString(this, 64)
  object address extends MappedString(this, 64)
  object latitude extends MappedDouble(this)
  object longitude extends MappedDouble(this)
  
  //object merchantId extends 
  


}

