package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormRecognizerApiKeyCredential;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.ReceiptPageResult;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.PollerFlux;
import reactor.core.publisher.Mono;

public class AnalyzePrebuiltReceiptAsync {
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        final FormRecognizerAsyncClient client = new FormRecognizerClientBuilder()
            .apiKey(new FormRecognizerApiKeyCredential(""))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildAsyncClient();

        String receiptUrl = "https://docs.microsoft.com/en-us/azure/cognitive-services/form-recognizer/media/contoso-allinone.jpg";
        PollerFlux<OperationResult, IterableStream<ReceiptPageResult>> analyzeReceiptPoller =
            client.beginAnalyzeReceipt(receiptUrl, true);

        IterableStream<ReceiptPageResult> receiptPageResults = analyzeReceiptPoller
            .last()
            .flatMap(trainingOperationResponse -> {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (trainingOperationResponse.getStatus().isComplete()) {
                    System.out.println("Polling completed successfully");
                    // training completed successfully, retrieving final result.
                    return trainingOperationResponse.getFinalResult();
                } else {
                    System.out.println("polling completed unsuccessfully with status:"
                        + trainingOperationResponse.getStatus());
                    return Mono.empty();
                }
            }).block();

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
