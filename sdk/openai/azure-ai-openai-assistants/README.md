# Azure OpenAI: OpenAI Assistants client library for Java

The Azure OpenAI Assistants client library for Java is an adaptation of OpenAI's REST APIs that provides an idiomatic interface
and rich integration with the rest of the Azure SDK ecosystem. It will connect to Azure OpenAI resources *or* to the
non-Azure OpenAI inference endpoint, making it a great choice for even non-Azure OpenAI development.

Use this library to:

- Create and manage assistants, threads, messages, and runs
- Configure and use tools with assistants
- Upload and manage files for use with assistants

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Azure OpenAI access][azure_openai_access]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-openai-assistants;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-openai-assistants</artifactId>
    <version>1.0.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

See [OpenAI's "how assistants work"](https://platform.openai.com/docs/assistants/how-it-works) documentation for an
overview of the concepts and relationships used with assistants. This overview closely follows
[OpenAI's overview example](https://platform.openai.com/docs/assistants/overview) to demonstrate the basics of
creating, running, and using assistants and threads.

#### Create a Azure OpenAI client with key credential
Get Azure OpenAI `key` credential from the Azure Portal.

```java readme-sample-createSyncClientKeyCredential
AssistantsClient client = new AssistantsClientBuilder()
        .credential(new AzureKeyCredential("{key}"))
        .endpoint("{endpoint}")
        .buildClient();
```
or
```java readme-sample-createAsyncClientKeyCredential
AssistantsAsyncClient client = new AssistantsClientBuilder()
        .credential(new AzureKeyCredential("{key}"))
        .endpoint("{endpoint}")
        .buildAsyncClient();
```

### Support for non-Azure OpenAI

The SDK also supports operating against the public non-Azure OpenAI. The response models remain the same, only the
setup of the `Assistants Client` is slightly different. First, get Non-Azure OpenAI API key from
[Open AI authentication API keys][non_azure_openai_authentication]. Then setup your `Assistants Client` as follows:

```java readme-sample-createNonAzureAssistantSyncClientApiKey
AssistantsClient client = new AssistantsClientBuilder()
        .credential(new KeyCredential("{openai-secret-key}"))
        .buildClient();
```
or

```java readme-sample-createNonAzureAssistantAsyncClientApiKey
AssistantsAsyncClient client = new AssistantsClientBuilder()
        .credential(new KeyCredential("{openai-secret-key}"))
        .buildAsyncClient();
```

## Key concepts

### Overview

For an overview of Assistants and the pertinent key concepts like Threads, Messages, Runs, and Tools, please see
[OpenAI's Assistants API overview](https://platform.openai.com/docs/assistants/overview).

## Examples

### Working with simple assistant operations

#### Create an assistant
With an authenticated client, an assistant can be created:
```java readme-sample-createAssistant
AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("{deploymentOrModelId}")
        .setName("Math Tutor")
        .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
Assistant assistant = client.createAssistant(assistantCreationOptions);
```

#### Create a thread with message and then run it
Then a thread can be created:
```java readme-sample-createThread
AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());
String threadId = thread.getId();
```

With a thread created, a message can be created on it:
```java readme-sample-createMessage
String userMessage = "I need to solve the equation `3x + 11 = 14`. Can you help me?";
ThreadMessage threadMessage = client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, userMessage));
```

As we have a thread and message, we can create a run:
```java readme-sample-createRun
ThreadRun run = client.createRun(threadId, new CreateRunOptions(assistantId));
```

There is also a convenience method to create a thread and message, and then run it in one call:
```java readme-sample-createThreadAndRun
CreateAndRunThreadOptions createAndRunThreadOptions = new CreateAndRunThreadOptions(assistantId)
        .setThread(new AssistantThreadCreationOptions()
                .setMessages(Arrays.asList(new ThreadMessageOptions(MessageRole.USER,
                        "I need to solve the equation `3x + 11 = 14`. Can you help me?"))));
run = client.createThreadAndRun(createAndRunThreadOptions);
```

Once the run has started, it should then be polled until it reaches a terminal status:
```java readme-sample-pollRun
do {
    run = client.getRun(run.getThreadId(), run.getId());
    Thread.sleep(1000);
} while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);
```

Assuming the run successfully completed, listing messages from the thread that was run will now reflect new information
added by the assistant:
```java readme-sample-listMessagesAfterRun
PageableList<ThreadMessage> messages = client.listMessages(run.getThreadId());
List<ThreadMessage> data = messages.getData();
for (int i = 0; i < data.size(); i++) {
    ThreadMessage dataMessage = data.get(i);
    MessageRole role = dataMessage.getRole();
    for (MessageContent messageContent : dataMessage.getContent()) {
        MessageTextContent messageTextContent = (MessageTextContent) messageContent;
        System.out.println(i + ": Role = " + role + ", content = " + messageTextContent.getText().getValue());
    }
}
```

For more examples, such as listing assistants/threads/messages/runs/runSteps, upload files, delete assistants/threads,
etc, see the [samples][samples_readme].

### Working with files for retrieval

Files can be uploaded and then referenced by assistants or messages. First, use the generalized upload API with a 
purpose of 'assistants' to make a file ID available:

```java readme-sample-uploadFile
Path filePath = Paths.get("src", "samples", "resources", fileName);
BinaryData fileData = BinaryData.fromFile(filePath);
FileDetails fileDetails = new FileDetails(fileData, fileName);

OpenAIFile openAIFile = client.uploadFile(fileDetails, FilePurpose.ASSISTANTS);
```

Once uploaded, the file ID can then be provided to an assistant upon creation. Note that file IDs will only be used if 
an appropriate tool like Code Interpreter or Retrieval is enabled.

```java readme-sample-createRetrievalAssistant
// Create Tool Resources. This is how we pass files to the Assistant.
CreateToolResourcesOptions createToolResourcesOptions = new CreateToolResourcesOptions();
createToolResourcesOptions.setFileSearch(
    new CreateFileSearchToolResourceOptions(
        new CreateFileSearchToolResourceVectorStoreOptionsList(
            Arrays.asList(new CreateFileSearchToolResourceVectorStoreOptions(Arrays.asList(openAIFile.getId()))))));

Assistant assistant = client.createAssistant(
    new AssistantCreationOptions(deploymentOrModelId)
        .setName("Java SDK Retrieval Sample")
        .setInstructions("You are a helpful assistant that can help fetch data from files you know about.")
        .setTools(Arrays.asList(new FileSearchToolDefinition()))
        .setToolResources(createToolResourcesOptions)
);
```

With a file ID association and a supported tool enabled, the assistant will then be able to consume the associated data 
when running threads.

### Using function tools and parallel function calling

As [described in OpenAI's documentation for assistant tools](https://platform.openai.com/docs/assistants/tools/function-calling), 
tools that reference caller-defined capabilities as functions can be provided to an assistant to allow it to dynamically 
resolve and disambiguate during a run.

Here, outlined is a simple assistant that "knows how to," via caller-provided functions:

1. Get the user's favorite city
2. Get a nickname for a given city
3. Get the current weather, optionally with a temperature unit, in a city

To do this, begin by defining the functions to use -- the actual implementations here are merely representative stubs.
For the full sample, please follow this [link][function_tool_call_full_sample].

```java readme-sample-functionDefinition
private FunctionToolDefinition getUserFavoriteCityToolDefinition() {

    class UserFavoriteCityParameters implements JsonSerializable<UserFavoriteCityParameters> {

        private String type = "object";

        private Map<String, JsonSerializable<?>> properties = new HashMap<>();

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeStartObject("properties");
            for (Map.Entry<String, JsonSerializable<?>> entry : this.properties.entrySet()) {
                jsonWriter.writeFieldName(entry.getKey());
                entry.getValue().toJson(jsonWriter);
            }
            jsonWriter.writeEndObject();
            return jsonWriter.writeEndObject();
        }
    }

    return new FunctionToolDefinition(
        new FunctionDefinition(
            GET_USER_FAVORITE_CITY,
            BinaryData.fromObject(new UserFavoriteCityParameters()
            )
        ).setDescription("Gets the user's favorite city."));
}
```

Please refer to [full sample][function_tool_call_full_sample] for more details on how to set up methods with mandatory
parameters and enum types.

With the functions defined in their appropriate tools, an assistant can be now created that has those tools enabled:

```java readme-sample-createAssistantFunctionCall
AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(deploymentOrModelId)
    .setName("Java Assistants SDK Function Tool Sample Assistant")
    .setInstructions("You are a weather bot. Use the provided functions to help answer questions. "
        + "Customize your responses to the user's preferences as much as possible and use friendly "
        + "nicknames for cities whenever possible.")
    .setTools(Arrays.asList(
        getUserFavoriteCityToolDefinition(),
        getCityNicknameToolDefinition(),
        getWeatherAtLocationToolDefinition()
    ));

Assistant assistant = client.createAssistant(assistantCreationOptions);
```

If the assistant calls tools, the calling code will need to resolve ToolCall instances into matching ToolOutput instances. 
For convenience, a basic example is extracted here:

```java readme-sample-resolveToolOutput
private ToolOutput getResolvedToolOutput(RequiredToolCall toolCall) {
    if (toolCall instanceof RequiredFunctionToolCall) {
        RequiredFunctionToolCall functionToolCall = (RequiredFunctionToolCall) toolCall;
        RequiredFunctionToolCallDetails functionCallDetails = functionToolCall.getFunction();
        String name = functionCallDetails.getName();
        String arguments = functionCallDetails.getArguments();
        ToolOutput toolOutput = new ToolOutput().setToolCallId(toolCall.getId());
        if (GET_USER_FAVORITE_CITY.equals(name)) {
            toolOutput.setOutput(getUserFavoriteCity());
        } else if (GET_CITY_NICKNAME.equals(name)) {
            Map<String, String> parameters = BinaryData.fromString(arguments)
                    .toObject(new TypeReference<Map<String, String>>() {});
            String location = parameters.get("location");

            toolOutput.setOutput(getCityNickname(location));
        } else if (GET_WEATHER_AT_LOCATION.equals(name)) {
            Map<String, String> parameters = BinaryData.fromString(arguments)
                    .toObject(new TypeReference<Map<String, String>>() {});
            String location = parameters.get("location");
            // unit was not marked as required on our Function tool definition, so we need to handle its absence
            String unit = parameters.getOrDefault("unit", "c");

            toolOutput.setOutput(getWeatherAtLocation(location, unit));
        }
        return toolOutput;
    }
    throw new IllegalArgumentException("Tool call not supported: " + toolCall.getClass());
}
```

To handle user input like "what's the weather like right now in my favorite city?", polling the response for completion
should be supplemented by a `RunStatus` check for `RequiresAction` or, in this case, the presence of the
`RequiredAction` property on the run. Then, the collection of `toolOutputs` should be submitted to the
run via the `SubmitRunToolOutputs` method so that the run can continue:

```java readme-sample-functionHandlingRunPolling
do {
    Thread.sleep(1000);
    run = client.getRun(thread.getId(), run.getId());

    if (run.getStatus() == RunStatus.REQUIRES_ACTION
        && run.getRequiredAction() instanceof SubmitToolOutputsAction) {
        SubmitToolOutputsAction requiredAction = (SubmitToolOutputsAction) run.getRequiredAction();
        List<ToolOutput> toolOutputs = new ArrayList<>();

        for (RequiredToolCall toolCall : requiredAction.getSubmitToolOutputs().getToolCalls()) {
            toolOutputs.add(getResolvedToolOutput(toolCall));
        }
        run = client.submitToolOutputsToRun(thread.getId(), run.getId(), toolOutputs);
    }
} while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);
```

Note that, when using supported models, the assistant may request that several functions be called in parallel. Older
models may only call one function at a time.

Once all needed function calls have been resolved, the run will proceed normally and the completed messages on the
thread will contain model output supplemented by the provided function tool outputs.

## Troubleshooting
### Enable client logging
You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][logLevels].

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
- Samples are explained in detail [here][samples_readme].

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[logLevels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[non_azure_openai_authentication]: https://platform.openai.com/docs/api-reference/authentication
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/openai/azure-ai-openai-assistants/src/samples

[azure_openai_access]: https://learn.microsoft.com/azure/cognitive-services/openai/overview#how-do-i-get-access-to-azure-openai
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[function_tool_call_full_sample]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai-assistants/src/samples/java/com/azure/ai/openai/assistants/FunctionToolCallSample.java
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fopenai%2Fassistants%2Fazure-ai-openai-assistants%2FREADME.png)
