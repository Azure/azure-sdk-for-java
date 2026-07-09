# Financial-jersey hosted-agent SDK client sample

This sample shows the **client side** of Azure AI Foundry hosted agents. It takes the
azure-agentserver-langchain4j-financial-jersey-sample Agent Server, packaged as a container image, and:

1. Deploys it as a **hosted agent** in an Azure AI Foundry project.
2. Waits for the agent version to become `ACTIVE`.
3. Creates a session pinned to that version.
4. Configures the agent endpoint to route all traffic to the version over the Responses protocol.
5. Invokes the agent with a financial question using an **agent-scoped OpenAI Responses client**.
6. Prints the response and then deletes the session and agent version.

Everything is driven through the [
`com.azure:azure-ai-agents`](https://central.sonatype.com/artifact/com.azure/azure-ai-agents)
Java SDK — there are no raw REST calls or shell scripts.

## How it differs from the other financial samples

The sibling `azure-agentserver-langchain4j-financial-*` modules implement the **server** (the agent
that runs inside the container). This module is the **caller** that deploys and drives one of those
servers as a hosted agent from Java.

## Prerequisites

- An Azure AI Foundry project and permission to create hosted agents in it.
- The financial-jersey container image built and pushed to a registry the project can pull from.
- Logged in with a credential `DefaultAzureCredential` can resolve (for example `az login`).
- A model deployment (for example `gpt-5.4`) available in the project.

### Build and push the agent image

From the financial-jersey sample directory:

```bash
cd ../financial-agent/azure-agentserver-langchain4j-financial-jersey-sample
../../../mvnw -q package -DskipTests
docker build --platform linux/amd64 \
  -t <acr>.azurecr.io/lc4jfinancialjersey:latest .
docker push <acr>.azurecr.io/lc4jfinancialjersey:latest
```

## Configuration

The sample is configured entirely through environment variables:

| Variable                        | Required | Default                            | Description                                                                      |
|---------------------------------|----------|------------------------------------|----------------------------------------------------------------------------------|
| `FOUNDRY_PROJECT_ENDPOINT`      | yes      | –                                  | The Azure AI Foundry project endpoint.                                           |
| `FOUNDRY_AGENT_CONTAINER_IMAGE` | yes      | –                                  | The financial-jersey container image reference (tag or digest).                  |
| `MODEL_DEPLOYMENT_NAME`         | no       | `gpt-5.4`                          | Model deployment the financial agent uses. Injected into the container.          |
| `AGENT_NAME`                    | no       | `java-financial-jersey-sdk-sample` | The hosted-agent name.                                                           |
| `AGENT_CPU`                     | no       | `2`                                | vCPU allocation for the hosted agent.                                            |
| `AGENT_MEMORY`                  | no       | `4Gi`                              | Memory allocation for the hosted agent.                                          |
| `PROMPT`                        | no       | a sample expense-summary prompt    | The question to send to the financial agent.                                     |
| `SKIP_CLEANUP`                  | no       | `false`                            | If `true`, leaves the agent version and session in place after the run.          |
| `AZURE_SUBSCRIPTION_ID`         | no       | –                                  | Subscription hosting the AI Services account. Used in the printed RBAC commands. |

## Run

The quickest way is the helper script, which builds the sample and runs it (and can optionally
build+push the agent image first with `BUILD_IMAGE=true`):

```bash
FOUNDRY_PROJECT_ENDPOINT="https://<account>.services.ai.azure.com/api/projects/<project>" \
FOUNDRY_AGENT_CONTAINER_IMAGE="<acr>.azurecr.io/lc4jfinancialjersey:latest" \
MODEL_DEPLOYMENT_NAME="gpt-5.4" \
./run-sample.sh
```

`run-sample.sh` automatically sources a `.env` file next to it if present. The provided `.env`
uses `${VAR:-default}` defaults, so any variable you export before running still takes precedence.
Edit `.env` to set your endpoint, image, and (optionally) `AZURE_SUBSCRIPTION_ID`, then just run
`./run-sample.sh` with no arguments.

Or build and run manually:

```bash
../../mvnw -q package -DskipTests \
  -pl azure-agentserver-samples/azure-ai-agents-sdk-client-sample -am

export FOUNDRY_PROJECT_ENDPOINT="https://<account>.services.ai.azure.com/api/projects/<project>"
export FOUNDRY_AGENT_CONTAINER_IMAGE="<acr>.azurecr.io/lc4jfinancialjersey:latest"
export MODEL_DEPLOYMENT_NAME="gpt-5.4"

java -jar target/azure-ai-agents-sdk-client-sample-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

## Notes

- **Preview features.** Agent endpoints and sessions are preview features that only work with hosted
  agents, so the `AgentsClientBuilder` is built with `allowPreview(true)`.
- **RBAC.** The hosted agent runs under a managed identity that needs access to the model. Once the
  agent version is `ACTIVE`, the sample reads its `instanceIdentity.principalId` and prints the exact
  `az role assignment create` command to run *before* the responses call — for example:

  ```bash
  az account set --subscription <subscription-id>

  ACCOUNT_ID=$(az cognitiveservices account list --subscription <subscription-id> \
    --query "[?name=='<account>'].id | [0]" -o tsv)

  az role assignment create \
    --assignee-object-id <agent-principal-id> \
    --assignee-principal-type ServicePrincipal \
    --role "Cognitive Services OpenAI User" \
    --scope "$ACCOUNT_ID"
  ```

  If invocation returns `500`/`server_error`, this missing RBAC is the usual cause (the container logs
  a `401 PermissionDenied`). Grant the role, wait ~1-2 minutes for propagation, and re-run.
- **Keeping resources for debugging.** Set `SKIP_CLEANUP=true` to leave the agent version and session in
  place after the run (for example to grant RBAC and re-invoke, or to stream container logs). By default
  the sample deletes both.
- **Idempotent deployment.** Deploying is safe to repeat. If the agent does not exist it is created;
  if it already exists a new version is added. The service assigns version numbers and deduplicates
  identical definitions, so re-running with a **new** container image produces a new version, while
  re-running with the **same** image reuses the current one. The sample logs which path it took.
- **HTTP client.** The sample uses the OkHttp-based Azure core HTTP client (`azure-core-http-okhttp`)
  and excludes `azure-core-http-netty`, because the agentserver parent POM strips the transitive Netty
  dependencies from the Netty client.
- **OpenAI SDK version.** `azure-ai-agents` 2.2.0 is built against `openai-java` 4.14.0, so this sample
  pins that version explicitly to stay binary-compatible with the agent-scoped OpenAI client.
