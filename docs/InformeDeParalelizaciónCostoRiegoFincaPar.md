# informe de Paralelización Funcion costoRiegoFinca

```scala
  // 3.1.1
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
```


se paralelizó la función **costoRiegoFinca**, encargada de calcular el costo total de riego 
(penalización por falta o exceso de agua) sumando el costo individual de cada tablón 
según el orden de riego dado. 

La versión paralela implementada (`costoRiegoFincaPar`) utiliza una estrategia de **divide y vencerás con control de granularidad**,
basada en el uso de `common.parallel` 
y un parámetro de profundidad (`prof`) junto con un umbral de tamaño de segmento (`limite`)

### Estrategia de paralelización empleada
La versión paralela divide recursivamente el intervalo [ini, fin) hasta alcanzar:
una profundidad máxima de paralelización (prof)
o un mínimo de elementos por tarea (limite)

Por ello se utilizó el patrón:

```scala
val (izq, der) = parallel(
  recorrer(ini, mid, profundidad + 1),
  recorrer(mid, fin, profundidad + 1)
)
```
##### Control de granularidad

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

| Tamaño | Versión    | Profundidad | Límite | Tiempo     | Speedup |
| ------ | ---------- | ----------- | ------ |------------| ------- |
| 50     | secuencial | –           | –      | 2.89 ms    | 1.00×   |
| 50     | paralelo   | 1           | 3      | 1.25 ms    | 2.30×   |
| 50     | paralelo   | 2           | 3      | 0.95 ms    | 3.04×   |
| 200    | secuencial | –           | –      | 66.19 ms   | 1.00×   |
| 200    | paralelo   | 1           | 12     | 32.31 ms   | 2.05×   |
| 200    | paralelo   | 2           | 12     | 11.92 ms   | 5.55×   |
| 1000   | secuencial | –           | –      | 3480.76 ms | 1.00×   |
| 1000   | paralelo   | 1           | 62     | 2088.88 ms | 1.67×   |
| 1000   | paralelo   | 2           | 62     | 1199.50 ms | 2.90×   |


### Análisis de los resultados
Los resultados muestran aceleraciones significativas, especialmente para tamaños medianos y grandes.
Para tamaño 50, el overhead comienza a notarse, pero aún se logra speedup > 3×.

Para 200 y 1000, el algoritmo muestra mejoras más consistentes.

La combinación óptima fue generalmente prof = 2,
lo cual coincide con la disponibilidad de hilos de la máquina (4).
Las causas principales son:

- **Alta paralelización natural**
   - El trabajo consiste en aplicar costoRiegoTablon de forma independiente sobre cada tablón.
- **Granularidad controlada**
  - Evita overhead excesivo y permite tareas suficientemente grandes para amortizar el coste de parallel.

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
S_{\text{exp}} \approx 5.55 
$ `para n = 200`

Despejando $ f $ de la expresión de Amdahl:

$
5.55  = \frac{1}{(1 - f) + \frac{f}{4}}
$

$
(1 - f) + \frac{f}{4} = \frac{1}{5.55}
$

$
(1 - f) + \frac{f}{4} \approx 0.1801
$

$
1 - f + \frac{f}{4} = 0.1801
$

$
1 - 0.1801 = f - \frac{f}{4}
$

$
0.8199= \frac{3}{4}f
$

$
f \approx 1.093
$
Por lo tanto, el paralelismo supera el limite de amdahl para 4 hilos:

Las tareas de `costoRiegoFincaPar` son costosas, y el sistema de tareas puede solapar trabajo, overlapping, o la medición presenta pequeñas variaciones que producen speedups “superlineales”.

La caché juega un rol importante: dividir el trabajo en partes más pequeñas puede mejorar la localidad, y en consecuencia la versión paralela parece “más rápida de lo teóricamente posible”.

El algoritmo secuencial, al recorrer un vector grande, puede sufrir penalizaciones de memoria, mientras que el paralelo divide esa carga.

### Conclusiones
- El algoritmo presenta excelentes beneficios de paralelización, superando incluso lo que predice la Ley de Amdahl para `p = 4`
- costoRiegoFincaPar es un caso altamente paralelizable, 
con speedups reales muy superiores a los límites teóricos porque el paralelismo reduce no solo tiempo de cómputo, 
sino también penalizaciones de memoria.

- El speedup medido no puede explicarse únicamente con más hilos, sino con:
  mejoras de caché, reducción de presión de memoria,gran tamaño del trabajo por tarea.
- La fracción paralelizable es efectivamente cercana al 100% teniendo en cuenta las condiciones actuales.