#!/bin/bash

# Checkstand Model Deployment Script
# This script helps deploy the Gemma model to your Android device

MODEL_FILE="gemma-3n-E4B-it-int4.task"
DEVICE_PATH="/data/local/tmp/llm/gemma-3n-E4B-it-int4.task"

echo "🚀 Checkstand Model Deployment"
echo "==============================="

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK platform tools."
    exit 1
fi

# Check if device is connected
DEVICES=$(adb devices | grep -v "List of devices attached" | grep "device" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No Android device connected."
    echo "   Please connect your device and enable USB debugging."
    exit 1
fi

echo "✅ Device connected: $(adb devices | grep device | cut -f1)"

# Check if model file exists locally
if [ ! -f "$MODEL_FILE" ]; then
    echo "❌ Model file '$MODEL_FILE' not found in current directory."
    echo "   Please place your model file here first."
    exit 1
fi

MODEL_SIZE=$(du -h "$MODEL_FILE" | cut -f1)
echo "📦 Model file found: $MODEL_FILE ($MODEL_SIZE)"

# Create directory on device
echo "📁 Creating directory on device..."
adb shell mkdir -p /data/local/tmp/llm/

# Push model to device
echo "📲 Pushing model to device (this may take a while)..."
adb push "$MODEL_FILE" "$DEVICE_PATH"

if [ $? -eq 0 ]; then
    echo "✅ Model deployed successfully!"
    echo "📊 Verifying deployment..."
    
    # Verify the file exists and get its size
    DEVICE_SIZE=$(adb shell ls -lh "$DEVICE_PATH" | awk '{print $5}')
    echo "   Device file size: $DEVICE_SIZE"
    
    echo ""
    echo "🎉 Deployment complete! You can now:"
    echo "   1. Build and install the app: ./gradlew installDebug"
    echo "   2. Launch the app on your device"
    echo "   3. The app should detect the model automatically"
    echo ""
else
    echo "❌ Failed to push model to device."
    exit 1
fi
