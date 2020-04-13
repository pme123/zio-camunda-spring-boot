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
  * Publish content on Twitter. It really goes live! Watch out http://twitter.com/#!/camunda_demo for your postings.
  */
@Service("tweetAdapter")
class TweetContentDelegate
  extends CamundaDelegate {

  def execute(execution: DelegateExecution): Unit =
    unsafeRun(
      (for {
        tweet <- ZIO.fromFuture { _ =>
          val content = execution.stringVar("content")
          // For webex demos, force a network error
          if ("network error" == content) Future.failed(new UnknownHostException("demo twitter account"))
          //TODO wrap in twitter module
          val consumerToken = ConsumerToken("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS")
          val accessToken = new AccessToken("220324559-jet1dkzhSOeDWdaclI48z5txJRFLCnLOK45qStvo", "B28Ze8VDucBdiE38aVQqTxOyPc7eHunxBVv7XgGim4say")
          val twitter = TwitterRestClient(consumerToken, accessToken)
          twitter.createTweet(content)
        }
        _ <- console.putStrLn(s"$tweet sent")
      } yield ())
        .mapError {
          case ex: TwitterException if ex.code.intValue == 187 =>
            new BpmnError("duplicateMessage")
        }
    )

}
