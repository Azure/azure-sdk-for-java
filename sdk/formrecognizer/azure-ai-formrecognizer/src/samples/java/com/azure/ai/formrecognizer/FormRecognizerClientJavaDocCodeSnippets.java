// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizeOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Code snippet for {@link FormRecognizerClient}
 */
public class FormRecognizerClientJavaDocCodeSnippets {
    private FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link FormRecognizerClient}
     */
    public void createFormRecognizerClient() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.instantiation
        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.instantiation
    }

    /**
     * Code snippet for creating a {@link FormRecognizerClient} with pipeline
     */
    public void createFormRecognizerClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildClient();
        // END:  com.azure.ai.formrecognizer.FormRecognizerClient.pipeline.instantiation
    }


    // Recognize Custom Form

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeCustomFormsFromUrl(String, String)}
     */
    public void beginRecognizeCustomFormsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string
        String formUrl = "{form_url}";
        String modelId = "{custom_trained_model_id}";

        formRecognizerClient.beginRecognizeCustomFormsFromUrl(formUrl, modelId).getFinalResult()
            .forEach(recognizedForm -> {
                recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                });
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerClient#beginRecognizeCustomForms(InputStream, long, String, FormContentType)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-long-string-FormContentType
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        byte[] fileContent = Files.readAllBytes(form.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeCustomForms(targetStream, form.length(), modelId,
            FormContentType.IMAGE_JPEG).getFinalResult().forEach(recognizedForm ->
            recognizedForm.getFields().entrySet().forEach(entry -> {
                String fieldText = entry.getKey();
                FormField fieldValue = entry.getValue();
                System.out.printf("Field text: %s%n", fieldText);
                System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-long-string-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeCustomForms(RecognizeCustomFormsOptions)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#recognizeCustomFormsOptions
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        boolean includeTextContent = true;

        byte[] fileContent = Files.readAllBytes(form.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeCustomForms(new RecognizeCustomFormsOptions(targetStream,
            form.length(), modelId).setFormContentType(FormContentType.IMAGE_JPEG)
            .setIncludeFieldElement(includeTextContent).setPollInterval(Duration.ofSeconds(5))).getFinalResult()
            .forEach(recognizedForm -> recognizedForm.getFields().entrySet().forEach(entry -> {
                String fieldText = entry.getKey();
                FormField fieldValue = entry.getValue();
                System.out.printf("Field text: %s%n", fieldText);
                System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#recognizeCustomFormsOptions
    }

    // Recognize Content

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContentFromUrl(String)}
     */
    public void beginRecognizeContentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string
        String formUrl = "{form_url}";
        formRecognizerClient.beginRecognizeContentFromUrl(formUrl).getFinalResult().forEach(recognizedForm -> {
            System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
            System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
            // Table information
            System.out.println("Recognized Tables: ");
            recognizedForm.getTables().forEach(formTable -> formTable.getCells().forEach(recognizedTableCell ->
                System.out.printf("%s ", recognizedTableCell.getText())));
        });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContent(InputStream, long, FormContentType)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContent() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType
        File form = new File("{local/file_path/fileName.pdf}");
        byte[] fileContent = Files.readAllBytes(form.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        // Table information
        formRecognizerClient.beginRecognizeContent(targetStream, form.length(), FormContentType.APPLICATION_PDF)
            .getFinalResult().forEach(recognizedForm -> {
                System.out.printf("Page Angle: %f%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable -> formTable.getCells().forEach(recognizedTableCell ->
                    System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContent(RecognizeOptions)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#recognizeOptions
        File form = new File("{file_source_url}");
        byte[] fileContent = Files.readAllBytes(form.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeContent(
            new RecognizeOptions(targetStream, form.length())
                .setFormContentType(FormContentType.APPLICATION_PDF)
                .setPollInterval(Duration.ofSeconds(5)))
            .getFinalResult().forEach(recognizedForm -> {
                System.out.printf("Page Angle: %f%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable -> formTable.getCells().forEach(recognizedTableCell ->
                    System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#recognizeOptions
    }

    // Recognize Receipts

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceiptsFromUrl(String)}
     */
    public void beginRecognizeReceiptsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string
        String receiptUrl = "{file_source_url}";
        formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl).getFinalResult()
            .forEach(recognizedReceipt -> {
                Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
                FormField merchantNameField = recognizedFields.get("MerchantName");
                if (merchantNameField.getFieldValue().getType() == FieldValueType.STRING) {
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantNameField.getFieldValue().asString(),
                        merchantNameField.getConfidence());
                }
                FormField transactionDateField = recognizedFields.get("TransactionDate");
                if (transactionDateField.getFieldValue().getType() == FieldValueType.DATE) {
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDateField.getFieldValue().asDate(),
                        transactionDateField.getConfidence());
                }
                FormField receiptItemsField = recognizedFields.get("Items");
                System.out.printf("Receipt Items: %n");
                if (receiptItemsField.getFieldValue().getType() == FieldValueType.LIST) {
                    List<FormField> receiptItems = receiptItemsField.getFieldValue().asList();
                    receiptItems.forEach(receiptItem -> {
                        if (receiptItem.getFieldValue().getType() == FieldValueType.MAP) {
                            receiptItem.getFieldValue().asMap().forEach((key, formField) -> {
                                if (key.equals("Quantity")) {
                                    if (formField.getFieldValue().getType() == FieldValueType.INTEGER) {
                                        System.out.printf("Quantity: %s, confidence: %.2f%n",
                                            formField.getFieldValue().asInteger(), formField.getConfidence());
                                    }
                                }
                            });
                        }
                    });
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceipts(InputStream, long, FormContentType)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceipts() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType
        File receipt = new File("{file_source_url}");
        byte[] fileContent = Files.readAllBytes(receipt.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);
        formRecognizerClient.beginRecognizeReceipts(targetStream, receipt.length(), FormContentType.IMAGE_JPEG)
            .getFinalResult().forEach(recognizedReceipt -> {
                Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
                FormField merchantNameField = recognizedFields.get("MerchantName");
                if (merchantNameField.getFieldValue().getType() == FieldValueType.STRING) {
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantNameField.getFieldValue().asString(),
                        merchantNameField.getConfidence());
                }
                FormField transactionDateField = recognizedFields.get("TransactionDate");
                if (transactionDateField.getFieldValue().getType() == FieldValueType.DATE) {
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDateField.getFieldValue().asDate(),
                        transactionDateField.getConfidence());
                }
                FormField receiptItemsField = recognizedFields.get("Items");
                System.out.printf("Receipt Items: %n");
                if (receiptItemsField.getFieldValue().getType() == FieldValueType.LIST) {
                    List<FormField> receiptItems = receiptItemsField.getFieldValue().asList();
                    receiptItems.forEach(receiptItem -> {
                        if (receiptItem.getFieldValue().getType() == FieldValueType.MAP) {
                            receiptItem.getFieldValue().asMap().forEach((key, formField) -> {
                                if (key.equals("Quantity")) {
                                    if (formField.getFieldValue().getType() == FieldValueType.INTEGER) {
                                        System.out.printf("Quantity: %d, confidence: %.2f%n",
                                            formField.getFieldValue().asInteger(), formField.getConfidence());
                                    }
                                }
                            });
                        }
                    });
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceipts(RecognizeOptions)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceiptsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#recognizeOptions
        File receipt = new File("{local/file_path/fileName.jpg}");
        boolean includeTextContent = true;
        byte[] fileContent = Files.readAllBytes(receipt.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);
        formRecognizerClient.beginRecognizeReceipts(new RecognizeOptions(targetStream, receipt.length())
            .setFormContentType(FormContentType.IMAGE_JPEG)
            .setIncludeFieldElement(includeTextContent)
            .setPollInterval(Duration.ofSeconds(5))).getFinalResult()
            .forEach(recognizedReceipt -> {
                Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
                FormField merchantNameField = recognizedFields.get("MerchantName");
                if (merchantNameField.getFieldValue().getType() == FieldValueType.STRING) {
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantNameField.getFieldValue().asString(),
                        merchantNameField.getConfidence());
                }
                FormField transactionDateField = recognizedFields.get("TransactionDate");
                if (transactionDateField.getFieldValue().getType() == FieldValueType.DATE) {
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDateField.getFieldValue().asDate(),
                        transactionDateField.getConfidence());
                }
                FormField receiptItemsField = recognizedFields.get("Items");
                System.out.printf("Receipt Items: %n");
                if (receiptItemsField.getFieldValue().getType() == FieldValueType.LIST) {
                    List<FormField> receiptItems = receiptItemsField.getFieldValue().asList();
                    receiptItems.forEach(receiptItem -> {
                        if (receiptItem.getFieldValue().getType() == FieldValueType.MAP) {
                            receiptItem.getFieldValue().asMap().forEach((key, formField) -> {
                                if (key.equals("Quantity")) {
                                    if (formField.getFieldValue().getType() == FieldValueType.INTEGER) {
                                        System.out.printf("Quantity: %d, confidence: %.2f%n",
                                            formField.getFieldValue().asInteger(), formField.getConfidence());
                                    }
                                }
                            });
                        }
                    });
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#recognizeOptions
    }
}
