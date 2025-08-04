# Checkstand - AI-Powered Receipt Scanner

> **Google Gemma 3n Impact Challenge Submission**  
> *"Think bigger than a simple chatbot"* - Financial empowerment through private, on-device receipt intelligence.

[![Gemma 3n](https://img.shields.io/badge/Powered%20by-Gemma%203n-blue)](https://github.com/google/gemma)
[![MediaPipe](https://img.shields.io/badge/AI-MediaPipe-green)](https://mediapipe.dev/)
[![Privacy First](https://img.shields.io/badge/Trust-Uncompromising%20Privacy-red)](https://github.com)

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
- **Material Design Icons**: Professional vector drawables replace emoji icons
- **Clean UX**: Removed confusing processing notifications  
- **Background Processing**: Queue system with timeout handling
- **Visual Status Indicators**: Clear receipt processing states

## 🚀 **Quick Start**

### **Prerequisites**
- Android Studio Hedgehog or newer
- Android device with API level 24+ (Android 7.0)
- 4GB+ RAM (recommended for optimal model performance)

### **Build & Run**
```bash
git clone https://github.com/gryphon2411/Checkstand.git
cd Checkstand
./gradlew assembleDebug
```

### **Installation**
1. Enable "Unknown Sources" in Android settings
2. Install the generated APK: `app/build/outputs/apk/debug/app-debug.apk`
3. Grant camera permission when prompted
4. Start scanning receipts!

## 🧠 **How It Works**

1. **Image Capture**: User scans receipt with camera or selects from gallery
2. **OCR Processing**: Google ML Kit extracts text from receipt image
3. **AI Analysis**: Gemma 3n processes text to identify merchant, date, total
4. **Smart Parsing**: Robust extraction with intelligent fallbacks
5. **Local Storage**: All data stays on device in local database

## 📊 **Quantified Impact & Market Size**

Checkstand addresses a massive, underserved market by providing a private, educational tool for financial empowerment.

- **Budget-Conscious Households (~79M in U.S.)**: A simple, effective tool for the 60% of U.S. households that do not have a formal budget.
- **Freelancers & Gig Workers (~64M in U.S.)**: Meticulous, offline-first expense tracking for the 38% of the workforce that needs it for tax purposes and managing variable income.
- **The Cash-Reliant Population (~36M in U.S.)**: Serves the ~14% of U.S. adults who use cash for most purchases and are excluded from mainstream digital finance apps.
- **Privacy-Aware Users**: In a market where **~79% of consumers are concerned about data privacy**, Checkstand's on-device AI is a critical differentiator that builds essential trust.

## 🏆 **Competition Alignment**

**Google AI Edge Prize Target**: *"Most compelling and effective use case built using Google AI Edge implementation of Gemma 3n"*

✅ **Compelling Use Case**: Universal need for receipt management  
✅ **Effective Implementation**: Production-ready Android application  
✅ **Google AI Edge**: MediaPipe framework with Gemma 3n model  
✅ **Real-World Impact**: Solves genuine user problems with privacy focus  

## 🔧 **Technical Deep Dive**

### **Project Structure**
```
app/src/main/java/com/checkstand/
├── domain/          # Business logic and use cases
├── data/            # Repositories and data sources  
├── service/         # LLM, OCR, and Camera services
├── ui/              # Jetpack Compose screens and components
└── utils/           # Helper utilities and extensions
```

### **Key Innovations**
- **Session Management**: Prevents LLM context contamination between receipts
- **Fallback Parsing**: Regex-based extraction when structured parsing fails
- **Multimodal Workflow**: Optimized text-before-image prompt ordering
- **Error Resilience**: Graceful degradation with user-friendly error handling

## 🏅 **Performance Metrics**

- **Model Loading**: ~1-2 seconds on modern devices
- **Receipt Processing**: ~24 seconds average (includes OCR + LLM)
- **Memory Usage**: Efficient with 4.4GB model size
- **Accuracy**: Robust extraction across various receipt formats

## Technical Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM
- **AI Framework**: MediaPipe LLM Inference
- **Model**: Gemma 3n E4B (int4 quantized)

## Requirements

- Android API 26+ (Android 8.0)
- ~4.4GB free storage on device for model
- ADB access to push model to device
- Gemma-3n E4B model file (gemma-3n-E4B-it-int4.task)

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/gryphon2411/Checkstand.git
   cd Checkstand
   ```

2. **Open in Android Studio**
   - Open the project in Android Studio
   - Wait for Gradle sync to complete

3. **Push the Model to Device**
   
   **Important**: You need to push your `gemma-3n-E4B-it-int4.task` file to the device first:
   
   ```bash
   # Connect your device and enable USB debugging
   adb devices
   
   # Create the directory on device
   adb shell mkdir -p /data/local/tmp/llm/
   
   # Push your model file to the device
   adb push /path/to/your/gemma-3n-E4B-it-int4.task /data/local/tmp/llm/gemma-3n-E4B-it-int4.task
   
   # Verify the file was pushed correctly
   adb shell ls -la /data/local/tmp/llm/
   ```

4. **Build and Run**
   - Build the project
   - Run on device (API 26+)
   - The app should detect the model automatically

## Usage

1. **Launch the app**
2. **Complete initial setup** (model should be detected if pushed correctly)
3. **Start scanning receipts** with the camera or gallery
4. **Enjoy privacy-focused AI** - all processing happens on your device

## Model Information

- **Model**: Gemma-3n E4B Instruct (int4 quantized)
- **Size**: ~4.4GB
- **Optimization**: GPU acceleration where available
- **Provider**: Google
- **Supports**: Multimodal (text + image) prompting

## Privacy & Security

- ✅ All inference runs on-device
- ✅ No data sent to external servers
- ✅ Receipt data stored locally only
- ✅ No telemetry or analytics
- ✅ No internet required after setup

## Development Notes

This app demonstrates:
- Integration with MediaPipe LLM inference for receipt analysis
- Efficient model management with on-device AI
- Clean Android architecture patterns
- Privacy-preserving multimodal AI implementation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

CC BY 4.0 License - see [LICENSE](https://github.com/gryphon2411/Checkstand/blob/master/LICENSE)

## Acknowledgments

- Google MediaPipe team for the LLM inference framework
- Google for the Gemma model
- Android and Jetpack Compose teams
