package taller


import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import SolucionFuncional._

@RunWith(classOf[JUnitRunner])
class GenerarProgramacionesTest extends AnyFunSuite {

  val obj = SolucionFuncional

  // ---------------------------------------------------------
  // Test 1: perms para un vector pequeño
  // ---------------------------------------------------------
  test("perms genera todas las permutaciones de Vector(0,1,2)") {
    val entrada = Vector(0, 1, 2)
    val esperado = Set(
      Vector(0,1,2), Vector(0,2,1),
      Vector(1,0,2), Vector(1,2,0),
      Vector(2,0,1), Vector(2,1,0)
    )

    val resultado = perms(entrada).toSet

    assert(resultado == esperado)
  }

  // ---------------------------------------------------------
  // Test 2: orderToProg convierte una permutación en vector de turnos
  // ---------------------------------------------------------
  test("orderToProg convierte correctamente order → pi") {
    // orden: 2, 0, 1
    val order = Vector(2, 0, 1)

    // pi(i) = posición donde aparece el tablón i
    // 0 está en la posición 1
    // 1 está en la posición 2
    // 2 está en la posición 0
    val esperado = Vector(1, 2, 0)

    assert(orderToProg(order) == esperado)
  }

  // ---------------------------------------------------------
  // Test 3: generarProgramacionesRiego para finca de 2 tablones
  // ---------------------------------------------------------
  test("generarProgramacionesRiego para finca de 2 tablones produce 2 programaciones") {
    val f: Finca = Vector(
      (10, 2, 1),
      (12, 3, 2)
    )

    val resultado = generarProgramacionesRiego(f)

    // Permutaciones posibles para 2 tablones:
    val esperado = Set(
      Vector(0,1),  // orden 0,1
      Vector(1,0)   // orden 1,0
    )

    assert(resultado.toSet == esperado)
  }

  // ---------------------------------------------------------
  // Test 4: generarProgramacionesRiego vs generarProgramacionesRiegoPar
  // ---------------------------------------------------------
  test("generarProgramacionesRiegoPar coincide con la versión secuencial") {
    val f: Finca = Vector(
      (10, 2, 1),
      (11, 3, 1),
      (8,  1, 2)
    )

    val sec = generarProgramacionesRiego(f).toSet
    val par = generarProgramacionesRiegoPar(f).toSet

    assert(sec == par)
  }

  // ---------------------------------------------------------
  // Test 5: número de permutaciones correcto para 3 tablones
  // ---------------------------------------------------------
  test("generarProgramacionesRiego genera factorial(3) = 6 programaciones") {
    val f: Finca = Vector(
      (5, 1, 1),
      (7, 2, 2),
      (9, 1, 3)
    )

    val resultado = generarProgramacionesRiego(f)

    assert(resultado.length == 6)
  }

}

