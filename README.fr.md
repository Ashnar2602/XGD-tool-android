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

# XGDTool pour Android

Portage non officiel de [XGDTool](https://github.com/wiredopposite/XGDTool)
(GPL-3.0) sur Android : convertit les images disque Xbox / Xbox 360 (ISO,
XISO allégée) aux formats **ZAR**, **GOD**, **CCI** et **CSO**
directement depuis le téléphone, pour sauvegarder sa collection physique
sans passer par un PC.

<p align="center">
  <img src="docs/assets/screenshot_it.png" alt="Écran principal de l'application" width="280">
</p>

## Fonctionnalités

- Conversion entre XISO, ZAR, GOD, CCI, CSO — le même moteur C++ que la
  version bureau de XGDTool.
- Conversion **par lot** : sélectionnez plusieurs fichiers à la fois, ils
  sont traités automatiquement en file d'attente.
- Recherche automatique (facultative) du titre du jeu en ligne, pour
  renommer la sortie de façon lisible.
- Aucune permission de stockage intrusive : tout passe par le Storage
  Access Framework d'Android — vous choisissez quels dossiers rendre
  visibles à l'application.
- Interface en 7 langues (détectées automatiquement selon la langue du
  téléphone) : italien, anglais, français, allemand, espagnol, portugais,
  chinois simplifié.
- Fonctionne comme un service au premier plan : vous pouvez quitter
  l'application pendant une longue conversion sans l'interrompre.

## Prérequis

- Android 8.0 (API 26) ou supérieur, architecture **arm64-v8a**.
- Espace libre égal à au moins 2× la taille du plus gros jeu de votre
  collection.

## Installation

Rendez-vous sur la page [Releases](../../releases) de ce dépôt,
téléchargez le dernier `XGDTool-android-debug.apk` et installez-le sur
votre téléphone (il faudra autoriser "Installer des applications
inconnues"). Pour le guide d'utilisation complet et le dépannage, voir
[docs/MANUAL.fr.md](docs/MANUAL.fr.md).

## Compiler depuis les sources

Nécessite Android NDK r27, Android SDK (platform 34), Gradle 8.7+, JDK
17+. Instructions complètes dans
[docs/MANUAL.fr.md](docs/MANUAL.fr.md#compiler-depuis-les-sources).

## Avertissement

Projet amateur, non affilié ni approuvé par Microsoft. "Xbox" est une
marque déposée de son propriétaire respectif, utilisée ici uniquement à
titre descriptif. Conçu pour la sauvegarde personnelle de disques
possédés légalement.

## Licence

Le cœur XGDTool est sous licence GPL-3.0 — voir [LICENSE](LICENSE). Les
composants tiers intégrés au cœur sont listés dans
[XGDTool/ATTRIBUTION.md](XGDTool/ATTRIBUTION.md). Si vous partagez
publiquement une version modifiée, la GPL-3.0 exige de rendre également
disponible le code source modifié.
