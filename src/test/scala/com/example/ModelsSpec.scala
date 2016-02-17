package com.example


import org.json4s.JsonAST._
import org.scalatest._

/**
  * Created by stremlenye on 15/02/16.
  */
class ModelsSpec extends FlatSpec with Matchers {
  import com.example.Readers._

  "User reader" should "produce User from json" in {
    val json = JObject(JField("name", JString("John Doe")))
    val user: User = UserReads.read(json)
    user shouldNot be(null)
    user.name shouldEqual "John Doe"
  }

  "User reader" should "fail if there json format is not supported" in {
    val json = JObject(JField("not_a_name", JString("John Doe")))
    an [RuntimeException] should be thrownBy  UserReads.read(json)
  }

  "Tweet reader" should "produce tweet from json" in {
    val json = JObject(
      JField("text", JString("I am in a toilet")),
      JField("user", JObject(JField("name", JString("John Doe"))))
    )
    val tweet: Tweet = TweetReads.read(json)
    tweet shouldNot be(null)
    tweet.text shouldEqual "I am in a toilet"
    tweet.user.name shouldEqual "John Doe"
  }

  "Tweet reader" should "fail if there json format is not supported" in {
    val json = JObject(
      JField("not_a_text", JString("I am in a toilet")),
      JField("not_a_user", JObject(JField("name", JString("John Doe"))))
    )
    an [RuntimeException] should be thrownBy TweetReads.read(json)
  }
}
