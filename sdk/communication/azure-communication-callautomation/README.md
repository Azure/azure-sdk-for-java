# Azure Communication Call Automation Service client library for Java

This package contains a Java SDK for Azure Communication Call Automation Service.

[Source code][source] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][product_docs]

## Getting started

### Prerequisites

- An Azure account with an active subscription. [Create an account for free](https://azure.microsoft.com/free/?WT.mc_id=A261C142F).
- [Java Development Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable) version 8 or above.
- [Apache Maven](https://maven.apache.org/download.cgi).
- A deployed Communication Services resource. You can use the [Azure Portal](https://docs.microsoft.com/azure/communication-services/quickstarts/create-communication-resource?tabs=windows&pivots=platform-azp) or the [Azure PowerShell](https://docs.microsoft.com/powershell/module/az.communication/new-azcommunicationservice) to set it up.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-communication-callautomation;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-communication-callautomation</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
This is a refresh of Calling Server Service. It is renamed to Call Automation service and being more intuitive to use.

`CallAutomationClient` provides the functionality to make call, answer/reject incoming call and redirect a call.

`CallConnection` provides the functionality to perform actions in an established call connection such as adding participants and terminate the call.

`CallMedia` introduces media related functionalities into the call.

`CallRecording` provides the functionality of recording the call.

`EventHandler` provides the functionality to handle events from the ACS resource.

### Using statements
```java
import com.azure.communication.callautomation.*;
import com.azure.communication.callautomation.models.*;
import com.azure.communication.callautomation.models.events.*;
```

### Authenticate the client
Call Automation client can be authenticated using the connection string acquired from an Azure Communication Resource in the [Azure Portal][azure_portal].
```java
CallAutomationAsyncClient client = new CallAutomationClientBuilder()
                .connectionString("<ACS_CONNECTION_STRING>")
                .buildAsyncClient();
```
Or alternatively using a valid Active Directory token / Key.
```java
CallAutomationAsyncClient client = new CallAutomationClientBuilder()
                .endpoint("<ENDPOINT_URL>") // e.g: https://my-resource.communication.azure.com
                .credential("<TOKEN_CREDENTIAL> OR <AZURE_KEY_CREDENTIAL>")
                .buildAsyncClient();
```

## Examples
### Make a call to a phone number recipient
```java
List<CommunicationIdentifier> targets = new ArrayList<>(Arrays.asList(new PhoneNumberIdentifier("<RECIPIENT_PHONE_NUMBER>")));// E.164 formatted phone number like +18001234567
CreateCallOptions options = new CreateCallOptions(new CommunicationUserIdentifier("<CALLER_IDENTIFIER>"), targets, "<CALL_BACK_URL>")
        .setSourceCallerId("<CALLER_PHONE_NUMBER>"); // E.164 formatted phone number. This is required when dialing to a PSTN recipient
    
Response<CreateCallResult> response = client.createCallWithResponse(options).block();
```

### Idempotent Requests
An operation is idempotent if it can be performed multiple times and have the same result as a single execution.

The following operations are idempotent:
- `answerCall`
- `redirectCall`
- `rejectCall`
- `createCall`
- `hangUp` when terminating the call for everyone, ie. `forEveryone` parameter is set to `true`.
- `transferToParticipantCall`
- `addParticipants`
- `removeParticipants`
- `startRecording`

By default, SDK generates a new `RepeatabilityHeaders` object every time the above operation is called. If you would
like to provide your own `RepeatabilityHeaders` for your application (eg. for your own retry mechanism), you can do so by specifying
the `RepeatabilityHeaders` in the operation's `Options` object. If this is not set by user, then the SDK will generate
it.

The parameters for the `RepeatabilityHeaders` class are `repeatabilityRequestId` and `repeatabilityFirstSent`. Two or
more requests are considered the same request **if and only if** both repeatability parameters are the same.
- `repeatabilityRequestId`: an opaque string representing a client-generated unique identifier for the request.
  It is a version 4 (random) UUID.
- `repeatabilityFirstSent`: The value should be the date and time at which the request was **first** created.

To set repeatability parameters, see below Java code snippet as an example:
```java
CreateCallOptions createCallOptions = new CreateCallOptions(caller, targets, callbackUrl)
    .setRepeatabilityHeaders(new RepeatabilityHeaders(UUID.randomUUID(), Instant.now()));
Response<CreateCallResult> response1 = callAsyncClient.createCallWithResponse(createCallOptions).block();

await Task.Delay(5000);

Response<CreateCallResult> response2 = callAsyncClient.createCallWithResponse(createCallOptions).block();
// response1 and response2 will have the same callConnectionId as they have the same reapeatability parameters which means that the CreateCall operation was only executed once.
```

### Handle Mid-Connection call back events
Your app will receive mid-connection call back events via the callback url you provided. You will need to write event handler controller to receive the events and direct your app flow based on your business logic.

```java
    @RequestMapping(value = "/api/callEvents", method = POST)
    public ResponseEntity<?> handleCallEvents(@RequestBody(required = false) String requestBody) {
        List<CallAutomationEventBase> acsEvents = EventHandler.parseEventList(requestBody);

        for (CallAutomationEventBase acsEvent : acsEvents) {
            if (acsEvent instanceof CallConnected) {
                // Do something...
            } else if (acsEvent instanceof RecognizeCompleted) {
                // Do something...
            } else if (acsEvent instanceof CallDisconnected){
                // Do something...   
            }
            ...
        }
        
        return new ResponseEntity<>(HttpStatus.OK);
    }
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
- [Call Automation Overview][overview]
- [Incoming Call Concept][incomingcall]
- [Build a customer interaction workflow using Call Automation][build1]
- [Redirect inbound telephony calls with Call Automation][build2]
- [Quickstart: Play action][build3]
- [Quickstart: Recognize action][build4]
- [Read more about Call Recording in Azure Communication Services][recording1]
- [Record and download calls with Event Grid][recording2]

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[product_docs]: https://docs.microsoft.com/azure/communication-services/
<!-- [package]: TODO: point to maven when available -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java/
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/communication/azure-communication-callautomation/src
[overview]: https://learn.microsoft.com/azure/communication-services/concepts/voice-video-calling/call-automation
[incomingcall]: https://learn.microsoft.com/azure/communication-services/concepts/voice-video-calling/incoming-call-notification
[build1]: https://learn.microsoft.com/azure/communication-services/quickstarts/voice-video-calling/callflows-for-customer-interactions?pivots=programming-language-java
[build2]: https://learn.microsoft.com/azure/communication-services/how-tos/call-automation-sdk/redirect-inbound-telephony-calls?pivots=programming-language-java
[build3]: https://learn.microsoft.com/azure/communication-services/quickstarts/voice-video-calling/play-action?pivots=programming-language-java
[build4]: https://learn.microsoft.com/azure/communication-services/quickstarts/voice-video-calling/recognize-action?pivots=programming-language-java
[recording1]: https://learn.microsoft.com/azure/communication-services/concepts/voice-video-calling/call-recording
[recording2]: https://learn.microsoft.com/azure/communication-services/quickstarts/voice-video-calling/get-started-call-recording?pivots=programming-language-java
[azure_portal]: https://portal.azure.com
