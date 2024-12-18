// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.Document;
import com.azure.ai.documentintelligence.models.DocumentField;
import com.azure.ai.documentintelligence.models.DocumentFieldType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Async sample for analyzing commonly found receipt fields from a local file input stream.
 * See fields found on a receipt <a href=https://aka.ms/documentintelligence/receiptfields>here</a>
 */
public class AnalyzeReceiptsAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        DocumentIntelligenceAsyncClient client = new DocumentIntelligenceClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        File sourceFile = new File("../documentintelligence/azure-ai-documentintelligence/src/samples/resources/"
            + "sample-forms/receipts/contoso-allinone.jpg");

        PollerFlux<AnalyzeResultOperation, AnalyzeResult> analyzeReceiptPoller;
        analyzeReceiptPoller = client.beginAnalyzeDocument("prebuilt-receipt",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            new AnalyzeDocumentRequest().setBase64Source(Files.readAllBytes(sourceFile.toPath())));


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
                Document analyzedReceipt = receiptResults.getDocuments().get(i);
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

                DocumentField merchantAddressField = receiptFields.get("MerchantAddress");
                if (merchantAddressField != null) {
                    if (DocumentFieldType.STRING == merchantAddressField.getType()) {
                        String merchantAddress = merchantAddressField.getValueString();
                        System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                            merchantAddress, merchantAddressField.getConfidence());
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
                    if (DocumentFieldType.ARRAY == receiptItemsField.getType()) {
                        List<DocumentField> receiptItems = receiptItemsField.getValueArray();
                        receiptItems.stream()
                            .filter(receiptItem -> DocumentFieldType.OBJECT == receiptItem.getType())
                            .map(documentField -> documentField.getValueObject())
                            .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                                if ("Name".equals(key)) {
                                    if (DocumentFieldType.STRING == documentField.getType()) {
                                        String name = documentField.getValueString();
                                        System.out.printf("Name: %s, confidence: %.2fs%n",
                                            name, documentField.getConfidence());
                                    }
                                }
                                if ("Quantity".equals(key)) {
                                    if (DocumentFieldType.NUMBER == documentField.getType()) {
                                        Double quantity = documentField.getValueNumber();
                                        System.out.printf("Quantity: %f, confidence: %.2f%n",
                                            quantity, documentField.getConfidence());
                                    }
                                }
                                if ("Price".equals(key)) {
                                    if (DocumentFieldType.NUMBER == documentField.getType()) {
                                        Double price = documentField.getValueNumber();
                                        System.out.printf("Price: %f, confidence: %.2f%n",
                                            price, documentField.getConfidence());
                                    }
                                }
                                if ("TotalPrice".equals(key)) {
                                    if (DocumentFieldType.NUMBER == documentField.getType()) {
                                        Double totalPrice = documentField.getValueNumber();
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
