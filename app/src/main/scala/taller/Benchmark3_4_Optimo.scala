package taller

import org.scalameter._
import SolucionFuncional.{fincaAlAzar, distanciaAlAzar, ProgramacionRiegoOptimo}
import SolucionFuncionalPar.{ProgramacionRiegoOptimoPar}

object Benchmark3_4_Optimo {

  val warmer = withWarmer(new Warmer.Default)

  val tamaños = Seq(6, 7, 8, 9, 10)

  def main(args: Array[String]): Unit = {

    println("========================================================")
    println(" BENCHMARK 3.4  Programacion Optima")
    println("========================================================")
    println("| n | Secuencial (ms) | Paralelo (ms) | Speedup |")
    println("|---|------------------|--------------|---------|")

    for (n <- tamaños) {

      val finca = fincaAlAzar(n)
      val dist  = distanciaAlAzar(n)

      val sec = warmer measure {
        ProgramacionRiegoOptimo(finca, dist)
      }

      val par = warmer measure {
        ProgramacionRiegoOptimoPar(finca, dist)
      }

      val speed = sec.value / par.value

      println(f"| $n%2d | ${sec.value}%16.2f | ${par.value}%12.2f | ${speed}%7.2f |")
    }
  }
}
