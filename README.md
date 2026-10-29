# mk96-app-pc-clicker

**MK96 PC Clicker**: autoclicker de escritorio hecho en Java con Swing. Hace clic de forma automática y puede mover el ratón siguiendo un patrón, todo configurado desde una ventana pequeña y controlado con atajos de teclado que funcionan aunque la ventana no tenga el foco.

## Qué hace

La ventana tiene tres bloques de opciones y tres botones.

| Bloque | Opción | Valores |
|---|---|---|
| **Mouse click** | Activar o desactivar los clics (`on/off`) | |
| | `Click/sec`: clics por segundo | De 1 a 1000 |
| **Move mouse** | Activar o desactivar el movimiento (`on/off`) | |
| | `Move Type`: tipo de movimiento | `RANDOM` (aleatorio), `CIRCLE` (círculo), `POLYGON` (polígono) o `REBOUND` (rebote) |
| | `Vertex`: número de vértices | De 2 a 300 |
| | `Radius`: radio | De 1 a 100 |
| **Time** | `Time (min)`: duración en minutos | Vacío significa infinito |

| Botón | Qué hace |
|---|---|
| `Start` | Inicia el autoclicker. Antes aparece una ventana que te pide colocar el ratón (`Place the mouse`) con una cuenta atrás (`Get ready`) hasta el `GO` |
| `Info` | Muestra la ayuda de la aplicación en un cuadro de diálogo |
| `Close` | Cierra la aplicación |

### Atajos de teclado mientras funciona

Los captura un gancho de teclado global, así que valen aunque estés en otra ventana.

| Tecla | Acción |
|---|---|
| `ESC` | Cierra el autoclicker |
| `X` | Lo detiene |
| `P` | Lo pausa |

## Estado actual: todavía no compila

El proyecto **no compila tal cual**, porque depende de `com.markes96:common-code:1.0.0`. Esa es una versión local antigua de la librería compartida que nunca se publicó, y su sucesora está en [`mk96-lib-java-common-code`](https://github.com/mk96-lib/mk96-lib-java-common-code) con otras coordenadas y otras clases.

Es poco lo que usa de ella:

- `FieldUtils.loadFileFromRelativePath`, una sola llamada en `ButtonPanel`, para leer el texto de ayuda `files/ProgramInfo.txt`.
- `commons-lang3` (`StringUtils` en `FrameUtils`), que le llegaba de forma transitiva a través de esa librería.

Comprobado: con esas dos piezas resueltas, el proyecto compila y genera el `.jar`. La solución prevista es leer el texto de ayuda con Java estándar y declarar `commons-lang3` como dependencia propia.

## Requisitos

- JDK 17 o superior
- Maven 3.9 o superior
- Librería [`system-hook`](https://github.com/kristian/system-hook) 3.8 (se descarga de Maven Central), que da el gancho de teclado global
- No necesita claves ni cuentas

## Compilar y ejecutar

Cuando se resuelva la dependencia pendiente:

```bash
mvn clean package
java -jar target/mk96-app-pc-clicker-1.0.0.jar
```

El `.jar` se genera con el plugin de Spring Boot para Maven, así que es ejecutable por sí solo.

## Estructura del repositorio

```
.
├── pom.xml
├── .gitignore
└── src/
    └── main/
        ├── java/com/markes96/
        │   ├── App.java                    Arranque y registro del gancho de teclado
        │   ├── constant/
        │   │   ├── FrameConstant.java
        │   │   └── MoveMouseType.java      Tipos de movimiento: RANDOM, CIRCLE, POLYGON, REBOUND
        │   ├── flag/
        │   │   └── Flags.java              Estado: en marcha y en pausa
        │   ├── frame/
        │   │   ├── display/
        │   │   │   └── DisplayFrame.java
        │   │   ├── main/
        │   │   │   ├── MainFrame.java      Ventana principal
        │   │   │   ├── constrain/
        │   │   │   │   ├── BottomConstraint.java
        │   │   │   │   └── GridConstraints.java
        │   │   │   └── panel/
        │   │   │       ├── ButtonPanel.java
        │   │   │       ├── MouseClickPanel.java
        │   │   │       ├── MoveMousePanel.java
        │   │   │       └── TimePanel.java
        │   │   └── wait/
        │   │       └── PlaceMouseFrame.java    Cuenta atrás para colocar el ratón
        │   ├── keylistener/
        │   │   └── ClikerKeyListener.java     Atajos globales: ESC, X y P
        │   ├── mesh/
        │   │   ├── Mesh.java                   Base de los patrones de movimiento
        │   │   └── type/
        │   │       ├── CircleMesh.java
        │   │       ├── PolygonMesh.java
        │   │       ├── RandomMesh.java
        │   │       └── ReboundMesh.java
        │   ├── thread/
        │   │   ├── MouseClickThread.java       Hilo que hace los clics
        │   │   └── MouseMoveThread.java        Hilo que mueve el ratón
        │   └── utils/
        │       └── FrameUtils.java
        └── resources/files/
            └── ProgramInfo.txt                 Texto de la ayuda (botón Info)
```
