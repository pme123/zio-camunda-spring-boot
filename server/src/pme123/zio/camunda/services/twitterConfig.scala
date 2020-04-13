package pme123.zio.camunda.services

import java.io.File

import zio._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfig
import zio.config.{Config, ConfigDescriptor, config}

object twitterConfig {
  type TwitterConfig = Has[Service]

  trait Service {
    def auth(): Task[TwitterAuth]
  }

  def auth(): RIO[TwitterConfig, TwitterAuth] =
    ZIO.accessM(_.get.auth())

  case class TwitterAuth(consumerToken: Token, accessToken: Token)

  case class Token(key: String, value: String)

  private val tokenConfig =
    (string("key") |@|
      string("value")
      ) (Token.apply, Token.unapply)

  private val twitterAuthConfig: ConfigDescriptor[String, String, TwitterAuth] =
    (nested("consumerToken")(tokenConfig) |@|
      nested("accessToken")(tokenConfig)
      ) (TwitterAuth.apply, TwitterAuth.unapply)

  lazy val sourceLayer: TaskLayer[Config[TwitterAuth]] = TypesafeConfig.fromHoconFile(new File("twitter-auth.conf"), twitterAuthConfig)

  lazy val live: TaskLayer[TwitterConfig] =
    ZLayer.succeed(
      new Service {
        def auth(): Task[TwitterAuth] =
          config[TwitterAuth]
            .provideLayer(sourceLayer)
      })

}