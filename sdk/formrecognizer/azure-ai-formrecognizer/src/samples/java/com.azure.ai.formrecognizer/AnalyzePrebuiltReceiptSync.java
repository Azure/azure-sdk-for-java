package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormRecognizerApiKeyCredential;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.ReceiptPageResult;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class AnalyzePrebuiltReceiptSync {

    public static void main(final String[] args) throws IOException {
        // Instantiate a client that will be used to call the service.

        final FormRecognizerClient client = new FormRecognizerClientBuilder()
            .apiKey(new FormRecognizerApiKeyCredential(""))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        File sourceFile = new File("/C://Downloads/contoso-allinone.jpg");

        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);
        SyncPoller<OperationResult, IterableStream<ReceiptPageResult>> analyzeReceiptPoller =
            client.beginAnalyzeReceipt(targetStream, sourceFile.length(), false, FormContentType.IMAGE_JPEG);

        IterableStream<ReceiptPageResult> receiptPageResults = analyzeReceiptPoller.getFinalResult();

        for (ReceiptPageResult receiptPageResultItem : receiptPageResults) {
            System.out.printf("Page Number %s%n", receiptPageResultItem.getPageInfo().getPageNumber());
            System.out.printf("Merchant Name %s%n", receiptPageResultItem.getMerchantName().getText());
            System.out.printf("Merchant Address %s%n", receiptPageResultItem.getMerchantAddress().getText());
            System.out.printf("Merchant Phone Number %s%n", receiptPageResultItem.getMerchantPhoneNumber().getText());
            System.out.printf("Total: %s%n", receiptPageResultItem.getTotal().getText());
            System.out.printf("Receipt Items: %n");
            receiptPageResultItem.getReceiptItems().forEach(receiptItem -> {
                System.out.printf("Name: %s%n", receiptItem.getName().getText());
                System.out.printf("Quantity: %s%n", receiptItem.getQuantity().getText());
                System.out.printf("Total Price: %s%n", receiptItem.getTotalPrice().getText());
                System.out.println();
            });
            // print raw ocr
            if (receiptPageResultItem.getExtractedLines() != null) {
                receiptPageResultItem.getExtractedLines().forEach(extractedLine -> {
                    System.out.printf("Extracted Line text: %s%n", extractedLine.getText());
                    extractedLine.extractedWords.forEach(extractedWord -> {
                        System.out.printf("Extracted Word text: %s%n", extractedWord.getText());
                        System.out.printf("Extracted Word confidence: %.2f%n", extractedWord.getConfidence());
                    });
                });
            }
        }
    }
}
