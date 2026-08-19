<p align="center">
  <a href="MANUAL.md">🇬🇧 English</a> ·
  <a href="MANUAL.it.md">🇮🇹 Italiano</a> ·
  <a href="MANUAL.fr.md">🇫🇷 Français</a> ·
  <a href="MANUAL.de.md">🇩🇪 Deutsch</a> ·
  <a href="MANUAL.es.md">🇪🇸 Español</a> ·
  <a href="MANUAL.pt.md">🇵🇹 Português</a> ·
  <a href="MANUAL.zh-CN.md">🇨🇳 简体中文</a>
</p>

# Manuel d'utilisation et de débogage — XGDTool pour Android

## Sommaire

- [Qu'est-ce que c'est](#quest-ce-que-cest)
- [Prérequis](#prérequis)
- [Installation](#installation)
- [Démarrage rapide](#démarrage-rapide)
- [Guide complet de l'interface](#guide-complet-de-linterface)
- [Formats de sortie](#formats-de-sortie)
- [Fonctionnement interne](#fonctionnement-interne)
- [Débogage / résolution de problèmes](#débogage--résolution-de-problèmes)
- [FAQ](#faq)
- [Limites connues](#limites-connues)
- [Compiler depuis les sources](#compiler-depuis-les-sources)
- [Licence et crédits](#licence-et-crédits)

## Qu'est-ce que c'est

XGDTool pour Android porte le cœur en C++ de
[XGDTool](https://github.com/wiredopposite/XGDTool) (GPL-3.0) — le même
moteur de conversion que sur PC — directement sur le téléphone, pour
convertir les images disque Xbox et Xbox 360 (ISO, XISO allégée) en
formats plus compacts ou compatibles avec les émulateurs (**ZAR**,
**GOD**, **CCI**, **CSO**), sans avoir besoin d'un ordinateur.

L'interface graphique d'origine (wxWidgets) n'est pas portable sur
Android ; elle a donc été remplacée par une application Kotlin qui
communique avec le même cœur C++ via JNI.

## Prérequis

- Android 8.0 (API 26) ou supérieur.
- Architecture **arm64-v8a** (la grande majorité des téléphones Android
  récents). Les appareils uniquement 32 bits ne sont pas pris en charge
  par cette version — voir
  [Compiler depuis les sources](#compiler-depuis-les-sources).
- Espace libre égal à au moins **2× la taille du plus gros jeu** de votre
  collection (le fichier source copié et le résultat produit coexistent
  temporairement pendant la conversion).
- Connexion internet **facultative** : nécessaire uniquement pour la
  recherche automatique du titre en ligne (pour nommer plus clairement
  les fichiers convertis) ; l'application fonctionne très bien sans
  réseau.

## Installation

1. Téléchargez le dernier `XGDTool-android-debug.apk` depuis la section
   [Releases](../../releases) de ce dépôt.
2. Sur votre téléphone, autorisez "Installer des applications inconnues"
   pour l'application que vous utilisez pour ouvrir le fichier
   (gestionnaire de fichiers, navigateur, etc.).
3. Installez l'APK. Il est signé avec la clé de débogage standard
   d'Android — parfait pour un usage personnel, non destiné au Play
   Store.
4. Au premier lancement, l'application demande la permission des
   notifications (nécessaire pour la notification de progression du
   service en arrière-plan).

L'application détecte automatiquement la langue du système parmi les 7
prises en charge (italien, anglais, français, allemand, espagnol,
portugais, chinois simplifié) ; si la langue du téléphone n'en fait pas
partie, elle bascule sur l'anglais.

## Démarrage rapide

1. Appuyez sur **Sélectionner des ISO/XISO à convertir** et choisissez un
   ou plusieurs fichiers (sélection multiple prise en charge : appui long
   sur un fichier dans le sélecteur système pour l'activer).
2. Appuyez sur **Sélectionner le dossier de destination** et choisissez
   où seront enregistrés les fichiers convertis — n'importe quel dossier
   accessible depuis votre téléphone, y compris une carte SD externe.
3. Choisissez le **format de sortie** parmi les puces disponibles (ZAR
   est présélectionné).
4. Appuyez sur **Convertir**.
5. Suivez la progression dans la carte "Progression" et le journal en bas
   de l'écran. Vous pouvez quitter l'application pendant la conversion :
   elle fonctionne comme un service au premier plan avec sa propre
   notification, et survit au passage en arrière-plan.

## Guide complet de l'interface

**Source** — le ou les fichiers ISO/XISO à convertir. Le sélecteur
utilise le Storage Access Framework d'Android : vous pouvez choisir
n'importe quel fournisseur de stockage visible par votre téléphone
(stockage interne, carte SD, dossiers de synchronisation cloud locaux,
etc.).

**Destination** — le dossier où seront écrits les fichiers convertis. Là
aussi, n'importe quel dossier accessible via SAF.

**Options de conversion**
- *Format* : voir [Formats de sortie](#formats-de-sortie) ci-dessous.
- *Mode hors ligne* : si désactivé, l'application essaie de trouver en
  ligne le titre exact du jeu pour nommer plus clairement la sortie
  (nécessite le réseau, avec un délai maximal de quelques secondes — si
  le réseau est lent ou absent, l'application attend ce délai maximal
  puis continue hors ligne). Si activé, cette étape est entièrement
  ignorée.

**Progression** — chaque fichier passe par 3 phases visibles, chacune
avec son étiquette et sa barre de progression :

1. **Copie locale** — le fichier choisi est copié depuis son
   emplacement SAF vers le cache privé de l'application (nécessaire car
   le moteur natif travaille sur des chemins réels, pas sur des Uri
   content://).
2. **Conversion** — vérification de la connexion, recherche du titre en
   ligne (si non hors ligne), puis écriture proprement dite. Démarre
   avec une barre "indéterminée" (défilante) car la durée de la phase
   réseau n'est pas prévisible, puis passe à un pourcentage réel dès que
   l'écriture des données commence.
3. **Écriture de la sortie** — copie du résultat du cache vers le
   dossier de destination choisi.

Avec plusieurs fichiers sélectionnés, ces 3 phases se répètent pour
chacun d'eux (aucune conversion en parallèle — voir
[Limites connues](#limites-connues)) ; l'en-tête affiche "Fichier X sur
Y" avec le nom du fichier en cours.

**Annuler** arrête proprement la conversion en cours, au prochain point
de contrôle utile — cela ne tronque pas une écriture en cours.

**Journal** — affiche chaque ligne produite par le moteur natif, utile
pour voir exactement ce qu'il fait ou pourquoi un fichier a échoué.

## Formats de sortie

| Format | Usage typique |
|---|---|
| **ZAR** | Archive compressée universelle, le format le plus efficace pour une utilisation avec **Xenia Canary** (émulateur Xbox 360). Présélectionné. |
| **GOD** | *Games on Demand* — format natif utilisé par la Xbox 360 et divers front-ends/configurations RGH. |
| **CCI** | Format compressé destiné aux émulateurs Xbox d'origine. |
| **CSO** | Format compressé, alternative à CCI prise en charge par plusieurs émulateurs. |

## Fonctionnement interne

```
Uri SAF (entrée)
   │  copie octet par octet avec progression
   ▼
cache privé de l'application (chemin réel)
   │  XgdNative.convert() — JNI, synchrone, sur un thread dédié
   ▼
libxgdtool.so (cœur C++ de XGDTool)
   │  écrit le format choisi dans un dossier de cache
   ▼
cache privé de l'application (sortie)
   │  copie octet par octet avec progression vers la destination SAF
   ▼
Dossier de destination choisi par l'utilisateur
```

Un seul fichier à la fois, en file d'attente séquentielle. Chaque phase
signale les octets traités/total à l'interface via des callbacks JNI
(`XgdCallback.onLog` / `onProgress`), gérés dans `ConvertService` (un
service au premier plan Android).

## Débogage / résolution de problèmes

Si un fichier échoue, le journal de l'application affiche la raison —
cherchez une ligne du type :

```
<Type d'erreur> dans <fichier:ligne> : <détail>
```

Si vous avez besoin de plus de contexte que ce qui est affiché dans
l'application (par ex. pour comprendre un crash natif, pas seulement une
exception gérée), connectez le téléphone à un PC avec `adb` et lancez,
pendant une conversion :

```bash
adb logcat -s XgdJNI:* XgdCore:*
```

- `XgdCore` affiche chaque ligne de journal et chaque mise à jour de
  progression émise par le code C++ du convertisseur (même contenu que
  le journal dans l'application, mais rien n'est perdu si l'application
  passe en arrière-plan ou si le processus meurt).
- `XgdJNI` affiche les diagnostics du pont JNI (résolution des méthodes
  de callback, exceptions en attente).

Si l'application se comporte étrangement sans journal utile même dans
logcat, un crash natif (SIGSEGV) est probable — dans ce cas, il vous
faudra un `adb logcat` complet (non filtré) pris juste après le crash,
ou un tombstone (`adb bugreport` / `/data/tombstones`).

Problèmes courants :

- **Le sélecteur de fichiers système affiche "Aucun élément"** même en
  naviguant dans le stockage principal du téléphone — c'est un problème
  d'un composant système Android (DocumentsUI/ExternalStorageProvider),
  pas de l'application. Essayez de redémarrer le téléphone, ou de vider
  le cache de l'application système "Fichiers"/"Gestionnaire de
  fichiers" depuis les paramètres.
- **Copie bloquée ou fichier tronqué** : vérifiez l'espace libre (voir
  [Prérequis](#prérequis)) ; l'application détecte et signale une copie
  incomplète au lieu de continuer avec des données partielles.
- **Conversion lente à démarrer** : si le mode hors ligne est désactivé
  et que le réseau est absent ou très lent, l'application attend le
  délai maximal de la recherche de titre en ligne avant de continuer —
  activer le mode hors ligne évite cette attente.

## FAQ

**L'application modifie-t-elle ou supprime-t-elle les fichiers
originaux ?**
Non. Les fichiers source sont uniquement lus (copiés dans un cache
temporaire pour la conversion, puis la copie en cache est supprimée une
fois terminé). La sortie est toujours un nouveau fichier dans le dossier
de destination que vous avez choisi.

**Ai-je besoin d'une connexion internet ?**
Non, c'est facultatif. Elle n'est utilisée que pour la recherche
automatique du titre en ligne (meilleur nommage de la sortie). Avec le
mode hors ligne activé, ou sans réseau disponible, l'application
convertit quand même, en utilisant le nom du jeu tel qu'il apparaît sur
le disque.

**Puis-je convertir plusieurs fichiers à la fois ?**
Oui, sélectionnez plusieurs fichiers à l'étape 1 — ils seront traités en
file d'attente un par un, avec un résumé final du nombre de réussites et
d'échecs.

**Est-ce une application officielle Microsoft/Xbox ?**
Non. C'est un projet amateur, non affilié ni approuvé par Microsoft.
"Xbox" est une marque déposée de ses propriétaires respectifs, utilisée
ici uniquement à titre descriptif/de compatibilité.

## Limites connues

- Un seul fichier à la fois, aucune conversion en parallèle.
- La copie SAF → cache nécessite un espace libre égal à au moins 2× la
  taille du plus gros jeu de votre collection (le fichier source copié
  et la sortie produite coexistent temporairement).
- **arm64-v8a** uniquement (la grande majorité des téléphones récents ;
  si le vôtre est un appareil uniquement 32 bits, il faut le recompiler
  aussi pour `armeabi-v7a`, non inclus dans cette version).
- APK signé avec la clé de débogage : réinstaller par-dessus une version
  précédente fonctionne toujours (même signature), mais n'est pas
  distribuable via les stores d'applications.
- Aucune suite de tests automatisée : chaque modification est vérifiée
  par une compilation propre et des tests manuels sur un appareil réel.

## Compiler depuis les sources

Nécessite Android NDK r27, Android SDK (platform 34, build-tools 34.0.0),
Gradle 8.7+, JDK 17+.

```bash
# 1. Compilez zstd, lz4, OpenSSL, curl pour arm64-v8a avec le NDK.
#    XGDTool/android/CMakeLists.txt attend tout sous
#    ~/android/install-arm64 par défaut (à modifier avec -DXGD_DEPS_PREFIX).

# 2. Compilation de la bibliothèque native
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
# <host> = linux-x86_64, windows-x86_64 ou darwin-x86_64 selon votre système

# 3. Compilation de l'APK
cd ../../../XGDToolAndroid
gradle clean assembleDebug
# APK dans app/build/outputs/apk/debug/app-debug.apk
```

Note : `XGDTool/cmake/embed_resources.cmake` génère
`XGDTool/src/Executable/AttachXbe.h` à partir d'un binaire inclus dans le
sous-module `external/Repackinator` lors de la première configuration
CMake — ne le modifiez pas à la main, il se régénère tout seul.

## Licence et crédits

Le cœur de XGDTool et ce portage sont distribués sous **GPL-3.0** — voir
[LICENSE](../LICENSE) à la racine du dépôt. Le cœur C++ intègre à son
tour des composants tiers listés dans
[ATTRIBUTION.md](../XGDTool/ATTRIBUTION.md).

Projet d'origine : [wiredopposite/XGDTool](https://github.com/wiredopposite/XGDTool).
