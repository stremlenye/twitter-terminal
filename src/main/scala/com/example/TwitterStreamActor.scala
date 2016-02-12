package com.example

import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import scala.concurrent.duration._
import akka.actor.{ReceiveTimeout, Props, ActorLogging, Actor}
import com.example.TwitterStreamActor.{PollTheStream, StopStreaming, StartStreaming}
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.event.Event
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.core.{HttpHosts, Constants}
import com.twitter.hbc.core.endpoint.{StatusesSampleEndpoint, UserstreamEndpoint, StatusesFilterEndpoint}
import com.twitter.hbc.httpclient.BasicClient
import com.twitter.hbc.httpclient.auth.OAuth1

/**
  * Created by stremlenye on 11/02/16.
  */
class TwitterStreamActor extends Actor with ActorLogging {

  var client: Option[BasicClient] = None
  val msgQueue = new LinkedBlockingQueue[String](100000)
  val eventQueue = new LinkedBlockingQueue[Event](1000)
  context.setReceiveTimeout(5 seconds)
  val printer = context.actorOf(PrinterActor.props, "printer")

  override def receive: Receive = {
    case StartStreaming =>
      val hosebirdHosts = HttpHosts.SITESTREAM_HOST
      val hosebirdEndpoint = new StatusesSampleEndpoint()
      val hosebirdAuth = new OAuth1(
        "gHPvEU5JmjdKmySu9SVaPTYAF", // consumerKey
        "POB161NfpHkoDl4TKp97eE8H0BAaXD8VjfgwuUwG1bBEfIRDKA", // consumerSecret
        "84540044-0hrab0Dd2RqkMfzI3xNAD2OpjIGSjXYkB7FbDe2fB", // token
        "iaBk1io5BOlx9XShvhLXp8nuA0udMMrH6uAb2dtZs72VY") // tokenSecret

      val hosebirdClient = new ClientBuilder()
        .name("Hosebird-Client-01")
        .hosts(hosebirdHosts)
        .authentication(hosebirdAuth)
        .endpoint(hosebirdEndpoint)
        .processor(new StringDelimitedProcessor(msgQueue))
        .eventMessageQueue(eventQueue)
        .build()
      hosebirdClient.connect()
      client = Some(hosebirdClient)
      self ! PollTheStream


    case PollTheStream =>
      client.filter(!_.isDone).foreach(_ => {
        val msg = Option(msgQueue.poll(2, TimeUnit.SECONDS))
        printer ! PrinterActor.TweetRecieved(msg.getOrElse("No messages received"))
      })

    case StopStreaming =>
      client.map(c => {
        c.stop(1000)
        None
      })

    case ReceiveTimeout => self ! PollTheStream
  }
}

object TwitterStreamActor {
  val props = Props[TwitterStreamActor]
  case object StartStreaming
  case object StopStreaming
  case object PollTheStream
}

