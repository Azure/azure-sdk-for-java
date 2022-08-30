// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentOperationResult;
import com.azure.ai.formrecognizer.documentanalysis.models.TypedDocumentField;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

/**
 * Sample for analyzing commonly found receipt fields from a file source URL.
 * See fields found on a receipt here:
 * https://aka.ms/formrecognizer/receiptfields
 */
public class AnalyzeReceiptsFromUrl {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String receiptUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
                + "/azure-ai-formrecognizer/src/samples/resources/sample-forms/receipts/contoso-allinone.jpg";

        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeReceiptPoller =
            client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", receiptUrl);

        AnalyzeResult receiptResults = analyzeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptResults.getDocuments().size(); i++) {
            AnalyzedDocument analyzedReceipt = receiptResults.getDocuments().get(i);
            // Current
            Map<String, DocumentField> receiptFields = analyzedReceipt.getFields();
            System.out.printf("----------- Analyzing receipt info %d -----------%n", i);
            DocumentField merchantNameField = receiptFields.get("MerchantName");
            if (merchantNameField != null) {
                if (DocumentFieldType.STRING == merchantNameField.getType()) {
                    String merchantName = merchantNameField.getValueAsString();
                    // Users can also access `getValue` with a cast:
                    // String merchantName = (String) merchantNameField.getValue();

                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantName, merchantNameField.getConfidence());
                }
            }

            // Future feature to support strongly typed data example code
            ReceiptDocument receiptDocument = getFullyTyped(analyzedReceipt);
            TypedDocumentField<String> typedMerchantNameField = receiptDocument.getMerchantName();
            // Here users can still have access to non-typed/raw fields by:
            // Map<String, DocumentField> receiptFields = receiptDocument.getFields() // invokes AnalyzedDocument.getFields()
            if (typedMerchantNameField != null) {
                String valueMerchantName = typedMerchantNameField.getValue();

                System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                    valueMerchantName, typedMerchantNameField.getConfidence());
            }
        }
    }


    static ReceiptDocument getFullyTyped(AnalyzedDocument analyzedDocument) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(df);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper.convertValue(analyzedDocument, ReceiptDocument.class);
    }
}

