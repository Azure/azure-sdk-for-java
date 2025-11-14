# Azure AI Speech Transcription Samples

This directory contains runnable code samples that demonstrate how to use the Azure AI Speech Transcription client library for Java.

## Prerequisites

To run these samples, you need:

1. **Azure Subscription**: An active Azure subscription
2. **Azure AI Speech Service Resource**: Create one in the [Azure Portal](https://portal.azure.com)
3. **Authentication**: Choose one of the following authentication methods:

   ### Option 1: Azure AD Authentication (Recommended for Production)
   
   Set the endpoint and configure Azure AD credentials:
   
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
   
   **Required dependency** for Azure AD authentication:
   
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
   
   **Required dependency** for Azure AD authentication:
   
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

- **Azure AD (TokenCredential)**: Uses `DefaultAzureCredential` from azure-identity
- **API Key (KeyCredential)**: Uses the `SPEECH_API_KEY` environment variable

The samples will automatically detect which authentication method to use based on the environment variables you've set. If `SPEECH_API_KEY` is set, it will use API Key authentication; otherwise, it will attempt Azure AD authentication.

## Available Samples

### BasicTranscriptionSample.java

**Champion scenario**: Simple audio transcription

Demonstrates the most common use case - transcribing a single audio file with minimal configuration.

**Key features**:

- Creating a TranscriptionClient with API Key or Azure AD authentication
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
- Both API Key and Azure AD authentication examples
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

- Creating a TranscriptionAsyncClient with both authentication methods
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

- API Key authentication error handling
- Azure AD authentication error handling (token expiration, missing credentials)
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
- [SDK README](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cognitiveservices/azure-ai-speech-transcription)
- [GitHub Issues](https://github.com/Azure/azure-sdk-for-java/issues)

## Additional Resources

- [Azure SDK for Java Guidelines](https://azure.github.io/azure-sdk/java_introduction.html)
- [Project Reactor Documentation](https://projectreactor.io/docs)
- [Azure SDK Blog](https://devblogs.microsoft.com/azure-sdk/)
