// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 * <p>
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    private FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder().buildClient();

    /**
     * Code snippet for configuring http client.
     */
    public void configureHttpClient() {
        HttpClient client = new NettyAsyncHttpClientBuilder()
            .port(8080)
            .wiretap(true)
            .build();
    }

    /**
     * Code snippet for getting sync client using the API key authentication.
     */
    public void useApiKeySyncClient() {
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildClient();
    }

    /**
     * Code snippet for getting async client using API key authentication.
     */
    public void useApiKeyAsyncClient() {
        FormRecognizerAsyncClient formRecognizerAsyncClient = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
    }

    /**
     * Code snippet for rotating API key of the client
     */
    public void rotatingApiKey() {
        AzureKeyCredential credential = new AzureKeyCredential("{api_key}");
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .apiKey(credential)
            .endpoint("{endpoint}")
            .buildClient();

        credential.update("{new_api_key}");
    }

    public void extractReceipt() {
        String receiptSourceUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-allinone.jpg";
        SyncPoller<OperationResult, IterableStream<ExtractedReceipt>> syncPoller =
            formRecognizerClient.beginExtractReceiptsFromUrl(receiptSourceUrl);
        IterableStream<ExtractedReceipt> extractedReceipts = syncPoller.getFinalResult();

        for (ExtractedReceipt extractedReceiptItem : extractedReceipts) {
            System.out.printf("Page Number %s%n", extractedReceiptItem.getPageMetadata().getPageNumber());
            System.out.printf("Merchant Name %s%n", extractedReceiptItem.getMerchantName().getText());
            System.out.printf("Merchant Address %s%n", extractedReceiptItem.getMerchantAddress().getText());
            System.out.printf("Merchant Phone Number %s%n", extractedReceiptItem.getMerchantPhoneNumber().getText());
            System.out.printf("Total: %s%n", extractedReceiptItem.getTotal().getText());
            System.out.printf("Receipt Items: %n");
            extractedReceiptItem.getReceiptItems().forEach(receiptItem -> {
                System.out.printf("Name: %s%n", receiptItem.getName().getText());
                System.out.printf("Quantity: %s%n", receiptItem.getQuantity().getText());
                System.out.printf("Total Price: %s%n", receiptItem.getTotalPrice().getText());
                System.out.println();
            });
        }
    }
}
