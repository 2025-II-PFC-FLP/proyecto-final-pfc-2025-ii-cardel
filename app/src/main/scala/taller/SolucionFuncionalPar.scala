package taller
import common._
import SolucionFuncional._
import common.parallel
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

  //  3.2 GENERACIÓN PARALELA
  /**
   * Genera todas las permutaciones de 0 hasta n-1 de forma paralela
   * usando divide y vencerás + parallel
   */
  def permsPar(xs: Vector[Int], prof: Int = 4): Vector[Vector[Int]] = {

    def generar(xs: Vector[Int], profundidad: Int): Vector[Vector[Int]] = {
      if (xs.isEmpty) Vector(Vector())
      else if (xs.length == 1) Vector(xs)
      else if (profundidad >= prof) {
        // Versión secuencial cuando la profundidad es suficiente
        SolucionFuncional.perms(xs)
      } else {
        // Para paralelizar, procesamos cada posible primer elemento en paralelo
        val resultados = for {
          i <- xs.indices.toVector
          elem = xs(i)
          resto = xs.patch(i, Nil, 1)  // Remove element at index i
        } yield {
          // Procesar el resto recursivamente
          if (profundidad < prof - 1 && xs.length > 3) {
            // Procesar en paralelo para casos más grandes
            parallel(
              generar(resto, profundidad + 1),
              Vector.empty[Vector[Int]] // tarea dummy para balance
            )._1.map(perm => elem +: perm)
          } else {
            // Procesar secuencialmente
            generar(resto, profundidad + 1).map(perm => elem +: perm)
          }
        }

        resultados.flatten
      }
    }

    generar(xs, 0)
  }

  /**
   * Versión alternativa más eficiente usando .par
   */
  def permsPar2(xs: Vector[Int], prof: Int = 4): Vector[Vector[Int]] = {
    if (xs.isEmpty) Vector(Vector())
    else if (xs.length == 1) Vector(xs)
    else if (prof <= 0 || xs.length <= 3) {
      // Secuencial para casos pequeños
      SolucionFuncional.perms(xs)
    } else {
      // Dividir en grupos para procesar en paralelo
      val grupos = xs.indices.grouped(math.max(1, xs.length / 4)).toVector

      val resultadosPar = grupos.flatMap { indicesGrupo =>
        // Procesar cada grupo
        indicesGrupo.flatMap { i =>
          val elem = xs(i)
          val resto = xs.patch(i, Nil, 1)
          // Generar permutaciones del resto
          val permsResto = permsPar2(resto, prof - 1)
          // Añadir el elemento al principio
          permsResto.map(perm => elem +: perm)
        }
      }

      resultadosPar
    }
  }

  /**
   * Versión que simplemente llama a la secuencial para n pequeños
   * y solo paraleliza para n grandes
   */
  def generarProgramacionesRiegoPar(f: Finca, prof: Int = 4): Vector[ProgRiego] = {
    val n = f.length

    if (n <= 6) {
      // Para n pequeño, es más eficiente hacerlo secuencial
      SolucionFuncional.generarProgramacionesRiego(f)
    } else {
      val indices = Vector.tabulate(n)(identity)
      val permutaciones = permsPar2(indices, prof)
      permutaciones.map(orderToProg)
    }
  }

  def generarProgramacionesRiegoPar(f: Finca): Vector[ProgRiego] =
    generarProgramacionesRiegoPar(f, prof = 4)




  // 3.3 BÚSQUEDA ÓPTIMA PARALELA

  /**
   * Calcula el costo total de una programación dada
   */
  def costoTotal(pi: ProgRiego, f: Finca, d: Distancia): Int = {
    val cr = costoRiegoFincaPar(f, pi, prof = 6, limite = 4)
    val cm = costoMovilidadPar(f, pi, d)
    cr + cm
  }

  /**
   * Encuentra la programación con menor costo usando paralelismo de tareas
   */
  def ProgramacionRiegoOptimoPar(f: Finca, d: Distancia, prof: Int = 6): (ProgRiego, Int) = {
    val todas = generarProgramacionesRiegoPar(f, prof)

    def buscarMejor(ini: Int, fin: Int, profundidad: Int): (ProgRiego, Int) = {
      if (fin - ini <= 1) {
        val pi = todas(ini)
        val costo = costoTotal(pi, f, d)
        (pi, costo)
      } else if (profundidad >= prof) {
        // Secuencial en el segmento
        todas.slice(ini, fin)
          .map(pi => (pi, costoTotal(pi, f, d)))
          .minBy(_._2)
      } else {
        val mid = (ini + fin) / 2
        val (mejorIzq, mejorDer) = parallel(
          buscarMejor(ini, mid, profundidad + 1),
          buscarMejor(mid, fin, profundidad + 1)
        )
        if (mejorIzq._2 <= mejorDer._2) mejorIzq else mejorDer
      }
    }

    buscarMejor(0, todas.length, 0)
  }




}