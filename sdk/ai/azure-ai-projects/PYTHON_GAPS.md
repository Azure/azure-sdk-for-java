# Java vs Python SDK — Custom Code Comparison

## Datasets

| Feature | Python | Java | Status |
|---|---|---|---|
| `upload_file` / `createDatasetWithFile` | ✅ Takes `connection_name` param | ✅ Takes `connectionName` param | ✅ Same |
| `upload_folder` / `createDatasetWithFolder` | ✅ Takes `connection_name` param, `file_pattern` regex filter | ✅ Takes `connectionName` param, no `file_pattern` | ⚠️ **Gap** — Java is missing `file_pattern`. Python lets you filter which files to upload via regex. |
| `list` / `listLatestVersion` | ✅ | ✅ | ✅ Same |
| `list_versions` / `listVersions` | ✅ | ✅ | ✅ Same |
| `get` / `getDatasetVersion` | ✅ | ✅ | ✅ Same |
| `delete` / `deleteVersion` | ✅ | ✅ | ✅ Same |
| `create_or_update` / `createOrUpdateVersion` | ✅ | ✅ | ✅ Same |
| `pending_upload` / `pendingUpload` | ✅ | ✅ | ✅ Same |
| `get_credentials` / `getCredentials` | ✅ | ✅ | ✅ Same |

## Connections

| Feature | Python | Java | Status |
|---|---|---|---|
| `list` / `listConnections` | ✅ | ✅ | ✅ Same |
| `get(name, include_credentials=)` | ✅ Single method with flag | ✅ `getConnection(name, includeCredentials)` | ✅ Same |
| `get_default(connection_type, include_credentials=)` | ✅ | ✅ `getDefaultConnection(connectionType, includeCredentials)` | ✅ Same |

## Deployments

| Feature | Python | Java | Status |
|---|---|---|---|
| `list` / `listDeployments` | ✅ | ✅ | ✅ Same |
| `get` / `getDeployment` | ✅ | ✅ | ✅ Same |

## Indexes

| Feature | Python | Java | Status |
|---|---|---|---|
| `list` / `listLatest` | ✅ | ✅ | ✅ Same |
| `list_versions` / `listVersions` | ✅ | ✅ | ✅ Same |
| `get` / `getVersion` | ✅ | ✅ | ✅ Same |
| `delete` / `deleteVersion` | ✅ | ✅ | ✅ Same |
| `create_or_update` / `createOrUpdateVersion` | ✅ | ✅ | ✅ Same |

## Telemetry

| Feature | Python | Java | Status |
|---|---|---|---|
| `telemetry.get_application_insights_connection_string()` | ✅ Custom sub-client | ❌ **Missing entirely** | 🔴 **Gap** — Python has a `TelemetryOperations` sub-client. Java has nothing. |

## Memory Stores (Beta)

| Feature | Python | Java | Status |
|---|---|---|---|
| `beta.memory_stores.search_memories()` | ✅ Custom patch with OpenAI `ResponseInputParam` support | ✅ Surfaced in `azure-ai-agents` SDK | ✅ Different SDK, not a gap |
| `beta.memory_stores.begin_update_memories()` | ✅ Custom LRO poller | ✅ Surfaced in `azure-ai-agents` SDK | ✅ Different SDK, not a gap |

## Top-level Client

| Feature | Python | Java | Status |
|---|---|---|---|
| `get_openai_client()` | ✅ Returns `openai.OpenAI` with auth wired up | ✅ `buildOpenAIClient()` on the builder | ✅ Different pattern but equivalent |
| Console logging via `AZURE_AI_PROJECTS_CONSOLE_LOGGING` | ✅ | ❌ | Minor — nice-to-have |
