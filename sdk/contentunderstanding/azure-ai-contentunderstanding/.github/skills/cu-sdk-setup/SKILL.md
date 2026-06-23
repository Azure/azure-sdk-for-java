---
name: cu-sdk-setup
description: Guide SDK users through setting up their Java environment for Azure AI Content Understanding. Use this skill when users need help installing the SDK, configuring Azure resources, deploying required models, setting environment variables, or running samples.
---

# SDK User Environment Setup for Azure AI Content Understanding (Java)

Set up your Java environment to use the Azure AI Content Understanding SDK and run samples.

> **[COPILOT INTERACTION MODEL]:** This skill is designed to be interactive. At each step marked with **[ASK USER]**, pause execution and prompt the user for input or confirmation before proceeding. Do NOT silently skip these prompts. Use the `ask_questions` tool when available.

## Prerequisites

Before starting, ensure you have:

- **JDK 8 or later** installed (JDK 11+ recommended; JDK 17/21 LTS also supported)
- **Apache Maven 3.6+** installed
- An **Azure subscription** ([create one for free](https://azure.microsoft.com/free/))
- A **Microsoft Foundry resource** in a [supported region](https://learn.microsoft.com/azure/ai-services/content-understanding/language-region-support)
- **Azure CLI** installed (recommended for `DefaultAzureCredential` auth via `az login`)

> **[COPILOT] Probe JDK/Maven runtime first (before asking):**
> Do not take the user's word for it — run these checks, then report. This prevents silent failures later during `mvn` operations.
>
> ```bash
> java -version 2>&1 | head -1
> mvn -version 2>&1 | head -1
> ```
>
> **Decision table:**
>
> | Finding | Action |
> |---|---|
> | JDK 8+ and Maven 3.6+ both present | ✓ Good to go. Proceed to the `[ASK USER]` block below. |
> | `java` missing | Report the finding, then go to the **[ASK USER] JDK/Maven install choice** block below. |
> | JDK version < 8 | Report the finding, then go to the **[ASK USER] JDK/Maven install choice** block below. |
> | `mvn` missing | Report the finding, then go to the **[ASK USER] JDK/Maven install choice** block below. |
> | Maven version < 3.6 | Report the finding, then go to the **[ASK USER] JDK/Maven install choice** block below. |
>
> **[ASK USER] JDK/Maven install choice (only when probe fails):**
> Ask the user: "JDK or Maven is missing / too old. How would you like to proceed?"
> - **Option A: Install it for me** — Agent runs the platform-appropriate install command (see below), verifies, and continues.
> - **Option B: I'll install it myself** — Agent prints the install command for the user's platform and stops. User runs it, re-opens the terminal, and tells the agent to resume.
>
> **Default install commands (Option A):**
> - **macOS** → `brew install openjdk@21 maven` (requires Homebrew; if not installed, fall back to Option B)
> - **Debian / Ubuntu / WSL** → `sudo apt update && sudo apt install -y openjdk-21-jdk maven`
> - **Windows** → `winget install Microsoft.OpenJDK.21` and `winget install Apache.Maven`
>
> **Before running Option A, confirm with the user one more time** by restating the exact command that will execute, then proceed. After install, re-run the probe to verify JDK 8+ and Maven 3.6+ before continuing.
>
> Report the detected versions back to the user in one sentence before the `[ASK USER]` block below.

> **[ASK USER] Prerequisites Check:**
> After the probe above, confirm the remaining items:
> 1. "Do you already have a **Microsoft Foundry resource** set up in Azure?" — If no, jump to **Step 5** (Azure Resource Setup) first, then return here.
> 2. "Have you already deployed the required **AI models** (GPT-4.1, GPT-4.1-mini, text-embedding-3-large) in Microsoft Foundry?" — If no, include Step 5.3 and Step 6 in the workflow.

## Package Directory

```
sdk/contentunderstanding/azure-ai-contentunderstanding
```

## How Java Samples Use Environment Variables

Java samples read configuration via `System.getenv()`. The variables must be exported in the shell before running `mvn exec:java`.

**Linux / macOS (bash / zsh):**

```bash
set -a && source .env && set +a
```

**Windows PowerShell (or PowerShell on macOS / Linux):**

```powershell
. ./load-env.ps1
```

The `load-env.ps1` helper is generated next to `.env` by the setup scripts. It strips matching surrounding single/double quotes (which the setup scripts add to make values bash-safe) before exporting, so values reach the JVM unquoted.

Alternatively, use the optional sample-run helper:

```bash
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh <SampleName> --env .env
```

## Workflow

### Step 1: Navigate to Package Directory

```bash
cd sdk/contentunderstanding/azure-ai-contentunderstanding
```

### Step 2: Pick Platform

> **[ASK USER] Platform:**
> Ask the user: "Which **platform** are you on?" with options:
> - Linux/macOS
> - Windows PowerShell
> - Windows Command Prompt
>
> Use their answer to show the correct commands throughout the rest of the setup.

> **[COPILOT] Toolchain already verified.**
> The JDK / Maven probe in the **Prerequisites** section above is the source of truth — do not re-ask the user to confirm `java -version` / `mvn -version` here. Reference command (only print if the user explicitly wants to recheck):
>
> ```bash
> java -version    # JDK 8+ (11/17/21 LTS recommended)
> mvn -version     # Maven 3.6+
> ```

### Step 3: Install SDK Dependencies

> **[ASK USER] Installation mode:**
> Ask the user: "How would you like to install the SDK?"
> - **Option A: Use the published artifact (recommended)** — Maven will download `com.azure:azure-ai-contentunderstanding` from Maven Central. Best for running samples and developing Content Understanding-based solutions using the SDK.
> - **Option B: Local build (for Content Understanding SDK contribution)** — Use this only when you are contributing to the Content Understanding SDK. Installs the current source tree into your local Maven repo so changes are reflected immediately without reinstalling.

**Option A: Download dependencies only:**
```bash
mvn dependency:resolve
```

**Option B: Local install from source:**
```bash
mvn install -DskipTests -Djacoco.skip=true
```

This compiles the SDK and all sample sources under `src/samples/java`.

> **Note:** `-Djacoco.skip=true` is required because the default build enforces a minimum test coverage ratio. When `-DskipTests` is set, no coverage data is produced and the jacoco `check` goal would fail the build. Skipping jacoco is safe for environment setup / running samples.

> **[ASK USER] Installation check:**
> After running the command, ask: "Did the build complete with `BUILD SUCCESS`?" If the user reports errors (e.g., dependency resolution failures, JDK version mismatches), help troubleshoot before continuing.

> **[COPILOT] Repeated-run behavior:**
> On repeated runs, if Maven reports that all dependencies are already downloaded (i.e., `mvn dependency:resolve` completes instantly with no downloads), the setup scripts may skip the dependency resolution step. Only rerun when dependencies are missing, the POM has changed, or the user is experiencing classpath issues.

### Step 4: Configure Environment Variables

#### 4.1 Check for Existing .env

> **[ASK USER] Existing .env check:**
> Check if `.env` already exists in the package directory.
> - If it exists: Ask "You already have a `.env` file. Would you like to **update** it or **start fresh**?"
>   - Update: Read the current file and ask which values to change.
>   - Start fresh: Overwrite with new values (confirm destructive action first).
> - If it doesn't exist: Proceed to 4.2.

**Linux/macOS:**
```bash
if [ -f ".env" ]; then
    echo "NOTE: .env file already exists"
else
    echo "No .env file found — will create one"
fi
```

**Windows PowerShell:**
```powershell
if (Test-Path ".env") {
    Write-Host "NOTE: .env file already exists"
} else {
    Write-Host "No .env file found — will create one"
}
```

#### 4.2 Gather Required Configuration

> **[ASK USER] Endpoint:**
> Ask: "Please provide your **Microsoft Foundry endpoint URL**."
> - It should look like: `https://<your-resource-name>.services.ai.azure.com/`
> - Validate: it should NOT include `api-version` or other query parameters.
> - If the user doesn't know where to find it: direct them to Azure Portal → Their Foundry resource → Keys and Endpoint.

> **[ASK USER] Authentication method:**
> Ask: "How would you like to **authenticate** with Azure?"
> - **Option A: API Key** — You'll need your `CONTENTUNDERSTANDING_KEY` from the Azure Portal.
> - **Option B: DefaultAzureCredential (recommended)** — Uses `az login`, managed identity, or other Azure credential chain. No API key needed.
>
> If Option A: Ask for the key value (retrievable at Azure Portal → Foundry resource → Keys and Endpoint → Key1 or Key2).
> If Option B: Remind the user to run `az login` before invoking samples. Leave `CONTENTUNDERSTANDING_KEY` empty.

> **[COPILOT] Probe existing model defaults on the Foundry resource:**
> Before asking the user for deployment names, probe what the resource already has configured. Use `curl` with the endpoint and credentials gathered above.
>
> ```bash
> probe_endpoint="${CONTENTUNDERSTANDING_ENDPOINT%/}"
> http_code=""
> body=""
> if [ -n "$CONTENTUNDERSTANDING_KEY" ]; then
>     probe_response=$(curl -s -w "\n%{http_code}" \
>       -H "Ocp-Apim-Subscription-Key: $CONTENTUNDERSTANDING_KEY" \
>       "$probe_endpoint/contentunderstanding/defaults?api-version=2025-11-01")
> else
>     token=$(az account get-access-token --resource https://cognitiveservices.azure.com --query accessToken -o tsv 2>/dev/null)
>     if [ -z "$token" ]; then
>         # Short-circuit: skip the curl call and go straight to the AUTH_ERROR branch below.
>         http_code="401"
>         body=""
>     else
>         probe_response=$(curl -s -w "\n%{http_code}" \
>           -H "Authorization: Bearer $token" \
>           "$probe_endpoint/contentunderstanding/defaults?api-version=2025-11-01")
>     fi
> fi
> if [ -z "$http_code" ]; then
>     http_code=$(echo "$probe_response" | tail -1)
>     body=$(echo "$probe_response" | sed '$d')
> fi
> ```
>
> Branch on the HTTP status and response body:
>
> | HTTP code | Meaning | Action |
> |-----------|---------|--------|
> | `200` + all 3 models present in `modelDeployments` | **ALL_SET** | Show the detected values and ask *"Detected existing defaults: gpt-4.1=`<A>`, gpt-4.1-mini=`<B>`, text-embedding-3-large=`<C>`. Use these? (Y/n)"*. On Y, prefill the 3 env vars and **skip Step 6** (defaults already configured). On n, fall through to the per-model prompts below. |
> | `200` + some models present | **PARTIAL** | Prefill the ones that are set. For missing models, ask per-item with the default shown below. After Step 4 completes, run Step 6 to fill the gaps. |
> | `200` + no models | **NONE** | Fall through to the per-model prompts below. Step 6 will configure them. |
> | `401` / `403` | **AUTH_ERROR** | Print a one-line warning: *"Probe unavailable (auth failed). If you're using DefaultAzureCredential, run `az login` and ensure the Cognitive Services User role is assigned. Continuing with manual entry."* Fall through to per-model prompts. |
> | other | Unexpected error | Print *"Probe failed. Continuing with manual entry."* Fall through. |
>
> Only proceed to the per-model prompts below when the probe outcome requires it.
>
> The `setup_user_env.sh` / `setup_user_env.ps1` scripts implement this probe with hardened error handling (connect/read timeouts, transport-failure fallbacks). The pseudocode above is a conceptual sketch — treat the scripts as the source of truth.

> **[ASK USER] Model deployment names (only when probe did not yield all values):**
> For each model not already prefilled from the probe, ask with a sensible default:
> - "What is your **GPT-4.1** deployment name?" (default: `gpt-4.1`) → `GPT_4_1_DEPLOYMENT`
> - "What is your **GPT-4.1-mini** deployment name?" (default: `gpt-4.1-mini`) → `GPT_4_1_MINI_DEPLOYMENT`
> - "What is your **text-embedding-3-large** deployment name?" (default: `text-embedding-3-large`) → `TEXT_EMBEDDING_3_LARGE_DEPLOYMENT`
>
> If the user prefers to configure these later, let them know they can run `Sample00_UpdateDefaults` (Step 6) anytime before using prebuilt analyzers.

> **[ASK USER] Cross-resource copy (optional):**
> Ask: "Do you plan to use **cross-resource analyzer copying** (`Sample15_GrantCopyAuth`)?"
> - If no: Skip this section.
> - If yes: Gather the following additional values:
>   1. Source Azure Resource Manager (ARM) resource ID — the full `/subscriptions/.../resourceGroups/.../providers/Microsoft.CognitiveServices/accounts/<name>` path (find it at Azure Portal → your Foundry resource → **Overview** → **JSON View** → `id`)
>   2. Source region (e.g., `eastus`)
>   3. Target endpoint URL
>   4. Target API key (or empty for DefaultAzureCredential)
>   5. Target ARM resource ID (same format as above, for the target Foundry resource)
>   6. Target region (e.g., `swedencentral`)

> **[ASK USER] Labeled training data (optional):**
> Ask: "Do you plan to use **labeled training data** (`Sample16_CreateAnalyzerWithLabels`)?"
> - If no: Skip this section. The sample still runs but creates an analyzer **without** training data.
> - If yes: Gather the following additional values:
>   1. SAS URL for the Azure Blob container that holds your uploaded label files (full URL including the `?sv=...&se=...` query). The repo ships labeled receipts at `src/samples/resources/receipt_labels/` — you upload these into a container, then generate a SAS with at least **List** and **Read** permissions (Azure Portal → Storage account → Containers → your container → **Shared access tokens**).
>   2. (Optional) Path prefix within the container (e.g., `receipt_labels/`). Leave empty if files sit at the container root.

#### 4.3 Validate Configuration

> **[ASK USER] Validate configuration:**
> After the user has provided all values, summarize the configuration and ask them to confirm:
> ```
> Here's your configuration:
>   CONTENTUNDERSTANDING_ENDPOINT = <value>
>   Authentication: DefaultAzureCredential / API Key (masked)
>   GPT_4_1_DEPLOYMENT = <value>
>   GPT_4_1_MINI_DEPLOYMENT = <value>
>   TEXT_EMBEDDING_3_LARGE_DEPLOYMENT = <value>
>
> Does this look correct? (Yes / No — let me fix something)
> ```
> Only write to `.env` after the user confirms.

#### 4.4 Write the .env File

Write the `.env` file to the package root directory (`sdk/contentunderstanding/azure-ai-contentunderstanding/.env`).

**Template (basic):**

```bash
# Azure AI Content Understanding - Environment Variables
# Generated by cu-sdk-setup skill

# Required: Your Microsoft Foundry resource endpoint
CONTENTUNDERSTANDING_ENDPOINT=https://<your-resource>.services.ai.azure.com/

# Optional: API key (leave empty to use DefaultAzureCredential via az login)
CONTENTUNDERSTANDING_KEY=

# Model deployment names (used by Sample00_UpdateDefaults)
GPT_4_1_DEPLOYMENT=gpt-4.1
GPT_4_1_MINI_DEPLOYMENT=gpt-4.1-mini
TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=text-embedding-3-large
```

**Template (with cross-resource copy):**

```bash
# Azure AI Content Understanding - Environment Variables
# Generated by cu-sdk-setup skill

# Required: Your Microsoft Foundry resource endpoint
CONTENTUNDERSTANDING_ENDPOINT=https://<your-resource>.services.ai.azure.com/

# Optional: API key (leave empty to use DefaultAzureCredential via az login)
CONTENTUNDERSTANDING_KEY=

# Model deployment names (used by Sample00_UpdateDefaults)
GPT_4_1_DEPLOYMENT=gpt-4.1
GPT_4_1_MINI_DEPLOYMENT=gpt-4.1-mini
TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=text-embedding-3-large

# Cross-resource copy settings (only for Sample15_GrantCopyAuth)
CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID=/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.CognitiveServices/accounts/{sourceAccountName}
CONTENTUNDERSTANDING_SOURCE_REGION=eastus
CONTENTUNDERSTANDING_TARGET_ENDPOINT=https://<your-target-resource>.services.ai.azure.com/
CONTENTUNDERSTANDING_TARGET_KEY=
CONTENTUNDERSTANDING_TARGET_RESOURCE_ID=/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.CognitiveServices/accounts/{targetAccountName}
CONTENTUNDERSTANDING_TARGET_REGION=swedencentral
```

**Optional add-on for `Sample16_CreateAnalyzerWithLabels`:**

Append the following lines to your `.env` if you want Sample16 to train with labeled data. If unset, the sample still runs but creates an analyzer **without** training data.

```bash
# Labeled training data (only for Sample16_CreateAnalyzerWithLabels)
# Full container SAS URL (must include ?sv=...&se=...). Required for labeled training.
CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL=https://<account>.blob.core.windows.net/<container>?sv=...&se=...

# Optional path prefix within the container. Omit if files are at the container root.
CONTENTUNDERSTANDING_TRAINING_DATA_PREFIX=receipt_labels/
```

### Step 5: Azure Resource Setup (if not done)

> **[NOTE]:** Only guide the user through this step if they indicated during the prerequisites check that they do NOT yet have a Microsoft Foundry resource. Otherwise, skip to Step 6.

#### 5.1 Create Microsoft Foundry Resource

1. Go to [Azure Portal](https://portal.azure.com/)
2. Create a **Microsoft Foundry resource** in a [supported region](https://learn.microsoft.com/azure/ai-services/content-understanding/language-region-support)
3. Navigate to **Resource Management** → **Keys and Endpoint**
4. Copy the **Endpoint** URL and optionally a **Key**

> **[ASK USER] Resource created:**
> After guiding the user to create the resource, ask: "Have you created the Microsoft Foundry resource? Please share the **endpoint URL** so we can continue with configuration."

#### 5.2 Grant Cognitive Services User Role

This role is required even if you own the resource:

1. In your Foundry resource, go to **Access Control (IAM)**
2. Click **Add** → **Add role assignment**
3. Select **Cognitive Services User** role
4. Assign it to yourself

> **[ASK USER] Role assigned:**
> Ask: "Have you assigned the **Cognitive Services User** role to yourself? This is required even if you own the resource."

#### 5.3 Deploy Required Models

| Analyzer Type | Required Models |
|--------------|-----------------|
| `prebuilt-documentSearch`, `prebuilt-imageSearch`, `prebuilt-audioSearch`, `prebuilt-videoSearch` | gpt-4.1-mini, text-embedding-3-large |
| Other prebuilt analyzers (invoice, receipt, etc.) | gpt-4.1, text-embedding-3-large |

**To deploy a model:**
1. In Microsoft Foundry → **Deployments** → **Deploy model** → **Deploy base model**
2. Search and deploy: `gpt-4.1`, `gpt-4.1-mini`, `text-embedding-3-large`
3. Note deployment names (recommendation: use the model name as the deployment name)

> **[ASK USER] Models deployed:**
> Ask: "Have you deployed the required models? Please provide the **deployment names** you used for each (GPT-4.1, GPT-4.1-mini, text-embedding-3-large)." Use these names to populate the `.env` file.

### Step 6: Load .env and Configure Model Defaults (One-Time Setup)

#### 6.1 Load .env into the current shell

```bash
set -a && source .env && set +a
```

> **[ASK USER] Verify loaded:**
> Ask the user to verify the variables are set:
> ```bash
> echo $CONTENTUNDERSTANDING_ENDPOINT
> ```
> Ask: "Does the endpoint value look correct?"

#### 6.2 Run Sample00_UpdateDefaults

> **[COPILOT] Skip condition:**
> If the Step 4.2 probe returned **ALL_SET** and the user accepted the detected values, defaults are already configured on the Foundry resource — skip this step and tell the user *"Your Foundry resource already has model defaults configured; skipping Step 6.2."* Otherwise continue below.

> **[ASK USER] Run model defaults?:**
> Ask: "Would you like to run `Sample00_UpdateDefaults` now to configure model defaults? This is a **one-time setup** per Microsoft Foundry resource. (Yes / Skip for now)"
> - If yes, ensure deployment name env vars are set, then run the sample.
> - If no, let them know they'll need to run it before using prebuilt analyzers.

```bash
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample00_UpdateDefaults" \
  -Dexec.classpathScope=test \
  -Djacoco.skip=true -q
```

> **Note:** `-Dexec.classpathScope=test` is required because sample sources live under `src/samples/java` and are compiled into the test classpath. Without it you will get `ClassNotFoundException: com.azure.ai.contentunderstanding.samples.Sample00_UpdateDefaults`.

This is a **one-time setup per Microsoft Foundry resource**.

### Step 7: Run Samples

> **[ASK USER] Which samples?:**
> Ask: "Which sample would you like to run first?" with options:
> - `Sample01_AnalyzeBinary` — Analyze a local PDF (quickest; completes in under a minute)
> - `Sample02_AnalyzeUrl` — Full demo: document + video + audio + image from URLs (runs several analyses; takes a few minutes, please be patient)
> - `Sample03_AnalyzeInvoice` — Extract invoice fields
> - Other — Let me see the full list
> - Skip — I'll run samples on my own later
>
> If the user picks "Other", list available samples from `src/samples/java/com/azure/ai/contentunderstanding/samples/`.
>
> **[COPILOT] Timing note (do not parrot verbatim to user):** `Sample02_AnalyzeUrl` runs multiple sequential LROs (document + video + audio + image, with multiple content-range variants). Video/audio chapter generation is slow on the service side, so total runtime can be on the order of 15+ minutes today. Do not interpret quiet periods (no stdout for several minutes during a video/audio LRO) as a hang. Only consider killing if there is **no new stdout for 5+ minutes** AND no active HTTP traffic. When talking to the user, prefer phrasing like "takes a few minutes" or "please be patient" rather than citing exact large minute counts.

**Sync sample:**
```bash
set -a && source .env && set +a
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl" \
  -Dexec.classpathScope=test \
  -Djacoco.skip=true -q
```

**Async sample (same package, `*Async` suffix):**
```bash
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrlAsync" \
  -Dexec.classpathScope=test \
  -Djacoco.skip=true -q
```

For a more fluent experience, use the sample-run helper skill:
```bash
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh Sample02_AnalyzeUrl --env .env
```

> **[ASK USER] Sample result:**
> After running a sample, ask: "Did the sample run successfully? Would you like to run another sample or are you all set?"

## Automated Setup Script (Linux/macOS)

Run the interactive setup script that handles Steps 2–4 automatically:

```bash
# From the package directory
cd sdk/contentunderstanding/azure-ai-contentunderstanding
.github/skills/cu-sdk-setup/scripts/setup_user_env.sh
```

The script will:
1. Check `java` and `mvn` prerequisites (with offer to install if missing)
2. Install SDK dependencies (skip if already resolved; prompt for `mvn dependency:resolve` vs. `mvn install -DskipTests -Djacoco.skip=true`)
3. Create `.env` (without overwriting existing) and interactively prompt for:
   - `CONTENTUNDERSTANDING_ENDPOINT`
   - Authentication method (DefaultAzureCredential or API key)
   - Probe existing model defaults on the Foundry resource (skip manual entry if all set)
   - Model deployment names (with sensible defaults, pre-filled from probe when available)
   - Optional cross-resource copy vars (Sample15)
4. Print next-step commands for loading `.env`, running `Sample00_UpdateDefaults`, and running samples.

**Windows PowerShell:**
```powershell
cd sdk\contentunderstanding\azure-ai-contentunderstanding
.github\skills\cu-sdk-setup\scripts\setup_user_env.ps1
```

> **Note:** The script does **not** load `.env` into your shell for you — you must still load it before invoking `mvn exec:java`, because Java samples read values via `System.getenv()`. Use `set -a && source .env && set +a` in bash, or `. ./load-env.ps1` in PowerShell.

### Manual Quick Setup

If you prefer to run steps manually:

```bash
cd sdk/contentunderstanding/azure-ai-contentunderstanding

# Verify toolchain
java -version
mvn -version

# Resolve dependencies (Option A) — or run the Option B `mvn install` command from Step 3.
mvn dependency:resolve

# Create .env if absent (no env.sample exists — use the template from Step 4.4)
if [ ! -f ".env" ]; then
    cat > .env <<'EOF'
CONTENTUNDERSTANDING_ENDPOINT=https://<your-resource>.services.ai.azure.com/
CONTENTUNDERSTANDING_KEY=
GPT_4_1_DEPLOYMENT=gpt-4.1
GPT_4_1_MINI_DEPLOYMENT=gpt-4.1-mini
TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=text-embedding-3-large
EOF
    echo "Created .env — please edit and configure required variables"
else
    echo "WARNING: .env already exists — skipping creation"
fi

# Load .env into the current shell before running samples
set -a && source .env && set +a
```

## Environment Variable Reference

Required for all samples:

- `CONTENTUNDERSTANDING_ENDPOINT` — Microsoft Foundry resource endpoint URL.
- `CONTENTUNDERSTANDING_KEY` — API key. Leave empty to use `DefaultAzureCredential` (run `az login` first).

Required for `Sample00_UpdateDefaults` (one-time model mapping):

- `GPT_4_1_DEPLOYMENT` (default: `gpt-4.1`)
- `GPT_4_1_MINI_DEPLOYMENT` (default: `gpt-4.1-mini`)
- `TEXT_EMBEDDING_3_LARGE_DEPLOYMENT` (default: `text-embedding-3-large`)

Required for `Sample15_GrantCopyAuth` (cross-resource analyzer copy) only:

- `CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID`, `CONTENTUNDERSTANDING_SOURCE_REGION`
- `CONTENTUNDERSTANDING_TARGET_ENDPOINT`, `CONTENTUNDERSTANDING_TARGET_KEY` (optional)
- `CONTENTUNDERSTANDING_TARGET_RESOURCE_ID`, `CONTENTUNDERSTANDING_TARGET_REGION`

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `java: command not found` | Install a JDK 8+ (Microsoft Build of OpenJDK or Temurin) and ensure `JAVA_HOME` is set. |
| `mvn: command not found` | Install Maven 3.6+ and add it to `PATH`. |
| `CONTENTUNDERSTANDING_ENDPOINT` is null at runtime | Load `.env` in the same terminal before `mvn exec:java`: `set -a && source .env && set +a` (bash) or `. ./load-env.ps1` (PowerShell). |
| `ClassNotFoundException: ...samples.SampleXX_...` | Add `-Dexec.classpathScope=test` to the `mvn exec:java` command. Samples live under `src/samples/java` (test classpath). |
| `jacoco-maven-plugin:...:check` fails after `mvn install -DskipTests` | Add `-Djacoco.skip=true`. Skipping tests produces no coverage data, which fails the coverage check. |
| `Access denied` / 401 errors | Check API key is correct, or run `az login` if using DefaultAzureCredential. Verify the `Cognitive Services User` role is assigned. |
| `Model deployment not found` | Deploy required models in Microsoft Foundry and run `Sample00_UpdateDefaults`. |
| Changes to `.env` not taking effect | Re-run the loader (`set -a && source .env && set +a` in bash, or `. ./load-env.ps1` in PowerShell) — changes are not auto-reloaded. |
| `.env` committed to git | Add `.env` to `.gitignore` — never commit credentials. |
| Probe returns 401/403 even after `az login` | Assign the `Cognitive Services User` role on the Foundry resource to your account in Azure Portal → Access Control (IAM). |
| `load-env.ps1` reports `'.env' not found` | Run the loader from the package root (`sdk/contentunderstanding/azure-ai-contentunderstanding`), not a subdirectory. |
| Re-running the setup script doesn’t change my `.env` | The script never overwrites an existing `.env`. Delete it first (`rm .env`) and re-run, or edit it manually. |
| `ClassNotFoundException` after a clean tree change despite the script reporting “deps already resolved” | Stale marker. Delete `target/.cu-setup-deps-ok` (or run `mvn clean`) and re-run the setup script. |

## Related Skills

- `cu-sdk-sample-run` — Run individual samples (including `Sample00_UpdateDefaults` for model deployment setup)
- `cu-sdk-common-knowledge` — Domain knowledge for Content Understanding concepts

## Additional Resources

- [SDK README](../../../README.md) - Full documentation
- [Samples README](../../../src/samples/README.md) - Sample descriptions
- [Product Documentation](https://learn.microsoft.com/azure/ai-services/content-understanding/)
- [Prebuilt Analyzers](https://learn.microsoft.com/azure/ai-services/content-understanding/concepts/prebuilt-analyzers)
