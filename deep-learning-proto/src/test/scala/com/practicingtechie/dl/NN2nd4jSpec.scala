package com.practicingtechie.dl

import org.specs2.mutable._

class NN2nd4jSpec extends Specification {
  import NN2nd4j._, Activation._
  import org.nd4j.linalg.factory.Nd4j

  def computeCostTestCase() = {
    val al = Nd4j.create(Array(0.8, 0.9, 0.4), Array(1, 3), 'c')
    val y = Nd4j.ones(1, 3)
    println(al)
    println(y)
    (al, y)
  }

  "nd4j 2 layer model" should {
    "compute cost" in {
      val (al, y) = computeCostTestCase()
      computeCost(al, y) must beCloseTo(0.41493159961539694 +/- 0.001)
    }

  }
}
