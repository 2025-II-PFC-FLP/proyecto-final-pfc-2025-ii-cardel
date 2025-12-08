package taller

import org.scalameter._
import SolucionFuncional.{fincaAlAzar, generarProgramacionesRiego}
import SolucionFuncionalPar.{generarProgramacionesRiegoPar}

object Benchmark3_4_Generacion {

  val warmer = withWarmer(new Warmer.Default)

  val tamaños = Seq(5, 6, 7, 8)

  def main(args: Array[String]): Unit = {

    println("========================================================")
    println(" BENCHMARK 3.4  Generacion de Programaciones (perms)")
    println("========================================================")
    println("| n | Secuencial (ms) | Paralelo (ms) | Speedup |")
    println("|---|------------------|--------------|---------|")

    for (n <- tamaños) {

      val finca = fincaAlAzar(n) // solo para tamaño

      val sec = warmer measure {
        generarProgramacionesRiego(finca)
      }

      val par = warmer measure {
        generarProgramacionesRiegoPar(finca)
      }

      val speed = sec.value / par.value

      println(f"| $n%2d | ${sec.value}%16.2f | ${par.value}%12.2f | ${speed}%7.2f |")
    }
  }
}
