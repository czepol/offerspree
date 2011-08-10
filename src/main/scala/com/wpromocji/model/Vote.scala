package com.wpromocji.model

import java.text.SimpleDateFormat
import java.util.Date
import scala.xml.{NodeSeq}
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.common.Full
import S._

object Vote extends Vote with LongKeyedMetaMapper[Vote] 
with CRUDify[Long,Vote] {

  override def dbTableName = "votes"

  override def pageWrapper(body: NodeSeq) =
    <lift:surround with="admin" at="content">{body}</lift:surround>
  override def calcPrefix = List("admin",_dbTableNameLC)
  override def showAllMenuLocParams = LocGroup("admin") :: Nil
  override def createMenuLocParams  = LocGroup("admin") :: Nil
  override def viewMenuLocParams    = LocGroup("admin") :: Nil
  override def editMenuLocParams    = LocGroup("admin") :: Nil
  override def deleteMenuLocParams  = LocGroup("admin") :: Nil
  
  def loginAndComeBack = {
    val uri = S.uri 
    RedirectWithState("/user/login", RedirectState(() => User.loginReferer(uri))) 
  }
  
  val loggedIn = If(User.loggedIn_? _, loginAndComeBack _)
  val superUserLoggedIn = If(User.superUser_? _, {S.error("Nie masz uprawnień"); S.redirectTo("/")})
  override protected def addlMenuLocParams: List[Loc.AnyLocParam] = loggedIn :: superUserLoggedIn :: Nil

}

class Vote extends LongKeyedMapper[Vote] with IdPK {

  def getSingleton = Vote
  
  object userid extends MappedLongForeignKey(this, User)
  object dealid extends MappedLongForeignKey(this, Deal)
  object valueUp  extends MappedBoolean(this)
  object valueDown extends MappedBoolean(this)
  object date extends MappedDateTime(this) {
    val dateFormat = new SimpleDateFormat("yyyy-mm-dd, hh:mm:ss")
    override def defaultValue = new Date
  }
  object value extends MappedDouble(this) {
    override def defaultValue = 1.0
  }
  
  object ip extends MappedString(this,39) //39 - length compatible with IP.v6
  
  def getVotes(dealId: Long): Int = {
    val down: Int = Vote.count(
       By(Vote.valueUp, false),
       By(Vote.valueDown, true),
       By(Vote.dealid, dealId)
    ).toInt
    
    val up: Int = Vote.count(
       By(Vote.valueUp, true),
       By(Vote.valueDown, false),
       By(Vote.dealid, dealId)
    ).toInt
    
    val value = ((-1)*down)+((1)*up)
    return value
  }
  
  def voteUp(dealId: Long, userId: Long) = {
    val currUser = User.find(By(User.id, userId))
    currUser match {
      case Full(u) => {
        if(Vote.alreadyVoted_?(dealId, userId) == false) {
          val voteWeight = u.voteWeight
          val userIP = S.containerRequest.map(_.remoteAddress).openOr("localhost")
          val vote = Vote.create
                         .userid(userId)
                         .dealid(dealId)
                         .valueUp(true)
                         .valueDown(false)
                         .value(voteWeight.toLong)
                         .ip(userIP)
          vote.save
          val deal = Deal.find(By(Deal.id, dealId))
          var value = 0
          deal match {
            case Full(d) => {
              val current = d.value.toInt
              current match {
                case current: Int => value = current+1
                case _ => value = 0
              }
            }
            case _ => S.error(?("deal.not.found"))
          }
          deal.open_!.value(value).save
        }
      }
      case _ => S.error("Jakiś błąd"); S.redirectTo("/404.html");
    }
  }
  
  def voteDown(dealId: Long, userId: Long) = {
    val currUser = User.find(By(User.id, userId))
    currUser match {
      case Full(u) => {
        if(Vote.alreadyVoted_?(dealId, userId) == false) {
          val voteWeight = u.voteWeight
          val userIP = S.containerRequest.map(_.remoteAddress).openOr("localhost")
          val vote = Vote.create
                         .userid(userId)
                         .dealid(dealId)
                         .valueUp(false)
                         .valueDown(true)
                         .value(voteWeight)
                         .ip(userIP)
          vote.save
          val deal = Deal.find(By(Deal.id, dealId))
          var value = 0
          deal match {
            case Full(d) => {
              val current = d.value.toInt
              current match {
                case current: Int => value = current-1
                case _ => value = 0
              }
            }
            case _ => S.error(?("deal.not.found"))
          }
          deal.open_!.value(value).save
        }
      }
      case _ => S.error("Jakiś błąd"); S.redirectTo("/404.html");
    }
  }
  
  def alreadyVoted_?(dealId: Long, userId: Long): Boolean = {
    val count = Vote.count(By(Vote.dealid, dealId), By(Vote.userid, userId)).toInt
    if(count == 0)
      false
    else
      true
  }
  
}
