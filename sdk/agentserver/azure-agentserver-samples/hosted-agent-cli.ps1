# ------------------------------------
# Copyright (c) Microsoft Corporation.
# Licensed under the MIT License.
# ------------------------------------
#
# Build (once) and run the hosted-agent deployment CLI, forwarding all arguments
# straight through to the tool. This launcher lives in the samples root and
# drives the "azure-agentserver-hosted-agent-cli" sub-project.
#
# Usage:
#   ./hosted-agent-cli.ps1 <command> [options]
#
# Examples:
#   ./hosted-agent-cli.ps1 list --endpoint "https://<account>.services.ai.azure.com/api/projects/<project>"
#
#   ./hosted-agent-cli.ps1 deploy `
#     --endpoint "https://<account>.services.ai.azure.com/api/projects/<project>" `
#     --name my-agent `
#     --image yourregistry.azurecr.io/my-agent:latest `
#     --model gpt-5.4 --env LOG_LEVEL=debug
#
#   ./hosted-agent-cli.ps1 status --endpoint $Endpoint --name my-agent
#   ./hosted-agent-cli.ps1 logs   --endpoint $Endpoint --name my-agent
#
# Environment variables:
#   SKIP_BUILD   If "true", skip the Maven build and use the existing jar.
#
# Config auto-detection:
#   If "azure-agentserver-hosted-agent-cli/config.yaml" exists and no "--config"
#   flag was passed, it is used automatically (equivalent to
#   "--config azure-agentserver-hosted-agent-cli/config.yaml").

[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Args
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ScriptDir

# The CLI sub-project this launcher builds and runs.
$CliDir = Join-Path $ScriptDir 'azure-agentserver-hosted-agent-cli'

$JarGlob = Join-Path $CliDir 'target/azure-agentserver-hosted-agent-cli-*-jar-with-dependencies.jar'
$DefaultConfig = Join-Path $CliDir 'config.yaml'

# ── Build the runnable jar (unless SKIP_BUILD=true) ──────────────────
if ($env:SKIP_BUILD -ne 'true') {
    [Console]::Error.WriteLine('=== Building hosted-agent CLI ===')

    $mvnwCmd = Join-Path $ScriptDir '../mvnw.cmd'
    $mvnwSh = Join-Path $ScriptDir '../mvnw'
    if ($IsWindows -and (Test-Path $mvnwCmd)) {
        & $mvnwCmd -q -f (Join-Path $CliDir 'pom.xml') package -DskipTests
    }
    else {
        & $mvnwSh -q -f (Join-Path $CliDir 'pom.xml') package -DskipTests
    }
    if ($LASTEXITCODE -ne 0) {
        [Console]::Error.WriteLine('Error: Maven build failed.')
        exit $LASTEXITCODE
    }
}

# ── Locate the jar ───────────────────────────────────────────────────
$Jar = Get-ChildItem -Path $JarGlob -File -ErrorAction SilentlyContinue |
    Select-Object -First 1 -ExpandProperty FullName

if (-not $Jar -or -not (Test-Path $Jar)) {
    [Console]::Error.WriteLine('Error: runnable jar not found under target/. Run without SKIP_BUILD=true first.')
    exit 1
}

# ── Auto-detect config.yaml (unless a --config flag was already given) ─
$ForwardArgs = @()
if ($Args) { $ForwardArgs += $Args }

if ((Test-Path $DefaultConfig) -and $ForwardArgs.Count -gt 0 -and ($ForwardArgs -notcontains '--config')) {
    [Console]::Error.WriteLine("=== Using $DefaultConfig ===")
    $ForwardArgs += @('--config', $DefaultConfig)
}

# ── Run, forwarding all arguments to the CLI ─────────────────────────
& java -jar $Jar @ForwardArgs
exit $LASTEXITCODE
