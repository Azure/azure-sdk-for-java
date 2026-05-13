# AI Projects / Agents sample coverage plan

Reference SDK: `C:\Users\josealvar\code\azure_repos\azure-sdk-for-python\sdk\ai\azure-ai-projects`.

Goal: add sample coverage in two iterations. Iteration 1 mirrors Python-backed features and leaves existing Java samples untouched. Iteration 2 can cover Java-only/generated surfaces after Iteration 1 is validated live.

## Iteration 1: Python-matched samples

Status: implemented in this branch; compile validation passed. Endpoint-only live validation passed for Skills package and Toolboxes async samples. Hosted-agent live validation is pending `FOUNDRY_AGENT_CONTAINER_IMAGE`.

### `azure-ai-agents`: hosted-agent sessions and endpoints

Add new samples under:

`/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/hostedagents/`

Added files:

- `HostedAgentsSampleUtils.java`
  - Shared helper for creating a hosted agent version, waiting for it to become active, creating a session, and cleaning up.
- `SessionsSample.java`
- `SessionsAsyncSample.java`
  - Python references: `sample_sessions_crud.py`, `sample_sessions_crud_async.py`.
  - Covers create/get/list/delete session for a hosted agent.
- `SessionFilesSample.java`
- `SessionFilesAsyncSample.java`
  - Python references: `sample_sessions_files_upload_download.py`, `sample_sessions_files_upload_download_async.py`.
  - Covers upload/list/download/delete files in a session sandbox.
- `AgentEndpointSample.java`
- `AgentEndpointAsyncSample.java`
  - Python references: `sample_agent_endpoint.py`, `sample_agent_endpoint_async.py`.
  - Covers configuring an agent endpoint for Responses protocol and invoking it with an agent-scoped OpenAI client.
- `SessionLogStreamSample.java`
- `SessionLogStreamAsyncSample.java`
  - Python references: `sample_session_log_stream.py`, `sample_session_log_stream_async.py`.
  - Covers invoking the hosted agent endpoint and reading session log SSE frames.

Expected live env vars:

- `FOUNDRY_PROJECT_ENDPOINT`
- `FOUNDRY_AGENT_CONTAINER_IMAGE`

### `azure-ai-projects`: Skills package upload/download

Add new samples under:

`/sdk/ai/azure-ai-projects/src/samples/java/com/azure/ai/projects/`

Added files:

- `SkillsPackageSample.java`
- `SkillsPackageAsyncSample.java`
  - Python references: `sample_skills_upload_and_download.py`, `sample_skills_upload_and_download_async.py`.
  - Covers `createSkillFromPackage`, `getSkill`, `downloadSkill`, and `deleteSkill`.
  - Generate a small in-memory ZIP containing `SKILL.md` instead of checking in Python's larger `canvas-design.zip`.

Expected live env var:

- `FOUNDRY_PROJECT_ENDPOINT`

### `azure-ai-agents`: async toolbox parity

Add one new async end-to-end sample under:

`/sdk/ai/azure-ai-agents/src/samples/java/com/azure/ai/agents/toolboxes/`

Added file:

- `ToolboxesAsyncSample.java`
  - Python reference: `sample_toolboxes_crud_async.py`.
  - Leaves existing sync toolbox operation samples untouched.
  - Covers creating two toolbox versions, updating default version, retrieving toolbox/version, listing toolboxes, and deleting the toolbox.

Expected live env var:

- `FOUNDRY_PROJECT_ENDPOINT`

## Iteration 1 validation plan

- Use work-resources to load a live resource once, then reuse the loaded values for sample validation.
- Current status: work-resources was available via absolute path, and `azure-agents` was loaded once for the project endpoint. The loaded resource did not include `FOUNDRY_AGENT_CONTAINER_IMAGE`, so hosted-agent samples still need an image value before live validation.
- Required skill guidance checked:
  - `wr-load`: use `wr-load -Resource <resource>` at most once per session and cache values.
  - `run-tests`: use Maven from the relevant module; start with simple commands and only add troubleshooting flags if needed.
- Validate by compiling the affected sample sources and running the new sample `main` classes against the live Foundry project.

## Iteration 2: Java-only/generated feature candidates

Status: partially implemented. Compile validation passed. Live validation passed for list-style Data Generation Jobs and Models samples, code-based hosted agent create/download/version/delete samples, and Toolbox Search toolbox creation. Evaluator generation jobs are deferred because the live service returned `UnsupportedApiVersionValue` for SDK service version `v1` and reported supported API versions `2025-10-15-preview` and `2025-11-15-preview`.

Added samples:

- `DataGenerationJobsSample.java` and `DataGenerationJobsAsyncSample.java`
  - Covers listing data generation jobs with the preview feature flag.
  - Includes create/get/cancel/delete sample methods requiring `FOUNDRY_MODEL_NAME`.
- `ModelsSample.java` and `ModelsAsyncSample.java`
  - Covers listing latest model versions.
  - Includes optional get/create/update/delete methods requiring model asset environment variables.
- `CodeAgentSample.java` and `CodeAgentAsyncSample.java`
  - Covers code-based hosted agent create/download/create-version/delete flows without requiring `FOUNDRY_AGENT_CONTAINER_IMAGE`.
- `ToolboxSearchToolboxSample.java`
  - Covers creating a toolbox version with `ToolboxSearchPreviewTool`.
- `FabricIQSync.java` and `FabricIQAsync.java`
  - Covers constructing and using `FabricIQPreviewTool`; live validation requires `FABRIC_IQ_PROJECT_CONNECTION_ID` and `FOUNDRY_MODEL_NAME`.

Deferred:

- `EvaluatorGenerationJobsClient` / `EvaluatorGenerationJobsAsyncClient` samples until the SDK/service API version mismatch is resolved.
