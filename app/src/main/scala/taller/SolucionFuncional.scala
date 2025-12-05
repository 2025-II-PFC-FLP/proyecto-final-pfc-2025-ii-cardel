package taller


import scala.util.Random
import scala.collection.parallel.CollectionConverters._


object SolucionFuncional {


  // ================ 2 TIPOS DE RIEGO ================
  // Un tablon es una tripleta
  type Tablon = (Int, Int, Int)            // (TiempoSupervivencia, TiempoRegado, Prioridad)

  // Una finca es un vector de tablones
  type Finca = Vector[Tablon]              // Si f : Finca, f(i) = (tsi, tri, pi)

  // La distancia entre dos tablones se re  presenta por una matriz
  type Distancia = Vector[Vector[Int]]

  // Vector que asocia cada tablon i con su turno de riego
  // (0 es el primer turno, n-1 es el ´ultimo turno)
  type ProgRiego = Vector[Int]             // n-1 es el ´ultimo turno)
  // Si v : ProgRiego, y v.length == n, v es una permutación
  // de {0, ..., n-1} v(i) es el turno de riego del tablon i para 0 <= i < n

  // El tiempo de inicio de riego es un vector que asocia
  // cada tablon i con el momento del tiempo en que se riega
  type TiempoInicioRiego = Vector[Int]
  // Si t : TiempoInicioRiego y t.length == n, t(i) es la hora a
  // la que inicia a regarse el tablon i


  // =============== 2.1 GENERACIÓN AL AZAR ===============
  private val random = new Random()

  def fincaAlAzar(long: Int): Finca =
    Vector.fill(long)(
      (
        random.nextInt(long * 2) + 1, // tsup
        random.nextInt(long) + 1,     // treg
        random.nextInt(4) + 1         // prioridad
      )
    )

  def distanciaAlAzar(long: Int): Distancia = {
    val base = Vector.fill(long, long)(random.nextInt(long * 3) + 1)

    Vector.tabulate(long, long) { (i, j) =>
      if (i == j) 0
      else if (i < j) base(i)(j)
      else base(j)(i)
    }
  }


  // ==================== 2.2 EXPLORACIÓN =================
  def tsup(f: Finca, i: Int): Int = f(i)._1
  def treg(f: Finca, i: Int): Int = f(i)._2
  def prio(f: Finca, i: Int): Int = f(i)._3


  // ========== 2.3 TIEMPO DE INICIO DE RIEGO =============
  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    val n = f.length

    // order(j) = índice del tablón regado en el turno j
    val order = Vector.tabulate(n)(j => pi.indexOf(j))

    // tiempos de regado en ese orden
    val tiempos = order.map(i => treg(f, i))

    // acumulados: [0, t0, t0+t1, ...]
    val acumulados = tiempos.scanLeft(0)(_ + _).init

    // t(i) = acumulado en el turno pi(i)
    Vector.tabulate(n)(i => acumulados(pi(i)))
  }


  // ==================== 2.4 COSTOS =======================
  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    val tIni = tIR(f, pi)(i)
    val ts = tsup(f, i)
    val tr = treg(f, i)
    val p  = prio(f, i)

    if (ts - tr >= tIni)
      ts - (tIni + tr)
    else
      p * ((tIni + tr) - ts)
  }

  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int =
    Vector.tabulate(f.length)(i => costoRiegoTablon(i, f, pi)).sum

  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    val n = f.length
    val order = Vector.tabulate(n)(j => pi.indexOf(j))

    order
      .sliding(2)
      .collect { case Vector(a, b) => d(a)(b) }
      .sum
  }


  // ======= VERSIONES PARALELAS (SOLO .par) ==============

  // ==================== 2.4
  def costoRiegoFincaPar(f: Finca, pi: ProgRiego): Int =
    Vector.tabulate(f.length)(i => costoRiegoTablon(i, f, pi)).par.sum

  def costoMovilidadPar(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    val order = Vector.tabulate(f.length)(j => pi.indexOf(j))

    order
      .sliding(2)
      .toVector
      .par
      .map { case Vector(a, b) => d(a)(b) }
      .sum
  }


  // ============ 2.5 GENERAR PROGRAMACIONES =============
  // Permutaciones funcionales
  def perms(xs: Vector[Int]): Vector[Vector[Int]] =
    if (xs.isEmpty) Vector(Vector())
    else
      xs.flatMap(x =>
        perms(xs.filterNot(_ == x)).map(rest => x +: rest)
      )

  def orderToProg(order: Vector[Int]): ProgRiego =
    Vector.tabulate(order.length)(i => order.indexOf(i))

  def generarProgramacionesRiego(f: Finca): Vector[ProgRiego] =
    perms(Vector.tabulate(f.length)(i => i)).map(orderToProg)

  def generarProgramacionesRiegoPar(f: Finca): Vector[ProgRiego] =
    perms(Vector.tabulate(f.length)(i => i))
      .par
      .map(orderToProg)
      .toVector


  // =========== 2.6 PROGRAMACIÓN ÓPTIMA =================
  def ProgramacionRiegoOptimo(f: Finca, d: Distancia): (ProgRiego, Int) =
    generarProgramacionesRiego(f)
      .map { pi =>
        val costo = costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)
        (pi, costo)
      }
      .minBy(_._2)

  def ProgramacionRiegoOptimoPar(f: Finca, d: Distancia): (ProgRiego, Int) =
    generarProgramacionesRiegoPar(f)
      .par
      .map { pi =>
        val costo = costoRiegoFincaPar(f, pi) + costoMovilidadPar(f, pi, d)
        (pi, costo)
      }
      .minBy(_._2)

}


