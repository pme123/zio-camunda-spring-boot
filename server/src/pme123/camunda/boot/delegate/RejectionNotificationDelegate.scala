package pme123.camunda.boot.delegate

import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}
import org.springframework.stereotype.Service
import zio.Runtime.default.unsafeRun
import zio.ZIO
import zio.console

/**
  * Rejection is just done via a sysout. You could, for example, implement sending mail to the author here.
  * Use your own Mail mechanisms for this or use your application server features.
  */
@Service("emailAdapter")
class RejectionNotificationDelegate
  extends CamundaDelegate {

  def execute(execution: DelegateExecution): Unit =
    unsafeRun(
      for {
        text <- ZIO.succeed {
          val content = execution.stringVar("content")
          val comments = execution.stringVar("comments")
          s"""Hi!
             |
             |Unfortunately your tweet has been rejected.
             |
             |Original content: $content
             |
             |Comment: $comments
             |
             |Sorry, please try with better content the next time :-)
             |""".stripMargin
        }
        _ <- console.putStrLn(text)
      } yield ()
    )
}