package com.example

import akka.actor.{Props, ActorLogging, Actor, ReceiveTimeout}
import com.example.PrinterActor.{Line}

/**
  * Created by stremlenye on 11/02/16.
  */
class PrinterActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case Line(ln) =>
      println(ln)
  }
}

object PrinterActor {
  val props = Props[PrinterActor]
  case class Line(ln: String)
}
