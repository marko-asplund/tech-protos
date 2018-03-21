package com.practicingtechie.dl

import org.specs2.mutable._

class NN2nd4jSpec extends Specification {
  import NN2nd4j._, Activation._
  import com.practicingtechie.dl.Nd4jUtil._
  import org.nd4j.linalg.factory.Nd4j

  def computeCostTestCase() = {
    val al = Nd4j.create(Array(0.8, 0.9, 0.4), Array(1, 3), 'c')
    val y = Nd4j.ones(1, 3)
    (al, y)
  }

  def linearActivationForwardTestCase2() = {
    val (a, w, b) = (
      Nd4j.create(Array(-0.41675785, -0.05626683,
        -2.1361961, 1.64027081,
        -1.79343559, -0.84174737), Array(3, 2), 'c'),
      Nd4j.create(Array(0.50288142, -1.24528809, -1.05795222),
        Array(1, 3), 'c'),
      Nd4j.create(Array(-0.90900761), Array(1, 1), 'c')
    )
    (a, w, b)
  }

  def linearActivationBackwardTestCase1() = {
    val (al, lCache) = (
      Nd4j.create(Array(-0.41675785, -0.05626683), Array(1, 2), 'c'),
      Cache(LCache(
        Nd4j.create(Array(-2.1361961, 1.64027081,
          -1.79343559, -0.84174737,
          0.50288142, -1.24528809), Array(3, 2), 'c'),
        Nd4j.create(Array(-1.05795222, -0.90900761, 0.55145404), Array(1, 3), 'c'),
        Nd4j.create(Array(2.29220801), Array(1, 1), 'c')
      ), ACache(Nd4j.create(Array(0.04153939, -1.11792545), Array(1, 2), 'c')))
    )
    (al, lCache)
  }

  "nd4j 2 layer model" should {
    "linearForward #2" in {
      val (a, w, b) = (
        Nd4j.create(Array(1.62434536, -0.61175641,
          -0.52817175, -1.07296862,
          0.86540763, -2.3015387), Array(3, 2), 'c'),
        Nd4j.create(Array(1.74481176, -0.7612069, 0.3190391),
          Array(1, 3), 'c'),
        Nd4j.create(Array(-0.24937038), Array(1, 1), 'c')
      )
      val (z, _) = linearForward(a, w, b) // [[ 3.26295337 -1.23429987]]
      z.getDouble(0, 0) must beCloseTo(3.26295337 +/- 0.001)
    }

    "linearActivationForward / sigmoid #2" in {
      val (aPrev, w, b) = linearActivationForwardTestCase2
      val (a, _) = linearActivationForward(aPrev, w, b, Sigmoid) // [[ 0.96890023 0.11013289]]
      a.getDouble(0, 0) must beCloseTo(0.9689002334527027 +/- 0.001)
    }

    "linearActivationForward / ReLu #2" in {
      val (aPrev, w, b) = linearActivationForwardTestCase2
      val (a, _) = linearActivationForward(aPrev, w, b, ReLu) // [[ 3.43896131 0. ]]
      a.getDouble(0, 0) must beCloseTo(3.43896131 +/- 0.001)
    }

    "compute cost" in {
      val (al, y) = computeCostTestCase()
      computeCost(al, y) must beCloseTo(0.41493159961539694 +/- 0.001)
    }

    "linearActivationBackward / sigmoid #1" in {
      val (al, lCache) = linearActivationBackwardTestCase1()
      val (dAPrev, dW, db) = linearActivationBackward(al, lCache, Sigmoid)
      dAPrev.getDouble(2, 1) must beCloseTo(-0.00576 +/- 0.0001)
    }

    "linearActivationBackward / ReLu #1" in {
      val (al, lCache) = linearActivationBackwardTestCase1()
      val (dAPrev, dW, db) = linearActivationBackward(al, lCache, ReLu)
      dAPrev.getDouble(2, 0) must beCloseTo(-0.2298228 +/- 0.0001)
    }

  }
}
