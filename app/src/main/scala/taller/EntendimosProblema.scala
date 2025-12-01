package taller

import scala.collection.immutable.Vector
// Importamos los tipos de datos comunes definidos en el objeto TiposRiego para que
// las firmas de las funciones (Finca, Distancia, ProgRiego, etc.) sean válidas.
import TiposRiego._

class EntendimosProblema {

  // Instancia de la clase Formalizacion.
  // Necesitas esta instancia para llamar a los métodos de cálculo del punto 1.2.
  private val formalizacion = new Formalizacion()

  // --- 1.2.5. y 2.6. Programación de Riego Óptimo (Solución 1.3) ---

  /**
   * Resuelve el Problema del Riego Óptimo: encuentra la programación pi que minimiza
   * el costo total combinado (Costo Riego + Costo Movilidad).
   * * @param f La finca (entrada)
   * @param d La matriz de distancias (entrada)
   * @return Una tupla (Programación Óptima, Costo Mínimo Total)
   */
  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) = {

    // 1. Generar todas las programaciones posibles
    val todasLasProgramaciones = formalizacion.generarProgramacionesRiego(f)

    // 2. Calcular el costo total (Costo Riego + Costo Movilidad) para cada programación
    val costosPorProgramacion = todasLasProgramaciones.map { pi =>
      // Usamos los métodos de la instancia 'formalizacion' para los cálculos de costo
      val costoRiego = formalizacion.costoRiegoFinca(f, pi)
      val costoMov = formalizacion.costoMovilidad(f, pi, d)
      val costoTotal = costoRiego + costoMov
      (pi, costoTotal)
    }

    // 3. Encontrar la programación con el costo total mínimo.
    if (costosPorProgramacion.isEmpty)
      (Vector(), 0)
    else
      costosPorProgramacion.minBy(_._2)
  }
}