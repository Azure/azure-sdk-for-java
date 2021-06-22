# Azure Communication CallingServer Service client library for Java

This package contains a Java SDK for Azure Communication CallingServer Service.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-callingserver;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-callingserver</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

At a high level the Azure Communication CallingServer API will support two kinds of scenarios:

- In-call app: Contoso server app is a participant in the call.  

- Out-call app: Contoso server app is not a participant in the call - Server app can subscribe to events for calls between specific users or even all users belonging to the ACS azure resource.  

Based on if the Contoso app join a call or not, APIs can be divided into two categories:   

- In-call APIs: Contoso app is one of the participant in a call. It can be applicable for app to person (A2P) or person to app (P2A) case, or multi-party/group calls that server apps joined as a participant to provide audio/prompt.  

- Out-of-call APIs: Contoso app can invoke these set of APIs without joining a call. It is applicable for actions on P2P calls, A2P calls, P2A calls and group calls.  

## Examples

### Authenticate the client


You can provide the connection string using the connectionString() function of `CallingServerClientBuilder`. Once you initialized a `CallingServerClient` class, you can do the different server calling operations.
<!-- embedme src/samples/java/com/azure/communication/callingserver/ReadmeSamples.java#L31-L37 -->
```java
// Your connectionString retrieved from your Azure Communication Service
String connectionString = "endpoint=https://<resource-name>.communication.azure.com/;accesskey=<access-key>";

// Initialize the calling server client
final CallingServerClientBuilder builder = new CallingServerClientBuilder();
builder.connectionString(connectionString);
CallingServerClient callingServerClient = builder.buildClient();
```

### Create call, Add participant and Hangup a call

#### Create a Call: 
<!-- embedme src/samples/java/com/azure/communication/callingserver/ReadmeSamples.java#L49-L68 -->
```java
CommunicationIdentifier source = new CommunicationUserIdentifier("<acs-user-identity>");
CommunicationIdentifier firstCallee = new CommunicationUserIdentifier("<acs-user-identity-1>");
CommunicationIdentifier secondCallee = new CommunicationUserIdentifier("<acs-user-identity-2>");

List<CommunicationIdentifier> targets = Arrays.asList(firstCallee, secondCallee);

String callbackUri = "<callback-uri-for-notification>";

List<MediaType> requestedMediaTypes = Arrays.asList(MediaType.AUDIO, MediaType.VIDEO);

List<EventSubscriptionType> requestedCallEvents = Arrays.asList(
    EventSubscriptionType.DTMF_RECEIVED,
    EventSubscriptionType.PARTICIPANTS_UPDATED);

CreateCallOptions createCallOptions = new CreateCallOptions(
    callbackUri,
    requestedMediaTypes,
    requestedCallEvents);

CallConnection callConnection = callingServerClient.createCallConnection(source, targets, createCallOptions);
```

#### Add a participant to a Call:
<!-- embedme src/samples/java/com/azure/communication/callingserver/ReadmeSamples.java#L88-L89 -->
```java
CommunicationIdentifier thirdCallee = new CommunicationUserIdentifier("<acs-user-identity-3>");
callConnection.addParticipant(thirdCallee, "ACS User 3", "<string-for-tracing-responses>");
```

#### Hangup a Call:
<!-- embedme src/samples/java/com/azure/communication/callingserver/ReadmeSamples.java#L78-L78 -->
```java
callConnection.hangup();
```

### Start, Pause, Resume, Stop and Get a recording

#### Start a Recording: 
<!-- embedme src/samples/java/com/azure/communication/callingserver/ConversationClientReadmeSamples.java#L47-L51 -->
```java
String serverCallId = "<serverCallId received from starting call>";
String recordingStateCallbackUri = "<webhook endpoint to which calling service can report status>";
ServerCall serverCall = callingServerClient.initializeServerCall(serverCallId);
StartCallRecordingResponse response = serverCall.startRecording(recordingStateCallbackUri);
String recordingId = response.getRecordingId();
```

#### Pause a Recording: 
<!-- embedme src/samples/java/com/azure/communication/callingserver/ConversationClientReadmeSamples.java#L65-L65 -->
```java
serverCall.pauseRecording(recordingId);
```

#### Resume a Recording: 
<!-- embedme src/samples/java/com/azure/communication/callingserver/ConversationClientReadmeSamples.java#L78-L78 -->
```java
serverCall.resumeRecording(recordingId);
```

#### Stop a Recording: 
<!-- embedme src/samples/java/com/azure/communication/callingserver/ConversationClientReadmeSamples.java#L91-L91 -->
```java
serverCall.stopRecording(recordingId);
```

#### Get the Recording State: 
<!-- embedme src/samples/java/com/azure/communication/callingserver/ConversationClientReadmeSamples.java#L105-L105 -->
```java
CallRecordingStateResult callRecordingStateResult = serverCall.getRecordingState(recordingId);
```

#### Download a Recording into a file:
<!-- embedme src/samples/java/com/azure/communication/callingserver/ReadmeSamples.java#L100-L100 -->
```java
callingServerClient.downloadTo(
            recordingUrl,
            Paths.get(filePath),
            null,
            true
        );
```
### Play Audio in Call

#### Play Audio: 
<!-- embedme src/samples/java/com/azure/communication/callingserver/ConversationClientReadmeSamples.java#L122-L127 -->
```java
String audioFileUri = "<uri of the file to play>";
String audioFileId = "<a name to use for caching the audio file>";
String callbackUri = "<webhook endpoint to which calling service can report status>";
String context = "<Identifier for correlating responses>";
PlayAudioResponse playAudioResponse = serverCall.playAudio(audioFileUri, audioFileId, callbackUri, context);
```

## Troubleshooting

If you recieve a CommunicationErrorException with the messagae: "Action is invalid when call is not in Established state." This usually means the call has ended. This can occur if the participants all leave
the call, or participants did not accept the call before the call timed out. 

If you fail to start a call because of an HMAC validation error, be sure your access key is correct, and
that you are passing in a valid conversation id.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

## Next steps

Check out other client libraries for Azure Communication Services

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
[package]: https://search.maven.org/artifact/com.azure/azure-communication-callingserver
[api_documentation]: https://aka.ms/java-docs
[source]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/communication/azure-communication-callingserver/src
