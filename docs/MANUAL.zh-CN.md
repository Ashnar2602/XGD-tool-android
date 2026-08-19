<p align="center">
  <a href="MANUAL.md">🇬🇧 English</a> ·
  <a href="MANUAL.it.md">🇮🇹 Italiano</a> ·
  <a href="MANUAL.fr.md">🇫🇷 Français</a> ·
  <a href="MANUAL.de.md">🇩🇪 Deutsch</a> ·
  <a href="MANUAL.es.md">🇪🇸 Español</a> ·
  <a href="MANUAL.pt.md">🇵🇹 Português</a> ·
  <a href="MANUAL.zh-CN.md">🇨🇳 简体中文</a>
</p>

# 使用与调试手册 — XGDTool Android 版

## 目录

- [简介](#简介)
- [系统要求](#系统要求)
- [安装](#安装)
- [快速上手](#快速上手)
- [界面完整指南](#界面完整指南)
- [输出格式](#输出格式)
- [内部工作原理](#内部工作原理)
- [调试与故障排除](#调试与故障排除)
- [常见问题](#常见问题)
- [已知限制](#已知限制)
- [从源码构建](#从源码构建)
- [许可与致谢](#许可与致谢)

## 简介

XGDTool Android 版将 [XGDTool](https://github.com/wiredopposite/XGDTool)
（GPL-3.0 协议）的 C++ 核心 —— 与 PC 版相同的转换引擎 —— 直接带到手机上，
用于将 Xbox 和 Xbox 360 光盘镜像（ISO、精简版 XISO）转换为更紧凑或与模拟
器兼容的格式（**ZAR**、**GOD**、**CCI**、**CSO**），完全无需借助电脑。

原始的图形界面（基于 wxWidgets）无法移植到 Android，因此被替换为一个通
过 JNI 与同一 C++ 核心通信的 Kotlin 应用。

## 系统要求

- Android 8.0（API 26）或更高版本。
- **arm64-v8a** 架构（绝大多数近期的 Android 手机均满足）。仅支持
  32 位的设备不受本构建支持 —— 参见[从源码构建](#从源码构建)。
- 可用存储空间至少为你收藏中**最大游戏文件大小的 2 倍**（转换过程中，
  复制的源文件与生成的输出文件会暂时同时存在）。
- 网络连接为**可选项**：仅在线标题查询功能需要联网（用于为转换后的文件
  生成更清晰的名称）；应用在离线状态下同样可以正常工作。

## 安装

1. 从本仓库的 [Releases](../../releases) 页面下载最新的
   `XGDTool-android-debug.apk`。
2. 在手机上，为用于打开该文件的应用（文件管理器、浏览器等）开启"安装未
   知应用"权限。
3. 安装该 APK。它使用 Android 标准调试密钥签名 —— 适合个人侧载安装，
   并非面向 Play 商店发布。
4. 首次启动时，应用会请求通知权限（用于后台服务的进度通知）。

应用会自动从 7 种受支持的语言中检测系统语言（意大利语、英语、法语、德
语、西班牙语、葡萄牙语、简体中文）；如果手机语言不在其中，则默认使用
英语。

## 快速上手

1. 点击**选择要转换的 ISO/XISO**，选择一个或多个文件（支持多选：在系
   统选择器中长按某个文件即可启用多选）。
2. 点击**选择目标文件夹**，选择转换后文件的保存位置 —— 手机上任何可访
   问的文件夹均可，包括外部 SD 卡。
3. 在可用的选项标签中选择**输出格式**（默认预选 ZAR）。
4. 点击**开始转换**。
5. 在"进度"卡片和屏幕底部的日志中查看进度。转换过程中可以退出应用：它
   会以带有专属通知的前台服务方式运行，切到后台也不会中断。

## 界面完整指南

**源文件** —— 需要转换的 ISO/XISO 文件（可以是多个）。选择器使用
Android 的 Storage Access Framework：你可以从手机可见的任何存储提供方
中选择（内部存储、SD 卡、本地云同步文件夹等）。

**目标位置** —— 转换后文件的写入文件夹。同样，任何通过 SAF 可访问的文
件夹均可。

**转换选项**
- *格式*：见下方[输出格式](#输出格式)。
- *离线模式*：若关闭，应用会尝试在线查询正确的游戏标题，以便为输出文
  件生成更清晰的名称（需要联网，最长等待几秒钟 —— 如果网络较慢或不可
  用，应用会等待该最长时限后仍然继续离线处理）。若开启，则完全跳过此
  步骤。

**进度** —— 每个文件会经历 3 个可见阶段，每个阶段都有各自的标签和进度
条：

1. **本地复制** —— 所选文件从其 SAF 位置复制到应用的私有缓存中（这是
   必要的，因为原生引擎处理的是真实路径，而非 content:// URI）。
2. **转换中** —— 网络检测、在线标题查询（若非离线模式），随后进行真正
   的写入。由于网络阶段耗时不可预测，起始阶段进度条为"不确定"（滚动）
   状态，一旦开始写入数据，即切换为真实的百分比进度。
3. **写入输出** —— 将结果从缓存复制到所选的目标文件夹。

选择多个文件时，这 3 个阶段会按顺序依次对每个文件重复执行（不支持并行
转换 —— 参见[已知限制](#已知限制)）；顶部会显示"第 X / Y 个文件"及当前
文件名。

**取消** 会在下一个合适的检查点干净地中止当前转换 —— 不会在写入过程中
截断数据。

**日志** —— 显示原生引擎产生的每一行输出，便于准确了解它正在做什么，
或某个文件失败的原因。

## 输出格式

| 格式 | 典型用途 |
|---|---|
| **ZAR** | 通用压缩存档，与 **Xenia Canary**（Xbox 360 模拟器）配合使用时效率最高的格式。默认预选。 |
| **GOD** | *Games on Demand* —— Xbox 360 及多种前端/RGH 方案使用的原生格式。 |
| **CCI** | 专为初代 Xbox 模拟器设计的压缩格式。 |
| **CSO** | 压缩格式，是多种模拟器都支持的 CCI 替代方案。 |

## 内部工作原理

```
SAF Uri（输入）
   │  带进度的逐字节复制
   ▼
应用私有缓存（真实路径）
   │  XgdNative.convert() —— JNI，同步执行，运行在独立线程
   ▼
libxgdtool.so（XGDTool C++ 核心）
   │  将所选格式写入缓存文件夹
   ▼
应用私有缓存（输出）
   │  带进度的逐字节复制，写入 SAF 目标位置
   ▼
用户选择的目标文件夹
```

一次只处理一个文件，按顺序排队执行。每个阶段都会通过 JNI 回调
（`XgdCallback.onLog` / `onProgress`）向界面报告已处理/总字节数，由
`ConvertService`（一个 Android 前台服务）负责管理。

## 调试与故障排除

如果某个文件转换失败，应用日志会显示原因 —— 查找类似这样的一行：

```
<错误类型> in <文件:行号>：<详细信息>
```

如果应用内显示的信息不足以定位问题（例如需要分析原生崩溃，而非已捕获
的异常），可以用 `adb` 将手机连接到电脑，并在转换过程中运行：

```bash
adb logcat -s XgdJNI:* XgdCore:*
```

- `XgdCore` 会打印转换器 C++ 代码输出的每一行日志和每次进度更新（内容
  与应用内日志相同，但即使应用被切到后台或进程被杀死也不会丢失）。
- `XgdJNI` 会打印 JNI 桥接层的诊断信息（回调方法解析、待处理异常等）。

如果应用行为异常，且连 logcat 中也没有有用的日志，很可能是发生了原生
崩溃（SIGSEGV）—— 此时需要在崩溃后立即获取一份完整（未过滤）的
`adb logcat`，或获取 tombstone 文件（`adb bugreport` /
`/data/tombstones`）。

常见问题：

- **系统文件选择器显示"没有项目"**，即使浏览的是手机主存储 —— 这是
  Android 系统组件（DocumentsUI/ExternalStorageProvider）的问题，与本
  应用无关。可尝试重启手机，或在系统设置中清除系统"文件"/"文件管理器"
  应用的缓存。
- **复制卡住或文件被截断**：检查可用存储空间（见[系统要求](#系统要求)）；
  应用会检测并报告复制不完整的情况，而不会使用部分数据继续处理。
- **转换启动缓慢**：如果关闭了离线模式且网络缺失或非常缓慢，应用会先
  等待在线标题查询的最长超时时间才会继续 —— 开启离线模式可避免这种等
  待。

## 常见问题

**应用会修改或删除原始文件吗？**
不会。源文件只会被读取（转换时复制到临时缓存，完成后该缓存副本会被删
除）。输出始终是你所选目标文件夹中的一个全新文件。

**需要网络连接吗？**
不需要，网络是可选的。它仅用于在线自动标题查询（让输出文件名更清晰易
读）。开启离线模式，或在没有可用网络时，应用仍会正常转换，使用光盘上
原本显示的游戏名称。

**可以一次转换多个文件吗？**
可以，在第 1 步选择多个文件即可 —— 它们会依次排队处理，完成后会显示
成功/失败数量的最终汇总。

**这是 Microsoft/Xbox 官方应用吗？**
不是。这是一个业余爱好项目，与 Microsoft 无任何关联，也未获其认可。
"Xbox" 是其各自所有者的注册商标，此处仅作描述性/兼容性说明之用。

## 已知限制

- 一次只能处理一个文件，不支持并行转换。
- SAF → 缓存的复制过程需要至少 2 倍于你收藏中最大游戏文件大小的可用空
  间（复制的源文件与生成的输出会暂时同时存在）。
- 仅支持 **arm64-v8a**（绝大多数近期手机均满足；如果你的设备仅支持
  32 位，还需要为 `armeabi-v7a` 重新编译，本构建未包含该架构）。
- APK 使用调试密钥签名：覆盖安装旧版本始终可行（签名相同），但不适合
  通过应用商店分发。
- 没有自动化测试套件：每次改动都通过全新编译加真机手动测试来验证。

## 从源码构建

需要 Android NDK r27、Android SDK（platform 34, build-tools 34.0.0）、
Gradle 8.7+、JDK 17+。

```bash
# 1. 使用 NDK 为 arm64-v8a 交叉编译 zstd、lz4、OpenSSL、curl。
#    XGDTool/android/CMakeLists.txt 默认期望所有依赖都位于
#    ~/android/install-arm64（可用 -DXGD_DEPS_PREFIX 覆盖）。

# 2. 构建原生库
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
# <host> 根据系统不同为 linux-x86_64、windows-x86_64 或 darwin-x86_64

# 3. 构建 APK
cd ../../../XGDToolAndroid
gradle clean assembleDebug
# APK 位于 app/build/outputs/apk/debug/app-debug.apk
```

注意：`XGDTool/cmake/embed_resources.cmake` 会在首次配置 CMake 时，从
`external/Repackinator` 子模块中包含的二进制文件生成
`XGDTool/src/Executable/AttachXbe.h` —— 请勿手动修改该文件，它会自动重
新生成。

## 许可与致谢

XGDTool 核心及本移植版均采用 **GPL-3.0** 协议发布 —— 详见仓库根目录的
[LICENSE](../LICENSE)。C++ 核心中还集成了第三方组件，列于
[ATTRIBUTION.md](../XGDTool/ATTRIBUTION.md)。

上游项目：[wiredopposite/XGDTool](https://github.com/wiredopposite/XGDTool)。
