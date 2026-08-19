<p align="center">
  <a href="MANUAL.md">🇬🇧 English</a> ·
  <a href="MANUAL.it.md">🇮🇹 Italiano</a> ·
  <a href="MANUAL.fr.md">🇫🇷 Français</a> ·
  <a href="MANUAL.de.md">🇩🇪 Deutsch</a> ·
  <a href="MANUAL.es.md">🇪🇸 Español</a> ·
  <a href="MANUAL.pt.md">🇵🇹 Português</a> ·
  <a href="MANUAL.zh-CN.md">🇨🇳 简体中文</a>
</p>

# Benutzer- und Debug-Handbuch — XGDTool für Android

## Inhaltsverzeichnis

- [Was es ist](#was-es-ist)
- [Voraussetzungen](#voraussetzungen)
- [Installation](#installation)
- [Schnellstart](#schnellstart)
- [Vollständige Oberflächen-Anleitung](#vollständige-oberflächen-anleitung)
- [Ausgabeformate](#ausgabeformate)
- [Funktionsweise im Detail](#funktionsweise-im-detail)
- [Debugging / Fehlerbehebung](#debugging--fehlerbehebung)
- [FAQ](#faq)
- [Bekannte Einschränkungen](#bekannte-einschränkungen)
- [Aus dem Quellcode kompilieren](#aus-dem-quellcode-kompilieren)
- [Lizenz und Danksagungen](#lizenz-und-danksagungen)

## Was es ist

XGDTool für Android bringt den C++-Kern von
[XGDTool](https://github.com/wiredopposite/XGDTool) (GPL-3.0) — dieselbe
Konvertierungs-Engine wie am PC — direkt auf dein Smartphone, um Xbox-
und Xbox-360-Disk-Images (ISO, gestripptes XISO) in kompaktere oder
emulatorfreundliche Formate (**ZAR**, **GOD**, **CCI**, **CSO**) zu
konvertieren, ganz ohne Computer.

Die ursprüngliche GUI (wxWidgets) lässt sich nicht auf Android portieren,
daher wurde sie durch eine Kotlin-App ersetzt, die über JNI mit demselben
C++-Kern kommuniziert.

## Voraussetzungen

- Android 8.0 (API 26) oder höher.
- **arm64-v8a**-Architektur (die überwiegende Mehrheit aktueller
  Android-Smartphones). Reine 32-Bit-Geräte werden von diesem Build
  nicht unterstützt — siehe
  [Aus dem Quellcode kompilieren](#aus-dem-quellcode-kompilieren).
- Freier Speicherplatz von mindestens dem **2-Fachen der Größe des
  größten Spiels** deiner Sammlung (die kopierte Quelldatei und die
  erzeugte Ausgabe bestehen während der Konvertierung vorübergehend
  gleichzeitig).
- Internetverbindung ist **optional**: nur für die automatische
  Online-Titelsuche nötig (um konvertierte Dateien klarer zu benennen);
  die App funktioniert auch offline einwandfrei.

## Installation

1. Lade die neueste `XGDTool-android-debug.apk` aus dem
   [Releases](../../releases)-Bereich dieses Repos herunter.
2. Aktiviere auf dem Smartphone "Unbekannte Apps installieren" für die
   App, mit der du die Datei öffnest (Dateimanager, Browser usw.).
3. Installiere die APK. Sie ist mit dem Standard-Android-Debug-Schlüssel
   signiert — gut für persönliches Sideloading, nicht für den Play Store
   gedacht.
4. Beim ersten Start fragt die App nach der Benachrichtigungsberechtigung
   (nötig für die Fortschrittsbenachrichtigung des Hintergrunddienstes).

Die App erkennt automatisch die Systemsprache unter den 7 unterstützten
(Italienisch, Englisch, Französisch, Deutsch, Spanisch, Portugiesisch,
vereinfachtes Chinesisch); ist die Sprache des Smartphones nicht darunter,
wird auf Englisch zurückgegriffen.

## Schnellstart

1. Tippe auf **ISO/XISO zum Konvertieren auswählen** und wähle eine oder
   mehrere Dateien (Mehrfachauswahl wird unterstützt: langes Drücken auf
   eine Datei im Systemauswahldialog aktiviert sie).
2. Tippe auf **Zielordner auswählen** und lege fest, wo die konvertierten
   Dateien gespeichert werden — jeder vom Smartphone aus zugängliche
   Ordner, auch eine externe SD-Karte.
3. Wähle das **Ausgabeformat** unter den verfügbaren Chips (ZAR ist
   vorausgewählt).
4. Tippe auf **Konvertieren**.
5. Verfolge den Fortschritt in der Karte "Fortschritt" und im Protokoll
   am unteren Bildschirmrand. Du kannst die App während der Konvertierung
   verlassen: Sie läuft als Vordergrunddienst mit eigener Benachrichtigung
   und übersteht das Verschieben in den Hintergrund.

## Vollständige Oberflächen-Anleitung

**Quelle** — die zu konvertierende(n) ISO/XISO-Datei(en). Der
Auswahldialog nutzt Androids Storage Access Framework: Du kannst aus
jedem für dein Smartphone sichtbaren Speicheranbieter wählen (interner
Speicher, SD-Karte, lokale Cloud-Sync-Ordner usw.).

**Ziel** — der Ordner, in den die konvertierten Dateien geschrieben
werden. Auch hier: jeder über SAF zugängliche Ordner.

**Konvertierungsoptionen**
- *Format*: siehe [Ausgabeformate](#ausgabeformate) unten.
- *Offline-Modus*: Ist er deaktiviert, versucht die App, den korrekten
  Spieltitel online zu ermitteln, um die Ausgabe klarer zu benennen
  (erfordert Netzwerk, mit maximal wenigen Sekunden Timeout — ist das
  Netzwerk langsam oder nicht vorhanden, wartet die App bis zu diesem
  Timeout und fährt dann trotzdem offline fort). Ist er aktiviert, wird
  dieser Schritt komplett übersprungen.

**Fortschritt** — jede Datei durchläuft 3 sichtbare Phasen, jede mit
eigener Beschriftung und Fortschrittsbalken:

1. **Lokale Kopie** — die gewählte Datei wird von ihrem SAF-Speicherort
   in den privaten Cache der App kopiert (nötig, weil die native Engine
   mit echten Pfaden arbeitet, nicht mit content://-Uris).
2. **Konvertierung** — Verbindungsprüfung, Online-Titelsuche (falls
   nicht offline), dann der eigentliche Schreibvorgang. Startet mit
   einem "unbestimmten" (durchlaufenden) Balken, da die Dauer der
   Netzwerkphase nicht vorhersehbar ist, und wechselt zu einer echten
   Prozentanzeige, sobald Daten geschrieben werden.
3. **Ausgabe schreiben** — Kopieren des Ergebnisses vom Cache in den
   gewählten Zielordner.

Bei mehreren ausgewählten Dateien wiederholen sich diese 3 Phasen für
jede einzelne nacheinander (keine parallele Konvertierung — siehe
[Bekannte Einschränkungen](#bekannte-einschränkungen)); die Kopfzeile
zeigt "Datei X von Y" mit dem Namen der aktuellen Datei.

**Abbrechen** stoppt die laufende Konvertierung sauber am nächsten
sinnvollen Kontrollpunkt — ein laufender Schreibvorgang wird nicht mitten
drin abgeschnitten.

**Protokoll** — zeigt jede vom nativen Motor erzeugte Zeile, nützlich um
genau zu sehen, was er gerade tut oder warum eine Datei fehlgeschlagen
ist.

## Ausgabeformate

| Format | Typische Verwendung |
|---|---|
| **ZAR** | Universelles komprimiertes Archiv, das effizienteste Format für die Verwendung mit **Xenia Canary** (Xbox-360-Emulator). Vorausgewählt. |
| **GOD** | *Games on Demand* — natives Format, verwendet von Xbox 360 und diversen Front-Ends/RGH-Setups. |
| **CCI** | Komprimiertes Format für Original-Xbox-Emulatoren gedacht. |
| **CSO** | Komprimiertes Format, Alternative zu CCI, von mehreren Emulatoren unterstützt. |

## Funktionsweise im Detail

```
SAF-Uri (Eingabe)
   │  byteweise Kopie mit Fortschrittsanzeige
   ▼
privater App-Cache (echter Pfad)
   │  XgdNative.convert() — JNI, synchron, in einem eigenen Thread
   ▼
libxgdtool.so (C++-Kern von XGDTool)
   │  schreibt das gewählte Format in einen Cache-Ordner
   ▼
privater App-Cache (Ausgabe)
   │  byteweise Kopie mit Fortschrittsanzeige zum SAF-Ziel
   ▼
Vom Nutzer gewählter Zielordner
```

Immer nur eine Datei gleichzeitig, in einer sequenziellen Warteschlange.
Jede Phase meldet verarbeitete/gesamte Bytes über JNI-Callbacks an die
Oberfläche (`XgdCallback.onLog` / `onProgress`), verwaltet im
`ConvertService` (ein Android-Vordergrunddienst).

## Debugging / Fehlerbehebung

Schlägt eine Datei fehl, zeigt das Protokoll der App den Grund — suche
nach einer Zeile wie:

```
<Fehlertyp> in <Datei:Zeile>: <Detail>
```

Brauchst du mehr Kontext als in der App angezeigt (z. B. um einen
nativen Absturz zu verstehen, nicht nur eine behandelte Ausnahme),
verbinde das Smartphone per `adb` mit einem PC und starte während einer
Konvertierung:

```bash
adb logcat -s XgdJNI:* XgdCore:*
```

- `XgdCore` gibt jede Log-Zeile und jedes Fortschritts-Update aus, das
  vom C++-Code des Konverters ausgegeben wird (gleicher Inhalt wie das
  Protokoll in der App, aber nichts geht verloren, wenn die App in den
  Hintergrund verschoben wird oder der Prozess abstürzt).
- `XgdJNI` gibt die Diagnose der JNI-Bridge aus (Auflösung der
  Callback-Methoden, ausstehende Ausnahmen).

Verhält sich die App merkwürdig, ohne dass selbst logcat nützliche
Einträge zeigt, liegt vermutlich ein nativer Absturz (SIGSEGV) vor — in
diesem Fall brauchst du ein vollständiges (ungefiltertes) `adb logcat`
direkt nach dem Absturz oder einen Tombstone (`adb bugreport` /
`/data/tombstones`).

Häufige Probleme:

- **Der System-Dateiauswahldialog zeigt "Keine Elemente"** an, selbst
  beim Durchsuchen des Hauptspeichers des Smartphones — das ist ein
  Problem einer Android-Systemkomponente
  (DocumentsUI/ExternalStorageProvider), nicht der App. Versuche, das
  Smartphone neu zu starten, oder leere in den Einstellungen den Cache
  der System-App "Dateien"/"Dateimanager".
- **Kopiervorgang hängt oder Datei ist abgeschnitten**: prüfe den
  freien Speicherplatz (siehe [Voraussetzungen](#voraussetzungen)); die
  App erkennt und meldet eine unvollständige Kopie, statt mit
  Teildaten fortzufahren.
- **Konvertierung startet langsam**: Ist der Offline-Modus deaktiviert
  und das Netzwerk nicht vorhanden oder sehr langsam, wartet die App das
  maximale Timeout der Online-Titelsuche ab, bevor sie fortfährt — der
  Offline-Modus vermeidet diese Wartezeit.

## FAQ

**Verändert oder löscht die App die Originaldateien?**
Nein. Quelldateien werden nur gelesen (für die Konvertierung in einen
temporären Cache kopiert, danach wird die Cache-Kopie gelöscht). Die
Ausgabe ist immer eine neue Datei im gewählten Zielordner.

**Brauche ich eine Internetverbindung?**
Nein, sie ist optional. Sie wird nur für die automatische
Online-Titelsuche genutzt (schönere Benennung der Ausgabe). Mit
aktiviertem Offline-Modus oder ohne verfügbares Netzwerk konvertiert die
App trotzdem, wobei der Spielname so verwendet wird, wie er auf dem
Datenträger erscheint.

**Kann ich mehrere Dateien gleichzeitig konvertieren?**
Ja, wähle mehrere Dateien in Schritt 1 aus — sie werden nacheinander in
einer Warteschlange verarbeitet, mit einer abschließenden
Zusammenfassung, wie viele erfolgreich/fehlgeschlagen waren.

**Ist das eine offizielle Microsoft-/Xbox-App?**
Nein. Es handelt sich um ein Hobbyprojekt, das nicht mit Microsoft
verbunden ist oder von Microsoft unterstützt wird. "Xbox" ist eine
eingetragene Marke der jeweiligen Eigentümer, hier nur beschreibend
verwendet.

## Bekannte Einschränkungen

- Immer nur eine Datei gleichzeitig, keine parallele Konvertierung.
- Die Kopie SAF → Cache erfordert freien Speicherplatz von mindestens
  dem 2-Fachen der Größe des größten Spiels deiner Sammlung (die kopierte
  Quelldatei und die erzeugte Ausgabe bestehen vorübergehend gleichzeitig).
- Nur **arm64-v8a** (die überwiegende Mehrheit aktueller Smartphones;
  bei einem reinen 32-Bit-Gerät muss auch für `armeabi-v7a` neu
  kompiliert werden, was in diesem Build nicht enthalten ist).
- APK mit Debug-Schlüssel signiert: eine vorherige Version zu
  überschreiben funktioniert immer (gleiche Signatur), ist aber nicht
  über App-Stores verteilbar.
- Keine automatisierte Testsuite: jede Änderung wird durch einen sauberen
  Build und manuelle Tests auf einem echten Gerät überprüft.

## Aus dem Quellcode kompilieren

Erfordert Android NDK r27, Android SDK (Platform 34, Build-Tools
34.0.0), Gradle 8.7+, JDK 17+.

```bash
# 1. zstd, lz4, OpenSSL, curl für arm64-v8a mit dem NDK cross-kompilieren.
#    XGDTool/android/CMakeLists.txt erwartet standardmäßig alles unter
#    ~/android/install-arm64 (überschreibbar mit -DXGD_DEPS_PREFIX).

# 2. Build der nativen Bibliothek
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
# <host> = linux-x86_64, windows-x86_64 oder darwin-x86_64, je nach System

# 3. Build der APK
cd ../../../XGDToolAndroid
gradle clean assembleDebug
# APK unter app/build/outputs/apk/debug/app-debug.apk
```

Hinweis: `XGDTool/cmake/embed_resources.cmake` generiert
`XGDTool/src/Executable/AttachXbe.h` bei der ersten CMake-Konfiguration
aus einer im Submodul `external/Repackinator` enthaltenen Binärdatei —
nicht von Hand anfassen, sie generiert sich selbst neu.

## Lizenz und Danksagungen

Der XGDTool-Kern und dieses Portierung stehen unter **GPL-3.0** — siehe
[LICENSE](../LICENSE) im Repo-Root. Der C++-Kern bindet wiederum
Drittkomponenten ein, aufgeführt in
[ATTRIBUTION.md](../XGDTool/ATTRIBUTION.md).

Ursprungsprojekt: [wiredopposite/XGDTool](https://github.com/wiredopposite/XGDTool).
