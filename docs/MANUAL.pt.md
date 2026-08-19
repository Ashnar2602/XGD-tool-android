<p align="center">
  <a href="MANUAL.md">🇬🇧 English</a> ·
  <a href="MANUAL.it.md">🇮🇹 Italiano</a> ·
  <a href="MANUAL.fr.md">🇫🇷 Français</a> ·
  <a href="MANUAL.de.md">🇩🇪 Deutsch</a> ·
  <a href="MANUAL.es.md">🇪🇸 Español</a> ·
  <a href="MANUAL.pt.md">🇵🇹 Português</a> ·
  <a href="MANUAL.zh-CN.md">🇨🇳 简体中文</a>
</p>

# Manual de utilização e depuração — XGDTool para Android

## Índice

- [O que é](#o-que-é)
- [Requisitos](#requisitos)
- [Instalação](#instalação)
- [Guia rápido](#guia-rápido)
- [Guia completo da interface](#guia-completo-da-interface)
- [Formatos de saída](#formatos-de-saída)
- [Como funciona internamente](#como-funciona-internamente)
- [Depuração / diagnóstico de problemas](#depuração--diagnóstico-de-problemas)
- [Perguntas frequentes](#perguntas-frequentes)
- [Limitações conhecidas](#limitações-conhecidas)
- [Compilar a partir do código-fonte](#compilar-a-partir-do-código-fonte)
- [Licença e créditos](#licença-e-créditos)

## O que é

O XGDTool para Android traz o núcleo em C++ do
[XGDTool](https://github.com/wiredopposite/XGDTool) (GPL-3.0) — o mesmo
motor de conversão usado no PC — diretamente para o telemóvel, para
converter imagens de disco Xbox e Xbox 360 (ISO, XISO reduzida) em
formatos mais compactos ou compatíveis com emuladores (**ZAR**, **GOD**,
**CCI**, **CSO**), sem precisar de um computador.

A interface gráfica original (wxWidgets) não é portável para Android,
por isso foi substituída por uma app em Kotlin que comunica com o mesmo
núcleo C++ via JNI.

## Requisitos

- Android 8.0 (API 26) ou superior.
- Arquitetura **arm64-v8a** (a grande maioria dos telemóveis Android
  recentes). Dispositivos apenas de 32 bits não são suportados por esta
  compilação — ver
  [Compilar a partir do código-fonte](#compilar-a-partir-do-código-fonte).
- Espaço livre de pelo menos **2× o tamanho do maior jogo** da tua
  coleção (o ficheiro de origem copiado e a saída produzida coexistem
  temporariamente durante a conversão).
- Ligação à internet **opcional**: apenas necessária para a pesquisa
  automática do título online (para nomear melhor os ficheiros
  convertidos); a app funciona perfeitamente sem ligação.

## Instalação

1. Transfere o `XGDTool-android-debug.apk` mais recente a partir da
   secção [Releases](../../releases) deste repositório.
2. No telemóvel, ativa "Instalar apps desconhecidas" para a app que usas
   para abrir o ficheiro (gestor de ficheiros, navegador, etc.).
3. Instala o APK. Está assinado com a chave de depuração padrão do
   Android — ótimo para uso pessoal, não pensado para a Play Store.
4. No primeiro arranque, a app pede a permissão de notificações
   (necessária para a notificação de progresso do serviço em segundo
   plano).

A app deteta automaticamente o idioma do sistema entre os 7 suportados
(italiano, inglês, francês, alemão, espanhol, português, chinês
simplificado); se o idioma do telemóvel não estiver entre estes, usa o
inglês como alternativa.

## Guia rápido

1. Toca em **Selecionar ISO/XISO a converter** e escolhe um ou mais
   ficheiros (seleção múltipla suportada: mantém premido um ficheiro no
   seletor do sistema para a ativar).
2. Toca em **Selecionar pasta de destino** e escolhe onde serão guardados
   os ficheiros convertidos — qualquer pasta acessível a partir do
   telemóvel, incluindo um cartão SD externo.
3. Escolhe o **formato de saída** entre os chips disponíveis (ZAR está
   pré-selecionado).
4. Toca em **Converter**.
5. Acompanha o progresso no cartão "Progresso" e no registo no fundo do
   ecrã. Podes sair da app durante a conversão: ela funciona como um
   serviço em primeiro plano com a sua própria notificação, e sobrevive
   ao passar para segundo plano.

## Guia completo da interface

**Origem** — o(s) ficheiro(s) ISO/XISO a converter. O seletor usa o
Storage Access Framework do Android: podes escolher qualquer fornecedor
de armazenamento visível para o teu telemóvel (armazenamento interno,
cartão SD, pastas de sincronização na nuvem local, etc.).

**Destino** — a pasta onde serão escritos os ficheiros convertidos.
Também aqui, qualquer pasta acessível via SAF.

**Opções de conversão**
- *Formato*: ver [Formatos de saída](#formatos-de-saída) abaixo.
- *Modo offline*: se desativado, a app tenta procurar online o título
  correto do jogo para nomear melhor a saída (requer rede, com um tempo
  limite máximo de alguns segundos — se a rede for lenta ou estiver
  ausente, a app espera esse tempo máximo e depois continua offline de
  qualquer forma). Se ativado, este passo é totalmente ignorado.

**Progresso** — cada ficheiro passa por 3 fases visíveis, cada uma com a
sua etiqueta e barra de progresso:

1. **Cópia local** — o ficheiro escolhido é copiado da sua localização
   SAF para a cache privada da app (necessário porque o motor nativo
   trabalha com caminhos reais, não com Uris content://).
2. **Conversão** — verificação de ligação, pesquisa de título online (se
   não estiver offline), e depois a escrita propriamente dita. Começa com
   uma barra "indeterminada" (a deslizar) porque a duração da fase de
   rede não é previsível, e depois passa a uma percentagem real assim
   que começa a escrever dados.
3. **Escrita de saída** — cópia do resultado da cache para a pasta de
   destino escolhida.

Com vários ficheiros selecionados, estas 3 fases repetem-se em sequência
para cada um (sem conversão em paralelo — ver
[Limitações conhecidas](#limitações-conhecidas)); o cabeçalho mostra
"Ficheiro X de Y" com o nome do ficheiro atual.

**Cancelar** interrompe a conversão em curso de forma limpa, no próximo
ponto de controlo útil — não trunca uma escrita a meio.

**Registo** — mostra cada linha produzida pelo motor nativo, útil para
perceber exatamente o que está a fazer ou porque é que um ficheiro
falhou.

## Formatos de saída

| Formato | Uso típico |
|---|---|
| **ZAR** | Arquivo comprimido universal, o formato mais eficiente para usar com o **Xenia Canary** (emulador de Xbox 360). Pré-selecionado. |
| **GOD** | *Games on Demand* — formato nativo usado pela Xbox 360 e por vários front-ends/configurações RGH. |
| **CCI** | Formato comprimido pensado para emuladores da Xbox original. |
| **CSO** | Formato comprimido, alternativa ao CCI suportada por vários emuladores. |

## Como funciona internamente

```
Uri SAF (entrada)
   │  cópia byte a byte com progresso
   ▼
cache privada da app (caminho real)
   │  XgdNative.convert() — JNI, síncrono, numa thread dedicada
   ▼
libxgdtool.so (núcleo C++ do XGDTool)
   │  escreve o formato escolhido numa pasta de cache
   ▼
cache privada da app (saída)
   │  cópia byte a byte com progresso para o destino SAF
   ▼
Pasta de destino escolhida pelo utilizador
```

Um ficheiro de cada vez, em fila sequencial. Cada fase reporta bytes
processados/total à interface através de callbacks JNI
(`XgdCallback.onLog` / `onProgress`), geridos no `ConvertService` (um
serviço em primeiro plano do Android).

## Depuração / diagnóstico de problemas

Se um ficheiro falhar, o registo da app mostra o motivo — procura uma
linha do tipo:

```
<Tipo de erro> em <ficheiro:linha>: <detalhe>
```

Se precisares de mais contexto do que o mostrado na app (por exemplo,
para perceber um crash nativo, não apenas uma exceção tratada), liga o
telemóvel a um PC com `adb` e executa, durante uma conversão:

```bash
adb logcat -s XgdJNI:* XgdCore:*
```

- `XgdCore` imprime cada linha de registo e cada atualização de
  progresso emitida pelo código C++ do conversor (mesmo conteúdo do
  registo na app, mas nada se perde se a app for colocada em segundo
  plano ou o processo morrer).
- `XgdJNI` imprime o diagnóstico da ponte JNI (resolução de métodos de
  callback, exceções pendentes).

Se a app se comportar de forma estranha sem registos úteis nem no
logcat, é provável um crash nativo (SIGSEGV) — nesse caso vais precisar
de um `adb logcat` completo (não filtrado) capturado logo após o crash,
ou de um tombstone (`adb bugreport` / `/data/tombstones`).

Problemas comuns:

- **O seletor de ficheiros do sistema mostra "Nenhum elemento"** mesmo
  ao navegar no armazenamento principal do telemóvel — é um problema de
  um componente do sistema Android
  (DocumentsUI/ExternalStorageProvider), não da app. Tenta reiniciar o
  telemóvel, ou limpar a cache da app do sistema "Ficheiros"/"Gestor de
  ficheiros" nas Definições.
- **Cópia bloqueada ou ficheiro truncado**: verifica o espaço livre (ver
  [Requisitos](#requisitos)); a app deteta e assinala uma cópia
  incompleta em vez de continuar com dados parciais.
- **Conversão demora a iniciar**: se o modo offline estiver desativado e
  a rede estiver ausente ou muito lenta, a app espera pelo tempo limite
  máximo da pesquisa de título online antes de continuar — ativar o
  modo offline evita esta espera.

## Perguntas frequentes

**A app modifica ou apaga os ficheiros originais?**
Não. Os ficheiros de origem são apenas lidos (copiados para uma cache
temporária para conversão, e depois a cópia em cache é eliminada assim
que termina). A saída é sempre um novo ficheiro na pasta de destino que
escolheste.

**Preciso de ligação à internet?**
Não, é opcional. É usada apenas para a pesquisa automática do título
online (nomeação mais clara da saída). Com o modo offline ativado, ou
sem rede disponível, a app converte na mesma, usando o nome do jogo tal
como aparece no disco.

**Posso converter vários ficheiros de uma vez?**
Sim, seleciona vários ficheiros no passo 1 — serão processados numa fila
um a um, com um resumo final de quantos tiveram sucesso/falharam.

**É uma app oficial da Microsoft/Xbox?**
Não. É um projeto amador, não afiliado nem apoiado pela Microsoft.
"Xbox" é uma marca registada dos respetivos proprietários, usada aqui
apenas de forma descritiva/de compatibilidade.

## Limitações conhecidas

- Um ficheiro de cada vez, sem conversão em paralelo.
- A cópia SAF → cache requer espaço livre de pelo menos 2× o tamanho do
  maior jogo da tua coleção (o ficheiro de origem copiado e a saída
  produzida coexistem temporariamente).
- Apenas **arm64-v8a** (a grande maioria dos telemóveis recentes; se o
  teu for um dispositivo apenas de 32 bits, é preciso recompilar também
  para `armeabi-v7a`, não incluído nesta compilação).
- APK assinado com a chave de depuração: reinstalar sobre uma versão
  anterior funciona sempre (mesma assinatura), mas não é distribuível
  através de lojas de apps.
- Sem suite de testes automatizada: cada alteração é verificada com uma
  compilação limpa e testes manuais num dispositivo real.

## Compilar a partir do código-fonte

Requer Android NDK r27, Android SDK (platform 34, build-tools 34.0.0),
Gradle 8.7+, JDK 17+.

```bash
# 1. Compila cruzadamente zstd, lz4, OpenSSL, curl para arm64-v8a com o NDK.
#    XGDTool/android/CMakeLists.txt espera tudo em
#    ~/android/install-arm64 por predefinição (substituível com -DXGD_DEPS_PREFIX).

# 2. Compilação da biblioteca nativa
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
# <host> = linux-x86_64, windows-x86_64 ou darwin-x86_64 consoante o teu sistema

# 3. Compilação do APK
cd ../../../XGDToolAndroid
gradle clean assembleDebug
# APK em app/build/outputs/apk/debug/app-debug.apk
```

Nota: `XGDTool/cmake/embed_resources.cmake` gera
`XGDTool/src/Executable/AttachXbe.h` a partir de um binário incluído no
submódulo `external/Repackinator` na primeira configuração do CMake —
não mexas nele manualmente, regenera-se sozinho.

## Licença e créditos

O núcleo do XGDTool e este port são distribuídos sob **GPL-3.0** — ver
[LICENSE](../LICENSE) na raiz do repositório. O núcleo em C++ integra
por sua vez componentes de terceiros listados em
[ATTRIBUTION.md](../XGDTool/ATTRIBUTION.md).

Projeto original: [wiredopposite/XGDTool](https://github.com/wiredopposite/XGDTool).
