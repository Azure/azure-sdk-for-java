# Release History

## 1.0.0-beta.4 (2024-08-15)

### Features Added
- Added support for the Analyze Batch Documents API with the LRO method `beginAnalyzeBatchDocuments` to `DocumentIntelligenceClient` and `DocumentIntelligenceAsyncClient`.
- Added support for method `getAnalyzeResultPdfWithResponse` and `getAnalyzeResultPdf` to `DocumentIntelligenceClient` and `DocumentIntelligenceAsyncClient`.
- Added support for method `getAnalyzeResultFiguresWithResponse` and `getAnalyzeResultFigures` to `DocumentIntelligenceClient` and `DocumentIntelligenceAsyncClient`.
- Added a model `AnalyzeOutputOption` to specify other kinds of output: either `Pdf` and `Figures`.
- Added property `Id` to `DocumentFigure` model.
- Added support for the Copy Classifier API with method `authorizeClassifierCopy` and `authorizeClassifierCopyWithResponse` to
`DocumentIntelligenceAdministrationClient` and `DocumentIntelligenceAdministrationAsyncClient`.
- Added method `copyClassifierTo` to `DocumentIntelligenceAdministrationClient` and `DocumentIntelligenceAdministrationAsyncClient`.
- Added new enum type to `DocumentBuildMode`: `GENERATIVE`.
- Added property `Warnings` to `AnalyzeResult` model.
- Added properties `ClassifierId`, `Split`, and `TrainingHours` to `DocumentModelDetails` model.
- Added properties `ConfidenceThreshold`, `Features`, `MaxDocumentsToAnalyze`, `ModelId`, and `QueryFields` to `DocumentTypeDetails` model.
- Added properties `AllowOverwrite` and `MaxTrainingHours` to `BuildDocumentModelContent` model.
- Added parameter `pages` to `ClassifyDocument` overloads in `DocumentIntelligenceClient` and `DocumentIntelligenceAsyncClient`.
- Added properties `ClassifierId`, `DocTypes`, and `Split` to `ComposeDocumentModelRequest`.
- Added property `AllowOverwrite` to `BuildDocumentClassifierRequest`.
- Added property `operationId` to `AnalyzeResultOperation`.

### Breaking Changes
- `DocumentIntelligenceClient` and `DocumentIntelligenceAdministrationClient` now target service API version `2024-07-31-preview`. Support for `2024-02-29-preview` has been removed.
- Removed support for extracting lists from analyzed documents:
    - Removed models `DocumentList` and `DocumentListItem`.
    - Removed property `Lists` from `AnalyzeResult`.
- Changes to the Compose Document API:
    - Removed model `ComponentDocumentModelDetails`.
    - Removed property `ComponentModels` from `ComposeDocumentModelRequest`.
    - `ComposeDocumentModelRequest` now requires a map of `DocumentTypeDetails` and a classifier ID.
- Removed model `QuotaDetails`.
- Removed property `CustomNeuralDocumentModelBuilds` from `ResourceDetails.`
- Removed required property `fieldSchema` and from `DocumentTypeDetails` and made it settable.
- `DocumentFieldType` is now a required property on the constructor of `DocumentFieldSchema`.- 

## 1.0.0-beta.3 (2024-06-12)

### Breaking Changes

- Modified the final result of `beginAnalyzeDocument` API in `DocumentIntelligenceClient` to `AnalyzeResult` class.
- Modified the final result of `beginClassifyDocument` API in `DocumentIntelligenceClient` to `AnalyzeResult` class.

### Other Changes
#### Dependency Updates

- Upgraded `azure-core-http-netty` to version`1.15.1`.
- Upgraded `azure-core` to version `1.49.1`.

## 1.0.0-beta.2 (2024-03-06)

### Features Added
- Support `retry-header` in `DocumentIntelligenceAdministrationClient` and `DocumentIntelligenceClient` for retrying failed polling operations.
- Added a property, `baseClassifierId` to `BuildDocumentClassfiierOptions` to specify the base classifier id to build upon.
- Added a property, `baseClassifierId` to `DocumentClassifierDetails` to specify the base classfier if on top of which the classifier was trained.
- Added a property, `warnings`, to `DocumentModelDetails` and `DocumentClassifierDetails`, to represent the list of warnings encountered when building the model.
- Added a property, `valueSelectionGroup` to `DocumentField` model.

### Breaking Changes
- The Azure Document Intelligence Client Library, now targets the Azure AI Document Intelligence service API version `"2024-02-29-preview"`.
Please note that support for `2023-10-31-preview` has been discontinued.

### Other Changes
#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.
- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.

## 1.0.0-beta.1 (2023-11-16)

_**Note: Form Recognizer has been rebranded to Document Intelligence**_

This marks the first preview of `azure-ai-documentintelligence` client library for the `Azure AI Document
Intelligence` service (formerly known as Form Recognizer), targeting service API version `"2023-10-31-preview"`.

It is developer-friendly and idiomatic to the Java ecosystem. The principles that guide our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).
For more information about this, and preview releases of other Azure SDK libraries, please visit
https://azure.github.io/azure-sdk/releases/latest/java.html.

### Features Added

- [azure-ai-documentintelligence](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/documentintelligence/azure-ai-documentintelligence) 
  is the new package, replacing [azure-ai-formrecognizer](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer)
  package. The new package is not compatible with the previous `azure-ai-formrecognizer` package without necessary 
  changes to your code.
- The new `"2023-10-31-preview"` service version comes with some new features
  and a few breaking changes
  when compared to the API versions supported by the `azure-ai-formrecognizer` library.
  - **Markdown content format**
  
    Supports output with Markdown content format along with the default plain _text_. For now, this is only supported for 
    "prebuilt-layout". Markdown content format is deemed a more friendly format for LLM consumption in a chat or 
    automation use scenario. Service follows the GFM spec ([GitHub Flavored Markdown](https://github.github.com/gfm/))
    for the Markdown format. Also introduces a new _contentFormat_ property with value "text" or "markdown" to indicate 
    the result content format.

    ```java
    File document = new File("{your-file-to-analyze}");
    SyncPoller<AnalyzeResultOperation, AnalyzeResultOperation> analyzeLayoutResultPoller =
            client.beginAnalyzeDocument("prebuilt-layout", null,
                    null,
                    null,
                    null,
                    null,
                    ContentFormat.MARKDOWN,
                    new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(document.toPath())));
    ```
    For the complete sample, see [Sample: Markdown](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeLayoutMarkdownOutput.java).

  - **Query Fields**

    When this feature flag is specified, the service will further extract the values of the fields specified via the 
    `queryFields` query parameter to supplement any existing fields defined by the model as fallback.

    ```java
    File document = new File("{your-file-to-analyze}");
    SyncPoller<AnalyzeResultOperation, AnalyzeResultOperation> analyzeLayoutResultPoller =
            client.beginAnalyzeDocument("prebuilt-layout", null,
                    null,
                    null,
                    Arrays.asList(DocumentAnalysisFeature.QUERY_FIELDS),
                    Arrays.asList("Address", "InvoiceNumber"),
                    null,
                    new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(document.toPath())));
    ```
    For the complete sample, see [Sample: Query Fields](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeAddOnQueryFields.java).

  - **Split Options**

    In the previous API versions supported by the older `azure-ai-formrecognizer` library, document splitting and 
    classification operation (`"/documentClassifiers/{classifierId}:analyze"`) always tried to split the input file 
    into multiple documents.

    To enable a wider set of scenarios, service introduces a "split" query parameter with the new "2023-10-31-preview" 
    service version. The following values are supported:

    - `split: "auto"`

      Let service determine where to split.

    - `split: "none"`

      The entire file is treated as a single document. No splitting is performed.

    - `split: "perPage"`

      Each page is treated as a separate document. Each empty page is kept as its own document.

### Breaking Changes

- The SDKs targeting service API version `2023-10-31-preview` have renamed the clients to
    - `DocumentIntelligenceAdministrationClient`/`DocumentIntelligenceAdministrationAsyncClient`
    - `DocumentIntelligenceClient`/`DocumentIntelligenceAsyncClient`
  which is different from older `azure-ai-formrecognizer` SDKs which targeting service API version `2023-07-31` and `2022-08-31`.
    - `DocumentAnalysisClient`/`DocumentAnalysisAsyncClient`
    - `DocumentModelAdministrationClient`/`DocumentModelAdministrationAsyncClient`
  
- **prebuilt-receipt** - Currency related fields have been updated. Currency symbol ("$") and code ("USD") are returned along with the amount as shown below.

  ```json
  {
    "content": "$123.45",
    "confidence": 0.995,
    "type": "currency",
    "valueCurrency": {
      "amount": 123.45,
      "currencySymbol": "$",
      "currencyCode": "USD"
    }
  }
  ...
  ```
- API shapes have been designed from scratch to support new SDK client for the Document Intelligence service.
  Please refer to the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/README.md)
  and [Samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/README.md) 
  for more understanding.

### Other Changes

#### Retirements/Deprecations

- `"prebuilt-businessCard"` model is retired.
- `"prebuilt-document"` model is retired, this model is essentially `"prebuilt-layout"` with `DocumentAnalysisFeature.FORMULAS` specified.
  _(This is only supported as an optional feature for "prebuilt-layout" and "prebuilt-invoice".)_.

  ```java
  File document = new File("{your-file-to-analyze}");
  SyncPoller<AnalyzeResultOperation, AnalyzeResultOperation> analyzeLayoutResultPoller =
          client.beginAnalyzeDocument("prebuilt-layout", null,
                  null,
                  null,
                  Arrays.asList(DocumentAnalysisFeature.FORMULAS),
                  null,
                  null,
                  new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(document.toPath())));
    ```
  
  For the complete sample, see [Sample: KeyValuePair](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeAddOnKeyValuePair.java).

- If you wish to still use these models, please rely on the older `azure-ai-formrecognizer` library through the older service API versions.
