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
@Service("tweetAdapter")
class TweetContentDelegate
  extends CamundaDelegate {

  def execute(execution: DelegateExecution): Unit =
    unsafeRun(
      {
        val content = execution.stringVar("content")
        if ("network error" == content)
          ZIO.fail(new UnknownHostException("demo twitter account"))
        twitterApi.createTweet(content)
        }
        .mapError {
          case ex: TwitterException if ex.code.intValue == 187 =>
            new BpmnError("duplicateMessage")
        }.provideCustomLayer((Console.live ++ twitterConfig.live) >>> twitterApi.live)
    )

}
