package com.wpromocji.model

import net.liftweb.mapper._

object DealTag extends DealTag with LongKeyedMetaMapper[DealTag] {

}

class DealTag extends LongKeyedMapper[DealTag] with IdPK {

  def getSingleton = DealTag
  
  object dealid extends MappedLongForeignKey(this, Deal)
  object tagid extends MappedLongForeignKey(this, Tag)

}
