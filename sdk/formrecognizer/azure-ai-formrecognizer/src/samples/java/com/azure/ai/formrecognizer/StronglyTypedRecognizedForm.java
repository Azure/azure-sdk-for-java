// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormRecognizerLocale;
import com.azure.ai.formrecognizer.models.FormRecognizerOperationResult;
import com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

/**
 * Sample demonstrating converting recognized form fields to strongly typed US receipt field values.
 * See
 * <a href="https://aka.ms/formrecognizer/receiptfields"></a>
 * for information on the strongly typed fields returned by service when recognizing receipts.
 * More information on the Receipt used in the example below can be found
 * <a href="https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/formrecognizer/azure-ai-formrecognizer/src/samples/resources/java/com/azure/ai/formrecognizer/Receipt.java">here</a>
 */
public class StronglyTypedRecognizedForm {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String receiptUrl =
            "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/formrecognizer"
                + "/azure-ai-formrecognizer/src/samples/resources/sample-forms/receipts/contoso-allinone.jpg";
        SyncPoller<FormRecognizerOperationResult, List<RecognizedForm>> recognizeReceiptPoller =
            client.beginRecognizeReceiptsFromUrl(receiptUrl,
                new RecognizeReceiptsOptions()
                    .setLocale(FormRecognizerLocale.EN_US),
                Context.NONE);

        List<RecognizedForm> receiptPageResults = recognizeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptPageResults.size(); i++) {
            final RecognizedForm recognizedForm = receiptPageResults.get(i);
            System.out.printf("----------- Recognized receipt info for page %d -----------%n", i);
            // Use Receipt model to transform the recognized form to a strongly typed US receipt
            Receipt usReceipt = new Receipt(recognizedForm);
            System.out.printf("Merchant Name: %s, confidence: %.2f%n", usReceipt.getMerchantName().getValue(),
                usReceipt.getMerchantName().getConfidence());
            System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                usReceipt.getMerchantAddress().getValue(),
                usReceipt.getMerchantAddress().getConfidence());
            System.out.printf("Merchant Phone Number %s, confidence: %.2f%n",
                usReceipt.getMerchantPhoneNumber().getValue(), usReceipt.getMerchantPhoneNumber().getConfidence());
            System.out.printf("Total: %.2f confidence: %.2f%n", usReceipt.getTotal().getValue(),
                usReceipt.getTotal().getConfidence());
            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                usReceipt.getTransactionDate().getValue(), usReceipt.getTransactionDate().getConfidence());
            System.out.printf("Transaction Time: %s, confidence: %.2f%n",
                usReceipt.getTransactionTime().getValue(), usReceipt.getTransactionTime().getConfidence());
            System.out.printf("Receipt Items: %n");
            usReceipt.getReceiptItems().forEach(receiptItem -> {
                if (receiptItem.getName() != null) {
                    System.out.printf("Name: %s, confidence: %.2f%n", receiptItem.getName().getValue(),
                        receiptItem.getName().getConfidence());
                }
                if (receiptItem.getQuantity() != null) {
                    System.out.printf("Quantity: %.2f, confidence: %.2f%n", receiptItem.getQuantity().getValue(),
                        receiptItem.getQuantity().getConfidence());
                }
                if (receiptItem.getPrice() != null) {
                    System.out.printf("Price: %.2f, confidence: %.2f%n", receiptItem.getPrice().getValue(),
                        receiptItem.getPrice().getConfidence());
                }
                if (receiptItem.getTotalPrice() != null) {
                    System.out.printf("Total Price: %.2f, confidence: %.2f%n",
                        receiptItem.getTotalPrice().getValue(), receiptItem.getTotalPrice().getConfidence());
                }
            });
            System.out.println("-----------------------------------");
        }
    }
}
