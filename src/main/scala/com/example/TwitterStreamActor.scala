package com.example

import java.util.concurrent.{TimeUnit, LinkedBlockingQueue}
import scala.concurrent.duration._
import akka.actor.{ReceiveTimeout, Props, ActorLogging, Actor}
import com.example.TwitterStreamActor.{PollTheStream, StopStreaming, StartStreaming}
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.event.Event
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.core.{HttpHosts}
import com.twitter.hbc.core.endpoint._
import com.twitter.hbc.httpclient.BasicClient
import com.twitter.hbc.httpclient.auth.OAuth1
import com.typesafe.config._

/**
  * Created by stremlenye on 11/02/16.
  */
class TwitterStreamActor extends Actor with ActorLogging {

  var client: Option[BasicClient] = None
  val msgQueue = new LinkedBlockingQueue[String](100000)
  val eventQueue = new LinkedBlockingQueue[Event](1000)

  context.setReceiveTimeout(1 seconds)

  val parser = context.actorOf(MessageParserActor.props, "parser")

  override def receive: Receive = {
    case StartStreaming =>
      val hosebirdHosts = HttpHosts.USERSTREAM_HOST
      val hosebirdEndpoint = new UserstreamEndpoint()
      hosebirdEndpoint.withFollowings(true)
      val config = ConfigFactory.load()
      val hosebirdAuth = new OAuth1(
        config.getString("consumerKey"),
        config.getString("consumerSecret"),
        config.getString("token"),
        config.getString("tokenSecret"))

      val hosebirdClient = new ClientBuilder()
        .name("switter-terminal")
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
        Option(msgQueue.poll(2, TimeUnit.SECONDS)) foreach { msg =>
          parser ! MessageParserActor.MessageReceived(msg)
        }
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

