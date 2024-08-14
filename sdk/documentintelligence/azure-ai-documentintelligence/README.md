# Azure DocumentIntelligence client library for Java

Azure Document Intelligence ([previously known as Form Recognizer][service-rename]) is a cloud service that uses machine
learning to analyze text and structured data from your documents.
It includes the following main features:

* Layout - Analyze text, table structures, and selection marks, along with their bounding region coordinates, from documents.
* Prebuilt - Analyze data from certain types of common documents (such as receipts, invoices, identity documents or US W2 tax forms) using prebuilt models.
* Custom - Build custom models to extract text, field values, selection marks, and table data from documents. Custom models are built with your own data, so they're tailored to your documents.
* Read - Read information about textual elements, such as page words and lines in addition to text language information.
* Classifiers - Build custom classifiers to categorize documents into predefined classes.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk_link] with version 8 or above
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Azure Subscription][azure_subscription]
- [AI Services or Document Intelligence account][form_recognizer_account] to use this package.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-ai-documentintelligence;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-documentintelligence</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

> Note: This version of the client library defaults to the `"2024-07-31-preview"` version of the service.

This table shows the relationship between SDK versions and supported API versions of the service:

| SDK version  | Supported API version of service |
|--------------|----------------------------------|
| 1.0.0-beta.1 | 2023-10-31-preview               |
| 1.0.0-beta.2 | 2024-02-29-preview               |
| 1.0.0-beta.3 | 2024-02-29-preview               |
| 1.0.0-beta.3 | 2024-07-31-preview               |

> Note: Please rely on the older `azure-ai-formrecognizer` library through the older service API versions for retired
> models, such as `"prebuilt-businessCard"` and `"prebuilt-document"`. For more information, see [Changelog][changelog].
> The below table describes the relationship of each client and its supported API version(s):

| API version                                                | Supported clients                                                                             |
|------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| 2023-10-31-preview, 2024-02-29-preview, 2024-07-31-preview | DocumentIntelligenceClient and DocumentIntelligenceAsyncClient                                |
| 2023-07-31                                                 | DocumentAnalysisClient and DocumentModelAdministrationClient in `azure-ai-formrecognizer` SDK |

Please see the [Migration Guide][migration_guide] for more information about migrating from `azure-ai-formrecognizer` to `azure-ai-documentintelligence`.

### Authentication

In order to interact with the Azure Document Intelligence Service you'll need to create an instance of client class,
[DocumentIntelligenceAsyncClient][document_analysis_async_client] or [DocumentIntelligenceClient][document_analysis_sync_client] by using
[DocumentIntelligenceClientBuilder][document_analysis_client_builder]. To configure a client for use with
Azure DocumentIntelligence, provide a valid endpoint URI to an Azure DocumentIntelligence resource along with a corresponding key credential,
token credential, or [Azure Identity][azure_identity] credential that's authorized to use the Azure DocumentIntelligence resource.

#### Create an Azure DocumentIntelligence client with key credential
Get Azure DocumentIntelligence `key` credential from the Azure Portal.

```java com.azure.ai.documentintelligence.readme.createDocumentAnalysisClient
DocumentIntelligenceClient documentIntelligenceClient = new DocumentIntelligenceClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
or
```java readme-sample-createDocumentModelAdministrationClient
DocumentIntelligenceAdministrationClient client =
    new DocumentIntelligenceAdministrationClientBuilder()
        .credential(new AzureKeyCredential("{key}"))
        .endpoint("{endpoint}")
        .buildClient();
```

#### Create an Azure DocumentIntelligence client with Azure Active Directory credential
Azure SDK for Java supports an Azure Identity package, making it easy to get credentials from Microsoft identity
platform.

Authentication with AAD requires some initial setup:
* Add the Azure Identity package

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

After setup, you can choose which type of [credential][azure_identity_credential_type] from azure-identity to use.
As an example, [DefaultAzureCredential][wiki_identity] can be used to authenticate the client:
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables:
`AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_CLIENT_SECRET`.

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with DocumentIntelligence service, please
refer to [the associated documentation][aad_authorization].

```java com.azure.ai.documentanalysis.readme.DocumentAnalysisAsyncClient.withAAD
DocumentIntelligenceAsyncClient documentIntelligenceAsyncClient = new DocumentIntelligenceClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

## Key concepts
### DocumentAnalysisClient
The [DocumentAnalysisClient][document_analysis_sync_client] and [DocumentAnalysisAsyncClient][document_analysis_async_client]
provide both synchronous and asynchronous operations for analyzing input documents using custom and prebuilt models
through the `beginAnalyzeDocument` API.
See a full list of supported models [here][fr_models].

Sample code snippets to illustrate using a DocumentAnalysisClient [here][sample_readme].
More information about analyzing documents, including supported features, locales, and document types can be found
[here][fr_models].

### DocumentModelAdministrationClient
The [DocumentModelAdministrationClient][document_model_admin_sync_client] and
[DocumentModelAdministrationAsyncClient][document_model_admin_async_client] provide both synchronous and asynchronous operations
- Build custom document analysis models to analyze text content, fields, and values found in your custom documents. See example [Build a document model](#build-a-document-model).
  A `DocumentModelDetails` is returned indicating the document types that the model can analyze, along with the fields and schemas it will extract.
- Managing models created in your account by building, listing, deleting, and see the limit of custom models your account. See example [Manage models](#manage-your-models).
- Copying a custom model from one Document Intelligence resource to another.
- Creating a composed model from a collection of existing built models.
- Listing document model operations associated with the Document Intelligence resource.

Sample code snippets are provided to illustrate using a DocumentModelAdministrationClient [here](#examples "Examples").

### Long-running operations
Long-running operations are operations that consist of an initial request sent to the service to start an operation,
followed by polling the service at intervals to determine whether the operation has completed or failed, and if it has
succeeded, to get the result.

Methods that build models, analyze values from documents, or copy and compose models are modeled as long-running operations.
The client exposes a `begin<MethodName>` method that returns a `SyncPoller` or `PollerFlux` instance.
Callers should wait for the operation to be completed by calling `getFinalResult()` on the returned operation from the
`begin<MethodName>` method. Sample code snippets are provided to illustrate using long-running operations
[below](#examples).

## Examples

The following section provides several code snippets covering some of the most common Document Intelligence tasks, including:

* [Analyze Layout](#analyze-layout "Analyze Layout")
* [Use Prebuilt Models](#use-prebuilt-models)
* [Build a Document Model](#build-a-document-model "Build a Document Model")
* [Analyze Documents using a Custom Model](#analyze-documents-using-a-custom-model "Analyze Documents using a Custom Model")
* [Manage Your Models](#manage-your-models "Manage Your Models")

### Analyze Layout
Analyze text, table structures, and selection marks like radio buttons and check boxes, along with their bounding box coordinates from documents without the need to build a model.
```java com.azure.ai.documentintelligence.readme.analyzeLayout
File layoutDocument = new File("local/file_path/filename.png");
Path filePath = layoutDocument.toPath();
BinaryData layoutDocumentData = BinaryData.fromFile(filePath, (int) layoutDocument.length());

SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeLayoutResultPoller =
    documentIntelligenceClient.beginAnalyzeDocument("prebuilt-layout",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(layoutDocument.toPath())));

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

### Use Prebuilt Models
Extract fields from select document types such as receipts, invoices, and identity documents using prebuilt models provided by the Document Intelligence service.
Supported prebuilt models are:
- Analyze receipts using the `prebuilt-receipt` model (fields recognized by the service can be found [here][service_analyze_receipt_fields])
- Analyze invoices using the `prebuilt-invoice` model (fields recognized by the service can be found [here][service_analyze_invoices_fields]).
- Analyze identity documents using the `prebuilt-idDocuments` model (fields recognized by the service can be found [here][service_analyze_identity_documents_fields]).
- Analyze US W2 tax forms using the `prebuilt-tax.us.w2` model. [Supported fields][service_analyze_w2_documents_fields].

For example, to analyze fields from a sales receipt, into the `beginAnalyzeDocumentFromUrl` method:
```java com.azure.ai.documentintelligence.readme.analyzeReceipt
File sourceFile = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/"
    + "sample-forms/receipts/contoso-allinone.jpg");

SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeReceiptPoller =
    documentIntelligenceClient.beginAnalyzeDocument("prebuilt-receipt",
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(sourceFile.toPath())));

AnalyzeResult receiptResults = analyzeReceiptPoller.getFinalResult();

for (int i = 0; i < receiptResults.getDocuments().size(); i++) {
    Document analyzedReceipt = receiptResults.getDocuments().get(i);
    Map<String, DocumentField> receiptFields = analyzedReceipt.getFields();
    System.out.printf("----------- Analyzing receipt info %d -----------%n", i);
    DocumentField merchantNameField = receiptFields.get("MerchantName");
    if (merchantNameField != null) {
        if (DocumentFieldType.STRING == merchantNameField.getType()) {
            String merchantName = merchantNameField.getValueString();
            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                merchantName, merchantNameField.getConfidence());
        }
    }

    DocumentField merchantPhoneNumberField = receiptFields.get("MerchantPhoneNumber");
    if (merchantPhoneNumberField != null) {
        if (DocumentFieldType.PHONE_NUMBER == merchantPhoneNumberField.getType()) {
            String merchantAddress = merchantPhoneNumberField.getValuePhoneNumber();
            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                merchantAddress, merchantPhoneNumberField.getConfidence());
        }
    }

    DocumentField merchantAddressField = receiptFields.get("MerchantAddress");
    if (merchantAddressField != null) {
        if (DocumentFieldType.STRING == merchantAddressField.getType()) {
            String merchantAddress = merchantAddressField.getValueString();
            System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                merchantAddress, merchantAddressField.getConfidence());
        }
    }

    DocumentField transactionDateField = receiptFields.get("TransactionDate");
    if (transactionDateField != null) {
        if (DocumentFieldType.DATE == transactionDateField.getType()) {
            LocalDate transactionDate = transactionDateField.getValueDate();
            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                transactionDate, transactionDateField.getConfidence());
        }
    }
}
```

For more information and samples using prebuilt models, see:
- [Identity Documents][analyze_identity_documents_from_url]
- [Invoices][analyze_invoices_from_url]
- [Receipts sample][analyze_receipts_from_url]

### Build a document model
Build a machine-learned model on your own document type. The resulting model will be able to analyze values from the types of documents it was built on.
Provide a container SAS url to your Azure Storage Blob container where you're storing the training documents. See details on setting this up
in the [service quickstart documentation][quickstart_training].

**Note**

You can use the [Document Intelligence Studio preview][fr-studio] for creating a labeled file for your training forms.
More details on setting up a container and required file structure can be found in [here][fr_build_training_set].

```java com.azure.ai.documentintelligence.readme.buildModel
// Build custom document analysis model
String blobContainerUrl = "{SAS_URL_of_your_container_in_blob_storage}";
// The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
SyncPoller<DocumentModelBuildOperationDetails, DocumentModelDetails> buildOperationPoller =
    administrationClient.beginBuildDocumentModel(new BuildDocumentModelRequest("modelID", DocumentBuildMode.TEMPLATE)
        .setAzureBlobSource(new AzureBlobContentSource(blobContainerUrl)));

DocumentModelDetails documentModelDetails = buildOperationPoller.getFinalResult();

// Model Info
System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
System.out.printf("Model created on: %s%n%n", documentModelDetails.getCreatedDateTime());

System.out.println("Document Fields:");
documentModelDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
    documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
        System.out.printf("Field: %s", field);
        System.out.printf("Field type: %s", documentFieldSchema.getType());
        System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
    });
});
```

### Analyze Documents using a Custom Model
Analyze the key/value pairs and table data from documents. These models are built with your own data,
so they're tailored to your documents. You should only analyze documents of the same doc type that the custom model
was built on.
```java com.azure.ai.documentintelligence.readme.analyzeCustomModel
String documentUrl = "{document-url}";
String modelId = "{custom-built-model-ID}";
SyncPoller<AnalyzeResultOperation, AnalyzeResult> analyzeDocumentPoller = documentIntelligenceClient.beginAnalyzeDocument(modelId,
    "1",
    "en-US",
    StringIndexType.TEXT_ELEMENTS,
    Arrays.asList(DocumentAnalysisFeature.LANGUAGES),
    null,
    ContentFormat.TEXT,
    null,
    new AnalyzeDocumentRequest().setUrlSource(documentUrl));

AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult();

for (int i = 0; i < analyzeResult.getDocuments().size(); i++) {
    final Document analyzedDocument = analyzeResult.getDocuments().get(i);
    System.out.printf("----------- Analyzing custom document %d -----------%n", i);
    System.out.printf("Analyzed document has doc type %s with confidence : %.2f%n",
        analyzedDocument.getDocType(), analyzedDocument.getConfidence());
}

analyzeResult.getPages().forEach(documentPage -> {
    System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
        documentPage.getWidth(),
        documentPage.getHeight(),
        documentPage.getUnit());

    // lines
    documentPage.getLines().forEach(documentLine ->
        System.out.printf("Line '%s' is within a bounding polygon %s.%n",
            documentLine.getContent(),
            documentLine.getPolygon()));

    // words
    documentPage.getWords().forEach(documentWord ->
        System.out.printf("Word '%s' has a confidence score of %.2f.%n",
            documentWord.getContent(),
            documentWord.getConfidence()));
});

// tables
List<DocumentTable> tables = analyzeResult.getTables();
for (int i = 0; i < tables.size(); i++) {
    DocumentTable documentTable = tables.get(i);
    System.out.printf("Table %d has %d rows and %d columns.%n", i, documentTable.getRowCount(),
        documentTable.getColumnCount());
    documentTable.getCells().forEach(documentTableCell -> {
        System.out.printf("Cell '%s', has row index %d and column index %d.%n",
            documentTableCell.getContent(),
            documentTableCell.getRowIndex(), documentTableCell.getColumnIndex());
    });
    System.out.println();
}
```

### Manage your models
Manage the models in your Document Intelligence account.
```java com.azure.ai.documentintelligence.readme.manageModels

ResourceDetails resourceDetails = administrationClient.getResourceInfo();
System.out.printf("The resource has %s models, and we can have at most %s models.%n",
    resourceDetails.getCustomDocumentModels().getCount(), resourceDetails.getCustomDocumentModels().getLimit());

// Next, we get a paged list of all of our models
PagedIterable<DocumentModelDetails> customDocumentModels = administrationClient.listModels();
System.out.println("We have following models in the account:");
customDocumentModels.forEach(documentModelInfo -> {
    System.out.println();
    // get custom document analysis model info
    DocumentModelDetails documentModel = administrationClient.getModel(documentModelInfo.getModelId());
    System.out.printf("Model ID: %s%n", documentModel.getModelId());
    System.out.printf("Model Description: %s%n", documentModel.getDescription());
    System.out.printf("Model created on: %s%n", documentModel.getCreatedDateTime());
    if (documentModel.getDocTypes() != null) {
        documentModel.getDocTypes().forEach((key, documentTypeDetails) -> {
            documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
                System.out.printf("Field: %s, ", field);
                System.out.printf("Field type: %s, ", documentFieldSchema.getType());
                if (documentTypeDetails.getFieldConfidence() != null) {
                    System.out.printf("Field confidence: %.2f%n",
                        documentTypeDetails.getFieldConfidence().get(field));
                }
            });
        });
    }
});
```

For more detailed examples, refer to [samples][sample_examples].

## Troubleshooting
### Enable client logging
You can set the `AZURE_LOG_LEVEL` environment variable to view logging statements made in the client library. For
example, setting `AZURE_LOG_LEVEL=2` would show all informational, warning, and error log messages. The log levels can
be found here: [log levels][logLevels].

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
- Samples are explained in detail [here][samples_readme].

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[aad_authorization]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[azure_key_credential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureKeyCredential.java
[key]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource
[api_reference_doc]: https://learn.microsoft.com/java/api/overview/azure/ai-documentintelligence-readme?view=azure-java-preview
[form_recognizer_doc]: https://aka.ms/azsdk-java-documentintelligence-ref-doc
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_cli]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account-cli?tabs=windows
[azure_cli_endpoint]: https://docs.microsoft.com/cli/azure/cognitiveservices/account?view=azure-cli-latest#az-cognitiveservices-account-show
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[azure_portal]: https://ms.portal.azure.com
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[create_new_resource]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#create-a-new-azure-cognitive-services-resource
[form_recognizer_account]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[grant_access]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[http_response_exception]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[logLevels]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/logging/ClientLogger.java
[package]: https://central.sonatype.com/artifact/com.azure/azure-ai-documentintelligence
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview
[register_AAD_application]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[fr-studio]: https://aka.ms/azsdk/formrecognizer/documentintelligencestudio
[fr_build_training_set]: https://aka.ms/azsdk/formrecognizer/buildcustommodel
[sample_examples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples#examples
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples#readme
[migration_guide]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/documentintelligence/azure-ai-documentintelligence/MIGRATION_GUIDE.md
[changelog]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/CHANGELOG.md

[sample_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/
[document_analysis_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/DocumentIntelligenceAsyncClient.java
[document_analysis_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/DocumentIntelligenceClient.java
[document_model_admin_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/DocumentIntelligenceAdministrationAsyncClient.java
[document_model_admin_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/DocumentIntelligenceAdministrationClient.java
[document_analysis_client_builder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/main/java/com/azure/ai/documentintelligence/DocumentIntelligenceClientBuilder.java
[manage_custom_models]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/administration/ManageCustomModels.java
[manage_custom_models_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/administration/ManageCustomModelsAsync.java
[build_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/administration/BuildDocumentModel.java
[build_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/administration/BuildDocumentModelAsync.java
[build_document_classifier]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/administration/BuildDocumentClassifier.java
[build_document_classifier_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/administration/BuildDocumentClassifierAsync.java
[analyze_identity_documents_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeIdentityDocumentsFromUrl.java
[analyze_identity_documents_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeIdentityDocumentsFromUrlAsync.java
[analyze_invoices_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeInvoicesFromUrl.java
[analyze_receipts_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src/samples/java/com/azure/ai/documentintelligence/AnalyzeReceiptsFromUrl.java

[fr_models]: https://aka.ms/azsdk/formrecognizer/models
[service_access]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[service_analyze_invoices_fields]: https://aka.ms/azsdk/formrecognizer/invoicefieldschema
[service_analyze_identity_documents_fields]: https://aka.ms/azsdk/formrecognizer/iddocumentfieldschema
[service_analyze_receipt_fields]: https://aka.ms/azsdk/formrecognizer/receiptfieldschema
[service_analyze_w2_documents_fields]: https://aka.ms/azsdk/formrecognizer/taxusw2fieldschema
[service-rename]: https://techcommunity.microsoft.com/t5/azure-ai-services-blog/azure-form-recognizer-is-now-azure-ai-document-intelligence-with/ba-p/3875765
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/documentintelligence/azure-ai-documentintelligence/src
[quickstart_training]: https://learn.microsoft.com/azure/applied-ai-services/form-recognizer/quickstarts/get-started-sdks-rest-api?view=form-recog-3.0.0&pivots=programming-language-java
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication
