// scala
package taller

import org.junit.Test
import org.junit.Assert._
import TiposRiego._

class FormalizacionTest {
  val formal = new Formalizacion

  // Finca de ejemplo: (tsup, treg, prio)
  val finca3: Finca = Vector((10, 5, 2), (8, 3, 1), (12, 4, 3))

  @Test
  def tIR_con_programacion_0_1_2_devuelve_tiempos_acumulados(): Unit = {
    val prog: ProgRiego = Vector(0, 1, 2)
    val esperado: TiempoInicioRiego = Vector(0, 5, 8)
    assertEquals(esperado, formal.tIR(finca3, prog))
  }

  @Test
  def costoRiegoTablon_termina_antes_o_en_ts_devuelve_diferencia(): Unit = {
    val prog: ProgRiego = Vector(0, 1, 2)
    // Para el tablón 0: ti=0, tr=5, ts=10 => fin=5 <= ts => costo = 10 - 5 = 5
    assertEquals(5, formal.costoRiegoTablon(0, finca3, prog))
  }

  @Test
  def costoRiegoTablon_aplica_penalizacion_cuando_se_retrasa(): Unit = {
    // Finca pequeña donde el tablón se retrasa: ts=4, tr=5, p=2 -> ti=0, fin=5 => penalización 2*(5-4)=2
    val fincaRetraso: Finca = Vector((4, 5, 2))
    val prog: ProgRiego = Vector(0)
    assertEquals(2, formal.costoRiegoTablon(0, fincaRetraso, prog))
  }

  @Test
  def costoRiegoFinca_suma_correctamente_los_costes(): Unit = {
    val prog: ProgRiego = Vector(0, 1, 2)
    // costes por tablón en finca3: 5, 0, 0 => total 5
    assertEquals(5, formal.costoRiegoFinca(finca3, prog))
  }

  @Test
  def generarProgramacionesRiego_produce_todas_las_permutaciones(): Unit = {
    val perms = formal.generarProgramacionesRiego(finca3)
    // Para 3 tablones hay 3! = 6 permutaciones
    assertEquals(6, perms.length)
    // verificar que contiene una permutación conocida
    assertTrue(perms.contains(Vector(2, 1, 0)))
  }
}
