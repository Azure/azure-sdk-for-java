# Guide for migrating to `azure-ai-formrecognizer (4.0.0-beta.1)` from `azure-ai-formrecognizer (3.0.x)`

This guide is intended to assist in the migration to `azure-ai-formrecognizer (4.0.0-beta.1)` from `azure-ai-formrecognizer (3.0.x)`. It will focus on side-by-side comparisons for similar operations between the two packages.

We assume that you are familiar with the old SDK `azure-ai-formrecognizer (3.0.x)`. If not, please refer to the new SDK README for [azure-ai-formrecognizer][README] directly rather than this migration guide.

## Table of contents

- [Guide for migrating to `azure-ai-formrecognizer (4.0.0-beta.1)` from `azure-ai-formrecognizer (3.0.x)`](#guide-for-migrating-to-azure-ai-formrecognizer-400-beta1-from-azure-ai-formrecognizer-30x)
    - [Table of contents](#table-of-contents)
    - [Migration benefits](#migration-benefits)
        - [Cross Service SDK improvements](#cross-service-sdk-improvements)
    - [Important changes](#important-changes)
        - [Instantiating clients](#instantiating-clients)
        - [Analyze Documents using a Prebuilt Model](#analyze-documents-using-a-prebuilt-model)
        - [Build a custom document analysis model](#build-a-custom-document-analysis-model)
        - [Analyze Documents using a Custom Model](#analyze-documents-using-a-custom-model)
        - [Manage models](#manage-models)
    - [Additional samples](#additional-samples)

## Migration benefits

A natural question to ask when considering whether or not to adopt a new version or library is its benefits. As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learning the patterns and practices to best support developer productivity and to understand the gaps that the Java
client libraries have.

There were several areas of consistent feedback expressed across the Azure client library ecosystem. The most important is that many developers have felt that the learning curve was difficult, and the APIs did not offer a good, approachable, and consistent onboarding story for those learning Azure or exploring a specific Azure service.

To improve the development experience across Azure services, a set of uniform [design guidelines][Guidelines] was created for all languages to drive a
consistent experience with established API patterns for all services. A set of [Java design guidelines][GuidelinesJava] was introduced to ensure that Java clients have a natural and idiomatic feel with respect to the Java ecosystem. Further details are available in the guidelines
for those interested.

Aside from the benefits of the new design mentioned above, the `azure-ai-formrecognizer (4.0.0-beta.1)` replaces the multiple API design and adds a unified method to analyze documents with the `beginAnalyzeDocument` method and managing of operations in your form recognizer account.This version of the client library defaults to the 2021-09-30-preview version of the service. Refer to the [README][README] for more information about analyzing documents.

### Cross Service SDK improvements

The modern Form Recognizer client library also provides the ability to share in some of the cross-service improvements made to the Azure development experience, such as

- A unified method for analyzing analyzing text and structured data from your documents across each of the client libraries.
- A unified return type `DocumentModel` indicating the document type the model can analyze and the specific fields it can analyze along with the estimated confidence for each field.
- Specifying a modelId instead of the generated GUID when creating custom models, along with an optional description.
- Modified `Generate Copy Authorization operation` response to return the target resource information so that it could be used directly when copying custom models method.
- List Models operation now returns a paged list of prebuilt in addition to custom models and does not return in-progress and failed models.
- Added methods for getting/listing operations useful to track the status of model creation/copying operations and any resulting errors.

## Important changes

#### Instantiating clients

In 3.x, the `FormRecognizerClient`, instantiated via the `FormRecognizerClientBuilder`. The client contains both sync and async methods.
In 4.x, the `FormRecognizerClient` has been replaced by the `DocumentAnalysisClient`and the instantiation of the client is done through the [DocumentAnalysisClientBuilder][DocumentAnalysisClientBuilder]. The sync and async operations are separated to [DocumentAnalysisClient][DocumentAnalysisClient] and [DocumentAnalysisAsyncClient][DocumentAnalysisAsyncClient].
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L52-L55 -->
```java
DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
Similarly, with 4.X, the `FormTrainingClient` has been replaced by the `DocumentModelAdministrationClient`and the instantiation of the client is done through the [DocumentModelAdministrationClientBuilder][DocumentModelAdministrationClientBuilder][DocumentModelAdministrationClientBuilder]. The sync and async operations are separated to [DocumentModelAdministrationClient][DocumentModelAdministrationClient] and [DocumentModelAdministrationAsyncClient][DocumentModelAdministrationAsyncClient].
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L62-L65 -->
```java
DocumentModelAdministrationClient documentModelAdminClient = new DocumentModelAdministrationClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

#### Analyze Documents using a Prebuilt Model
Analyze data from certain types of common documents (such as receipts, invoices, business cards, or identity documents) using prebuilt models.
In 3.x, `beginRecognizeReceipts` or `beginRecognizeReceiptsFromUrl` method was used to analyze receipts.
In 4.X, `beginRecognizeReceipts` and `beginRecognizeReceiptsFromUrl` has been replaced with `beginAnalyzeDocument` and `beginAnalyzeDocumentFromUrl` respectively. Below is an example to analyze receipt data using 4.X `beginAnalyzeDocumentFromUrl`:
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

#### Build a custom document analysis model
In 3.X, creating a custom model required specifying `useTrainingLabels` to indicate if the model creation was using the custom labeling or not with the `beginTraining` method.
In 4.X, we introduced the new general document model (prebuilt-document) consisting of of key-value pair extraction, it is no longer necessary to train a custom model to extract key-value pairs. Thus, we removed the option to train custom models without labels. Hence, with 4.X, we can build a custom document analysis model using the `beginBuildModel` method as shown in the example below:
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

### Manage models
In 3.X, listing models returned only the custom trained models using the `listCustomModel` method.
With 4.X, list Models operation `listModels` now returns a paged list of prebuilt in addition to custom models and does not return in-progress and failed models.
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

#### Analyze Documents using a Custom model
Analyze data from custom documents using your own custom models, built with your own data, so they're tailored to your documents.
In 3.x, `beginRecognizeCustomForms` or `beginRecognizeCustomFormsFromUrl` method was used to analyze receipts.
In 4.X, `beginRecognizeCustomForms` and `beginRecognizeCustomFormsFromUrl` has been replaced with `beginAnalyzeDocument` and `beginAnalyzeDocumentFromUrl` respectively. Below is an example to analyze custom document data using 4.X `beginAnalyzeDocumentFromUrl`:
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

## Additional samples

More examples can be found at:

- [Form Recognizer samples][README-Samples]

<!-- Links -->
<!-- [DocumentAnalysisClientBuilder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/DocumentAnalysisClientBuilder.java
[DocumentAnalysisClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/DocumentAnalysisClient.java
[DocumentAnalysisAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/DocumentAnalysisAsyncClient.java
[DocumentModelAdministrationClientBuilder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/administration/DocumentModelAdministrationClientBuilder.java
[DocumentModelAdministrationClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/administration/DocumentModelAdministrationClient.java
[DocumentModelAdministrationAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/administration/DocumentModelAdministrationAsyncClient.java -->
[Guidelines]: https://azure.github.io/azure-sdk/general_introduction.html
[GuidelinesJava]: https://azure.github.io/azure-sdk/java_introduction.html
[GuidelinesJavaDesign]: https://azure.github.io/azure-sdk/java_introduction.html#namespaces
[README-Samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/README.md
[README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/README.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2Fmigration-guide.png)
