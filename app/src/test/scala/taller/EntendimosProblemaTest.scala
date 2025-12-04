package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.junit.Assert._
import TiposRiego._

@RunWith(classOf[JUnitRunner])
class EntendimosProblemaTest extends AnyFunSuite {

  val solver = new EntendimosProblema
  val formal = new Formalizacion


  // Datos base para los tests
  val finca3: Finca = Vector(
    (10, 5, 2),
    (8, 3, 1),
    (12, 4, 3)
  )

  val d3: Distancia = Vector(
    Vector(0, 4, 3),
    Vector(4, 0, 2),
    Vector(3, 2, 0)
  )

  // ---------------------------------------------------------
  test("ProgramacionRiegoOptimo encuentra la programación con costo total mínimo") {

    val (progOpt, costoOpt) = solver.ProgramacionRiegoOptimo(finca3, d3)

    val todas = formal.generarProgramacionesRiego(finca3)
    assertTrue(todas.contains(progOpt))

    val costos = todas.map(pi => formal.costoRiegoFinca(finca3, pi) + formal.costoMovilidad(finca3, pi, d3))
    val minimoReal = costos.min

    assertEquals(minimoReal, costoOpt)
  }

  // ---------------------------------------------------------
  test("ProgramacionRiegoOptimo con una finca de un solo tablón debe devolver la única programación") {

    val finca1: Finca = Vector((5, 3, 1))
    val dist1: Distancia = Vector(Vector(0))

    val (progOpt, costoOpt) = solver.ProgramacionRiegoOptimo(finca1, dist1)

    assertEquals(Vector(0), progOpt)
    assertEquals(2, costoOpt) // ts=5, fin=3 → costo=2
  }

  // ---------------------------------------------------------
  test("ProgramacionRiegoOptimo en finca vacía devuelve programación vacía y costo 0") {

    val finca0: Finca = Vector()
    val dist0: Distancia = Vector()

    val (progOpt, costoOpt) = solver.ProgramacionRiegoOptimo(finca0, dist0)

    assertEquals(Vector(), progOpt)
    assertEquals(0, costoOpt)
  }

  // ---------------------------------------------------------
  test("La programación óptima realmente tiene el costo total mínimo frente a todas las permutaciones") {

    val (progOpt, costoOpt) = solver.ProgramacionRiegoOptimo(finca3, d3)

    val todas = formal.generarProgramacionesRiego(finca3)

    val costoProgOpt =
      formal.costoRiegoFinca(finca3, progOpt) + formal.costoMovilidad(finca3, progOpt, d3)

    val costos = todas.map(pi => formal.costoRiegoFinca(finca3, pi) + formal.costoMovilidad(finca3, pi, d3))
    val minimoReal = costos.min

    assertEquals(minimoReal, costoProgOpt)
    assertEquals(minimoReal, costoOpt)
  }


  // Movilidad extremadamente grande
  test("ProgramacionRiegoOptimo elige secuencia que minimiza movilidad cuando las distancias son enormes") {

    val distGrande: Distancia = Vector(
      Vector(0, 1000, 1000),
      Vector(1000, 0, 1000),
      Vector(1000, 1000, 0)
    )

    val (progOpt, costoOpt) = solver.ProgramacionRiegoOptimo(finca3, distGrande)

    // Aquí la movilidad domina completamente el costo → evitar saltos
    // Las secuencias que minimizan movilidad son: (0,1,2) y (2,1,0)
    val validos = Set(Vector(0,1,2), Vector(2,1,0))

    assertTrue(validos.contains(progOpt))
  }


  // TEST EXTRA 2: Penalización por riego extremadamente alta

  test("ProgramacionRiegoOptimo prioriza el tablón con mayor penalización cuando el riego se retrasa mucho") {

    // Penalización enorme en un tablón
    val fincaPenal: Finca = Vector(
      (5, 5, 50), // si se retrasa, cuesta MUCHO
      (10, 3, 1),
      (12, 4, 1)
    )

    val distNormal: Distancia = Vector(
      Vector(0, 4, 3),
      Vector(4, 0, 2),
      Vector(3, 2, 0)
    )

    val (progOpt, _) = solver.ProgramacionRiegoOptimo(fincaPenal, distNormal)

    // El tablón con prio=50 debe ser regado primero para evitar retrasos
    assertEquals(0, progOpt.head)
  }
}
