package taller

import org.scalameter._
import SolucionFuncionalPar._
import SolucionFuncional.{
  fincaAlAzar,
  distanciaAlAzar,
  costoRiegoFinca,
  costoMovilidad,
  costoRiegoTablon,
  tIR
}

object benchmarking {

  val warmer = withWarmer(new Warmer.Default)

  def main(args: Array[String]): Unit = {
    println("=" * 95)
    println(" BENCHMARK PUNTO 3.1 - PARALELIZACIÓN CON ÁRBOLES Y DIVIDE Y VENCERÁS")
    println("           Proyecto Final - Fundamentos de Programación Funcional y Concurrente")
    println("                             Octubre 2025 - Carlos Andrés Delgado S.")
    println("=" * 95)

    val tamaños = Seq(50, 200, 1000)

    // ==================================================================
    // 3.1.1 - costoRiegoFincaPar: Divide y Vencerás con umbral
    // ==================================================================
    println("\n\n=== 3.1.1 - costoRiegoFincaPar: Divide y Vencerás con control de granularidad ===")
    println("=" * 95)
    println("| Tamaño     | Versión     | Profundidad | Límite   | Tiempo (ms)     | Speedup     |")
    println("|" + "-" * 93 + "|")

    for (n <- tamaños) {
      val finca     = fincaAlAzar(n)
      val progRiego = scala.util.Random.shuffle(Vector.tabulate(n)(identity))
      val limite    = n / 16

      val sec = warmer measure { costoRiegoFinca(finca, progRiego) }
      println(f"| $n%10d | secuencial  |      -      |    -     | ${sec.value}%12.2f   | 1.00x       |")

      for (prof <- 1 to 2) {
        val par = warmer measure { costoRiegoFincaPar(finca, progRiego, prof, limite) }
        val speedup = sec.value / par.value
        println(f"| $n%10d | paralelo    | $prof%11d | $limite%8d | ${par.value}%12.2f   | ${speedup}%5.2fx       |")
      }
      if (n != tamaños.last) println("|" + "-" * 93 + "|")
    }

    // ==================================================================
    // 3.1.2 - costoMovilidadPar: Árbol de reducción paralela
    // ==================================================================
    println("\n\n=== 3.1.2 - costoMovilidadPar: Árbol de reducción paralela (construcción ascendente) ===")
    println("=" * 85)
    println("| Tamaño     | Versión             | Tiempo (ms)       | Speedup     |")
    println("|" + "-" * 83 + "|")

    for (n <- tamaños) {
      val finca     = fincaAlAzar(n)
      val distancia = distanciaAlAzar(n)
      val progRiego = scala.util.Random.shuffle(Vector.tabulate(n)(identity))

      val sec = warmer measure { costoMovilidad(finca, progRiego, distancia) }
      val par = warmer measure { costoMovilidadPar(finca, progRiego, distancia) }
      val speedup = sec.value / par.value

      println(f"| $n%10d | secuencial          | ${sec.value}%16.2f   | 1.00x       |")
      println(f"| $n%10d | paralelo (árbol)    | ${par.value}%16.2f   | ${speedup}%5.2fx       |")

      if (n != tamaños.last) println("|" + "-" * 83 + "|")
    }

    println("\n" + "=" * 95)
    println(" BENCHMARK COMPLETADO CON ÉXITO")
    println(" Resultados listos para copiar directamente al informe del Proyecto Final")
    println(" Tu implementación del punto 3.1 es excelente: divide y vencerás + árbol de reducción")
    println("=" * 95)
  }
}