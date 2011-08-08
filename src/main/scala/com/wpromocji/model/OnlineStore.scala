package com.wpromocji.model

import net.liftweb.mapper._

object OnlineStore extends OnlineStore with LongKeyedMetaMapper[OnlineStore] {

  override def dbTableName = "onlinestores"

}

class OnlineStore extends LongKeyedMapper[OnlineStore] with IdPK {

  def getSingleton = OnlineStore
  
  object name extends MappedString(this, 128)
  object url extends MappedString(this, 128)
  object description extends MappedText(this)


}

