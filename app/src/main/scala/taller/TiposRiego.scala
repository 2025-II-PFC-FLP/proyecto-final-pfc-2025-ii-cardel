package taller

import scala.collection.immutable.Vector

// Definimos los tipos dentro de un 'object' para que sean accesibles estáticamente
object TiposRiego {

  type Tablon = (Int, Int, Int)            // (TiempoSupervivencia, TiempoRegado, Prioridad)
  type Finca = Vector[Tablon]
  type Distancia = Vector[Vector[Int]]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]

}
