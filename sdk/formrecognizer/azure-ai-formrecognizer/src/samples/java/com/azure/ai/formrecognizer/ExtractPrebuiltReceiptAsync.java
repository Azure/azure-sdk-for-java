// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;

/**
 * Sample for extracting receipt information using input stream.
 */
public class ExtractPrebuiltReceiptAsync {
    /**
     * Sample for extracting receipt information using input stream.
     *
     * @param args Unused. Arguments to the program.
     * @throws IOException Unused. If an I/O error occurs reading from the stream of file.
     */
    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.
        FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new AzureKeyCredential("{api_key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        File sourceFile = new File("azure-ai-formrecognizer/src/test/resources/sample-files/contoso-allinone.jpg");
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        PollerFlux<OperationResult, IterableStream<ExtractedReceipt>> analyzeReceiptPoller =
            client.beginExtractReceipts(buffer, sourceFile.length(), true, FormContentType.IMAGE_PNG,
                Duration.ofSeconds(5));

        IterableStream<ExtractedReceipt> receiptPageResults = analyzeReceiptPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                if (trainingOperationResponse.getStatus().isComplete()) {
                    System.out.println("Polling completed successfully");
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    System.out.printf("polling completed unsuccessfully with status: %s.", trainingOperationResponse.getStatus());
                    return Mono.empty();
                }
            }).block();

        receiptPageResults.forEach(extractedReceiptItem -> {
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
        });
    }
}
