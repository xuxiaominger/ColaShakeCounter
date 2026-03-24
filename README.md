# 摇动可乐计数器 (Cola Shake Counter)

一个简单有趣的 Android 应用 - 摇动手机，计数器加一，可乐晃动！

## 功能特点

- 🥤 精美的玻璃杯可乐动画（含气泡、冰块、泡沫）
- 📱 摇动手机检测（使用加速度传感器）
- 🔢 右上角白色数字计数器
- 📳 摇动时震动反馈
- 🌊 液面波浪动画效果

## 截图效果

- 黑色全屏背景
- 透明玻璃杯（带高光效果）
- 深棕色可乐饮料
- 持续上浮的气泡
- 浮动的冰块
- 液面波浪动画

## 技术细节

- **最低 SDK**: Android 7.0 (API 24)
- **目标 SDK**: Android 14 (API 34)
- **开发语言**: Java
- **动画实现**: Canvas 自定义绘制 + ValueAnimator
- **传感器**: TYPE_ACCELEROMETER

## 构建方式

```bash
# 克隆项目
git clone https://github.com/YOUR_USERNAME/ColaShakeCounter.git
cd ColaShakeCounter

# 构建 Debug APK
./gradlew assembleDebug

# APK 输出位置
# app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```
app/src/main/
├── java/com/colashake/counter/
│   ├── MainActivity.java      # 主活动，处理传感器和计数
│   ├── ColaGlassView.java     # 自定义视图，绘制可乐动画
│   └── ShakeDetector.java     # 摇动检测器
├── res/
│   ├── layout/activity_main.xml
│   └── values/strings.xml
└── AndroidManifest.xml
```

## 许可证

MIT License
