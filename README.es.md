<p align="center">
  <img src="docs/assets/logo.png" alt="XGDTool Android" width="220">
</p>

<p align="center">
  <a href="README.md">🇬🇧 English</a> ·
  <a href="README.it.md">🇮🇹 Italiano</a> ·
  <a href="README.fr.md">🇫🇷 Français</a> ·
  <a href="README.de.md">🇩🇪 Deutsch</a> ·
  <a href="README.es.md">🇪🇸 Español</a> ·
  <a href="README.pt.md">🇵🇹 Português</a> ·
  <a href="README.zh-CN.md">🇨🇳 简体中文</a>
</p>

# XGDTool para Android

Port no oficial de [XGDTool](https://github.com/wiredopposite/XGDTool)
(GPL-3.0) a Android: convierte imágenes de disco Xbox / Xbox 360 (ISO,
XISO recortada) a los formatos **ZAR**, **GOD**, **CCI** y **CSO**
directamente desde el teléfono, para hacer copia de seguridad de tu
colección física sin necesidad de un PC.

<p align="center">
  <img src="docs/assets/screenshot_it.png" alt="Pantalla principal de la app" width="280">
</p>

## Características

- Conversión entre XISO, ZAR, GOD, CCI, CSO — el mismo motor en C++ que
  usa la versión de escritorio de XGDTool.
- Conversión **por lotes**: selecciona varios archivos a la vez, se
  procesan automáticamente en cola.
- Búsqueda automática (opcional) del título del juego en línea, para
  renombrar la salida de forma legible.
- Sin permisos de almacenamiento invasivos: todo pasa por el Storage
  Access Framework de Android — tú eliges qué carpetas puede ver la app.
- Interfaz en 7 idiomas (detectados automáticamente según el idioma del
  teléfono): italiano, inglés, francés, alemán, español, portugués,
  chino simplificado.
- Se ejecuta como servicio en primer plano: puedes salir de la app
  durante una conversión larga sin interrumpirla.

## Requisitos

- Android 8.0 (API 26) o superior, arquitectura **arm64-v8a**.
- Espacio libre de al menos 2× el tamaño del juego más grande de tu
  colección.

## Instalación

Ve a la página de [Releases](../../releases) de este repositorio,
descarga el último `XGDTool-android-debug.apk` e instálalo en tu
teléfono (deberás habilitar "Instalar apps desconocidas"). Para la guía
de uso completa y la resolución de problemas, consulta
[docs/MANUAL.es.md](docs/MANUAL.es.md).

## Compilar desde el código fuente

Requiere Android NDK r27, Android SDK (platform 34), Gradle 8.7+, JDK
17+. Instrucciones completas en
[docs/MANUAL.es.md](docs/MANUAL.es.md#compilar-desde-el-código-fuente).

## Aviso

Proyecto de aficionado, no afiliado ni patrocinado por Microsoft. "Xbox"
es una marca registrada de su respectivo propietario, usada aquí solo
con fines descriptivos. Pensado para copias de seguridad personales de
discos poseídos legalmente.

## Licencia

El núcleo de XGDTool está bajo GPL-3.0 — ver [LICENSE](LICENSE). Los
componentes de terceros integrados en el núcleo están listados en
[XGDTool/ATTRIBUTION.md](XGDTool/ATTRIBUTION.md). Si compartes
públicamente una versión modificada, la GPL-3.0 exige poner también a
disposición el código fuente modificado.
