# Testing azure-ai-discovery

The Discovery data-plane tests run in **playback** mode by default against recorded sessions managed by the test proxy (see `assets.json`), so **no live Azure resources are required** for CI or a local `mvn test`.

## Running against live resources

To run the tests in **live** or **record** mode you must point them at **pre-provisioned** Discovery resources. Discovery Workspace and Bookshelf services — and the projects, investigations, tools, node pools, and storage assets they contain — are data-plane resources that are **not** created by `test-resources.json`; that template only surfaces externally supplied values as outputs. Provide the following environment variables, each referencing an existing resource:

| Environment variable | Description |
| --- | --- |
| `AZURE_DISCOVERY_WORKSPACE_ENDPOINT` | Endpoint of a pre-provisioned Discovery Workspace service. |
| `AZURE_DISCOVERY_BOOKSHELF_ENDPOINT` | Endpoint of a pre-provisioned Discovery Bookshelf service. |
| `AZURE_DISCOVERY_PROJECT_NAME` | Name of a pre-provisioned project used by Workspace tests. |
| `AZURE_DISCOVERY_INVESTIGATION_NAME` | Name of a pre-provisioned investigation used by Workspace tests. |
| `AGENT_NAME` | Name of a pre-provisioned agent used by Workspace tests. |
| `KNOWLEDGE_BASE_NAME` | Name of a pre-provisioned knowledge base used by Bookshelf tests. |
| `TOOL_ID` | Resource ID of a pre-provisioned Discovery tool used by Tools tests. |
| `NODE_POOL_ID` | Resource ID of a pre-provisioned node pool used by Tools tests. |
| `STORAGE_ASSET_ID` | Resource ID of a pre-provisioned storage asset used by Bookshelf tests. |
| `USER_ASSIGNED_IDENTITY` | Resource ID of a user-assigned managed identity used by Bookshelf tests. |

These names match the parameter defaults and outputs declared in `test-resources.json`. When a variable is not set, the test base falls back to a sanitized placeholder that is only valid for playback.

## Authentication

Tests authenticate with `DefaultAzureCredential`. Before a live/record run, sign in with the Azure CLI (`az login`) or set the standard `AZURE_CLIENT_ID` / `AZURE_TENANT_ID` / `AZURE_CLIENT_SECRET` variables, and ensure the identity has access to the workspace and bookshelf resources above.

## Test mode

Set `AZURE_TEST_MODE` to `LIVE` or `RECORD` to run against the resources above; leave it unset (or `PLAYBACK`) to use the recordings.
