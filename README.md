# Banco de Preguntas Saber Pro

Taller 4 - Laboratorio de Ingenieria del Software II (Periodo 2-2026)
Patron Capas + micro patron MVC + patron Observer, en Java SE Desktop con Swing.

## Resumen del proyecto

El programa de Ingenieria de Sistemas requiere una plataforma para construir y
administrar un banco de preguntas que apoye la preparacion de los estudiantes
para las pruebas Saber Pro. Esta entrega implementa la gestion del estado de las
preguntas: se selecciona una pregunta del banco en un comboBox, se cargan sus
datos en un formulario (Id, Nombre, Pregunta, Opciones, Respuesta correcta,
Estado) y se cambia su estado entre *Borrador*, *Pendiente de revision* y
*Eliminada*. Cada cambio de estado notifica automaticamente a dos vistas: una
con el conteo de preguntas por estado y otra con una grafica en pastel de los
porcentajes.

## Arquitectura en capas

```
co.edu.unicauca.bancopreguntas
├── Main.java                        punto de ensamblaje (composition root)
├── presentation                     capa de presentacion
│   ├── GUIQuestions.java            vista principal (comboBox + formulario)
│   ├── GUIObserver1.java            vista observadora: estadisticas por estado
│   ├── GUIObserver2.java            vista observadora: grafica en pastel
│   └── QuestionController.java      controlador del MVC
├── domain                           capa de dominio
│   ├── Question.java                entidad y sus reglas de negocio
│   ├── QuestionDistractors.java     opciones de respuesta y distractores
│   ├── QuestionState.java           estados validos de una pregunta
│   ├── QuestionStatistics.java      conteo y porcentajes por estado
│   ├── QuestionRepository.java      abstraccion de la persistencia
│   ├── IQuestionService.java        abstraccion del servicio de dominio
│   └── QuestionService.java         servicio de dominio y sujeto observable
├── access                           capa de acceso a datos
│   └── QuestionImplRepository.java  implementacion en memoria (LinkedHashMap)
└── infra                            capa transversal
    ├── Observer.java                contrato del observador
    └── Subject.java                 sujeto observable
```

Las dependencias apuntan siempre hacia abajo: la presentacion depende del
dominio, el dominio depende solo de sus propias abstracciones y la capa de
acceso a datos implementa el contrato que define el dominio.

## MVC y Observer

| Rol | Clase |
|-----|-------|
| Modelo (y sujeto observable) | `QuestionService` (extiende `Subject`, implementa `IQuestionService`) |
| Vista | `GUIQuestions`, `GUIObserver1`, `GUIObserver2` |
| Controlador | `QuestionController` |

Cuando el usuario pulsa *Actualizar estado*, `GUIQuestions` llama al
controlador, este invoca `QuestionService.changeState(...)`, el servicio aplica
la regla de negocio, persiste el cambio y ejecuta `notifyAllObservers(...)`
publicando un `QuestionStatistics`. `GUIObserver1` y `GUIObserver2` reciben ese
objeto en su metodo `update(...)` y se repintan solas: la vista principal no
conoce a las otras vistas.

## Principios SOLID aplicados

- **SRP**: cada clase tiene una sola razon para cambiar. `Question` guarda las
  reglas de la entidad, `QuestionService` la logica de negocio,
  `QuestionImplRepository` la persistencia y las clases `GUI*` solo la interfaz.
- **OCP**: agregar una tercera vista observadora no obliga a modificar
  `Subject`, `QuestionService` ni las vistas existentes; basta implementar
  `Observer` y registrarla en `Main`.
- **LSP**: cualquier implementacion de `QuestionRepository` es intercambiable.
  Las pruebas usan `FakeQuestionRepository` en lugar de la implementacion real
  sin cambiar una linea del servicio.
- **ISP**: `Observer` expone un unico metodo, `QuestionRepository` solo las
  cuatro operaciones de persistencia e `IQuestionService` solo las operaciones
  de negocio que la presentacion necesita (los metodos de suscripcion quedan en
  `Subject`, fuera del contrato del servicio).
- **DIP**: las dos fronteras del sistema dependen de abstracciones.
  `QuestionController` depende de `IQuestionService` y `QuestionService` depende
  de `QuestionRepository`; ambas se reciben por constructor. Las
  implementaciones concretas se conocen unicamente en `Main`.

### Interfaces del diseno

| Interfaz | Implementacion | Frontera que desacopla |
|----------|----------------|------------------------|
| `Observer` | `GUIObserver1`, `GUIObserver2` | sujeto -> vistas |
| `IQuestionService` | `QuestionService` | presentacion -> dominio |
| `QuestionRepository` | `QuestionImplRepository` | dominio -> acceso a datos |

`Subject` no es una interfaz sino una clase concreta: aporta estado (la lista de
observadores) y el comportamiento comun de registro y notificacion, que de otro
modo habria que repetir en cada sujeto.

## Pruebas unitarias automatizadas

48 pruebas con JUnit 5 sobre las clases del dominio, el sujeto observable, el
repositorio y el controlador: reglas de la entidad, validacion de las opciones
de respuesta, calculo de estadisticas y porcentajes, transiciones de estado
invalidas y notificacion efectiva a todos los observadores. Gracias a las
interfaces, el servicio se prueba con un repositorio falso y el controlador con
un servicio falso, sin levantar la interfaz grafica.

```
src/test/java/co/edu/unicauca/bancopreguntas
├── access/QuestionImplRepositoryTest.java
├── presentation/QuestionControllerTest.java
├── domain/FakeQuestionRepository.java   doble de prueba
├── domain/QuestionTest.java
├── domain/QuestionDistractorsTest.java
├── domain/QuestionServiceTest.java
├── domain/QuestionStatisticsTest.java
└── infra/SubjectTest.java
```

## Requisitos

- JDK 21 **completo** (`openjdk-21-jdk`). Un JRE *headless* compila pero no
  puede abrir las ventanas Swing.
- Maven 3.8 o superior.

## Como ejecutar

```bash
# Ejecutar las pruebas unitarias
mvn test

# Compilar y generar el jar ejecutable
mvn clean package

# Ejecutar la aplicacion
java -jar target/banco-preguntas-1.0.0.jar
```

Al iniciar se abren las tres ventanas: gestion de preguntas, vista de
estadisticas y vista grafica. El repositorio en memoria se precarga con ocho
preguntas de ejemplo.

## Repositorio

<!-- Reemplazar por el enlace del repositorio del grupo de trabajo -->
https://github.com/USUARIO/REPOSITORIO
