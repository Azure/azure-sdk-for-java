# Guide for migrating to `azure-ai-formrecognizer (4.0.0-beta.1 - above)` from `azure-ai-formrecognizer (3.1.x - below)`

This guide is intended to assist in the migration to `azure-ai-formrecognizer (4.0.0-beta.1 - above)` from `azure-ai-formrecognizer (3.1.x - below)`. It will focus on side-by-side comparisons for similar operations between the two package versions.

We assume that you are familiar with the previous SDK `azure-ai-formrecognizer (3.1.x - below)`. If not, please refer to the new SDK README for [azure-ai-formrecognizer][README] directly rather than this migration guide.

## Table of contents
- [Migration benefits](#migration-benefits)
- [Important changes](#important-changes)
    - [Instantiating clients](#instantiating-clients)
    - [Analyze documents](#analyze-documents)
      - [Using a prebuilt Model](#using-a-prebuilt-model)
      - [Using a layout model](#using-a-layout-model)
      - [Using custom model](#using-a-custom-model)
    - [Manage models](#manage-models)
- [Additional samples](#additional-samples)

## Migration benefits

A natural question to ask when considering whether to adopt a new version of the library is what the benefits of
doing so would be. As Azure Form Recognizer has matured and been embraced by a more diverse group of developers, 
we have been focused on learning the patterns and practices to best support developer productivity and add value to our 
customers.

To improve the development experience and addressing the consistent feedback across the Form Recognizer SDK, this new 
version of the library replaces the previously existing clients `FomRecognizerClient` and `FormTrainingClient` with
`DocumentAnalysisClient` and the `DocumentModelAdministrationClient` that provide unified methods for 
analyzing documents and providing support for the new features added by the service in 
API version `2021-09-30-preview` and later.

The below table describes the relationship of each client and its supported API version(s):

|API version|Supported clients
|-|-
|2021-09-30-preview | DocumentAnalysisClient and DocumentModelAdministrationClient
|2.1 | FormRecognizerClient and FormTrainingClient
|2.0 | FormRecognizerClient and FormTrainingClient

The newer Form Recognizer client library also provides the ability to share in some improvements made to the Azure development experience, such as:

- A unified method, `beginAnalyzeDocument` and `beginAnalyzeDocumentFromUrl`, for analyzing text and structured data from documents.
This method uses a `modelId` parameter for specifying the type of analysis to perform. 
The newly introduced method return type `AnalyzeResult` removes hierarchical dependencies between the previously known `FormElements`
and move them to a more top level and easily accessible position such as `AnalyzeResult.tables` instead of `RecognizedForm.pages.tables`.
The service has further matured to define cross-page elements by using the `BoundingRegion` model and by specifying the content and span information on document fields.
- A unified return type `DocumentModel` indicating the document types the model can analyze and the specific fields it can analyze along with the estimated confidence for each field.
- Specifying a modelId instead of the generated GUID when creating custom models, along with an optional description. See [here][service_supported_models], for the supported model types.
- Modified `Generate Copy Authorization operation` response to return the target resource information so that it could be used directly when copying custom models method instead of needed to be provided by the user.
- List Models operation now returns a paged list of prebuilt in addition to custom models that are built successfully. 
Also, when using the `getModel()` model, users can get the field schema (field names and types that the model can extract) for the model they specified, including for prebuilt models.
- Added methods for getting/listing operations of the past 24 hours, useful to track the status of model creation/copying operations and any resulting errors.

Please refer to the [README][README] for more information on these new clients.

## Important changes

#### Instantiating clients

In 3.x.x, the `FormRecognizerClient` and the `FormRecognizerAsyncClient` is instantiated via the `FormRecognizerClientBuilder`.

In 4.x.x, the `FormRecognizerClient` and the `FormRecognizerAsyncClient`, has been replaced by the `DocumentAnalysisClient` and the `DocumentAnalysisAsyncClient` respectively and is instantiated via the [DocumentAnalysisClientBuilder][DocumentAnalysisClientBuilder].
The sync and async operations are separated to [DocumentAnalysisClient][DocumentAnalysisClient] and [DocumentAnalysisAsyncClient][DocumentAnalysisAsyncClient].

Instantiating FormRecognizerClient client with 3.x.x:
```java
FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

Instantiating DocumentAnalysisClient client with 4.x.x:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L52-L55 -->
```java
DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```
Similarly, with 4.x.x, the `FormTrainingClient` and `FormTrainingAsyncClient` has been replaced by the `DocumentModelAdministrationClient` 
and `DocumentModelAdministrationAsyncClient`, instantiated via the [DocumentModelAdministrationClientBuilder][DocumentModelAdministrationClientBuilder].
The sync and async operations are separated to [DocumentModelAdministrationClient][DocumentModelAdministrationClient] and [DocumentModelAdministrationAsyncClient][DocumentModelAdministrationAsyncClient].

Instantiating FormRecognizerClient client with 3.x.x:
```java
FormTrainingClient formTrainingClient = new FormTrainingClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

Instantiating DocumentModelAdministrationClient client with 4.x.x:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L62-L65 -->
```java
DocumentModelAdministrationClient documentModelAdminClient = new DocumentModelAdministrationClientBuilder()
    .credential(new AzureKeyCredential("{key}"))
    .endpoint("{endpoint}")
    .buildClient();
```

#### Analyze documents
Analyze data from certain types of common documents (such as receipts, invoices, business cards, or identity documents) 
using prebuilt models or from custom documents using your own custom models and extract table and structured layout data.

With 4.x.x, the unified method, `beginAnalyzeDocument` and `beginAnalyzeDocumentFromUrl`:
- accepts a string type `modelId` to be any of the prebuilt model IDs or a custom model ID.
- the return type `AnalyzeResult` model now exposes document elements, such as key-value pairs, entities, tables,
  document fields and values at the top level of the returned model. As compared to the previously returned model
  `RecognizedForm` which included hierarchical relationships between `FormElements` for instance tables were an element
  of a `FormPage` and not a top-level element.
- provides the functionality of `beginRecognizeCustomForms`, `beginRecognizeContent`, `beginRecognizeReceipt`,
  `beginRecognizeReceipts`, `beginRecognizeInvoices` `beginRecognizeIdentityDocuments` and `beginRecognizeBusinessCards` from the previous (azure-ai-formrecognizer 3.1.X - below) package versions.
- accepts unified `AnalyzeDocumentOptions` to specify pages and locale information for the outgoing request
- the `includeFieldElements` parameter is not supported with the `DocumentAnalysisClient`, text details are automatically included with API version `2021-09-30-preview` and later.
- the `readingOrder` parameter does not exist as the service uses `natural` reading order for the returned data.

#### Using a prebuilt model
- In 3.x.x, `beginRecognizeReceipts` and `beginRecognizeReceiptsFromUrl` method was used to analyze receipts.
- In 4.x.x, `beginRecognizeReceipts` and `beginRecognizeReceiptsFromUrl` has been replaced with `beginAnalyzeDocument` and `beginAnalyzeDocumentFromUrl` respectively.

Analyze receipt using 3.x.x `beginRecognizeReceipts`:
```java
String receiptUrl = "https://docs.microsoft.com/azure/cognitive-services/form-recognizer/media"
    + "/contoso-allinone.jpg";
SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> syncPoller =
    formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl);
List<RecognizedForm> receiptPageResults = syncPoller.getFinalResult();

for (int i = 0; i < receiptPageResults.size(); i++) {
    RecognizedForm recognizedForm = receiptPageResults.get(i);
    Map<String, FormField> recognizedFields = recognizedForm.getFields();
    System.out.printf("----------- Recognizing receipt info for page %d -----------%n", i);
    FormField merchantNameField = recognizedFields.get("MerchantName");
    if (merchantNameField != null) {
        if (FieldValueType.STRING == merchantNameField.getValue().getValueType()) {
            String merchantName = merchantNameField.getValue().asString();
            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                merchantName, merchantNameField.getConfidence());
        }
    }

    FormField merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
    if (merchantPhoneNumberField != null) {
        if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue().getValueType()) {
            String merchantAddress = merchantPhoneNumberField.getValue().asPhoneNumber();
            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                merchantAddress, merchantPhoneNumberField.getConfidence());
        }
    }

    FormField transactionDateField = recognizedFields.get("TransactionDate");
    if (transactionDateField != null) {
        if (FieldValueType.DATE == transactionDateField.getValue().getValueType()) {
            LocalDate transactionDate = transactionDateField.getValue().asDate();
            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                transactionDate, transactionDateField.getConfidence());
        }
    }

    FormField receiptItemsField = recognizedFields.get("Items");
    if (receiptItemsField != null) {
        System.out.printf("Receipt Items: %n");
        if (FieldValueType.LIST == receiptItemsField.getValue().getValueType()) {
            List<FormField> receiptItems = receiptItemsField.getValue().asList();
            receiptItems.stream()
                .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValue().getValueType())
                .map(formField -> formField.getValue().asMap())
                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                    if ("Quantity".equals(key)) {
                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                            Float quantity = formField.getValue().asFloat();
                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                quantity, formField.getConfidence());
                        }
                    }
                }));
        }
    }
}
```

Analyze receipt data using 4.x.x `beginAnalyzeDocumentFromUrl`:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L134-L199-->
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
                .map(documentField -> documentField.getValueMap())
                .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                    if ("Name".equals(key)) {
                        if (DocumentFieldType.STRING == documentField.getType()) {
                            String name = documentField.getValueString();
                            System.out.printf("Name: %s, confidence: %.2fs%n",
                                name, documentField.getConfidence());
                        }
                    }
                    if ("Quantity".equals(key)) {
                        if (DocumentFieldType.FLOAT == documentField.getType()) {
                            Float quantity = documentField.getValueFloat();
                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                quantity, documentField.getConfidence());
                        }
                    }
                }));
        }
    }
}
```
#### Using a layout model
- Recognize text, table structures and selection marks like radio buttons and check boxes, along with their bounding box
coordinates, from documents.

Analyze layout using 3.x.x `beginRecognizeContent`:
```java
// recognize form content using file input stream
File form = new File("local/file_path/filename.png");
byte[] fileContent = Files.readAllBytes(form.toPath());
InputStream inputStream = new ByteArrayInputStream(fileContent);

SyncPoller<FormRecognizerOperationResult, List<FormPage>> recognizeContentPoller =
    formRecognizerClient.beginRecognizeContent(inputStream, form.length());

List<FormPage> contentPageResults = recognizeContentPoller.getFinalResult();

for (int i = 0; i < contentPageResults.size(); i++) {
    FormPage formPage = contentPageResults.get(i);
    System.out.printf("----Recognizing content info for page %d ----%n", i);
    // Table information
    System.out.printf("Has width: %f and height: %f, measured with unit: %s.%n", formPage.getWidth(),
        formPage.getHeight(),
        formPage.getUnit());
    formPage.getTables().forEach(formTable -> {
        System.out.printf("Table has %d rows and %d columns.%n", formTable.getRowCount(),
            formTable.getColumnCount());
        formTable.getCells().forEach(formTableCell ->
            System.out.printf("Cell has text %s.%n", formTableCell.getText()));
    });
    // Selection Mark
    formPage.getSelectionMarks().forEach(selectionMark -> System.out.printf(
        "Page: %s, Selection mark is %s within bounding box %s has a confidence score %.2f.%n",
        selectionMark.getPageNumber(), selectionMark.getState(), selectionMark.getBoundingBox().toString(),
        selectionMark.getConfidence()));
}
```

Analyze layout using 4.x.x `beginAnalyzeDocument`:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L85-L127-->
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

#### Using a custom model
- Analyze key/value pairs and table data from documents. These models are trained with your own data,
so they're tailored to your documents.

Analyze custom document using 3.x.x `beginRecognizeCustomFormsFromUrl`:
```java
String formUrl = "{form_url}";
String modelId = "{custom_trained_model_id}";
SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> recognizeFormPoller =
    formRecognizerClient.beginRecognizeCustomFormsFromUrl(modelId, formUrl);

List<RecognizedForm> recognizedForms = recognizeFormPoller.getFinalResult();

for (int i = 0; i < recognizedForms.size(); i++) {
    RecognizedForm form = recognizedForms.get(i);
    System.out.printf("----------- Recognized custom form info for page %d -----------%n", i);
    System.out.printf("Form type: %s%n", form.getFormType());
    System.out.printf("Form type confidence: %.2f%n", form.getFormTypeConfidence());
    form.getFields().forEach((label, formField) ->
        System.out.printf("Field %s has value %s with confidence score of %f.%n", label,
            formField.getValueData().getText(),
            formField.getConfidence())
    );
}
```

Analyze custom document using 4.x.x `beginAnalyzeDocumentFromUrl`
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L235-L287-->
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

analyzeResult.getPages().forEach(documentPage -> {
    System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
        documentPage.getWidth(),
        documentPage.getHeight(),
        documentPage.getUnit());

    // lines
    documentPage.getLines().forEach(documentLine ->
        System.out.printf("Line %s is within a bounding box %s.%n",
            documentLine.getContent(),
            documentLine.getBoundingBox().toString()));

    // words
    documentPage.getWords().forEach(documentWord ->
        System.out.printf("Word %s has a confidence score of %.2f%n.",
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

Analyzing general prebuilt document types with 4.x.x:
> NOTE: Analyzing a document with the prebuilt-document model replaces training without labels in version 3.1.x of the library.
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L294-L357-->
```java
String documentUrl = "{document-url}";
String modelId = "prebuilt-document";
SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeDocumentPoller =
    documentAnalysisClient.beginAnalyzeDocumentFromUrl(modelId, documentUrl);

AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult();

for (int i = 0; i < analyzeResult.getDocuments().size(); i++) {
    final AnalyzedDocument analyzedDocument = analyzeResult.getDocuments().get(i);
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
        System.out.printf("Line %s is within a bounding box %s.%n",
            documentLine.getContent(),
            documentLine.getBoundingBox().toString()));

    // words
    documentPage.getWords().forEach(documentWord ->
        System.out.printf("Word %s has a confidence score of %.2f%n.",
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

// Entities
analyzeResult.getEntities().forEach(documentEntity -> {
    System.out.printf("Entity category : %s, sub-category %s%n: ",
        documentEntity.getCategory(), documentEntity.getSubCategory());
    System.out.printf("Entity content: %s%n: ", documentEntity.getContent());
    System.out.printf("Entity confidence: %.2f%n", documentEntity.getConfidence());
});

// Key-value
analyzeResult.getKeyValuePairs().forEach(documentKeyValuePair -> {
    System.out.printf("Key content: %s%n", documentKeyValuePair.getKey().getContent());
    System.out.printf("Key content bounding region: %s%n",
        documentKeyValuePair.getKey().getBoundingRegions().toString());

    System.out.printf("Value content: %s%n", documentKeyValuePair.getValue().getContent());
    System.out.printf("Value content bounding region: %s%n", documentKeyValuePair.getValue().getBoundingRegions().toString());
});
```

#### Build a custom document analysis model
- In 3.x.x, creating a custom model required specifying `useTrainingLabels` to indicate whether to use labeled data when creating the custom model with the `beginTraining` method.
- In 4.x.x, we introduced the new general document model (prebuilt-document) to replace the train without labels 
functionality from 3.x.x which extracts entities, key-value pairs, and layout from a document with the `beginBuildModel` method.

Train a custom model using 3.x.x `beginTraining`:
```java
String trainingFilesUrl = "{SAS_URL_of_your_container_in_blob_storage}";
SyncPoller<FormRecognizerOperationResult, CustomFormModel> trainingPoller =
    formTrainingClient.beginTraining(trainingFilesUrl,
        false,
        new TrainingOptions()
            .setModelName("my model trained without labels"),
        Context.NONE);

CustomFormModel customFormModel = trainingPoller.getFinalResult();

// Model Info
System.out.printf("Model Id: %s%n", customFormModel.getModelId());
System.out.printf("Model name given by user: %s%n", customFormModel.getModelName());
System.out.printf("Model Status: %s%n", customFormModel.getModelStatus());
System.out.printf("Training started on: %s%n", customFormModel.getTrainingStartedOn());
System.out.printf("Training completed on: %s%n%n", customFormModel.getTrainingCompletedOn());

System.out.println("Recognized Fields:");
// looping through the subModels, which contains the fields they were trained on
// Since the given training documents are unlabeled we still group them but, they do not have a label.
customFormModel.getSubmodels().forEach(customFormSubmodel -> {
    System.out.printf("Submodel Id: %s%n: ", customFormSubmodel.getModelId());
    // Since the training data is unlabeled, we are unable to return the accuracy of this model
    customFormSubmodel.getFields().forEach((field, customFormModelField) ->
        System.out.printf("Field: %s Field Label: %s%n",
            field, customFormModelField.getLabel()));
});

System.out.println();
customFormModel.getTrainingDocuments().forEach(trainingDocumentInfo -> {
    System.out.printf("Document name: %s%n", trainingDocumentInfo.getName());
    System.out.printf("Document status: %s%n", trainingDocumentInfo.getStatus());
    System.out.printf("Document page count: %d%n", trainingDocumentInfo.getPageCount());
    if (!trainingDocumentInfo.getErrors().isEmpty()) {
        System.out.println("Document Errors:");
        trainingDocumentInfo.getErrors().forEach(formRecognizerError ->
            System.out.printf("Error code %s, Error message: %s%n", formRecognizerError.getErrorCode(),
            formRecognizerError.getMessage()));
    }
});
```

Build a custom document model using 4.x.x `beginBuildModel`:
<!-- embedme ./src/samples/java/com/azure/ai/formrecognizer/ReadmeSamples.java#L206-L228 -->
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
In 3.x.x, listing models returned only the custom trained models using the `listCustomModel` method.

With 4.x.x, list Models operation `listModels`:
- returns a paged list of prebuilt in addition to custom models.
- no longer includes submodels, instead a model can analyze different document types.
- Only returns custom models that are built successfully. 
Unsuccessful model operations can be viewed with the get and list operation methods (note that document model operation data persists for only 24 hours).
- In version `3.1.x` of the library, models that had not succeeded were still created, had to be deleted by the user, 
and were returned in the list models response.

## Additional samples

For additional samples please take a look at the [Form Recognizer samples][README-Samples]

<!-- Links -->
[DocumentAnalysisClientBuilder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/DocumentAnalysisClientBuilder.java
[DocumentAnalysisClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/DocumentAnalysisClient.java
[DocumentAnalysisAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/DocumentAnalysisAsyncClient.java
[DocumentModelAdministrationClientBuilder]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/administration/DocumentModelAdministrationClientBuilder.java
[DocumentModelAdministrationClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/administration/DocumentModelAdministrationClient.java
[DocumentModelAdministrationAsyncClient]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/main/java/com/azure/ai/formrecognizer/administration/DocumentModelAdministrationAsyncClient.java
[Guidelines]: https://azure.github.io/azure-sdk/general_introduction.html
[GuidelinesJava]: https://azure.github.io/azure-sdk/java_introduction.html
[GuidelinesJavaDesign]: https://azure.github.io/azure-sdk/java_introduction.html#namespaces
[README-Samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/README.md
[README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/README.md
<!-- [service_supported_models]: TODO -->

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fformrecognizer%2Fazure-ai-formrecognizer%2Fmigration-guide.png)
