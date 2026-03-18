# Azure Agents client library for Java

Develop Agents using the Azure AI Foundry platform, leveraging an extensive ecosystem of models, tools, and capabilities from OpenAI, Microsoft, and other LLM providers.

The client library uses a single service version `v1` of the AI Foundry [data plane REST APIs](https://aka.ms/azsdk/azure-ai-projects/ga-rest-api-reference).

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-agents;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-agents</artifactId>
    <version>2.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

### Create an AgentsClient

To interact with the Azure Agents service, you'll need to create an instance of the `AgentsClient` class.

```java
AgentsClient agentsClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildClient();
```

Alternatively, you can create an asynchronous client using the `AgentsAsyncClient` class.

```java
AgentsAsyncClient agentsAsyncClient = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .buildAsyncClient();
``` 

The Agents client library has 3 sub-clients which group the different operations that can be performed: 
- `AgentsClient` / `AgentsAsyncClient`: Perform operations related to agents, such as creating, retrieving, updating, and deleting agents.
- `ConversationsClient` / `ConversationsAsyncClient`: Handle conversation operations. See the [OpenAI's Conversation API documentation][openai_conversations_api_docs] for more information.
- `ResponsesClient` / `ResponsesAsyncClient`: Handle responses operations. See the [OpenAI's Responses API documentation][openai_responses_api_docs] for more information.
- `MemoryStoresClient` / `MemoryStoresAsyncClient` **(preview)**: Manage memory stores for agents. This operation group requires the `MemoryStores=V1Preview` feature opt-in flag and is automatically set by the SDK on every request.

To access each sub-client you need to use your `AgentsClientBuilder()`. The Agents client library takes the [Official OpenAI SDK][openai_java_sdk] as a dependency, which is used for all operations, except the ones corresponding to direct Agent management.

```java
AgentsClientBuilder builder = new AgentsClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint);

// Agents sub-clients
AgentsClient agentsClient = builder.buildClient();
AgentsAsyncClient agentsAsyncClient = builder.buildAsyncClient();
// Conversations sub-clients.
ConversationsClient conversationsClient = builder.buildConversationsClient();
ConversationsAsyncClient conversationsAsyncClient = builder.buildConversationsAsyncClient();
// Responses sub-clients.
ResponsesClient responsesClient = builder.buildResponsesClient();
ResponsesAsyncClient responsesAsyncClient = builder.buildResponsesAsyncClient();
```

The [OpenAI Official Java SDK][openai_java_sdk] is imported transitively and can be accessed from either the `ResponsesClient` or the `ConversationsClient` using the `getOpenAIClient()` method. Alternatively, you can build an `OpenAIClient` or `OpenAIClientAsync` directly from the `AgentsClientBuilder`:

```java
OpenAIClient openAIClient = builder.buildOpenAIClient();
OpenAIClientAsync openAIAsyncClient = builder.buildOpenAIAsyncClient();
```

### Agent tools

The SDK supports a variety of tools that can be attached to agent definitions. Some tools are generally available, while others are in **preview** and may change in future releases.

**Generally available tools:**

| Tool class | Description |
|---|---|
| `AzureAISearchTool` | Azure AI Search |
| `AzureFunctionTool` | Azure Functions |
| `BingGroundingTool` | Bing grounding |
| `CodeInterpreterTool` | Code interpreter |
| `FileSearchTool` | File search |
| `FunctionTool` | Custom function calling |
| `ImageGenTool` | Image generation |
| `OpenApiTool` | OpenAPI spec-based tools |

**Preview tools:**

| Tool class | Description |
|---|---|
| `A2APreviewTool` | Agent-to-agent communication |
| `BingCustomSearchPreviewTool` | Bing custom search |
| `BrowserAutomationPreviewTool` | Browser automation |
| `ComputerUsePreviewTool` | Computer use |
| `McpTool` | Model Context Protocol (MCP) |
| `MemorySearchPreviewTool` | Memory search |
| `MicrosoftFabricPreviewTool` | Microsoft Fabric |
| `SharepointPreviewTool` | SharePoint grounding |
| `WebSearchPreviewTool` | Web search |

### Experimental features and opt-in flags

Some features require an opt-in via the `Foundry-Features` HTTP header. The SDK provides two enums for these flags:

- **`AgentDefinitionFeatureKeys`** — Used when creating or updating agents. Passed as a parameter to `createAgent`, `updateAgent`, `createAgentVersion`, and related methods. Available keys: `HOSTED_AGENTS_V1_PREVIEW`, `WORKFLOW_AGENTS_V1_PREVIEW`.
- **`FoundryFeaturesOptInKeys`** — Defines all known opt-in keys, including: `HOSTED_AGENTS_V1_PREVIEW`, `WORKFLOW_AGENTS_V1_PREVIEW`, `EVALUATIONS_V1_PREVIEW`, `SCHEDULES_V1_PREVIEW`, `RED_TEAMS_V1_PREVIEW`, `INSIGHTS_V1_PREVIEW`, `MEMORY_STORES_V1_PREVIEW`.

> **Note:** The `MemoryStoresClient` automatically sets the `MemoryStores=V1Preview` opt-in flag on every request.

```java
// OpenAI SDK ResponsesService accessed from ResponsesClient
ResponsesClient responsesClient = builder.buildResponsesClient();
ResponsesService responsesService = responsesClient.getOpenAIClient();

// OpenAI SDK ConversationService accessed from ConversationsClient
ConversationsClient conversationsClient = builder.buildConversationsClient();
ConversationService conversationService = conversationsClient.getOpenAIClient();
```

### Using OpenAI's official library

If you prefer using the [OpenAI official Java client library][openai_java_sdk] instead, you can do so by including that dependency in your project instead and following the instructions in the linked repository. Additionally, you will have to set up your `OpenAIClient` as shown below:

```java com.azure.ai.agents.openai_official_library
OpenAIClient client = OpenAIOkHttpClient.builder()
    .baseUrl(endpoint.endsWith("/") ? endpoint + "openai/v1" : endpoint + "/openai/v1")
    .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
        new DefaultAzureCredentialBuilder().build(), "https://ai.azure.com/.default")))
    .build();

ResponseCreateParams responseRequest = new ResponseCreateParams.Builder()
    .input("Hello, how can you help me?")
    .model(model)
    .build();

Response result = client.responses().create(responseRequest);
```

Remember to adjust your base URL so that your AI Foundry project `endpoint`'s path ends with `openai/v1` like it's shown in the above code snippet.

## Examples

### Prompt Agent

This example will show how to create the context necessary for a `PromptAgent` to work. Note that the way that context is handled in this scenario would allow you to share the context with multiple agents. 

#### Create an Agent

Creating an Agent can be done like in the following code snippet:

```java com.azure.ai.agents.create_prompt_agent
PromptAgentDefinition promptAgentDefinition = new PromptAgentDefinition("gpt-4o");
AgentVersionDetails agent = agentsClient.createAgentVersion("my-agent", promptAgentDefinition);
```

This will return an `AgentVersionObject` which contains the information necessary to create an `AgentReference`. But first it's necessary to setup the `Conversation` and its messages to be able to obtain `Response`s with a centralized context.

#### Create conversation

First we need to create our `Conversation` object so we can attach items to it:

```java com.azure.ai.agents.create_conversation
Conversation conversation = conversationsClient.create();
```

With `conversation.id()` contains the reference we will use to append messages to this `Conversation`. `Conversation` objects can be used by multiple agents and serve the purpose of being a centralized source of context. To add items:

```java com.azure.ai.agents.add_message_to_conversation
conversationsClient.items().create(
    ItemCreateParams.builder()
        .conversationId(conversation.id())
        .addItem(EasyInputMessage.builder()
            .role(EasyInputMessage.Role.SYSTEM)
            .content("You are a helpful assistant that speaks like a pirate.")
            .build()
        ).addItem(EasyInputMessage.builder()
            .role(EasyInputMessage.Role.USER)
            .content("Hello, agent!")
            .build()
    ).build()
);
```

#### Text generation with Responses

And the final step that ties everything together, we pass the `AgentReference` and the `conversation.id()` as parameters for the `Response` creation:

```java com.azure.ai.agents.create_response
AgentReference agentReference = new AgentReference(agent.getName()).setVersion(agent.getVersion());
Response response = responsesClient.createWithAgentConversation(agentReference, conversation.id());
```

### Using Agent tools

Agents can be enhanced with specialized tools for various capabilities. For complete working examples, see the `tools/` folder under [samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools).

In the description below, tools are organized by their Foundry connection requirements: "Built-in Tools" (which do not require a Foundry connection) and "Connection-based Tools" (which require a Foundry connection).

#### Built-in Tools

These tools work immediately without requiring external connections.

---

##### **Code Interpreter** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/code-interpreter?pivots=java))

Write and run Python code in a sandboxed environment, process files and work with diverse data formats.

```java com.azure.ai.agents.define_code_interpreter
// Create a CodeInterpreterTool with default auto container configuration
CodeInterpreterTool tool = new CodeInterpreterTool();
```

See the full sample in [CodeInterpreterSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/CodeInterpreterSync.java).

---

##### **File Search** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/file-search?pivots=java))

Search through files in a vector store for knowledge retrieval:

```java com.azure.ai.agents.define_file_search
// Create a FileSearchTool with the vector store ID
FileSearchTool tool = new FileSearchTool(Collections.singletonList(vectorStore.id()));
```

See the full sample in [FileSearchSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/FileSearchSync.java).

---

##### **Image Generation** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/image-generation?pivots=java))

Generate images from text descriptions:

```java com.azure.ai.agents.define_image_generation
// Create image generation tool with model, quality, and size
ImageGenTool imageGenTool = new ImageGenTool()
    .setModel(ImageGenToolModel.fromString(imageModel))
    .setQuality(ImageGenToolQuality.LOW)
    .setSize(ImageGenToolSize.fromString("1024x1024"));
```

See the full sample in [ImageGenerationSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/ImageGenerationSync.java).

---

##### **Web Search (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/web-search?pivots=java))

Search the web for current information:

```java com.azure.ai.agents.define_web_search
// Create a WebSearchPreviewTool
WebSearchPreviewTool tool = new WebSearchPreviewTool();
```

See the full sample in [WebSearchSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/WebSearchSync.java).

---

##### **Computer Use (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/computer-use?pivots=java))

Interact with computer interfaces through simulated actions and screenshots:

```java com.azure.ai.agents.define_computer_use
ComputerUsePreviewTool tool = new ComputerUsePreviewTool(
    ComputerEnvironment.WINDOWS,
    1026,
    769
);
```

See the full sample in [ComputerUseSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/ComputerUseSync.java).

---

##### **Model Context Protocol (MCP)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/model-context-protocol?pivots=java))

Connect agents to external MCP servers:

```java com.azure.ai.agents.built_in_mcp
// Uses gitmcp.io to expose a GitHub repository as an MCP-compatible server
McpTool tool = new McpTool("api-specs")
    .setServerUrl("https://gitmcp.io/Azure/azure-rest-api-specs")
    .setRequireApproval("always");
```

See the full sample in [McpSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/McpSync.java).

---

##### **OpenAPI** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/openapi?pivots=java))

Call external APIs defined by OpenAPI specifications without additional client-side code:

```java com.azure.ai.agents.define_openapi
// Load the OpenAPI spec from a JSON file
Map<String, BinaryData> spec = OpenApiFunctionDefinition.readSpecFromFile(
    SampleUtils.getResourcePath("assets/httpbin_openapi.json"));

OpenApiTool tool = new OpenApiTool(
    new OpenApiFunctionDefinition(
        "httpbin_get",
        spec,
        new OpenApiAnonymousAuthDetails())
        .setDescription("Get request metadata from an OpenAPI endpoint."));
```

See the full sample in [OpenApiSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/OpenApiSync.java).

---

##### **Function Tool** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/function-calling?pivots=java))

Define custom functions that allow agents to interact with external APIs, databases, or application logic:

```java  com.azure.ai.agents.define_function_call
Map<String, Object> locationProp = new LinkedHashMap<String, Object>();
locationProp.put("type", "string");
locationProp.put("description", "The city and state, e.g. Seattle, WA");

Map<String, Object> unitProp = new LinkedHashMap<String, Object>();
unitProp.put("type", "string");
unitProp.put("enum", Arrays.asList("celsius", "fahrenheit"));

Map<String, Object> properties = new LinkedHashMap<String, Object>();
properties.put("location", locationProp);
properties.put("unit", unitProp);

Map<String, BinaryData> parameters = new HashMap<String, BinaryData>();
parameters.put("type", BinaryData.fromObject("object"));
parameters.put("properties", BinaryData.fromObject(properties));
parameters.put("required", BinaryData.fromObject(Arrays.asList("location", "unit")));
parameters.put("additionalProperties", BinaryData.fromObject(false));

FunctionTool tool = new FunctionTool("get_weather", parameters, true)
    .setDescription("Get the current weather in a given location");
```

See the full sample in [FunctionCallSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/FunctionCallSync.java).

---

##### **Azure Functions**

Integrate Azure Functions with agents to extend capabilities via serverless compute. Functions are invoked through Azure Storage Queue triggers, allowing asynchronous execution of custom logic:

```java com.azure.ai.agents.define_azure_function
// Create Azure Function tool with Storage Queue bindings
AzureFunctionTool azureFunctionTool = new AzureFunctionTool(
    new AzureFunctionDefinition(
        new AzureFunctionDefinitionDetails("queue_trigger", parameters)
            .setDescription("Get weather for a given location"),
        new AzureFunctionBinding(
            new AzureFunctionStorageQueue(queueServiceEndpoint, inputQueueName)),
        new AzureFunctionBinding(
            new AzureFunctionStorageQueue(queueServiceEndpoint, outputQueueName))
    )
);
```

*After calling `responsesClient.createWithAgent()`, the agent enqueues function arguments to the input queue. Your Azure Function processes the request and returns results via the output queue.*

See the full sample in [AzureFunctionSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/AzureFunctionSync.java).

---

##### **Memory Search (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/concepts/what-is-memory))

The Memory Search tool adds memory to an agent, allowing the agent's AI model to search for past information related to the current user prompt:

```java com.azure.ai.agents.define_memory_search
// Create memory search tool
MemorySearchPreviewTool tool = new MemorySearchPreviewTool(memoryStore.getName(), scope)
    .setUpdateDelaySeconds(1);
```

See the full sample in [MemorySearchSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/MemorySearchSync.java) showing how to create an agent with a memory store and use it across multiple conversations.

---

#### Connection-Based Tools

These tools require configuring connections in your Microsoft Foundry project and use a `projectConnectionId`.

---

##### **Azure AI Search** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/ai-search?pivots=java))

Integrate with Azure AI Search indexes for powerful knowledge retrieval and semantic search capabilities:

```java com.azure.ai.agents.define_azure_ai_search
// Create Azure AI Search tool with index configuration
AzureAISearchTool aiSearchTool = new AzureAISearchTool(
    new AzureAISearchToolResource(Arrays.asList(
        new AISearchIndexResource()
            .setProjectConnectionId(connectionId)
            .setIndexName(indexName)
            .setQueryType(AzureAISearchQueryType.SIMPLE)
    ))
);
```

See the full sample in [AzureAISearchSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/AzureAISearchSync.java).

---

##### **Bing Grounding** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/bing-tools?pivots=java))

Ground agent responses with real-time web search results from Bing to provide up-to-date information:

```java com.azure.ai.agents.define_bing_grounding
// Create Bing grounding tool with connection configuration
BingGroundingTool bingTool = new BingGroundingTool(
    new BingGroundingSearchToolParameters(Arrays.asList(
        new BingGroundingSearchConfiguration(bingConnectionId)
    ))
);
```

See the full sample in [BingGroundingSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/BingGroundingSync.java).

---

##### **Bing Custom Search (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/bing-tools?pivots=java))

**Warning**: Grounding with Bing Custom Search uses Grounding with Bing, which has additional costs and terms: [terms of use](https://www.microsoft.com/bing/apis/grounding-legal-enterprise) and [privacy statement](https://go.microsoft.com/fwlink/?LinkId=521839&clcid=0x409). Customer data will flow outside the Azure compliance boundary.

Use custom-configured Bing search instances for domain-specific or filtered web search results:

```java com.azure.ai.agents.define_bing_custom_search
// Create Bing Custom Search tool with connection and instance configuration
BingCustomSearchPreviewTool bingCustomSearchTool = new BingCustomSearchPreviewTool(
    new BingCustomSearchToolParameters(Arrays.asList(
        new BingCustomSearchConfiguration(connectionId, instanceName)
    ))
);
```

See the full sample in [BingCustomSearchSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/BingCustomSearchSync.java).

---

##### **Microsoft Fabric (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/fabric?pivots=java))

Query data from Microsoft Fabric data sources:

```java com.azure.ai.agents.define_fabric
// Create Microsoft Fabric tool with connection configuration
MicrosoftFabricPreviewTool fabricTool = new MicrosoftFabricPreviewTool(
    new FabricDataAgentToolParameters()
        .setProjectConnections(Arrays.asList(
            new ToolProjectConnection(fabricConnectionId)
        ))
);
```

See the full sample in [FabricSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/FabricSync.java).

---

##### **Microsoft SharePoint (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/sharepoint?pivots=java))

Search through SharePoint documents for grounding:

```java com.azure.ai.agents.define_sharepoint
// Create SharePoint grounding tool with connection configuration
SharepointPreviewTool sharepointTool = new SharepointPreviewTool(
    new SharepointGroundingToolParameters()
        .setProjectConnections(Arrays.asList(
            new ToolProjectConnection(sharepointConnectionId)
        ))
);
```

See the full sample in [SharePointGroundingSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/SharePointGroundingSync.java).

---

##### **Browser Automation (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/browser-automation?pivots=java))

Interact with web pages through browser automation:

```java com.azure.ai.agents.define_browser_automation
// Create browser automation tool with connection configuration
BrowserAutomationPreviewTool browserTool = new BrowserAutomationPreviewTool(
    new BrowserAutomationToolParameters(
        new BrowserAutomationToolConnectionParameters(connectionId)
    )
);
```

See the full sample in [BrowserAutomationSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/BrowserAutomationSync.java).

---

##### **Agent-to-Agent (A2A) (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/agent-to-agent?pivots=java))

Enable agent-to-agent communication with remote A2A endpoints:

```java com.azure.ai.agents.define_agent_to_agent
// Create agent-to-agent tool with connection ID
A2APreviewTool a2aTool = new A2APreviewTool()
    .setProjectConnectionId(a2aConnectionId);
```

See the full sample in [AgentToAgentSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/AgentToAgentSync.java).

---

##### **MCP with Project Connection** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/model-context-protocol?pivots=java))

MCP integration using project-specific connections for accessing connected MCP servers:

```java com.azure.ai.agents.define_mcp_with_connection
// Create MCP tool with project connection authentication
McpTool mcpTool = new McpTool("api-specs")
    .setServerUrl("https://api.githubcopilot.com/mcp")
    .setProjectConnectionId(mcpConnectionId)
    .setRequireApproval("always");
```

See the full sample in [McpWithConnectionSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/McpWithConnectionSync.java).

---

##### **OpenAPI with Project Connection** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/openapi?pivots=java))

Call external APIs defined by OpenAPI specifications using project connection authentication:

```java com.azure.ai.agents.define_openapi_with_connection
// Create OpenAPI tool with project connection authentication
OpenApiTool openApiTool = new OpenApiTool(
    new OpenApiFunctionDefinition(
        "httpbin_get",
        spec,
        new OpenApiProjectConnectionAuthDetails(
            new OpenApiProjectConnectionSecurityScheme(connectionId)))
        .setDescription("Get request metadata from an OpenAPI endpoint."));
```

See the full sample in [OpenApiWithConnectionSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/OpenApiWithConnectionSync.java).

---

### Streaming responses

The `ResponsesClient` and `ResponsesAsyncClient` support streaming, which allows you to process response events as they arrive rather than waiting for the full response. This is useful for displaying text to users in real time and observing tool execution progress.

#### Synchronous streaming

The synchronous streaming methods return `IterableStream<ResponseStreamEvent>`, which can be consumed with a standard for-each loop. Use the `ResponseAccumulator` from the OpenAI SDK to collect events into a final `Response`:

```java com.azure.ai.agents.streaming.simple_sync
// Use ResponseAccumulator to collect streamed events into a final Response
ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

// Stream response - text is printed as it arrives
IterableStream<ResponseStreamEvent> events =
    responsesClient.createStreamingWithAgent(agentReference,
        ResponseCreateParams.builder()
            .input("Tell me a short story about a brave explorer."));

for (ResponseStreamEvent event : events) {
    responseAccumulator.accumulate(event);
    event.outputTextDelta()
        .ifPresent(textEvent -> System.out.print(textEvent.delta()));
}
System.out.println(); // newline after streamed text

// Access the complete accumulated response
Response response = responseAccumulator.response();
System.out.println("\nResponse ID: " + response.id());
```

See the full samples in [SimpleStreamingSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/streaming/SimpleStreamingSync.java), [FunctionCallStreamingSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/streaming/FunctionCallStreamingSync.java), and [CodeInterpreterStreamingSync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/streaming/CodeInterpreterStreamingSync.java).

#### Asynchronous streaming

The asynchronous streaming methods return `Flux<ResponseStreamEvent>`, integrating naturally with Reactor pipelines:

```java com.azure.ai.agents.streaming.simple_async
// Use ResponseAccumulator to collect streamed events into a final Response
ResponseAccumulator responseAccumulator = ResponseAccumulator.create();

// Stream response asynchronously - text is printed as each chunk arrives
return responsesAsyncClient.createStreamingWithAgent(agentReference,
        ResponseCreateParams.builder()
            .input("Tell me a short story about a brave explorer."))
    .doOnNext(event -> {
        responseAccumulator.accumulate(event);
        event.outputTextDelta()
            .ifPresent(textEvent -> System.out.print(textEvent.delta()));
    })
    .then(Mono.fromCallable(() -> {
        System.out.println(); // newline after streamed text

        // Access the complete accumulated response
        Response response = responseAccumulator.response();
        System.out.println("\nResponse ID: " + response.id());
```

See the full samples in [SimpleStreamingAsync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/streaming/SimpleStreamingAsync.java), [FunctionCallStreamingAsync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/streaming/FunctionCallStreamingAsync.java), and [CodeInterpreterStreamingAsync.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/streaming/CodeInterpreterStreamingAsync.java).

---

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
[product_documentation]: https://aka.ms/azsdk/azure-ai-agents/product-doc
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[openai_java_sdk]: https://github.com/openai/openai-java/
[openai_responses_api_docs]: https://platform.openai.com/docs/api-reference/responses
[openai_conversations_api_docs]: https://platform.openai.com/docs/api-reference/conversations
