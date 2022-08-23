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
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sample for analyzing commonly found receipt fields from a local file input stream.
 * See fields found on a receipt here:
 * https://aka.ms/formrecognizer/receiptfields
 */
public class AnalyzeReceipts {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException from reading file.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("../formrecognizer/azure-ai-formrecognizer/src/samples/resources/"
            + "sample-forms/receipts/contoso-allinone.jpg");
        Path filePath = sourceFile.toPath();
        BinaryData receiptData = BinaryData.fromFile(filePath);

        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeReceiptPoller =
            client.beginAnalyzeDocument("prebuilt-receipt", receiptData, sourceFile.length());

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

            DocumentField merchantAddressField = receiptFields.get("MerchantAddress");
            if (merchantAddressField != null) {
                if (DocumentFieldType.STRING == merchantAddressField.getType()) {
                    String merchantAddress = merchantAddressField.getValueAsString();
                    System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                        merchantAddress, merchantAddressField.getConfidence());
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
                        .map(formField -> formField.getValueAsMap())
                        .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                            if ("Name".equals(key)) {
                                if (DocumentFieldType.STRING == formField.getType()) {
                                    String name = formField.getValueAsString();
                                    System.out.printf("Name: %s, confidence: %.2fs%n",
                                        name, formField.getConfidence());
                                }
                            }
                            if ("Quantity".equals(key)) {
                                if (DocumentFieldType.FLOAT == formField.getType()) {
                                    Float quantity = formField.getValueAsFloat();
                                    System.out.printf("Quantity: %f, confidence: %.2f%n",
                                        quantity, formField.getConfidence());
                                }
                            }
                            if ("Price".equals(key)) {
                                if (DocumentFieldType.FLOAT == formField.getType()) {
                                    Float price = formField.getValueAsFloat();
                                    System.out.printf("Price: %f, confidence: %.2f%n",
                                        price, formField.getConfidence());
                                }
                            }
                            if ("TotalPrice".equals(key)) {
                                if (DocumentFieldType.FLOAT == formField.getType()) {
                                    Float totalPrice = formField.getValueAsFloat();
                                    System.out.printf("Total Price: %f, confidence: %.2f%n",
                                        totalPrice, formField.getConfidence());
                                }
                            }
                        }));
                }
            }
        }
    }
}
