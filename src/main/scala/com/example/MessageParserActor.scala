package com.example

import akka.actor.{Props, ActorLogging, Actor}
import com.example.MessageParserActor.MessageReceived
import org.json4s._
import org.json4s.native.JsonMethods._

/**
  * Created by stremlenye on 12/02/16.
  */
class MessageParserActor extends Actor with ActorLogging {
  import com.example.Readers._

  val printer = context.actorOf(PrinterActor.props, "printer")

  override def receive: Receive = {
    case MessageReceived(msg) => parseOpt(msg) flatMap (_.getAs[Tweet]) foreach { tweet => printer ! PrinterActor.Line(
      s"""
         |From: ${tweet.user.name}
         |\t${tweet.text.replace("\n","\n\t")}
       """.stripMargin)}
  }
}

object MessageParserActor {
  val props = Props[MessageParserActor]
  case class MessageReceived(message: String)
}