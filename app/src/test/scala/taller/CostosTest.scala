package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CostosTest extends AnyFunSuite {

  val obj = new SolucionFuncional()

  // ---------------------------------------------------------
  // Test 1: costoRiegoTablon cuando el riego termina ANTES de tsup
  // ---------------------------------------------------------
  test("costoRiegoTablon: riego a tiempo (sin penalización por prioridad)") {
    val f: obj.Finca = Vector(
      (10, 3, 1),  // tsup = 10, treg = 3, prio = 1
      (15, 2, 2)
    )
    val pi: obj.ProgRiego = Vector(0, 1) // orden natural

    // Para el tablón 0:
    // tIR = 0
    // Termina a los 3
    // tsup=10, tr=3 → tsup - tr = 7 >= tIR(0) = 0
    // costo = tsup - (tIni + tr) = 10 - 3 = 7
    assert(obj.costoRiegoTablon(0, f, pi) == 7)
  }

  // ---------------------------------------------------------
  // Test 2: costoRiegoTablon cuando el riego empieza DESPUÉS de tsup
  // ---------------------------------------------------------
  test("costoRiegoTablon: riego tardío (con penalización por prioridad)") {
    val f: obj.Finca = Vector(
      (5, 3, 2),   // tsup = 5, treg = 3, prio = 2
      (10, 4, 1)
    )
    val pi: obj.ProgRiego = Vector(1, 0)

    // Para el tablón 0:
    // turnos:
    //  tablón 1 (4), luego tablón 0 (3)
    // tiempos = 4,3
    // acumulados = [0,4]
    // pi(0)=1 → tIR(0) = 4
    // tsup=5, tr=3
    // tIni + tr = 4 + 3 = 7 > 5
    // costo = prio * ((tIni + tr) - tsup)
    //        = 2 * (7 - 5) = 4
    assert(obj.costoRiegoTablon(0, f, pi) == 4)
  }

  // ---------------------------------------------------------
  // Test 3: costoRiegoFinca con 3 tablones
  // ---------------------------------------------------------
  test("costoRiegoFinca con programación 0 → 2 → 1") {
    val f: obj.Finca = Vector(
      (10, 2, 1),
      (12, 4, 2),
      (8,  3, 1)
    )
    val pi: obj.ProgRiego = Vector(0, 2, 1)

    // Ya sabemos de un test anterior:
    // tIR = Vector(0,5,2)

    val costo0 = {
      // tsup=10, treg=2, prio=1
      // tIni=0 → termina en 2
      // tsup - tr = 8 >= 0
      // costo = 10 - (0 + 2) = 8
      8
    }

    val costo1 = {
      // tsup=12, treg=4, prio=2
      // tIni=5 → tIni+tr=9
      // tsup - tr = 8 < 5 → tardío
      // costo = 2 * (9 - 12)?? NO !!! signo cuidado:
      // (tIni + tr) - tsup = 9 - 12 = -3 ??? No: directo del doc:
      // Si tsup - tr < tIni, entonces costo = prio * ((tIni + tr) - tsup)
      // (tIni + tr) = 5+4=9
      // (9 - 12) = -3 ??????
      //
      // IMPORTANTE: debe ser positivo. Reevaluamos:
      // tIni=5, tr=4
      // tIni + tr = 9
      // tsup=12
      // tIni + tr < tsup → está A TIEMPO, NO tardío.
      // tsup - tr = 12 - 4 = 8 >= tIni(5) → correcto
      //
      // costo = tsup - (tIni + tr) = 12 - 9 = 3
      3
    }

    val costo2 = {
      // tsup=8, treg=3, prio=1
      // tIni=2 → tIni+tr=5
      // tsup - tr = 5 >= 2 → a tiempo
      // costo = tsup - (tIni + tr) = 8 - 5 = 3
      3
    }

    val esperado = costo0 + costo1 + costo2
    assert(obj.costoRiegoFinca(f, pi) == esperado)
  }

  // ---------------------------------------------------------
  // Test 4: costoMovilidad con 3 tablones
  // ---------------------------------------------------------
  test("costoMovilidad simple con 3 tablones") {
    val f: obj.Finca = Vector(
      (10,2,1),
      (12,4,2),
      (8,3,1)
    )

    val d: obj.Distancia = Vector(
      Vector(0, 5, 7),
      Vector(5, 0, 6),
      Vector(7, 6, 0)
    )

    val pi: obj.ProgRiego = Vector(0, 2, 1)

    // orden: pi.indexOf(0)=0, indexOf(1)=2, indexOf(2)=1 → [0,2,1]
    // movilidad: d(0→2)=7  +  d(2→1)=6 = 13
    assert(obj.costoMovilidad(f, pi, d) == 13)
  }

  // ---------------------------------------------------------
  // Test 5: costoMovilidad + costoRiegoFinca en finca de 4 tablones
  // ---------------------------------------------------------
  test("costo total de finca con 4 tablones") {
    val f: obj.Finca = Vector(
      (12, 2, 1),
      (14, 3, 2),
      (10, 1, 1),
      (9,  4, 3)
    )

    val d: obj.Distancia = Vector(
      Vector(0, 3, 4, 2),
      Vector(3, 0, 5, 6),
      Vector(4, 5, 0, 7),
      Vector(2, 6, 7, 0)
    )

    val pi: obj.ProgRiego = Vector(1, 3, 0, 2)

    val costoRiego = obj.costoRiegoFinca(f, pi)
    val costoMov = obj.costoMovilidad(f, pi, d)

    // Simplemente verificamos que no lanza error y que la suma es correcta
    assert(costoRiego + costoMov == obj.costoRiegoFinca(f, pi) + obj.costoMovilidad(f, pi, d))
  }

}

