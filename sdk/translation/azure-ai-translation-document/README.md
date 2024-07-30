# Azure DocumentTranslation client library for Java

Document Translation is a cloud-based machine translation feature of the Azure AI Translator service. You can translate multiple and complex documents across all supported languages and dialects while preserving original document structure and data format. The Document translation API supports two translation processes:

Asynchronous batch translation supports the processing of multiple documents and large files. The batch translation process requires an Azure Blob storage account with storage containers for your source and translated documents.

Synchronous single file supports the processing of single file translations. The file translation process doesn't require an Azure Blob storage account. The final response contains the translated document and is returned directly to the calling client.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Document Translator service or Cognitive Services resource.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-translation-document;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-translation-document</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

Interaction with the service using the client library begins with creating an instance of the [DocumentTranslationClient][document_translator_client_class] class. You will need an **API key** or ``TokenCredential`` and **Endpoint** to instantiate a document translation client object. Similarly for [SingleDocumentTranslationclient][single_document_translator_client_class]

#### Get an API key

You can get the `endpoint`, `API key` and `Region` from the Cognitive Services resource or Document Translator service resource information in the [Azure Portal][azure_portal].

Alternatively, use the [Azure CLI][azure_cli] snippet below to get the API key from the Translator service resource.

```PowerShell
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

#### Create a `DocumentTranslationClient` using endpoint and API key credential

Once you have the value for the API key, create an `AzureKeyCredential`. This will allow you to
update the API key without creating a new client.

With the value of the `endpoint` and `AzureKeyCredential` , you can create the [DocumentTranslationClient][document_translator_client_class]:

```java createDocumentTranslationClient
String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");

AzureKeyCredential credential = new AzureKeyCredential(apiKey);

DocumentTranslationClient client = new DocumentTranslationClientBuilder()
                    .endpoint(endpoint)
                    .credential(credential)
                    .buildClient();
```

You can similarly create the [SingleDocumentTranslationClient][single_document_translator_client_class]:
```java createSingleDocumentTranslationClient
String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");
String apiKey = System.getenv("DOCUMENT_TRANSLATION_API_KEY");

AzureKeyCredential credential = new AzureKeyCredential(apiKey);

SingleDocumentTranslationClient client = new SingleDocumentTranslationClientBuilder()
                    .endpoint(endpoint)
                    .credential(credential)
                    .buildClient();
```

## Key concepts
### `DocumentTranslationClient` and `DocumentTranslationAsyncClient`

A `DocumentTranslationClient` is the primary interface for developers using the Document Translator client library. It provides both synchronous operations to access a specific use of document translator operations, such as get supported formats, get translation status, get translations status, get document status, get documents status, cancel translation or batch translation.

For asynchronous operations use `DocumentTranslationAsyncClient`.

A `SingleDocumentTranslationClient` provides an interface for developers to synchronously translate a single document.

For asynchronous operations use `SingleDocumentTranslationAsyncClient`.

### Input

A **batch request element** (`BatchRequest`), is a single unit of input to be processed by the document translation models in the document Translator service. Operations on `DocumentTranslationClient` may take a single batchRequest element or a collection of elements.

## Examples
The following section provides several code snippets using the `client` [created above](#create-a-documenttranslationclient-using-endpoint-and-api-key-credential)), and covers the main features present in this client library. Although most of the snippets below make use of asynchronous service calls, keep in mind that the `Azure.AI.Translation.Document` package supports both synchronous and asynchronous APIs.

### Get Supported Formats

Gets a list of document and glossary formats supported by the Document Translation feature. The list includes common file extensions and content-type if using the upload API.

```java getSupportedFormats
SupportedFileFormats documentResponse = documentTranslationClient.getSupportedFormats(FileFormatType.DOCUMENT);
List<FileFormat> documentFileFormats = documentResponse.getValue();
for (FileFormat fileFormat : documentFileFormats) {
    System.out.println("FileFormat:" + fileFormat.getFormat());
    System.out.println("FileExtensions:" + fileFormat.getFileExtensions());
    System.out.println("ContentTypes:" + fileFormat.getContentTypes());
    System.out.println("Type:" + fileFormat.getType());
}

SupportedFileFormats glossaryResponse = documentTranslationClient.getSupportedFormats(FileFormatType.GLOSSARY);
List<FileFormat> glossaryFileFormats = glossaryResponse.getValue();
for (FileFormat fileFormat : glossaryFileFormats) {
    System.out.println("FileFormat:" + fileFormat.getFormat());
    System.out.println("FileExtensions:" + fileFormat.getFileExtensions());
    System.out.println("ContentTypes:" + fileFormat.getContentTypes());
    System.out.println("Type:" + fileFormat.getType());
}
```

Please refer to the service documentation for a conceptual discussion of [documentFormats][documentFormats_doc] and [glossaryFormats][glossaryFormats_doc].

### Batch Translation
Executes an asynchronous batch translation request. The method requires an Azure Blob storage account with storage containers for your source and translated documents.

```java startDocumentTranslation
SyncPoller<TranslationStatus, Void> response
    = documentTranslationClient
        .beginStartTranslation(
            new StartTranslationDetails(Arrays.asList(new BatchRequest(
                new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                    .setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"))
                    .setLanguage("en")
                    .setStorageSource(StorageSource.AZURE_BLOB),
                Arrays
                    .asList(
                        new TargetInput("https://myblob.blob.core.windows.net/destinationContainer1", "fr")
                            .setCategory("general")
                            .setGlossaries(Arrays.asList(new Glossary(
                                "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf", "XLIFF")
                                .setStorageSource(StorageSource.AZURE_BLOB)))
                            .setStorageSource(StorageSource.AZURE_BLOB),
                        new TargetInput("https://myblob.blob.core.windows.net/destinationContainer2", "es")
                            .setCategory("general")
                            .setStorageSource(StorageSource.AZURE_BLOB)))
                .setStorageType(StorageInputType.FOLDER))));
```
Please refer to the service documentation for a conceptual discussion of [batchTranslation][batchTranslation_doc].

### Single Document Translation 
Synchronously translate a single document.

```java SingleDocumentTranslation
DocumentFileDetails document = createDocumentContent();
DocumentTranslateContent documentTranslateContent = new DocumentTranslateContent(document);
String targetLanguage = "hi";    

BinaryData response = singleDocumentTranslationClient.documentTranslate(targetLanguage, documentTranslateContent);        
String translatedResponse = response.toString();
System.out.println("Translated Response: " + translatedResponse);
```
Please refer to the service documentation for a conceptual discussion of [singleDocumentTranslation][singleDocumentTranslation_doc].

### Cancel Translation
Cancels a translation job that is currently processing or queued (pending) as indicated in the request by the id query parameter.

```java CancelDocumentTranslation
DocumentTranslationClient documentTranslationClient = new DocumentTranslationClientBuilder()
    .endpoint("{endpoint}")
    .credential(new AzureKeyCredential("{key}"))
    .buildClient();
       
SyncPoller<TranslationStatus, Void> response
    = documentTranslationClient
        .beginStartTranslation(
            new StartTranslationDetails(Arrays.asList(new BatchRequest(
                new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                    .setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"))
                    .setLanguage("en")
                    .setStorageSource(StorageSource.AZURE_BLOB),
                Arrays
                    .asList(
                        new TargetInput("https://myblob.blob.core.windows.net/destinationContainer1", "fr")
                            .setCategory("general")
                            .setGlossaries(Arrays.asList(new Glossary(
                                "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf", "XLIFF")
                                .setStorageSource(StorageSource.AZURE_BLOB)))
                            .setStorageSource(StorageSource.AZURE_BLOB),
                        new TargetInput("https://myblob.blob.core.windows.net/destinationContainer2", "es")
                            .setCategory("general")
                            .setStorageSource(StorageSource.AZURE_BLOB)))
                .setStorageType(StorageInputType.FOLDER))));

String translationId = response.poll().getValue().getId();
documentTranslationClient.cancelTranslation(translationId);        
TranslationStatus translationStatus = documentTranslationClient.getTranslationStatus(translationId);

System.out.println("Translation ID is: " + translationStatus.getId());
System.out.println("Translation status is: " + translationStatus.getStatus().toString());
```
Please refer to the service documentation for a conceptual discussion of [cancelTranslation][cancelTranslation_doc].

### Get Translations Status
Gets a list and the status of all translation jobs submitted by the user (associated with the resource).

```java GetTranslationsStatus
SyncPoller<TranslationStatus, Void> response = documentTranslationClient
        .beginStartTranslation(
                new StartTranslationDetails(Arrays.asList(new BatchRequest(
                        new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                                .setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"))
                                .setLanguage("en")
                                .setStorageSource(StorageSource.AZURE_BLOB),
                        Arrays
                                .asList(
                                        new TargetInput(
                                                "https://myblob.blob.core.windows.net/destinationContainer1",
                                                "fr")
                                                .setCategory("general")
                                                .setGlossaries(Arrays.asList(new Glossary(
                                                        "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
                                                        "XLIFF")
                                                        .setStorageSource(StorageSource.AZURE_BLOB)))
                                                .setStorageSource(StorageSource.AZURE_BLOB),
                                        new TargetInput(
                                                "https://myblob.blob.core.windows.net/destinationContainer2",
                                                "es")
                                                .setCategory("general")
                                                .setStorageSource(StorageSource.AZURE_BLOB)))
                        .setStorageType(StorageInputType.FOLDER))));

PagedIterable<TranslationStatus> translationStatuses = documentTranslationClient.getTranslationsStatus();
for (TranslationStatus translationStatus : translationStatuses) {
    System.out.println("Translation ID is: " + translationStatus.getId());
    System.out.println("Translation status is: " + translationStatus.getStatus().toString());
}
```
Please refer to the service documentation for a conceptual discussion of [getTranslationsStatus][getTranslationsStatus_doc].

### Get Translation Status
Request a summary of the status for a specific translation job. The response includes the overall job status and the status for documents that are being translated as part of that job.

```java GetTranslationStatus
SyncPoller<TranslationStatus, Void> response
    = documentTranslationClient
        .beginStartTranslation(
            new StartTranslationDetails(Arrays.asList(new BatchRequest(
                new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                    .setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"))
                    .setLanguage("en")
                    .setStorageSource(StorageSource.AZURE_BLOB),
                Arrays
                    .asList(
                        new TargetInput("https://myblob.blob.core.windows.net/destinationContainer1", "fr")
                            .setCategory("general")
                            .setGlossaries(Arrays.asList(new Glossary(
                                "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf", "XLIFF")
                                .setStorageSource(StorageSource.AZURE_BLOB)))
                            .setStorageSource(StorageSource.AZURE_BLOB),
                        new TargetInput("https://myblob.blob.core.windows.net/destinationContainer2", "es")
                            .setCategory("general")
                            .setStorageSource(StorageSource.AZURE_BLOB)))
                .setStorageType(StorageInputType.FOLDER))));

String translationId = response.poll().getValue().getId();      
TranslationStatus translationStatus = documentTranslationClient.getTranslationStatus(translationId);

System.out.println("Translation ID is: " + translationStatus.getId());
System.out.println("Translation status is: " + translationStatus.getStatus().toString());
```

Please refer to the service documentation for a conceptual discussion of [getTranslationStatus][getTranslationStatus_doc].

### Get Documents Status
Gets the status for all documents in a translation job.

```java GetDocumentsStatus
SyncPoller<TranslationStatus, Void> response = documentTranslationClient
        .beginStartTranslation(
                new StartTranslationDetails(Arrays.asList(new BatchRequest(
                        new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                                .setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"))
                                .setLanguage("en")
                                .setStorageSource(StorageSource.AZURE_BLOB),
                        Arrays
                                .asList(
                                        new TargetInput(
                                                "https://myblob.blob.core.windows.net/destinationContainer1",
                                                "fr")
                                                .setCategory("general")
                                                .setGlossaries(Arrays.asList(new Glossary(
                                                        "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
                                                        "XLIFF")
                                                        .setStorageSource(StorageSource.AZURE_BLOB)))
                                                .setStorageSource(StorageSource.AZURE_BLOB),
                                        new TargetInput(
                                                "https://myblob.blob.core.windows.net/destinationContainer2",
                                                "es")
                                                .setCategory("general")
                                                .setStorageSource(StorageSource.AZURE_BLOB)))
                        .setStorageType(StorageInputType.FOLDER))));

String translationId = response.poll().getValue().getId();

// Add Status filter
List<String> succeededStatusList = Arrays.asList(Status.SUCCEEDED.toString());
try {
    PagedIterable<DocumentStatus> documentStatusResponse = documentTranslationClient
            .getDocumentsStatus(translationId, null, null, null, succeededStatusList, null, null, null);
    for (DocumentStatus documentStatus : documentStatusResponse) {
        String id = documentStatus.getId();
        System.out.println("Document Translation ID is: " + id);
        String status = documentStatus.getStatus().toString();
        System.out.println("Document Translation status is: " + status);
    }
} catch (Exception e) {
    System.err.println("An exception occurred: " + e.getMessage());
    e.printStackTrace();
}
```
Please refer to the service documentation for a conceptual discussion of [getDocumentsStatus][getDocumentsStatus_doc].

### Get Document Status
Request the status for a specific document in a job.

```java GetDocumentStatus
SyncPoller<TranslationStatus, Void> response = documentTranslationClient
        .beginStartTranslation(
                new StartTranslationDetails(Arrays.asList(new BatchRequest(
                        new SourceInput("https://myblob.blob.core.windows.net/sourceContainer")
                                .setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"))
                                .setLanguage("en")
                                .setStorageSource(StorageSource.AZURE_BLOB),
                        Arrays
                                .asList(
                                        new TargetInput(
                                                "https://myblob.blob.core.windows.net/destinationContainer1",
                                                "fr")
                                                .setCategory("general")
                                                .setGlossaries(Arrays.asList(new Glossary(
                                                        "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
                                                        "XLIFF")
                                                        .setStorageSource(StorageSource.AZURE_BLOB)))
                                                .setStorageSource(StorageSource.AZURE_BLOB),
                                        new TargetInput(
                                                "https://myblob.blob.core.windows.net/destinationContainer2",
                                                "es")
                                                .setCategory("general")
                                                .setStorageSource(StorageSource.AZURE_BLOB)))
                        .setStorageType(StorageInputType.FOLDER))));

String translationId = response.poll().getValue().getId();

// Add Status filter
List<String> succeededStatusList = Arrays.asList(Status.SUCCEEDED.toString());
try {
    PagedIterable<DocumentStatus> documentStatusResponse = documentTranslationClient
            .getDocumentsStatus(translationId, null, null, null, succeededStatusList, null, null, null);
    for (DocumentStatus documentsStatus : documentStatusResponse) {
        String id = documentsStatus.getId();
        System.out.println("Document Translation ID is: " + id);
        DocumentStatus documentStatus = documentTranslationClient.getDocumentStatus(translationId, id);
        System.out.println("Document ID is: " + documentStatus.getId());
        System.out.println("Document Status is: " + documentStatus.getStatus().toString());
        System.out.println("Characters Charged is: " + documentStatus.getCharacterCharged().toString());
        System.out.println("Document path is: " + documentStatus.getPath());
        System.out.println("Document source path is: " + documentStatus.getSourcePath());
    }
} catch (Exception e) {
    System.err.println("An exception occurred: " + e.getMessage());
    e.printStackTrace();
}
```
Please refer to the service documentation for a conceptual discussion of [getDocumentStatus][getDocumentStatus_doc].

## Troubleshooting
When you interact with the Document Translator Service using the DocumentTranslator client library, errors returned by the service correspond to the same HTTP status codes returned for REST API requests.

For example, if you submit a document translation request without a target translate language, a `400` error is returned, indicating "Bad Request".

## Next steps
Samples showing how to use this client library are available in this GitHub repository.
Samples are provided for each main functional area.

* [BatchDocumentTranslation][sample_batchDocumentTranslation]
* [SingleDocumentTranslation][sample_singleDocumentTranslation]
* [CancelTranslation][sample_cancelTranslation]
* [GetTranslationsStatus][sample_getTranslationsStatus]
* [GetTranslationStatus][sample_getTranslationStatus]
* [GetDocumentsStatus][sample_getDocumentsStatus]
* [GetDocumentStatus][sample_getDocumentStatus]
* [GetSupportedFormats][sample_getSupportedFormats]

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[product_documentation]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/overview
[docs]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/rest-api-guide
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[documentFormats_doc]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-supported-document-formats
[glossaryFormats]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-supported-glossary-formats
[batchTranslation_doc]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/start-batch-translation
[singleDocumentTranslation_doc]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/translate-document
[cancelTranslation_doc]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/cancel-translation
[getTranslationsStatus_doc]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-translations-status
[getTranslationStatus_doc]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-translation-status
[getDocumentsStatus_doc]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-documents-status
[getDocumentStatus_doc]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/reference/get-document-status
[document_translator_client_class]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-document/src/main/java/com/azure/ai/translation/document/DocumentTranslationClient.java
[single_document_translator_client_class]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/translation/azure-ai-translation-document/src/main/java/com/azure/ai/translation/document/SingleDocumentTranslationClient.java
[sample_batchDocumentTranslation]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/StartDocumentTranslation.java
[sample_singleDocumentTranslation]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/StartSingleDocumentTranslation.java
[sample_cancelTranslation]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/CancelDocumentTranslation.java
[sample_getTranslationsStatus]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/GetTranslationsStatus.java
[sample_getTranslationStatus]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/GetTranslationStatus.java
[sample_getDocumentsStatus]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/GetDocumentsStatus.java
[sample_getDocumentStatus]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/GetDocumentStatus.java
[sample_getSupportedFormats]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/GetSupportedFormats.java

