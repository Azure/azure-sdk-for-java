---
name: cu-sdk-setup
description: Guide SDK users through setting up their Java environment for Azure AI Content Understanding. Use this skill when users need help installing the SDK, configuring Azure resources, deploying required models, setting environment variables, or running samples.
---

# SDK User Environment Setup for Azure AI Content Understanding (Java)

Set up your Java environment to use the Azure AI Content Understanding SDK and run samples.

> **Note:** This skill is for SDK users who want to run samples and use the SDK. For SDK development (regenerating code, running tests, pushing recordings), see the sibling `sdkinternal-java-*` skills.

> **[COPILOT INTERACTION MODEL]:** This skill is designed to be interactive. At each step marked with **[ASK USER]**, pause execution and prompt the user for input or confirmation before proceeding. Do NOT silently skip these prompts. Use the `ask_questions` tool when available.

## Prerequisites

Before starting, ensure you have:

- **JDK 8 or later** installed (JDK 11+ recommended; JDK 17/21 LTS also supported)
- **Apache Maven 3.6+** installed
- An **Azure subscription** ([create one for free](https://azure.microsoft.com/free/))
- A **Microsoft Foundry resource** in a [supported region](https://learn.microsoft.com/azure/ai-services/content-understanding/language-region-support)
- **Azure CLI** installed (recommended for `DefaultAzureCredential` auth via `az login`)

> **[ASK USER] Prerequisites Check:**
> Before proceeding, ask the user to confirm their prerequisites:
> 1. "Do you have **JDK 8+** installed? (`java -version`)" — If no, guide them to install a JDK first (e.g., Microsoft Build of OpenJDK, Temurin).
> 2. "Do you have **Maven 3.6+** installed? (`mvn -version`)" — If no, install Maven.
> 3. "Do you already have a **Microsoft Foundry resource** set up in Azure?" — If no, jump to **Step 5** (Azure Resource Setup) first, then return here.
> 4. "Have you already deployed the required **AI models** (GPT-4.1, GPT-4.1-mini, text-embedding-3-large) in Microsoft Foundry?" — If no, include Step 5.3 and Step 6 in the workflow.

## Package Directory

```
sdk/contentunderstanding/azure-ai-contentunderstanding
```

## How Java Samples Use Environment Variables

Java samples read configuration via `System.getenv()`. The variables must be exported in the shell before running `mvn exec:java`. The `.env` file created by this skill can be sourced into your shell with:

```bash
set -a && source .env && set +a
```

Or used with the optional helper script:

```bash
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh <SampleName> --env .env
```

## Workflow

### Step 1: Navigate to Package Directory

```bash
cd sdk/contentunderstanding/azure-ai-contentunderstanding
```

### Step 2: Verify Toolchain

> **[ASK USER] Platform:**
> Ask the user: "Which **platform** are you on?" with options:
> - Linux/macOS
> - Windows PowerShell
> - Windows Command Prompt
>
> Use their answer to show the correct commands throughout the rest of the setup.

Verify your toolchain:

```bash
java -version    # Should print 1.8.x or higher (11+, 17, 21 also fine)
mvn -version     # Should print 3.6.x or higher
```

> **[ASK USER] Confirm toolchain:**
> Ask: "Did both commands print valid versions? If either is missing, install it before continuing."

### Step 3: Install SDK Dependencies

> **[ASK USER] Installation mode:**
> Ask the user: "How would you like to install the SDK?"
> - **Option A: Use the published artifact (recommended)** — Maven will download `com.azure:azure-ai-contentunderstanding` from Maven Central. Best for running samples as-is.
> - **Option B: Local build (for development)** — Installs the current source tree into your local Maven repo so changes are picked up immediately.

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
> - **Option A: DefaultAzureCredential (recommended)** — Uses `az login`, managed identity, or other Azure credential chain. No API key needed.
> - **Option B: API Key** — You'll need your `CONTENTUNDERSTANDING_KEY` from the Azure Portal.
>
> If Option A: Remind the user to run `az login` before invoking samples. Leave `CONTENTUNDERSTANDING_KEY` empty.
> If Option B: Ask for the key value (retrievable at Azure Portal → Foundry resource → Keys and Endpoint → Key1 or Key2).

> **[ASK USER] Model deployment names:**
> Ask: "What are your **model deployment names**? Press Enter to accept each default."
> - GPT-4.1 deployment name (default: `gpt-4.1`) → `GPT_4_1_DEPLOYMENT`
> - GPT-4.1-mini deployment name (default: `gpt-4.1-mini`) → `GPT_4_1_MINI_DEPLOYMENT`
> - text-embedding-3-large deployment name (default: `text-embedding-3-large`) → `TEXT_EMBEDDING_3_LARGE_DEPLOYMENT`
>
> These are required by `Sample00_UpdateDefaults` (one-time mapping setup).

> **[ASK USER] Cross-resource copy (optional):**
> Ask: "Do you plan to use **cross-resource analyzer copying** (`Sample15_GrantCopyAuth`)?"
> - If no: Skip this section.
> - If yes: Gather the following additional values:
>   1. Source resource ID (ARM resource ID)
>   2. Source region (e.g., `eastus`)
>   3. Target endpoint URL
>   4. Target API key (or empty for DefaultAzureCredential)
>   5. Target resource ID (ARM resource ID)
>   6. Target region (e.g., `swedencentral`)

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
> - `Sample02_AnalyzeUrl` — Analyze content from a URL (recommended start)
> - `Sample01_AnalyzeBinary` — Analyze a local file
> - `Sample03_AnalyzeInvoice` — Extract invoice fields
> - Other — Let me see the full list
> - Skip — I'll run samples on my own later
>
> If the user picks "Other", list available samples from `src/samples/java/com/azure/ai/contentunderstanding/samples/`.

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

## Environment Variable Reference

| Variable | Required By | Description |
|----------|------------|-------------|
| `CONTENTUNDERSTANDING_ENDPOINT` | **All samples** | Microsoft Foundry resource endpoint URL |
| `CONTENTUNDERSTANDING_KEY` | All (optional) | API key. If empty, `DefaultAzureCredential` is used (run `az login` first) |
| `GPT_4_1_DEPLOYMENT` | Sample00_UpdateDefaults | GPT-4.1 deployment name (default: `gpt-4.1`) |
| `GPT_4_1_MINI_DEPLOYMENT` | Sample00_UpdateDefaults | GPT-4.1-mini deployment name (default: `gpt-4.1-mini`) |
| `TEXT_EMBEDDING_3_LARGE_DEPLOYMENT` | Sample00_UpdateDefaults | text-embedding-3-large deployment name (default: `text-embedding-3-large`) |
| `CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID` | Sample15_GrantCopyAuth | Source ARM resource ID |
| `CONTENTUNDERSTANDING_SOURCE_REGION` | Sample15_GrantCopyAuth | Source region (e.g., `eastus`) |
| `CONTENTUNDERSTANDING_TARGET_ENDPOINT` | Sample15_GrantCopyAuth | Target Foundry endpoint for cross-resource copy |
| `CONTENTUNDERSTANDING_TARGET_KEY` | Sample15_GrantCopyAuth (optional) | Target API key (empty = DefaultAzureCredential) |
| `CONTENTUNDERSTANDING_TARGET_RESOURCE_ID` | Sample15_GrantCopyAuth | Target ARM resource ID |
| `CONTENTUNDERSTANDING_TARGET_REGION` | Sample15_GrantCopyAuth | Target region (e.g., `swedencentral`) |

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `java: command not found` | Install a JDK 8+ (Microsoft Build of OpenJDK or Temurin) and ensure `JAVA_HOME` is set. |
| `mvn: command not found` | Install Maven 3.6+ and add it to `PATH`. |
| `CONTENTUNDERSTANDING_ENDPOINT` is null at runtime | Make sure you ran `set -a && source .env && set +a` in the same terminal before `mvn exec:java`. |
| `ClassNotFoundException: ...samples.SampleXX_...` | Add `-Dexec.classpathScope=test` to the `mvn exec:java` command. Samples live under `src/samples/java` (test classpath). |
| `jacoco-maven-plugin:...:check` fails after `mvn install -DskipTests` | Add `-Djacoco.skip=true`. Skipping tests produces no coverage data, which fails the coverage check. |
| `Access denied` / 401 errors | Check API key is correct, or run `az login` if using DefaultAzureCredential. Verify the `Cognitive Services User` role is assigned. |
| `Model deployment not found` | Deploy required models in Microsoft Foundry and run `Sample00_UpdateDefaults`. |
| Changes to `.env` not taking effect | Re-run `set -a && source .env && set +a` — changes are not auto-reloaded. |
| `.env` committed to git | Add `.env` to `.gitignore` — never commit credentials. |

## Security Notes

- **Never commit `.env` files** to version control. Ensure `.gitignore` includes `.env`.
- Prefer **DefaultAzureCredential** over API keys when possible.
- If using API keys, rotate them regularly via the Azure Portal.
- When displaying `.env` contents back to the user for confirmation, **mask API keys** (e.g., show only the first 4 characters + `***`).

## Related Skills

- `cu-sdk-sample-run` — Run SDK samples (uses the env vars configured here)
- `cu-sdk-common-knowledge` — Domain knowledge for Content Understanding concepts

## Additional Resources

- [SDK README](../../../README.md) - Full documentation
- [Samples README](../../../src/samples/README.md) - Sample descriptions
- [Product Documentation](https://learn.microsoft.com/azure/ai-services/content-understanding/)
- [Prebuilt Analyzers](https://learn.microsoft.com/azure/ai-services/content-understanding/concepts/prebuilt-analyzers)
