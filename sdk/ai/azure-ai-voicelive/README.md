# Azure VoiceLive client library for Java

Azure VoiceLive client library for Java.

This package contains Microsoft Azure VoiceLive client library.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-voicelive;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-voicelive</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

### VoiceLiveAsyncClient
The main entry point for interacting with the Azure VoiceLive service. Use the `VoiceLiveAsyncClientBuilder` to construct a client instance.

### VoiceLiveSession
Represents an active WebSocket connection for bidirectional streaming communication. Sessions support:
- Sending audio input streams
- Sending command events
- Receiving server events as a reactive stream (Flux)

### VoiceLiveSessionOptions
Configuration options for customizing session behavior, including:
- Model selection
- Voice settings
- Modalities (text, audio)
- Turn detection
- Tools and function calling

## Examples

### Creating a client with API key authentication

```java
VoiceLiveAsyncClient client = new VoiceLiveAsyncClientBuilder()
    .endpoint("https://your-endpoint.cognitiveservices.azure.com")
    .credential(new AzureKeyCredential("your-api-key"))
    .buildAsyncClient();
```

### Creating a client with Azure AD authentication

```java
VoiceLiveAsyncClient client = new VoiceLiveAsyncClientBuilder()
    .endpoint("https://your-endpoint.cognitiveservices.azure.com")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();
```

### Starting a session with configuration

```java
// Using OpenAI voice (extends VoiceProvider)
VoiceLiveSessionOptions options = new VoiceLiveSessionOptions("gpt-4o-realtime-preview")
    .setInstructions("You are a helpful AI assistant")
    .setVoice(new OpenAIVoice(OpenAIVoiceName.ALLOY))
    .setModalities(Arrays.asList(Modality.TEXT, Modality.AUDIO));

// Or using Azure voice
// VoiceProvider azureVoice = new AzureStandardVoice("en-US-JennyNeural");
// options.setVoice(azureVoice);

client.startSession(options)
    .flatMap(session -> {
        // Session is now connected and ready
        return Mono.just(session);
    })
    .subscribe(
        session -> System.out.println("Session started"),
        error -> System.err.println("Error: " + error.getMessage())
    );
```

### Sending audio input

```java
session.sendInputAudio(audioInputStream)
    .subscribe(
        () -> System.out.println("Audio sent successfully"),
        error -> System.err.println("Error sending audio: " + error)
    );
```

### Receiving server events

```java
session.receiveUpdates()
    .subscribe(
        binaryData -> {
            // Process server events
            String eventJson = binaryData.toString();
            System.out.println("Received: " + eventJson);
        },
        error -> System.err.println("Error: " + error),
        () -> System.out.println("Stream completed")
    );
```

### Complete example: Voice assistant

```java
VoiceLiveAsyncClient client = new VoiceLiveAsyncClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(apiKey))
    .buildAsyncClient();

VoiceLiveSessionOptions options = new VoiceLiveSessionOptions("gpt-4o-realtime-preview")
    .setInstructions("You are a helpful assistant")
    .setVoice(new OpenAIVoice(OpenAIVoiceName.ALLOY));

client.startSession(options)
    .flatMap(session -> {
        // Subscribe to receive events
        session.receiveUpdates()
            .subscribe(
                event -> handleEvent(event),
                error -> System.err.println("Error: " + error)
            );
        
        // Send audio input
        FileInputStream audioStream = new FileInputStream("audio.wav");
        return session.sendInputAudio(audioStream)
            .then(Mono.just(session));
    })
    .block();
```

### Handling specific event types

```java
session.receiveUpdates()
    .subscribe(binaryData -> {
        String json = binaryData.toString();
        
        // Parse and handle different event types
        if (json.contains("\"type\":\"session.created\"")) {
            System.out.println("Session created");
        } else if (json.contains("\"type\":\"response.text.delta\"")) {
            // Extract and display text
            System.out.println("Text response received");
        } else if (json.contains("\"type\":\"response.audio.delta\"")) {
            // Process audio data
            System.out.println("Audio response received");
        } else if (json.contains("\"type\":\"error\"")) {
            System.err.println("Error: " + json);
        }
    });
```

### Voice Configuration

The SDK supports multiple voice providers through the `VoiceProvider` base class:

#### OpenAI Voices

```java
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;

VoiceProvider voice = new OpenAIVoice(OpenAIVoiceName.ALLOY);
// Other options: ECHO, FABLE, ONYX, NOVA, SHIMMER
```

#### Azure Standard Voices

```java
import com.azure.ai.voicelive.models.AzureStandardVoice;

VoiceProvider voice = new AzureStandardVoice("en-US-JennyNeural");
```

#### Azure Custom Voices

```java
import com.azure.ai.voicelive.models.AzureCustomVoice;

VoiceProvider voice = new AzureCustomVoice("your-custom-voice-id", "your-deployment-id");
```

#### Azure Personal Voices

```java
import com.azure.ai.voicelive.models.AzurePersonalVoice;

VoiceProvider voice = new AzurePersonalVoice("your-speaker-profile-id");
```

### Closing a session

```java
session.closeAsync()
    .subscribe(
        () -> System.out.println("Session closed"),
        error -> System.err.println("Error closing: " + error)
    );

// Or synchronously
session.close();
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

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
