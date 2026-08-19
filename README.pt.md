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

Port não oficial do [XGDTool](https://github.com/wiredopposite/XGDTool)
(GPL-3.0) para Android: converte imagens de disco Xbox / Xbox 360 (ISO,
XISO reduzida) para os formatos **ZAR**, **GOD**, **CCI** e **CSO**
diretamente a partir do telemóvel, para fazer cópia de segurança da tua
coleção física sem precisar de um PC.

<p align="center">
  <img src="docs/assets/screenshot_it.png" alt="Ecrã principal da aplicação" width="280">
</p>

## Funcionalidades

- Conversão entre XISO, ZAR, GOD, CCI, CSO — o mesmo motor em C++
  utilizado pela versão de secretária do XGDTool.
- Conversão **em lote**: seleciona vários ficheiros de uma vez, são
  processados automaticamente em fila.
- Pesquisa automática (opcional) do título do jogo online, para nomear a
  saída de forma legível.
- Sem permissões de armazenamento invasivas: tudo passa pelo Storage
  Access Framework do Android — tu escolhes quais pastas ficam visíveis
  para a aplicação.
- Interface em 7 idiomas (detetados automaticamente pelo idioma do
  telemóvel): italiano, inglês, francês, alemão, espanhol, português,
  chinês simplificado.
- Funciona como serviço em primeiro plano: podes sair da aplicação
  durante uma conversão longa sem a interromper.

## Requisitos

- Android 8.0 (API 26) ou superior, arquitetura **arm64-v8a**.
- Espaço livre de pelo menos 2× o tamanho do maior jogo da tua coleção.

## Instalação

Vai à página [Releases](../../releases) deste repositório, transfere o
`XGDTool-android-debug.apk` mais recente e instala-o no telemóvel (será
necessário ativar "Instalar apps desconhecidas"). Para o guia de
utilização completo e resolução de problemas, consulta
[docs/MANUAL.pt.md](docs/MANUAL.pt.md).

## Compilar a partir do código-fonte

Requer Android NDK r27, Android SDK (platform 34), Gradle 8.7+, JDK 17+.
Instruções completas em
[docs/MANUAL.pt.md](docs/MANUAL.pt.md#compilar-a-partir-do-código-fonte).

## Aviso

Projeto amador, não afiliado nem patrocinado pela Microsoft. "Xbox" é
uma marca registada do respetivo proprietário, usada aqui apenas de
forma descritiva. Concebido para cópias de segurança pessoais de discos
possuídos legalmente.

## Licença

O núcleo do XGDTool está sob GPL-3.0 — ver [LICENSE](LICENSE).
Componentes de terceiros integrados no núcleo estão listados em
[XGDTool/ATTRIBUTION.md](XGDTool/ATTRIBUTION.md). Se partilhares
publicamente uma versão modificada, a GPL-3.0 exige disponibilizar
também o código-fonte modificado.
