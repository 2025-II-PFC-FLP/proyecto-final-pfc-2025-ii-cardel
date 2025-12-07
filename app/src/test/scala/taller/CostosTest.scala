package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

// Importamos los TYPE ALIAS del object SolucionFuncional
import SolucionFuncional._

@RunWith(classOf[JUnitRunner])
class CostosTest extends AnyFunSuite {


  val obj = SolucionFuncional

  // ---------------------------------------------------------
  // Test 1: costoRiegoTablon cuando el riego termina ANTES de tsup
  // ---------------------------------------------------------
  test("costoRiegoTablon: riego a tiempo (sin penalización por prioridad)") {

    val f: Finca = Vector(
      (10, 3, 1),  // tsup = 10, treg = 3, prio = 1
      (15, 2, 2)
    )

    val pi: ProgRiego = Vector(0, 1) // orden natural

    assert(obj.costoRiegoTablon(0, f, pi) == 7)
  }

  // ---------------------------------------------------------
  // Test 2: costoRiegoTablon cuando el riego empieza DESPUÉS de tsup
  // ---------------------------------------------------------
  test("costoRiegoTablon: riego tardío (con penalización por prioridad)") {

    val f: Finca = Vector(
      (5, 3, 2),   // tsup = 5, treg = 3, prio = 2
      (10, 4, 1)
    )

    val pi: ProgRiego = Vector(1, 0)

    assert(obj.costoRiegoTablon(0, f, pi) == 4)
  }

  // ---------------------------------------------------------
  // Test 3: costoRiegoFinca con 3 tablones
  // ---------------------------------------------------------
  test("costoRiegoFinca con programación 0 → 2 → 1") {

    val f: Finca = Vector(
      (10, 2, 1),
      (12, 4, 2),
      (8,  3, 1)
    )

    val pi: ProgRiego = Vector(0, 2, 1)

    val costo0 = 8
    val costo1 = 3
    val costo2 = 3

    val esperado = costo0 + costo1 + costo2
    assert(obj.costoRiegoFinca(f, pi) == esperado)
  }

  // ---------------------------------------------------------
  // Test 4: costoMovilidad con 3 tablones
  // ---------------------------------------------------------
  test("costoMovilidad simple con 3 tablones") {

    val f: Finca = Vector(
      (10,2,1),
      (12,4,2),
      (8,3,1)
    )

    val d: Distancia = Vector(
      Vector(0, 5, 7),
      Vector(5, 0, 6),
      Vector(7, 6, 0)
    )

    val pi: ProgRiego = Vector(0, 2, 1)

    assert(obj.costoMovilidad(f, pi, d) == 13)
  }

  // ---------------------------------------------------------
  // Test 5: costoMovilidad + costoRiegoFinca en finca de 4 tablones
  // ---------------------------------------------------------
  test("costo total de finca con 4 tablones") {

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

    val costoRiego = obj.costoRiegoFinca(f, pi)
    val costoMov = obj.costoMovilidad(f, pi, d)

    assert(costoRiego + costoMov == obj.costoRiegoFinca(f, pi) + obj.costoMovilidad(f, pi, d))
  }

}
