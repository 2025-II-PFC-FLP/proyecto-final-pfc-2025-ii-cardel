package taller
import common._
import SolucionFuncional._
object SolucionFuncionalPar {

  // ------------------------------------------------------------------
  // Árbol de reducción paralela para sumar distancias entre tablones consecutivos
  // ------------------------------------------------------------------
  sealed trait TreeDist {
    def res: Int // Cada nodo del árbol sabe cuál es su suma parcial
  }

  case class LeafDist(dist: Int) extends TreeDist {
    def res: Int = dist // Una hoja contiene una sola distancia → su resultado es ella misma
  }

  case class NodeDist(left: TreeDist, right: TreeDist) extends TreeDist {
    def res: Int = left.res + right.res // Un nodo interno suma los resultados de sus dos hijos
  }
  //3.1 -  1
  def costoRiegoFincaPar(f: Finca, pi: ProgRiego, prof: Int, limite: Int): Int = {
    val n = f.length

    def recorrer(ini: Int, fin: Int, profundidad: Int): Int = {
      if (profundidad >= prof || (fin - ini) < limite) {
        // secuencial en el segmento
        (ini until fin).map(i => costoRiegoTablon(i, f, pi)).sum
      } else {
        val mid = (ini + fin) / 2
        val (izq, der) = parallel(
          recorrer(ini, mid, profundidad + 1),
          recorrer(mid, fin, profundidad + 1)
        )
        izq + der
      }
    }

    recorrer(0, n, 0)
  }
  //3.1 - 2 funciona auxiliar
  // Construye un árbol binario de reducción con todas las distancias entre tablones consecutivos
  def construirArbolDistancias(order: Vector[Int], d: Distancia): TreeDist = {
    if (order.length < 2) return LeafDist(0) // No hay movimientos → costo 0

    // Vector con las distancias reales: d(order(0)→order(1)), d(order(1)→order(2)), ...
    val dists = (0 until order.length - 1).map(i => d(order(i))(order(i + 1))).toVector

    // Fase ascendente: construye el árbol de abajo hacia arriba en paralelo
    def subir(ini: Int, fin: Int): TreeDist = {
      if (fin - ini <= 1) {
        LeafDist(dists(ini)) // Caso base: un solo elemento
      } else {
        val mid = (ini + fin) / 2
        val (l, r) = parallel(subir(ini, mid), subir(mid, fin)) // Divide y procesa en paralelo
        NodeDist(l, r) // Une los subárboles
      }
    }

    subir(0, dists.length) // Inicia la construcción
  }
  //3.1 - 2
  // Calcula el costo total de movilidad usando reducción paralela en árbol
  def costoMovilidadPar(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    // Reconstruye el orden real de riego a partir de la programacion de riego
    val order = Vector.tabulate(f.length)(j => pi.indexOf(j))
    // Construye el árbol y devuelve la suma total almacenada en la raíz
    construirArbolDistancias(order, d).res
  }




}