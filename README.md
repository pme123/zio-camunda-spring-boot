# Camunda Spring Boot with Scala & ZIO

A hello world Spring Boot Application with:
* [Scala](https://www.scala-lang.org)
* [ZIO](https://zio.dev)
* [Mill Build Tool](http://www.lihaoyi.com/mill/)

This should be a starting point to develop Camunda Apps with **_Scala_** in a pure functional way.

## Run the App

    mill server.run
    
Go to http://localhost:9999 

You can login with `demo` / `demo`
 
# Buildtool Mill

## Update dependencies in Intellij

    mill mill.scalalib.GenIdea/idea