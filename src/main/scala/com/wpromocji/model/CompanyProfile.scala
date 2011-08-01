package com.wpromocji.model

import scala.xml.{NodeSeq, Elem}
import net.liftweb.mapper._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.http._  
import js._  
import JsCmds._  

import Helpers._

object CompanyProfile 
       extends CompanyProfile 
       with LongKeyedMetaMapper[CompanyProfile] {

}

class CompanyProfile extends LongKeyedMapper[CompanyProfile] 
      with IdPK with ManyToMany {

  def getSingleton = CompanyProfile
  
  object name extends MappedString(this, 128)
  
	/*object admins extends LongMappedMapper(this, User) {
    override def validSelectValues = 
      Full(User.findMap(OrderBy(User.id, Ascending)) {
        case u: User => Full(u.id.is -> u.userName.is)
      })
	}
	object authors extends MappedManyToMany(
BookAuthors, BookAuthors.book, BookAuthors.author, Author)

	*/
	
	object admins extends MappedManyToMany(
    CompanyAdmins, CompanyAdmins.company, CompanyAdmins.admin, User)


}
