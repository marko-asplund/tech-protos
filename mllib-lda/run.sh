#!/bin/bash

# sbt 'export compile:dependencyClasspath' > spark-cp.txt
eval scala -J-Xmx8G -classpath `cat spark-cp.txt`:target/scala-2.11/mllib-lda_2.11-1.0.jar fi.markoa.proto.mllib.LDADemo $1
