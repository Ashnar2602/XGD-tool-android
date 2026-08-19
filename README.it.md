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

# XGDTool per Android

Porting non ufficiale di [XGDTool](https://github.com/wiredopposite/XGDTool)
(GPL-3.0) su Android: converte immagini disco Xbox / Xbox 360 (ISO, XISO
strippate) nei formati **ZAR**, **GOD**, **CCI** e **CSO** direttamente
dal telefono, per gestire il backup della propria collezione fisica senza
passare da un PC.

<p align="center">
  <img src="docs/assets/screenshot_it.png" alt="Schermata principale dell'app" width="280">
</p>

## Caratteristiche

- Conversione tra XISO, ZAR, GOD, CCI, CSO — lo stesso motore C++ usato
  dalla versione desktop di XGDTool.
- Conversione **batch**: seleziona più file insieme, vengono processati
  in coda automaticamente.
- Ricerca automatica (opzionale) del titolo del gioco online, per
  rinominare l'output in modo leggibile.
- Nessun permesso di storage invasivo: tutto passa dallo Storage Access
  Framework di Android — scegli tu quali cartelle rendere visibili
  all'app.
- Interfaccia in 7 lingue (rilevate automaticamente dalla lingua del
  telefono): italiano, inglese, francese, tedesco, spagnolo, portoghese,
  cinese semplificato.
- Gira come servizio in primo piano: puoi uscire dall'app durante una
  conversione lunga senza interromperla.

## Requisiti

- Android 8.0 (API 26) o superiore, architettura **arm64-v8a**.
- Spazio libero pari ad almeno 2× la dimensione del gioco più grande
  della tua collezione.

## Installazione

Vai su [Releases](../../releases) di questa repo, scarica l'ultimo
`XGDTool-android-debug.apk` e installalo sul telefono (serve abilitare
"Installa app sconosciute"). Per la guida d'uso completa e la risoluzione
problemi vedi [docs/MANUAL.it.md](docs/MANUAL.it.md).

## Build da sorgente

Richiede Android NDK r27, Android SDK (platform 34), Gradle 8.7+, JDK 17+.
Istruzioni complete in [docs/MANUAL.it.md](docs/MANUAL.it.md#ricompilare-da-zero).

## Avviso

Progetto amatoriale, non affiliato né sponsorizzato da Microsoft. "Xbox"
è un marchio registrato dei rispettivi proprietari, usato qui solo a
scopo descrittivo. Pensato per il backup personale di dischi posseduti
legalmente.

## Licenza

Il core XGDTool è GPL-3.0 — vedi [LICENSE](LICENSE). Componenti di terze
parti integrate nel core sono elencate in
[XGDTool/ATTRIBUTION.md](XGDTool/ATTRIBUTION.md). Se condividi
pubblicamente una versione modificata, la GPL-3.0 richiede di rendere
disponibile anche il sorgente modificato.
