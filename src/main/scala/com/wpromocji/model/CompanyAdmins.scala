package com.wpromocji.model

import net.liftweb.mapper._

object CompanyAdmins extends CompanyAdmins with MetaMapper[CompanyAdmins]

class CompanyAdmins extends Mapper[CompanyAdmins] {
 
  def getSingleton = CompanyAdmins
 
  object admin extends LongMappedMapper(this, User)
  object company extends LongMappedMapper(this, CompanyProfile)

}
