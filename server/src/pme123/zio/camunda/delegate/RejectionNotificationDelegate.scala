package pme123.zio.camunda.delegate

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
        content <- execution.stringVar("content")
        comments <- execution.stringVar("comments")
        text <- ZIO.succeed (
          s"""Hi!
             |
             |Unfortunately your tweet has been rejected.
             |
             |Original content: $content
             |
             |Comment: $comments
             |
             |Sorry, please try with better content the next time :-)
             |""".stripMargin)
        _ <- console.putStrLn(text)
      } yield ()
    )
}