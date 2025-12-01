package taller

import scala.collection.immutable.Vector
import TiposRiego._ // Importamos los tipos estáticamente

class Formalizacion {

  // --- Funciones Auxiliares ---
  def tsup(f: Finca, i: Int): Int = f(i)._1
  def treg(f: Finca, i: Int): Int = f(i)._2
  def prio(f: Finca, i: Int): Int = f(i)._3

  // --- 1.2.1. Cálculo del Tiempo de Inicio de Riego (tIR) ---
  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val n = f.length

    // ... (El código de tIR sigue igual)
    val tiemposInicio = pi.foldLeft((Vector.fill(n)(0), 0)) {
      case ((tiempos, tiempoAcumulado), indiceTablonActual) =>
        val nuevosTiempos = tiempos.updated(indiceTablonActual, tiempoAcumulado)
        val nuevoTiempoAcumulado = tiempoAcumulado + treg(f, indiceTablonActual)
        (nuevosTiempos, nuevoTiempoAcumulado)
    }._1

    tiemposInicio
  }

  // --- 1.2.2. Costo de Riego de un Tablón ---
  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    // ... (El resto de funciones de cálculo de 1.2 sigue igual)
    val t = tIR(f, pi)

    val ts = tsup(f, i)
    val tr = treg(f, i)
    val p = prio(f, i)
    val ti = t(i)

    val tiempoFinRiego = ti + tr

    if (tiempoFinRiego <= ts) {
      ts - tiempoFinRiego
    } else {
      p * (tiempoFinRiego - ts)
    }
  }

  // --- 1.2.3. Costo Total de Riego (CR) ---
  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    (0 until f.length).map(i => costoRiegoTablon(i, f, pi)).sum
  }

  // --- 1.2.4. Costo de Movilidad (CM) ---
  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    if (f.length < 2) 0
    else {
      pi.dropRight(1).zip(pi.drop(1))
        .map { case (origen, destino) =>
          d(origen)(destino)
        }.sum
    }
  }

  // --- Utilidad para el 1.3 ---
  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] = {
    val indices = (0 until f.length).toVector

    // Función recursiva para generar permutaciones
    def permutaciones(elementos: Vector[Int]): Vector[ProgRiego] = elementos match {
      case Vector() => Vector(Vector())
      case _ =>
        // OJO: Usar 'elementos' aquí
        elementos.flatMap { elem =>
          // CORRECCIÓN CLAVE: La variable debe ser 'elementos'
          val restantes = elementos.filterNot(_ == elem) // LÍNEA 71 corregida
          val permsRestantes = permutaciones(restantes)
          permsRestantes.map(perm => elem +: perm)
        }
    }
    permutaciones(indices)
  }
}