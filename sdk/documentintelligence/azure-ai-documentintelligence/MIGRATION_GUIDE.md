# Guide for migrating to `azure-ai-documentintelligence` from `azure-ai-formrecognizer`

> Note: on July 2023, the Azure Cognitive Services Form Recognizer service was renamed to Azure AI Document Intelligence.
Any mentions to Form Recognizer or Document Intelligence in documentation refer to the same Azure service.

This guide is intended to assist in the migration to `azure-ai-documentintelligence` from `azure-ai-formrecognizer`.
It will focus on side-by-side comparisons for similar operations between the two package versions.

We assume that you are familiar with the previous SDK `azure-ai-formrecognizer`. If you are new to this library,
please refer to the SDK README for [azure-ai-documentintelligence][README] directly rather than this migration guide.

## Table of contents
- [Migration benefits](#migration-benefits)
- [Feature Added](#features-added)
    - [Markdown Content Format](#markdown-content-format)
    - [Query Fields](#query-fields)
    - [Split Options](#split-options)
- [Breaking changes](#breaking-changes)
- [Additional samples](#additional-samples)

## Migration benefits

A natural question to ask when considering whether to adopt a new version of the library is what the benefits of
doing so would be. As Azure Form Recognizer has matured and been embraced by a more diverse group of developers,
we have been focused on learning the patterns and practices to best support developer productivity and add value to our
customers. So We're rebranding the package name to Azure `Document Intelligence`.

## Features Added

### Markdown Content Format

Supports output with Markdown content format along with the default plain _text_. For now, this is only supported for
"prebuilt-layout". Markdown content format is deemed a more friendly format for LLM consumption in a chat or automation
use scenario. Custom models should continue to use the default "text" content format for generating .ocr.json results.

Service follows the GFM spec ([GitHub Flavored Markdown](https://github.github.com/gfm/)) for the Markdown format. This
SDK introduces a new enum _ContentFormat_ with value "text" or "markdown" to indicate the result content format.

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
For the complete sample, see [Sample: Markdown][Sample-AnalyzeLayoutMarkdownOutput].

### Query Fields

We reintroduce query fields as a premium add-on feature. When the keyword argument `features` is specified, the service
will further extract the values of the fields specified via the keyword argument `queryFields` to supplement any 
existing fields defined by the model as fallback.

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
For the complete sample, see [Sample: Query Fields][Sample-AnalyzeAddOnQueryFields].

### Split Options

In the previous API versions supported by the older `azure-ai-formrecognizer` library, document splitting and
classification operation (`"/documentClassifiers/{classifierId}:analyze"`) always tried to split the input file into
multiple documents.

To enable a wider set of scenarios, this SDK introduces a keyword argument "split" to specify the document splitting 
mode with the new "2023-10-31-preview" service API version. The following values are supported:

- `split: "auto"`

Let service determine where to split.

- `split: "none"`

The entire file is treated as a single document. No splitting is performed.

- `split: "perPage"`

Each page is treated as a separate document. Each empty page is kept as its own document.


## Breaking Changes

### Clients names updates
To improve the development experience and address the consistent feedback across the Document Intelligence SDK, this new
version of the library introduces two new clients
`DocumentIntelligenceClient` and the `DocumentIntelligenceAdministrationClient` that provide unified methods for
analyzing documents and provide support for the new features added by the service in
API version `2023-10-31-preview` and later.

This below table describes the relationship between SDK versions and supported API versions of the service:

| SDK version                 | Supported API version of service |
|-----------------------------|----------------------------------|
| 1.0.0-beta.x                | 2023-10-31-preview (default)     |
| 3.3.X (Form Recognizer SDK) | 2.0, 2.1, 2022-08-31, 2023-07-31 |
| 3.2.X (Form Recognizer SDK) | 2.0, 2.1, 2022-08-31             |
| 3.1.X (Form Recognizer SDK) | 2.0, 2.1                         |
| 3.0.0 (Form Recognizer SDK) | 2.0                              |

The below table describes the relationship of each client and its supported API version(s):

| API version                          | Supported clients                                                       |
|--------------------------------------|-------------------------------------------------------------------------|
| 2023-10-31-preview                   | DocumentIntelligenceClient and DocumentIntelligenceAdministrationClient |
| 2023-07-31 (Use Form Recognizer SDK) | DocumentAnalysisClient and DocumentModelAdministrationClient            |
| 2022-08-31 (Use Form Recognizer SDK) | DocumentAnalysisClient and DocumentModelAdministrationClient            |
| 2.1 (Use Form Recognizer SDK)        | FormRecognizerClient and FormTrainingClient                             |
| 2.0 (Use Form Recognizer SDK)        | FormRecognizerClient and FormTrainingClient                             |

Please refer to the [README][README] for more information on these new clients.

### Not backward compatible with azure-ai-formrecognizer
`azure-ai-documentintelligence` is a new package, it is not compatible with the previous `azure-ai-formrecognizer` 
package without necessary changes to your code.

### API shape changes
API shapes have been designed from scratch to support new SDK client for the `Document Intelligence` service.
Please refer to the [README][README] and [Samples][README-Samples] for more understanding.

### Field changes in prebuilt-receipt model
In `prebuilt-receipt` model, change to tract currency-related fields values from _number_ to _currency_.

```json
"Total": {
    "type": "currency",
    "valueCurrency": {
        "amount": 123.45,
        "currencySymbol": "$",
        "currencyCode": "USD"
    },
    ...
}
```
Now each currency-related field returning its own currency info to better support receipts with multi-currency, so
the _Currency_ field in result has been removed.

```json
"fields": {
    "Total": {
        "type": "currency",
        "valueCurrency": {
            "amount": 123.45,
            "currencySymbol": "$",
            "currencyCode": "USD"
        },
    ...
    },
    "Tax": { "type": "currency", "valueCurrency": ... },
    ...
}
```

### Model Retirements/Deprecations

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

  For the complete sample, see [Sample: KeyValuePair][Sample-AnalyzeAddOnKeyValuePair].

- All prebuilt models can be seen [here][service_supported_models]. If you wish to still use these models, please rely on the older 
  `azure-ai-formrecognizer` library through the older service API versions.

## Additional samples

For additional samples please take a look at the [Document Intelligence Samples][README-Samples]

<!-- Links -->
[DocumentIntelligenceClientBuilder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/documentanalysis/DocumentIntelligenceClientBuilder.java
[DocumentIntelligenceClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/documentanalysis/DocumentIntelligenceClient.java
[DocumentIntelligenceAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/documentanalysis/DocumentIntelligenceAsyncClient.java
[DocumentIntelligenceAdministrationClientBuilder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/documentanalysis/administration/DocumentIntelligenceAdministrationClientBuilder.java
[DocumentIntelligenceAdministrationClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/documentanalysis/administration/DocumentIntelligenceAdministrationClient.java
[DocumentIntelligenceAdministrationAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/documentanalysis/administration/DocumentIntelligenceAdministrationAsyncClient.java
[Guidelines]: https://azure.github.io/azure-sdk/general_introduction.html
[GuidelinesJava]: https://azure.github.io/azure-sdk/java_introduction.html
[GuidelinesJavaDesign]: https://azure.github.io/azure-sdk/java_introduction.html#namespaces
[README-Samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/README.md
[README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/README.md
[Sample-AnalyzeAddOnKeyValuePair]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeAddOnKeyValuePair.java
[Sample-AnalyzeAddOnQueryFields]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeAddOnQueryFields.java
[Sample-AnalyzeLayoutMarkdownOutput]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeLayoutMarkdownOutput.java
[service_supported_models]: https://aka.ms/azsdk/documentintelligence/models

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fdocumentintelligence%2Fazure-ai-documentintelligence%2Fmigration-guide.png)