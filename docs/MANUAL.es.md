<p align="center">
  <a href="MANUAL.md">🇬🇧 English</a> ·
  <a href="MANUAL.it.md">🇮🇹 Italiano</a> ·
  <a href="MANUAL.fr.md">🇫🇷 Français</a> ·
  <a href="MANUAL.de.md">🇩🇪 Deutsch</a> ·
  <a href="MANUAL.es.md">🇪🇸 Español</a> ·
  <a href="MANUAL.pt.md">🇵🇹 Português</a> ·
  <a href="MANUAL.zh-CN.md">🇨🇳 简体中文</a>
</p>

# Manual de uso y depuración — XGDTool para Android

## Índice

- [Qué es](#qué-es)
- [Requisitos](#requisitos)
- [Instalación](#instalación)
- [Guía rápida](#guía-rápida)
- [Guía completa de la interfaz](#guía-completa-de-la-interfaz)
- [Formatos de salida](#formatos-de-salida)
- [Cómo funciona internamente](#cómo-funciona-internamente)
- [Depuración / diagnóstico de problemas](#depuración--diagnóstico-de-problemas)
- [Preguntas frecuentes](#preguntas-frecuentes)
- [Limitaciones conocidas](#limitaciones-conocidas)
- [Compilar desde el código fuente](#compilar-desde-el-código-fuente)
- [Licencia y créditos](#licencia-y-créditos)

## Qué es

XGDTool para Android lleva el núcleo en C++ de
[XGDTool](https://github.com/wiredopposite/XGDTool) (GPL-3.0) — el mismo
motor de conversión usado en PC — directamente al teléfono, para
convertir imágenes de disco Xbox y Xbox 360 (ISO, XISO recortada) a
formatos más compactos o compatibles con emuladores (**ZAR**, **GOD**,
**CCI**, **CSO**), sin necesidad de un ordenador.

La GUI original (wxWidgets) no es portable a Android, así que se
sustituyó por una app en Kotlin que se comunica con el mismo núcleo C++
mediante JNI.

## Requisitos

- Android 8.0 (API 26) o superior.
- Arquitectura **arm64-v8a** (la gran mayoría de teléfonos Android
  recientes). Los dispositivos solo de 32 bits no están soportados por
  esta compilación — ver
  [Compilar desde el código fuente](#compilar-desde-el-código-fuente).
- Espacio libre de al menos **2× el tamaño del juego más grande** de tu
  colección (el archivo de origen copiado y la salida producida
  coexisten temporalmente durante la conversión).
- La conexión a internet es **opcional**: solo se necesita para la
  búsqueda automática del título en línea (para nombrar mejor los
  archivos convertidos); la app funciona perfectamente sin conexión.

## Instalación

1. Descarga el último `XGDTool-android-debug.apk` desde la sección
   [Releases](../../releases) de este repositorio.
2. En el teléfono, activa "Instalar apps desconocidas" para la app que
   uses para abrir el archivo (gestor de archivos, navegador, etc.).
3. Instala el APK. Está firmado con la clave de depuración estándar de
   Android — perfecto para uso personal, no pensado para la Play Store.
4. En el primer inicio, la app pide el permiso de notificaciones
   (necesario para la notificación de progreso del servicio en segundo
   plano).

La app detecta automáticamente el idioma del sistema entre los 7
soportados (italiano, inglés, francés, alemán, español, portugués,
chino simplificado); si el idioma del teléfono no está entre estos, usa
el inglés como alternativa.

## Guía rápida

1. Toca **Selecciona ISO/XISO a convertir** y elige uno o más archivos
   (selección múltiple soportada: mantén pulsado un archivo en el
   selector del sistema para activarla).
2. Toca **Selecciona carpeta de destino** y elige dónde se guardarán los
   archivos convertidos — cualquier carpeta accesible desde el teléfono,
   incluida una tarjeta SD externa.
3. Elige el **formato de salida** entre los chips disponibles (ZAR está
   preseleccionado).
4. Toca **Convertir**.
5. Sigue el progreso en la tarjeta "Progreso" y en el registro al final
   de la pantalla. Puedes salir de la app durante la conversión: se
   ejecuta como un servicio en primer plano con su propia notificación, y
   sobrevive al pasar a segundo plano.

## Guía completa de la interfaz

**Origen** — el archivo (o archivos) ISO/XISO a convertir. El selector
usa el Storage Access Framework de Android: puedes elegir cualquier
proveedor de almacenamiento visible para tu teléfono (almacenamiento
interno, tarjeta SD, carpetas de sincronización en la nube local, etc.).

**Destino** — la carpeta donde se escribirán los archivos convertidos.
También aquí, cualquier carpeta accesible vía SAF.

**Opciones de conversión**
- *Formato*: ver [Formatos de salida](#formatos-de-salida) más abajo.
- *Modo sin conexión*: si está desactivado, la app intenta buscar en
  línea el título correcto del juego para nombrar mejor la salida
  (requiere red, con un tiempo de espera máximo de unos segundos — si la
  red es lenta o está ausente, la app espera ese tiempo máximo y luego
  continúa sin conexión de todos modos). Si está activado, este paso se
  omite por completo.

**Progreso** — cada archivo pasa por 3 fases visibles, cada una con su
propia etiqueta y barra de progreso:

1. **Copia local** — el archivo elegido se copia desde su ubicación SAF
   a la caché privada de la app (necesario porque el motor nativo
   trabaja con rutas reales, no con Uris content://).
2. **Conversión** — comprobación de conexión, búsqueda de título en
   línea (si no está sin conexión), y luego la escritura propiamente
   dicha. Empieza con una barra "indeterminada" (deslizante) porque la
   duración de la fase de red no es predecible, y luego pasa a un
   porcentaje real en cuanto empieza a escribir datos.
3. **Escritura de salida** — copia del resultado de la caché a la
   carpeta de destino elegida.

Con varios archivos seleccionados, estas 3 fases se repiten en secuencia
para cada uno (sin conversión en paralelo — ver
[Limitaciones conocidas](#limitaciones-conocidas)); el encabezado
muestra "Archivo X de Y" con el nombre del archivo actual.

**Cancelar** detiene la conversión en curso de forma limpia, en el
siguiente punto de control útil — no trunca una escritura a medias.

**Registro** — muestra cada línea producida por el motor nativo, útil
para ver exactamente qué está haciendo o por qué falló un archivo.

## Formatos de salida

| Formato | Uso típico |
|---|---|
| **ZAR** | Archivo comprimido universal, el formato más eficiente para usar con **Xenia Canary** (emulador de Xbox 360). Preseleccionado. |
| **GOD** | *Games on Demand* — formato nativo usado por Xbox 360 y varios front-ends/configuraciones RGH. |
| **CCI** | Formato comprimido pensado para emuladores de Xbox original. |
| **CSO** | Formato comprimido, alternativa a CCI soportada por varios emuladores. |

## Cómo funciona internamente

```
Uri SAF (entrada)
   │  copia byte a byte con progreso
   ▼
caché privada de la app (ruta real)
   │  XgdNative.convert() — JNI, síncrono, en un hilo dedicado
   ▼
libxgdtool.so (núcleo C++ de XGDTool)
   │  escribe el formato elegido en una carpeta de caché
   ▼
caché privada de la app (salida)
   │  copia byte a byte con progreso hacia el destino SAF
   ▼
Carpeta de destino elegida por el usuario
```

Un solo archivo a la vez, en cola secuencial. Cada fase informa a la
interfaz de los bytes procesados/totales mediante callbacks JNI
(`XgdCallback.onLog` / `onProgress`), gestionados en `ConvertService`
(un servicio en primer plano de Android).

## Depuración / diagnóstico de problemas

Si un archivo falla, el registro de la app muestra el motivo — busca una
línea del tipo:

```
<Tipo de error> en <archivo:línea>: <detalle>
```

Si necesitas más contexto del que se muestra en la app (por ejemplo,
para entender un fallo nativo, no solo una excepción gestionada),
conecta el teléfono a un PC con `adb` y ejecuta, durante una conversión:

```bash
adb logcat -s XgdJNI:* XgdCore:*
```

- `XgdCore` imprime cada línea de registro y cada actualización de
  progreso emitida por el código C++ del convertidor (mismo contenido
  que el registro en la app, pero no se pierde nada si la app pasa a
  segundo plano o el proceso muere).
- `XgdJNI` imprime el diagnóstico del puente JNI (resolución de métodos
  de callback, excepciones pendientes).

Si la app se comporta de forma extraña sin registros útiles ni en
logcat, es probable un fallo nativo (SIGSEGV) — en ese caso necesitarás
un `adb logcat` completo (sin filtrar) tomado justo después del fallo, o
un tombstone (`adb bugreport` / `/data/tombstones`).

Problemas comunes:

- **El selector de archivos del sistema muestra "Ningún elemento"**
  incluso navegando por el almacenamiento principal del teléfono — es un
  problema de un componente del sistema Android
  (DocumentsUI/ExternalStorageProvider), no de la app. Prueba a reiniciar
  el teléfono, o a borrar la caché de la app del sistema
  "Archivos"/"Gestor de archivos" desde Ajustes.
- **Copia bloqueada o archivo truncado**: comprueba el espacio libre
  (ver [Requisitos](#requisitos)); la app detecta y notifica una copia
  incompleta en lugar de continuar con datos parciales.
- **La conversión tarda en arrancar**: si el modo sin conexión está
  desactivado y la red está ausente o es muy lenta, la app espera el
  tiempo máximo de la búsqueda de título en línea antes de continuar —
  activar el modo sin conexión evita esta espera.

## Preguntas frecuentes

**¿La app modifica o elimina los archivos originales?**
No. Los archivos de origen solo se leen (se copian a una caché temporal
para la conversión, y luego la copia en caché se elimina al terminar).
La salida es siempre un archivo nuevo en la carpeta de destino elegida.

**¿Necesito conexión a internet?**
No, es opcional. Solo se usa para la búsqueda automática del título en
línea (mejor nombrado de la salida). Con el modo sin conexión activado,
o sin red disponible, la app convierte igualmente, usando el nombre del
juego tal como aparece en el disco.

**¿Puedo convertir varios archivos a la vez?**
Sí, selecciona varios archivos en el paso 1 — se procesarán en cola uno
a uno, con un resumen final de cuántos tuvieron éxito o fallaron.

**¿Es una app oficial de Microsoft/Xbox?**
No. Es un proyecto de aficionado, no afiliado ni respaldado por
Microsoft. "Xbox" es una marca registrada de sus respectivos
propietarios, usada aquí solo con fines descriptivos/de compatibilidad.

## Limitaciones conocidas

- Un solo archivo a la vez, sin conversión en paralelo.
- La copia SAF → caché requiere espacio libre de al menos 2× el tamaño
  del juego más grande de tu colección (el archivo de origen copiado y
  la salida producida coexisten temporalmente).
- Solo **arm64-v8a** (la gran mayoría de teléfonos recientes; si el tuyo
  es un dispositivo solo de 32 bits, hay que recompilar también para
  `armeabi-v7a`, no incluido en esta compilación).
- APK firmado con la clave de depuración: reinstalarlo sobre una versión
  anterior siempre funciona (misma firma), pero no es distribuible a
  través de tiendas de apps.
- Sin suite de pruebas automatizada: cada cambio se verifica con una
  compilación limpia y pruebas manuales en un dispositivo real.

## Compilar desde el código fuente

Requiere Android NDK r27, Android SDK (platform 34, build-tools 34.0.0),
Gradle 8.7+, JDK 17+.

```bash
# 1. Compila de forma cruzada zstd, lz4, OpenSSL, curl para arm64-v8a con el NDK.
#    XGDTool/android/CMakeLists.txt espera todo bajo
#    ~/android/install-arm64 por defecto (sobrescribible con -DXGD_DEPS_PREFIX).

# 2. Compilación de la biblioteca nativa
cd XGDTool/android && mkdir build && cd build
cmake -G Ninja \
  -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK_HOME/build/cmake/android.toolchain.cmake \
  -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-24 \
  -DCMAKE_PREFIX_PATH=$HOME/android/install-arm64 \
  -DCMAKE_FIND_ROOT_PATH=$HOME/android/install-arm64 \
  -DCMAKE_FIND_ROOT_PATH_MODE_INCLUDE=BOTH \
  -DCMAKE_FIND_ROOT_PATH_MODE_LIBRARY=BOTH \
  -DCMAKE_FIND_ROOT_PATH_MODE_PACKAGE=BOTH ..
ninja
$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/<host>/bin/llvm-strip \
  --strip-unneeded libxgdtool.so -o ../../../XGDToolAndroid/app/src/main/jniLibs/arm64-v8a/libxgdtool.so
# <host> = linux-x86_64, windows-x86_64 o darwin-x86_64 según tu sistema

# 3. Compilación del APK
cd ../../../XGDToolAndroid
gradle clean assembleDebug
# APK en app/build/outputs/apk/debug/app-debug.apk
```

Nota: `XGDTool/cmake/embed_resources.cmake` genera
`XGDTool/src/Executable/AttachXbe.h` a partir de un binario incluido en
el submódulo `external/Repackinator` la primera vez que se configura
CMake — no lo modifiques a mano, se regenera solo.

## Licencia y créditos

El núcleo de XGDTool y este port se distribuyen bajo **GPL-3.0** — ver
[LICENSE](../LICENSE) en la raíz del repositorio. El núcleo en C++
integra a su vez componentes de terceros listados en
[ATTRIBUTION.md](../XGDTool/ATTRIBUTION.md).

Proyecto original: [wiredopposite/XGDTool](https://github.com/wiredopposite/XGDTool).
