package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import SolucionFuncional._

@RunWith(classOf[JUnitRunner])
class TiempoInicioRiegoTest extends AnyFunSuite {

  val obj = SolucionFuncional

  // ---------------------------
  // Test 1: Caso simple
  // ---------------------------
  test("tIR con finca de 2 tablones y programación identidad") {
    val f: Finca = Vector(
      (10, 3, 1),  // tsup, treg, prio
      (12, 2, 2)
    )
    val pi: ProgRiego = Vector(0, 1) // en orden natural

    val esperado = Vector(0, 3) // t0 = 0, t1 = treg(0) = 3
    assert(tIR(f, pi) == esperado)
  }

  // ---------------------------
  // Test 2: Cambio de orden
  // ---------------------------
  test("tIR con programación invertida") {
    val f: Finca = Vector(
      (10, 3, 1),
      (12, 2, 2)
    )
    val pi: ProgRiego = Vector(1, 0) // ahora primero riega el 1

    // orden: tablón 1 → tablón 0
    // tiempos: t1 = 0, t0 = treg(1) = 2
    val esperado = Vector(2, 0)
    assert(tIR(f, pi) == esperado)
  }

  // ---------------------------
  // Test 3: Tres tablones
  // ---------------------------
  test("tIR con 3 tablones en orden 0 → 2 → 1") {
    val f: Finca = Vector(
      (10, 2, 1), // t0=2
      (12, 4, 2), // t1=4
      (8, 3, 1)   // t2=3
    )

    // Programa:
    // tablón 0 en turno 0
    // tablón 1 en turno 2
    // tablón 2 en turno 1
    val pi: ProgRiego = Vector(0, 2, 1)

    // orden: 0, 2, 1
    // tiempos: 2, 3, 4
    // acumulados: [0, 2, 5]
    // tIR:
    //  t0 = acumulados(0) = 0
    //  t1 = acumulados(2) = 5
    //  t2 = acumulados(1) = 2
    val esperado = Vector(0, 5, 2)

    assert(tIR(f, pi) == esperado)
  }

  // ---------------------------
  // Test 4: Todos los tiempos iguales
  // ---------------------------
  test("tIR con treg iguales para todos los tablones") {
    val f: Finca = Vector(
      (10, 5, 1),
      (12, 5, 2),
      (11, 5, 3)
    )

    val pi: ProgRiego = Vector(2, 0, 1)
    // orden: tablones en turnos 0,1,2 son: 1,2,0 → según pi.indexOf(turno)
    // tiempos de riego: treg(1)=5, treg(2)=5, treg(0)=5
    // acumulados: [0, 5, 10]
    // asignación por pi:
    // t0 = acumulados(pi(0)=2) = 10
    // t1 = acumulados(pi(1)=0) = 0
    // t2 = acumulados(pi(2)=1) = 5

    val esperado = Vector(10, 0, 5)
    assert(tIR(f, pi) == esperado)
  }

  // ---------------------------
  // Test 5: Caso de 4 tablones
  // ---------------------------
  test("tIR con 4 tablones y programación arbitraria") {
    val f: Finca = Vector(
      (15, 1, 1),  // t0 = 1
      (12, 3, 2),  // t1 = 3
      (14, 2, 3),  // t2 = 2
      (10, 4, 2)   // t3 = 4
    )

    val pi: ProgRiego = Vector(3, 0, 2, 1)

    // orden de riego:
    // turno 0 → tablón 1
    // turno 1 → tablón 3
    // turno 2 → tablón 2
    // turno 3 → tablón 0

    // tiempos: 3, 4, 2, 1
    // acumulados: [0, 3, 7, 9]

    // t0 = acumulados(pi(0)=3) = 9
    // t1 = acumulados(pi(1)=0) = 0
    // t2 = acumulados(pi(2)=2) = 7
    // t3 = acumulados(pi(3)=1) = 3

    val esperado = Vector(9, 0, 7, 3)

    assert(tIR(f, pi) == esperado)
  }

}
