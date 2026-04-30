---
name: api-diff
description: 'Diff the Java SDK to identify new API features added between commits or branches. Buckets new additions by functionality (e.g. agents, toolboxes, sessions, memory, skills). WHEN: what is new in the API; diff API changes; compare API between branches; see what changed in the SDK; new API additions.'
---

# API Diff — Identify New Features

Diff the generated Java SDK source to identify new public API additions (methods, models, clients) and bucket them by functionality area.

## Preconditions

- You must be in `sdk/ai/azure-ai-agents` or `sdk/ai/azure-ai-projects`.
- The user must provide a **base reference** to diff against: a commit hash, branch name, or tag. If not provided, ask for it (e.g. `main`, `HEAD~1`, a specific commit).

## Workflow

### 1. Determine the diff range

Ask for or infer:
- **Base**: the starting point (e.g. `main`, a commit hash, a tag)
- **Head**: the current state (defaults to working tree / `HEAD`)

### 2. Get the raw diff of public API files

Diff only the public-facing source files (exclude implementation, tests, samples):

```bash
# For azure-ai-agents
git diff <base> -- \
  src/main/java/com/azure/ai/agents/*Client.java \
  src/main/java/com/azure/ai/agents/*AsyncClient.java \
  src/main/java/com/azure/ai/agents/models/

# For azure-ai-projects
git diff <base> -- \
  src/main/java/com/azure/ai/projects/*Client.java \
  src/main/java/com/azure/ai/projects/*AsyncClient.java \
  src/main/java/com/azure/ai/projects/models/
```

To see only new files:
```bash
git diff <base> --name-status --diff-filter=A -- src/main/java/
```

To see only modified files:
```bash
git diff <base> --name-status --diff-filter=M -- src/main/java/
```

### 3. Extract new public methods

For each client class, find newly added public methods:

```bash
git diff <base> -- src/main/java/com/azure/ai/agents/*Client.java | grep "^+" | grep "public "
```

Focus on convenience methods (skip `*WithResponse` protocol methods unless they have no convenience equivalent).

### 4. Extract new models

Find newly added model classes:

```bash
git diff <base> --name-status --diff-filter=A -- src/main/java/com/azure/ai/agents/models/
```

For modified models, find new fields/getters:

```bash
git diff <base> -- src/main/java/com/azure/ai/agents/models/<Model>.java | grep "^+" | grep "public \|private "
```

### 5. Bucket by functionality

Categorize each new addition into a functionality area. Use **two sources** for bucket names: the known-buckets table below and dynamic discovery from the diff itself.

#### 5a. Discover buckets dynamically

Scan the current client classes to find all feature areas — don't rely solely on the table:

```bash
# List all client class names (each maps to a bucket)
ls src/main/java/com/azure/ai/agents/*Client.java 2>/dev/null | sed 's/.*\///' | sed 's/Client.java//' | sort -u
ls src/main/java/com/azure/ai/projects/*Client.java 2>/dev/null | sed 's/.*\///' | sed 's/Client.java//' | sort -u
```

Each `*Client.java` defines a top-level bucket. Methods within `AgentsClient` that share a resource prefix (e.g., `createSession`, `getSession`, `deleteSession`) form sub-buckets.

For new models without a clear client, bucket by the model's package or the client method that references it.

#### 5b. Known buckets (reference, may be incomplete)

This table is a **starting point** — new feature areas may exist that aren't listed here. If you discover a new bucket during the diff, add it to this table and note it in your output.

| Bucket | Client class | Method/model indicators |
|--------|-------------|------------------------|
| **Agents** | `AgentsClient` | `createAgent`, `deleteAgent`, `getAgent`, `listAgents`, `AgentVersionDetails`, `PromptAgentDefinition` |
| **Hosted Agents** | `AgentsClient` | `HostedAgentDefinition`, `AgentProtocol`, `ProtocolVersionRecord`, methods with `HOSTED_AGENTS_V1_PREVIEW` |
| **Sessions** | `AgentsClient` | `createSession`, `getSession`, `deleteSession`, `listSessions`, `AgentSessionResource` |
| **Agent Endpoints** | `AgentsClient` | `patchAgentObject`, `AgentEndpoint`, `VersionSelector`, methods with `AGENT_ENDPOINT_V1_PREVIEW` |
| **Toolboxes** | `ToolboxesClient` | `createToolboxVersion`, `getToolbox`, `updateToolbox`, `deleteToolbox`, `ToolboxDetails`, `ToolboxVersionDetails` |
| **Memory** | `MemoryStoresClient` | `createMemoryStore`, `getMemoryStore`, `deleteMemoryStore`, `MemoryStoreDetails` |
| **Conversations** | `AgentsClient` | `createConversation`, `getConversation`, `deleteConversation`, `ConversationDetails` |
| **Responses** | `ResponsesClient` | `createResponse`, response-related models |
| **Session Files** | `AgentSessionFilesClient` | `uploadFile`, `listFiles`, `getFile`, `deleteFile` |
| **Tools** | models package | `McpTool`, `CodeInterpreterTool`, `FileSearchTool`, `AzureAISearchTool`, `OpenApiTool`, `Tool` subclasses |
| **Skills** | `SkillsClient` | `createSkill`, `getSkill`, `deleteSkill`, `listSkills`, `SkillDetails` |
| **Connections** | `ConnectionsClient` | `getConnection`, `listConnections`, `ConnectionDetails` |
| **Datasets** | `DatasetsClient` | dataset operations |
| **Deployments** | `DeploymentsClient` | deployment operations |
| **Indexes** | `IndexesClient` | index operations |
| **Evaluations** | `EvaluatorsClient`, `EvaluationRulesClient` | evaluation operations |

#### 5c. Keeping this table current

If you created a new bucket during this diff, **update this SKILL.md** to add it to the table above. This keeps the table useful for future runs. Add a row with the bucket name, the client class, and a few representative method/model indicators.

```bash
# Path to this skill file (for self-update)
# sdk/ai/.github/skills/api-diff/SKILL.md
```

### 6. Output the summary

Present findings as a structured summary:

```
## New API Additions (<base> → <head>)

### Agents
- New method: `AgentsClient.createAgentFromManifest(...)` 
- New model: `CreateAgentFromManifestInput`

### Toolboxes
- New client: `ToolboxesClient` (entirely new)
- New methods: createToolboxVersion, getToolbox, updateToolbox, ...
- New models: ToolboxDetails, ToolboxVersionDetails, ToolboxPolicies

### Memory
- New method: `MemoryStoresClient.searchMemoryStore(...)`
- New model: `MemorySearchResult`

### Models (cross-cutting)
- New field on `AgentVersionDetails`: `containerProtocolVersions`
- Modified: `FixedRatioVersionSelectionRule.trafficPercentage` type changed int → Integer
```

## Tips

- Use `git diff --stat` for a quick overview of what changed
- Use `git log --oneline <base>..HEAD -- src/main/java/` to see commits that touched the source
- For large diffs, focus on client classes first (they define the public API surface), then drill into models
- New clients (entirely new `*Client.java` files) indicate a major new feature area
- New `*OptInKeys` or `Foundry-Features` values indicate preview features

## Example Usage

User: "What's new since the last release?"
```bash
# Find the last release tag
git tag --list "azure-ai-agents_*" --sort=-version:refname | head -1
# Diff against it
git diff <tag> -- src/main/java/com/azure/ai/agents/
```

User: "What changed in this PR branch?"
```bash
git diff main -- src/main/java/
```
