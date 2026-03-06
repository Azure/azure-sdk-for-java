# README Link Updates — Pending Sample Merge

These README tool sections need sample links added **after** the corresponding `.java` files are checked into `main`.

CI rejects `https://github.com/Azure/azure-sdk-for-java/tree/main/...` URLs that point to files not yet on `main`, so these links must be added in a follow-up commit.

## Samples not yet on `main`

All tool samples were renamed from `*Sample.java` / `*Agent.java` to `*Sync.java` / `*Async.java`.
This means **every** README sample link will need updating once the new files land on `main`.

### Renamed samples (exist on `main` under old names)

| Old Name (on `main`) | New Name | README Section |
|---|---|---|
| `CodeInterpreterSample.java` | `CodeInterpreterSync.java` | **Code Interpreter** |
| `FileSearchSample.java` | `FileSearchSync.java` | **File Search** |
| `ImageGenerationSample.java` | `ImageGenerationSync.java` | **Image Generation** |
| `WebSearchSample.java` | `WebSearchSync.java` | **Web Search (Preview)** |
| `ComputerUseSync.java` | `ComputerUseSync.java` | **Computer Use** (unchanged) |
| `McpToolSample.java` | `McpSync.java` | **MCP** |
| `FunctionCallingSample.java` | `FunctionCallSync.java` | **Function Tool** |
| `AzureAISearchSample.java` | `AzureAISearchSync.java` | **Azure AI Search** |
| `BingGroundingSample.java` | `BingGroundingSync.java` | **Bing Grounding** |
| `FabricSample.java` | `FabricSync.java` | **Microsoft Fabric** |
| `SharePointGroundingSample.java` | `SharePointGroundingSync.java` | **Microsoft SharePoint** |
| `BrowserAutomationSample.java` | `BrowserAutomationSync.java` | **Browser Automation** |
| `AgentToAgentSample.java` | `AgentToAgentSync.java` | **Agent-to-Agent (A2A)** |

### Brand-new samples (not on `main` at all)

| Sample File | README Section |
|---|---|
| `OpenApiSync.java` | **OpenAPI** (restore link — currently HTML comment) |
| `AzureFunctionSync.java` | **Azure Functions** (new Built-in section) |
| `MemorySearchSync.java` | **Memory Search (Preview)** (new Built-in section) |
| `BingCustomSearchSync.java` | **Bing Custom Search (Preview)** (new Connection-Based section) |
| `McpWithConnectionSync.java` | **MCP with Project Connection** (new Connection-Based section) |
| `OpenApiWithConnectionSync.java` | **OpenAPI with Project Connection** (new Connection-Based section) |

### Deleted files (remove old links from `main`)

| Old Name (on `main`) | Replaced By |
|---|---|
| `CodeInterpreterAgent.java` | `CodeInterpreterSync.java` |
| `CodeInterpreterAgentAsync.java` | `CodeInterpreterAsync.java` |
| `FunctionCallAgent.java` | `FunctionCallSync.java` |
| `FunctionCallAgentAsync.java` | `FunctionCallAsync.java` |
| `McpAgent.java` | `McpSync.java` |
| `McpAgentAsync.java` | `McpAsync.java` |
| `WebSearchAgent.java` | `WebSearchSync.java` |
| `WebSearchAgentAsync.java` | `WebSearchAsync.java` |
| `FileSearchAgent.java` | `FileSearchSync.java` |
| `FunctionCallingSample.java` | `FunctionCallSync.java` |
| `McpToolSample.java` | `McpSync.java` |

## What to update in README

### 1. Update all existing sample links (renamed files)

Replace each `*Sample.java` / `*Agent.java` link with the corresponding `*Sync.java`:

```
CodeInterpreterSample.java  →  CodeInterpreterSync.java
FileSearchSample.java       →  FileSearchSync.java
ImageGenerationSample.java  →  ImageGenerationSync.java
WebSearchSample.java        →  WebSearchSync.java
McpToolSample.java          →  McpSync.java
FunctionCallingSample.java  →  FunctionCallSync.java
AzureAISearchSample.java    →  AzureAISearchSync.java
BingGroundingSample.java    →  BingGroundingSync.java
FabricSample.java           →  FabricSync.java
SharePointGroundingSample.java → SharePointGroundingSync.java
BrowserAutomationSample.java   → BrowserAutomationSync.java
AgentToAgentSample.java     →  AgentToAgentSync.java
```

### 2. Restore OpenAPI link

Replace the `<!-- TODO -->` comment with:
```markdown
See the full sample in [OpenApiSync.java](...tools/OpenApiSync.java).
```

### 3. Add 5 new tool sections

See previous version of this file for the exact markdown snippets. Update file names from `*Sample.java` to `*Sync.java`.
