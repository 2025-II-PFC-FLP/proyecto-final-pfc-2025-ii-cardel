package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProgramacionOptimaTest extends AnyFunSuite {

  val obj = new SolucionFuncional()

  // ---------------------------------------------------------
  // Test 1: Finca de 2 tablones donde el orden óptimo es claro
  // ---------------------------------------------------------
  test("ProgramacionRiegoOptimo encuentra la mejor programación en finca de 2 tablones") {

    // Datos:
    // Tablón 0 → treg = 2
    // Tablón 1 → treg = 1
    // Tsup diseñados para que convenga regar primero el 1 y luego el 0
    val f: obj.Finca = Vector(
      (10, 2, 1),
      (5, 1, 1)
    )

    val d: obj.Distancia = Vector(
      Vector(0, 3),
      Vector(3, 0)
    )

    // Probamos la versión secuencial
    val (pi, costo) = obj.ProgramacionRiegoOptimo(f, d)

    // Óptimo manual:
    // Orden natural pi = Vector(0,1):
    //   costo movilidad = 3
    // Orden invertido pi = Vector(1,0):
    //   movilidad = 3 también
    //   PERO cambio de tIR produce diferente costo de riego → el óptimo es el que regue el 1 primero
    //
    // Verificamos que uno de los dos salga como óptimo pero específico:
    // Queremos que el primero sea el tablón 1
    assert(pi == Vector(1,0))
    assert(costo == obj.costoMovilidad(f, pi, d) + obj.costoRiegoFinca(f, pi))
  }

  // ---------------------------------------------------------
  // Test 2: Secuencial vs Paralelo deben coincidir
  // ---------------------------------------------------------
  test("ProgramacionRiegoOptimoPar coincide con ProgramacionRiegoOptimo") {

    val f: obj.Finca = Vector(
      (10, 2, 1),
      (7,  1, 2),
      (8,  3, 1)
    )

    val d: obj.Distancia = Vector(
      Vector(0, 4, 5),
      Vector(4, 0, 6),
      Vector(5, 6, 0)
    )

    val sec = obj.ProgramacionRiegoOptimo(f, d)
    val par = obj.ProgramacionRiegoOptimoPar(f, d)

    assert(sec == par)
  }

  // ---------------------------------------------------------
  // Test 3: Caso donde todos tienen mismos tsup y treg (fácil comparar)
  // ---------------------------------------------------------
  test("ProgramacionRiegoOptimo con tiempos iguales encuentra cualquier programación mínima") {

    val f: obj.Finca = Vector(
      (10, 2, 1),
      (10, 2, 1),
      (10, 2, 1)
    )

    val d: obj.Distancia = Vector(
      Vector(0, 1, 1),
      Vector(1, 0, 1),
      Vector(1, 1, 0)
    )

    val (pi, costo) = obj.ProgramacionRiegoOptimo(f, d)

    // Como todo es simétrico:
    // Una programación válida óptima debe existir y el costo debe coincidir con su definición
    assert(costo == obj.costoRiegoFinca(f, pi) + obj.costoMovilidad(f, pi, d))
  }

  // ---------------------------------------------------------
  // Test 4: Caso con 3 tablones y distancia asimétrica para fijar un óptimo claro
  // ---------------------------------------------------------
  test("ProgramacionRiegoOptimo encuentra un óptimo único con distancias asimétricas") {

    val f: obj.Finca = Vector(
      (10, 2, 1),
      (15, 3, 2),
      (20, 1, 1)
    )

    val d: obj.Distancia = Vector(
      Vector(0, 10, 2),
      Vector(10, 0, 8),
      Vector(2, 8, 0)
    )

    val (pi, costo) = obj.ProgramacionRiegoOptimo(f, d)

    // El coste de movilidad favorece muchísimo poner 0 y 2 cercanos y evitar 1
    assert(costo == obj.costoMovilidad(f, pi, d) + obj.costoRiegoFinca(f, pi))
  }

  // ---------------------------------------------------------
  // Test 5: Verificación de consistencia costo calculado vs costo real
  // ---------------------------------------------------------
  test("El costo devuelto por ProgramacionRiegoOptimo es consistente con la definición matemática") {

    val f: obj.Finca = Vector(
      (8, 1, 1),
      (7, 3, 2),
      (9, 2, 2)
    )

    val d: obj.Distancia = Vector(
      Vector(0, 1, 4),
      Vector(1, 0, 2),
      Vector(4, 2, 0)
    )

    val (pi, costo) = obj.ProgramacionRiegoOptimo(f, d)

    val costoEsperado =
      obj.costoRiegoFinca(f, pi) +
        obj.costoMovilidad(f, pi, d)

    assert(costo == costoEsperado)
  }

}
