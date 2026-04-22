# Azure AI Speech Transcription Samples

This directory contains runnable code samples that demonstrate how to use the Azure AI Speech Transcription client library for Java.

## Prerequisites

To run these samples, you need:

1. **Azure Subscription**: An active Azure subscription
2. **Azure AI Speech Service Resource**: Create one in the [Azure Portal](https://portal.azure.com)
3. **Authentication**: Choose one of the following authentication methods:

### Option 1: Entra ID Authentication (Recommended for Production)

   Set the endpoint and configure Entra ID credentials:
   
   ```bash
   set SPEECH_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
   ```
   
   **And** configure one of the following credential sources:
   - **Managed Identity**: For apps running in Azure (App Service, Azure Functions, VMs, etc.)
   - **Azure CLI**: Run `az login` on your development machine
   - **Environment Variables**: Set `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, and `AZURE_CLIENT_SECRET`
   - **Visual Studio Code or IntelliJ**: Sign in through your IDE
   
   **Note**: You'll also need to assign the "Cognitive Services User" role to your identity:
   
   ```bash
   az role assignment create --assignee <your-identity> \
       --role "Cognitive Services User" \
       --scope /subscriptions/<subscription-id>/resourceGroups/<resource-group>/providers/Microsoft.CognitiveServices/accounts/<speech-resource-name>
   ```
   
   **Required dependency** for Entra ID authentication:
   
   ```xml
   <dependency>
       <groupId>com.azure</groupId>
       <artifactId>azure-identity</artifactId>
       <version>1.13.0</version>
   </dependency>
   ```

   ### Option 2: API Key Authentication (Easier for Getting Started)
   
   Set these environment variables:
   
   ```bash
   set SPEECH_ENDPOINT=https://your-resource-name.cognitiveservices.azure.com/
   ```
   
   **And** configure one of the following credential sources:
   - **Managed Identity**: For apps running in Azure (App Service, Azure Functions, VMs, etc.)
   - **Azure CLI**: Run `az login` on your development machine
   - **Environment Variables**: Set `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, and `AZURE_CLIENT_SECRET`
   - **Visual Studio Code or IntelliJ**: Sign in through your IDE
   
   **Note**: You'll also need to assign the "Cognitive Services User" role to your identity:
   
   ```bash
   az role assignment create --assignee <your-identity> \
       --role "Cognitive Services User" \
       --scope /subscriptions/<subscription-id>/resourceGroups/<resource-group>/providers/Microsoft.CognitiveServices/accounts/<speech-resource-name>
   ```
   
   **Required dependency** for Entra ID authentication:
   
   ```xml
   <dependency>
       <groupId>com.azure</groupId>
       <artifactId>azure-identity</artifactId>
       <version>1.13.0</version>
   </dependency>
   ```

4. **Audio File**: Some samples require an audio file named `sample-audio.wav` in the working directory

## Authentication Methods

All samples in this directory support **both authentication methods**:

- **Entra ID (TokenCredential)**: Uses `DefaultAzureCredential` from azure-identity
- **API Key (KeyCredential)**: Uses the `SPEECH_API_KEY` environment variable

The samples will automatically detect which authentication method to use based on the environment variables you've set. If `SPEECH_API_KEY` is set, it will use API Key authentication; otherwise, it will attempt Entra ID authentication.

## Available Samples

### TranscribeAudioFileSample.java

**Champion scenario**: Basic audio transcription from a file

Demonstrates the most common use case - transcribing a single audio file with minimal configuration.

**Key features**:

- Creating a TranscriptionClient
- Reading an audio file
- Transcribing with default options
- Processing results

**Run**:

```bash
cd sdk/transcription/azure-ai-speech-transcription
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.TranscribeAudioFileSample"
```

---

### TranscribeFromUrlSample.java

**Champion scenario**: Transcribe audio from a URL

Demonstrates how to transcribe audio directly from a URL without downloading the file locally.

**Key features**:

- Creating TranscriptionOptions with a URL
- Transcribing remote audio files

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.TranscribeFromUrlSample"
```

---

### TranscribeMultiLanguageSample.java

**Champion scenario**: Multi-language transcription

Demonstrates how to transcribe audio containing multiple languages with automatic language detection.

**Key features**:

- Automatic language detection
- Handling multi-language results

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.TranscribeMultiLanguageSample"
```

---

### EnhancedModeSample.java

**Champion scenario**: Enhanced transcription quality

Demonstrates how to use enhanced mode with custom prompts and other advanced features.

**Key features**:

- Using EnhancedModeOptions
- Providing custom prompts for better accuracy
- Specifying task types

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.EnhancedModeSample"
```

---

### TranscribeWithDiarizationSample.java

**Champion scenario**: Speaker diarization

Demonstrates how to identify different speakers in the audio.

**Key features**:

- Enabling speaker diarization
- Configuring max speakers
- Processing speaker-separated results

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.TranscribeWithDiarizationSample"
```

---

### TranscribeWithPhraseListSample.java

**Champion scenario**: Improving accuracy with phrase lists

Demonstrates how to use a phrase list to improve recognition of specific terms.

**Key features**:

- Creating a PhraseListOptions
- Adding custom phrases and boosting their probability
- Improving accuracy for domain-specific terminology

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.TranscribeWithPhraseListSample"
```

---

### TranscribeWithProfanityFilterSample.java

**Champion scenario**: Profanity filtering

Demonstrates how to configure profanity filtering options.

**Key features**:

- Setting ProfanityFilterMode (Masked, Removed, None)
- Handling filtered results

**Run**:

```bash
mvn exec:java -Dexec.mainClass="com.azure.ai.speech.transcription.TranscribeWithProfanityFilterSample"
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
- [SDK README](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/transcription/azure-ai-speech-transcription)
- [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues)

## Additional Resources

- [Azure SDK for Java Guidelines](https://azure.github.io/azure-sdk/java_introduction.html)
- [Project Reactor Documentation](https://projectreactor.io/docs)
- [Azure SDK Blog](https://devblogs.microsoft.com/azure-sdk/)
