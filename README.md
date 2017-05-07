# tech-protos

## camel-proto

Use [Apache Camel](http://camel.apache.org/) for integration framework for watching directory contents and
delivering files to a JMS queue (in [HornetQ](http://hornetq.jboss.org/)).

see [camel-proto](camel-proto) for more information.

## cassandra-proto

Access [Apache Cassandra](http://cassandra.apache.org/) NoSQL database using the [Astyanax](https://github.com/Netflix/astyanax/) client.
Demonstrates how to use the Astyanax and CQL 3 based APIs as well as prepared statements and batch update features.

Sample code contributed to Astyanax [astyanax-examples](https://github.com/Netflix/astyanax/tree/master/astyanax-examples).

## hc-proto

Experiment with the new [HttpClient](http://hc.apache.org/) execution chain APIs.
The aim here is to intercept requests, so that a particular response can be issued on the client-side.
Useful e.g. for integration testing and some environments where outbound connections are restricted.

## hk2-proto

Experiment with the [HK2](https://hk2.java.net/) dependency injection framework.
This experiment was aimed at the Jersey 2 Spring integration implementation, see
[Implementing Jersey 2 Spring integration](http://practicingtechie.com/2014/02/08/implementing-jersey-2-spring-integration/).

## hornetq-hermes

Absolutely nothing to see here :-)

## jersey1-proto

Demonstrate an bug with Jersey 1 & Jackson 2 JAX-RS JSON provider.

## jersey2-proto

* jaxrs-arquillian-test: implement JAX-RS resource class integration tests with [Arquillian](http://arquillian.org/).
  see [jaxrs-arquillian-test](jersey2-proto/jaxrs-arquillian-test).
* jersey-hello: testing JAX-RS filters using the [Jersey 2](https://jersey.java.net/) Test Framework.
* jersey-jetty: execute WAR web apps in programmatically bootstrapped Jetty container.

## jetty-proto

Experiment with implementing a custom [ServletContainerInitializer](http://docs.oracle.com/javaee/6/api/javax/servlet/ServletContainerInitializer.html).

## jruby-proto

Experiment with rendering [Liquid](http://liquidmarkup.org/) templates using JRuby.
This work eventually evolved into
[A JVM polyglot experiment with JRuby](http://practicingtechie.com/2013/01/21/a-jvm-polyglot-experiment-with-jruby/).

## metrics-proto

Experiments with [Metrics](http://metrics.codahale.com/) monitoring toolkit.

## mllib-lda

LDA proto with Apache Spark MLlib

## oauth2-resource-server

OAuth2 resource server prototype in Scala.

## oauth2-proto

OAuth2 client and provider experiments in Java.

## tsv_to_xls

[TsvToExcel](tsv_to_xls) tool can read in multiple data files in TSV format and produce a single
SpreadsheetML (e.g. MS Excel 2003 and later) file with each source file in its own sheet.

## vw

Prototype code for reading [Vowpal Wabbit](https://github.com/JohnLangford/vowpal_wabbit) model files in Scala.
