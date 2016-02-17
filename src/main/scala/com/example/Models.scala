package com.example

import org.json4s._
import org.json4s.DefaultReaders._

import scala.util.Try

/**
  * Created by stremlenye on 12/02/16.
  */
case class Tweet (text: String, user: User)
case class User (name: String)

object Readers {

  private def tryParse[T](x: => T): T = Try{x} recover {
    case e: NoSuchElementException => throw new RuntimeException(s"Couldn't parse json")
  } get

  implicit object UserReads extends Reader[User] {
    override def read(value: JValue): User = tryParse {
      (value match {
        case obj: JObject => (obj \ "name").getAs[String].map(User)
        case _ => None
      }).get
    }
  }

  implicit object TweetReads extends Reader[Tweet] {
    override def read(value: JValue): Tweet = tryParse {
      (value match {
        case obj: JObject => Some(obj)
        case _ => None
      }) flatMap { obj => for (
          text ← (obj \ "text").getAs[String];
          user ← (obj \ "user").getAs[User]
        ) yield Tweet(text, user)
      } get
    }
  }
}