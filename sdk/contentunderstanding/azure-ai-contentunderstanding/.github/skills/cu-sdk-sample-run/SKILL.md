---
name: cu-sdk-sample-run
description: Run a specific sample for the Azure AI Content Understanding Java SDK. Use when users want to run a particular sample like Sample02_AnalyzeUrl or Sample03_AnalyzeInvoice.
---

# Run a Specific Sample

Run a specific sample from the Azure AI Content Understanding Java SDK.

> **[COPILOT INTERACTION MODEL]:** This skill is designed to be interactive. At each step marked with **[ASK USER]**, pause execution and prompt the user for input or confirmation before proceeding. Do NOT silently skip these prompts. Use the `ask_questions` tool when available.

## Prerequisites

- Java >= 8 (JDK)
- Maven
- SDK package available (public Maven Central or local build)
- Environment variables configured (via shell `export`)
- For prebuilt analyzers: model deployments configured (run `Sample00_UpdateDefaults` first)

> **[ASK USER] Prerequisites check:**
> Before proceeding, verify the user's environment:
> 1. "Do you have **Java** and **Maven** installed?" -- If no, direct them to install JDK 8+ and Maven.
> 2. "Have you **built the SDK** or is it available on Maven Central?" -- If no, direct them to Step 2 below.
> 3. "Have you configured your **environment variables** (endpoint and credentials)?" -- If no, direct them to Step 3.
> 4. "Have you run `Sample00_UpdateDefaults` to configure model defaults?" -- If no and they want to use prebuilt analyzers, guide them to run it first.

## Package Directory

```
sdk/contentunderstanding/azure-ai-contentunderstanding
```

## Available Samples

All sync samples have async versions with an `Async` suffix. Samples are located in:

```
src/samples/java/com/azure/ai/contentunderstanding/samples/
```

### Getting Started (Run These First)

#### `Sample00_UpdateDefaults` -- Required First!
**One-time setup** - Configures model deployment mappings (GPT-4.1, GPT-4.1-mini, text-embedding-3-large) for your Microsoft Foundry resource. Must run before using prebuilt analyzers.

#### `Sample02_AnalyzeUrl` -- Start Here!
Analyzes content from a URL using `prebuilt-documentSearch`. Works with documents, images, audio, and video.
- Key concepts: URL input, markdown extraction, multi-modal content

#### `Sample01_AnalyzeBinary`
Analyzes local PDF/image files using `prebuilt-documentSearch`.
- Key concepts: Binary input, local file reading, page properties

### Document Analysis

#### `Sample03_AnalyzeInvoice`
Extracts structured fields from invoices using `prebuilt-invoice`.
- Key concepts: Field extraction (customer name, totals, dates, line items), confidence scores, array fields

#### `Sample10_AnalyzeConfigs`
Extracts advanced features: charts, hyperlinks, formulas, annotations.
- Key concepts: Chart.js output, LaTeX formulas, PDF annotations, enhanced analysis options

#### `Sample11_AnalyzeReturnRawJson`
Gets raw JSON response for custom processing.
- Key concepts: Raw response access, saving to file, debugging

### Custom Analyzers

#### `Sample04_CreateAnalyzer`
Creates custom analyzer with field schema for domain-specific extraction.
- Key concepts: Field types (string, number, date, object, array), extraction methods (extract, generate, classify)

#### `Sample05_CreateClassifier`
Creates classifier to categorize documents (Loan_Application, Invoice, Bank_Statement).
- Key concepts: Content categories, segmentation, document routing

#### `Sample16_CreateAnalyzerWithLabels`
Builds analyzers with training labels (labeled data from Azure Blob Storage).
- Key concepts: Labeled data, knowledge sources, Blob Storage SAS URIs

### Analyzer Management

#### `Sample06_GetAnalyzer`
Retrieves analyzer details and configuration.

#### `Sample08_UpdateAnalyzer`
Updates analyzer description and tags.

#### `Sample09_DeleteAnalyzer`
Deletes a custom analyzer.

#### `Sample14_CopyAnalyzer`
Copies analyzer within the same resource.

#### `Sample15_GrantCopyAuth`
Cross-resource copying between different Azure resources/regions.
- Requires additional env vars: `CONTENTUNDERSTANDING_TARGET_ENDPOINT`, `CONTENTUNDERSTANDING_TARGET_RESOURCE_ID`

### Result Management

#### `Sample12_GetResultFile`
Retrieves keyframe images from video analysis.
- Key concepts: Operation IDs, extracting generated files

#### `Sample13_DeleteResult`
Deletes analysis results for data cleanup.
- Key concepts: Result retention (24-hour auto-deletion), compliance

## Workflow

### Step 1: Navigate to Package Directory

```bash
cd sdk/contentunderstanding/azure-ai-contentunderstanding
```

### Step 2: Build the SDK Package

The SDK package must be available for Maven to resolve. It will be published to **Maven Central** — if it's already available there, Maven will download it automatically and you can **skip this step**.

If the package is **not yet published** (or you want to test local changes), build and install it to your local Maven repository. **Run from the azure-sdk-for-java repo root:**

```bash
cd ~/repos/azure-sdk-for-java   # or wherever you cloned the repo
mvn install -DskipTests -pl sdk/contentunderstanding/azure-ai-contentunderstanding -am
```

> **Important:** You must build from the repo root with `-pl` and `-am` flags. Building from within the package directory will fail because in-repo dependencies cannot be resolved without the `-am` (also-make) flag.

> **[ASK USER] Build check:**
> Ask: "Is the package already published on Maven Central, or do you need to build locally?"
> - If published: Skip to Step 3.
> - If not published / unsure: Run `mvn install -DskipTests` above and confirm it shows `BUILD SUCCESS`.
>
> If the build fails, common fixes:
> - Missing JDK: ensure `java -version` shows JDK 8+
> - Missing Maven: ensure `mvn -version` works
> - Parent POM not found: run `mvn install -DskipTests -f ../../parents/azure-client-sdk-parent/pom.xml` first

<details>
<summary>Alternative: use the setup script (optional)</summary>

The `setup_samples.sh` script automates this — it checks Maven Central first and falls back to a local build:

```bash
.github/skills/cu-sdk-sample-run/scripts/setup_samples.sh
```

Use `--local` to force local build:

```bash
.github/skills/cu-sdk-sample-run/scripts/setup_samples.sh --local
```

</details>

### Step 3: Configure Environment Variables

> **[ASK USER] Configuration check:**
> Ask the user: "Do you already have your environment variables configured (`.env` file or exported in shell)?"
> - If yes: Skip to Step 4.
> - If no: Direct them to the `cu-sdk-setup` skill for interactive setup, or guide them through the steps below.

Java samples read credentials from **OS environment variables** via `System.getenv()`. Java does not load `.env` files automatically, so the variables must be present in the shell environment when the JVM starts.

The recommended approach is to create a **`.env` file** and source it before running samples.

> **Tip:** Use the `cu-sdk-setup` skill for an interactive walkthrough that creates your `.env` file step by step.

**Create a `.env` file** in the package root (`sdk/contentunderstanding/azure-ai-contentunderstanding/.env`):

```
# Azure AI Content Understanding - Environment Variables

# Required: Your Microsoft Foundry resource endpoint
CONTENTUNDERSTANDING_ENDPOINT=https://your-foundry.services.ai.azure.com/

# Optional: API key (leave empty to use DefaultAzureCredential via az login)
CONTENTUNDERSTANDING_KEY=

# Model deployment names (used by Sample00_UpdateDefaults)
GPT_4_1_DEPLOYMENT=gpt-4.1
GPT_4_1_MINI_DEPLOYMENT=gpt-4.1-mini
TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=text-embedding-3-large
```

**Then load it into your shell:**

```bash
set -a && source .env && set +a
```

> **Note:** You must re-run `set -a && source .env && set +a` each time you open a new terminal or edit `.env`.

<details>
<summary>Alternative: export variables directly (without .env file)</summary>

**Linux / macOS:**

```bash
export CONTENTUNDERSTANDING_ENDPOINT="https://your-foundry.services.ai.azure.com/"
export CONTENTUNDERSTANDING_KEY=""   # Leave empty to use DefaultAzureCredential

export GPT_4_1_DEPLOYMENT="gpt-4.1"
export GPT_4_1_MINI_DEPLOYMENT="gpt-4.1-mini"
export TEXT_EMBEDDING_3_LARGE_DEPLOYMENT="text-embedding-3-large"
```

**Windows (PowerShell):**

```powershell
$env:CONTENTUNDERSTANDING_ENDPOINT = "https://your-foundry.services.ai.azure.com/"
$env:CONTENTUNDERSTANDING_KEY = ""   # Leave empty to use DefaultAzureCredential

$env:GPT_4_1_DEPLOYMENT = "gpt-4.1"
$env:GPT_4_1_MINI_DEPLOYMENT = "gpt-4.1-mini"
$env:TEXT_EMBEDDING_3_LARGE_DEPLOYMENT = "text-embedding-3-large"
```

</details>

> **[ASK USER] Provide endpoint:**
> Ask the user: "Please provide your **Microsoft Foundry endpoint URL**."
> - It should look like: `https://<your-resource-name>.services.ai.azure.com/`
> - If the user does not know where to find it: direct them to Azure Portal → Their Foundry resource → Keys and Endpoint.

> **[ASK USER] Authentication method:**
> Ask the user: "How would you like to **authenticate** with Azure?"
> - **Option A: DefaultAzureCredential (recommended)** — Uses `az login` or managed identity. No API key needed. Make sure you have run `az login`.
> - **Option B: API Key** — Provide your `CONTENTUNDERSTANDING_KEY` from the Azure Portal → Keys and Endpoint → Key1 or Key2.

> **[ASK USER] Confirm env vars:**
> After the user sets their variables, ask: "Does this configuration look correct?" Wait for confirmation before proceeding.

#### Settings by sample

| Setting                             | Required By            | Description                                                                                                  |
| ----------------------------------- | ---------------------- | ------------------------------------------------------------------------------------------------------------ |
| `CONTENTUNDERSTANDING_ENDPOINT`     | **All samples**        | Your Microsoft Foundry resource endpoint URL                                                                 |
| `CONTENTUNDERSTANDING_KEY`          | All samples (optional) | API key for key-based auth. If empty, `DefaultAzureCredential` is used (recommended — run `az login` first) |
| `GPT_4_1_DEPLOYMENT`                | Sample00_UpdateDefaults| Deployment name for gpt-4.1 model (default: `gpt-4.1`)                                                       |
| `GPT_4_1_MINI_DEPLOYMENT`           | Sample00_UpdateDefaults| Deployment name for gpt-4.1-mini model (default: `gpt-4.1-mini`)                                             |
| `TEXT_EMBEDDING_3_LARGE_DEPLOYMENT` | Sample00_UpdateDefaults| Deployment name for text-embedding-3-large model (default: `text-embedding-3-large`)                         |

| `CONTENTUNDERSTANDING_TARGET_ENDPOINT`     | Sample15_GrantCopyAuth | Target Foundry resource endpoint for cross-resource copy    |
| `CONTENTUNDERSTANDING_TARGET_RESOURCE_ID`  | Sample15_GrantCopyAuth | Target ARM resource ID for cross-resource copy              |

#### Samples that need a local file

The `Sample01_AnalyzeBinary` and `Sample10_AnalyzeConfigs` samples load a local file from `src/samples/resources/`. The default file paths are built into the samples. To use your own file, update the `filePath` variable in the sample code.

> **[ASK USER] Local file (if applicable):**
> If the user chose a sample that requires a local file (Sample01_AnalyzeBinary, Sample10_AnalyzeConfigs), ask:
> "This sample requires a local document file. Would you like to:"
> - **Use the default test file** — The sample has a built-in file path under `src/samples/resources/`.
> - **Provide your own file** — You'll need to update the `filePath` variable in the sample code.

#### Setting up Sample15_GrantCopyAuth cross-resource environment

The `Sample15_GrantCopyAuth` sample requires **two separate Microsoft Foundry resources** (source and target).

Add the following environment variables:

```bash
export CONTENTUNDERSTANDING_TARGET_ENDPOINT="https://your-target-foundry.services.ai.azure.com/"
export CONTENTUNDERSTANDING_TARGET_RESOURCE_ID="/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.CognitiveServices/accounts/{targetAccountName}"
```

> **[ASK USER] Cross-resource setup (Sample15_GrantCopyAuth only):**
> If the user chose Sample15_GrantCopyAuth, ask:
> 1. "Do you have **two separate Microsoft Foundry resources** (source and target) set up?" — If no, guide them to create a second resource.
> 2. "Please provide the **target** resource endpoint URL and ARM Resource ID."
> 3. Confirm: "Both resources must have the **Cognitive Services User** role assigned if using `DefaultAzureCredential`. Is this configured?"

### Step 4: Choose and Run the Sample

> **[ASK USER] Which sample?:**
> Ask the user: "Which sample would you like to run?" with options:
> - `Sample00_UpdateDefaults` — Configure model defaults (one-time setup, required first)
> - `Sample02_AnalyzeUrl` — Analyze content from a URL (recommended for first-time users)
> - `Sample01_AnalyzeBinary` — Analyze a local PDF/image file
> - `Sample03_AnalyzeInvoice` — Extract structured fields from an invoice
> - `Sample04_CreateAnalyzer` — Create a custom analyzer
> - Other — Let me see the full list

> **[ASK USER] Sync or async?:**
> Ask: "Would you like to run the **sync** or **async** version of this sample?"
> - Sync (default) — e.g., `Sample02_AnalyzeUrl`
> - Async — e.g., `Sample02_AnalyzeUrlAsync`

Run the sample with Maven directly:

```bash
# Make sure .env is loaded first (if not already done in Step 3)
set -a && source .env && set +a

# From the package directory: sdk/contentunderstanding/azure-ai-contentunderstanding
mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl" -Dexec.classpathScope=test
```

**More examples:**

```bash
# Run async sample
mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrlAsync" -Dexec.classpathScope=test

# Run update defaults (one-time setup)
mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample00_UpdateDefaults" -Dexec.classpathScope=test

# Run invoice extraction
mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample03_AnalyzeInvoice" -Dexec.classpathScope=test
```

> **Note:** The `-Dexec.classpathScope=test` flag is **required**. Samples live in `src/samples/`, which is compiled as a test source root — not part of the main classpath. This is an Azure SDK for Java convention: samples are not shipped in the published JAR, and they depend on test-scoped dependencies (e.g., `azure-identity`). Without this flag, Maven cannot find the sample classes and will fail with `ClassNotFoundException`.

> **Note:** Maven inherits the current shell's environment variables. `System.getenv()` in the sample code reads these values at runtime, so your `.env` must be sourced in the same terminal session before running `mvn`.

<details>
<summary>Alternative: use the helper script (optional)</summary>

The `run_sample.sh` script is a convenience wrapper around `mvn exec:java`. It resolves the class name, validates the sample exists, and optionally loads `.env` files.

```bash
# Run a sample
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh Sample02_AnalyzeUrl

# Run with .env file (auto-loads environment variables into the shell)
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh Sample02_AnalyzeUrl --env .env

# List all available samples
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh --list
```

</details>

### After the Sample Runs — Review Results and Explain the Sample

After the sample completes, the skill **must** do the following for the user (do not skip):

1. **Show the terminal command to re-run this sample directly**, so the user can iterate without the skill. For example:
   ```bash
   set -a && source .env && set +a
   mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl" -Dexec.classpathScope=test
   ```
   Substitute `Sample02_AnalyzeUrl` with the sample the user just ran.

2. **Briefly explain the key code concepts** demonstrated in the sample. Tailor the explanation to the specific sample; common concepts include:
   - **Client creation** — how `ContentUnderstandingClient` is constructed via the builder (endpoint + `DefaultAzureCredentialBuilder` or `AzureKeyCredential`)
   - **Analyzer selection** — which prebuilt (`prebuilt-documentSearch`, `prebuilt-invoice`, etc.) or custom analyzer is used and why
   - **Input type** — URL vs. `BinaryData` vs. local file
   - **Result processing** — how the returned `AnalyzeResult` is traversed (pages, fields, contents)
   - **Content type casting** — e.g., casting `AnalyzedContent` to `AnalyzedDocumentContent` / `AnalyzedImageContent` / `AnalyzedAudioContent` / `AnalyzedVideoContent` when needed
   - **Long-running operation polling** — if the sample uses `SyncPoller` / `beginAnalyze`

> **[ASK USER] Sample result:**
> Ask: "Did the sample run successfully?"
> - If yes: present the re-run command and the key-code explanation (above), then ask: "Would you like to run another sample, or are you all set?"
> - If no: help troubleshoot using the Troubleshooting section below. Common issues include missing environment variables, SDK not built, or model defaults not configured.

> **[ASK USER] Run another?:**
> If the user wants to run another sample, loop back to the "Which sample?" prompt above.

## Quick Reference

### Most Common Samples for New Users

1. **First-time setup** (run once per Foundry resource):
   ```bash
   mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample00_UpdateDefaults" -Dexec.classpathScope=test
   ```

2. **Analyze a document from URL:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl" -Dexec.classpathScope=test
   ```

3. **Analyze a local PDF file:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample01_AnalyzeBinary" -Dexec.classpathScope=test
   ```

4. **Extract invoice fields:**
   ```bash
   mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample03_AnalyzeInvoice" -Dexec.classpathScope=test
   ```

## Scripts (Optional)

Helper scripts are provided in `scripts/` as a convenience. They are **not required** — you can always use `mvn exec:java` directly.

### `setup_samples.sh` -- Automated Environment Setup

Checks Maven Central for the published package, falls back to local build, and creates a `.env` template.

```bash
# Default: try Maven Central, fall back to local build
.github/skills/cu-sdk-sample-run/scripts/setup_samples.sh

# Force local build (e.g., testing local changes)
.github/skills/cu-sdk-sample-run/scripts/setup_samples.sh --local

# Local mode: skip build if already built
.github/skills/cu-sdk-sample-run/scripts/setup_samples.sh --local --skip-build
```

### `run_sample.sh` -- Run a Sample with Conveniences

Wraps `mvn exec:java` with sample name resolution, validation, and optional `.env` loading.

```bash
# Run a sample (resolves class name automatically)
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh Sample02_AnalyzeUrl

# Load env vars from .env file before running
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh Sample02_AnalyzeUrl --env .env

# List available samples
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh --list

# Dry run (show what would be executed)
.github/skills/cu-sdk-sample-run/scripts/run_sample.sh Sample02_AnalyzeUrl --dry-run
```

## Troubleshooting

| Error | Solution |
|-------|----------|
| `BUILD FAILURE` during compile | Ensure JDK 8+ and Maven are installed; run `mvn install -DskipTests` from the package directory |
| `ClassNotFoundException` or `NoClassDefFoundError` | Add `-Dexec.classpathScope=test` to the `mvn exec:java` command. Samples are compiled as test sources (Azure SDK convention) and are not on the main classpath. If still failing, rebuild with: `mvn compile test-compile` |
| `CONTENTUNDERSTANDING_ENDPOINT` is null | Set the environment variable: `export CONTENTUNDERSTANDING_ENDPOINT="https://..."` |
| `Access denied` or authorization errors | Ensure **Cognitive Services User** role is assigned; check API key or run `az login` |
| `Model deployment not found` | Run `Sample00_UpdateDefaults` first to configure model mappings |
| `FileNotFoundException` for binary samples | Run samples from the package root directory (`sdk/contentunderstanding/azure-ai-contentunderstanding`) |
| `Parent POM not resolved` | Run `mvn install -DskipTests -f ../../parents/azure-client-sdk-parent/pom.xml` first |
| `Permission denied` when running scripts | Make scripts executable: `chmod +x .github/skills/cu-sdk-sample-run/scripts/*.sh` |

## Related Skills

- `cu-sdk-setup` — Interactive .env file setup (configure endpoint, auth, and model deployments before running samples)
- `cu-sdk-common-knowledge` — Domain knowledge for Content Understanding concepts

## Additional Resources

- [SDK README](../../../README.md) — Full SDK documentation
- [Product Documentation](https://learn.microsoft.com/azure/ai-services/content-understanding/)
- [Azure SDK for Java Contributing Guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md)
