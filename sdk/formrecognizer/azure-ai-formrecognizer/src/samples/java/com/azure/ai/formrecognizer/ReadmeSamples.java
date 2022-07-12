// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.administration.models.AzureBlobContentSourceT;
import com.azure.ai.formrecognizer.administration.models.ResourceInfo;
import com.azure.ai.formrecognizer.administration.models.BuildModelOptions;
import com.azure.ai.formrecognizer.administration.models.DocumentBuildMode;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.administration.models.DocumentModelSummary;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.ai.formrecognizer.models.DocumentTable;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
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
     * Code snippet for getting sync DocumentModelAdministration client using the AzureKeyCredential authentication.
     */
    public void useAzureKeyCredentialDocumentModelAdministrationClient() {
        // BEGIN: readme-sample-createDocumentModelAdministrationClient
        DocumentModelAdministrationClient documentModelAdminClient = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: readme-sample-createDocumentModelAdministrationClient
    }

    /**
     * Code snippet for getting async client using AAD authentication.
     */
    public void useAadAsyncClient() {
        // BEGIN: readme-sample-createDocumentAnalysisClientWithAAD
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .endpoint("{endpoint}")
            .credential(credential)
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
        BinaryData layoutDocumentData = BinaryData.fromFile(filePath);

        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeLayoutResultPoller =
            documentAnalysisClient.beginAnalyzeDocument("prebuilt-layout", layoutDocumentData, layoutDocument.length());

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
                    documentSelectionMark.getState().toString(),
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
        // END: readme-sample-analyzeReceiptFromUrl
    }

    /**
     * Code snippet for building custom document analysis models using training data.
     */
    public void buildModel() {
        // BEGIN: readme-sample-buildModel
        // Build custom document analysis model
        String trainingFilesUrl = "{SAS_URL_of_your_container_in_blob_storage}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your forms.
        SyncPoller<DocumentOperationResult, DocumentModelInfo> buildOperationPoller =
            documentModelAdminClient.beginBuildModel(
                new AzureBlobContentSourceT().setContainerUrl(trainingFilesUrl),
                DocumentBuildMode.TEMPLATE,
                new BuildModelOptions().setModelId("my-build-model").setDescription("model desc"),
                Context.NONE);

        DocumentModelInfo documentModelInfo = buildOperationPoller.getFinalResult();

        // Model Info
        System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
        System.out.printf("Model Description: %s%n", documentModelInfo.getDescription());
        System.out.printf("Model created on: %s%n%n", documentModelInfo.getCreatedOn());
        documentModelInfo.getDocTypes().forEach((key, docTypeInfo) -> {
            System.out.printf("Document type: %s%n", key);
            docTypeInfo.getFieldSchema().forEach((name, documentFieldSchema) -> {
                System.out.printf("Document field: %s%n", name);
                System.out.printf("Document field type: %s%n", documentFieldSchema.getType().toString());
                System.out.printf("Document field confidence: %.2f%n", docTypeInfo.getFieldConfidence().get(name));
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
        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeDocumentPoller =
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
        ResourceInfo resourceInfo = documentModelAdminClient.getResourceInfo();
        System.out.printf("The resource has %s models, and we can have at most %s models",
            resourceInfo.getDocumentModelCount(), resourceInfo.getDocumentModelLimit());

        // Next, we get a paged list of all of our models
        PagedIterable<DocumentModelSummary> customDocumentModels = documentModelAdminClient.listModels();
        System.out.println("We have following models in the account:");
        customDocumentModels.forEach(documentModelInfo -> {
            System.out.printf("Model ID: %s%n", documentModelInfo.getModelId());
            modelId.set(documentModelInfo.getModelId());

            // get custom document analysis model info
            DocumentModelInfo documentModel = documentModelAdminClient.getModel(documentModelInfo.getModelId());
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
        }
        // END: readme-sample-handlingException
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
}
