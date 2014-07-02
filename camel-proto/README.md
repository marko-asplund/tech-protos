
# camel-proto setup

1. download and start HornetQ

2. configure HornetQ
  Create a queue named "test1" in HornetQ.
  This can be done either by configuring the queue in "hornetq-jms.xml" config file or
  via JMX e.g. using jconsole (createQueue operation).

3. start the app
  mvn -Dexec.mainClass=fi.markoa.proto.camel.DirectoryWatcher exec:java

4. copy a file to /tmp/camel

5. check the queue contents
   e.g. via JMX using jconsole (invoke listMessages operation on the queue).

Tested with
* Apache Camel v2.10
* HornetQ v2.4.0.Final (standalone)
