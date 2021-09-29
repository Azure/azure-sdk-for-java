# Azure Form Recognizer client library for Java
Azure Cognitive Services Form Recognizer is a cloud service that uses machine learning to accurately extract text, 
key-value pairs, tables, and structured data from documents. It includes the following main functionalities:

* Custom document analysis models - Analyze content and structured data from documents using your own custom-built models. 
These models are built with your own data, so they're tailored to your custom documents.
With this library you can also manage the custom models you've created by building, listing, deleting, and copying models 
and see how close you are to the limit of custom models your account can hold.
* Analyze Layout - Analyze text, style and table structures, along with their bounding box coordinates, from documents.
* Prebuilt receipt model - Analyze data from sales receipts using a prebuilt model ("prebuilt-receipt").
* Prebuilt invoice model - Analyze data from USA sales invoices using a prebuilt model ("prebuilt-invoice").
* Prebuilt business card model - Analyze data from business cards using a prebuilt model ("prebuilt-businessCard").
* Prebuilt identity document model - Analyze data from identity documents using a prebuilt model ("prebuilt-idDocument").
* Prebuilt document model - Analyze data from documents using a prebuilt model ("prebuilt-document").

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Form Recognizer account][form_recognizer_account] to use this package.

### Include the Package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on GA version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
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
and then include the direct dependency in the dependencies section without the version tag.

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
    <version>4.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})
> Note: This version of the client library defaults to the `"V2021-09-30-preview"` version of the service.

This table shows the relationship between SDK versions and supported API versions of the service:

|SDK version|Supported API version of service
|-|-
|3.0.x | 2.0
|3.1.X - 3.1.3| 2.0, 2.1 (default)
|4.0.0-beta.1 - Latest GA release| V2021-09-30-preview (default)

#### Create a Form Recognizer resource
Form Recognizer supports both [multi-service and single-service access][service_access]. Create a Cognitive Service's
resource if you plan to access multiple cognitive services under a single endpoint/key. For Form Recognizer access only,
create a Form Recognizer resource.

You can create either resource using the

**Option 1:** [Azure Portal][create_new_resource]

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

##### Looking up the endpoint
You can find the **endpoint** for your Form Recognizer resource in the [Azure Portal][azure_portal],
or [Azure CLI][azure_cli_endpoint].
```bash
# Get the endpoint for the resource
az cognitiveservices account show --name "resource-name" --resource-group "resource-group-name" --query "endpoint"
```

#### Create a Document Analysis client using AzureKeyCredential
To use `AzureKeyCredential` authentication, provide the [key][key] as a string to the [AzureKeyCredential][azure_key_credential].
This key can be found in the [Azure Portal][azure_portal] in your created Form Recognizer
resource, or by running the following Azure CLI command to get the key from the Form Recognizer resource:

```bash
az cognitiveservices account keys list --resource-group <your-resource-group-name> --name <your-resource-name>
```
Use the API key as the credential parameter to authenticate the client:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L52-L55 -->
```java
DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L62-L65 -->
```java
DocumentModelAdministrationClient documentModelAdminClient = new DocumentModelAdministrationClientBuilder()
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
    <version>1.3.6</version>
</dependency>
```
[//]: # ({x-version-update-end})
* [Register a new Azure Active Directory application][register_AAD_application]
* [Grant access][grant_access] to Form Recognizer by assigning the `"Cognitive Services User"` role to your service principal.

After the setup, you can choose which type of [credential][azure_identity_credential_type] from azure.identity to use.
As an example, [DefaultAzureCredential][wiki_identity] can be used to authenticate the client:
Set the values of the client ID, tenant ID, and client secret of the AAD application as environment variables:
AZURE_CLIENT_ID, AZURE_TENANT_ID, AZURE_CLIENT_SECRET.

Authorization is easiest using [DefaultAzureCredential][wiki_identity]. It finds the best credential to use in its
running environment. For more information about using Azure Active Directory authorization with Form Recognizer, please
refer to [the associated documentation][aad_authorization].

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L72-L76 -->
```java
TokenCredential credential = new DefaultAzureCredentialBuilder().build();
DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
    .endpoint("{endpoint}")
    .credential(credential)
    .buildClient();
```

## Key concepts
### DocumentAnalysisClient
The [DocumentAnalysisClient][document_analysis_sync_client] and [DocumentAnalysisAsyncClient][document_analysis_async_client]
provide both synchronous and asynchronous operations
 - Recognizing document fields and layout using custom models built to analyze your custom documents.
 These values are returned in a `AnalyzeResult` object. See example [Analyze Custom Documents](#analyze-documents-using-a-custom-model).
 - Recognizing document layout, including tables, lines and words, without the need to build a model.
 Document layout is returned in a `AnalyzeResult` object. See example [Analyze Layout](#analyze-layout).
- Recognizing common fields from the following document types using prebuilt models. These fields and meta-data are returned
  in a collection of fields on a `AnalyzeResult` object.
  Supported prebuilt models:
    - Receipts
    - Business cards
    - Invoices
    - Identity Documents
    - Documents
    
  See example [Prebuilt Models](#use-prebuilt-models).

### DocumentModelAdministrationClient
The [DocumentModelAdministrationClient][document_model_admin_sync_client] and
[DocumentModelAdministrationAsyncClient][document_model_admin_sync_client] provide both synchronous and asynchronous operations
- Build custom document analysis models to analyze text content, fields and values found in your custom documents. See example [Build a model](#build-a-model).
 A `DocumentModel` is returned indicating the document types that the model can analyze, and the fields and schemas it will extract in
each doc type.
- Managing models created in your account by building, listing, deleting, and see how close you are to the limit of 
custom models your account can hold. See example [Manage models](#manage-your-models).
- Copying a custom model from one Form Recognizer resource to another.
- Creating a composed model from a collection of existing built models.
- Listing all document model operations associated with the Form Recognizer resource.

### Long-Running Operations
Long-running operations are operations which consist of an initial request sent to the service to start an operation,
followed by polling the service at intervals to determine whether the operation has completed or failed, and if it has
succeeded, to get the result.

Methods that build models or analyze values from documents are modeled as long-running operations. The client exposes
a `begin<MethodName>` method that returns a `SyncPoller` or `PollerFlux` instance.
Callers should wait for the operation to completed by calling `getFinalResult()` on the returned operation from the
`begin<MethodName>` method. Sample code snippets are provided to illustrate using long-running operations
[below](#examples).

## Examples

The following section provides several code snippets covering some of the most common Form Recognizer tasks, including:

* [Analyze Documents using a Custom Model](#analyze-documents-using-a-custom-model "Analyze Documents using a Custom Model")
* [Analyze Layout](#analyze-layout "Analyze Layout")
* [Use Prebuilt Models](#use-prebuilt-models)
* [Build a Model](#build-a-model "Build a model")
* [Manage Your Models](#manage-your-models "Manage Your Models")

### Analyze Documents using a Custom Model
Analyze the key/value pairs and table data from documents. These models are built with your own data,
so they're tailored to your documents. You should only analyze documents of the same doc type that the custom model 
was built on.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L83-L102 -->
```java
String documentUrl = "{document-url}";
String modelId = "{custom-built-model-ID}";
SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeDocumentPoller =
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
        System.out.printf("Document Field Type: %.2f%n", documentField.getType().toString());
        System.out.printf("Document Field found within bounding region: %s%n",
            documentField.getBoundingRegions().toString());
    });
}
```

### Analyze Layout
Analyze text, table structures and selection marks like radio buttons and check boxes, along with their bounding box
coordinates, from documents, without the need to build a model.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L111-L153 -->
```java
// analyze document layout using file input stream
File layoutDocument = new File("local/file_path/filename.png");
byte[] fileContent = Files.readAllBytes(layoutDocument.toPath());
InputStream fileStream = new ByteArrayInputStream(fileContent);

SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeLayoutResultPoller =
    documentAnalysisClient.beginAnalyzeDocument("prebuilt-layout", fileStream, layoutDocument.length());

AnalyzeResult analyzeLayoutResult = analyzeLayoutResultPoller.getFinalResult();

// pages
analyzeLayoutResult.getPages().forEach(documentPage -> {
    System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
        documentPage.getWidth(),
        documentPage.getHeight(),
        documentPage.getUnit());

    // lines
    documentPage.getLines().forEach(documentLine ->
        System.out.printf("Line %s is within a bounding box %s.%n",
            documentLine.getContent(),
            documentLine.getBoundingBox().toString()));

    // selection marks
    documentPage.getSelectionMarks().forEach(documentSelectionMark ->
        System.out.printf("Selection mark is %s and is within a bounding box %s with confidence %.2f.%n",
            documentSelectionMark.getState().toString(),
            documentSelectionMark.getBoundingBox().toString(),
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
Extract fields from certain types of common documents using prebuilt models provided by the Form Recognizer service. Supported prebuilt models are:
- Business cards. See fields found on a business card [here][service_analyze_business_cards_fields].
- Invoices. See fields found on an invoice [here][service_analyze_invoices_fields].
- Identity documents. See fields found on an identity document [here][service_analyze_identity_documents_fields].
- Sales receipts. See fields found on a receipt [here][service_analyze_receipt_fields].
- Document. See fields found on a document [here][service_analyze_document_fields]
- 
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L160-L225-->
```java
String receiptUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
    + "/azure-ai-formrecognizer/src/samples/resources/sample-documents/receipts/contoso-allinone.jpg";

SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeReceiptPoller =
    documentAnalysisClient.beginAnalyzeDocumentFromUrl("prebuilt-receipt", receiptUrl);

AnalyzeResult receiptResults = analyzeReceiptPoller.getFinalResult();

for (int i = 0; i < receiptResults.getDocuments().size(); i++) {
    AnalyzedDocument analyzedReceipt = receiptResults.getDocuments().get(i);
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

    DocumentField transactionDateField = receiptFields.get("TransactionDate");
    if (transactionDateField != null) {
        if (DocumentFieldType.DATE == transactionDateField.getType()) {
            LocalDate transactionDate = transactionDateField.getValueDate();
            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                transactionDate, transactionDateField.getConfidence());
        }
    }

    DocumentField receiptItemsField = receiptFields.get("Items");
    if (receiptItemsField != null) {
        System.out.printf("Receipt Items: %n");
        if (DocumentFieldType.LIST == receiptItemsField.getType()) {
            List<DocumentField> receiptItems = receiptItemsField.getValueList();
            receiptItems.stream()
                .filter(receiptItem -> DocumentFieldType.MAP == receiptItem.getType())
                .map(formField -> formField.getValueMap())
                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                    if ("Name".equals(key)) {
                        if (DocumentFieldType.STRING == formField.getType()) {
                            String name = formField.getValueString();
                            System.out.printf("Name: %s, confidence: %.2fs%n",
                                name, formField.getConfidence());
                        }
                    }
                    if ("Quantity".equals(key)) {
                        if (DocumentFieldType.FLOAT == formField.getType()) {
                            Float quantity = formField.getValueFloat();
                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                quantity, formField.getConfidence());
                        }
                    }
                }));
        }
    }
}
```

For more information and samples using prebuilt models see:
- [Business Cards][analyze_business_cards_from_url]
- [Identity Documents][analyze_identity_documents_from_url]
- [Invoices][analyze_invoices_from_url]
- [Receipts sample][analyze_receipts_from_url]

### Build a model
Build a machine-learned model on your own document type. The resulting model will be able to analyze values from the types of documents it was built on.
Provide a container SAS url to your Azure Storage Blob container where you're storing the training documents. See details on setting this up
in the [service quickstart documentation][quickstart_training].
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L293-L315 -->
```java
// Build custom document analysis model
String trainingFilesUrl = "{SAS_URL_of_your_container_in_blob_storage}";
// The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
SyncPoller<DocumentOperationResult, DocumentModel> buildOperationPoller =
    documentModelAdminClient.beginBuildModel(trainingFilesUrl,
        "my-build-model",
        new BuildModelOptions().setDescription("model desc"),
        Context.NONE);

DocumentModel documentModel = buildOperationPoller.getFinalResult();

// Model Info
System.out.printf("Model ID: %s%n", documentModel.getModelId());
System.out.printf("Model Description: %s%n", documentModel.getDescription());
System.out.printf("Model created on: %s%n%n", documentModel.getCreatedOn());
documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
    System.out.printf("Document type: %s%n", key);
    docTypeInfo.getFieldSchema().forEach((name, documentFieldSchema) -> {
        System.out.printf("Document field: %s%n", name);
        System.out.printf("Document field type: %s%n", documentFieldSchema.getType().toString());
        System.out.printf("Document field confidence: %.2f%n", docTypeInfo.getFieldConfidence().get(name));
    });
});
```

### Manage your models
Manage the models in your Form Recognizer account.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L322-L351 -->
```java
AtomicReference<String> modelId = new AtomicReference<>();

// First, we see how many models we have, and what our limit is
AccountProperties accountProperties = documentModelAdminClient.getAccountProperties();
System.out.printf("The account has %s models, and we can have at most %s models",
    accountProperties.getDocumentModelCount(), accountProperties.getDocumentModelLimit());

// Next, we get a paged list of all of our models
PagedIterable<DocumentModelInfo> customDocumentModels = documentModelAdminClient.listModels();
System.out.println("We have following models in the account:");
customDocumentModels.forEach(documentModelInfo -> {
    System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
    modelId.set(documentModelInfo.getModelId());

    // get custom document analysis model info
    DocumentModel documentModel = documentModelAdminClient.getModel(documentModelInfo.getModelId());
    System.out.printf("Model ID: %s%n", documentModel.getModelId());
    System.out.printf("Model Description: %s%n", documentModel.getDescription());
    System.out.printf("Model created on: %s%n", documentModel.getCreatedOn());
    documentModel.getDocTypes().forEach((key, docTypeInfo) -> {
        docTypeInfo.getFieldSchema().forEach((field, documentFieldSchema) -> {
            System.out.printf("Field: %s", field);
            System.out.printf("Field type: %s", documentFieldSchema.getType());
            System.out.printf("Field confidence: %.2f", docTypeInfo.getFieldConfidence().get(field));
        });
    });
});

// Delete Model
documentModelAdminClient.deleteModel(modelId.get());
```
For more detailed examples, refer to [samples][sample_readme].

## Troubleshooting
### General
Form Recognizer clients raises `HttpResponseException` [exceptions][http_response_exception]. For example, if you try
to provide an invalid file source URL an `HttpResponseException` would be raised with an error indicating the failure cause.
In the following code snippet, the error is handled
gracefully by catching the exception and display the additional information about the error.

<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L358-L362 -->
```java
try {
    documentAnalysisClient.beginAnalyzeDocumentFromUrl("prebuilt-receipt", "invalidSourceUrl");
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
}
```

### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure
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
* Analyze content: [AnalyzeContent][analyze_layout]
* Analyze custom documents from a URL: [AnalyzeCustomDocumentsFromUrl][analyze_custom_documents]
* Build a model: [BuildModel][build_model]
* Manage custom models: [ManageCustomModels][manage_custom_models]
* Copy a model between Form Recognizer resources: [CopyModel][copy_model]
* Create a composed model from a collection of custom-built models: [CreateComposedModel][create_composed_model]
* Get all document model operations associated with the Form Recognizer resource: [GetOperation][get_operation]

#### Async APIs
All the examples shown so far have been using synchronous APIs, but we provide full support for async APIs as well.
You'll need to use `DocumentAnalysisAsyncClient`
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L369-L372 -->
```java
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
* Analyze content from a URL: [AnalyzeContentFromUrlAsync][analyze_layout_from_url_async]
* Analyze custom documents: [AnalyzeCustomDocumentsAsync][analyze_custom_documents_async]
* Build a model: [BuildModelAsync][build_model_async]
* Manage custom models: [ManageCustomModelsAsync][manage_custom_models_async]
* Copy a model between Form Recognizer resources: [CopyModelAsync][copy_model_async]
* Create a composed model from a collection of custom-built models: [CreateComposedModelAsync][create_composed_model_async]

### Additional documentation

For more extensive documentation on Azure Cognitive Services Form Recognizer, see the [Form Recognizer documentation][github_io_docs].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[aad_authorization]: https://docs.microsoft.com/azure/cognitive-services/authentication#authenticate-with-azure-active-directory
[azure_key_credential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/credential/AzureKeyCredential.java
[key]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows#get-the-keys-for-your-resource
[api_reference_doc]: https://aka.ms/azsdk-java-formrecognizer-ref-docs
[github_io_docs]: https://aka.ms/azsdk-java-formrecognizer-ref-docs
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
[package]: https://mvnrepository.com/artifact/com.azure/azure-ai-formrecognizer
[product_documentation]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/overview

[comment]: <> ([create_composed_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CreateComposedModel.java)

[comment]: <> ([create_composed_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/CreateComposedModelAsync.java)

[comment]: <> ([sample_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/)

[comment]: <> ([document_analysis_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/DocumentAnalysisAsyncClient.java)

[comment]: <> ([document_analysis_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/DocumentAnalysisClient.java)

[comment]: <> ([document_model_admin_async_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/administration/DocumentModelAdministrationAsyncClient.java)

[comment]: <> ([document_model_admin_sync_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/administration/DocumentModelAdministrationClient.java)

[comment]: <> ([manage_custom_models]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/ManageCustomModels.java)

[comment]: <> ([manage_custom_models_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/ManageCustomModelsAsync.java)

[comment]: <> ([analyze_business_cards_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeBusinessCardFromUrl.java)

[comment]: <> ([analyze_business_cards_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeBusinessCardFromUrlAsync.java)

[comment]: <> ([analyze_identity_documents_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeIdentityDocumentsFromUrl.java)

[comment]: <> ([analyze_identity_documents_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeIdentityDocumentsFromUrlAsync.java)

[comment]: <> ([analyze_invoices_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeInvoicesAsync.java)

[comment]: <> ([analyze_invoices_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeInvoicesFromUrl.java)

[comment]: <> ([analyze_layout]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeLayout.java)

[comment]: <> ([analyze_layout_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeLayoutFromUrlAsync.java)

[comment]: <> ([analyze_receipts]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceipts.java)

[comment]: <> ([analyze_receipts_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsAsync.java)

[comment]: <> ([analyze_receipts_from_url]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsFromUrl.java)

[comment]: <> ([analyze_receipts_from_url_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeReceiptsFromUrlAsync.java)

[comment]: <> ([analyze_custom_documents]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeCustomDocumentsFromUrl.java)

[comment]: <> ([analyze_custom_documents_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/AnalyzeCustomDocumentsAsync.java)

[comment]: <> ([register_AAD_application]: https://docs.microsoft.com/azure/cognitive-services/authentication#assign-a-role-to-a-service-principal)

[comment]: <> ([build_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildModel.java)

[comment]: <> ([build_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/BuildModelAsync.java)

[comment]: <> ([copy_model]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CopyModel.java)

[comment]: <> ([copy_model_async]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/com/azure/ai/formrecognizer/administration/CopyModelAsync.java)

[comment]: <> ([service_analyze_document_fields]: TODO)

[comment]: <> ([get_operation]: TODO)

[service_access]: https://docs.microsoft.com/azure/cognitive-services/cognitive-services-apis-create-account?tabs=multiservice%2Cwindows
[service_analyze_business_cards_fields]: https://aka.ms/formrecognizer/businesscardfields
[service_analyze_invoices_fields]: https://aka.ms/formrecognizer/invoicefields
[service_analyze_identity_documents_fields]: https://aka.ms/formrecognizer/iddocumentfields
[service_analyze_receipt_fields]: https://aka.ms/formrecognizer/receiptfields
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src
[quickstart_training]: https://docs.microsoft.com/azure/cognitive-services/form-recognizer/quickstarts/curl-train-extract#train-a-form-recognizer-model
[wiki_identity]: https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2FREADME.png)
