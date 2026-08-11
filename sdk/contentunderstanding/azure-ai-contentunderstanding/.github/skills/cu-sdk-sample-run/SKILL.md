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
> 5. *(Deferred — only if the user later picks `Sample16_CreateAnalyzerWithLabels`.)* "Do you plan to **train with labeled data**? If yes, you'll need an Azure Blob container with the receipt label files uploaded and a SAS URL." Walk them through Step 5's Sample16 subsection when relevant.

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
Builds an analyzer using **labeled training data** loaded from Azure Blob Storage. The repo ships labeled receipt data at `src/samples/resources/receipt_labels/` (`*.jpg`, `*.jpg.labels.json`, optional `*.jpg.result.json`).
- Key concepts: `LabeledDataKnowledgeSource`, knowledge sources on `ContentAnalyzerConfig`, container SAS URLs, optional path prefix, falls back to creating analyzer **without** training data if SAS URL is unset
- Requires either: (a) a SAS URL for an Azure Blob container with labeled data uploaded, or (b) accepting that no training data is used
- For an easier labeling workflow, use [Azure AI Content Understanding Studio](https://contentunderstanding.ai.azure.com/)

### Analyzer Management

#### `Sample06_GetAnalyzer`
Retrieves analyzer details and configuration.

#### `Sample07_ListAnalyzers`
Lists all analyzers in the Content Understanding resource.
- Key concepts: Paginated listing, analyzer enumeration

#### `Sample08_UpdateAnalyzer`
Updates analyzer description and tags.

#### `Sample09_DeleteAnalyzer`
Deletes a custom analyzer.

#### `Sample14_CopyAnalyzer`
Copies analyzer within the same resource.

#### `Sample15_GrantCopyAuth`
Cross-resource copying between different Azure resources/regions.
- Requires additional env vars: `CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID`, `CONTENTUNDERSTANDING_SOURCE_REGION`, `CONTENTUNDERSTANDING_TARGET_ENDPOINT`, `CONTENTUNDERSTANDING_TARGET_RESOURCE_ID`, `CONTENTUNDERSTANDING_TARGET_REGION`, `CONTENTUNDERSTANDING_TARGET_KEY` (optional)

### Result Management

#### `Sample12_GetResultFile`
Retrieves keyframe images from video analysis.
- Key concepts: Operation IDs, extracting generated files

#### `Sample13_DeleteResult`
Deletes analysis results for data cleanup.
- Key concepts: Result retention (24-hour auto-deletion), compliance

### Advanced Helpers

#### `Sample_Advanced_ToLlmInput`
Advanced usage of the `LlmInputHelper.toLlmInput` helper that converts an `AnalysisResult` into LLM-ready text. For introductory usage, see `Sample01_AnalyzeBinary`, `Sample03_AnalyzeInvoice`, and `Sample05_CreateClassifier`.
- Key concepts: `ToLlmInputOptions`, content ranges, multi-modal flattening, prompt-friendly formatting

## Workflow

### Step 1: Navigate to Package Directory

```bash
cd sdk/contentunderstanding/azure-ai-contentunderstanding
```

### Step 2: Build the SDK Package

The SDK package must be available for Maven to resolve. It will be published to **Maven Central** — if it's already available there, Maven will download it automatically and you can **skip this step**.

If the package is **not yet published** (or you want to test local changes), build and install it to your local Maven repository. The recommended command (run from the azure-sdk-for-java repo root) is:

```bash
cd ~/repos/azure-sdk-for-java   # or wherever you cloned the repo
mvn install -DskipTests -pl sdk/contentunderstanding/azure-ai-contentunderstanding -am
```

> **Tip:** Building from the repo root with `-pl ... -am` is preferred when you are contributing across modules or testing in-repo dependency changes (e.g., a local `azure-core` patch). For most users, `mvn install -DskipTests` from within `sdk/contentunderstanding/azure-ai-contentunderstanding` also works, since this module's parent POM is resolved via `relativePath` and its runtime dependencies (e.g., `azure-core`) come from published artifacts.

> **[ASK USER] Build check:**
> Ask: "Is the package already published on Maven Central, or do you need to build locally?"
> - If published: Skip to Step 3.
> - If not published / unsure: Run `mvn install -DskipTests` above and confirm it shows `BUILD SUCCESS`.
>
> If the build fails, common fixes:
> - Missing JDK: ensure `java -version` shows JDK 8+
> - Missing Maven: ensure `mvn -version` works
> - Parent POM not found: run `mvn install -DskipTests -f ../../parents/azure-client-sdk-parent/pom.xml` first

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
> - **Option B: API Key** — Provide your `CONTENTUNDERSTANDING_KEY` from the Azure Portal → Keys and Endpoint → Key1 or Key2. Update `.env` so `CONTENTUNDERSTANDING_KEY=<your-key>` (replace the empty default).

> **[ASK USER] Confirm env vars:**
> After the user sets their variables, ask: "Does this configuration look correct?" Wait for confirmation before proceeding.

### Step 4: Choose the Sample

> **[ASK USER] Which sample?:**
> Ask the user: "Which sample would you like to run?" with options:
> - `Sample00_UpdateDefaults` — Configure model defaults (one-time setup, required first)
> - `Sample02_AnalyzeUrl` — Analyze content from a URL (recommended for first-time users)
> - `Sample01_AnalyzeBinary` — Analyze a local PDF/image file
> - `Sample03_AnalyzeInvoice` — Extract structured fields from an invoice
> - `Sample04_CreateAnalyzer` — Create a custom analyzer
> - `Sample16_CreateAnalyzerWithLabels` — Create an analyzer with labeled training data
> - Other — Let me see the full list

> **[ASK USER] Sync or async?:**
> Ask: "Would you like to run the **sync** or **async** version of this sample?"
> - Sync (default) — e.g., `Sample02_AnalyzeUrl`
> - Async — e.g., `Sample02_AnalyzeUrlAsync`

### Step 5: Configure Sample-Specific Settings

Most samples only need the base environment variables from Step 3. The following samples require **additional configuration** before running.

> **[ASK USER] Sample-specific config:**
> Based on the sample chosen in Step 4, walk the user through the matching subsection below:
> - **Prebuilt-analyzer samples** — `Sample02_AnalyzeUrl`, `Sample01_AnalyzeBinary`, `Sample03_AnalyzeInvoice`, `Sample10_AnalyzeConfigs`, `Sample11_AnalyzeReturnRawJson`, `Sample12_GetResultFile`, `Sample13_DeleteResult` → "Have you run `Sample00_UpdateDefaults`?" subsection
> - `Sample01_AnalyzeBinary`, `Sample10_AnalyzeConfigs` → also "Samples that need a local file" subsection
> - `Sample15_GrantCopyAuth` → "Sample15_GrantCopyAuth cross-resource environment" subsection
> - `Sample16_CreateAnalyzerWithLabels` → "Sample16_CreateAnalyzerWithLabels training data" subsection
> - `Sample00_UpdateDefaults` — sets up the model defaults itself; only the base env vars from Step 3 are needed
> - Custom-analyzer samples (`Sample04_CreateAnalyzer`, `Sample05_CreateClassifier`) and management samples (`Sample06`–`Sample09`, `Sample14`) — only the base env vars from Step 3 are needed
>
> If none apply, proceed directly to Step 6.

#### Settings by sample

| Setting                                      | Required By                       | Description                                                                                                  |
| -------------------------------------------- | --------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| `CONTENTUNDERSTANDING_ENDPOINT`              | **All samples**                   | Your Microsoft Foundry resource endpoint URL                                                                 |
| `CONTENTUNDERSTANDING_KEY`                   | All samples (optional)            | API key for key-based auth. If empty, `DefaultAzureCredential` is used (recommended — run `az login` first) |
| `GPT_4_1_DEPLOYMENT`                         | Sample00_UpdateDefaults           | Deployment name for gpt-4.1 model (default: `gpt-4.1`)                                                       |
| `GPT_4_1_MINI_DEPLOYMENT`                    | Sample00_UpdateDefaults           | Deployment name for gpt-4.1-mini model (default: `gpt-4.1-mini`)                                             |
| `TEXT_EMBEDDING_3_LARGE_DEPLOYMENT`          | Sample00_UpdateDefaults           | Deployment name for text-embedding-3-large model (default: `text-embedding-3-large`)                         |
| `CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID`    | Sample15_GrantCopyAuth            | Source ARM resource ID for cross-resource copy                                                               |
| `CONTENTUNDERSTANDING_SOURCE_REGION`         | Sample15_GrantCopyAuth            | Region of the source Foundry resource (e.g., `westus`)                                                       |
| `CONTENTUNDERSTANDING_TARGET_ENDPOINT`       | Sample15_GrantCopyAuth            | Target Foundry resource endpoint for cross-resource copy                                                     |
| `CONTENTUNDERSTANDING_TARGET_RESOURCE_ID`    | Sample15_GrantCopyAuth            | Target ARM resource ID for cross-resource copy                                                               |
| `CONTENTUNDERSTANDING_TARGET_REGION`         | Sample15_GrantCopyAuth            | Region of the target Foundry resource (e.g., `eastus`)                                                       |
| `CONTENTUNDERSTANDING_TARGET_KEY`            | Sample15_GrantCopyAuth (optional) | API key for the target resource. If empty, `DefaultAzureCredential` is used                                  |
| `CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL` | Sample16 (Option A) | Pre-generated container-level SAS URL pointing at your labeled training data. If set, the sample uses it directly and skips Option B. |
| `CONTENTUNDERSTANDING_TRAINING_DATA_PREFIX`  | Sample16 (optional) | Optional prefix (e.g., `receipt_labels` or `receipt_labels/`) that scopes the labeled data within the container. Both forms work — the SDK normalises the trailing slash. |
| `CONTENTUNDERSTANDING_TRAINING_DATA_STORAGE_ACCOUNT` | Sample16 (Option B) | Storage account name (e.g., `mystorageacct`). Used by Option B (auto-upload) to upload the bundled `src/samples/resources/receipt_labels/` files via `DefaultAzureCredential` and mint a User Delegation SAS URL. |
| `CONTENTUNDERSTANDING_TRAINING_DATA_CONTAINER` | Sample16 (Option B) | Container name (e.g., `cu-training-data`). Created on demand by Option B. |
| `CONTENTUNDERSTANDING_TRAINING_DATA_LOCAL_DIR` | Sample16 (Option B, optional) | Override the local folder of label files to upload. Defaults to `src/samples/resources/receipt_labels`. |

#### Have you run `Sample00_UpdateDefaults`?

Most samples that use prebuilt analyzers (e.g., `Sample02_AnalyzeUrl`, `Sample03_AnalyzeInvoice`, `Sample10_AnalyzeConfigs`, `Sample11_AnalyzeReturnRawJson`) require model deployments to be configured. `Sample00_UpdateDefaults` writes a one-time mapping from logical model names (gpt-4.1, gpt-4.1-mini, text-embedding-3-large) to your Foundry resource's actual deployment names. Without it, prebuilt analyzers fail with `Model deployment not found`.

> **[ASK USER] Update defaults check:**
> Ask: "Have you previously run `Sample00_UpdateDefaults` for this Foundry resource?"
> - If yes: Continue to the next subsection (or Step 6 if none apply).
> - If no and the chosen sample uses prebuilt analyzers:
>   1. Run `Sample00_UpdateDefaults` now using the command in Step 6: `mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample00_UpdateDefaults" -Dexec.classpathScope=test`
>   2. Wait for it to print success.
>   3. Then come back to Step 4, re-select the **original** sample the user wanted, and continue from Step 5.

#### Samples that need a local file

The `Sample01_AnalyzeBinary` and `Sample10_AnalyzeConfigs` samples load a local file from `src/samples/resources/`. The default file paths are built into the samples. To use your own file, update the `filePath` variable in the sample code.

> **[ASK USER] Local file (if applicable):**
> If the user chose a sample that requires a local file (Sample01_AnalyzeBinary, Sample10_AnalyzeConfigs), ask:
> "This sample requires a local document file. Would you like to:"
> - **Use the default test file** — The sample has a built-in file path under `src/samples/resources/`.
> - **Provide your own file** — You'll need to update the `filePath` variable in the sample code.

#### Setting up Sample15_GrantCopyAuth cross-resource environment

The `Sample15_GrantCopyAuth` sample requires **two separate Microsoft Foundry resources** (source and target).

Add the following environment variables to your `.env` file:

```
CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID=/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.CognitiveServices/accounts/{sourceAccountName}
CONTENTUNDERSTANDING_SOURCE_REGION=westus
CONTENTUNDERSTANDING_TARGET_ENDPOINT=https://your-target-foundry.services.ai.azure.com/
CONTENTUNDERSTANDING_TARGET_RESOURCE_ID=/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.CognitiveServices/accounts/{targetAccountName}
CONTENTUNDERSTANDING_TARGET_REGION=eastus
# Optional — only if you want key-based auth for the target resource:
# CONTENTUNDERSTANDING_TARGET_KEY=<your-target-resource-key>
```

Then reload your shell: `set -a && source .env && set +a`.

> **[ASK USER] Cross-resource setup (Sample15_GrantCopyAuth only):**
> If the user chose Sample15_GrantCopyAuth, ask:
> 1. "Do you have **two separate Microsoft Foundry resources** (source and target) set up?" — If no, guide them to create a second resource.
> 2. "Please provide the **source** ARM Resource ID and region, and the **target** endpoint URL, ARM Resource ID, and region."
> 3. "Will you authenticate the target resource with `DefaultAzureCredential` (recommended) or with `CONTENTUNDERSTANDING_TARGET_KEY`?"
> 4. Confirm: "Both resources must have the **Cognitive Services User** role assigned if using `DefaultAzureCredential`. Is this configured?"

#### Setting up Sample16_CreateAnalyzerWithLabels training data

The `Sample16_CreateAnalyzerWithLabels` sample creates an analyzer backed by **labeled training data** loaded from Azure Blob Storage via a SAS URL. You can configure training data in two ways:

- **Option A — Manual upload**: you upload the labeled triplets (image + `.labels.json` + `.result.json`) yourself and provide a container SAS URL via `CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL`.
- **Option B — Auto-upload via `DefaultAzureCredential`**: the sample uses your `az login` identity to upload the bundled receipt files from `src/samples/resources/receipt_labels/` into your storage account and mint a short-lived User Delegation SAS — set `CONTENTUNDERSTANDING_TRAINING_DATA_STORAGE_ACCOUNT` and `CONTENTUNDERSTANDING_TRAINING_DATA_CONTAINER`. The signed-in identity must have **Storage Blob Data Contributor** on the container.

> **Note:** If neither option is configured, the sample runs in **demo mode**: it still creates the analyzer (without labeled data) so you can see the API surface. To fully exercise the labeled-data path you must pick Option A or Option B.

The repo ships labeled receipt training data at `src/samples/resources/receipt_labels/`. Two labeled receipts are included; each receipt has three associated files:

```
17a84146-e910-460c-bf80-a625e6f64fea.jpg          # original image
17a84146-e910-460c-bf80-a625e6f64fea.jpg.labels.json  # labeled fields (required)
17a84146-e910-460c-bf80-a625e6f64fea.jpg.result.json  # OCR result (optional)
29d60394-3da1-4714-abdc-ff0993009872.jpg
29d60394-3da1-4714-abdc-ff0993009872.jpg.labels.json
29d60394-3da1-4714-abdc-ff0993009872.jpg.result.json
```

> **Option A — manual upload steps:**
> 1. Create an Azure Blob Storage container (or use an existing one).
> 2. Upload **all** files from `src/samples/resources/receipt_labels/` (the `.jpg`, `.jpg.labels.json`, and optional `.jpg.result.json` files listed above) into the container. You may upload them at the container root or inside a subfolder (e.g., `receipt_labels/`).
> 3. In Azure Portal: open the storage account, then either navigate Storage account → Containers → your container → **Shared access tokens**, or use the Portal search bar to find "Shared access tokens" (the exact UI path varies by Portal version). Set an expiry, grant at least **List** and **Read** permissions, then generate the SAS URL.
> 4. Add the SAS URL to your `.env` file:
>    ```
>    CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL=https://<account>.blob.core.windows.net/<container>?sv=...&se=...
>    # Only if you uploaded into a subfolder:
>    CONTENTUNDERSTANDING_TRAINING_DATA_PREFIX=receipt_labels
>    ```
>    *(Both `receipt_labels` and `receipt_labels/` work as prefix values — the SDK handles the trailing slash either way.)*
> 5. Reload your shell: `set -a && source .env && set +a`.

> **Option B — auto-upload via `DefaultAzureCredential`:**
> 1. Ensure `az login` has been completed and your account has **Storage Blob Data Contributor** on the target container (or the parent storage account).
> 2. Add to your `.env`:
>    ```
>    CONTENTUNDERSTANDING_TRAINING_DATA_STORAGE_ACCOUNT=mystorageacct
>    CONTENTUNDERSTANDING_TRAINING_DATA_CONTAINER=cu-training-data
>    # Leave CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL empty/unset to trigger Option B.
>    # Optional: override the local folder uploaded (defaults to src/samples/resources/receipt_labels).
>    # CONTENTUNDERSTANDING_TRAINING_DATA_LOCAL_DIR=/path/to/your/labels
>    # Optional: upload into a sub-folder (e.g., receipt_labels) instead of the container root.
>    # CONTENTUNDERSTANDING_TRAINING_DATA_PREFIX=receipt_labels
>    ```
> 3. Reload your shell: `set -a && source .env && set +a`.
> 4. The sample will create the container if it does not exist, upload the bundled files, mint a 1-hour User Delegation SAS, and use it to create the analyzer.

> **[REQUIRED GATE] Sample16 training data (Sample16_CreateAnalyzerWithLabels only):**
> Sample16 silently falls back to "create analyzer without labeled data" when no training-data
> source is configured. That fall-back path completes end-to-end and prints `✓ Sample completed`
> even though the labeled-data API surface is **not** actually exercised. Before invoking
> `mvn exec:java` (or `run_sample.sh Sample16_CreateAnalyzerWithLabels --run`), the agent
> **must** ask the user the questions below and act on the answer:
>
> 1. "Do you want to **train with labeled data** (recommended), or **create the analyzer without training data** (demo mode)?"
>    - If **demo mode**: confirm explicitly — "I will run Sample16 *without* training data. The output will say `Knowledge sources: 0` and you will see a `DEMO MODE` banner. The labeled-data API path will **not** be exercised. OK to proceed?" Only continue after the user says yes; leave both Option A and Option B env vars empty/unset.
>    - If **with training data**: continue with one of the next two questions.
> 2. "Will you use **Option A (pre-generated SAS URL)** or **Option B (auto-upload via `DefaultAzureCredential`)**?"
>    - **Option A**: ask for the SAS URL and (optionally) prefix; walk through the manual-upload steps above if not yet done.
>    - **Option B**: ask for the storage account name and container name; remind them about the **Storage Blob Data Contributor** role and `az login`.
> 3. "Did you upload the files at the **container root** or inside a **subfolder**?"
>    - If root: leave `CONTENTUNDERSTANDING_TRAINING_DATA_PREFIX` unset.
>    - If subfolder: ask for the prefix path (e.g., `receipt_labels/`).
> 4. Confirm: "For Option A, the SAS token must have at least **List** and **Read** permissions and must **not be expired**. For Option B, the signed-in identity must have **Storage Blob Data Contributor** on the container."
>
> **Belt-and-suspenders**: `run_sample.sh` itself emits a loud `DEMO MODE` banner before the
> `mvn exec:java` step when none of the four training-data env vars
> (`CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL`, `..._STORAGE_ACCOUNT`, `..._CONTAINER`,
> `..._LOCAL_DIR`) are set, so the fall-back is unmissable in the captured run output. Treat
> that banner as a signal that validation is **incomplete** unless the user explicitly opted
> into demo mode in step 1.

### Step 6: Run the Sample

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

# Run analyzer with labeled training data
mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample16_CreateAnalyzerWithLabels" -Dexec.classpathScope=test
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

> **Note:** For first-time environment setup (installing JDK/Maven, building the SDK, creating `.env`), use the `cu-sdk-setup` skill.

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
| Sample16: `AuthenticationFailed` / `403` reading training data | The SAS URL is invalid, expired, or missing required permissions. Regenerate the SAS with at least **List** and **Read** and a fresh expiry, then re-source `.env` |
| Sample16: `BlobNotFound` or empty training set | The `CONTENTUNDERSTANDING_TRAINING_DATA_PREFIX` does not match where you uploaded the files. Either upload files at the container root and unset the prefix, or set the prefix to the actual subfolder (e.g., `receipt_labels/`) |
| Sample16: created analyzer has no training data | Neither Option A nor Option B was configured. Set `CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL` (Option A), or set `CONTENTUNDERSTANDING_TRAINING_DATA_STORAGE_ACCOUNT` + `CONTENTUNDERSTANDING_TRAINING_DATA_CONTAINER` (Option B), then re-run `set -a && source .env && set +a` and re-run the sample. |

## Related Skills

- `cu-sdk-setup` — Interactive .env file setup (configure endpoint, auth, and model deployments before running samples)
- `cu-sdk-common-knowledge` — Domain knowledge for Content Understanding concepts

## Additional Resources

- [SDK README](../../../README.md) — Full SDK documentation
- [Product Documentation](https://learn.microsoft.com/azure/ai-services/content-understanding/)
- [Azure SDK for Java Contributing Guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md)
