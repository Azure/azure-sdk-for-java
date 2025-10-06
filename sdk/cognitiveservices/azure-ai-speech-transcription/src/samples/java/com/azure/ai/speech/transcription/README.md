# Azure AI Speech Transcription Samples

This directory contains runnable code samples that demonstrate how to use the Azure AI Speech Transcription client library for Java.

## Prerequisites

To run these samples, you need:

1. **Azure Subscription**: An active Azure subscription
2. **Azure AI Speech Service Resource**: Create one in the [Azure Portal](https://portal.azure.com)
3. **Environment Variables**: Set the following environment variables:

   ```bash
   set SPEECH_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
   set SPEECH_API_KEY=your-api-key
   ```

4. **Audio File**: Some samples require an audio file named `sample-audio.wav` in the working directory

## Available Samples

### BasicTranscriptionSample.java

**Champion scenario**: Simple audio transcription

Demonstrates the most common use case - transcribing a single audio file with minimal configuration.

**Key features**:

- Creating a TranscriptionClient
- Reading an audio file
- Transcribing with default options
- Processing results

**Run**:

```bash
cd sdk/cognitiveservices/azure-ai-speech-transcription
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.BasicTranscriptionSample"
```

---

### AdvancedTranscriptionSample.java

**Champion scenario**: Complex transcription with custom configuration

Shows how to use advanced features for production scenarios.

**Key features**:

- Custom client configuration (logging, retry policies)
- Advanced transcription options (locale, profanity filtering)
- Speaker diarization (identifying different speakers)
- Detailed result processing with word-level timings

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.AdvancedTranscriptionSample"
```

---

### AsyncTranscriptionSample.java

**Champion scenario**: Asynchronous transcription with reactive programming

Demonstrates non-blocking operations using TranscriptionAsyncClient and Project Reactor.

**Key features**:

- Creating a TranscriptionAsyncClient
- Using reactive patterns (subscribe, block, timeout)
- Error handling in async operations
- Different async patterns for various use cases

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.AsyncTranscriptionSample"
```

---

### ErrorHandlingSample.java

**Champion scenario**: Robust error handling

Shows how to properly handle errors and exceptions in production code.

**Key features**:

- Authentication error handling
- File I/O error handling
- Service error handling (rate limiting, server errors)
- Retry logic with exponential backoff
- Comprehensive error handling pattern

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.ErrorHandlingSample"
```

---

### ReadmeSamples.java

Code snippets used in the main README.md and API documentation (JavaDoc).

**Note**: This file is used by the `codesnippet-maven-plugin` to inject code into documentation. It's not meant to be run directly.

## Supported Audio Formats

The service supports various audio formats:

- **WAV** (recommended: 16 kHz, 16-bit, mono PCM)
- **MP3**
- **OGG**
- **FLAC**
- And more

**Constraints**:

- Maximum file size: 250 MB
- Maximum duration: 2 hours

## Getting Help

- [Azure AI Speech Documentation](https://learn.microsoft.com/azure/ai-services/speech-service/)
- [SDK Documentation](https://learn.microsoft.com/java/api/overview/azure/ai-speech-transcription-readme)
- [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues)

## Additional Resources

- [Azure SDK for Java Guidelines](https://azure.github.io/azure-sdk/java_introduction.html)
- [Project Reactor Documentation](https://projectreactor.io/docs)
- [Azure SDK Blog](https://devblogs.microsoft.com/azure-sdk/)
