package com.practicingtechie.dl

import org.specs2.mutable._

class NN2BreezeSpec extends Specification {
  import breeze.linalg._
  import NN2Breeze._, Activation._

  def linearForwardTestCase() = {
    val (a, w, b) = (DenseMatrix.rand[Double](3, 2),
      DenseMatrix.rand[Double](1, 3),
      DenseVector.rand[Double](1))
    (a, w, b)
  }

  def linearActivationForwardTestCase() = {
    val (aPrev, w, b) = (DenseMatrix.rand[Double](3, 2),
      DenseMatrix.rand[Double](1, 3),
      DenseVector.rand[Double](1))
    (aPrev, w, b)
  }

  def linearActivationForwardTestCase2() = {
    val (aPrev, w, b) = (
      DenseMatrix((-0.41675785, -0.05626683),
        (-2.1361961, 1.64027081),
        (-1.79343559, -0.84174737)),
      DenseMatrix(0.50288142, -1.24528809, -1.05795222).t,
      DenseVector(-0.90900761)
    )
    (aPrev, w, b)
  }


  def computeCostTestCase() = {
    val al = DenseMatrix((0.8), (0.9), (0.4))
    val y = DenseVector.ones[Double](3)
    (al, y)
  }

  def linearBackwardTestCase1() = {
    val (dZ, a, w, b) = (
      DenseMatrix.rand[Double](1, 2),
      DenseMatrix.rand[Double](3, 2),
      DenseMatrix.rand[Double](1, 3),
      DenseVector.rand[Double](1)
    )
    (dZ, a, w, b)
  }

  def linearBackwardTestCase2() = {
    val dZ = DenseMatrix((-0.8019545, 3.85763489))
    val a = DenseMatrix((-1.02387576, 1.12397796),
      (-1.62328545, 0.64667545),
      (-1.74314104, -0.59664964))
    val w = DenseMatrix((0.74505627, 1.97611078, -1.24412333))
    val b = DenseVector[Double](1)
    (dZ, a, w, b)
  }

  def linearActivationBackwardTestCase1() = {
    val (al, lCache) = (
      DenseMatrix(-0.41675785, -0.05626683).t,
      Cache(LCache(
        DenseMatrix((-2.1361961, 1.64027081),
          (-1.79343559, -0.84174737),
          (0.50288142, -1.24528809)),
        DenseMatrix(-1.05795222, -0.90900761, 0.55145404).t,
        DenseVector(2.29220801)
      ), ACache(DenseMatrix(0.04153939, -1.11792545).t))
    )
    (al, lCache)
  }

  "breeze 2 layer model" should {
    "linearForward #1" in {
      val (a, w, b) = linearForwardTestCase()
      val (z, _) = linearForward(a, w, b)
      z.rows ===  1 and z.cols === 2
    }

    "linearForward #2" in {
      val (a, w, b) = (
        DenseMatrix((1.62434536, -0.61175641),
          (-0.52817175, -1.07296862),
          (0.86540763, -2.3015387)),
        DenseMatrix(1.74481176, -0.7612069, 0.3190391).t,
        DenseVector(-0.24937038)
      )
      val (z, _) = linearForward(a, w, b) // [[ 3.26295337 -1.23429987]]
      z(0, 0) must beCloseTo(3.26295337 +/- 0.001)
    }

    "linearActivationForward / sigmoid #1" in {
      val (aPrev, w, b) = linearActivationForwardTestCase()
      val (a, _) = linearActivationForward(aPrev, w, b, Sigmoid)
      a.rows === 1 and a.cols === 2
    }

    "linearActivationForward / sigmoid #2" in {
      val (aPrev, w, b) = linearActivationForwardTestCase2
      val (a, _) = linearActivationForward(aPrev, w, b, Sigmoid) // [[ 0.96890023 0.11013289]]
      a(0, 0) must beCloseTo(0.9689002334527027 +/- 0.001)
    }

    "linearActivationForward / ReLu #1" in {
      val (aPrev, w, b) = linearActivationForwardTestCase()
      val (a, _) = linearActivationForward(aPrev, w, b, ReLu)
      a.rows === 1 and a.cols === 2
    }

    "linearActivationForward / ReLu #2" in {
      val (aPrev, w, b) = linearActivationForwardTestCase2
      val (a, _) = linearActivationForward(aPrev, w, b, ReLu) // [[ 3.43896131 0. ]]
      a(0, 0) must beCloseTo(3.43896131 +/- 0.001)
    }

    "compute cost" in {
      val (al, y) = computeCostTestCase()
      computeCost(al.t, y) must beCloseTo(0.41493159961539694 +/- 0.001)
    }

    "linearBackward #1" in {
      val (dZ, aPrev, w, b) = (
        DenseMatrix(1.62434536, -0.61175641).t,
        DenseMatrix((-0.52817175, -1.07296862),
          (0.86540763, -2.3015387),
          (1.74481176, -0.7612069)),
        DenseMatrix(0.3190391, -0.24937038, 1.46210794).t,
        DenseVector(-2.06014071)
      )
      val (dAPrev, dW, db) = linearBackward(dZ, aPrev, w, b)
      db(0) must beCloseTo(0.506294475 +/- 0.001)
    }

    "linearBackward #2" in {
      val (dZ, a, w, b) = linearBackwardTestCase2()
      val (dAPrev, dW, db) = linearBackward(dZ, a, w, b)
      1 === 1
    }

    "linearActivationBackward / sigmoid #1" in {
      val (al, lCache) = linearActivationBackwardTestCase1()
      val (dAPrev, dW, db) = linearActivationBackward(al, lCache, Sigmoid)
      dAPrev(2, 1) must beCloseTo(-0.00576 +/- 0.0001)
    }

    "linearActivationBackward / ReLu #1" in {
      val (al, lCache) = linearActivationBackwardTestCase1()
      val (dAPrev, dW, db) = linearActivationBackward(al, lCache, ReLu)
      dAPrev(2, 0) must beCloseTo(-0.2298228 +/- 0.0001)
    }

  }

}
