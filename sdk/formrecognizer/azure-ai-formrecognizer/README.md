# Azure Document Intelligence client library for Java
Azure Document Intelligence ([previously known as Form Recognizer][service-rename]) is a cloud service that uses machine
learning to analyze text and structured data from your documents. 
It includes the following main features:

* Layout - Extract text, table structures, and selection marks, along with their bounding region coordinates, from documents.
* Document - Analyze entities, key-value pairs, tables, and selection marks from documents using the general prebuilt document model.
* Prebuilt - Analyze data from certain types of common documents (such as receipts, invoices, business cards, identity documents or US W2 tax forms) using prebuilt models.
* Custom - Build custom models to extract text, field values, selection marks, and table data from documents. Custom models are built with your own data, so they're tailored to your documents.
* Read - Read information about textual elements, such as page words and lines in addition to text language information.
* Classifiers - Build custom classifiers to categorize documents into predefined classes.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Form Recognizer account][form_recognizer_account] to use this package.

### Include the Package

#### Include the BOM file

Do include the azure-sdk-bom to your project to take dependency on GA version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
Then, include the direct dependency in the dependencies' section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-formrecognizer</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-ai-formrecognizer;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-formrecognizer</artifactId>
    <version>4.1.10</version>
</dependency>
```
[//]: # ({x-version-update-end})
> Note: This version of the client library defaults to the `"2023-07-31"` version of the service.

This table shows the relationship between SDK versions and supported API versions of the service:

| SDK version    |Supported API version of service
|----------------|-
| 3.0.x          | 2.0
| 3.1.X - 3.1.12 | 2.0, 2.1 (default)
| 4.0.0          | 2.0, 2.1, 2022-08-31 (default)
| 4.1.0          | 2.0, 2.1, 2022-08-31, 2023-07-31 (default)

> Note: Starting with version 4.0.X, a new set of clients were introduced to leverage the newest features
> of the Form Recognizer service. Please see the [Migration Guide][migration_guide] for detailed instructions on how to update application
> code from client library version 3.1.X or lower to the latest version. For more information, see [Changelog][changelog].
> The below table describes the relationship of each client and its supported API version(s):

|API version|Supported clients
|-|-
|2023-07-31 | DocumentAnalysisClient and DocumentModelAdministrationClient
|2022-08-31 | DocumentAnalysisClient and DocumentModelAdministrationClient
|2.1 | FormRecognizerClient and FormTrainingClient
|2.0 | FormRecognizerClient and FormTrainingClient

#### Create a Form Recognizer resource
Form Recognizer supports both [multi-service and single-service access][service_access]. Create a Cognitive Service's
resource if you plan to access multiple cognitive services under a single endpoint/key. For Form Recognizer access only,
create a Form Recognizer resource.

You can create either resource using the

**Option 1:** [Azure portal][create_new_resource]

**Option 2:** [Azure CLI][azure_cli]

Below is an example of how you can create a Form Recognizer resource using the CLI:

```bash
# Create a new resource group to hold the Form Recognizer resource -
# if using an existing resource group, skip this step
az group create --name <your-resource-group> --location <location>
```

```bash
# Create Form Recognizer
az cognitiveservices account create \
    --name <your-form-recognizer-resource-name> \
    --resource-group <your-resource-group> \
    --kind FormRecognizer \
    --sku <sku> \
    --location <location> \
    --yes
```
### Authenticate the client
In order to interact with the Form Recognizer service, you will need to create an instance of the Document Analysis client.
Both the asynchronous and synchronous clients can be created by using `DocumentAnalysisClientBuilder`. Invoking `buildClient()`
will create the synchronous client, while invoking `buildAsyncClient` will create its asynchronous counterpart.

You will need an **endpoint**, and a **key** to instantiate a client object.

#### Looking up the endpoint
You can find the **endpoint** for your Form Recognizer resource in the [Azure portal][azure_portal],
or [Azure CLI][azure_cli_endpoint].
```bash
# Get the endpoint for the resource
az cognitiveservices account show --name "resource-name" --resource-group "resource-group-name" --query "endpoint"
```

#### Create a Document Analysis client using AzureKeyCredential
To use `AzureKeyCredential` authentication, provide the [key][key] as a string to the [AzureKeyCredential][azure_key_credential].
This key can be found in the [Azure portal][azure_portal] in your created Form Recognizer
resource, or by running the following Azure CLI command to get the key from the Form Recognizer resource:

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```
Use the API key as the credential parameter to authenticate the client:

```java readme-sample-createDocumentAnalysisClient
DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

```java readme-sample-createDocumentModelAdministrationClient
DocumentModelAdministrationClient client =
    new DocumentModelAdministrationClientBuilder()
        .credential(new AzureKeyCredential("{key}"))
        .endpoint("{endpoint}")
        .buildClient();
```

#### Create a Document Analysis client with Azure Active Directory credential
Azure SDK for Java supports an Azure Identity package, making it easy to get credentials from Microsoft identity
platform.

Authentication with AAD requires some initial setup:
* Add the Azure Identity package

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.13.1</version>
</dependency>
```
[//]: # ({x-version-update-end})
* [Register a new Azure Active Directory application][register_AAD_application]
* [Grant access][grant_access] to Form Recognizer by assigning the `"Cognitive Services User"` role to your service principal.

After the setup, you can choose which type of [credential][azure_identity_credential_type] from azure-identity to use.
As an example, [DefaultAzureCredential][wiki_identity] can be used to authenticate the client:
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables:
AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with Form Recognizer, see [the associated documentation][aad_authorization].

```java readme-sample-createDocumentAnalysisClientWithAAD
DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
    .endpoint("{endpoint}")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

## Key concepts
### DocumentAnalysisClient
The [DocumentAnalysisClient][document_analysis_sync_client] and [DocumentAnalysisAsyncClient][document_analysis_async_client]
provide both synchronous and asynchronous operations for analyzing input documents using custom and prebuilt models
through the `beginAnalyzeDocument` and `beginAnalyzeDocumentFromUrl` methods.
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
- Copying a custom model from one Form Recognizer resource to another.
- Creating a composed model from a collection of existing built models.
- Listing document model operations associated with the Form Recognizer resource.

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

The following section provides several code snippets covering some of the most common Form Recognizer tasks, including:

* [Extract Layout](#extract-layout "Extract Layout")
* [Use a General Document Model](#use-a-general-document-model)
* [Use Prebuilt Models](#use-prebuilt-models)
* [Build a Document Model](#build-a-document-model "Build a Document Model")
* [Analyze Documents using a Custom Model](#analyze-documents-using-a-custom-model "Analyze Documents using a Custom Model")
* [Manage Your Models](#manage-your-models "Manage Your Models")
* [Classify a document](#classify-a-document "Classify a Document")

### Extract Layout
Extract text, table structures, and selection marks like radio buttons and check boxes, along with their bounding box coordinates from documents without the need to build a model.
```java readme-sample-extractLayout
// analyze document layout using file input stream
File layoutDocument = new File("local/file_path/filename.png");
Path filePath = layoutDocument.toPath();
BinaryData layoutDocumentData = BinaryData.fromFile(filePath, (int) layoutDocument.length());

SyncPoller<OperationResult, AnalyzeResult> analyzeLayoutResultPoller =
    documentAnalysisClient.beginAnalyzeDocument("prebuilt-layout", layoutDocumentData);

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
            documentLine.getBoundingPolygon().toString()));

    // selection marks
    documentPage.getSelectionMarks().forEach(documentSelectionMark ->
        System.out.printf("Selection mark is '%s' and is within a bounding box %s with confidence %.2f.%n",
            documentSelectionMark.getSelectionMarkState().toString(),
            documentSelectionMark.getBoundingPolygon().toString(),
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

### Use a General Document Model
Analyze key-value pairs, tables, styles, and selection marks from documents using the general document model provided by
the Form Recognizer service.
Select the General Document Model by passing modelId="prebuilt-document" into the beginAnalyzeDocumentFromUrl method as follows:
```java readme-sample-analyzePrebuiltDocument
String documentUrl = "{document-url}";
String modelId = "prebuilt-document";
SyncPoller<OperationResult, AnalyzeResult> analyzeDocumentPoller =
    documentAnalysisClient.beginAnalyzeDocumentFromUrl(modelId, documentUrl);

AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult();

for (int i = 0; i < analyzeResult.getDocuments().size(); i++) {
    final AnalyzedDocument analyzedDocument = analyzeResult.getDocuments().get(i);
    System.out.printf("----------- Analyzing document %d -----------%n", i);
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
        System.out.printf("Line '%s' is within a bounding box %s.%n",
            documentLine.getContent(),
            documentLine.getBoundingPolygon().toString()));

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

// Key-value
analyzeResult.getKeyValuePairs().forEach(documentKeyValuePair -> {
    System.out.printf("Key content: %s%n", documentKeyValuePair.getKey().getContent());
    System.out.printf("Key content bounding region: %s%n",
        documentKeyValuePair.getKey().getBoundingRegions().toString());

    System.out.printf("Value content: %s%n", documentKeyValuePair.getValue().getContent());
    System.out.printf("Value content bounding region: %s%n", documentKeyValuePair.getValue().getBoundingRegions().toString());
});
```

### Use Prebuilt Models
Extract fields from select document types such as receipts, invoices, business cards, and identity documents using prebuilt models provided by the Form Recognizer service.
Supported prebuilt models are:
- Analyze receipts using the `prebuilt-receipt` model (fields recognized by the service can be found [here][service_analyze_receipt_fields])
- Analyze business cards using the `prebuilt-businessCard` model (fields recognized by the service can be found [here][service_analyze_business_cards_fields]).
- Analyze invoices using the `prebuilt-invoice` model (fields recognized by the service can be found [here][service_analyze_invoices_fields]).
- Analyze identity documents using the `prebuilt-idDocuments` model (fields recognized by the service can be found [here][service_analyze_identity_documents_fields]).
- Analyze US W2 tax forms using the `prebuilt-tax.us.w2` model. [Supported fields][service_analyze_w2_documents_fields].

For example, to analyze fields from a sales receipt, into the `beginAnalyzeDocumentFromUrl` method:
```java readme-sample-analyzeReceiptFromUrl
String receiptUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
    + "/azure-ai-formrecognizer/src/samples/resources/sample-documents/receipts/contoso-allinone.jpg";

SyncPoller<OperationResult, AnalyzeResult> analyzeReceiptPoller =
    documentAnalysisClient.beginAnalyzeDocumentFromUrl("prebuilt-receipt", receiptUrl);

AnalyzeResult receiptResults = analyzeReceiptPoller.getFinalResult();

for (int i = 0; i < receiptResults.getDocuments().size(); i++) {
    AnalyzedDocument analyzedReceipt = receiptResults.getDocuments().get(i);
    Map<String, DocumentField> receiptFields = analyzedReceipt.getFields();
    System.out.printf("----------- Analyzing receipt info %d -----------%n", i);
    DocumentField merchantNameField = receiptFields.get("MerchantName");
    if (merchantNameField != null) {
        if (DocumentFieldType.STRING == merchantNameField.getType()) {
            String merchantName = merchantNameField.getValueAsString();
            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                merchantName, merchantNameField.getConfidence());
        }
    }

    DocumentField merchantPhoneNumberField = receiptFields.get("MerchantPhoneNumber");
    if (merchantPhoneNumberField != null) {
        if (DocumentFieldType.PHONE_NUMBER == merchantPhoneNumberField.getType()) {
            String merchantAddress = merchantPhoneNumberField.getValueAsPhoneNumber();
            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                merchantAddress, merchantPhoneNumberField.getConfidence());
        }
    }

    DocumentField transactionDateField = receiptFields.get("TransactionDate");
    if (transactionDateField != null) {
        if (DocumentFieldType.DATE == transactionDateField.getType()) {
            LocalDate transactionDate = transactionDateField.getValueAsDate();
            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                transactionDate, transactionDateField.getConfidence());
        }
    }

    DocumentField receiptItemsField = receiptFields.get("Items");
    if (receiptItemsField != null) {
        System.out.printf("Receipt Items: %n");
        if (DocumentFieldType.LIST == receiptItemsField.getType()) {
            List<DocumentField> receiptItems = receiptItemsField.getValueAsList();
            receiptItems.stream()
                .filter(receiptItem -> DocumentFieldType.MAP == receiptItem.getType())
                .map(documentField -> documentField.getValueAsMap())
                .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                    if ("Name".equals(key)) {
                        if (DocumentFieldType.STRING == documentField.getType()) {
                            String name = documentField.getValueAsString();
                            System.out.printf("Name: %s, confidence: %.2fs%n",
                                name, documentField.getConfidence());
                        }
                    }
                    if ("Quantity".equals(key)) {
                        if (DocumentFieldType.DOUBLE == documentField.getType()) {
                            Double quantity = documentField.getValueAsDouble();
                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                quantity, documentField.getConfidence());
                        }
                    }
                }));
        }
    }
}
```

For more information and samples using prebuilt models, see:
- [Business Cards][analyze_business_cards_from_url]
- [Identity Documents][analyze_identity_documents_from_url]
- [Invoices][analyze_invoices_from_url]
- [Receipts sample][analyze_receipts_from_url]

### Build a document model
Build a machine-learned model on your own document type. The resulting model will be able to analyze values from the types of documents it was built on.
Provide a container SAS url to your Azure Storage Blob container where you're storing the training documents. See details on setting this up
in the [service quickstart documentation][quickstart_training].

**Note**

You can use the [Form Recognizer Studio preview][fr-studio] for creating a labeled file for your training forms.
More details on setting up a container and required file structure can be found in the [here][fr_build_training_set].

```java readme-sample-buildModel
// Build custom document analysis model
String blobContainerUrl = "{SAS_URL_of_your_container_in_blob_storage}";
// The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
String prefix = "{blob_name_prefix}}";
SyncPoller<OperationResult, DocumentModelDetails> buildOperationPoller =
    documentModelAdminClient.beginBuildDocumentModel(blobContainerUrl,
        DocumentModelBuildMode.TEMPLATE,
        prefix,
        new BuildDocumentModelOptions().setModelId("my-build-model").setDescription("model desc"),
        Context.NONE);

DocumentModelDetails documentModelDetails = buildOperationPoller.getFinalResult();

// Model Info
System.out.printf("Model ID: %s%n", documentModelDetails.getModelId());
System.out.printf("Model Description: %s%n", documentModelDetails.getDescription());
System.out.printf("Model created on: %s%n%n", documentModelDetails.getCreatedOn());
documentModelDetails.getDocumentTypes().forEach((key, documentTypeDetails) -> {
    System.out.printf("Document type: %s%n", key);
    documentTypeDetails.getFieldSchema().forEach((name, documentFieldSchema) -> {
        System.out.printf("Document field: %s%n", name);
        System.out.printf("Document field type: %s%n", documentFieldSchema.getType().toString());
        System.out.printf("Document field confidence: %.2f%n", documentTypeDetails.getFieldConfidence().get(name));
    });
});
```

### Analyze Documents using a Custom Model
Analyze the key/value pairs and table data from documents. These models are built with your own data,
so they're tailored to your documents. You should only analyze documents of the same doc type that the custom model
was built on.
```java readme-sample-analyzeCustomDocument
String documentUrl = "{document-url}";
String modelId = "{custom-built-model-ID}";
SyncPoller<OperationResult, AnalyzeResult> analyzeDocumentPoller =
    documentAnalysisClient.beginAnalyzeDocumentFromUrl(modelId, documentUrl);

AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult();

for (int i = 0; i < analyzeResult.getDocuments().size(); i++) {
    final AnalyzedDocument analyzedDocument = analyzeResult.getDocuments().get(i);
    System.out.printf("----------- Analyzing custom document %d -----------%n", i);
    System.out.printf("Analyzed document has doc type %s with confidence : %.2f%n",
        analyzedDocument.getDocType(), analyzedDocument.getConfidence());
    analyzedDocument.getFields().forEach((key, documentField) -> {
        System.out.printf("Document Field content: %s%n", documentField.getContent());
        System.out.printf("Document Field confidence: %.2f%n", documentField.getConfidence());
        System.out.printf("Document Field Type: %s%n", documentField.getType());
        System.out.printf("Document Field found within bounding region: %s%n",
            documentField.getBoundingRegions().toString());
    });
}

analyzeResult.getPages().forEach(documentPage -> {
    System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
        documentPage.getWidth(),
        documentPage.getHeight(),
        documentPage.getUnit());

    // lines
    documentPage.getLines().forEach(documentLine ->
        System.out.printf("Line '%s' is within a bounding box %s.%n",
            documentLine.getContent(),
            documentLine.getBoundingPolygon().toString()));

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
Manage the models in your Form Recognizer account.
```java readme-sample-manageModels
AtomicReference<String> modelId = new AtomicReference<>();

// First, we see how many models we have, and what our limit is
ResourceDetails resourceDetails = documentModelAdminClient.getResourceDetails();
System.out.printf("The resource has %s models, and we can have at most %s models",
    resourceDetails.getCustomDocumentModelCount(), resourceDetails.getCustomDocumentModelLimit());

// Next, we get a paged list of all of our models
PagedIterable<DocumentModelSummary> customDocumentModels = documentModelAdminClient.listDocumentModels();
System.out.println("We have following models in the account:");
customDocumentModels.forEach(documentModelSummary -> {
    System.out.printf("Model ID: %s%n", documentModelSummary.getModelId());
    modelId.set(documentModelSummary.getModelId());

    // get custom document analysis model info
    DocumentModelDetails documentModel = documentModelAdminClient.getDocumentModel(documentModelSummary.getModelId());
    System.out.printf("Model ID: %s%n", documentModel.getModelId());
    System.out.printf("Model Description: %s%n", documentModel.getDescription());
    System.out.printf("Model created on: %s%n", documentModel.getCreatedOn());
    documentModel.getDocumentTypes().forEach((key, documentTypeDetails) -> {
        documentTypeDetails.getFieldSchema().forEach((field, documentFieldSchema) -> {
            System.out.printf("Field: %s", field);
            System.out.printf("Field type: %s", documentFieldSchema.getType());
            System.out.printf("Field confidence: %.2f", documentTypeDetails.getFieldConfidence().get(field));
        });
    });
});

// Delete Model
documentModelAdminClient.deleteDocumentModel(modelId.get());
```

### Classify a document

The Form Recognizer service supports custom document classifiers that can classify documents into a set of predefined categories based on a training data set.
Documents can be classified with a custom classifier using the `beginClassifyDocument` or `beginClassifyDocumentFromUrl` 
method of `DocumentAnalysisClient`.
The following sample shows how to classify a document using a custom classifier:
```java readme-sample-classifyDocument
String documentUrl = "{file_source_url}";
String classifierId = "{custom_trained_classifier_id}";

documentAnalysisClient.beginClassifyDocumentFromUrl(classifierId, documentUrl, Context.NONE)
    .getFinalResult()
    .getDocuments()
    .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
```

For more detailed examples, refer to [samples][sample_examples].

## Troubleshooting
### General
Form Recognizer clients raise `HttpResponseException` [exceptions][http_response_exception]. For example, if you try
to provide an invalid file source URL an `HttpResponseException` would be raised with an error indicating the failure cause.
In the following code snippet, the error is handled
gracefully by catching the exception and display the additional information about the error.

```java readme-sample-handlingException
try {
    documentAnalysisClient.beginAnalyzeDocumentFromUrl("prebuilt-receipt", "invalidSourceUrl");
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
    // Do something with the exception
}
```

### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Add the above dependency to automatically configure
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki][http_clients_wiki].

## Next steps
The following section provides several code snippets illustrating common patterns used in the Form Recognizer API.
These code samples show common scenario operations with the Azure Form Recognizer client library.

* Analyze business card from a URL: [AnalyzeBusinessCardFromUrl][analyze_business_cards_from_url]
* Analyze identity documents from a URL: [AnalyzeIdentityDocumentsFromUrl][analyze_identity_documents_from_url]
* Analyze invoice from a URL: [AnalyzeInvoiceFromUrl][analyze_invoices_from_url]
* Analyze receipts: [AnalyzeReceipts][analyze_receipts]
* Analyze receipts from a URL: [AnalyzeReceiptsFromUrl][analyze_receipts_from_url]
* Extract layout: [AnalyzeLayout][analyze_layout]
* Analyze custom documents from a URL: [AnalyzeCustomDocumentFromUrl][analyze_custom_documents]
* Build a model: [BuildModel][build_model]
* Manage custom models: [ManageCustomModels][manage_custom_models]
* Copy a model between Form Recognizer resources: [CopyModel][copy_model]
* Create a composed model from a collection of custom-built models: [ComposeModel][compose_model]
* Get/List document model operations associated with the Form Recognizer resource: [GetOperation][get_operation]
* Build a document classifier : [BuildDocumentClassifier][build_document_classifier]

### Async APIs
All the examples shown so far have been using synchronous APIs, but we provide full support for async APIs as well.
You'll need to use `DocumentAnalysisAsyncClient`
```java readme-sample-asyncClient
DocumentAnalysisAsyncClient documentAnalysisAsyncClient = new DocumentAnalysisClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildAsyncClient();
```

* Analyze business card from a URL: [AnalyzeBusinessCardFromUrlAsync][analyze_business_cards_from_url_async]
* Analyze identity documents from a URL: [AnalyzeIdentityDocumentsFromUrlAsync][analyze_identity_documents_from_url_async]
* Analyze invoice: [AnalyzeInvoiceAsync][analyze_invoices_async]
* Analyze receipts: [AnalyzeReceiptsAsync][analyze_receipts_async]
* Analyze receipts from a URL: [AnalyzeReceiptsFromUrlAsync][analyze_receipts_from_url_async]
* Extract layout from a URL: [AnalyzeLayoutFromUrlAsync][analyze_layout_from_url_async]
* Analyze custom documents: [AnalyzeCustomDocumentAsync][analyze_custom_documents_async]
* Build a document model: [BuildModelAsync][build_model_async]
* Manage custom models: [ManageCustomModelsAsync][manage_custom_models_async]
* Copy a document model between Form Recognizer resources: [CopyModelAsync][copy_model_async]
* Create a composed document model from a collection of custom-built models: [ComposeModelAsync][compose_model_async]
* Get/List document model operations associated with the Form Recognizer resource: [GetOperationAsync][get_operation_async]
* Build a document classifier : [BuildDocumentClassifierAsync][build_document_classifier_async]

### Additional documentation
See the [Sample README][sample_readme] for several code snippets illustrating common patterns used in the Form Recognizer Java SDK.
For more extensive documentation on Azure Cognitive Services Form Recognizer, see the [Form Recognizer documentation][form_recognizer_doc].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information, see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[aad_authorization]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[azure_key_credential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureKeyCredential.java
[key]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource
[api_reference_doc]: https://azure.github.io/azure-sdk-for-java
[form_recognizer_doc]: https://aka.ms/azsdk-java-formrecognizer-ref-doc
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
[package]: https://central.sonatype.com/artifact/com.azure/azure-ai-formrecognizer
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview
[register_AAD_application]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal
[fr-studio]: https://aka.ms/azsdk/formrecognizer/formrecognizerstudio
[fr_build_training_set]: https://aka.ms/azsdk/formrecognizer/buildcustommodel
[sample_examples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples#examples
[sample_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples#readme
[migration_guide]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/formrecognizer/azure-ai-formrecognizer/migration-guide.md
[changelog]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/CHANGELOG.md

[sample_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/
[document_analysis_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/documentanalysis/DocumentAnalysisAsyncClient.java
[document_analysis_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/documentanalysis/DocumentAnalysisClient.java
[document_model_admin_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/documentanalysis/administration/DocumentModelAdministrationAsyncClient.java
[document_model_admin_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/documentanalysis/administration/DocumentModelAdministrationClient.java
[manage_custom_models]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/ManageCustomModels.java
[manage_custom_models_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/ManageCustomModelsAsync.java
[build_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildDocumentModel.java
[build_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildDocumentModelAsync.java
[build_document_classifier]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildDocumentClassifier.java
[build_document_classifier_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildDocumentClassifierAsync.java
[compose_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/ComposeDocumentModel.java
[compose_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/ComposeDocumentModelAsync.java
[copy_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CopyDocumentModel.java
[copy_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CopyDocumentModelAsync.java
[analyze_business_cards_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeBusinessCardFromUrl.java
[analyze_business_cards_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeBusinessCardFromUrlAsync.java
[analyze_identity_documents_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeIdentityDocumentsFromUrl.java
[analyze_identity_documents_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeIdentityDocumentsFromUrlAsync.java
[analyze_invoices_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeInvoicesAsync.java
[analyze_invoices_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeInvoicesFromUrl.java
[analyze_layout]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeLayout.java
[analyze_layout_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeLayoutFromUrlAsync.java
[analyze_receipts]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceipts.java
[analyze_receipts_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsAsync.java
[analyze_receipts_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsFromUrl.java
[analyze_receipts_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsFromUrlAsync.java
[analyze_custom_documents]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeCustomDocumentFromUrl.java
[analyze_custom_documents_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeCustomDocumentAsync.java
[get_operation]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/GetOperationSummary.java
[get_operation_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/GetOperationSummaryAsync.java

[fr_models]: https://aka.ms/azsdk/formrecognizer/models
[service_access]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[service_analyze_business_cards_fields]: https://aka.ms/azsdk/formrecognizer/businesscardfieldschema
[service_analyze_invoices_fields]: https://aka.ms/azsdk/formrecognizer/invoicefieldschema
[service_analyze_identity_documents_fields]: https://aka.ms/azsdk/formrecognizer/iddocumentfieldschema
[service_analyze_receipt_fields]: https://aka.ms/azsdk/formrecognizer/receiptfieldschema
[service_analyze_w2_documents_fields]: https://aka.ms/azsdk/formrecognizer/taxusw2fieldschema
[service-rename]: https://techcommunity.microsoft.com/t5/azure-ai-services-blog/azure-form-recognizer-is-now-azure-ai-document-intelligence-with/ba-p/3875765
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src
[quickstart_training]: https://learn.microsoft.com/azure/applied-ai-services/form-recognizer/quickstarts/get-started-sdks-rest-api?view=form-recog-3.0.0&pivots=programming-language-java
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
