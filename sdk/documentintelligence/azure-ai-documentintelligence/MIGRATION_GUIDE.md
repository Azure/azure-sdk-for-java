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
customers. So we are rebranding the package name to Azure `Document Intelligence`.

## Features Added
There are many benefits to using the new `azure-ai-documentintelligence` library. This new library introduces two new clients: `DocumentIntelligenceClient` and `DocumentIntelligenceAdministrationClient`, providing support for the new features added by the service in API version `2024-11-30` and higher.

New features provided by the `azure-ai-documentintelligence` library include:
- **Markdown content format:** support to output with Markdown content format along with the default plain text. This is only supported for the "prebuilt-layout" model. Markdown content format is deemed a more friendly format for LLM consumption in a chat or automation use scenario.
- **Query fields:** query fields are reintroduced as a premium add-on feature. When the `DocumentAnalysisFeature.queryFields` argument is passed to a document analysis request, the service will further extract the values of the fields specified via the parameter `queryFields` to supplement any existing fields defined by the model as fallback.
- **Split options:** in previous API versions, the document splitting and classification operation always tried to split the input file into multiple documents. To enable a wider set of scenarios, `ClassifyDocument` now supports a `split` parameter. The following values are supported:
    - `AUTO`: let the service determine where to split.
    - `NONE`: the entire file is treated as a single document. No splitting is performed.
    - `PER_PAGE`: each page is treated as a separate document. Each empty page is kept as its own document.
- **Batch analysis:** allows you to bulk process multiple documents using a single request.
Rather than having to submit documents individually, you can analyze a collection of documents like invoices, a series of a loan documents, or a group of custom documents simultaneously.

## Breaking Changes

### Clients names updates
To improve the development experience and address the consistent feedback across the Document Intelligence SDK, this new
version of the library introduces two new clients
`DocumentIntelligenceClient` and the `DocumentIntelligenceAdministrationClient` that provide unified methods for
analyzing documents and support for the new features added by the service in API version `2023-10-31-preview` and later.

The below table describes the relationship of each client and its supported API version(s):

| Package                       | API version                          | Supported clients                                                       |
|-------------------------------|--------------------------------------|-------------------------------------------------------------------------|
| azure-ai-documentintelligence | 2024-11-30                           | DocumentIntelligenceClient and DocumentIntelligenceAdministrationClient |
| azure-ai-formrecognizer       | 2023-07-31 (Use Form Recognizer SDK) | DocumentAnalysisClient and DocumentModelAdministrationClient            |
| azure-ai-formrecognizer       | 2022-08-31 (Use Form Recognizer SDK) | DocumentAnalysisClient and DocumentModelAdministrationClient            |
| azure-ai-formrecognizer       | 2.1 (Use Form Recognizer SDK)        | FormRecognizerClient and FormTrainingClient                             |
| azure-ai-formrecognizer       | 2.0 (Use Form Recognizer SDK)        | FormRecognizerClient and FormTrainingClient                             |

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
- All prebuilt models can be seen [here][service_supported_models]. If you wish to still use these models, please rely on the older
  `azure-ai-formrecognizer` library through the older service API versions.

For the complete sample, see [Sample: AnalyzeAddOnFormulas][Sample-AnalyzeAddOnFormulas].

### Analyzing documents

Differences between the versions:
- The former `AnalyzeDocument` method taking `bytes` as the input document is not supported in `azure-ai-documentintelligence` 1.0.0. As a workaround you will need to use a URL input or the bytes/BinaryData input option, as shown in the example below.
- `beginAnalyzeDocumentFromUrl` has been renamed to `beginAnalyzeDocument` and its input arguments have been reorganized:
    - The `documentUrl` parameter has been removed. Instead, an `AnalyzeDocumentOptions` object must be passed to the method to select the desired input type: URl or bytes/binary data.
- The property `DocumentField.value` has been removed. A field's value can now be extracted from one of the value properties, depending on the type of the field: `valueAddress` for type `Address`, `valueBoolean` for type `Boolean`, and so on.

```java com.azure.ai.documentintelligence.readme.analyzeLayout
File layoutDocument = new File("local/file_path/filename.png");
Path filePath = layoutDocument.toPath();
BinaryData layoutDocumentData = BinaryData.fromFile(filePath, (int) layoutDocument.length());

SyncPoller<AnalyzeOperationDetails, AnalyzeResult> analyzeLayoutResultPoller =
    documentIntelligenceClient.beginAnalyzeDocument("prebuilt-layout",
        new AnalyzeDocumentOptions(layoutDocumentData));

AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();

// pages
analyzeLayoutResult.getPages().forEach(documentPage -> {
    System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
        documentPage.getWidth(),
        documentPage.getHeight(),
        documentPage.getUnit());

    // lines
    documentPage.getLines().forEach(documentLine ->
        System.out.printf("Line '%s' is within a bounding box %s.%n",
            documentLine.getContent(),
            documentLine.getPolygon().toString()));

    // selection marks
    documentPage.getSelectionMarks().forEach(documentSelectionMark ->
        System.out.printf("Selection mark is '%s' and is within a bounding box %s with confidence %.2f.%n",
            documentSelectionMark.getState().toString(),
            documentSelectionMark.getPolygon().toString(),
            documentSelectionMark.getConfidence()));
});

// tables
List<DocumentTable> tables = analyzeLayoutResult.getTables();
for (int i = 0; i < tables.size(); i++) {
    DocumentTable documentTable = tables.get(i);
    System.out.printf("Table %d has %d rows and %d columns.%n", i, documentTable.getRowCount(),
        documentTable.getColumnCount());
    documentTable.getCells().forEach(documentTableCell -> {
        System.out.printf("Cell '%s', has row index %d and column index %d.%n", documentTableCell.getContent(),
            documentTableCell.getRowIndex(), documentTableCell.getColumnIndex());
    });
    System.out.println();
}
```

## Additional samples

For additional samples please take a look at the [Document Intelligence Samples][README-Samples] for more guidance.

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
[Sample-AnalyzeAddOnFormulas]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeAddOnFormulas.java
[service_supported_models]: https://aka.ms/azsdk/documentintelligence/models

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fdocumentintelligence%2Fazure-ai-documentintelligence%2Fmigration-guide.png)
