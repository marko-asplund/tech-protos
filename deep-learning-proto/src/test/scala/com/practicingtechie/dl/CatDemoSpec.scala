package com.practicingtechie.dl

import org.specs2.mutable._

class CatDemoSpec extends Specification {
  import breeze.linalg._
  import CatDemo._, CatDemo.Activation._

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

  "forward propagation" should {
    "linearForward" in {
      val (a, w, b) = linearForwardTestCase()
      val r = linearForward(a, w, b)
      r.rows ===  1 and r.cols === 2
    }

    "linearActivationForward / sigmoid" in {
      val (aPrev, w, b) = linearActivationForwardTestCase()
      val r = linearActivationForward(aPrev, w, b, Sigmoid)
      r.rows === 1 and r.cols === 2
    }

    "linearActivationForward / ReLu" in {
      val (aPrev, w, b) = linearActivationForwardTestCase()
      val r = linearActivationForward(aPrev, w, b, ReLu)
      r.rows === 1 and r.cols === 2
    }
  }

}
