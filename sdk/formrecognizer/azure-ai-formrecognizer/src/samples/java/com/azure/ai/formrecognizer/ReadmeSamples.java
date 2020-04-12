// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.ReceiptExtensions;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.USReceipt;
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
        SyncPoller<OperationResult, IterableStream<RecognizedReceipt>> syncPoller =
            formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptSourceUrl);
        IterableStream<RecognizedReceipt> receiptPageResults = syncPoller.getFinalResult();

        receiptPageResults.forEach(recognizedReceipt -> {
            USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
            System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
            System.out.printf("Merchant Name %s%n", usReceipt.getMerchantName().getName());
            System.out.printf("Merchant Name Value: %s%n", usReceipt.getMerchantName().getFieldValue());
            System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
            System.out.printf("Merchant Address Value: %s%n", usReceipt.getMerchantAddress().getFieldValue());
            System.out.printf("Merchant Phone Number %s%n", usReceipt.getMerchantPhoneNumber().getName());
            System.out.printf("Merchant Phone Number Value: %s%n", usReceipt.getMerchantPhoneNumber().getFieldValue());
            System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
            System.out.printf("Total Value: %s%n", usReceipt.getTotal().getFieldValue());
        });
    }
}
