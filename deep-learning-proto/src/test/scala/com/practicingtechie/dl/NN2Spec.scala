package com.practicingtechie.dl

import org.specs2.mutable._

class NN2Spec extends Specification {
  import breeze.linalg._
  import NN2._, NN2.Activation._

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

  def computeCostTestCase() = {
    val al = DenseVector(0.8, 0.9, 0.4)
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

  "forward propagation" should {
    "linearForward" in {
      val (a, w, b) = linearForwardTestCase()
      val (r, _) = linearForward(a, w, b)
      r.rows ===  1 and r.cols === 2
    }

    "linearActivationForward / sigmoid" in {
      val (aPrev, w, b) = linearActivationForwardTestCase()
      val (r, _) = linearActivationForward(aPrev, w, b, Sigmoid)
      r.rows === 1 and r.cols === 2
    }

    "linearActivationForward / ReLu" in {
      val (aPrev, w, b) = linearActivationForwardTestCase()
      val (r, _) = linearActivationForward(aPrev, w, b, ReLu)
      r.rows === 1 and r.cols === 2
    }

    "compute cost" in {
      val (al, y) = computeCostTestCase()
      computeCost(al, y) must beCloseTo(0.41493159961539694 +/- 0.001)
    }

    "linearBackward #1" in {
      val (dZ, a, w, b) = linearBackwardTestCase1()
      val (dAPrev, dW, db) = linearBackward(dZ, a, w, b)
      1 === 1
    }

    "linearBackward #2" in {
      val (dZ, a, w, b) = linearBackwardTestCase2()
      val (dAPrev, dW, db) = linearBackward(dZ, a, w, b)
      1 === 1
    }
  }

}
