# Java vs Python SDK — Custom Code Comparison

## Datasets

| Feature | Python | Java | Status |
|---|---|---|---|
| `upload_file` / `createDatasetWithFile` | ✅ Takes `connection_name` param | ✅ No `connection_name` param | ⚠️ **Gap** — Java doesn't pass `connection_name` to `pendingUpload`. Works today because service defaults, but diverges from Python API surface. |
| `upload_folder` / `createDatasetWithFolder` | ✅ Takes `connection_name` param, `file_pattern` regex filter | ✅ No `connection_name`, no `file_pattern` | ⚠️ **Gap** — Java is missing both optional params. Python lets you filter which files to upload via regex. |
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
| `get(name, include_credentials=)` | ✅ Single method with flag | ✅ Custom `getConnection(name, includeCredentials)` | ✅ Same pattern |
| `get_default(connection_type, include_credentials=)` | ✅ Custom convenience method | ❌ **Missing** | 🔴 **Gap** — Python has `connections.get_default(ConnectionType.AZURE_STORAGE_ACCOUNT)` which lists default connections filtered by type and returns the first. Java has no equivalent. |

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
| `beta.memory_stores.search_memories()` | ✅ Custom patch with OpenAI `ResponseInputParam` support | ❌ **Not in this SDK** | ⚠️ This lives in `azure-ai-agents` in Java, so may not be a gap here. |
| `beta.memory_stores.begin_update_memories()` | ✅ Custom LRO poller | ❌ Same | ⚠️ Same note |

## Top-level Client

| Feature | Python | Java | Status |
|---|---|---|---|
| `get_openai_client()` | ✅ Returns `openai.OpenAI` with auth wired up | ✅ `buildOpenAIClient()` on the builder | ✅ Different pattern but equivalent |
| Console logging via `AZURE_AI_PROJECTS_CONSOLE_LOGGING` | ✅ | ❌ | Minor — nice-to-have |
