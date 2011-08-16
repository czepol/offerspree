package com.wpromocji.model

import net.liftweb.mapper._

object DealTag extends DealTag with MetaMapper[DealTag]

class DealTag extends Mapper[DealTag] {

  def getSingleton = DealTag
  
  object dealid extends LongMappedMapper(this, Deal)
  object tagid extends LongMappedMapper(this, Tag)

}
