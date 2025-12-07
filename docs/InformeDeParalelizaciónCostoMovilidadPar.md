# informe de Paralelización Funcion costoMovilidad

```scala
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

```


se paralelizó la función **costoMovilidad**, encargada de calcular el costo de movilidad entre tablones.

La versión paralela implementada (`costoMovilidadPar`) utiliza una estrategia de **divide y vencerás con control de granularidad**,
basada en el uso de `common.parallel` 
y un parámetro de profundidad (`prof`) junto con un umbral de tamaño de segmento (`limite`)

### Estrategia de paralelización empleada
La versión paralela emplea la construcción ascendente de un árbol binario de reducción.

```scala
val (l, r) = parallel(subir(ini, mid), subir(mid, fin))
NodeDist(l, r)
```
Cada hoja del árbol posee una distancia, cada nodo interno suma sus dos hijos.
El resultado final es la suma total disponible en la raíz `res`
### Importancia de la Función auxiliar construirArbolDistancias
La función `costoMovilidadPar` delega el trabajo principal a `construirArbolDistancias`, 
que implementa la construcción paralela de un árbol binario de reducción. Por lo tanto, 
la fracción paralelizable del algoritmo y el comportamiento descrito por la Ley de Amdahl dependen íntegramente de esta función auxiliar, 
ya que es donde se generan todas las tareas paralelas y donde se producen las sincronizaciones por nivel.

Ademas la funcion `subir` ayuda a manejar la dependencia secuencial de la ecuación.
$$
CM_{\pi} = \sum_{j=0}^{n-2} D[\pi_j, \pi_{j+1}]
$$
mediante la implementación: 
```scala
total += d(pi(j))(pi(j+1))

```
La estrategia del árbol rompe esta dependencia, ya que:
- Cada par de distancias se procesa por separado.
- Los valores se combinan solo al final, en niveles superiores.
- No existe una variable acumuladora compartida.
En cada nivel, los nodos combinan resultados parciales consecutivos:
  $$
  (d_0 + d_1),\;\; (d_2 + d_3),\;\; (d_4 + d_5),\;\; \ldots
  $$

### Control de granularidad

Para evitar sobrecrear tareas y saturar el sistema, se utilizó un
parámetro de profundidad de paralelización prof:

Si prof <= 0, se usa la versión secuencial.

Cada incremento en prof duplica el paralelismo potencial:

prof = 1 → 2 tareas

prof = 2 → 4 tareas

prof = 3 → 8 tareas

prof = 4 → 16 tareas

Sin embargo el ordenador empleado en las pruebas solo dispone de 4 hilos.

### Benchmarking

Se realizaron pruebas sobre 50, 300 y 1000 expresiones generadas aleatoriamente.
En todos los casos se midió el tiempo secuencial y el tiempo paralelo para profundidades desde 1
hasta 4.

| Tamaño | Versión    | Tiempo  | Speedup |
| ------ |------------|---------| ------- |
| 50     | secuencial | 0.34 ms | 1.00×   |
| 50     | paralelo   | 0.28 ms | 1.18×   |
| 200    | secuencial | 1.65 ms | 1.00×   |
| 200    | paralelo   | 0.68 ms | 2.44×   |
| 1000   | secuencial | 7.62 ms | 1.00×   |
| 1000   | paralelo   | 6.42 ms | 1.19×   |



### Análisis de los resultados
- `n = 200` obtiene una aceleración de 2.44×, el arbol genera 7 niveles los cuales permiten una buena superposicion de trabajo.
- `n = 50 ` se realiza poco trabajo, por lo tanto el overhead es comparable al trabajo real.
- `n = 1000` el trabajo por tarea es muy pequeño 
(solo acceder a una matriz), 
por lo que el overhead de crear 
cientos de tareas supera el beneficio,
resultando en solo 1.19×.
  - Aunque el algoritmo es teóricamente óptimo, existen limitaciones prácticas:
    - **Granularidad extremadamente fina**: 
    Cada operación elemental es un simple acceso a matriz `d(i)(j)`. 
    El coste de crear una tarea `parallel` 
    es mucho mayor que el coste del trabajo.
    - **Sincronización implícita en cada nivel**:
      Aunque la construcción es paralela, cada nivel debe esperar a que terminen los dos hijos.
    - **Número limitado de niveles útiles**:
      Con 4 hilos, solo se pueden ejecutar 4 tareas simultáneamente. A partir del 
      nivel 3 ya se generan más tareas de las que pueden correr en paralelo, lo cual deriva en mayor overhead.
    
### Aplicación de la Ley de Amdahl
La Ley de Amdahl establece:
$
S(p) = \frac{1}{(1 - f) + \frac{f}{p}}
$
donde:

- $ p $ es el número de procesadores (en este caso, $p = 4$ ),
  a pesar de que el mejor resultado proviene de  $prof = 2$ el algoritmo emplea 4 hilos.
- $f $ es la fracción paralelizable del programa,
- $S(p)$ es la aceleración obtenida.

La aceleración máxima observada experimentalmente fue:
$
S_{\text{exp}} \approx 2.44 
$ `para n = 200`

Despejando $ f $ de la expresión de Amdahl:

$
2.44   = \frac{1}{(1 - f) + \frac{f}{4}}
$

$
(1 - f) + \frac{f}{4} = \frac{1}{2.44}
$

$
(1 - f) + \frac{f}{4} \approx 0.4098
$

$
1 - f + \frac{f}{4} = 0.4098
$

$
1 - 0.4098 = f - \frac{f}{4}
$

$
0.5902= \frac{3}{4}f
$

$
f \approx 0.787
$

por lo tanto, La fracción paralelizable estimada es:
$f \approx 78.7%$

- esto sugiere que casi el 80% del algoritmo es paralelizable y casi un 20% inevitablemente secuencial.
### Conclusiones
  - El algoritmo alcanza una fracción paralelizable alta cercana al 80%, pero no total. 
  - El speedup real (2.44×) está cerca del límite razonable para 4 hilos. considerando:La limitación física de 4 hilos
    La limitación física de 4 hilos y La sincronización necesaria en cada nivel del árbol.
  - El rendimiento se degrada cuando el tamaño es muy pequeño causando que el overhead sea similar al trabajo real.
  - costoMovilidadPar se beneficia del paralelismo, pero su naturaleza de reducción impone una fracción secuencial significativa que limita sus aceleraciones máximas.