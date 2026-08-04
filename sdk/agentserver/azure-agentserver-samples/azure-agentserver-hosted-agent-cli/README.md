# Hosted-agent deployment CLI (vnext)

A compact Java command-line tool that provides general **hosted-agent deployment services** for Azure AI
Foundry (vnext experience). Argument parsing uses [JCommander](https://jcommander.org) (the same library
the repo's perf/benchmark tooling relies on) and every operation is driven through the
[`com.azure:azure-ai-agents`](https://central.sonatype.com/artifact/com.azure/azure-ai-agents) Java SDK.
There are no raw REST calls.

| Command  | Description                                                                    |
|----------|--------------------------------------------------------------------------------|
| `deploy` | Deploy a container image as a hosted agent version, wait until it is `ACTIVE`. |
| `status` | Show an agent and the status of every one of its versions.                     |
| `list`   | List every hosted agent in the project.                                        |
| `delete` | Delete all versions of an agent and then the agent itself.                     |
| `logs`   | Show container logs: stream a live session, or dump Application Insights history. |

## Prerequisites

- An Azure AI Foundry project and permission to manage hosted agents in it.
- Logged in with a credential `DefaultAzureCredential` can resolve (for example `az login`).
- The container image built and pushed to a registry the project can pull from (for `deploy`).

## Build

```bash
../../mvnw -q package -DskipTests \
  -pl azure-agentserver-samples/azure-agentserver-hosted-agent-cli -am
```

This produces a runnable jar under `target/`:
`azure-agentserver-hosted-agent-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar`.

## Usage

```bash
JAR=target/azure-agentserver-hosted-agent-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar
ENDPOINT="https://<account>.services.ai.azure.com/api/projects/<project>"

# Deploy (idempotent — creates the agent or adds a new version)
java -jar "$JAR" deploy \
  --endpoint "$ENDPOINT" \
  --name my-agent \
  --image yourregistry.azurecr.io/my-agent:latest \
  --size large \
  --model gpt-5.4 \
  --env LOG_LEVEL=debug

# Status of a single agent and all its versions
java -jar "$JAR" status --endpoint "$ENDPOINT" --name my-agent

# List every hosted agent in the project
java -jar "$JAR" list --endpoint "$ENDPOINT"

# Delete every version and then the agent
java -jar "$JAR" delete --endpoint "$ENDPOINT" --name my-agent

# Stream live console logs (latest version, a fresh session is created if none is given)
java -jar "$JAR" logs --endpoint "$ENDPOINT" --name my-agent

# Dump recent Application Insights history and exit (traces, exceptions, requests, dependencies or all)
java -jar "$JAR" logs --endpoint "$ENDPOINT" --name my-agent \
  --source insights --app-insights "$APP_INSIGHTS_RESOURCE_ID" --log-type traces --since 1h --limit 20
```

Run with no arguments to print JCommander's generated usage for every command and option.

### Options

| Flag            | Commands       | Default   | Description                                                                        |
|-----------------|----------------|-----------|------------------------------------------------------------------------------------|
| `--endpoint`    | all            | –         | Azure AI Foundry project endpoint (required).                                      |
| `--name`        | all but `list` | –         | Hosted-agent name (required).                                                      |
| `--image`       | `deploy`       | –         | Container image reference (required).                                              |
| `--size`        | `deploy`       | `medium`  | Sandbox size: `small` (0.5 vCPU/1Gi), `medium` (1 vCPU/2Gi), `large` (2 vCPU/4Gi). |
| `--model`       | `deploy`       | –         | Model deployment; added as the `MODEL_DEPLOYMENT_NAME` container env var.          |
| `--subscription`| `deploy`       | –         | Subscription id of the AI Services account; enables the post-deploy Azure OpenAI permission check. |
| `--grant-openai-access` | `deploy` | `false`  | When `true`, grant the agent identity the Azure OpenAI role if missing (requires `--subscription`); prints the manual fix if the grant fails. |
| `--description` | `deploy`       | a default | Version description.                                                               |
| `--env`         | `deploy`       | –         | Container env var `KEY=VALUE` (repeatable).                                        |
| `--source`      | `logs`         | `stream`  | Log source: `stream` (live SSE console session) or `insights` (dump Application Insights history and exit). |
| `--version`     | `logs`         | latest    | (`stream`) Agent version to stream logs from.                                      |
| `--session`     | `logs`         | new       | (`stream`) Session to stream; a new one is created if omitted.                     |
| `--app-insights`| `logs`         | –         | (`insights`) ARM resource id of the Application Insights component to query.        |
| `--log-type`    | `logs`         | `traces`  | (`insights`) Telemetry table: `traces`, `exceptions`, `requests`, `dependencies` or `all`. |
| `--since`       | `logs`         | `1h`      | (`insights`) Look-back window such as `30m`, `1h` or `2d`.                          |
| `--limit`       | `logs`         | `20`      | (`insights`) Maximum number of rows to return.                                     |
| `--config`      | all            | –         | YAML file supplying any of the above (see below).                                  |

## Configuration file

Instead of (or in addition to) command-line flags, pass a YAML file with `--config <file>`. The keys mirror
the option names (without the leading `--`); the sub-command is always given on the command line, not in the
file. The nested `env` block may be a map or a list of `KEY=VALUE` strings. See
[`config.example.yaml`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agentserver/azure-agentserver-samples/config.example.yaml) in the samples root.

```yaml
endpoint: https://<account>.services.ai.azure.com/api/projects/<project>
name: my-agent
image: yourregistry.azurecr.io/my-agent:latest
size: large
model: gpt-5.4
env:
    LOG_LEVEL: debug
```

```bash
# The command comes from the command line; the file supplies the options
java -jar "$JAR" deploy --config ../config.example.yaml

# Any flag on the command line overrides the file (here, size large -> small)
java -jar "$JAR" deploy --config ../config.example.yaml --size small

# The same file works for other commands
java -jar "$JAR" status --config ../config.example.yaml
```

When you launch through [`hosted-agent-cli.sh`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agentserver/azure-agentserver-samples/hosted-agent-cli.sh) in the samples root, this
project's `config.yaml` is picked up automatically — `./hosted-agent-cli.sh deploy` behaves like
`./hosted-agent-cli.sh deploy --config azure-agentserver-hosted-agent-cli/config.yaml`. Pass an explicit
`--config <file>` to use a different file.

## Notes

- **Preview features.** Agent endpoints and sessions are preview features that only work with hosted agents,
  so the client is built with `allowPreview(true)`.
- **Idempotent deployment.** Deploying is safe to repeat. If the agent does not exist it is created; if it
  already exists a new version is added. The service assigns version numbers and deduplicates identical
  definitions, so re-running with a **new** image produces a new version while the **same** image reuses the
  current one.
- **RBAC.** A hosted agent that calls Azure OpenAI runs under a managed identity that needs the
  `Cognitive Services OpenAI User` role on the AI Services account. See the
  [`azure-ai-agents-sdk-client-sample`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agentserver/azure-agentserver-samples/azure-ai-agents-sdk-client-sample) for the exact role assignment.
- **Deploy-time permission check.** When `--subscription` is supplied, `deploy` verifies (via the Azure
  Resource Manager Java SDKs) that the new agent's managed identity already holds an Azure OpenAI data-plane
  role on the AI Services account. If it does not, it prints the exact `az role assignment create` command to
  fix it — so the missing permission is caught at deploy time instead of as a runtime 401. Add
  `--grant-openai-access true` to have `deploy` create the role assignment automatically (falling back to the
  printed fix if it lacks permission to do so). Log streaming also watches for the same authorization failure
  and prints the fix if it appears. The check is best-effort and never fails the deploy.
- **HTTP client.** Uses the OkHttp-based Azure core HTTP client and excludes `azure-core-http-netty`, because
  the agentserver parent POM strips the transitive Netty dependencies.
