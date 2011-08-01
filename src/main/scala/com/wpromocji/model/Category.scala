package com.wpromocji.model

import net.liftweb.mapper._
import net.liftweb.common.{Full}


/**
 * Category model
 * @author Marcin Szepczyński
 * @since 0.1
 */

object Category extends Category with LongKeyedMetaMapper[Category] {

  override def dbTableName = "categories"

}

class Category extends LongKeyedMapper[Category] 
               with IdPK 
               with OneToMany[Long, Category] {

  def getSingleton = Category

  object title extends MappedString(this, 128)
  object l10n extends MappedString(this, 64)
  
  object slug extends MappedString(this, 64) {
    override def validations = valUnique("Slug must be unique") _ :: super.validations
  }
  
  object description extends MappedText(this)
  
  object imagePath extends MappedString(this, 1024) 

  object deals extends MappedOneToMany(Deal, Deal.category, 
    OrderBy(Deal.start, Descending)) with Owned[Deal] with Cascade[Deal] 

  def withIdExist_?(categoryId: Long): Boolean = {
    Category.find(By(Category.id, categoryId)) match {
      case Full(category) => true
      case _ => false
    }
  }

}
