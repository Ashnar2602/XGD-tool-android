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

# XGDTool Android 版

[XGDTool](https://github.com/wiredopposite/XGDTool)（GPL-3.0 协议）的非官方
Android 移植版：直接在手机上将 Xbox / Xbox 360 光盘镜像（ISO、精简版
XISO）转换为 **ZAR**、**GOD**、**CCI**、**CSO** 格式，无需借助电脑即可
备份你的实体游戏收藏。

<p align="center">
  <img src="docs/assets/screenshot_it.png" alt="应用主界面" width="280">
</p>

## 功能特点

- 支持 XISO、ZAR、GOD、CCI、CSO 之间的转换 —— 与 XGDTool 桌面版相同的
  C++ 转换引擎。
- **批量转换**：一次选择多个文件，系统会自动按队列依次处理。
- 可选的在线游戏标题自动查询，用于为输出文件生成可读性更好的名称。
- 无需侵入性存储权限：一切都通过 Android 的 Storage Access Framework
  完成 —— 由你自己决定哪些文件夹对应用可见。
- 界面支持 7 种语言（根据手机系统语言自动识别）：意大利语、英语、法语、
  德语、西班牙语、葡萄牙语、简体中文。
- 以前台服务方式运行：转换过程中可以退出应用而不会中断任务。

## 系统要求

- Android 8.0（API 26）或更高版本，**arm64-v8a** 架构。
- 可用存储空间至少为你收藏中最大游戏文件大小的 2 倍。

## 安装

前往本仓库的 [Releases](../../releases) 页面，下载最新的
`XGDTool-android-debug.apk` 并安装到手机上（需要开启"安装未知应用"权限）。
完整使用指南和故障排除请参见 [docs/MANUAL.zh-CN.md](docs/MANUAL.zh-CN.md)。

## 从源码构建

需要 Android NDK r27、Android SDK（platform 34）、Gradle 8.7+、JDK 17+。
完整说明见
[docs/MANUAL.zh-CN.md](docs/MANUAL.zh-CN.md#从源码构建)。

## 免责声明

本项目为个人爱好项目，与 Microsoft 无任何关联，亦未获其认可或赞助。
"Xbox" 是其各自所有者的注册商标，此处仅作描述性使用。本工具旨在为合法
拥有的光盘进行个人备份。

## 许可协议

XGDTool 核心采用 GPL-3.0 协议 —— 详见 [LICENSE](LICENSE)。核心中集成的
第三方组件列于
[XGDTool/ATTRIBUTION.md](XGDTool/ATTRIBUTION.md)。若公开分享修改版本，
根据 GPL-3.0 协议要求，也必须公开提供修改后的源代码。
