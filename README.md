# VirtualInstrument

这是一个旨在通过多种连接方式（包括蓝牙、Wi-Fi以及USB）与虚拟示波器进行交互的安卓应用程序。

## 功能特性

- **多模式连接**：支持蓝牙、Wi-Fi和USB连接。
- **实时数据可视化**：提供实时数据采集和展示功能，帮助用户监控信号变化。
- **灵活配置**：允许用户自定义设置，以适应不同的测试环境和需求。
- **跨平台兼容性**：优化设计以确保在各种安卓设备上的兼容性和稳定性。

## 快速开始

### 环境搭建

1. **安装Android Studio**：访问[Android Studio官网](https://developer.android.com/studio)下载并安装最新版本。
2. **克隆项目**：
   git clone https://github.com/Anliaita-Losviser/VirtualInstrument.git

### 构建和运行

1. 打开Android Studio，选择“Open an existing Android Studio project”，然后导航到项目根目录。
2. 在工具栏中选择你的安卓设备或启动一个模拟器。
3. 点击“Run”按钮来构建并运行应用。

## 使用说明

- **连接设备**：进入主界面后，选择连接方式（蓝牙/Wi-Fi/USB），按照提示完成设备配对和连接。
- **数据采集与分析**：连接成功后，点击开始按钮进行数据采集，并查看实时数据图表。
- **保存数据**：支持将采集的数据导出为CSV文件，便于后续分析。

## 技术栈

- **编程语言**：Kotlin / Java
- **开发工具**：Android Studio
- **依赖库**：xxpermissions, com.github.mik3y:usb-serial-for-android:3.8.0

## 许可证

本项目基于MIT许可证发布。详情请参阅[LICENSE](LICENSE)文件。