# Technical Architecture

## System Overview

Checkstand implements a sophisticated multimodal AI pipeline that processes receipt images entirely on-device using Google's Gemma 3n model via MediaPipe framework.

## Architecture Layers

### 1. Presentation Layer (UI)
- **Framework**: Jetpack Compose with Material 3 design
- **Key Components**: 
  - `InvoiceCaptureScreen`: Main receipt scanning interface
  - `CameraService`: CameraX integration for image capture
  - `ModelStatusService`: Real-time model loading status

### 2. Domain Layer (Business Logic)
- **Use Cases**: 
  - `ProcessReceiptUseCase`: Orchestrates receipt analysis workflow
- **Models**: 
  - `Receipt`: Core data model with merchant, date, total, items
- **Repositories**: Abstract interfaces for data access

### 3. Data Layer (Implementation)
- **Repositories**: 
  - `MediaPipeLLMRepository`: Implements receipt processing using LLM + OCR
- **Services**:
  - `LLMService`: Manages Gemma 3n model lifecycle and inference
  - `OCRService`: Google ML Kit text recognition
- **Database**: Room database for local receipt storage

## Key Technical Innovations

### 1. Session Management
**Problem**: MediaPipe LLM sessions accumulate context, causing contamination between different receipts.

**Solution**: Create fresh `LlmInferenceSession` for each receipt analysis:
```kotlin
suspend fun generateResponse(prompt: String): String {
    return LlmInference.createFromOptions(context, options).use { inference ->
        inference.generateResponse(prompt)
    }
}
```

### 2. Robust Parsing with Fallbacks
**Problem**: LLM output format can vary, breaking strict parsing.

**Solution**: Multi-tier parsing strategy:
1. **Primary**: Structured format parsing (`MERCHANT:`, `DATE:`, `TOTAL:`)
2. **Fallback**: Regex-based amount extraction
3. **Graceful degradation**: Partial data with user notification

### 3. Multimodal Pipeline Optimization
**Problem**: Optimal ordering of text and image inputs for MediaPipe.

**Solution**: Text-first prompt structure followed by image analysis:
```kotlin
// 1. OCR extracts text from image
val extractedText = ocrService.extractTextFromImage(bitmap)

// 2. LLM analyzes text with structured prompt
val prompt = """
Analyze this receipt text and extract information:
$extractedText

Format: MERCHANT: [name] | DATE: [date] | TOTAL: [amount]
"""

// 3. Parse structured response with fallbacks
val receipt = parseReceiptFromResponse(llmResponse)
```

## Performance Characteristics

### Model Loading
- **Time**: 1-2 seconds on modern Android devices
- **Memory**: ~3.1GB for Gemma 3n E2B model
- **Optimization**: Lazy loading with progress indicators

### Receipt Processing
- **OCR Phase**: ~2-3 seconds (Google ML Kit)
- **LLM Phase**: ~20-22 seconds (Gemma 3n inference)
- **Total**: ~24 seconds average end-to-end
- **Accuracy**: High robustness across receipt formats

### Memory Management
- **Session Lifecycle**: Created per request, auto-disposed
- **Image Processing**: Efficient bitmap handling with rotation
- **Database**: SQLite with Room for local storage

## Security and Privacy

### On-Device Processing
- **Zero Network**: All inference happens locally
- **No Data Transmission**: Receipt data never leaves device
- **Offline Capable**: Works without internet after model download

### Data Protection
- **Local Storage**: SQLite database with app-specific access
- **Memory Safety**: Automatic cleanup of sensitive image data
- **No Analytics**: No telemetry or usage tracking

## Error Handling

### Graceful Degradation
1. **Model Loading Failures**: User-friendly error messages with retry
2. **OCR Failures**: Fallback to manual entry suggestion
3. **LLM Parsing Failures**: Partial data extraction with user confirmation
4. **Camera Issues**: Gallery alternative always available

### User Experience
- **Progress Indicators**: Real-time feedback during processing
- **Status Management**: Clear communication of app state
- **Error Recovery**: Multiple pathways for successful completion

## Scalability Considerations

### Device Compatibility
- **Minimum**: Android 7.0 (API 24), 2GB RAM
- **Recommended**: Android 10+, 4GB+ RAM
- **Testing**: Validated on Pixel, Samsung Galaxy series

### Future Enhancements
- **Multi-language**: OCR and LLM support for international receipts
- **Batch Processing**: Multiple receipt analysis in sequence
- **Export Features**: PDF reports, CSV data export
- **Voice Interface**: Accessibility features for visually impaired users

## Development Workflow

### Build Process
```bash
./gradlew assembleDebug      # Debug build
./gradlew assembleRelease    # Production build
./gradlew test              # Unit tests
./gradlew connectedAndroidTest  # Integration tests
```

### Testing Strategy
- **Unit Tests**: Business logic and parsing algorithms
- **Integration Tests**: OCR and LLM service interactions
- **UI Tests**: Receipt scanning workflow end-to-end
- **Performance Tests**: Memory usage and processing time benchmarks
