package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import SolucionFuncional._

@RunWith(classOf[JUnitRunner])
class VersionesParTest2_4 extends AnyFunSuite {

  val obj = SolucionFuncional

  // ---------------------------------------------------------
  // Test 1: costoRiegoFinca vs costoRiegoFincaPar (caso simple)
  // ---------------------------------------------------------
  test("costoRiegoFincaPar coincide con costoRiegoFinca en finca pequeña") {
    val f: Finca = Vector(
      (10, 3, 1),
      (12, 2, 2)
    )
    val pi: ProgRiego = Vector(0, 1)

    val sec = costoRiegoFinca(f, pi)
    val par = costoRiegoFincaPar(f, pi)

    assert(sec == par)
  }

  // ---------------------------------------------------------
  // Test 2: costoRiegoFincaPar con programación invertida
  // ---------------------------------------------------------
  test("costoRiegoFincaPar coincide con la versión secuencial (orden invertido)") {
    val f: Finca = Vector(
      (15, 4, 1),
      (12, 3, 3)
    )
    val pi: ProgRiego = Vector(1, 0)

    assert(costoRiegoFinca(f, pi) == costoRiegoFincaPar(f, pi))
  }

  // ---------------------------------------------------------
  // Test 3: costoMovilidadPar con distancias simétricas
  // ---------------------------------------------------------
  test("costoMovilidadPar coincide con costoMovilidad") {
    val f: Finca = Vector(
      (10, 2, 1),
      (12, 4, 2),
      (8,  3, 1)
    )

    val d: Distancia = Vector(
      Vector(0, 5, 7),
      Vector(5, 0, 6),
      Vector(7, 6, 0)
    )

    val pi: ProgRiego = Vector(0, 2, 1)

    assert(costoMovilidad(f, pi, d) == costoMovilidadPar(f, pi, d))
  }

  // ---------------------------------------------------------
  // Test 4: finca de 4 tablones (prueba más grande)
  // ---------------------------------------------------------
  test("costoRiegoFincaPar y costoMovilidadPar siguen coincidiendo en finca de 4 tablones") {
    val f: Finca = Vector(
      (12, 2, 1),
      (14, 3, 2),
      (10, 1, 1),
      (9,  4, 3)
    )

    val d: Distancia = Vector(
      Vector(0, 3, 4, 2),
      Vector(3, 0, 5, 6),
      Vector(4, 5, 0, 7),
      Vector(2, 6, 7, 0)
    )

    val pi: ProgRiego = Vector(1, 3, 0, 2)

    val costoSec = costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)
    val costoPar = costoRiegoFincaPar(f, pi) + costoMovilidadPar(f, pi, d)

    assert(costoSec == costoPar)
  }

  // ---------------------------------------------------------
  // Test 5: caso donde todas las distancias son iguales
  // ---------------------------------------------------------
  test("costoMovilidadPar con distancias uniformes coincide con la versión secuencial") {
    val f: Finca = Vector(
      (8, 1, 1),
      (7, 2, 2),
      (9, 3, 3)
    )

    val d: Distancia = Vector(
      Vector(0, 1, 1),
      Vector(1, 0, 1),
      Vector(1, 1, 0)
    )

    val pi: ProgRiego = Vector(2, 0, 1)

    assert(costoMovilidad(f, pi, d) == costoMovilidadPar(f, pi, d))
  }

}

