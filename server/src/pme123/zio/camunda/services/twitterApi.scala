package pme123.zio.camunda.services

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import pme123.zio.camunda.services.twitterConfig.TwitterConfig
import zio.console.Console
import zio._

object twitterApi {
  type TwitterApi = Has[Service]

  trait Service {
    def createTweet(tweet: String): Task[Unit]
  }

  def createTweet(tweet: String): RIO[TwitterApi, Unit] =
    ZIO.accessM(_.get.createTweet(tweet))

  type TwitterApiDeps = TwitterConfig with Console

  lazy val live: RLayer[TwitterApiDeps, TwitterApi] =
    ZLayer.fromServices[Console.Service, twitterConfig.Service, Service] {
      (console, twitterConfig) =>
        new Service {
          def createTweet(tweet: String): Task[Unit] =
            for {
              config <- twitterConfig.auth()
              tweet <- ZIO.fromFuture { _ =>
                val consumerToken = ConsumerToken(config.consumerToken.key, config.consumerToken.value)
                val accessToken = new AccessToken(config.accessToken.key, config.accessToken.value)
                val twitter = TwitterRestClient(consumerToken, accessToken)
                twitter.createTweet(tweet)
              }
              _ <- console.putStrLn(s"$tweet sent")
            } yield ()
        }
    }
}