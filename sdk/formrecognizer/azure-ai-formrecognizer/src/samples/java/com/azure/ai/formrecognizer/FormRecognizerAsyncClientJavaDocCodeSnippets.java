// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizeOptions;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.azure.ai.formrecognizer.implementation.Utility.toFluxByteBuffer;

/**
 * Code snippet for {@link FormRecognizerAsyncClient}
 */
public class FormRecognizerAsyncClientJavaDocCodeSnippets {
    FormRecognizerAsyncClient formRecognizerAsyncClient = new FormRecognizerClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link FormRecognizerAsyncClient}
     */
    public void createFormRecognizerAsyncClient() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.instantiation
        FormRecognizerAsyncClient formRecognizerAsyncClient = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.instantiation
    }

    /**
     * Code snippet for creating a {@link FormRecognizerAsyncClient} with pipeline
     */
    public void createFormRecognizerAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        FormRecognizerAsyncClient formRecognizerAsyncClient = new FormRecognizerClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildAsyncClient();
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.pipeline.instantiation
    }

    // Recognize Custom Form

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomFormsFromUrl(String, String)}
     */
    public void beginRecognizeCustomFormsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string
        String formUrl = "{form_url}";
        String modelId = "{custom_trained_model_id}";

        formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl(formUrl, modelId).subscribe(
            recognizePollingOperation ->
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedForms -> {
                    recognizedForms.forEach(recognizedForm -> recognizedForm.getFields().forEach((fieldText,
                        fieldValue) -> {
                        System.out.printf("Field text: %s%n", fieldText);
                        System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                        System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                    }));
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomForms}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-long-string-FormContentType
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        formRecognizerAsyncClient.beginRecognizeCustomForms(buffer, form.length(), modelId,
            FormContentType.IMAGE_JPEG).subscribe(recognizePollingOperation ->
            // if training polling operation completed, retrieve the final result.
            recognizePollingOperation.getFinalResult().subscribe(recognizedForms -> {
                recognizedForms.forEach(recognizedForm -> recognizedForm.getFields().forEach((fieldText,
                    fieldValue) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                }));
            }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-long-string-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomFormsFromUrl(RecognizeCustomFormsOptions)}
     * with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#recognizeCustomFormsOptions
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        boolean includeTextContent = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        formRecognizerAsyncClient.beginRecognizeCustomForms(
            new RecognizeCustomFormsOptions(buffer, form.length(), modelId)
                .setFormContentType(FormContentType.IMAGE_JPEG)
                .setIncludeTextContent(includeTextContent)
                .setPollInterval(Duration.ofSeconds(5)))
            .subscribe(recognizePollingOperation ->
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedForms -> {
                    recognizedForms.forEach(recognizedForm -> recognizedForm.getFields().forEach((fieldText,
                        fieldValue) -> {
                        System.out.printf("Field text: %s%n", fieldText);
                        System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                        System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                    }));
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#recognizeCustomFormsOptions
    }

    // Recognize Content

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContentFromUrl(String)}
     */
    public void beginRecognizeContentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string
        String formUrl = "{form_url}";
        formRecognizerAsyncClient.beginRecognizeContentFromUrl(formUrl).subscribe(
            recognizePollingOperation -> recognizePollingOperation.getFinalResult().subscribe(contentPageResult -> {
                // Table information
                contentPageResult.forEach(recognizedForm -> {
                    System.out.printf("Text angle: %s%n", recognizedForm.getTextAngle());
                    System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                    System.out.println("Recognized Tables: ");
                    recognizedForm.getTables().forEach(formTable ->
                        formTable.getCells().forEach(recognizedTableCell ->
                            System.out.printf("%s ", recognizedTableCell.getText())));
                });
            }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContent() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-FormContentType
        File form = new File("{local/file_path/fileName.jpg}");
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        formRecognizerAsyncClient.beginRecognizeContent(buffer, form.length(), FormContentType.APPLICATION_PDF)
            .subscribe(recognizePollingOperation ->
                recognizePollingOperation.getFinalResult().subscribe(contentPageResult ->
                    contentPageResult.forEach(recognizedForm -> {
                        System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                        System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                        // Table information
                        System.out.println("Recognized Tables: ");
                        recognizedForm.getTables().forEach(formTable ->
                            formTable.getCells().forEach(recognizedTableCell ->
                                System.out.printf("%s ", recognizedTableCell.getText())));
                    })
                ));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent(RecognizeOptions)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#recognizeOptions
        File form = new File("{local/file_path/fileName.jpg}");
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        formRecognizerAsyncClient.beginRecognizeContent(new RecognizeOptions(buffer, form.length())
            .setFormContentType(FormContentType.APPLICATION_PDF).setPollInterval(Duration.ofSeconds(5)))
            .subscribe(recognizePollingOperation -> recognizePollingOperation.getFinalResult().subscribe(
                layoutPageResults -> layoutPageResults.forEach(recognizedForm -> {
                    System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                    System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                    // Table information
                    System.out.println("Recognized Tables: ");
                    recognizedForm.getTables().forEach(formTable -> formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
                })
            ));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#recognizeOptions
    }

    // Recognize Receipts

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceiptsFromUrl(String)}
     */
    public void beginRecognizeReceiptsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string
        String receiptUrl = "{receipt_url}";
        formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptUrl).subscribe(recognizePollingOperation -> {
            // if training polling operation completed, retrieve the final result.
            recognizePollingOperation.getFinalResult().subscribe(recognizedReceipts -> {
                for (int i = 0; i < recognizedReceipts.size(); i++) {
                    RecognizedReceipt recognizedReceipt = recognizedReceipts.get(i);
                    Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
                    System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
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
                }
            });
        });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts(Flux, long, FormContentType)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceipts() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType
        File receipt = new File("{local/file_path/fileName.jpg}");
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(receipt.toPath())));
        formRecognizerAsyncClient.beginRecognizeReceipts(buffer, receipt.length(), FormContentType.IMAGE_JPEG)
            .subscribe(recognizePollingOperation -> {
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedReceipts -> {
                    for (int i = 0; i < recognizedReceipts.size(); i++) {
                        RecognizedReceipt recognizedReceipt = recognizedReceipts.get(i);
                        Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
                        System.out.printf("----------- Recognized Receipt page %s -----------%n", i);
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
                    }
                });
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts(RecognizeOptions)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceiptsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#recognizeOptions
        File receipt = new File("{local/file_path/fileName.jpg}");
        boolean includeTextContent = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(receipt.toPath())));
        formRecognizerAsyncClient.beginRecognizeReceipts(new RecognizeOptions(buffer, receipt.length())
            .setFormContentType(FormContentType.IMAGE_JPEG).setIncludeTextContent(includeTextContent)
            .setPollInterval(Duration.ofSeconds(5))).subscribe(recognizePollingOperation -> {
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedReceipts -> {
                    for (int i = 0; i < recognizedReceipts.size(); i++) {
                        RecognizedReceipt recognizedReceipt = recognizedReceipts.get(i);
                        Map<String, FormField> recognizedFields = recognizedReceipt.getRecognizedForm().getFields();
                        System.out.printf("----------- Recognized Receipt page %s -----------%n", i);
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
                    }
                });
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#recognizeOptions
    }
}
