<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="160" alt="Checkstand Logo" />
</p>

# Checkstand - AI-Powered Receipt Scanner

> **Google Gemma 3n Impact Challenge Submission**  
> *"Think bigger than a simple chatbot"* - Financial empowerment through private, on-device receipt intelligence.

[![Gemma 3n](https://img.shields.io/badge/Powered%20by-Gemma%203n-blue)](https://github.com/google/gemma)
[![MediaPipe](https://img.shields.io/badge/AI-MediaPipe-green)](https://mediapipe.dev/)
[![Privacy First](https://img.shields.io/badge/Trust-Uncompromising%20Privacy-red)](https://github.com)

🌐 **[Visit Live Landing Page](https://gryphon2411.github.io/Checkstand/)**

## 🎯 The Problem: Financial Data Black Holes

In today's economy, tracking expenses is crucial. Yet, our most frequent purchases—from supermarkets, cafes, and local shops—often end up as paper receipts, creating a black hole in our financial data. With **~60% of Americans lacking a formal budget** and **18% of all payments still made in cash**, a huge volume of financial data is lost. How can people control their spending if they can't track it?

**Checkstand's Answer:** Empower users with a tool that digitizes physical receipts, providing crucial financial insights that were previously lost.

## ✨ What Makes Checkstand Special

- **💰 Financial Empowerment**: Finally track spending from supermarkets and local shops that don't offer digital records.
- **🎓 Educational Insights**: Understand where your money goes, identify spending patterns, and take control of your budget.
- **🔒 Uncompromising Privacy**: Your financial data is sensitive. Checkstand processes everything on-device, so your information never leaves your phone. It's your data, your insights, your control.
- **🧠 Multimodal AI**: A powerful on-device pipeline combining Camera, OCR, and Gemma 3n to understand any receipt.
- **📱 Offline-First**: Works anywhere, anytime, without needing an internet connection.
- **⚡ Real-Time**: Process receipts in ~24 seconds on consumer hardware
- **🎯 Practical Impact**: Solves real financial tracking problems

## 🏗️ **Technical Innovation**

### **Gemma 3n Integration**
- **On-Device Inference**: 4.4GB E4B model running locally via MediaPipe
- **Session Management**: Fresh inference sessions per request (prevents context contamination)
- **Multimodal Pipeline**: Seamless integration of image and text processing

### **Architecture Highlights**
- **Clean Architecture**: Domain/Data/UI separation with Hilt dependency injection
- **Robust Parsing**: Smart fallback mechanisms when LLM output varies
- **Camera Integration**: CameraX with real-time preview and capture
- **Modern Android**: Jetpack Compose UI with Material 3 design

### **Why Native Android?**
*Development Journey Note*: We initially explored React Native for cross-platform efficiency, but encountered Metro bundler's 2GB file size limitation with our 4.4GB Gemma 3n model. While we successfully created a custom native module workaround, we ultimately chose pure Android native for optimal performance and simplified architecture. This decision exemplifies choosing the right tool for AI deployment constraints.

## 🎬 **See It In Action**

[🎥 **Demo Video**](https://www.youtube.com/watch?v=Pb0Qe0wS8Pk)

### ✨ **Latest Updates**
