# Informe de Corrección




## Argumentación sobre la corrección

En esta sección se argumenta la corrección de las funciones implementadas en el objeto `SolucionFuncional`. Para cada función clave, se demuestra formalmente que la implementación computa el valor especificado en el enunciado del problema, utilizando inducción estructural cuando la función es recursiva, o propiedades de las funciones de alto orden cuando es iterativa o basada en colecciones. Se considera que una función es correcta si para toda entrada válida, el resultado coincide con la definición matemática del problema.

Se cubren las funciones secuenciales principales: `tIR`, `costoRiegoTablon`, `costoRiegoFinca`, `costoMovilidad`, `generarProgramacionesRiego` (y su auxiliar recursiva `perms`) y `ProgramacionRiegoOptimo`. Las versiones paralelas (`Par`) se argumentan correctas si las secuenciales lo son, dado que usan operaciones asociativas y conmutativas (e.g., `sum`, `minBy`) sobre colecciones paralelas, preservando el resultado por las propiedades de paralelismo en datos de Scala.

### Argumentación para `tIR`

Sea $f: \text{Finca}$ con $|f| = n$, y $\pi: \text{ProgRiego}$ una permutación de $\{0, \dots, n-1\}$, donde $\pi(i)$ es el turno del tablón $i$. La función `tIR` computa el vector $t^\pi$ tal que $t^\pi_i = \sum_{k=0}^{\pi(i)-1} tr^F_{\text{order}(k)}$, donde $\text{order}(j) = \pi^{-1}(j)$ es el tablón regado en el turno $j$.

La implementación usa `scanLeft` para acumular los tiempos de riego en el orden de $\pi^{-1}$, y luego mapea cada $i$ a su acumulado correspondiente.

**Teorema:** $\forall f, \pi: tIR(f, \pi) = t^\pi$, donde $t^\pi$ se define como en la sección 1.2.1 del enunciado.

- **Prueba:** `scanLeft` computa correctamente los prefijos acumulativos por definición (propiedad de las funciones de alto orden en listas). El mapeo via `tabulate` asigna cada $t_i$ basado en $\pi(i)$, coincidiendo con la suma recursiva del enunciado. Como no hay recursión explícita, la corrección sigue de las propiedades inmutables de `Vector` y `scanLeft`.

La implementación es correcta.

### Argumentación para `costoRiegoTablon`

Sea $CR^\pi_F[i]$ definido como en la sección 1.2.2.

La función es un `if` directo que implementa la fórmula caso por caso, usando `tIR` para $t^\pi_i$.

**Teorema:** $\forall i, f, \pi: \text{costoRiegoTablon}(i, f, \pi) = CR^\pi_F[i]$.

- **Prueba:** Dado que `tIR` es correcto (por arriba), el `if` coincide exactamente con la definición condicional. No hay iteración ni recursión, por lo que es correcta por inspección.

La implementación es correcta.

### Argumentación para `costoRiegoFinca`

Sea $CR^\pi_F = \sum_{i=0}^{n-1} CR^\pi_F[i]$.

La implementación usa `tabulate` para generar los costos por tablón y `sum` para acumular.

**Teorema:** $\forall f, \pi: \text{costoRiegoFinca}(f, \pi) = CR^\pi_F$.

- **Prueba:** Por corrección de `costoRiegoTablon`, el vector de costos es correcto. `sum` computa la suma exacta por propiedad asociativa de la adición. La versión paralela `costoRiegoFincaPar` es correcta porque `par.sum` preserva el resultado para operaciones asociativas.

La implementación es correcta.

### Argumentación para `costoMovilidad`

Sea $CM^\pi_F = \sum_{j=0}^{n-2} D_F[\pi^{-1}(j), \pi^{-1}(j+1)]$.

La implementación computa el orden $\pi^{-1}$, usa `sliding(2)` para pares consecutivos y suma las distancias.

**Teorema:** $\forall f, \pi, d: \text{costoMovilidad}(f, \pi, d) = CM^\pi_F$.

- **Prueba:** `sliding` genera los pares correctos del orden, y `sum` acumula las distancias. Correcta por propiedades de iteradores. La versión paralela `costoMovilidadPar` es correcta por asociatividad de `sum`.

La implementación es correcta.

### Argumentación para `generarProgramacionesRiego` (y auxiliar `perms`)

Sea $\text{perms}(xs)$ la función recursiva que genera todas las permutaciones de $xs = \{0, \dots, n-1\}$.

Programa en Scala:

```scala
def perms(xs: Vector[Int]): Vector[Vector[Int]] =
  if (xs.isEmpty) Vector(Vector())
  else
    xs.flatMap(x =>
      perms(xs.filterNot(_ == x)).map(rest => x +: rest)
    )
```

`generarProgramacionesRiego` aplica `perms` y convierte cada orden a $\pi$ via `orderToProg`, donde $\pi(i) = \text{posición de } i$ en el orden.

**Teorema:** $\forall xs: |\text{perms}(xs)| = |xs|! \land$ todas son permutaciones únicas de $xs$.

- **Caso base:** $xs = \emptyset$, $\text{perms}(\emptyset) = \{\emptyset\}$, y $0! = 1$. Correcto.

- **Caso inductivo:** Asuma para $|xs| = k$, $\text{perms}$ genera $k!$ permutaciones únicas. Para $|xs| = k+1$, se elige cada $x \in xs$ como cabeza, y recursivamente se permuta el resto ($k$ elementos), agregando $x$ al frente. Por hipótesis inductiva (HI), cada subllamada genera $k!$ permutaciones únicas del resto. Como hay $k+1$ elecciones de $x$, total $(k+1) \cdot k! = (k+1)!$. Unicidad: cada permutación comienza con un $x$ único por elección, y el resto es único por HI.

**Conclusión:** $\forall n: \text{generarProgramacionesRiego}(f)$ genera todas las $n!$ programaciones $\pi$ únicas, donde $|f| = n$.

La versión paralela `generarProgramacionesRiegoPar` es correcta porque `par.map` preserva el conjunto de resultados (operación sin efectos laterales).

La implementación es correcta.

### Argumentación para `ProgramacionRiegoOptimo`

La función genera todas las $\pi$, computa $CR^\pi_F + CM^\pi_F$ para cada una, y selecciona la de mínimo costo via `minBy`.

**Teorema:** $\forall f, d: \text{ProgramacionRiegoOptimo}(f, d) = (\pi^*, c^*)$, donde $c^* = \min_\pi (CR^\pi_F + CM^\pi_F)$.

- **Prueba:** Por corrección de `generarProgramacionesRiego` (todas las $\pi$), y de los costos (por arriba), el mapeo genera pares correctos. `minBy` selecciona el mínimo por definición. La versión paralela es correcta por propiedades de `par.minBy` en operaciones independientes.

La implementación es correcta.



## Casos de prueba

Para cada función se incluyen al menos 5 casos de prueba representativos, implementados como pruebas de software en ScalaTest (paquete `taller`). Estos casos verifican que el valor computado coincide con el esperado, calculado manualmente según las definiciones matemáticas. Las pruebas se ejecutan automáticamente al construir el proyecto con Gradle.

Los casos cubren escenarios básicos, edge cases (e.g., $n=2$, órdenes invertidos), y casos con múltiples tablones para validar la generalidad. A continuación se resumen; el código completo está en los archivos de test proporcionados.

### Casos para `tIR` (en `TiempoInicioRiegoTest.scala`)

1. Finca de 2 tablones, $\pi$ identidad: Esperado $[0, 3]$, computado coincide.
2. $\pi$ invertida: Esperado $[2, 0]$, coincide.
3. 3 tablones, $\pi = [0,2,1]$: Esperado $[0,5,2]$, coincide.
4. Treg iguales: Esperado $[10,0,5]$, coincide.
5. 4 tablones, $\pi$ arbitraria: Esperado $[9,0,7,3]$, coincide.

Todas pasan, validando la corrección.

### Casos para `costoRiegoTablon`, `costoRiegoFinca`, `costoMovilidad` (en `CostosTest.scala`)

1. `costoRiegoTablon` a tiempo: Esperado $7$, coincide.
2. `costoRiegoTablon` tardío: Esperado $4$, coincide.
3. `costoRiegoFinca` con 3 tablones: Esperado suma manual $14$, coincide.
4. `costoMovilidad` con 3 tablones: Esperado $13$, coincide.
5. Costo total (riego + movilidad) en 4 tablones: Computado coincide con suma independiente.

Todas pasan.

### Casos para `generarProgramacionesRiego` y auxiliares (en `GenerarProgramacionesTest.scala`)

1. `perms` para $[0,1,2]$: Genera 6 únicas, coincide con set esperado.
2. `orderToProg`: Convierte orden a $\pi$, esperado $[1,2,0]$, coincide.
3. Generar para $n=2$: 2 programaciones, coincide.
4. Secuencial vs paralela para $n=3$: Sets iguales.
5. Número para $n=3$: Exactamente 6, coincide.

Todas pasan.

### Casos para `ProgramacionRiegoOptimo` (en `ProgramacionOptimaTest.scala`)

1. Óptimo en $n=2$ (riego prioritario): $\pi = [1,0]$, coincide.
2. Secuencial vs paralela en $n=3$: Iguales.
3. Tiempos iguales (simétrico): Costo consistente con definición.
4. Distancias asimétricas: Costo coincide con cálculo.
5. Consistencia costo: Computado = suma manual, coincide.

Todas pasan, confirmando corrección.