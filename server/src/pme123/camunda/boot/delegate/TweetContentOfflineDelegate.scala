package pme123.camunda.boot.delegate

import java.net.UnknownHostException

import org.camunda.bpm.engine.delegate.{BpmnError, DelegateExecution, JavaDelegate}
import zio.Runtime.default.unsafeRun
import zio.{ZIO, console}
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import com.danielasfregola.twitter4s.exceptions.TwitterException
import org.springframework.stereotype.Service

import scala.concurrent.Future

/**
  * Use this delegate instead of TweetContentDelegate, if you don't want to access Twitter, but just to do some sysout.
  */
class TweetContentOfflineDelegate
  extends CamundaDelegate {

  def execute(execution: DelegateExecution): Unit =
    unsafeRun(
      (for {
        tweet <- ZIO.succeed {
          val content = execution.stringVar("content")
          s"""|
              |
              |${"#" * 20} 
              | 
              |NOW WE WOULD TWEET: 
              |'$content'
              |
              |
              |${"#" * 20} 
              |
              |
              |""".stripMargin
        }
        _ <- console.putStrLn(tweet)
      } yield ())
    )

}
