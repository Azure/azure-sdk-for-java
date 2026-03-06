# README Link Updates — Pending Sample Merge

These README tool sections need sample links added **after** the corresponding `.java` files are checked into `main`.

CI rejects `https://github.com/Azure/azure-sdk-for-java/tree/main/...` URLs that point to files not yet on `main`, so these links must be added in a follow-up commit.

## Samples not yet on `main`

| Sample File | PR | README Section |
|---|---|---|
| `OpenApiSample.java` | [#48315](https://github.com/Azure/azure-sdk-for-java/pull/48315) | **OpenAPI** (Built-in) |
| `AzureFunctionSample.java` | _not yet opened_ | **Azure Functions** (new Built-in section) |
| `MemorySearchSample.java` | _not yet opened_ | **Memory Search (Preview)** (new Built-in section) |
| `BingCustomSearchSample.java` | _not yet opened_ | **Bing Custom Search (Preview)** (new Connection-Based section) |
| `McpWithConnectionSample.java` | _not yet opened_ | **MCP with Project Connection** (new Connection-Based section) |
| `OpenApiWithConnectionSample.java` | _not yet opened_ | **OpenAPI with Project Connection** (new Connection-Based section) |

## What to add after merge

### 1. OpenAPI (existing section — restore link)

After the `OpenApiTool` code snippet, replace the HTML comment with:

```markdown
See the full sample in [OpenApiSample.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/OpenApiSample.java).
```

### 2. Azure Functions (new Built-in section)

Insert before `#### Connection-Based Tools`:

```markdown
---

##### **Azure Functions**

Call Azure Functions via Storage Queue input/output bindings:

\```java
AzureFunctionTool tool = new AzureFunctionTool(
    new AzureFunctionDefinition(
        new AzureFunctionDefinitionDetails("queue_trigger", parameters)
            .setDescription("Get weather for a given location"),
        new AzureFunctionBinding(
            new AzureFunctionStorageQueue(queueServiceEndpoint, inputQueueName)),
        new AzureFunctionBinding(
            new AzureFunctionStorageQueue(queueServiceEndpoint, outputQueueName))));
\```

See the full sample in [AzureFunctionSample.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/AzureFunctionSample.java).
```

### 3. Memory Search (Preview) (new Built-in section)

Insert before `#### Connection-Based Tools`:

```markdown
---

##### **Memory Search (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/concepts/what-is-memory))

Retrieve relevant past user messages using memory stores:

\```java
MemorySearchPreviewTool tool = new MemorySearchPreviewTool(memoryStoreName, scope)
    .setUpdateDelaySeconds(1);
\```

See the full sample in [MemorySearchSample.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/MemorySearchSample.java).
```

### 4. Bing Custom Search (Preview) (new Connection-Based section)

Insert after the **Bing Grounding** section:

```markdown
---

##### **Bing Custom Search (Preview)** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/bing-tools?pivots=java#grounding-with-bing-custom-search-preview))

Search custom Bing search instances for targeted results:

\```java
BingCustomSearchPreviewTool tool = new BingCustomSearchPreviewTool(
    new BingCustomSearchToolParameters(Arrays.asList(
        new BingCustomSearchConfiguration(connectionId, instanceName))));
\```

See the full sample in [BingCustomSearchSample.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/BingCustomSearchSample.java).
```

### 5. MCP with Project Connection (new Connection-Based section)

Insert after the **Agent-to-Agent (A2A) (Preview)** section:

```markdown
---

##### **MCP with Project Connection** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/model-context-protocol?pivots=java))

Connect to MCP servers using project connection authentication, with approval handling:

\```java
McpTool tool = new McpTool("api-specs")
    .setServerUrl("https://api.githubcopilot.com/mcp")
    .setProjectConnectionId(mcpConnectionId)
    .setRequireApproval(BinaryData.fromObject("always"));
\```

See the full sample in [McpWithConnectionSample.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/McpWithConnectionSample.java).
```

### 6. OpenAPI with Project Connection (new Connection-Based section)

Insert after the **MCP with Project Connection** section:

```markdown
---

##### **OpenAPI with Project Connection** ([documentation](https://learn.microsoft.com/azure/foundry/agents/how-to/tools/openapi?pivots=java))

Call external APIs defined by OpenAPI specifications using project connection authentication:

\```java
OpenApiTool tool = new OpenApiTool(
    new OpenApiFunctionDefinition("my_api", spec,
        new OpenApiProjectConnectionAuthDetails(
            new OpenApiProjectConnectionSecurityScheme(connectionId)))
        .setDescription("Call an authenticated API endpoint."));
\```

See the full sample in [OpenApiWithConnectionSample.java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/tools/OpenApiWithConnectionSample.java).
```
