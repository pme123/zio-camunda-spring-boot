package pme123.zio.camunda.twitter

import pme123.zio.camunda.twitter.twitterConfig.{Token, TwitterAuth}
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.environment.TestConsole

object TwitterApiSuites
  extends DefaultRunnableSpec {
  private val expectedSendmessage =
    s"""|
        |${"#" * 20}
        |
        |NOW WE WOULD TWEET:
        |'hello from testing'
        |
        |
        |${"#" * 20}
        |""".stripMargin

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("TwitterApiSuites")(
      testM("the offline Api creates a Tweet correctly") {
        for {
          _ <- twitterApi.createTweet("hello from testing")
          output <- TestConsole.output
        } yield assert(output.head.trim)(equalTo(expectedSendmessage.trim))
      }.provideCustomLayer(twitterApi.offline.mapError(TestFailure.fail))
    )

}
