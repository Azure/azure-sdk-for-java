// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Async sample for analyzing commonly found receipt fields from a file source URL.
 * See fields found on a receipt <a href=https://aka.ms/formrecognizer/receiptfields>here</a>
 */
public class AnalyzeReceiptsFromUrlAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        DocumentAnalysisAsyncClient client = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String receiptUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
                + "/azure-ai-formrecognizer/src/samples/resources/sample-forms/receipts/contoso-allinone.jpg";

        PollerFlux<OperationResult, AnalyzeResult> analyzeReceiptPoller =
            client.beginAnalyzeDocumentFromUrl("prebuilt-receipt", receiptUrl);

        Mono<AnalyzeResult> receiptResultsMono = analyzeReceiptPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    System.out.println("Polling completed successfully");
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        receiptResultsMono.subscribe(receiptResults -> {
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
                                if ("Price".equals(key)) {
                                    if (DocumentFieldType.DOUBLE == documentField.getType()) {
                                        Double price = documentField.getValueAsDouble();
                                        System.out.printf("Price: %f, confidence: %.2f%n",
                                            price, documentField.getConfidence());
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
            }
        });

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
