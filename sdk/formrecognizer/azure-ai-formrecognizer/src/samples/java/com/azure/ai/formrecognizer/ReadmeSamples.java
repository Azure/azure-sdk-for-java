// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTable;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class containing code snippets that will be injected to README.md.
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private final DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder().buildClient();
    private final DocumentModelAdministrationClient documentModelAdminClient =
        new DocumentModelAdministrationClientBuilder().buildClient();

    /**
     * Code snippet for getting sync client using the AzureKeyCredential authentication.
     */
    public void useAzureKeyCredentialSyncClient() {
        // BEGIN: readme-sample-createDocumentAnalysisClient
        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: readme-sample-createDocumentAnalysisClient
    }

    /**
     * Code snippet for getting async client using AAD authentication.
     */
    public void useAadClient() {
        // BEGIN: readme-sample-createDocumentAnalysisClientWithAAD
        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createDocumentAnalysisClientWithAAD
    }

    /**
     * Extract layout data for provided document.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void extractLayout() throws IOException {
        // BEGIN: readme-sample-extractLayout
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
        // END: readme-sample-extractLayout
    }

    /**
     * Code snippet for analyzing receipt data using prebuilt receipt models.
     */
    public void analyzeReceiptFromUrl() {
        // BEGIN: readme-sample-analyzeReceiptFromUrl
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
        // END: readme-sample-analyzeReceiptFromUrl
    }

    /**
     * Code snippet for building custom document analysis models using training data.
     */
    public void buildModel() {
        // BEGIN: readme-sample-buildModel
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
        // END: readme-sample-buildModel
    }

    /**
     * Code snippet for analyzing custom documents using custom-built models.
     */
    public void analyzeCustomDocument() {
        // BEGIN: readme-sample-analyzeCustomDocument
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
        // END: readme-sample-analyzeCustomDocument
    }

    /**
     * Code snippet for analyzing general documents using "prebuilt-document" models.
     */
    public void analyzePrebuiltDocument() {
        // BEGIN: readme-sample-analyzePrebuiltDocument
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
        // END: readme-sample-analyzePrebuiltDocument
    }

    /**
     * Code snippet for managing models in form recognizer account.
     */
    public void manageModels() {
        // BEGIN: readme-sample-manageModels
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
        // END: readme-sample-manageModels
    }

    /**
     * Code snippet for handling exception
     */
    public void handlingException() {
        // BEGIN: readme-sample-handlingException
        try {
            documentAnalysisClient.beginAnalyzeDocumentFromUrl("prebuilt-receipt", "invalidSourceUrl");
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
            // Do something with the exception
        }
        // END: readme-sample-handlingException
    }

    /**
     * Code snippet for handling exception
     */
    public void handlingExceptionAsync() {
        DocumentModelAdministrationAsyncClient administrationAsyncClient = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        // BEGIN: readme-sample-async-handlingException
        administrationAsyncClient.deleteDocumentModel("{modelId}")
            .doOnSuccess(
                ignored -> System.out.println("Success!"))
            .doOnError(
                error -> error instanceof ResourceNotFoundException,
                error -> System.out.println("Exception: Delete could not be performed."));
        // END: readme-sample-async-handlingException
    }

    /**
     * Code snippet for getting async client using the AzureKeyCredential authentication.
     */
    public void useAzureKeyCredentialAsyncClient() {
        // BEGIN: readme-sample-asyncClient
        DocumentAnalysisAsyncClient documentAnalysisAsyncClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: readme-sample-asyncClient
    }

    /**
     * Code snippet for getting sync DocumentModelAdministration client using the AzureKeyCredential authentication.
     */
    public void enableLoggingDocumentAnalysisClient() {
        // BEGIN: readme-sample-enablehttplogging
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: readme-sample-enablehttplogging
    }

    public void beginClassifyDocumentFromUrl() throws IOException {
        // BEGIN: readme-sample-classifyDocument
        String documentUrl = "{file_source_url}";
        String classifierId = "{custom_trained_classifier_id}";

        documentAnalysisClient.beginClassifyDocumentFromUrl(classifierId, documentUrl, Context.NONE)
            .getFinalResult()
            .getDocuments()
            .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
        // END: readme-sample-classifyDocument
    }
}
