package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import SolucionFuncional._  // Para versiones secuenciales
import SolucionFuncionalPar._  // Para versiones paralelas

@RunWith(classOf[JUnitRunner])
class VersionesParTest3_3 extends AnyFunSuite {

  // Función auxiliar para calcular costo total
  def costoTotal(pi: ProgRiego, f: Finca, d: Distancia): Int = {
    val cr = costoRiegoFinca(f, pi)
    val cm = costoMovilidad(f, pi, d)
    cr + cm
  }

  // ---------------------------------------------------------
  // Tests para 3.3: ProgramacionRiegoOptimoPar
  // ---------------------------------------------------------

  test("ProgramacionRiegoOptimoPar coincide con la versión secuencial para finca pequeña (n=3)") {
    val f: Finca = Vector(
      (10, 3, 4),
      (5,  3, 3),
      (2,  2, 1)
    )

    val d: Distancia = Vector(
      Vector(0, 2, 2),
      Vector(2, 0, 4),
      Vector(2, 4, 0)
    )

    val (optSec, costoSec) = ProgramacionRiegoOptimo(f, d)
    val (optPar, costoPar) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 3)

    assert(costoSec == costoPar)
    // Verificar que el costo calculado para la programación óptima paralela sea correcto
    val costoParVerif = costoTotal(optPar, f, d)
    assert(costoParVerif == costoSec)
  }

  test("ProgramacionRiegoOptimoPar para n=4 con distancias uniformes") {
    val f: Finca = Vector(
      (12, 2, 1),
      (8,  4, 2),
      (10, 3, 1),
      (6,  1, 3)
    )

    val d: Distancia = Vector(
      Vector(0, 1, 1, 1),
      Vector(1, 0, 1, 1),
      Vector(1, 1, 0, 1),
      Vector(1, 1, 1, 0)
    )

    val (_, costoSec) = ProgramacionRiegoOptimo(f, d)
    val (_, costoPar) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 4)

    assert(costoSec == costoPar)
  }

  test("ProgramacionRiegoOptimoPar con prof alto (casi secuencial) debe coincidir") {
    val f: Finca = Vector(
      (15, 4, 1),
      (12, 3, 2),
      (9,  2, 1)
    )

    val d: Distancia = Vector(
      Vector(0, 5, 3),
      Vector(5, 0, 6),
      Vector(3, 6, 0)
    )

    val (optSec, costoSec) = ProgramacionRiegoOptimo(f, d)
    val (optPar, costoPar) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 10)

    assert(costoSec == costoPar)
  }

  test("La programación óptima paralela tiene costo menor o igual a cualquier permutación") {
    val f: Finca = Vector(
      (10, 3, 4),
      (8,  2, 2),
      (6,  4, 1)
    )

    val d: Distancia = Vector(
      Vector(0, 2, 4),
      Vector(2, 0, 3),
      Vector(4, 3, 0)
    )

    val (optPar, costoOptPar) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 3)
    val todas = generarProgramacionesRiego(f)

    for (pi <- todas) {
      val costoPi = costoTotal(pi, f, d)
      assert(costoOptPar <= costoPi)
    }
  }

  test("ProgramacionRiegoOptimoPar con un solo tablón") {
    val f: Finca = Vector((10, 3, 4))
    val d: Distancia = Vector(Vector(0))

    val (optSec, costoSec) = ProgramacionRiegoOptimo(f, d)
    val (optPar, costoPar) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 1)

    assert(costoSec == costoPar)
    assert(optSec == optPar)
    assert(optPar == Vector(0))
  }

  test("ProgramacionRiegoOptimoPar con dos tablones y distancias cero") {
    val f: Finca = Vector(
      (5, 2, 1),
      (7, 3, 2)
    )

    val d: Distancia = Vector(
      Vector(0, 0),
      Vector(0, 0)
    )

    val (optSec, costoSec) = ProgramacionRiegoOptimo(f, d)
    val (optPar, costoPar) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 2)

    assert(costoSec == costoPar)
  }

  test("ProgramacionRiegoOptimoPar encuentra el mínimo global") {
    val f: Finca = Vector(
      (20, 5, 4), // alta prioridad
      (10, 3, 1), // baja prioridad
      (15, 4, 2)  // prioridad media
    )

    val d: Distancia = Vector(
      Vector(0, 10, 5),
      Vector(10, 0, 8),
      Vector(5, 8, 0)
    )

    val (optPar, costoOptPar) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 3)

    // Calcular costos de todas las permutaciones para verificar
    val todas = generarProgramacionesRiego(f)
    val costos = todas.map(pi => (pi, costoTotal(pi, f, d)))
    val costoMinimo = costos.map(_._2).min

    assert(costoOptPar == costoMinimo)
  }

  test("ProgramacionRiegoOptimoPar con diferentes valores de prof encuentra el mismo costo mínimo") {
    val f: Finca = Vector(
      (12, 3, 1),
      (8,  2, 2),
      (10, 4, 1),
      (6,  1, 3)
    )

    val d: Distancia = Vector(
      Vector(0, 2, 3, 1),
      Vector(2, 0, 4, 2),
      Vector(3, 4, 0, 3),
      Vector(1, 2, 3, 0)
    )

    val (_, costoPar1) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 2)
    val (_, costoPar2) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 4)
    val (_, costoSec) = ProgramacionRiegoOptimo(f, d)

    assert(costoPar1 == costoSec)
    assert(costoPar2 == costoSec)
  }

  test("ProgramacionRiegoOptimoPar con prioridades extremas prioriza tablones críticos") {
    val f: Finca = Vector(
      (5, 2, 4),  // muy alta prioridad, poco tiempo de supervivencia
      (20, 5, 1), // baja prioridad, mucho tiempo
      (10, 3, 4)  // muy alta prioridad
    )

    val d: Distancia = Vector(
      Vector(0, 1, 1),
      Vector(1, 0, 1),
      Vector(1, 1, 0)
    )

    val (optPar, _) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d, prof = 3)

    // Los tablones con prioridad 4 deberían regarse primero
    val indicesPrioridad4 = f.zipWithIndex.filter(_._1._3 == 4).map(_._2)
    val primerosDos = optPar.take(2).toSet

    // Al menos uno de los tablones de prioridad 4 debería estar entre los primeros
    assert(indicesPrioridad4.exists(primerosDos.contains))
  }

  test("ProgramacionRiegoOptimoPar sin parámetro prof (usa valor por defecto)") {
    val f: Finca = Vector(
      (10, 2, 1),
      (12, 3, 2),
      (8,  1, 1)
    )

    val d: Distancia = Vector(
      Vector(0, 2, 3),
      Vector(2, 0, 4),
      Vector(3, 4, 0)
    )

    val (optSec, costoSec) = ProgramacionRiegoOptimo(f, d)
    val (optPar, costoPar) = SolucionFuncionalPar.ProgramacionRiegoOptimoPar(f, d)  // Sin prof

    assert(costoSec == costoPar)
  }
}