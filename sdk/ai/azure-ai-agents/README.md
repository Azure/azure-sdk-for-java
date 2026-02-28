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

The [OpenAI Official Java SDK][openai_java_sdk] is imported transitively and can be accessed from either the `ResponsesClient` or the `ConversationsClient` using the `getOpenAIClient()` method.

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
Conversation conversation = conversationsClient.getConversationService().create();
```

With `conversation.id()` contains the reference we will use to append messages to this `Conversation`. `Conversation` objects can be used by multiple agents and serve the purpose of being a centralized source of context. To add items:

```java com.azure.ai.agents.add_message_to_conversation
conversationsClient.getConversationService().items().create(
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
