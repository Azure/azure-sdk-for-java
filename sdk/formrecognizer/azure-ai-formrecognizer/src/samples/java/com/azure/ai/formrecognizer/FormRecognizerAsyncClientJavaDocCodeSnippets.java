// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.RecognizeOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.polling.AsyncPollResponse;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
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

        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl(formUrl, modelId)
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(recognizedForm -> recognizedForm.getFields()
                .forEach((fieldText, fieldValue) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeCustomFormsFromUrl(String, String, RecognizeOptions)} with options
     */
    public void beginRecognizeCustomFormsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-recognizeOptions
        String formUrl = "{formUrl}";
        String modelId = "{model_id}";
        boolean includeTextContent = true;

        formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl(formUrl, modelId,
            new RecognizeOptions()
                .setIncludeFieldElements(includeTextContent)
                .setPollInterval(Duration.ofSeconds(10)))
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(recognizedForm -> recognizedForm.getFields()
                .forEach((fieldText, fieldValue) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-recognizeOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomForms}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-long-string
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeCustomForms(buffer, form.length(), modelId)
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(recognizedForm -> recognizedForm.getFields()
                .forEach((fieldText, fieldValue) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-long-string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeCustomForms(Flux, long, String, RecognizeOptions)}
     * with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-long-string-recognizeOptions
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeCustomForms(buffer, form.length(), modelId,
            new RecognizeOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setIncludeFieldElements(includeFieldElements)
                .setPollInterval(Duration.ofSeconds(5)))
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(recognizedForm -> recognizedForm.getFields()
                .forEach((fieldText, fieldValue) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-long-string-recognizeOptions
    }

    // Recognize Content

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContentFromUrl(String)}
     */
    public void beginRecognizeContentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string
        String formUrl = "{formUrl}";
        formRecognizerAsyncClient.beginRecognizeContentFromUrl(formUrl)
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(formPage -> {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables().forEach(formTable ->
                    formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContentFromUrl(String, RecognizeOptions)} with
     * options
     */
    public void beginRecognizeContentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-recognizeOptions
        String formUrl = "{formUrl}";
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeContentFromUrl(formUrl,
            new RecognizeOptions().setPollInterval(Duration.ofSeconds(5)))
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(formPage -> {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables().forEach(formTable ->
                    formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-recognizeOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContent() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long
        File form = new File("{local/file_path/fileName.jpg}");
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeContent(buffer, form.length())
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(formPage -> {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables().forEach(formTable ->
                    formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent(Flux, long, RecognizeOptions)} with
     * options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-recognizeOptions
        File form = new File("{local/file_path/fileName.jpg}");
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeContent(buffer, form.length(),
            new RecognizeOptions()
                .setContentType(FormContentType.APPLICATION_PDF)
                .setPollInterval(Duration.ofSeconds(5)))
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(formPage -> {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables().forEach(formTable -> formTable.getCells().forEach(recognizedTableCell ->
                    System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-recognizeOptions
    }

    // Recognize Receipts

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceiptsFromUrl(String)}
     */
    public void beginRecognizeReceiptsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string
        String receiptUrl = "{receiptUrl}";
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptUrl)
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedReceipts -> {
                for (int i = 0; i < recognizedReceipts.size(); i++) {
                    RecognizedForm recognizedForm = recognizedReceipts.get(i);
                    Map<String, FormField<?>> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
                    FormField<?> merchantNameField = recognizedFields.get("MerchantName");
                    if (merchantNameField != null) {
                        if (FieldValueType.STRING == merchantNameField.getValueType()) {
                            String merchantName = FieldValueType.STRING.cast(merchantNameField);
                            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                                merchantName, merchantNameField.getConfidence());
                        }
                    }

                    FormField<?> merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
                    if (merchantPhoneNumberField != null) {
                        if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValueType()) {
                            String merchantAddress = FieldValueType.PHONE_NUMBER.cast(merchantPhoneNumberField);
                            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                                merchantAddress, merchantPhoneNumberField.getConfidence());
                        }
                    }

                    FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
                    if (transactionDateField != null) {
                        if (FieldValueType.DATE == transactionDateField.getValueType()) {
                            LocalDate transactionDate = FieldValueType.DATE.cast(transactionDateField);
                            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                                transactionDate, transactionDateField.getConfidence());
                        }
                    }

                    FormField<?> receiptItemsField = recognizedFields.get("Items");
                    if (receiptItemsField != null) {
                        System.out.printf("Receipt Items: %n");
                        if (FieldValueType.LIST == receiptItemsField.getValueType()) {
                            List<FormField<?>> receiptItems = FieldValueType.LIST.cast(receiptItemsField);
                            receiptItems.stream()
                                .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValueType())
                                .<Map<String, FormField<?>>>map(FieldValueType.MAP::cast)
                                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                    if ("Quantity".equals(key)) {
                                        if (FieldValueType.DOUBLE == formField.getValueType()) {
                                            Float quantity = FieldValueType.DOUBLE.cast(formField);
                                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                                quantity, formField.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceiptsFromUrl(String, RecognizeOptions)}
     */
    public void beginRecognizeReceiptsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-recognizeOptions
        String receiptUrl = "{receiptUrl}";
        boolean includeTextContent = true;
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptUrl,
            new RecognizeOptions()
                .setIncludeFieldElements(includeTextContent)
                .setPollInterval(Duration.ofSeconds(5)))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedReceipts -> {
                for (int i = 0; i < recognizedReceipts.size(); i++) {
                    RecognizedForm recognizedReceipt = recognizedReceipts.get(i);
                    Map<String, FormField<?>> recognizedFields = recognizedReceipt.getFields();
                    System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
                    FormField<?> merchantNameField = recognizedFields.get("MerchantName");
                    if (merchantNameField != null) {
                        if (FieldValueType.STRING == merchantNameField.getValueType()) {
                            String merchantName = FieldValueType.STRING.cast(merchantNameField);
                            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                                merchantName, merchantNameField.getConfidence());
                        }
                    }

                    FormField<?> merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
                    if (merchantPhoneNumberField != null) {
                        if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValueType()) {
                            String merchantAddress = FieldValueType.PHONE_NUMBER.cast(merchantPhoneNumberField);
                            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                                merchantAddress, merchantPhoneNumberField.getConfidence());
                        }
                    }

                    FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
                    if (transactionDateField != null) {
                        if (FieldValueType.DATE == transactionDateField.getValueType()) {
                            LocalDate transactionDate = FieldValueType.DATE.cast(transactionDateField);
                            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                                transactionDate, transactionDateField.getConfidence());
                        }
                    }

                    FormField<?> receiptItemsField = recognizedFields.get("Items");
                    if (receiptItemsField != null) {
                        System.out.printf("Receipt Items: %n");
                        if (FieldValueType.LIST == receiptItemsField.getValueType()) {
                            List<FormField<?>> receiptItems = FieldValueType.LIST.cast(receiptItemsField);
                            receiptItems.stream()
                                .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValueType())
                                .<Map<String, FormField<?>>>map(FieldValueType.MAP::cast)
                                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                    if ("Quantity".equals(key)) {
                                        if (FieldValueType.DOUBLE == formField.getValueType()) {
                                            Float quantity = FieldValueType.DOUBLE.cast(formField);
                                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                                quantity, formField.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-recognizeOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts(Flux, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceipts() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long
        File receipt = new File("{file_source_url}");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(receipt.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeReceipts(buffer, receipt.length())
            .flatMap(AsyncPollResponse::getFinalResult)
                    .subscribe(recognizedReceipts -> {
                        for (int i = 0; i < recognizedReceipts.size(); i++) {
                            RecognizedForm recognizedForm = recognizedReceipts.get(i);
                            Map<String, FormField<?>> recognizedFields = recognizedForm.getFields();
                            System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
                            FormField<?> merchantNameField = recognizedFields.get("MerchantName");
                            if (merchantNameField != null) {
                                if (FieldValueType.STRING == merchantNameField.getValueType()) {
                                    String merchantName = FieldValueType.STRING.cast(merchantNameField);
                                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                                        merchantName, merchantNameField.getConfidence());
                                }
                            }

                            FormField<?> merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
                            if (merchantPhoneNumberField != null) {
                                if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValueType()) {
                                    String merchantAddress = FieldValueType.PHONE_NUMBER.cast(merchantPhoneNumberField);
                                    System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                                        merchantAddress, merchantPhoneNumberField.getConfidence());
                                }
                            }

                            FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
                            if (transactionDateField != null) {
                                if (FieldValueType.DATE == transactionDateField.getValueType()) {
                                    LocalDate transactionDate = FieldValueType.DATE.cast(transactionDateField);
                                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                                        transactionDate, transactionDateField.getConfidence());
                                }
                            }

                            FormField<?> receiptItemsField = recognizedFields.get("Items");
                            if (receiptItemsField != null) {
                                System.out.printf("Receipt Items: %n");
                                if (FieldValueType.LIST == receiptItemsField.getValueType()) {
                                    List<FormField<?>> receiptItems = FieldValueType.LIST.cast(receiptItemsField);
                                    receiptItems.stream()
                                        .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValueType())
                                        .<Map<String, FormField<?>>>map(FieldValueType.MAP::cast)
                                        .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                            if ("Quantity".equals(key)) {
                                                if (FieldValueType.DOUBLE == formField.getValueType()) {
                                                    Float quantity = FieldValueType.DOUBLE.cast(formField);
                                                    System.out.printf("Quantity: %f, confidence: %.2f%n",
                                                        quantity, formField.getConfidence());
                                                }
                                            }
                                        }));
                                }
                            }
                        }
                    });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts(Flux, long, RecognizeOptions)} with
     * options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceiptsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-recognizeOptions
        File receipt = new File("{local/file_path/fileName.jpg}");
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(receipt.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeReceipts(buffer, receipt.length(),
            new RecognizeOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setIncludeFieldElements(includeFieldElements)
                .setPollInterval(Duration.ofSeconds(5)))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedReceipts -> {
                for (int i = 0; i < recognizedReceipts.size(); i++) {
                    RecognizedForm recognizedForm = recognizedReceipts.get(i);
                    Map<String, FormField<?>> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
                    FormField<?> merchantNameField = recognizedFields.get("MerchantName");
                    if (merchantNameField != null) {
                        if (FieldValueType.STRING == merchantNameField.getValueType()) {
                            String merchantName = FieldValueType.STRING.cast(merchantNameField);
                            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                                merchantName, merchantNameField.getConfidence());
                        }
                    }

                    FormField<?> merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
                    if (merchantPhoneNumberField != null) {
                        if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValueType()) {
                            String merchantAddress = FieldValueType.PHONE_NUMBER.cast(merchantPhoneNumberField);
                            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                                merchantAddress, merchantPhoneNumberField.getConfidence());
                        }
                    }

                    FormField<?> transactionDateField = recognizedFields.get("TransactionDate");
                    if (transactionDateField != null) {
                        if (FieldValueType.DATE == transactionDateField.getValueType()) {
                            LocalDate transactionDate = FieldValueType.DATE.cast(transactionDateField);
                            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                                transactionDate, transactionDateField.getConfidence());
                        }
                    }

                    FormField<?> receiptItemsField = recognizedFields.get("Items");
                    if (receiptItemsField != null) {
                        System.out.printf("Receipt Items: %n");
                        if (FieldValueType.LIST == receiptItemsField.getValueType()) {
                            List<FormField<?>> receiptItems = FieldValueType.LIST.cast(receiptItemsField);
                            receiptItems.stream()
                                .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValueType())
                                .<Map<String, FormField<?>>>map(FieldValueType.MAP::cast)
                                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                    if ("Quantity".equals(key)) {
                                        if (FieldValueType.DOUBLE == formField.getValueType()) {
                                            Float quantity = FieldValueType.DOUBLE.cast(formField);
                                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                                quantity, formField.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                }));

        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-recognizeOptions
    }
}
