// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormField;
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
import java.time.LocalDate;
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
                    System.out.printf("Field value: %s%n", fieldValue.getValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                });
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeCustomFormsFromUrl(String, String, RecognizeOptions)}
     */
    public void beginRecognizeCustomFormsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-recognizeOptions
        String analyzeFilePath = "{file_source_url}";
        String modelId = "{model_id}";
        boolean includeFieldElements = true;

        formRecognizerClient.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId,
            new RecognizeOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setIncludeFieldElements(includeFieldElements)
                .setPollInterval(Duration.ofSeconds(10)))
            .getFinalResult()
            .forEach(recognizedForm -> recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                System.out.printf("Field text: %s%n", fieldText);
                System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-recognizeOptions
    }

    /**
     * Code snippet for
     * {@link FormRecognizerClient#beginRecognizeCustomForms(InputStream, long, String)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-long-string
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        byte[] fileContent = Files.readAllBytes(form.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeCustomForms(targetStream, form.length(), modelId).getFinalResult()
            .forEach(recognizedForm -> recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                System.out.printf("Field text: %s%n", fieldText);
                System.out.printf("Field value: %s%n", fieldValue.getValue());
                System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-long-string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerClient#beginRecognizeCustomForms(InputStream, long, String, RecognizeOptions)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-long-string-recognizeOptions
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        boolean includeFieldElements = true;

        byte[] fileContent = Files.readAllBytes(form.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeCustomForms(targetStream, form.length(), modelId,
            new RecognizeOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setIncludeFieldElements(includeFieldElements)
                .setPollInterval(Duration.ofSeconds(10)))
            .getFinalResult()
            .forEach(recognizedForm -> recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                System.out.printf("Field text: %s%n", fieldText);
                System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-long-string-recognizeOptions
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
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContentFromUrl(String, RecognizeOptions)} with
     * options.
     */
    public void beginRecognizeContentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-recognizeOptions
        String formPath = "{file_source_url}";
        formRecognizerClient.beginRecognizeContentFromUrl(formPath,
            new RecognizeOptions()
                .setPollInterval(Duration.ofSeconds(5)))
            .getFinalResult()
            .forEach(recognizedForm -> {
                System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable ->
                    formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-recognizeOptions
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContent(InputStream, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContent() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long
        File form = new File("{local/file_path/fileName.pdf}");
        byte[] fileContent = Files.readAllBytes(form.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);
        formRecognizerClient.beginRecognizeContent(targetStream, form.length()).getFinalResult()
            .forEach(recognizedForm -> {
                System.out.printf("Page Angle: %f%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable -> formTable.getCells().forEach(recognizedTableCell ->
                    System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContent(InputStream, long, RecognizeOptions)} with
     * options.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-recognizeOptions
        File form = new File("{file_source_url}");
        byte[] fileContent = Files.readAllBytes(form.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeContent(targetStream, form.length(),
            new RecognizeOptions()
                .setPollInterval(Duration.ofSeconds(5)))
            .getFinalResult().forEach(recognizedForm -> {
                System.out.printf("Page Angle: %f%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable -> formTable.getCells().forEach(recognizedTableCell ->
                    System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-recognizeOptions
    }

    // Recognize Receipts

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceiptsFromUrl(String)}
     */
    @SuppressWarnings("unchecked")
    public void beginRecognizeReceiptsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string
        String receiptUrl = "{file_source_url}";
        formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl).getFinalResult()
            .forEach(recognizedReceipt -> {
                Map<String, FormField<?>> recognizedFields = recognizedReceipt.getFields();
                FormField<?> merchantNameField = recognizedFields.get("MerchantName");
                if (merchantNameField.getValue() instanceof String) {
                    String merchantName = (String) merchantNameField.getValue();
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantName, merchantNameField.getConfidence());
                }
                FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
                if (transactionDateField.getValue() instanceof LocalDate) {
                    LocalDate transactionDate = (LocalDate) transactionDateField.getValue();
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDate, transactionDateField.getConfidence());
                }
                FormField<?> receiptItemsField = recognizedFields.get("Items");
                System.out.printf("Receipt Items: %n");
                if (receiptItemsField.getValue() instanceof List) {
                    List<FormField<?>> receiptItems = (List<FormField<?>>) receiptItemsField.getValue();
                    receiptItems.forEach(receiptItem -> {
                        if (receiptItem.getValue() instanceof Map) {
                            ((Map<String, FormField<?>>) receiptItem.getValue()).forEach((key, formField) -> {
                                if ("Quantity".equals(key)) {
                                    if (formField.getValue() instanceof Integer) {
                                        Integer quantity = (Integer) formField.getValue();
                                        System.out.printf("Quantity: %s, confidence: %.2f%n",
                                            quantity, formField.getConfidence());
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
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceiptsFromUrl(String, RecognizeOptions)}
     */
    public void beginRecognizeReceiptsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-recognizeOptions
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
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-recognizeOptions
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceipts(InputStream, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    @SuppressWarnings("unchecked")
    public void beginRecognizeReceipts() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long
        File receipt = new File("{file_source_url}");
        byte[] fileContent = Files.readAllBytes(receipt.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeReceipts(targetStream, receipt.length())
            .getFinalResult().forEach(recognizedReceipt -> {
                Map<String, FormField<?>> recognizedFields = recognizedReceipt.getFields();
                FormField<?> merchantNameField = recognizedFields.get("MerchantName");
                if (merchantNameField.getValue() instanceof String) {
                    String merchantName = (String) merchantNameField.getValue();
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantName, merchantNameField.getConfidence());
                }
                FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
                if (transactionDateField.getValue() instanceof LocalDate) {
                    LocalDate transactionDate = (LocalDate) transactionDateField.getValue();
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDate, transactionDateField.getConfidence());
                }
                FormField<?> receiptItemsField = recognizedFields.get("Items");
                System.out.printf("Receipt Items: %n");
                if (receiptItemsField.getValue() instanceof List) {
                    List<FormField<?>> receiptItems = (List<FormField<?>>) receiptItemsField.getValue();
                    receiptItems.forEach(receiptItem -> {
                        if (receiptItem.getValue() instanceof Map) {
                            ((Map<String, FormField<?>>) receiptItem.getValue()).forEach((key, formField) -> {
                                if ("Quantity".equals(key)) {
                                    if (formField.getValue() instanceof Integer) {
                                        Integer quantity = (Integer) formField.getValue();
                                        System.out.printf("Quantity: %d, confidence: %.2f%n",
                                            quantity, formField.getConfidence());
                                    }
                                }
                            });
                        }
                    });
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceipts(InputStream, long, RecognizeOptions)}
     * with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    @SuppressWarnings("unchecked")
    public void beginRecognizeReceiptsWithOptions() throws IOException {

        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-recognizeOptions
        File receipt = new File("{local/file_path/fileName.jpg}");
        boolean includeFieldElements = true;
        byte[] fileContent = Files.readAllBytes(receipt.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);
        formRecognizerClient.beginRecognizeReceipts(targetStream, receipt.length(),
            new RecognizeOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setIncludeFieldElements(includeFieldElements)
                .setPollInterval(Duration.ofSeconds(5)))
            .getFinalResult().forEach(recognizedReceipt -> {
                Map<String, FormField<?>> recognizedFields = recognizedReceipt.getFields();
                FormField<?> merchantNameField = recognizedFields.get("MerchantName");
                if (merchantNameField.getValue() instanceof String) {
                    String merchantName = (String) merchantNameField.getValue();
                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                        merchantName, merchantNameField.getConfidence());
                }
                FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
                if (transactionDateField.getValue() instanceof LocalDate) {
                    LocalDate transactionDate = (LocalDate) transactionDateField.getValue();
                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                        transactionDate, transactionDateField.getConfidence());
                }
                FormField<?> receiptItemsField = recognizedFields.get("Items");
                System.out.printf("Receipt Items: %n");
                if (receiptItemsField.getValue() instanceof List) {
                    List<FormField<?>> receiptItems = (List<FormField<?>>) receiptItemsField.getValue();
                    receiptItems.forEach(receiptItem -> {
                        if (receiptItem.getValue() instanceof Map) {
                            ((Map<String, FormField<?>>) receiptItem.getValue()).forEach((key, formField) -> {
                                if ("Quantity".equals(key)) {
                                    if (formField.getValue() instanceof Integer) {
                                        Integer quantity = (Integer) formField.getValue();
                                        System.out.printf("Quantity: %d, confidence: %.2f%n",
                                            quantity, formField.getConfidence());
                                    }
                                }
                            });
                        }
                    });
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-recognizeOptions
    }
}
