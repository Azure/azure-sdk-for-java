package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.util.List;

/**
 * Sample demonstrating converting recognized form fields to strongly typed US receipt field values.
 * See
 * <a href="https://westus2.dev.cognitive.microsoft.com/docs/services/form-recognizer-api-v2-preview/operations/GetAnalyzeReceiptResult"></a>
 * for information on the strongly typed fields returned by service for recognizing receipts.
 */
public class StronglyTypedRecognizedFormUSReceipt {

    /**
     * Main method to invoke this demo.
     *
     * @param args Unused. Arguments to the program.
     */
    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {
        // Instantiate a client that will be used to call the service.
        FormRecognizerClient client = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{api_Key}"))
            .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
            .buildClient();

        String receiptUrl = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/sdk/formrecognizer"
            + "/azure-ai-formrecognizer/src/samples/java/sample-forms/receipts/contoso-allinone.jpg";
        SyncPoller<OperationResult, List<RecognizedForm>> recognizeReceiptPoller =
            client.beginRecognizeReceiptsFromUrl(receiptUrl);

        List<RecognizedForm> receiptPageResults = recognizeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptPageResults.size(); i++) {
            final RecognizedForm recognizedReceipt = receiptPageResults.get(i);
            System.out.printf("----------- Recognized Receipt page %s -----------%n", i);
            // Use extension method to convert the recognized form to strongly typed US receipt fields
            USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
            System.out.printf("Merchant Name: %s, confidence: %.2f%n", usReceipt.getMerchantName().getFieldValue(),
                usReceipt.getMerchantName().getConfidence());
            System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                usReceipt.getMerchantAddress().getFieldValue(),
                usReceipt.getMerchantAddress().getConfidence());
            System.out.printf("Merchant Phone Number %s, confidence: %.2f%n",
                usReceipt.getMerchantPhoneNumber().getFieldValue(), usReceipt.getMerchantPhoneNumber().getConfidence());
            System.out.printf("Total: %s confidence: %.2f%n", usReceipt.getTotal().getFieldValue(),
                usReceipt.getTotal().getConfidence());
            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                usReceipt.getTransactionDate().getFieldValue(), usReceipt.getTransactionDate().getConfidence());
            System.out.printf("Transaction Time: %s, confidence: %.2f%n",
                usReceipt.getTransactionTime().getFieldValue(), usReceipt.getTransactionTime().getConfidence());
            System.out.printf("Receipt Items: %n");
            usReceipt.getReceiptItems().forEach(receiptItem -> {
                if (receiptItem.getName() != null) {
                    System.out.printf("Name: %s, confidence: %.2f%n", receiptItem.getName().getFieldValue(),
                        receiptItem.getName().getConfidence());
                }
                if (receiptItem.getQuantity() != null) {
                    System.out.printf("Quantity: %s, confidence: %.2f%n", receiptItem.getQuantity().getFieldValue(),
                        receiptItem.getQuantity().getConfidence());
                }
                if (receiptItem.getPrice() != null) {
                    System.out.printf("Price: %s, confidence: %.2f%n", receiptItem.getPrice().getFieldValue(),
                        receiptItem.getPrice().getConfidence());
                }
                if (receiptItem.getTotalPrice() != null) {
                    System.out.printf("Total Price: %s, confidence: %.2f%n",
                        receiptItem.getTotalPrice().getFieldValue(), receiptItem.getTotalPrice().getConfidence());
                }
            });
            System.out.print("-----------------------------------");
        }
    }
}
