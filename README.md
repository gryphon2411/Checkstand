# Checkstand - On-Device AI Assistant

A privacy-focused Android application that demonstrates on-device Large Language Model (LLM) inference using Google's MediaPipe framework.

## Features

- **Privacy-First**: All AI processing happens on your device
- **Offline Capable**: Works without internet connection after model download
- **No Data Sharing**: Your conversations never leave your device
- **Efficient**: Optimized for mobile devices using MediaPipe
- **Simple UI**: Clean, intuitive chat interface

## Architecture

- **Native Android**: Built with Kotlin and Jetpack Compose
- **MediaPipe LLM**: Uses Google's MediaPipe framework for inference
- **MVVM Pattern**: Clean architecture with ViewModels
- **Coroutines**: Asynchronous operations for smooth UX

## Technical Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM
- **AI Framework**: MediaPipe LLM Inference
- **Model**: Gemma 2B (int4 quantized)

## Requirements

- Android API 26+ (Android 8.0)
- ~4.4GB free storage on device for model
- ADB access to push model to device
- Gemma-3n E4B model file (gemma-3n-E4B-it-int4.task)

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
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
3. **Start chatting** with the AI assistant
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
- ✅ Conversations stored locally only
- ✅ No telemetry or analytics
- ✅ No internet required after setup

## Development Notes

This app demonstrates:
- Integration with MediaPipe LLM inference
- Efficient model management
- Clean Android architecture patterns
- Privacy-preserving AI implementation

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

MIT License - see LICENSE file for details

## Acknowledgments

- Google MediaPipe team for the LLM inference framework
- Google for the Gemma model
- Android and Jetpack Compose teams
