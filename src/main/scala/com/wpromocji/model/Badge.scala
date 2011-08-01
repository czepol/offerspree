package com.wpromocji.model

import net.liftweb.mapper._

/**
 * Badge model
 * @author Marcin Szepczyński
 * @since 0.1
 */

object Badge extends Badge with LongKeyedMetaMapper[Badge] {

  override def dbTableName = "badges"

}

class Badge extends LongKeyedMapper[Badge] with IdPK {

  def getSingleton = Badge

	object title extends MappedString(this,200)
  object description extends MappedText(this)
  object imagePath extends MappedString(this,2048)

}

