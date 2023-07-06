# Samples

## Building Document Classifiers
```java readme-sample-build-classifier

// Build custom classifier document model
String trainingBlobUrl = "{SAS_URL_of_your_container_in_blob_storage}";

TrainingDataContentSource contentSourceA
    = new AzureBlobContentSource(trainingBlobUrl).setPrefix("IRS-1040-A/train");
TrainingDataContentSource contentSourceB
    = new AzureBlobFileListContentSource(trainingBlobUrl, "IRS-1040-B.jsonl");

HashMap<String, ClassifierDocumentTypeDetails> docTypes = new HashMap<String, ClassifierDocumentTypeDetails>() {
    {
        put("1040-A", new ClassifierDocumentTypeDetails(contentSourceA));
        put("1040-B", new ClassifierDocumentTypeDetails(contentSourceB));
    }};

DocumentClassifierDetails documentClassifierDetails
    = documentModelAdminClient.beginBuildDocumentClassifier(docTypes).getFinalResult();

System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());
System.out.printf("Classifier created on: %s%n", documentClassifierDetails.getCreatedOn());

documentClassifierDetails.getDocTypes().forEach((key, documentTypeDetails) -> {
    if (documentTypeDetails.getTrainingDataContentSource() instanceof AzureBlobContentSource) {
        System.out.printf("Blob Source container Url: %s", ((AzureBlobContentSource) documentTypeDetails
            .getTrainingDataContentSource()).getContainerUrl());
    }
});
```

## Classifying documents
```java readme-sample-classifyDocument
String documentUrl = "{file_source_url}";
String classifierId = "{custom_trained_classifier_id}";

documentAnalysisClient.beginClassifyDocumentFromUrl(classifierId, documentUrl, Context.NONE)
    .getFinalResult()
    .getDocuments()
    .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
```

## Build document model source overload
```java readme-sample-build-classifier-source-overload
// Existing
String blobContainerUrl = "{SAS_URL_of_your_container_in_blob_storage}";
String prefix = "{blob_name_prefix}}";

documentModelAdminClient.beginBuildDocumentModel(blobContainerUrl,
    DocumentModelBuildMode.TEMPLATE,
    prefix,
    new BuildDocumentModelOptions().setModelId("my-build-model").setDescription("model desc"),
    Context.NONE);

// New overload
documentModelAdminClient.beginBuildDocumentModel(
    new AzureBlobContentSource(blobContainerUrl).setPrefix(prefix),
    DocumentModelBuildMode.TEMPLATE);

// Using AzureBlobFileListContentSource source type
documentModelAdminClient.beginBuildDocumentModel(
    new AzureBlobFileListContentSource(blobContainerUrl, "file-list.jsonL"),
    DocumentModelBuildMode.TEMPLATE,
    new BuildDocumentModelOptions().setModelId("my-build-model").setDescription("model desc"),
    Context.NONE);
```

## Analyzing documents with premium features
```java readme-sample-analyzeDocument-premium
File document = new File("{local/file_path/fileName.jpg}");
String modelId = "{model_id}";
final AnalyzeDocumentOptions analyzeDocumentOptions
    = new AnalyzeDocumentOptions()
    .setDocumentAnalysisFeatures(Arrays.asList(DocumentAnalysisFeature.FORMULAS));

// Utility method to convert input stream to Binary Data
BinaryData data = BinaryData.fromStream(new ByteArrayInputStream(Files.readAllBytes(document.toPath())));

AnalyzeResult analyzeResult
    = documentAnalysisClient.beginAnalyzeDocument(modelId, data, analyzeDocumentOptions, Context.NONE)
    .getFinalResult();

analyzeResult.getPages()
    .forEach(documentPage -> {
        System.out.printf("Document page Number: %s%n", documentPage.getPageNumber());
        documentPage.getFormulas()
            .forEach(documentFormula -> System.out.printf("Formula value: %s%n", documentFormula.getValue()));
    });
```

## Manage Classifiers
```java readme-sample-manage-classifiers
// Get a paged list of all document classifiers
PagedIterable<DocumentClassifierDetails> documentClassifierDetailList = documentModelAdminClient.listDocumentClassifiers();
System.out.println("We have following classifiers in the account:");
documentClassifierDetailList.forEach(documentClassifierDetails -> {
    System.out.printf("Classifier ID: %s%n", documentClassifierDetails.getClassifierId());

    // get Classifier info
    classifierId.set(documentClassifierDetails.getClassifierId());
    DocumentClassifierDetails documentClassifier = documentModelAdminClient.getDocumentClassifier(documentClassifierDetails.getClassifierId());
    System.out.printf("Classifier ID: %s%n", documentClassifier.getClassifierId());
    System.out.printf("Classifier created on: %s%n", documentClassifier.getCreatedOn());
    documentClassifier.getDocTypes().forEach((key, documentTypeDetails) -> {
        if (documentTypeDetails.getTrainingDataContentSource() instanceof AzureBlobContentSource) {
            System.out.printf("Blob Source container Url: %s", ((AzureBlobContentSource) documentTypeDetails
                .getTrainingDataContentSource()).getContainerUrl());
        }
    });
});

// Delete classifier
System.out.printf("Deleted Classifier with Classifier ID: %s, operation completed with status: %s%n", classifierId.get(),
    documentModelAdminClient.deleteDocumentClassifierWithResponse(classifierId.get(), Context.NONE).getStatusCode());

```


