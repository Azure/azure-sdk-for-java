// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Async sample for recognizing US receipt information using file source URL.
 */
public class RecognizeReceiptsFromUrlAsync {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String receiptUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/java/sample-forms/receipts/contoso-allinone.jpg";
        PollerFlux<OperationResult, List<RecognizedReceipt>> recognizeReceiptPoller =
            client.beginRecognizeReceiptsFromUrl(receiptUrl);

        Mono<List<RecognizedReceipt>> receiptPageResults = recognizeReceiptPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    System.out.println("Polling completed successfully");
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus()));
                }
            });

        receiptPageResults.subscribe(recognizedReceipts -> {
            for (int i = 0; i < recognizedReceipts.size(); i++) {
                RecognizedReceipt recognizedReceipt = recognizedReceipts.get(i);
                Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
                System.out.printf("----------- Recognized Receipt page %s -----------%n", i);
                FormField merchantNameField = recognizedFields.get("MerchantName");
                if (merchantNameField != null) {
                    if (merchantNameField.getFieldValue().getType() == FieldValueType.STRING) {
                        System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                            merchantNameField.getFieldValue().asString(),
                            merchantNameField.getConfidence());
                    }
                }
                FormField merchantAddressField = recognizedFields.get("MerchantAddress");
                if (merchantAddressField != null) {
                    if (merchantAddressField.getFieldValue().getType() == FieldValueType.STRING) {
                        System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                            merchantAddressField.getFieldValue().asString(),
                            merchantAddressField.getConfidence());
                    }
                }
                FormField transactionDateField = recognizedFields.get("TransactionDate");
                if (transactionDateField != null) {
                    if (transactionDateField.getFieldValue().getType() == FieldValueType.DATE) {
                        System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                            transactionDateField.getFieldValue().asDate(),
                            transactionDateField.getConfidence());
                    }
                }
                FormField receiptItemsField = recognizedFields.get("Items");
                if (receiptItemsField != null) {
                    System.out.printf("Receipt Items: %n");
                    if (receiptItemsField.getFieldValue().getType() == FieldValueType.LIST) {
                        List<FormField> receiptItems = receiptItemsField.getFieldValue().asList();
                        receiptItems.forEach(receiptItem -> {
                            if (receiptItem.getFieldValue().getType() == FieldValueType.MAP) {
                                receiptItem.getFieldValue().asMap().forEach((key, formField) -> {
                                    if (key.equals("Name")) {
                                        if (formField.getFieldValue().getType() == FieldValueType.STRING) {
                                            System.out.printf("Name: %s, confidence: %.2fs%n",
                                                formField.getFieldValue().asString(),
                                                formField.getConfidence());
                                        }
                                    }
                                    if (key.equals("Quantity")) {
                                        if (formField.getFieldValue().getType() == FieldValueType.INTEGER) {
                                            System.out.printf("Quantity: %s, confidence: %.2f%n",
                                                formField.getFieldValue().asInteger(), formField.getConfidence());
                                        }
                                    }
                                    if (key.equals("Price")) {
                                        if (formField.getFieldValue().getType() == FieldValueType.FLOAT) {
                                            System.out.printf("Price: %s, confidence: %.2f%n",
                                                formField.getFieldValue().asFloat(),
                                                formField.getConfidence());
                                        }
                                    }
                                    if (key.equals("TotalPrice")) {
                                        if (formField.getFieldValue().getType() == FieldValueType.FLOAT) {
                                            System.out.printf("Total Price: %s, confidence: %.2f%n",
                                                formField.getFieldValue().asFloat(),
                                                formField.getConfidence());
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
                System.out.print("-----------------------------------");
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
