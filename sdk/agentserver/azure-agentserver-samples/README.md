# Azure AI Agent Server samples

This directory contains the Azure AI Agent Server samples, including the framework
integrations, the financial sample agents, and a command-line tool for deploying and
operating **hosted agents** on Azure AI Foundry (vnext).

## Hosted-agent launcher

Two convenience launchers in this directory build (once) and run the
[hosted-agent deployment CLI](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agentserver/azure-agentserver-samples/azure-agentserver-hosted-agent-cli/README.md), forwarding
all arguments straight through to the tool:

| Script                 | Shell                                       |
|------------------------|---------------------------------------------|
| `hosted-agent-cli.sh`  | Bash / Zsh (Linux, macOS, WSL)              |
| `hosted-agent-cli.ps1` | PowerShell (Windows, cross-platform `pwsh`) |

Both are thin wrappers around the CLI sub-project
(`azure-agentserver-hosted-agent-cli`). They:

1. Build the runnable jar with the repo's Maven wrapper (`../mvnw`) unless
   `SKIP_BUILD=true`.
2. Locate the built `*-jar-with-dependencies.jar` under the sub-project's `target/`.
3. Auto-detect `azure-agentserver-hosted-agent-cli/config.yaml` (see
   [Configuration](#configuration)).
4. Run `java -jar <jar> <your args>`.

### Usage

```bash
# Bash / Zsh
./hosted-agent-cli.sh <command> [options]
```

```powershell
# PowerShell
./hosted-agent-cli.ps1 <command> [options]
```

The available commands (see the
[CLI README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agentserver/azure-agentserver-samples/azure-agentserver-hosted-agent-cli/README.md) for full option details):

| Command  | Description                                                                       |
|----------|-----------------------------------------------------------------------------------|
| `deploy` | Deploy a container image as a hosted agent version and wait until it is `ACTIVE`. |
| `status` | Show an agent and the status of every one of its versions.                        |
| `list`   | List every hosted agent in the project.                                           |
| `delete` | Delete all versions of an agent and then the agent itself.                        |
| `logs`   | Stream a live console session, or dump Application Insights history.              |

### Examples

```bash
ENDPOINT="https://<account>.services.ai.azure.com/api/projects/<project>"

# Deploy (creates the agent, or adds a new version to an existing one)
./hosted-agent-cli.sh deploy \
  --endpoint "$ENDPOINT" \
  --name my-agent \
  --image yourregistry.azurecr.io/my-agent:latest \
  --size large \
  --model gpt-5.4 \
  --env LOG_LEVEL=debug

# Status of a single agent and all its versions
./hosted-agent-cli.sh status --endpoint "$ENDPOINT" --name my-agent

# List every hosted agent in the project
./hosted-agent-cli.sh list --endpoint "$ENDPOINT"

# Stream live console logs (latest version; a fresh session is created if none given)
./hosted-agent-cli.sh logs --endpoint "$ENDPOINT" --name my-agent

# Delete every version and then the agent
./hosted-agent-cli.sh delete --endpoint "$ENDPOINT" --name my-agent
```

Run with no command to print the CLI's generated usage for every command and option.

### Prerequisites

- A JDK (8+) and the repo's Maven wrapper (`../mvnw`, invoked automatically).
- An Azure AI Foundry project and permission to manage hosted agents in it.
- Logged in with a credential `DefaultAzureCredential` can resolve (for example
  `az login`).
- For `deploy`: the container image built and pushed to a registry the project can pull
  from.

### Configuration

Instead of (or in addition to) command-line flags, pass a YAML file with
`--config <file>`. The keys mirror the option names without the leading `--`; the
sub-command is always given on the command line. See
[`config.example.yaml`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agentserver/azure-agentserver-samples/config.example.yaml) for a fully commented template.

If `azure-agentserver-hosted-agent-cli/config.yaml` exists and no `--config` flag is
passed, the launchers use it automatically, so:

```bash
./hosted-agent-cli.sh deploy
```

behaves like:

```bash
./hosted-agent-cli.sh deploy --config azure-agentserver-hosted-agent-cli/config.yaml
```

Any flag on the command line overrides the value from the file.

### Environment variables

| Variable     | Effect                                                      |
|--------------|-------------------------------------------------------------|
| `SKIP_BUILD` | If `true`, skip the Maven build and reuse the existing jar. |

```bash
# Rebuild once, then iterate quickly without rebuilding
./hosted-agent-cli.sh status --endpoint "$ENDPOINT" --name my-agent
SKIP_BUILD=true ./hosted-agent-cli.sh list --endpoint "$ENDPOINT"
```

## More

- [Hosted-agent CLI reference](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/agentserver/azure-agentserver-samples/azure-agentserver-hosted-agent-cli/README.md) — full
  command and option documentation.
