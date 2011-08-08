package com.wpromocji.model

import net.liftweb.mapper._

object Merchant extends Merchant with LongKeyedMetaMapper[Merchant] {
  override def dbTableName = "merchants"
}

class Merchant extends LongKeyedMapper[Merchant] 
               with CreatedUpdated with IdPK {

  def getSingleton = Merchant

  object name extends MappedString(this, 128)
  object url extends MappedString(this, 128)
  object description extends MappedText(this)
  object networked extends MappedBoolean(this)
  //object meta

}

