package taller

import taller.TiposRiego._
import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.junit.Assert._

@RunWith(classOf[JUnitRunner])
class FormalizacionTest extends AnyFunSuite {

  val formal = new Formalizacion

  // Finca de ejemplo: (tsup, treg, prio)
  val finca3: Finca = Vector(
    (10, 5, 2),
    (8, 3, 1),
    (12, 4, 3)
  )


  // tIR
  test("tIR con prog 0-1-2 devuelve los tiempos acumulados correctos") {
    val prog: ProgRiego = Vector(0,1,2)
    val esperado: TiempoInicioRiego = Vector(0, 5, 8)
    assertEquals(esperado, formal.tIR(finca3, prog))
  }

  // costoRiegoTablon
  test("costoRiegoTablon termina antes del ts y devuelve la diferencia") {
    val prog: ProgRiego = Vector(0, 1, 2)
    // Para el tablón 0: ti=0, tr=5 → fin=5 ≤ ts=10 → costo = 10 - 5 = 5
    assertEquals(5, formal.costoRiegoTablon(0, finca3, prog))
  }

  test("costoRiegoTablon aplica penalización cuando se retrasa") {
    // Finca donde sí hay retraso
    val fincaRetraso: Finca = Vector((4, 5, 2))  // ts=4, tr=5, p=2
    val prog: ProgRiego = Vector(0)
    // ti=0, fin=5 → 5 > 4 → penalización = 2*(5-4)=2
    assertEquals(2, formal.costoRiegoTablon(0, fincaRetraso, prog))
  }

  // costoRiegoFinca
  test("costoRiegoFinca suma correctamente los costos individuales") {
    val prog: ProgRiego = Vector(0, 1, 2)
    // costos: tablón0 = 5, tablón1 = 0, tablón2 = 0 → total = 5
    assertEquals(5, formal.costoRiegoFinca(finca3, prog))
  }

  // costoMovilidad
  test("costoMovilidad calcula correctamente la suma de distancias entre la secuencia") {
    val prog: ProgRiego = Vector(0, 2, 1)

    // Matriz de distancias simétrica o no, da igual:
    val d: Distancia = Vector(
      Vector(0, 4, 3),
      Vector(4, 0, 2),
      Vector(3, 2, 0)
    )

    // movimiento 0→2 (3) + 2→1 (2) = 5
    assertEquals(5, formal.costoMovilidad(finca3, prog, d))
  }

  test("costoMovilidad con una finca de un solo tablón es cero") {
    val finca1: Finca = Vector((5, 3, 1))
    val prog: ProgRiego = Vector(0)
    val d: Distancia = Vector(Vector(0))
    assertEquals(0, formal.costoMovilidad(finca1, prog, d))
  }

  // generarProgramacionesRiego
  test("generarProgramacionesRiego produce todas las permutaciones posibles") {
    val perms = formal.generarProgramacionesRiego(finca3)
    assertEquals(6, perms.length)     // 3! = 6
    assertTrue(perms.contains(Vector(2,1,0)))
    assertTrue(perms.contains(Vector(0,2,1)))
  }

}
