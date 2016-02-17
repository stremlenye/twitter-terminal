package com.example

import akka.actor.ActorSystem

object ApplicationMain extends App {
  val system = ActorSystem("MyActorSystem")
  val twitterStreamActor = system.actorOf(TwitterStreamActor.props, "twitterStream")
  twitterStreamActor ! TwitterStreamActor.StartStreaming
  scala.io.StdIn.readLine()
  twitterStreamActor ! TwitterStreamActor.StopStreaming
  system.shutdown()
}