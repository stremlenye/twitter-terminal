package com.example

import akka.actor.{Props, ActorLogging, Actor, ReceiveTimeout}
import com.example.PrinterActor.TweetRecieved

/**
  * Created by stremlenye on 11/02/16.
  */
class PrinterActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case TweetRecieved(tweet) =>
      println(tweet)
  }
}

object PrinterActor {
  val props = Props[PrinterActor]
  case class TweetRecieved(tweet: String)
}
