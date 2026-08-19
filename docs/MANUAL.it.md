<p align="center">
  <a href="MANUAL.md">🇬🇧 English</a> ·
  <a href="MANUAL.it.md">🇮🇹 Italiano</a> ·
  <a href="MANUAL.fr.md">🇫🇷 Français</a> ·
  <a href="MANUAL.de.md">🇩🇪 Deutsch</a> ·
  <a href="MANUAL.es.md">🇪🇸 Español</a> ·
  <a href="MANUAL.pt.md">🇵🇹 Português</a> ·
  <a href="MANUAL.zh-CN.md">🇨🇳 简体中文</a>
</p>

# Manuale d'uso e debug — XGDTool per Android

## Indice

- [Cos'è](#cosè)
- [Requisiti](#requisiti)
- [Installazione](#installazione)
- [Guida rapida](#guida-rapida)
- [Guida completa all'interfaccia](#guida-completa-allinterfaccia)
- [Formati di output](#formati-di-output)
- [Come funziona internamente](#come-funziona-internamente)
- [Debug / diagnosi problemi](#debug--diagnosi-problemi)
- [Domande frequenti](#domande-frequenti)
- [Limiti noti](#limiti-noti)
- [Ricompilare da zero](#ricompilare-da-zero)
- [Licenza e crediti](#licenza-e-crediti)

## Cos'è

XGDTool per Android è un'app che porta il core C++ di
[XGDTool](https://github.com/wiredopposite/XGDTool) (GPL-3.0) — lo stesso
motore di conversione usato su PC — direttamente sul telefono, per
convertire immagini disco Xbox e Xbox 360 (ISO, XISO strippate) in formati
più compatti o compatibili con emulatori (**ZAR**, **GOD**, **CCI**,
**CSO**), senza dover passare da un computer.

La GUI originale (wxWidgets) non è portabile su Android, quindi è stata
sostituita con un'app Kotlin che parla con lo stesso core C++ tramite JNI.

## Requisiti

- Android 8.0 (API 26) o superiore.
- Architettura **arm64-v8a** (la stragrande maggioranza degli smartphone
  Android recenti). Dispositivi solo a 32 bit non sono supportati da
  questa build — vedi [Ricompilare da zero](#ricompilare-da-zero).
- Spazio libero pari ad almeno **2× la dimensione del gioco più grande**
  della tua collezione (il file sorgente copiato in cache e l'output
  prodotto coesistono temporaneamente durante la conversione).
- Connessione internet **opzionale**: serve solo se vuoi la ricerca
  automatica del titolo online (per nominare meglio i file convertiti);
  l'app funziona comunque offline.

## Installazione

1. Scarica l'ultimo `XGDTool-android-debug.apk` dalla sezione
   [Releases](../../releases) di questa repo.
2. Sul telefono, abilita "Installa app sconosciute" per l'app che usi per
   aprire il file (file manager, browser, ecc.).
3. Installa l'APK. È firmato con la debug key standard di Android — va
   bene per sideload personale, non è pensato per il Play Store.
4. Al primo avvio l'app chiede il permesso notifiche (serve per la
   notifica di progresso del servizio in background).

L'app rileva automaticamente la lingua del sistema tra le 7 supportate
(italiano, inglese, francese, tedesco, spagnolo, portoghese, cinese
semplificato); se la lingua del telefono non è tra queste, usa l'inglese
come lingua di riferimento.

## Guida rapida

1. Tocca **Seleziona ISO/XISO da convertire** e scegli uno o più file
   (selezione multipla supportata: tieni premuto un file nel selettore di
   sistema per attivarla).
2. Tocca **Seleziona cartella di destinazione** e scegli dove salvare i
   file convertiti — può essere qualunque cartella accessibile dal
   telefono, inclusa una SD esterna.
3. Scegli il **formato di output** tra i chip disponibili (ZAR è
   preselezionato).
4. Tocca **Converti**.
5. Segui l'avanzamento nella card "Avanzamento" e nel registro in fondo
   alla schermata. Puoi uscire dall'app durante la conversione: gira come
   servizio in primo piano con notifica dedicata, e sopravvive al
   backgrounding.

## Guida completa all'interfaccia

**Sorgente** — il file (o i file) ISO/XISO da convertire. Il selettore
usa lo Storage Access Framework di Android: puoi scegliere da qualunque
provider di archiviazione visibile al telefono (memoria interna, SD,
cloud locale, ecc.).

**Destinazione** — la cartella dove verranno scritti i file convertiti.
Anche qui, qualunque cartella accessibile via SAF.

**Opzioni di conversione**
- *Formato*: vedi [Formati di output](#formati-di-output) sotto.
- *Modalità offline*: se disattivata, l'app prova a cercare online il
  titolo corretto del gioco per nominare meglio l'output (richiede rete,
  con timeout massimo di qualche secondo — se la rete è lenta o assente
  l'app aspetta il tempo massimo e poi procede comunque offline). Se
  attivata, salta del tutto questo passaggio.

**Avanzamento** — ogni file passa per 3 fasi visibili, ciascuna con la
propria etichetta e barra di avanzamento:

1. **Copia locale** — il file scelto viene copiato dalla posizione SAF
   nella cache privata dell'app (necessario perché il motore nativo
   lavora su path reali, non su content:// Uri).
2. **Conversione** — verifica connessione, ricerca titolo online (se non
   offline), poi scrittura vera e propria. Parte con una barra
   "indeterminata" (a scorrimento) perché la durata della fase di rete
   non è prevedibile, poi passa a percentuale reale appena inizia a
   scrivere dati.
3. **Scrittura output** — copia del risultato dalla cache alla cartella
   di destinazione scelta.

Con più file selezionati, queste 3 fasi si ripetono in sequenza per
ciascuno (nessuna conversione in parallelo — vedi
[Limiti noti](#limiti-noti)); l'intestazione mostra "File X di Y" con il
nome del file corrente.

**Annulla** interrompe la conversione in corso (in modo pulito, al primo
punto di controllo utile — non tronca a metà una scrittura).

**Registro** — mostra ogni riga prodotta dal motore nativo, utile per
capire esattamente cosa sta facendo o perché un file è fallito.

## Formati di output

| Formato | Uso tipico |
|---|---|
| **ZAR** | Archivio compresso universale, formato più efficiente per l'uso con **Xenia Canary** (emulatore Xbox 360). Preselezionato. |
| **GOD** | *Games on Demand* — formato nativo usato da Xbox 360 e da diversi front-end/RGH. |
| **CCI** | Formato compresso pensato per l'uso con emulatori Xbox originali. |
| **CSO** | Formato compresso, alternativa a CCI supportata da diversi emulatori. |

## Come funziona internamente

```
SAF Uri (input)
   │  copia byte-a-byte con progresso
   ▼
cache privata app (path reale)
   │  XgdNative.convert() — JNI, sincrono, su thread dedicato
   ▼
libxgdtool.so (core C++ XGDTool)
   │  scrive il formato scelto in una cartella di cache
   ▼
cache privata app (output)
   │  copia byte-a-byte con progresso verso la destinazione SAF
   ▼
Cartella di destinazione scelta dall'utente
```

Un solo file alla volta, in coda sequenziale. Ogni fase riporta byte
processati/totali alla UI tramite callback JNI (`XgdCallback.onLog` /
`onProgress`), gestiti nel `ConvertService` (Foreground Service Android).

## Debug / diagnosi problemi

Se un file fallisce, il registro nell'app mostra il motivo — cerca una
riga del tipo:

```
<Tipo errore> in <file:riga>: <dettaglio>
```

Se ti serve più contesto di quello mostrato in app (es. per capire un
crash nativo, non solo un'eccezione gestita), collega il telefono a un PC
con `adb` e lancia, durante una conversione:

```bash
adb logcat -s XgdJNI:* XgdCore:*
```

- `XgdCore` stampa ogni riga di log e ogni aggiornamento di progresso
  emesso dal codice C++ del convertitore (stesso contenuto del registro
  in app, ma senza perdere nulla se l'app viene messa in background o se
  il processo muore).
- `XgdJNI` stampa la diagnostica del bridge JNI (risoluzione dei metodi
  di callback, eccezioni pendenti).

Se l'app si comporta in modo strano senza log utili nemmeno in logcat, è
probabile un crash nativo (SIGSEGV) — in quel caso serve un `adb logcat`
completo (non filtrato) preso subito dopo il crash, o un tombstone
(`adb bugreport` / `/data/tombstones`).

Problemi comuni:

- **Il selettore file di sistema mostra "Nessun elemento"** anche
  navigando nella memoria principale del telefono — è un problema del
  componente di sistema Android (DocumentsUI/ExternalStorageProvider),
  non dell'app. Prova a riavviare il telefono, o a svuotare la cache
  dell'app "File"/"Gestione file" di sistema da Impostazioni.
- **Copia bloccata o file troncato**: verifica lo spazio libero (vedi
  [Requisiti](#requisiti)); l'app rileva e segnala una copia incompleta
  invece di procedere con dati parziali.
- **Conversione lenta ad avviarsi**: se la modalità offline è disattivata
  e la rete è assente o molto lenta, l'app attende il timeout massimo
  della ricerca titolo online prima di procedere — attivare la modalità
  offline evita questa attesa.

## Domande frequenti

**L'app modifica o cancella i file originali?**
No. I file sorgente vengono solo letti (copiati in una cache temporanea
per la conversione, poi la copia in cache viene eliminata a fine
lavorazione). L'output è sempre un file nuovo nella cartella di
destinazione scelta.

**Serve una connessione internet?**
No, è opzionale. Serve solo per la ricerca automatica del titolo online
(rinomina più leggibile dell'output). Con la modalità offline attivata,
o senza rete disponibile, l'app converte comunque, usando il nome del
gioco come appare nel disco.

**Posso convertire più file insieme?**
Sì, seleziona più file nel passaggio 1 — verranno processati in coda uno
alla volta, con riepilogo finale di quanti sono riusciti/falliti.

**L'app è ufficiale Microsoft/Xbox?**
No. È un progetto amatoriale, non affiliato né sponsorizzato da
Microsoft. "Xbox" è un marchio registrato dei rispettivi proprietari,
usato qui solo a scopo descrittivo/di compatibilità.

## Limiti noti

- Un file alla volta, nessuna conversione parallela.
- La copia SAF → cache richiede spazio libero pari ad almeno 2× la
  dimensione del gioco più grande della tua collezione (il file sorgente
  copiato + l'output prodotto coesistono temporaneamente).
- Solo `arm64-v8a` (la stragrande maggioranza dei telefoni recenti; se il
  tuo è un dispositivo a 32 bit va ricompilato anche per `armeabi-v7a`,
  non incluso in questa build).
- APK firmato con debug key: reinstallarlo sovrascrivendo una versione
  precedente funziona sempre (stessa firma), ma non è distribuibile
  tramite store.
- Nessuna suite di test automatica: ogni modifica è verificata tramite
  compilazione pulita + test manuale su dispositivo reale.

## Ricompilare da zero

Serve: Android NDK r27, Android SDK (platform 34, build-tools 34.0.0),
Gradle 8.7+, JDK 17+.

```bash
# 1. Cross-compila zstd, lz4, OpenSSL, curl per arm64-v8a con l'NDK.
#    XGDTool/android/CMakeLists.txt si aspetta tutto sotto
#    ~/android/install-arm64 di default (override con -DXGD_DEPS_PREFIX).

# 2. Build della libreria nativa
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
# <host> = linux-x86_64, windows-x86_64 o darwin-x86_64 a seconda del tuo sistema

# 3. Build dell'APK
cd ../../../XGDToolAndroid
gradle clean assembleDebug
# APK in app/build/outputs/apk/debug/app-debug.apk
```

Nota: `XGDTool/cmake/embed_resources.cmake` genera
`XGDTool/src/Executable/AttachXbe.h` da un binario incluso nel submodule
`external/Repackinator` alla prima configurazione CMake — non toccarlo a
mano, si rigenera da solo.

## Licenza e crediti

Il core XGDTool e questo porting sono distribuiti sotto **GPL-3.0** — vedi
[LICENSE](../LICENSE) nella root della repo. Il core C++ integra a sua
volta componenti di terze parti elencati in
[ATTRIBUTION.md](../XGDTool/ATTRIBUTION.md).

Progetto upstream: [wiredopposite/XGDTool](https://github.com/wiredopposite/XGDTool).
