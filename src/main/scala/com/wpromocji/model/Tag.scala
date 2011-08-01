package com.wpromocji.model

import net.liftweb.mapper._

object Tag extends Tag with LongKeyedMetaMapper[Tag] {

  override def dbTableName = "tags"
  override def fieldOrder=List(name)
}

class Tag extends LongKeyedMapper[Tag] with IdPK {

  def getSingleton = Tag

  object name extends MappedPoliteString(this,120) {
		override def setFilter = List(x=> x.trim)
  }

}
