// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.aot.graalvm.samples.formrecognizer;

import com.azure.ai.formrecognizer.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A sample to demonstrate Form Recognizer's functionality to recognize receipts using GraalVM.
 */
public class FormRecognizerSample {
    private static final String AZURE_FORM_RECOGNIZER_ENDPOINT = System.getenv("AZURE_FORM_RECOGNIZER_ENDPOINT");
    private static final String AZURE_FORM_RECOGNIZER_KEY = System.getenv("AZURE_FORM_RECOGNIZER_KEY");

    /**
     * The method to run the formrecognizer sample.
     * @throws IOException if the input image cannot be read.
     */
    public static void runSample() throws IOException {
        System.out.println("\n================================================================");
        System.out.println(" Starting Form Recognizer Sample");
        System.out.println("================================================================");

        // Instantiate a client that will be used to call the service.
        DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
                .credential(new AzureKeyCredential(AZURE_FORM_RECOGNIZER_KEY))
                .endpoint(AZURE_FORM_RECOGNIZER_ENDPOINT)
                .buildClient();

        InputStream resourceAsStream = FormRecognizerSample.class.getClassLoader().getResourceAsStream("contoso-allinone.jpg");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = resourceAsStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        byte[] fileContent = buffer.toByteArray();

        InputStream targetStream = new ByteArrayInputStream(fileContent);

        SyncPoller<DocumentOperationResult, AnalyzeResult> analyzeReceiptPoller =
                client.beginAnalyzeDocument("prebuilt-receipt", targetStream, fileContent.length);

        AnalyzeResult receiptResults = analyzeReceiptPoller.getFinalResult();

        for (int i = 0; i < receiptResults.getDocuments().size(); i++) {
            AnalyzedDocument analyzedReceipt = receiptResults.getDocuments().get(i);
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
                if (DocumentFieldType.LIST == receiptItemsField.getType()) {
                    List<DocumentField> receiptItems = receiptItemsField.getValueList();
                    receiptItems.stream()
                            .filter(receiptItem -> DocumentFieldType.MAP == receiptItem.getType())
                            .map(formField -> formField.getValueMap())
                            .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                if ("Name".equals(key)) {
                                    if (DocumentFieldType.STRING == formField.getType()) {
                                        String name = formField.getValueString();
                                        System.out.printf("Name: %s, confidence: %.2fs%n",
                                                name, formField.getConfidence());
                                    }
                                }
                                if ("Quantity".equals(key)) {
                                    if (DocumentFieldType.FLOAT == formField.getType()) {
                                        Float quantity = formField.getValueFloat();
                                        System.out.printf("Quantity: %f, confidence: %.2f%n",
                                                quantity, formField.getConfidence());
                                    }
                                }
                                if ("Price".equals(key)) {
                                    if (DocumentFieldType.FLOAT == formField.getType()) {
                                        Float price = formField.getValueFloat();
                                        System.out.printf("Price: %f, confidence: %.2f%n",
                                                price, formField.getConfidence());
                                    }
                                }
                                if ("TotalPrice".equals(key)) {
                                    if (DocumentFieldType.FLOAT == formField.getType()) {
                                        Float totalPrice = formField.getValueFloat();
                                        System.out.printf("Total Price: %f, confidence: %.2f%n",
                                                totalPrice, formField.getConfidence());
                                    }
                                }
                            }));
                }
            }
        }

        System.out.println("\n================================================================");
        System.out.println(" Form Recognizer Sample Complete");
        System.out.println("================================================================");
    }
}
