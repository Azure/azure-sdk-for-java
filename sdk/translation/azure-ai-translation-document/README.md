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
    <version>1.1.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

Interaction with the service using the client library begins with creating an instance of the [DocumentTranslationClient][document_translator_client_class] class. You will need an **API key** or ``TokenCredential`` and **Endpoint** to instantiate a document translation client object. Similarly for [SingleDocumentTranslationclient][single_document_translator_client_class]

Managed identities for Azure resources are service principals that create a Microsoft Entra identity and specific permissions for Azure managed resources. Managed identities are a safer way to grant access to storage data and replace the requirement for you to include shared access signature tokens (SAS) with your source and target URLs.
Here is more information on [Managed identities for Document Translation] [managed_identities_for_document_translation].

#### Create DocumentTranslationClient and SingleDocumentTranslationClient using Azure Active Directory credential
Azure SDK for Java supports an Azure Identity package, making it easy to get credentials from Microsoft identity
platform.

Authentication with AAD requires some initial setup:
* Add the Azure Identity package

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.15.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

After setup, you can choose which type of [credential][azure_identity_credential_type] from `azure-identity` to use.
We recommend using [DefaultAzureCredential][identity_dac], configured through the `AZURE_TOKEN_CREDENTIALS` environment variable.
Set this variable as described in the [Learn documentation][customize_defaultAzureCredential], which provides the most up-to-date guidance and examples.

```java createDocumentTranslationClientWithAAD
String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");

TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
DocumentTranslationClient client = new DocumentTranslationClientBuilder()
    .endpoint(endpoint)
    .credential(defaultCredential)
    .buildClient();
```

```java createSingleDocumentTranslationClientWithAAD
String endpoint = System.getenv("DOCUMENT_TRANSLATION_ENDPOINT");

TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

SingleDocumentTranslationClient client = new SingleDocumentTranslationClientBuilder()
    .endpoint(endpoint)
    .credential(defaultCredential)
    .buildClient();
```
#### Create a `DocumentTranslationClient` or `SingleDocumentTranslationClient` using endpoint and API key credential

You can get the `endpoint`, `API key` and `Region` from the Cognitive Services resource or Document Translator service resource information in the [Azure Portal][azure_portal].

Alternatively, use the [Azure CLI][azure_cli] snippet below to get the API key from the Translator service resource.

```PowerShell
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```

Please refer to these samples for creating [DocumentTranslationClientWithAPIKey][sample_document_translation_client_with_apiKey] and [SingleDocumentTranslationClientWithAPIKey][sample_single_document_translation_client_with_apiKey].


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
List<FileFormat> documentFileFormats = documentTranslationClient.getSupportedFormats(FileFormatType.DOCUMENT);
for (FileFormat fileFormat : documentFileFormats) {
    System.out.println("FileFormat:" + fileFormat.getFormat());
    System.out.println("FileExtensions:" + fileFormat.getFileExtensions());
    System.out.println("ContentTypes:" + fileFormat.getContentTypes());
    System.out.println("Type:" + fileFormat.getType());
}

List<FileFormat> glossaryFileFormats = documentTranslationClient.getSupportedFormats(FileFormatType.GLOSSARY);
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
String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
TranslationSource translationSource = new TranslationSource(sourceUrl);
translationSource.setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"));
translationSource.setLanguage("en");
translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl1 = "https://myblob.blob.core.windows.net/destinationContainer1";
TranslationTarget translationTarget1 = new TranslationTarget(targetUrl1, "fr");
translationTarget1.setCategory("general");

TranslationGlossary translationGlossary = new TranslationGlossary(
    "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
    "XLIFF");
List<TranslationGlossary> translationGlossaries = new ArrayList<>();
translationGlossaries.add(translationGlossary);
translationTarget1.setGlossaries(translationGlossaries);
translationTarget1.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl2 = "https://myblob.blob.core.windows.net/destinationContainer2";
TranslationTarget translationTarget2 = new TranslationTarget(targetUrl2, "fr");
translationTarget2.setCategory("general");
translationTarget2.setStorageSource(TranslationStorageSource.AZURE_BLOB);

List<TranslationTarget> translationTargets = new ArrayList<>();
translationTargets.add(translationTarget1);
translationTargets.add(translationTarget2);

DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);
batchRequest.setStorageType(StorageInputType.FOLDER);

SyncPoller<TranslationStatusResult, TranslationStatusResult> response = documentTranslationClient
    .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest));
TranslationStatusResult translationStatus = response.waitForCompletion().getValue();
```
Please refer to the service documentation for a conceptual discussion of [batchTranslation][batchTranslation_doc].

### Single Document Translation 
Synchronously translate a single document.

```java SingleDocumentTranslation
DocumentFileDetails document = createDocumentContent();
DocumentTranslateContent documentTranslateContent = new DocumentTranslateContent(document);
String targetLanguage = "hi";

BinaryData response = singleDocumentTranslationClient.translate(targetLanguage, documentTranslateContent);
String translatedResponse = response.toString();
System.out.println("Translated Response: " + translatedResponse);
```
Please refer to the service documentation for a conceptual discussion of [singleDocumentTranslation][singleDocumentTranslation_doc].

### Cancel Translation
Cancels a translation job that is currently processing or queued (pending) as indicated in the request by the id query parameter.

```java CancelDocumentTranslation
String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
TranslationSource translationSource = new TranslationSource(sourceUrl);
translationSource.setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"));
translationSource.setLanguage("en");
translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl1 = "https://myblob.blob.core.windows.net/destinationContainer1";
TranslationTarget translationTarget1 = new TranslationTarget(targetUrl1, "fr");
translationTarget1.setCategory("general");

TranslationGlossary translationGlossary = new TranslationGlossary(
    "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
    "XLIFF");
List<TranslationGlossary> translationGlossaries = new ArrayList<>();
translationGlossaries.add(translationGlossary);
translationTarget1.setGlossaries(translationGlossaries);
translationTarget1.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl2 = "https://myblob.blob.core.windows.net/destinationContainer2";
TranslationTarget translationTarget2 = new TranslationTarget(targetUrl2, "fr");
translationTarget2.setCategory("general");
translationTarget2.setStorageSource(TranslationStorageSource.AZURE_BLOB);

List<TranslationTarget> translationTargets = new ArrayList<>();
translationTargets.add(translationTarget1);
translationTargets.add(translationTarget2);

DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);
batchRequest.setStorageType(StorageInputType.FOLDER);

SyncPoller<TranslationStatusResult, TranslationStatusResult> response = documentTranslationClient
    .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest));

String translationId = response.poll().getValue().getId();
documentTranslationClient.cancelTranslation(translationId);
TranslationStatusResult translationStatus = documentTranslationClient
    .getTranslationStatus(translationId);

System.out.println("Translation ID is: " + translationStatus.getId());
System.out.println("Translation status is: " + translationStatus.getStatus().toString());
```
Please refer to the service documentation for a conceptual discussion of [cancelTranslation][cancelTranslation_doc].

### Get Translations Status
Gets a list and the status of all translation jobs submitted by the user (associated with the resource).

```java GetTranslationsStatus
String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
TranslationSource translationSource = new TranslationSource(sourceUrl);
translationSource.setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"));
translationSource.setLanguage("en");
translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl1 = "https://myblob.blob.core.windows.net/destinationContainer1";
TranslationTarget translationTarget1 = new TranslationTarget(targetUrl1, "fr");
translationTarget1.setCategory("general");

TranslationGlossary translationGlossary = new TranslationGlossary(
    "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
    "XLIFF");
List<TranslationGlossary> translationGlossaries = new ArrayList<>();
translationGlossaries.add(translationGlossary);
translationTarget1.setGlossaries(translationGlossaries);
translationTarget1.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl2 = "https://myblob.blob.core.windows.net/destinationContainer2";
TranslationTarget translationTarget2 = new TranslationTarget(targetUrl2, "fr");
translationTarget2.setCategory("general");
translationTarget2.setStorageSource(TranslationStorageSource.AZURE_BLOB);

List<TranslationTarget> translationTargets = new ArrayList<>();
translationTargets.add(translationTarget1);
translationTargets.add(translationTarget2);

DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);
batchRequest.setStorageType(StorageInputType.FOLDER);

SyncPoller<TranslationStatusResult, TranslationStatusResult> response = documentTranslationClient
    .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest));

PagedIterable < TranslationStatusResult> translationStatuses = documentTranslationClient
    .listTranslationStatuses();
for (TranslationStatusResult translationStatus: translationStatuses) {
    System.out.println("Translation ID is: " + translationStatus.getId());
    System.out.println("Translation status is: " + translationStatus.getStatus().toString());
}
```
Please refer to the service documentation for a conceptual discussion of [getTranslationsStatus][getTranslationsStatus_doc].

### Get Translation Status
Request a summary of the status for a specific translation job. The response includes the overall job status and the status for documents that are being translated as part of that job.

```java GetTranslationStatus
String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
TranslationSource translationSource = new TranslationSource(sourceUrl);
translationSource.setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"));
translationSource.setLanguage("en");
translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl1 = "https://myblob.blob.core.windows.net/destinationContainer1";
TranslationTarget translationTarget1 = new TranslationTarget(targetUrl1, "fr");
translationTarget1.setCategory("general");

TranslationGlossary translationGlossary = new TranslationGlossary(
    "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
    "XLIFF");
List<TranslationGlossary> translationGlossaries = new ArrayList<>();
translationGlossaries.add(translationGlossary);
translationTarget1.setGlossaries(translationGlossaries);
translationTarget1.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl2 = "https://myblob.blob.core.windows.net/destinationContainer2";
TranslationTarget translationTarget2 = new TranslationTarget(targetUrl2, "fr");
translationTarget2.setCategory("general");
translationTarget2.setStorageSource(TranslationStorageSource.AZURE_BLOB);

List<TranslationTarget> translationTargets = new ArrayList<>();
translationTargets.add(translationTarget1);
translationTargets.add(translationTarget2);

DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);
batchRequest.setStorageType(StorageInputType.FOLDER);

SyncPoller<TranslationStatusResult, TranslationStatusResult> response = documentTranslationClient
    .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest));

String translationId = response.poll().getValue().getId();
TranslationStatusResult translationStatus = documentTranslationClient
    .getTranslationStatus(translationId);

System.out.println("Translation ID is: " + translationStatus.getId());
System.out.println("Translation status is: " + translationStatus.getStatus().toString());
```

Please refer to the service documentation for a conceptual discussion of [getTranslationStatus][getTranslationStatus_doc].

### Get Documents Status
Gets the status for all documents in a translation job.

```java GetDocumentsStatus
String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
TranslationSource translationSource = new TranslationSource(sourceUrl);
translationSource.setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"));
translationSource.setLanguage("en");
translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl1 = "https://myblob.blob.core.windows.net/destinationContainer1";
TranslationTarget translationTarget1 = new TranslationTarget(targetUrl1, "fr");
translationTarget1.setCategory("general");

TranslationGlossary translationGlossary = new TranslationGlossary(
    "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
    "XLIFF");
List<TranslationGlossary> translationGlossaries = new ArrayList<>();
translationGlossaries.add(translationGlossary);
translationTarget1.setGlossaries(translationGlossaries);
translationTarget1.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl2 = "https://myblob.blob.core.windows.net/destinationContainer2";
TranslationTarget translationTarget2 = new TranslationTarget(targetUrl2, "fr");
translationTarget2.setCategory("general");
translationTarget2.setStorageSource(TranslationStorageSource.AZURE_BLOB);

List<TranslationTarget> translationTargets = new ArrayList<>();
translationTargets.add(translationTarget1);
translationTargets.add(translationTarget2);

DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);
batchRequest.setStorageType(StorageInputType.FOLDER);

SyncPoller<TranslationStatusResult, TranslationStatusResult> response = documentTranslationClient
    .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest));

String translationId = response.poll().getValue().getId();

// Add Status filter
List<String> succeededStatusList = Arrays.asList(TranslationStatus.SUCCEEDED.toString());

ListDocumentStatusesOptions listDocumentStatusesOptions
    = new ListDocumentStatusesOptions(translationId).setStatuses(succeededStatusList);
try {
    PagedIterable < DocumentStatusResult> documentStatusResponse = documentTranslationClient
        .listDocumentStatuses(listDocumentStatusesOptions);
    for (DocumentStatusResult documentStatus: documentStatusResponse) {
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
String sourceUrl = "https://myblob.blob.core.windows.net/sourceContainer";
TranslationSource translationSource = new TranslationSource(sourceUrl);
translationSource.setFilter(new DocumentFilter().setPrefix("pre").setSuffix(".txt"));
translationSource.setLanguage("en");
translationSource.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl1 = "https://myblob.blob.core.windows.net/destinationContainer1";
TranslationTarget translationTarget1 = new TranslationTarget(targetUrl1, "fr");
translationTarget1.setCategory("general");

TranslationGlossary translationGlossary = new TranslationGlossary(
    "https://myblob.blob.core.windows.net/myglossary/en_fr_glossary.xlf",
    "XLIFF");
List<TranslationGlossary> translationGlossaries = new ArrayList<>();
translationGlossaries.add(translationGlossary);
translationTarget1.setGlossaries(translationGlossaries);
translationTarget1.setStorageSource(TranslationStorageSource.AZURE_BLOB);

String targetUrl2 = "https://myblob.blob.core.windows.net/destinationContainer2";
TranslationTarget translationTarget2 = new TranslationTarget(targetUrl2, "fr");
translationTarget2.setCategory("general");
translationTarget2.setStorageSource(TranslationStorageSource.AZURE_BLOB);

List<TranslationTarget> translationTargets = new ArrayList<>();
translationTargets.add(translationTarget1);
translationTargets.add(translationTarget2);

DocumentTranslationInput batchRequest = new DocumentTranslationInput(translationSource, translationTargets);
batchRequest.setStorageType(StorageInputType.FOLDER);

SyncPoller<TranslationStatusResult, TranslationStatusResult> response = documentTranslationClient
    .beginTranslation(TestHelper.getStartTranslationDetails(batchRequest));

String translationId = response.poll().getValue().getId();

// Add Status filter
List<String> succeededStatusList = Arrays.asList(TranslationStatus.SUCCEEDED.toString());
ListDocumentStatusesOptions listDocumentStatusesOptions
    = new ListDocumentStatusesOptions(translationId).setStatuses(succeededStatusList);
try {
    PagedIterable<DocumentStatusResult> documentStatusResponse = documentTranslationClient
        .listDocumentStatuses(listDocumentStatusesOptions);
    for (DocumentStatusResult documentsStatus: documentStatusResponse) {
        String id = documentsStatus.getId();
        System.out.println("Document Translation ID is: " + id);
        DocumentStatusResult documentStatus = documentTranslationClient
            .getDocumentStatus(translationId, id);
        System.out.println("Document ID is: " + documentStatus.getId());
        System.out.println("Document Status is: " + documentStatus.getStatus().toString());
        System.out.println("Characters Charged is: "
            + documentStatus.getCharacterCharged().toString());
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
[sample_document_translation_client_with_apiKey]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/ReadmeSamples.java
[sample_single_document_translation_client_with_apiKey]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/translation/azure-ai-translation-document/src/samples/java/com/azure/ai/translation/document/ReadmeSamples.java
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
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[aad_authorization]: https://learn.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[managed_identities_for_document_translation]: https://learn.microsoft.com/azure/ai-services/translator/document-translation/how-to-guides/create-use-managed-identities
[customize_defaultAzureCredential]: https://aka.ms/azsdk/java/identity/credential-chains#how-to-customize-defaultazurecredential
[identity_dac]: https://aka.ms/azsdk/java/identity/credential-chains#defaultazurecredential-overview
