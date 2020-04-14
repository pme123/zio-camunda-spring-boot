package pme123.zio.camunda.delegate

import java.net.UnknownHostException

import com.danielasfregola.twitter4s.exceptions.TwitterException
import org.camunda.bpm.engine.delegate.{BpmnError, DelegateExecution}
import org.springframework.stereotype.Service
import pme123.zio.camunda.services.{twitterApi, twitterConfig}
import zio.Runtime.default.unsafeRun
import zio.ZIO
import zio.console.Console

/**
  * Publish content on Twitter. It really goes live! Watch out http://twitter.com/#!/camunda_demo for your postings.
  */
class TweetContentDelegate
  extends CamundaDelegate {

  def execute(execution: DelegateExecution): Unit =
    unsafeRun(
      (for {
        tweet <- execution.stringVar("content")
        _ <-
          if ("network error" == tweet)
            ZIO.fail(new UnknownHostException("demo twitter account"))
          else
            twitterApi.createTweet(tweet)
      } yield ())
        .mapError {
          case ex: TwitterException if ex.code.intValue == 187 =>
            new BpmnError("duplicateMessage")
        }.provideCustomLayer((Console.live ++ twitterConfig.live) >>> twitterApi.live)
    )

}
