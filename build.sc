import mill._
import mill.scalajslib.ScalaJSModule
import mill.scalalib._

object Version {
  val scalaVersion = "2.13.1"

  val spring = "2.2.4.RELEASE"
  val camunda = "3.3.7"
  val h2 = "1.4.200"
  val postgres = "42.2.8"

  val twitter4s = "6.2"
  val zio = "1.0.0-RC18-2"
}

object Libs {
  val spring = ivy"org.springframework.boot:spring-boot-starter-web:${Version.spring}"
  val springJdbc = ivy"org.springframework.boot:spring-boot-starter-jdbc:${Version.spring}"
  val camunda = ivy"org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:${Version.camunda}"
  val h2 = ivy"com.h2database:h2:${Version.h2}"
  val postgres = ivy"org.postgresql:postgresql:${Version.postgres}"

  val twitter4s = ivy"com.danielasfregola::twitter4s:${Version.twitter4s}"

  val zio = ivy"dev.zio::zio:${Version.zio}"

  val zioTest = ivy"dev.zio::zio-test:${Version.zio}"
  val zioTestSbt = ivy"dev.zio::zio-test-sbt:${Version.zio}"
}

trait MyModule extends ScalaModule {
  val scalaVersion = Version.scalaVersion


  override def scalacOptions =
    defaultScalaOpts

  val defaultScalaOpts = Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "UTF-8", // Specify character encoding used by source files.
    "-language:higherKinds", // Allow higher-kinded types
    "-language:postfixOps", // Allows operator syntax in postfix position (deprecated since Scala 2.10)
    "-feature" // Emit warning and location for usages of features that should be imported explicitly.
    //  "-Ypartial-unification",      // Enable partial unification in type constructor inference
    //  "-Xfatal-warnings"            // Fail the compilation if there are any warnings
  )

}

trait MyModuleWithTests extends MyModule {

  object test extends Tests {
    override def moduleDeps = super.moduleDeps

    override def ivyDeps = Agg(
      Libs.zioTest,
      Libs.zioTestSbt
    )

    def testOne(args: String*) = T.command {
      super.runMain("org.scalatest.run", args: _*)
    }

    def testFrameworks =
      Seq("zio.test.sbt.ZTestFramework")
  }

}

object server extends MyModule {

  override def mainClass = Some("pme123.camunda.boot.hello.Application")

  override def ivyDeps = {
    Agg(
      Libs.spring,
      Libs.springJdbc,
      Libs.camunda,
      Libs.h2,
     // Libs.postgres,

      Libs.twitter4s,
      Libs.zio
    )
  }
}


