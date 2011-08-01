package com.wpromocji.model

import net.liftweb.mapper._


object UserBadge extends UserBadge with LongKeyedMetaMapper[UserBadge] {}

class UserBadge extends LongKeyedMapper[UserBadge] with IdPK {

  def getSingleton = UserBadge

  object badge_id extends MappedLongForeignKey(this,Badge)
  object user_id extends MappedLongForeignKey(this,User)
  object date extends MappedDateTime(this)

}
