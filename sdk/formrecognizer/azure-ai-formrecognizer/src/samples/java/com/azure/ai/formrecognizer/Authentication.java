// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Samples for two supported methods of authentication in Document Analysis and Document Model Administration clients:
 * 1) Use a Form Recognizer API key with AzureKeyCredential from azure.core.credentials
 * 2) Use a token credential from azure-identity to authenticate with Azure Active Directory
 */
public class Authentication {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        /*
          Set the environment variables with your own values before running the sample:
          AZURE_CLIENT_ID - the client ID of your active directory application.
          AZURE_TENANT_ID - the tenant ID of your active directory application.
          AZURE_CLIENT_SECRET - the secret of your active directory application.
         */

        // Document Analysis client: Key credential
        authenticationWithKeyCredentialDocumentAnalysisClient();
        // Document Analysis client: Azure Active Directory
        authenticationWithAzureActiveDirectoryDocumentAnalysisClient();
        // Document Analysis client: Azure Active Directory : China Cloud
        authenticationWithAzureActiveDirectoryChinaCloud();
        // Document Model Administration client: Key credential
        authenticationWithKeyCredentialDocumentModelAdministrationClient();
        // Document Model Administration client: Azure Active Directory
        authenticationWithAzureActiveDirectoryDocumentModelAdministrationClient();
    }

    private static void authenticationWithKeyCredentialDocumentAnalysisClient() {
        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        beginRecognizeCustomFormsFromUrl(documentAnalysisClient);
    }

    private static void authenticationWithAzureActiveDirectoryDocumentAnalysisClient() {
        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("{endpoint}")
            .buildClient();
        beginRecognizeCustomFormsFromUrl(documentAnalysisClient);
    }

    private static void authenticationWithAzureActiveDirectoryChinaCloud() {
        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().authorityHost(AzureAuthorityHosts.AZURE_CHINA).build())
            .endpoint("https://{endpoint}.cognitiveservices.azure.cn/")
            .buildClient();
        beginRecognizeCustomFormsFromUrl(documentAnalysisClient);
    }

    private static void authenticationWithKeyCredentialDocumentModelAdministrationClient() {
        DocumentModelAdministrationClient documentModelAdminClient = new DocumentModelAdministrationClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        getResourceInfo(documentModelAdminClient);
    }

    private static void authenticationWithAzureActiveDirectoryDocumentModelAdministrationClient() {
        DocumentModelAdministrationClient documentModelAdminClient = new DocumentModelAdministrationClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("{endpoint}")
            .buildClient();
        getResourceInfo(documentModelAdminClient);
    }

    private static void beginRecognizeCustomFormsFromUrl(DocumentAnalysisClient documentAnalysisClient) {
        String receiptUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
                + "/azure-ai-formrecognizer/src/samples/resources/sample-forms/receipts/contoso-allinone.jpg";
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
                            if ("TotalPrice".equals(key)) {
                                if (DocumentFieldType.DOUBLE == documentField.getType()) {
                                    Double totalPrice = documentField.getValueAsDouble();
                                    System.out.printf("Total Price: %f, confidence: %.2f%n",
                                        totalPrice, documentField.getConfidence());
                                }
                            }
                        }));
                }
            }
            System.out.print("-----------------------------------");
        }
    }

    private static void getResourceInfo(DocumentModelAdministrationClient documentModelAdminClient) {
        ResourceDetails resourceDetails = documentModelAdminClient.getResourceDetails();
        System.out.printf("Max number of models that can be trained for this account: %s%n",
            resourceDetails.getCustomDocumentModelLimit());
        System.out.printf("Current count of built custom models: %d%n", resourceDetails.getCustomDocumentModelCount());
    }
}
