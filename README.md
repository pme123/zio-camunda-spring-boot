# Camunda Spring Boot with Scala & ZIO - The Twitter example.

This is a port of [**Tweet directly from the Tasklist - Spring Boot packaged**](https://github.com/camunda/camunda-bpm-examples/tree/master/spring-boot-starter/example-twitter)
to my World;).

It should give you a starting point if you want to do 
**Camunda Project** with _**Scala**_ in a **functional way**.

Here are the main ingredients:
* Integrate with Camunda via its Java API and it's 
[Spring Boot start App](https://github.com/camunda/camunda-bpm-spring-boot-starter).
* Implementation in [**Scala**](https://www.scala-lang.org).
* Pure functional implementation using [ZIO](https://zio.dev)
* Building my Project with [Mill Build Tool](http://www.lihaoyi.com/mill/)

For the example it uses also a Scala implementation of the Twitter API:
[twitter4s](https://github.com/DanielaSfregola/twitter4s)

# From the original README
This example demonstrates how you can use BPM process and Tweeter API to build simple Twitter client. 
It uses `camunda-bpm-spring-boot-starter-webapp` and thus embed Tomcat as a web container.

The example contains:
- a process application with one process deployed on the Camunda engine
- custom forms to create and review the Tweet
- creates on startup an admin user "kermit" (password: kermit)

It also demonstrates the usage of the `application.yaml` configuration file.

## How is it done

1. To embed the Camunda Engine you must add following dependency to your `build.sc`:
   
    ```
    ...
    ivy"org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:${Version.camundaSpringBoot}"
    ...
    ```

2. With Spring Boot you usually create an "application" class annotated with `@SpringBootApplication`. In order to have a Camunda process application
registered, you can simply add the annotation `@EnableProcessApplication` to the same class and also include the `processes.xml` file in your `META-INF` folder:

    ```scala
    @SpringBootApplication
    @EnableProcessApplication
    class Application
    ```

3. You can also put BPMN, CMMN and DMN files in your classpath, they will be automatically deployed and registered within the process application (`server/resources`).

4. You can configure your Spring Boot application using `application.yaml` file. All possible Camunda-specific configuration parameters are listed [here](https://stage.docs.camunda.org/manual/7.12/user-guide/spring-boot-integration/configuration/)

5. This example provides two implementations for posting a Tweet:
   * `TweetContentOfflineDelegate` (default) - will just print the tweet content on console
   * `TweetContentDelegate` - will really post a tweet on http://twitter.com/#!/camunda_demo

   You can switch between two implementations by changing the name of a Spring bean to `tweetAdapter`. 
  (Just use `@Service("tweetAdapter")` on the Delegate you want)
  This `tweetAdapter` bean is further referenced in 
  the BPMN diagram via "Delegate expression" in a service task:

    ```xml
    ...
    <serviceTask id="service_task_publish_on_twitter" name="Publish on Twitter" camunda:delegateExpression="#{tweetAdapter}">
      ...
    </serviceTask>
    ...
    ```
   
## Run the application and check the result

You can build and run the application by `mill server.run`.

Go to `http://localhost:9999`, log in with "kermit/kermit", go to Tasklist and try to start the process and complete the tasks, observe log entries 
or the real tweet when `TweetContentDelegate` is used.

# Additional Stuff

## Spring Application
As we use ZIO, we want to create the Spring Application in a managed way:
```scala
private def managedSpringApp(args: List[String]): ZManaged[Console, Throwable, ConfigurableApplicationContext] =
ZManaged.make(
  console.putStrLn("Starting Spring Container...") *>
    ZIO.effect(
      SpringApplication.run(classOf[Application], args: _*)
    )
)(ctx =>
  console.putStrLn("Spring Container Stopping...") *>
    ZIO.effect(
      if (ctx.isActive)
        SpringApplication.exit(ctx)
    ).catchAll(ex =>
      console.putStrLn(s"Problem shutting down the Spring Container.\n${ex.getMessage}")
    )
)
```
So the first block will create the Spring Application; the second one will close it safely.
I figured that Spring does the exit itself, but as it is an excellent example I kept it.

## Modules
I separated the Camunda Integration from the Twitter API with two modules:
* camunda
* twitter

## Dependency Injection
Spring and ZIO bring their own ways of **DI**. 
### [Spring DI](https://www.springboottutorial.com/spring-framework-dependency-injection-inversion-of-control)
* Runtime DI.
* Used to integrate with Camunda.
* Example: 

  In Camunda the Service Tasks can be referred by a Delegate Expression, e.g. `#{tweetAdapter}`.
  Spring looks now for a `JavaDelegate` that is annotated like:
  
  ```scala`
  @Service("tweetAdapter")
  class TweetContentOfflineDelegate`
  ``

### [ZIO ZLayers](https://zio.dev/docs/howto/howto_use_layers)
* Compile Time DI.
* Used to wire the Services, like the Twitter API.
* Example:
 
  _ZIO_ modules provide its implementations via _ZLayers_.
  These Layers can be composed _horizontally_ and _vertically_. 
  So you provide them to your code (at the end of the world), 
  that is agnostic to its implementations:
  
  ```scala
  // your effectful code
  }.provideCustomLayer((Console.live ++ twitterConfig.live) >>> twitterApi.live)
  ```
  The compiler makes sure that you provide everything correctly.

### The End of the World
In functional programming, everything is a value. 
So the whole program is nothing more than a Datastructure.

But in the end you have to run it eventually. In our case we have two types 'End of World':
1. The Spring App itself, which is started as a `zio.App` and then run forever.
   
   ```scala
   object Application extends zio.App {
     def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
       managedSpringApp(args).useForever
       ...
   ```
2. Each Delegate function that was called from Camunda.

   ```scala
    class TweetContentDelegate
      extends CamundaDelegate {
      def execute(execution: DelegateExecution): Unit =
        unsafeRun(...)
   ```
   
## Testing
I use ZIO-Test, check `TwitterConfigSuites` for an example, or `TwitterApiSuites` where we can check the Console output out of the box. 

The process itself is not tested yet (as it is not in the Camunda Example).

## Buildtool Mill
In **Visual Studio Code** mill works with the Metals Plugin without any adjustments.

### Update dependencies in Intellij

    mill mill.scalalib.GenIdea/idea