# Missing Samples — Java vs Python Parity

Tool samples present in the [Python `azure-ai-projects` README](https://github.com/Azure/azure-sdk-for-python/tree/main/sdk/ai/azure-ai-projects) but **missing** from the Java `azure-ai-agents` samples.

| Tool | Python Sample | Java Status | Notes |
|---|---|---|---|
| **Azure Functions** | `sample_agent_azure_function.py` | ❌ Missing | Requires `AzureFunctionTool` + connection setup |
| **Memory Search (Preview)** | `sample_agent_memory_search.py` | ⚠️ Partial | `MemorySearchAgent.java` exists in root samples but not in `tools/` |
| **Bing Custom Search (Preview)** | `sample_agent_bing_custom_search.py` | ❌ Missing | Requires `BingCustomSearchPreviewTool` |
| **MCP with Project Connection** | `sample_agent_mcp_with_project_connection.py` | ❌ Missing | Connection-authenticated MCP variant |
| **OpenAPI with Project Connection** | `sample_agent_openapi_with_project_connection.py` | ❌ Missing | Uses `OpenApiProjectConnectionAuthDetails` instead of anonymous auth |

## Java extras not in Python README

| Tool | Java Sample | Notes |
|---|---|---|
| **Custom Code Interpreter** | `CustomCodeInterpreterSample.java` | Container-based code interpreter variant |
