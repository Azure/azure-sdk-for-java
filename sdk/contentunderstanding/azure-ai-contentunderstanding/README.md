# Azure Content Understanding client library for Java

Azure AI Content Understanding is a multimodal AI service that extracts semantic content from documents, video, audio, and image files. It transforms unstructured content into structured, machine-readable data optimized for retrieval-augmented generation (RAG) and automated workflows.

Use the client library for Azure AI Content Understanding to:

* **Extract document content** - Extract text, tables, figures, layout information, and structured markdown from documents (PDF, images with text or hand-written text, Office documents and more)
* **Transcribe and analyze audio** - Convert audio content into searchable transcripts with speaker diarization and timing information
* **Analyze video content** - Extract visual frames, transcribe audio tracks, and generate structured summaries from video files
* **Leverage prebuilt analyzers** - Use production-ready prebuilt analyzers across industries including finance and tax (invoices, receipts, tax forms), identity verification (passports, driver's licenses), mortgage and lending (loan applications, appraisals), procurement and contracts (purchase orders, agreements), and utilities (billing statements)
* **Create custom analyzers** - Build domain-specific analyzers for specialized content extraction needs across all four modalities (documents, video, audio, and images)
* **Classify documents and video** - Automatically categorize and extract information from documents and video by type

If you have encountered issues or want to suggest features, please [file an issue][file_issue].

[Source code][source_code] | [Package (Maven)][package_maven] | [API reference documentation][api_reference_docs] | [Product documentation][product_docs]

## Table of Contents

- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Configuring Microsoft Foundry resource](#configuring-microsoft-foundry-resource)
  - [Adding the package to your product](#adding-the-package-to-your-product)
  - [Service API versions](#service-api-versions)
  - [Authenticate the client](#authenticate-the-client)
- [Key concepts](#key-concepts)
  - [Prebuilt analyzers](#prebuilt-analyzers)
  - [Content types](#content-types)
  - [Asynchronous operations](#asynchronous-operations)
  - [Main classes](#main-classes)
  - [Thread safety](#thread-safety)
  - [Additional concepts](#additional-concepts)
- [Examples](#examples)
  - [Running samples](#running-samples)
- [Troubleshooting](#troubleshooting)
  - [Common issues](#common-issues)
  - [Enable logging](#enable-logging)
- [GitHub Copilot Skills](#github-copilot-skills)
  - [Available Skills](#available-skills)
  - [Using Skills in VS Code](#using-skills-in-vs-code)
  - [Troubleshooting Skill Selection](#troubleshooting-skill-selection)
- [Next steps](#next-steps)
- [Contributing](#contributing)

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure subscription][azure_subscription]
- A **Microsoft Foundry resource** to use this package

### Configuring Microsoft Foundry resource

Before using the Content Understanding SDK, set up a Microsoft Foundry resource and deploy supported generative models. The supported set changes over time; the examples in this README use `gpt-5.2` and `text-embedding-3-large`.

- Current supported and deprecated models: [Supported generative models][supported_generative_models]
- Models being retired: [Foundry model retirement schedule][model_retirement_schedule]
- Deployment guidance: [Content Understanding model deployments][cu_models_deployments]

#### Step 1: Create Microsoft Foundry resource

> **Important:** You must create your Microsoft Foundry resource in a region that supports Content Understanding. For a list of available regions, see [Azure Content Understanding region and language support][cu_region_support].

1. Follow the steps in the [Azure Content Understanding quickstart][cu_quickstart] to create a Microsoft Foundry resource in the Azure portal
2. Get your Foundry resource's endpoint URL from Azure Portal:
   - Go to [Azure Portal][azure_portal]
   - Navigate to your Microsoft Foundry resource
   - Go to **Resource Management** > **Keys and Endpoint**
   - Copy the **Endpoint** URL (typically `https://<your-resource-name>.services.ai.azure.com/`)

**Important: Grant Required Permissions**

After creating your Microsoft Foundry resource, you must grant yourself the **Cognitive Services User** role to enable API calls for setting default model deployments:

1. Go to [Azure Portal][azure_portal]
2. Navigate to your Microsoft Foundry resource
3. Go to **Access Control (IAM)** in the left menu
4. Click **Add** > **Add role assignment**
5. Select the **Cognitive Services User** role
6. Assign it to yourself (or the user/service principal that will run the application)

> **Note:** This role assignment is required even if you are the owner of the resource. Without this role, you will not be able to call the Content Understanding API to configure model deployments for prebuilt analyzers and custom analyzers.

#### Step 2: Deploy supported models

**Important:** Many prebuilt and custom analyzers require completion and embedding deployments. Deploy models that Content Understanding currently supports. This README uses:
- **gpt-5.2**
- **text-embedding-3-large**

**No LLM or embeddings required:** The analyzers **prebuilt-read** and **prebuilt-layout** do not use LLMs or embedding models. You can use them without deploying or configuring any models.

To deploy a model:

1. In Microsoft Foundry, go to **Deployments** > **Deploy model** > **Deploy base model**
2. Search for and select a [supported generative model][supported_generative_models] (this guide uses `gpt-5.2` and `text-embedding-3-large`)
3. Complete the deployment with your preferred settings
4. Note the deployment name you chose (by convention, use the model name as the deployment name). You can use any deployment name, but you'll need it in Step 3.

Repeat this process for each model required by your prebuilt analyzers.

For more information on deploying models, see [Create model deployments in Microsoft Foundry portal][deploy_models_docs].

> **Note on model retirement:** Foundry models follow a [model retirement schedule][model_retirement_schedule]. Before a configured model retires, deploy a supported replacement and update your Content Understanding defaults.

#### Step 3: Configure model deployments (required for prebuilt analyzers)

> **IMPORTANT:** This is a **one-time setup per Microsoft Foundry resource** that maps your deployed models to those required by the prebuilt analyzers and custom models. If you have multiple Microsoft Foundry resources, you need to configure each one separately.

You need to configure the default model mappings in your Microsoft Foundry resource. Prebuilt analyzers use the aliases `prebuilt-analyzer-completion`, `prebuilt-analyzer-completion-mini`, and `prebuilt-analyzer-embedding`. [Sample00_UpdateDefaults][sample00_update_defaults] maps these aliases and their logical model names to your configured deployments.

To configure model deployments using code, see [Sample00_UpdateDefaults][sample00_update_defaults] for a complete example. The sample shows how to:
- Map your deployed models to the models required by prebuilt analyzers
- Retrieve the current default model deployment configuration
- Update the configuration with your model deployment mappings
- Verify the updated configuration

The following shows how to set up the environment to run this sample successfully:

**3-1. Set environment variables**

The environment variables define your Microsoft Foundry resource endpoint and the deployment names from Step 2. Deployment names must exactly match those configured in Foundry.

**On Linux/macOS (bash):**
```bash
export CONTENTUNDERSTANDING_ENDPOINT="https://<your-resource-name>.services.ai.azure.com/"
export CONTENTUNDERSTANDING_KEY="<your-api-key>"  # Optional if using DefaultAzureCredential
export CU_COMPLETION_MODEL="gpt-5.2"
export CU_COMPLETION_MODEL_MINI="gpt-5.2"
export CU_EMBEDDING_MODEL="text-embedding-3-large"
export CU_COMPLETION_MODEL_DEPLOYMENT="gpt-5.2"
export CU_COMPLETION_MINI_DEPLOYMENT="gpt-5.2"
export CU_EMBEDDING_DEPLOYMENT="text-embedding-3-large"
```

**On Windows (PowerShell):**
```powershell
$env:CONTENTUNDERSTANDING_ENDPOINT="https://<your-resource-name>.services.ai.azure.com/"
$env:CONTENTUNDERSTANDING_KEY="<your-api-key>"  # Optional if using DefaultAzureCredential
$env:CU_COMPLETION_MODEL="gpt-5.2"
$env:CU_COMPLETION_MODEL_MINI="gpt-5.2"
$env:CU_EMBEDDING_MODEL="text-embedding-3-large"
$env:CU_COMPLETION_MODEL_DEPLOYMENT="gpt-5.2"
$env:CU_COMPLETION_MINI_DEPLOYMENT="gpt-5.2"
$env:CU_EMBEDDING_DEPLOYMENT="text-embedding-3-large"
```

**On Windows (Command Prompt):**
```bat
set CONTENTUNDERSTANDING_ENDPOINT=https://<your-resource-name>.services.ai.azure.com/
set CONTENTUNDERSTANDING_KEY=<your-api-key>  # Optional if using DefaultAzureCredential
set CU_COMPLETION_MODEL=gpt-5.2
set CU_COMPLETION_MODEL_MINI=gpt-5.2
set CU_EMBEDDING_MODEL=text-embedding-3-large
set CU_COMPLETION_MODEL_DEPLOYMENT=gpt-5.2
set CU_COMPLETION_MINI_DEPLOYMENT=gpt-5.2
set CU_EMBEDDING_DEPLOYMENT=text-embedding-3-large
```

**Notes:**
- If `CONTENTUNDERSTANDING_KEY` is not set, the SDK will fall back to `DefaultAzureCredential`. Ensure you have authenticated (e.g., `az login`).
- The deployment names must exactly match what you created in Microsoft Foundry in Step 2.
- `CU_COMPLETION_MODEL_MINI` defaults to `CU_COMPLETION_MODEL`, and `CU_COMPLETION_MINI_DEPLOYMENT` defaults to `CU_COMPLETION_MODEL_DEPLOYMENT` when omitted.

**3-2. Run the configuration sample**

To run the configuration sample, you'll need to add the SDK to your project and copy the sample code:

**Step 1:** Add the SDK dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-contentunderstanding</artifactId>
    <version>1.1.0-beta.3</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
  <version>1.18.4</version>
</dependency>
```

**Step 2:** Download or copy [Sample00_UpdateDefaults.java][sample00_update_defaults] to your project.

**Step 3:** Run the sample:

```bash
# Compile and run (from your project directory)
mvn compile
mvn exec:java -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample00_UpdateDefaults"
```

Or run it directly from your IDE by executing the `main` method in `Sample00_UpdateDefaults.java`.

**Verification**

After the script runs successfully, you can use prebuilt analyzers like `prebuilt-invoice` or `prebuilt-documentSearch`. For more examples and sample code, see the [Examples](#examples) section.

If you encounter errors:
- **Access Denied**: Ensure you have the **Cognitive Services User** role assignment.
- **Deployment Not Found**: Check that deployment names in environment variables match exactly what you created in Foundry.

### Adding the package to your product

This README documents the current beta package, which includes support for service API version
`2026-06-01-preview` and the preview-only capabilities described below. Maven resolves the exact
version declared in your project, so use `1.1.0-beta.3` or later when following the preview samples.

[//]: # ({x-version-update-start;com.azure:azure-ai-contentunderstanding;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-contentunderstanding</artifactId>
    <version>1.1.0-beta.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

To use only the generally available `2025-11-01` service API surface, use the stable `1.0.0`
package.

### Service API versions

This package supports both the latest generally available (GA) service API and the latest preview API.

| SDK version | Supported service API versions | Default service API version |
|-------------|--------------------------------|-----------------------------|
| `1.0.0` (stable) | `2025-11-01` | `2025-11-01` |
| `1.1.0-beta.3` (beta) | `2025-11-01`, `2026-06-01-preview` | `2026-06-01-preview` |

If you don't call `serviceVersion`, the beta package uses `ContentUnderstandingServiceVersion.getLatest()`, currently `2026-06-01-preview`.

Use the GA service API for stable, generally available behavior:

```java
ContentUnderstandingClient client = new ContentUnderstandingClientBuilder()
  .endpoint(endpoint)
  .credential(credential)
  .serviceVersion(ContentUnderstandingServiceVersion.V2025_11_01)
  .buildClient();
```

Use the preview service API for capabilities such as inline analysis, analyzer workflows, and semantic chunking:

```java
ContentUnderstandingClient client = new ContentUnderstandingClientBuilder()
  .endpoint(endpoint)
  .credential(credential)
  .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW)
  .buildClient();
```

Preview-only capabilities include inline analysis, semantic chunking, analyzer workflows, signature detection, in-page segmentation, embedded document metadata, and analysis-result metadata in `LlmInputHelper` output. See Sample04, samples 17-20, and the advanced samples for complete examples.

### Authenticate the client

In order to interact with the Content Understanding service, you'll need to create an instance of the `ContentUnderstandingClient` class. To authenticate the client, you need your Microsoft Foundry resource endpoint and credentials. You can use either an API key or Microsoft Entra ID authentication.

#### Using DefaultAzureCredential

The simplest way to authenticate is using `DefaultAzureCredential`, which supports multiple authentication methods and works well in both local development and production environments:

```java
// Example: https://your-foundry.services.ai.azure.com/
String endpoint = "<endpoint>";
ContentUnderstandingClient client = new ContentUnderstandingClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

#### Using API key

You can also authenticate using an API key from your Microsoft Foundry resource:

```java
// Example: https://your-foundry.services.ai.azure.com/
String endpoint = "<endpoint>";
String apiKey = "<apiKey>";
ContentUnderstandingClient client = new ContentUnderstandingClientBuilder()
    .endpoint(endpoint)
    .credential(new AzureKeyCredential(apiKey))
    .buildClient();
```

> **⚠️ Security Warning**: API key authentication is less secure and is only recommended for testing purposes with test resources. For production, use `DefaultAzureCredential` or other secure authentication methods.

To get your API key:
1. Go to [Azure Portal][azure_portal]
2. Navigate to your Microsoft Foundry resource
3. Go to **Resource Management** > **Keys and Endpoint**
4. Copy one of the **Keys** (Key1 or Key2)

For more information on authentication, see [Azure Identity client library for Java][azure_identity].



## Key concepts

### Prebuilt analyzers

Content Understanding provides a rich set of prebuilt analyzers that are ready to use without any configuration. These analyzers are powered by knowledge bases of thousands of real-world document examples, enabling them to understand document structure and adapt to variations in format and content.

Prebuilt analyzers are organized into several categories:

* **RAG analyzers** - Optimized for retrieval-augmented generation scenarios with semantic analysis and markdown extraction. These analyzers return markdown and a one-paragraph `Summary` for each content item:
  * **`prebuilt-documentSearch`** - Extracts content from documents (PDF, images, Office documents) with layout preservation, table detection, figure analysis, and structured markdown output. Optimized for RAG scenarios.
  * **`prebuilt-imageSearch`** - Analyzes standalone images and returns a one-paragraph description of the image content. Optimized for image understanding and search scenarios. For images that contain text (including hand-written text), use `prebuilt-documentSearch`.
  * **`prebuilt-audioSearch`** - Transcribes audio content with speaker diarization, timing information, and conversation summaries. Supports multilingual transcription.
  * **`prebuilt-videoSearch`** - Analyzes video content with visual frame extraction, audio transcription, and structured summaries. Provides temporal alignment of visual and audio content and can return multiple segments per video.
* **Content extraction analyzers** - Focus on OCR and layout analysis (e.g., `prebuilt-read`, `prebuilt-layout`)
* **Base analyzers** - Fundamental content processing capabilities used as parent analyzers for custom analyzers (e.g., `prebuilt-document`, `prebuilt-image`, `prebuilt-audio`, `prebuilt-video`)
* **Domain-specific analyzers** - Preconfigured analyzers for common document categories including financial documents (invoices, receipts, bank statements), identity documents (passports, driver's licenses), tax forms, mortgage documents, and contracts
* **Utility analyzers** - Specialized tools for schema generation and field extraction (e.g., `prebuilt-documentFieldSchema`, `prebuilt-documentFields`)

For a complete list of available prebuilt analyzers and their capabilities, see the [Prebuilt analyzers documentation][prebuilt_analyzers_docs].

### Content types

The API returns different content types based on the input:

* **`DocumentContent`** - For document files (PDF, HTML, images, Office documents such as Word, Excel, PowerPoint, and more). Provides basic information such as page count and MIME type. Retrieve detailed information including pages, tables, figures, paragraphs, and many others.
* **`AudioVisualContent`** - For audio and video files. Provides basic information such as timing information (start/end times) and frame dimensions (for video). Retrieve detailed information including transcript phrases, timing information, and for video, key frame references and more.

### Asynchronous operations

By default, Content Understanding analysis uses an asynchronous **long-running operation (LRO)**. The client starts
the request, and the SDK polls until the result is ready:

1. **Begin Analysis** - Start the analysis operation (returns immediately with an operation location)
2. **Poll for Results** - Poll the operation location until the analysis completes
3. **Process Results** - Extract and display the structured results

The SDK provides `SyncPoller<T, U>` and `PollerFlux<T, U>` types that handle polling automatically. For analysis operations, the SDK returns pollers that provide access to the final `AnalysisResult`.

Use the LRO APIs (`beginAnalyze` / `beginAnalyzeBinary`) for larger files or page counts, broader analyzer coverage, and results retained for up to 24 hours unless deleted earlier. The SDK pollers handle the operation lifecycle.

Use `AnalyzeOptions` with `beginAnalyze` or `analyzeInline` when JSON analysis needs model deployment overrides or processing location. Use `AnalyzeBinaryOptions` for the corresponding binary settings together with content range and content type.

The `2026-06-01-preview` service version also provides minimal and options-based `analyzeInline` and `analyzeBinaryInline` methods. These methods return `ContentAnalyzerInlineResponse` without polling, preserving the `AnalysisResult`, operation status, and `UsageDetails`; the service does not persist the result. If the HTTP response contains an inline operation state other than `Succeeded`, the methods throw `HttpResponseException`. With no polling and no wait tied to a polling interval, the inline path is faster for supported smaller inputs. This preview supports document analyzers without field schemas or figure analysis: `prebuilt-digitalParse`, `prebuilt-read`, `prebuilt-layout`, or a custom document analyzer without fields. See the [service limits][cu_service_limits] for current details.

Usage meters depend on the API shape and the processing performed for the input. After `getFinalResult()` completes successfully, the terminal analyze LRO status exposes usage through `poller.waitForCompletion().getValue().getUsage()`. Inline responses expose usage directly through `inlineResponse.getUsage()`. LRO usage includes `getDocumentPagesMinimal()`, `getDocumentPagesBasic()`, or `getDocumentPagesStandard()`; inline usage includes the corresponding `getDocumentPagesMinimalInline()`, `getDocumentPagesBasicInline()`, and `getDocumentPagesStandardInline()` meters. See the [Content Understanding pricing explainer][cu_pricing_explainer] for which meter applies.

To choose between LRO and inline analysis, review the [service limits][cu_service_limits],
[pricing explainer][cu_pricing_explainer], [Sample 19][sample19_inline], and [Sample 20][sample20_inline].

### Main classes

* **`ContentUnderstandingClient`** - The synchronous client for analyzing content, as well as creating, managing, and configuring analyzers
* **`ContentUnderstandingAsyncClient`** - The asynchronous client with the same capabilities
* **`AnalysisResult`** - Contains the structured results of an analysis operation, including content elements, markdown, and metadata

### Thread safety

We guarantee that all client instance methods are thread-safe and independent of each other. This ensures that the recommendation of reusing client instances is always safe, even across threads.

### Additional concepts

The following concepts are common across all Azure SDK client libraries:

[Client options][azure_core_http_client] |
[Accessing the response][azure_core_response] |
[Long-running operations][azure_core_lro] |
[Handling failures][azure_core_exceptions] |
[Logging][logging]

## Examples

You can familiarize yourself with different APIs using [Samples][samples_directory].

The samples demonstrate:

* **Configuration** - Configure model deployment defaults for prebuilt analyzers and custom analyzers
* **Document Content Extraction** - Extract structured markdown content from PDFs and images using `prebuilt-documentSearch`, optimized for RAG (Retrieval-Augmented Generation) applications
* **Multi-Modal Content Analysis** - Analyze content from URLs across all modalities: extract markdown and summaries from documents, images, audio, and video using `prebuilt-documentSearch`, `prebuilt-imageSearch`, `prebuilt-audioSearch`, and `prebuilt-videoSearch`
* **Domain-Specific Analysis** - Extract structured fields from invoices using `prebuilt-invoice`
* **Advanced Document Features** - Extract charts, hyperlinks, formulas, and annotations from documents
* **Preview Analysis** - Analyze URL or binary input inline without polling
* **Agentic Workflows and Chunking** - Use agentic analysis for complex documents and semantic chunks for RAG pipelines
* **Document Details** - Detect signatures, extract embedded metadata, and classify multiple documents that share a page
* **Custom Analyzers** - Create custom analyzers with field schemas for specialized extraction needs
* **Document Classification** - Create and use classifiers to categorize documents
* **Analyzer Management** - Get, list, update, copy, and delete analyzers
* **Result Management** - Retrieve result files from video analysis and delete analysis results

See the [samples directory][samples_directory] for complete examples.

### Running samples

All samples can be run using Maven's `exec:java` plugin. Before running samples, ensure you have set the required environment variables (see [Step 3: Configure model deployments](#step-3-configure-model-deployments-required-for-prebuilt-analyzers)).

**Important:** All samples support both API key and `DefaultAzureCredential` authentication. If you set `CONTENTUNDERSTANDING_KEY`, the samples use API key authentication; otherwise, they fall back to `DefaultAzureCredential`.

Sample04, samples 17-20, and the preview advanced samples require service API version `2026-06-01-preview`.
Their service tests include playback recordings and can also run in LIVE mode against a configured preview resource.

### Option 1: Run samples in your own project (Recommended)

The simplest way to run samples is to copy them into your own Maven project:

1. Add the SDK dependency to your `pom.xml` (see [Adding the package to your product](#adding-the-package-to-your-product))
2. Add `azure-identity` if using `DefaultAzureCredential`:
   ```xml
   <dependency>
       <groupId>com.azure</groupId>
       <artifactId>azure-identity</artifactId>
       <version>1.18.4</version>
   </dependency>
   ```
3. Copy any sample file from the [samples directory][samples_directory] to your project
4. Run it like any other Java class (e.g., `mvn compile exec:java -Dexec.mainClass="YourSampleClass"` or run from your IDE)

### Option 2: Run samples from the SDK source repository

If you want to run samples directly from the SDK source code:

**Step 1: Clone and compile**

```bash
# Clone the repository
git clone https://github.com/Azure/azure-sdk-for-java.git
cd azure-sdk-for-java/sdk/contentunderstanding/azure-ai-contentunderstanding

# Compile the library and samples. The repository registers src/samples/java as test sources.
mvn test-compile
```

**Step 2: Run samples**

Choose one of the following authentication methods:

**Option A: API key authentication**

If you have set `CONTENTUNDERSTANDING_KEY`, the sample uses API key authentication. Samples and their dependencies are compiled in the repository's test scope, so use the test classpath even for API key authentication:

```bash
# Set environment variables
export CONTENTUNDERSTANDING_ENDPOINT="https://<your-resource-name>.services.ai.azure.com/"
export CONTENTUNDERSTANDING_KEY="<your-api-key>"

# Run a sample with API key authentication
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl" \
  -Dexec.classpathScope=test \
  -Dexec.cleanupDaemonThreads=false
```

**Option B: DefaultAzureCredential authentication**

If you don't set `CONTENTUNDERSTANDING_KEY`, the sample will use `DefaultAzureCredential`. Ensure you're authenticated (e.g. `az login`).

```bash
# Set environment variables (no CONTENTUNDERSTANDING_KEY set)
export CONTENTUNDERSTANDING_ENDPOINT="https://<your-resource-name>.services.ai.azure.com/"

# Run a sample (DefaultAzureCredential)
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl" \
  -Dexec.classpathScope=test \
  -Dexec.cleanupDaemonThreads=false
```

**Common sample commands:**

```bash
# Analyze document from URL
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl" \
  -Dexec.classpathScope=test \
  -Dexec.cleanupDaemonThreads=false

# Analyze document from binary file
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample01_AnalyzeBinary" \
  -Dexec.classpathScope=test \
  -Dexec.cleanupDaemonThreads=false

# Analyze invoice
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample03_AnalyzeInvoice" \
  -Dexec.classpathScope=test \
  -Dexec.cleanupDaemonThreads=false

# Create a custom analyzer
mvn exec:java \
  -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample04_CreateAnalyzer" \
  -Dexec.classpathScope=test \
  -Dexec.cleanupDaemonThreads=false
```

### Convert results to LLM-ready text

> **Note:** `LlmInputHelper.toLlmInput()` is currently in preview and may change in future releases.
> We welcome feedback — please [file an issue][file_issue].

Use the `LlmInputHelper.toLlmInput()` helper to convert any analysis result into a text format
that LLMs can consume directly — YAML front matter with extracted fields followed by the markdown
body. This works with all content types (documents, images, audio, video) and handles
multi-segment results and classification hierarchies automatically.

```java
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.LlmInputHelper;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

// Build the client
String endpoint = System.getenv("CONTENTUNDERSTANDING_ENDPOINT");
ContentUnderstandingClient client = new ContentUnderstandingClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

// Analyze a document using prebuilt-documentSearch (CU's primary RAG analyzer)
byte[] pdfBytes = Files.readAllBytes(Paths.get("src/samples/resources/sample_document_features.pdf"));
AnalysisResult result = client.beginAnalyzeBinary(
    "prebuilt-documentSearch", BinaryData.fromBytes(pdfBytes), "application/pdf")
    .getFinalResult();

// One line to get LLM-ready text
String text = LlmInputHelper.toLlmInput(result);
System.out.println(text);
```

Expected output:

```
---
mimeType: application/pdf
pages: 1
fields:
  Summary: The document provides an overview of Latin, includes a sample
    table with names and corporate affiliations, presents a bar chart
    figure illustrating monthly values, and describes the AI Document
    Intelligence service...
---
<!-- InputPageNumber: 1 -->
# ==This is title==
## 1. Text
[Latin](https://en.wikipedia.org/wiki/Latin) refers to an ancient Italic language...
## 2. Page Objects
### 2.1 Table
<table><caption>Table 1: This is a dummy table</caption>...</table>
### 2.2. Figure
![Values...](figures/1.1 "Bar chart with six bars: Jan=200, Feb=300...")
...
```

> **About `<!-- InputPageNumber: N -->`**
>
> The helper emits `<!-- InputPageNumber: N -->` markers at page boundaries in
> the markdown body. `N` is the **original 1-based page number from the source
> document** (i.e., the page index in the analyzed PDF), not a counter that
> restarts at 1 for each call. Downstream consumers (RAG indexers, page-citation
> prompts) can rely on the marker value to cite the correct source page even
> when only a subset of pages was analyzed.
>
> **Why this matters when a page range is specified**
>
> Use `ContentRange` on the analyze input to analyze only a subset of pages in
> a multi-page document. The markers in the rendered output preserve the
> original page identity:
>
> ```java
> // Analyze pages 2-3 and page 5 of a 10-page PDF.
> SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller
>     = contentUnderstandingClient.beginAnalyze("prebuilt-documentSearch",
>         Arrays.asList(new AnalysisInput()
>             .setUrl(multiPageUrl)
>             .setContentRange(new ContentRange("2-3,5"))));
>
> AnalysisResult result = poller.getFinalResult();
> String text = LlmInputHelper.toLlmInput(result);
> // Output contains markers for the *original* page numbers, not 1, 2, 3:
> //   pages: 2-3, 5
> //   ...
> //   <!-- InputPageNumber: 2 -->
> //   ...page 2 content...
> //   <!-- InputPageNumber: 3 -->
> //   ...page 3 content...
> //   <!-- InputPageNumber: 5 -->
> //   ...page 5 content...
> ```
>
> An LLM or RAG indexer can therefore cite "see page 5" with the correct page
> number, even though page 5 is the *third* segment in the response.

See the [advanced sample][java_cu_sample_to_llm_input] for output options (fields-only,
markdown-only, custom metadata), multi-page content ranges, and multi-segment video.

## Troubleshooting

### Common issues

**Error: "Access denied due to invalid subscription key or wrong API endpoint"**
- Verify your `endpoint URL` is correct
- Ensure your `API key` is valid or that your Microsoft Entra ID credentials have the correct permissions
- Make sure you have the **Cognitive Services User** role assigned to your account

**Error: "Model deployment not found" or "Default model deployment not configured"**
- Ensure you have deployed the required models (`gpt-5.2` and `text-embedding-3-large`) in Microsoft Foundry
- Verify you have configured the default model deployments (see [Configure Model Deployments](#step-3-configure-model-deployments-required-for-prebuilt-analyzers))
- Check that your deployment names match what you configured in the defaults

**Error: "Operation failed" or timeout**
- Content Understanding operations are asynchronous and may take time to complete
- Ensure you are properly polling for results using `SyncPoller.waitForCompletion()` or `getFinalResult()`
- Check the operation status for more details about the failure

### Enable logging

To enable logging for debugging, configure the HTTP client with logging options:

```java
ContentUnderstandingClient client = new ContentUnderstandingClientBuilder()
    .endpoint(endpoint)
    .credential(new DefaultAzureCredentialBuilder().build())
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
    .buildClient();
```

For more information, see [Azure SDK for Java logging][logging].

## GitHub Copilot Skills

This package includes [GitHub Copilot][github_copilot] skills under `.github/skills/` that provide interactive, AI-assisted workflows for common tasks. In VS Code, Copilot can use these skills to help with environment setup, running samples, and understanding the service.

### Available Skills

| Skill | Description | How to Use |
|-------|-------------|------------|
| [**cu-sdk-setup**][cu_sdk_setup_skill] | Interactive environment setup — creates and configures your `.env` file with endpoint, credentials, and model deployment settings | In VS Code Copilot Chat, ask: *"Set up my Java environment for Content Understanding"* or reference the skill directly |
| [**cu-sdk-sample-run**][cu_sdk_sample_run_skill] | Guided sample runner — helps you build the SDK, configure credentials, and run specific samples with Maven | Ask: *"Run Sample02_AnalyzeUrl"* or *"Run the invoice analysis sample"* |
| [**cu-sdk-common-knowledge**][cu_sdk_common_knowledge_skill] | Domain knowledge reference — answers questions about Content Understanding concepts, analyzers, field schemas, API operations, and Java SDK usage | Ask: *"What prebuilt analyzers are available?"* or *"How do I create a custom analyzer?"* |

### Using Skills in VS Code

1. In VS Code, open the package folder `sdk/contentunderstanding/azure-ai-contentunderstanding` (File → Open Folder). This is required for VS Code to discover the skills in `.github/skills/`.
2. Ensure [GitHub Copilot][github_copilot] is installed and activated
3. Open Copilot Chat from the Chat view or Command Palette
4. Ask a question related to Content Understanding; Copilot can use the relevant skill when appropriate

**Example prompts:**
- *"Set up my Content Understanding environment"* → likely uses `cu-sdk-setup`
- *"Run Sample03_AnalyzeInvoice"* → likely uses `cu-sdk-sample-run`
- *"Explain how custom analyzers work"* → likely uses `cu-sdk-common-knowledge`

### Troubleshooting Skill Selection

If Copilot does not use the expected skill, try the following:

1. Be explicit about intent and context in one prompt (for example: *"Use cu-sdk-sample-run to run Sample01_AnalyzeBinary"*).
2. Include your goal and current state (for example: *"My .env is configured; help me run Sample02_AnalyzeUrl"*).
3. Ask for a step-by-step interactive flow when needed (for example: *"Guide me step by step to set up my environment"*).
4. For build or runtime errors, mention the exact error text so Copilot can apply the right troubleshooting path.

## Next steps

* [Sample 00: Configure model deployment defaults][sample00] - Required one-time setup to configure model deployments for prebuilt and custom analyzers
* [Sample 01: Analyze a document from binary data][sample01] - Analyze PDF files from disk using `prebuilt-documentSearch`
* Explore the [samples directory][samples_directory] for complete code examples
* Read the [Azure AI Content Understanding documentation][product_docs] for detailed service information

## Contributing

For details on contributing to this repository, see the [contributing guide][contributing].

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

This project has adopted the [Microsoft Open Source Code of Conduct][code_of_conduct]. For more information see the [Code of Conduct FAQ][code_of_conduct_faq] or contact [opencode@microsoft.com][opencode_email] with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/contentunderstanding/azure-ai-contentunderstanding
[package_maven]: https://central.sonatype.com/artifact/com.azure/azure-ai-contentunderstanding
[api_reference_docs]: https://azure.github.io/azure-sdk-for-java/
[product_docs]: https://learn.microsoft.com/azure/ai-services/content-understanding/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md
[azure_portal]: https://portal.azure.com/
[cu_quickstart]: https://learn.microsoft.com/azure/ai-services/content-understanding/quickstart/use-rest-api?tabs=portal%2Cdocument
[cu_region_support]: https://learn.microsoft.com/azure/ai-services/content-understanding/language-region-support
[cu_models_deployments]: https://learn.microsoft.com/azure/ai-services/content-understanding/concepts/models-deployments
[cu_service_limits]: https://learn.microsoft.com/azure/ai-services/content-understanding/service-limits
[cu_pricing_explainer]: https://learn.microsoft.com/azure/ai-services/content-understanding/pricing-explainer
[supported_generative_models]: https://learn.microsoft.com/azure/ai-services/content-understanding/service-limits#supported-generative-models
[model_retirement_schedule]: https://learn.microsoft.com/azure/foundry/openai/concepts/model-retirement-schedule
[deploy_models_docs]: https://learn.microsoft.com/azure/ai-studio/how-to/deploy-models-openai
[prebuilt_analyzers_docs]: https://learn.microsoft.com/azure/ai-services/content-understanding/concepts/prebuilt-analyzers
[samples_directory]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples
[sample00]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample00_UpdateDefaults.java
[sample01]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample01_AnalyzeBinary.java
[sample19_inline]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample19_AnalyzeInline.java
[sample20_inline]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample20_AnalyzeBinaryInline.java
[sample00_update_defaults]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample00_UpdateDefaults.java
[logging]: https://learn.microsoft.com/azure/developer/java/sdk/logging-overview
[java_cu_sample_to_llm_input]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/contentunderstanding/azure-ai-contentunderstanding/src/samples/java/com/azure/ai/contentunderstanding/samples/Sample_Advanced_ToLlmInput.java
[azure_core_http_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/README.md#configuring-service-clients
[azure_core_response]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/README.md#accessing-http-response-details-using-responset
[azure_core_lro]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/README.md#long-running-operations-with-pollerfluxt
[azure_core_exceptions]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/README.md#exception-hierarchy-with-azureexception
[contributing]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
[code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[code_of_conduct_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[opencode_email]: mailto:opencode@microsoft.com
[github_copilot]: https://github.com/features/copilot
[cu_sdk_setup_skill]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/contentunderstanding/azure-ai-contentunderstanding/.github/skills/cu-sdk-setup
[cu_sdk_sample_run_skill]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/contentunderstanding/azure-ai-contentunderstanding/.github/skills/cu-sdk-sample-run
[cu_sdk_common_knowledge_skill]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/contentunderstanding/azure-ai-contentunderstanding/.github/skills/cu-sdk-common-knowledge
[file_issue]: https://github.com/Azure/azure-sdk-for-java/issues/new?labels=Cognitive%20-%20Content%20Understanding&title=[ContentUnderstanding]%20&body=%23%23%20Library%20Version%0A%0A%23%23%20Repro%20Steps%0A%0A%23%23%20Expected%20Result%0A%0A%23%23%20Actual%20Result
