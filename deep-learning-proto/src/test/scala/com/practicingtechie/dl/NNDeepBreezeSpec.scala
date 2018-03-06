package com.practicingtechie.dl

import org.specs2.mutable._

class NNDeepBreezeSpec extends Specification {
  import breeze.linalg._
  import Activation._
  import NNDeepBreeze._

  // L_model_backward
  def lModelBackwardTestCase() = {
    val al = DenseMatrix((1.78862847, 0.43650985))
    val y = DenseMatrix((1.0, 0.0))
    val caches = List(
      Cache(LCache(DenseMatrix((0.09649747, -1.8634927),
        (-0.2773882, -0.35475898),
        (-0.08274148, -0.62700068),
        (-0.04381817, -0.47721803)),
        DenseMatrix((-1.31386475,  0.88462238,  0.88131804, 1.70957306),
          (0.05003364, -0.40467741, -0.54535995, -1.54647732),
          (0.98236743, -1.10106763, -1.18504653, -0.2056499)),
        DenseMatrix((1.48614836, 0.23671627, -1.02378514))
      ), ACache(DenseMatrix((-0.7129932, 0.62524497),
        (-0.16051336, -0.76883635),
        (-0.23003072, 0.74505627)))),
      Cache(LCache(DenseMatrix(
        (1.97611078, -1.24412333),
        (-0.62641691, -0.80376609),
        (-2.41908317, -0.92379202)),
        DenseMatrix((-1.02387576, 1.12397796, -0.13191423)),
        DenseMatrix((-1.62328545))),
        ACache(DenseMatrix((0.64667545, -0.35627076)))
      )
    )
    (al, y, caches)
  }

  def recalculateParametersTestCase() = {
    val params = Parameters(List(
      DenseMatrix((-0.41675785, -0.05626683, -2.1361961, 1.64027081),
        (-1.79343559, -0.84174737,  0.50288142, -1.24528809),
        (-1.05795222, -0.90900761,  0.55145404,  2.29220801)),
      DenseMatrix((-0.5961597, -0.0191305, 1.17500122))),
      List(DenseMatrix((0.04153939, -1.11792545, 0.53905832)), DenseMatrix((-0.74787095))))
    val grads = List(
      LGradients(DenseMatrix.ones(1, 1), DenseMatrix((1.78862847, 0.43650985, 0.09649747, -1.8634927),
        (-0.2773882, -0.35475898, -0.08274148, -0.62700068),
        (-0.04381817, -0.47721803, -1.31386475, 0.88462238)), DenseMatrix((0.88131804,
        1.70957306,
        0.05003364))),
      LGradients(DenseMatrix.ones(1, 1), DenseMatrix((-0.40467741, -0.54535995, -1.54647732)), DenseMatrix((0.98236743)))
    )
    params -> grads
  }

  "breeze deep layer model" should {

    "update parameters" in {
      val (params, grads) = recalculateParametersTestCase()
      val r = recalculateParametersDeep(params, grads, 0.1)
      1 === 1
    }

    "lModelBackwardTestCase" in {
      val (al, y, caches) = lModelBackwardTestCase()
      val z = lModelBackward(al, y, caches)
      1 === 1
    }

  }

}
