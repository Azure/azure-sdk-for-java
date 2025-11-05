# Azure AI Speech Transcription client library for Java

The Azure AI Speech Transcription client library provides a simple and efficient way to convert audio to text using Azure Cognitive Services. This library enables you to transcribe audio files with features like speaker diarization, profanity filtering, and phrase hints for improved accuracy.

## Documentation

Various documentation is available to help you get started:

- [API reference documentation][docs]
- [Product documentation][product_documentation]
- [Azure Speech Service documentation](https://learn.microsoft.com/azure/ai-services/speech-service/)

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An [Azure Speech resource](https://learn.microsoft.com/azure/ai-services/speech-service/overview#try-the-speech-service-for-free) or [Cognitive Services multi-service resource](https://learn.microsoft.com/azure/ai-services/multi-service-resource)

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-speech-transcription;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-speech-transcription</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

#### Optional: For Azure AD Authentication

If you plan to use Azure AD authentication (recommended for production), also add the `azure-identity` dependency:

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.18.1</version>
</dependency>
```

### Authentication

Azure Speech Transcription supports two authentication methods:

#### Option 1: API Key Authentication (Subscription Key)

You can find your Speech resource's API key in the [Azure Portal](https://portal.azure.com) or by using the Azure CLI:

```bash
az cognitiveservices account keys list --name <your-resource-name> --resource-group <your-resource-group>
```

Once you have an API key, you can authenticate using `KeyCredential`:

```java
import com.azure.core.credential.KeyCredential;

TranscriptionClient client = new TranscriptionClientBuilder()
    .endpoint("https://<your-resource-name>.cognitiveservices.azure.com/")
    .credential(new KeyCredential("<your-api-key>"))
    .buildClient();
```

#### Option 2: Azure AD OAuth2 Authentication (Recommended for Production)

For production scenarios, it's recommended to use Azure Active Directory (Azure AD) authentication with managed identities or service principals. This provides better security and easier credential management.

The OAuth2 scope for Azure Cognitive Services is: `https://cognitiveservices.azure.com/.default`

```java
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

// Use DefaultAzureCredential which works with managed identities, service principals, Azure CLI, etc.
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();

TranscriptionClient client = new TranscriptionClientBuilder()
    .endpoint("https://<your-resource-name>.cognitiveservices.azure.com/")
    .credential(credential)
    .buildClient();
```

**Note:** To use Azure AD authentication, you need to:
1. Add the `azure-identity` dependency to your project
2. Assign the appropriate role (e.g., "Cognitive Services User") to your managed identity or service principal
3. Ensure your Cognitive Services resource has Azure AD authentication enabled

For more information on Azure AD authentication, see:
- [Authenticate with Azure Identity](https://learn.microsoft.com/azure/developer/java/sdk/identity)
- [Azure Cognitive Services authentication](https://learn.microsoft.com/azure/ai-services/authentication)

## Key concepts

### TranscriptionClient

The `TranscriptionClient` is the primary interface for interacting with the Speech Transcription service. It provides methods to transcribe audio files to text.

### TranscriptionAsyncClient

The `TranscriptionAsyncClient` provides asynchronous methods for transcribing audio, allowing non-blocking operations that return reactive types.

### Audio Formats

The service supports various audio formats including WAV, MP3, OGG, and more. Audio files must be:

- Shorter than 2 hours in duration
- Smaller than 250 MB in size

### Transcription Options

You can customize transcription with options like:

- **Profanity filtering**: Control how profanity is handled in transcriptions
- **Speaker diarization**: Identify different speakers in multi-speaker audio
- **Phrase lists**: Provide domain-specific phrases to improve accuracy
- **Language detection**: Automatically detect the spoken language
- **Enhanced mode**: Improve transcription quality with custom prompts, translation, and task-specific configurations

## Examples

### Transcribe an audio file

```java com.azure.ai.speech.transcription.readme
TranscriptionClient client = new TranscriptionClientBuilder()
    .endpoint("https://<your-resource-name>.cognitiveservices.azure.com/")
    .credential(new KeyCredential("<your-api-key>"))
    .buildClient();

try {
    // Read audio file
    byte[] audioData = Files.readAllBytes(Paths.get("path/to/audio.wav"));

    // Create audio file details
    AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
        .setFilename("audio.wav");

    // Create transcription options
    TranscriptionOptions options = new TranscriptionOptions();

    // Create transcribe request content
    TranscriptionContent requestContent = new TranscriptionContent()
        .setAudio(audioFileDetails)
        .setOptions(options);

    // Transcribe audio
    TranscriptionResult result = client.transcribe(requestContent);

    // Process results
    System.out.println("Duration: " + result.getDuration() + "ms");
    result.getCombinedPhrases().forEach(phrase -> {
        System.out.println("Channel " + phrase.getChannel() + ": " + phrase.getText());
    });
} catch (Exception e) {
    System.err.println("Error during transcription: " + e.getMessage());
}
```

### Use enhanced mode for improved transcription quality

Enhanced mode provides advanced features to improve transcription accuracy with custom prompts, translation capabilities, and task-specific optimizations.

#### Basic enhanced mode

```java readme-sample-enhancedModeBasic
byte[] audioData = Files.readAllBytes(Paths.get("path/to/audio.wav"));

AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
    .setFilename("audio.wav");

// Enable enhanced mode for improved transcription quality
EnhancedModeOptions enhancedMode = new EnhancedModeOptions();

TranscriptionOptions options = new TranscriptionOptions()
    .setLocales(java.util.Arrays.asList("en-US"))
    .setEnhancedModeOptions(enhancedMode);

TranscriptionContent requestContent = new TranscriptionContent()
    .setAudio(audioFileDetails)
    .setOptions(options);

TranscriptionResult result = client.transcribe(requestContent);
```

#### Enhanced mode with custom prompts

Use prompts to guide transcription with domain-specific terminology, improving accuracy for specialized content like medical, legal, or technical discussions.

```java readme-sample-enhancedModeWithPrompts
byte[] audioData = Files.readAllBytes(Paths.get("path/to/audio.wav"));

AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
    .setFilename("audio.wav");

// Use prompts to guide transcription with domain-specific terminology
EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
    .setPrompts(java.util.Arrays.asList(
        "Medical consultation discussing hypertension and diabetes",
        "Common medications: metformin, lisinopril, atorvastatin",
        "Patient symptoms and treatment plan"
    ));

TranscriptionOptions options = new TranscriptionOptions()
    .setLocales(java.util.Arrays.asList("en-US"))
    .setEnhancedModeOptions(enhancedMode);

TranscriptionContent requestContent = new TranscriptionContent()
    .setAudio(audioFileDetails)
    .setOptions(options);

TranscriptionResult result = client.transcribe(requestContent);
```

#### Enhanced mode with translation

Transcribe audio in one language and translate to another language simultaneously.

```java readme-sample-enhancedModeWithTranslation
byte[] audioData = Files.readAllBytes(Paths.get("path/to/audio.wav"));

AudioFileDetails audioFileDetails = new AudioFileDetails(BinaryData.fromBytes(audioData))
    .setFilename("audio.wav");

// Configure enhanced mode to transcribe Spanish audio and translate to English
EnhancedModeOptions enhancedMode = new EnhancedModeOptions()
    .setTargetLanguage("en-US"); // Translate to English

TranscriptionOptions options = new TranscriptionOptions()
    .setLocales(java.util.Arrays.asList("es-ES")) // Source language: Spanish
    .setEnhancedModeOptions(enhancedMode);

TranscriptionContent requestContent = new TranscriptionContent()
    .setAudio(audioFileDetails)
    .setOptions(options);

TranscriptionResult result = client.transcribe(requestContent);
```

### Service API versions

The client library targets the latest service API version by default.
The service client builder accepts an optional service API version parameter to specify which API version to communicate.

#### Select a service API version

You have the flexibility to explicitly select a supported service API version when initializing a service client via the service client builder.
This ensures that the client can communicate with services using the specified API version.

When selecting an API version, it is important to verify that there are no breaking changes compared to the latest API version.
If there are significant differences, API calls may fail due to incompatibility.

Always ensure that the chosen API version is fully supported and operational for your specific use case and that it aligns with the service's versioning policy.

## Troubleshooting

### Enable client logging

You can enable logging to debug issues with the client library. The Azure client libraries for Java use the SLF4J logging facade. You can configure logging by adding a logging dependency and configuration file. For more information, see the [logging documentation](https://learn.microsoft.com/azure/developer/java/sdk/logging-overview).

### Common issues

#### Authentication errors

- Verify that your API key is correct and has not expired
- Ensure your endpoint URL matches your Azure resource region

#### Audio format errors

- Verify your audio file is in a supported format
- Ensure the audio file size is under 250 MB and duration is under 2 hours

### Getting help

If you encounter issues:

- Check the [troubleshooting guide](https://learn.microsoft.com/azure/ai-services/speech-service/troubleshooting)
- Search for existing issues or create a new one on [GitHub](https://github.com/Azure/azure-sdk-for-java/issues)
- Ask questions on [Stack Overflow](https://stackoverflow.com/questions/tagged/azure-java-sdk) with the `azure-java-sdk` tag

## Next steps

- Explore the [samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cognitiveservices/azure-ai-speech-transcription/src/samples) for more examples
- Learn more about [Azure Speech Service](https://learn.microsoft.com/azure/ai-services/speech-service/)
- Review the [API reference documentation][docs] for detailed information about classes and methods

## Contributing


For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://learn.microsoft.com/azure/ai-services/speech-service/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/

