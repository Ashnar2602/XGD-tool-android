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

# XGDTool für Android

Inoffizieller Port von [XGDTool](https://github.com/wiredopposite/XGDTool)
(GPL-3.0) auf Android: konvertiert Xbox- / Xbox-360-Disk-Images (ISO,
gestripptes XISO) direkt auf dem Smartphone in die Formate **ZAR**,
**GOD**, **CCI** und **CSO** — ohne PC, um die eigene physische Sammlung
zu sichern.

<p align="center">
  <img src="docs/assets/screenshot_it.png" alt="Hauptbildschirm der App" width="280">
</p>

## Funktionen

- Konvertierung zwischen XISO, ZAR, GOD, CCI, CSO — dieselbe C++-Engine
  wie die Desktop-Version von XGDTool.
- **Stapelkonvertierung**: mehrere Dateien gleichzeitig auswählen, sie
  werden automatisch nacheinander verarbeitet.
- Optionale automatische Online-Titelsuche, um die Ausgabedatei lesbar
  zu benennen.
- Keine aufdringlichen Speicherberechtigungen: alles läuft über Androids
  Storage Access Framework — du entscheidest, welche Ordner für die App
  sichtbar sind.
- Oberfläche in 7 Sprachen (automatisch anhand der Systemsprache
  erkannt): Italienisch, Englisch, Französisch, Deutsch, Spanisch,
  Portugiesisch, vereinfachtes Chinesisch.
- Läuft als Vordergrunddienst: du kannst die App während einer langen
  Konvertierung verlassen, ohne sie zu unterbrechen.

## Voraussetzungen

- Android 8.0 (API 26) oder höher, **arm64-v8a**-Architektur.
- Freier Speicherplatz von mindestens dem 2-Fachen der Größe des größten
  Spiels deiner Sammlung.

## Installation

Gehe zur [Releases](../../releases)-Seite dieses Repos, lade die neueste
`XGDTool-android-debug.apk` herunter und installiere sie auf dem
Smartphone (dafür muss "Unbekannte Apps installieren" aktiviert werden).
Die vollständige Anleitung und Fehlerbehebung findest du in
[docs/MANUAL.de.md](docs/MANUAL.de.md).

## Aus dem Quellcode kompilieren

Erfordert Android NDK r27, Android SDK (Platform 34), Gradle 8.7+, JDK
17+. Vollständige Anleitung in
[docs/MANUAL.de.md](docs/MANUAL.de.md#aus-dem-quellcode-kompilieren).

## Hinweis

Hobbyprojekt, nicht verbunden mit oder unterstützt von Microsoft. "Xbox"
ist eine eingetragene Marke des jeweiligen Eigentümers und wird hier nur
beschreibend verwendet. Gedacht für persönliche Backups legal erworbener
Datenträger.

## Lizenz

Der XGDTool-Kern steht unter GPL-3.0 — siehe [LICENSE](LICENSE). Im Kern
enthaltene Drittkomponenten sind in
[XGDTool/ATTRIBUTION.md](XGDTool/ATTRIBUTION.md) aufgeführt. Wer eine
modifizierte Version öffentlich teilt, muss gemäß GPL-3.0 auch den
modifizierten Quellcode bereitstellen.
