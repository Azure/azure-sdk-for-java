// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormRecognizerLocale;
import com.azure.ai.formrecognizer.models.RecognizeBusinessCardsOptions;
import com.azure.ai.formrecognizer.models.RecognizeContentOptions;
import com.azure.ai.formrecognizer.models.RecognizeIdentityDocumentOptions;
import com.azure.ai.formrecognizer.models.RecognizeInvoicesOptions;
import com.azure.ai.formrecognizer.models.RecognizeReceiptsOptions;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
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
    private final FormRecognizerAsyncClient formRecognizerAsyncClient
        = new FormRecognizerClientBuilder().buildAsyncClient();

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
        formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl(modelId, formUrl)
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(recognizedForm -> recognizedForm.getFields()
                .forEach((fieldText, formField) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value data text: %s%n", formField.getValueData().getText());
                    System.out.printf("Confidence score: %.2f%n", formField.getConfidence());
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeCustomFormsFromUrl(String, String, RecognizeCustomFormsOptions)} with options
     */
    public void beginRecognizeCustomFormsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-RecognizeCustomFormsOptions
        String formUrl = "{formUrl}";
        String modelId = "{model_id}";
        boolean includeFieldElements = true;

        formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl(modelId, formUrl,
            new RecognizeCustomFormsOptions()
                .setFieldElementsIncluded(includeFieldElements)
                .setPollInterval(Duration.ofSeconds(10)))
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(recognizedForm -> recognizedForm.getFields()
                .forEach((fieldText, formField) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value data text: %s%n", formField.getValueData().getText());
                    System.out.printf("Confidence score: %.2f%n", formField.getConfidence());
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-RecognizeCustomFormsOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomForms}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeCustomForms(modelId, buffer, form.length())
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(recognizedForm -> recognizedForm.getFields()
                .forEach((fieldText, formField) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value data text: %s%n", formField.getValueData().getText());
                    System.out.printf("Confidence score: %.2f%n", formField.getConfidence());
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeCustomForms(String, Flux, long, RecognizeCustomFormsOptions)}
     * with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long-RecognizeCustomFormsOptions
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeCustomForms(modelId, buffer, form.length(),
            new RecognizeCustomFormsOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setFieldElementsIncluded(includeFieldElements)
                .setPollInterval(Duration.ofSeconds(5)))
            // if training polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .flatMap(Flux::fromIterable)
            .subscribe(recognizedForm -> recognizedForm.getFields()
                .forEach((fieldName, formField) -> {
                    System.out.printf("Field text: %s%n", fieldName);
                    System.out.printf("Field value data text: %s%n", formField.getValueData().getText());
                    System.out.printf("Confidence score: %.2f%n", formField.getConfidence());
                }));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#string-Flux-long-RecognizeCustomFormsOptions
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
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContentFromUrl(String, RecognizeContentOptions)} with
     * options
     */
    public void beginRecognizeContentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-RecognizeContentOptions
        String formUrl = "{formUrl}";
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeContentFromUrl(formUrl,
            new RecognizeContentOptions().setPollInterval(Duration.ofSeconds(5)))
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
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-RecognizeContentOptions
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
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent(Flux, long, RecognizeContentOptions)} with
     * options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-RecognizeContentOptions
        File form = new File("{local/file_path/fileName.jpg}");
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeContent(buffer, form.length(),
            new RecognizeContentOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
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
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-RecognizeContentOptions
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
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
                    FormField merchantNameField = recognizedFields.get("MerchantName");
                    if (merchantNameField != null) {
                        if (FieldValueType.STRING == merchantNameField.getValue().getValueType()) {
                            String merchantName = merchantNameField.getValue().asString();
                            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                                merchantName, merchantNameField.getConfidence());
                        }
                    }

                    FormField merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
                    if (merchantPhoneNumberField != null) {
                        if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue().getValueType()) {
                            String merchantAddress = merchantPhoneNumberField.getValue().asPhoneNumber();
                            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                                merchantAddress, merchantPhoneNumberField.getConfidence());
                        }
                    }

                    FormField transactionDateField = recognizedFields.get("TransactionDate");
                    if (transactionDateField != null) {
                        if (FieldValueType.DATE == transactionDateField.getValue().getValueType()) {
                            LocalDate transactionDate = transactionDateField.getValue().asDate();
                            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                                transactionDate, transactionDateField.getConfidence());
                        }
                    }

                    FormField receiptItemsField = recognizedFields.get("Items");
                    if (receiptItemsField != null) {
                        System.out.printf("Receipt Items: %n");
                        if (FieldValueType.LIST == receiptItemsField.getValue().getValueType()) {
                            List<FormField> receiptItems = receiptItemsField.getValue().asList();
                            receiptItems.stream()
                                .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValue().getValueType())
                                .map(formField -> formField.getValue().asMap())
                                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                    if ("Quantity".equals(key)) {
                                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                                            Float quantity = formField.getValue().asFloat();
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
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceiptsFromUrl(String, RecognizeReceiptsOptions)}
     */
    public void beginRecognizeReceiptsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-RecognizeReceiptsOptions
        String receiptUrl = "{receiptUrl}";
        boolean includeFieldElements = true;
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptUrl,
            new RecognizeReceiptsOptions()
                .setFieldElementsIncluded(includeFieldElements)
                .setLocale(FormRecognizerLocale.EN_US)
                .setPollInterval(Duration.ofSeconds(5)))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedReceipts -> {
                for (int i = 0; i < recognizedReceipts.size(); i++) {
                    RecognizedForm recognizedReceipt = recognizedReceipts.get(i);
                    Map<String, FormField> recognizedFields = recognizedReceipt.getFields();
                    System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
                    FormField merchantNameField = recognizedFields.get("MerchantName");
                    if (merchantNameField != null) {
                        if (FieldValueType.STRING == merchantNameField.getValue().getValueType()) {
                            String merchantName = merchantNameField.getValue().asString();
                            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                                merchantName, merchantNameField.getConfidence());
                        }
                    }

                    FormField merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
                    if (merchantPhoneNumberField != null) {
                        if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue().getValueType()) {
                            String merchantAddress = merchantPhoneNumberField.getValue().asPhoneNumber();
                            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                                merchantAddress, merchantPhoneNumberField.getConfidence());
                        }
                    }

                    FormField transactionDateField = recognizedFields.get("TransactionDate");
                    if (transactionDateField != null) {
                        if (FieldValueType.DATE == transactionDateField.getValue().getValueType()) {
                            LocalDate transactionDate = transactionDateField.getValue().asDate();
                            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                                transactionDate, transactionDateField.getConfidence());
                        }
                    }

                    FormField receiptItemsField = recognizedFields.get("Items");
                    if (receiptItemsField != null) {
                        System.out.printf("Receipt Items: %n");
                        if (FieldValueType.LIST == receiptItemsField.getValue().getValueType()) {
                            List<FormField> receiptItems = receiptItemsField.getValue().asList();
                            receiptItems.stream()
                                .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValue().getValueType())
                                .map(formField -> formField.getValue().asMap())
                                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                    if ("Quantity".equals(key)) {
                                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                                            Float quantity = formField.getValue().asFloat();
                                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                                quantity, formField.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-RecognizeReceiptsOptions
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
                            Map<String, FormField> recognizedFields = recognizedForm.getFields();
                            System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
                            FormField merchantNameField = recognizedFields.get("MerchantName");
                            if (merchantNameField != null) {
                                if (FieldValueType.STRING == merchantNameField.getValue().getValueType()) {
                                    String merchantName = merchantNameField.getValue().asString();
                                    System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                                        merchantName, merchantNameField.getConfidence());
                                }
                            }

                            FormField merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
                            if (merchantPhoneNumberField != null) {
                                if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue().getValueType()) {
                                    String merchantAddress = merchantPhoneNumberField.getValue().asPhoneNumber();
                                    System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                                        merchantAddress, merchantPhoneNumberField.getConfidence());
                                }
                            }

                            FormField transactionDateField = recognizedFields.get("TransactionDate");
                            if (transactionDateField != null) {
                                if (FieldValueType.DATE == transactionDateField.getValue().getValueType()) {
                                    LocalDate transactionDate = transactionDateField.getValue().asDate();
                                    System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                                        transactionDate, transactionDateField.getConfidence());
                                }
                            }

                            FormField receiptItemsField = recognizedFields.get("Items");
                            if (receiptItemsField != null) {
                                System.out.printf("Receipt Items: %n");
                                if (FieldValueType.LIST == receiptItemsField.getValue().getValueType()) {
                                    List<FormField> receiptItems = receiptItemsField.getValue().asList();
                                    receiptItems.stream()
                                        .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValue().getValueType())
                                        .map(formField -> formField.getValue().asMap())
                                        .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                            if ("Quantity".equals(key)) {
                                                if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                                                    Float quantity = formField.getValue().asFloat();
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
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts(Flux, long, RecognizeReceiptsOptions)} with
     * options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceiptsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-RecognizeReceiptsOptions
        File receipt = new File("{local/file_path/fileName.jpg}");
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(receipt.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeReceipts(buffer, receipt.length(),
            new RecognizeReceiptsOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setFieldElementsIncluded(includeFieldElements)
                .setLocale(FormRecognizerLocale.EN_US)
                .setPollInterval(Duration.ofSeconds(5)))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedReceipts -> {
                for (int i = 0; i < recognizedReceipts.size(); i++) {
                    RecognizedForm recognizedForm = recognizedReceipts.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized Receipt page %d -----------%n", i);
                    FormField merchantNameField = recognizedFields.get("MerchantName");
                    if (merchantNameField != null) {
                        if (FieldValueType.STRING == merchantNameField.getValue().getValueType()) {
                            String merchantName = merchantNameField.getValue().asString();
                            System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                                merchantName, merchantNameField.getConfidence());
                        }
                    }

                    FormField merchantPhoneNumberField = recognizedFields.get("MerchantPhoneNumber");
                    if (merchantPhoneNumberField != null) {
                        if (FieldValueType.PHONE_NUMBER == merchantPhoneNumberField.getValue().getValueType()) {
                            String merchantAddress = merchantPhoneNumberField.getValue().asPhoneNumber();
                            System.out.printf("Merchant Phone number: %s, confidence: %.2f%n",
                                merchantAddress, merchantPhoneNumberField.getConfidence());
                        }
                    }

                    FormField transactionDateField = recognizedFields.get("TransactionDate");
                    if (transactionDateField != null) {
                        if (FieldValueType.DATE == transactionDateField.getValue().getValueType()) {
                            LocalDate transactionDate = transactionDateField.getValue().asDate();
                            System.out.printf("Transaction Date: %s, confidence: %.2f%n",
                                transactionDate, transactionDateField.getConfidence());
                        }
                    }

                    FormField receiptItemsField = recognizedFields.get("Items");
                    if (receiptItemsField != null) {
                        System.out.printf("Receipt Items: %n");
                        if (FieldValueType.LIST == receiptItemsField.getValue().getValueType()) {
                            List<FormField> receiptItems = receiptItemsField.getValue().asList();
                            receiptItems.stream()
                                .filter(receiptItem -> FieldValueType.MAP == receiptItem.getValue().getValueType())
                                .map(formField -> formField.getValue().asMap())
                                .forEach(formFieldMap -> formFieldMap.forEach((key, formField) -> {
                                    if ("Quantity".equals(key)) {
                                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                                            Float quantity = formField.getValue().asFloat();
                                            System.out.printf("Quantity: %f, confidence: %.2f%n",
                                                quantity, formField.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-RecognizeReceiptsOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeBusinessCardsFromUrl(String)}
     */
    public void beginRecognizeBusinessCardsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl#string
        String businessCardUrl = "{business_card_url}";
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl(businessCardUrl)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedBusinessCards -> {
                for (int i = 0; i < recognizedBusinessCards.size(); i++) {
                    RecognizedForm recognizedForm = recognizedBusinessCards.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized Business Card page %d -----------%n", i);
                    FormField contactNamesFormField = recognizedFields.get("ContactNames");
                    if (contactNamesFormField != null) {
                        if (FieldValueType.LIST == contactNamesFormField.getValue().getValueType()) {
                            List<FormField> contactNamesList = contactNamesFormField.getValue().asList();
                            contactNamesList.stream()
                                .filter(contactName -> FieldValueType.MAP == contactName.getValue().getValueType())
                                .map(contactName -> {
                                    System.out.printf("Contact name: %s%n", contactName.getValueData().getText());
                                    return contactName.getValue().asMap();
                                })
                                .forEach(contactNamesMap -> contactNamesMap.forEach((key, contactName) -> {
                                    if ("FirstName".equals(key)) {
                                        if (FieldValueType.STRING == contactName.getValue().getValueType()) {
                                            String firstName = contactName.getValue().asString();
                                            System.out.printf("\tFirst Name: %s, confidence: %.2f%n",
                                                firstName, contactName.getConfidence());
                                        }
                                    }
                                    if ("LastName".equals(key)) {
                                        if (FieldValueType.STRING == contactName.getValue().getValueType()) {
                                            String lastName = contactName.getValue().asString();
                                            System.out.printf("\tLast Name: %s, confidence: %.2f%n",
                                                lastName, contactName.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                    FormField jobTitles = recognizedFields.get("JobTitles");
                    if (jobTitles != null) {
                        if (FieldValueType.LIST == jobTitles.getValue().getValueType()) {
                            List<FormField> jobTitlesItems = jobTitles.getValue().asList();
                            jobTitlesItems.forEach(jobTitlesItem -> {
                                if (FieldValueType.STRING == jobTitlesItem.getValue().getValueType()) {
                                    String jobTitle = jobTitlesItem.getValue().asString();
                                    System.out.printf("Job Title: %s, confidence: %.2f%n",
                                        jobTitle, jobTitlesItem.getConfidence());
                                }
                            });
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl#string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeBusinessCardsFromUrl(String, RecognizeBusinessCardsOptions)}
     */
    public void beginRecognizeBusinessCardsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl#string-RecognizeBusinessCardsOptions
        String businessCardUrl = "{business_card_url}";
        boolean includeFieldElements = true;
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl(businessCardUrl,
            new RecognizeBusinessCardsOptions()
                .setFieldElementsIncluded(includeFieldElements))
            .setPollInterval(Duration.ofSeconds(5))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedBusinessCards -> {
                for (int i = 0; i < recognizedBusinessCards.size(); i++) {
                    RecognizedForm recognizedBusinessCard = recognizedBusinessCards.get(i);
                    Map<String, FormField> recognizedFields = recognizedBusinessCard.getFields();
                    System.out.printf("----------- Recognized Business Card page %d -----------%n", i);
                    FormField contactNamesFormField = recognizedFields.get("ContactNames");
                    if (contactNamesFormField != null) {
                        if (FieldValueType.LIST == contactNamesFormField.getValue().getValueType()) {
                            List<FormField> contactNamesList = contactNamesFormField.getValue().asList();
                            contactNamesList.stream()
                                .filter(contactName -> FieldValueType.MAP == contactName.getValue().getValueType())
                                .map(contactName -> {
                                    System.out.printf("Contact name: %s%n", contactName.getValueData().getText());
                                    return contactName.getValue().asMap();
                                })
                                .forEach(contactNamesMap -> contactNamesMap.forEach((key, contactName) -> {
                                    if ("FirstName".equals(key)) {
                                        if (FieldValueType.STRING == contactName.getValue().getValueType()) {
                                            String firstName = contactName.getValue().asString();
                                            System.out.printf("\tFirst Name: %s, confidence: %.2f%n",
                                                firstName, contactName.getConfidence());
                                        }
                                    }
                                    if ("LastName".equals(key)) {
                                        if (FieldValueType.STRING == contactName.getValue().getValueType()) {
                                            String lastName = contactName.getValue().asString();
                                            System.out.printf("\tLast Name: %s, confidence: %.2f%n",
                                                lastName, contactName.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                    FormField jobTitles = recognizedFields.get("JobTitles");
                    if (jobTitles != null) {
                        if (FieldValueType.LIST == jobTitles.getValue().getValueType()) {
                            List<FormField> jobTitlesItems = jobTitles.getValue().asList();
                            jobTitlesItems.forEach(jobTitlesItem -> {
                                if (FieldValueType.STRING == jobTitlesItem.getValue().getValueType()) {
                                    String jobTitle = jobTitlesItem.getValue().asString();
                                    System.out.printf("Job Title: %s, confidence: %.2f%n",
                                        jobTitle, jobTitlesItem.getConfidence());
                                }
                            });
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCardsFromUrl#string-RecognizeBusinessCardsOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeBusinessCards(Flux, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeBusinessCards() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long
        File businessCard = new File("{local/file_path/fileName.jpg}");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(businessCard.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeBusinessCards(buffer, businessCard.length())
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedBusinessCards -> {
                for (int i = 0; i < recognizedBusinessCards.size(); i++) {
                    RecognizedForm recognizedForm = recognizedBusinessCards.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized Business Card page %d -----------%n", i);
                    FormField contactNamesFormField = recognizedFields.get("ContactNames");
                    if (contactNamesFormField != null) {
                        if (FieldValueType.LIST == contactNamesFormField.getValue().getValueType()) {
                            List<FormField> contactNamesList = contactNamesFormField.getValue().asList();
                            contactNamesList.stream()
                                .filter(contactName -> FieldValueType.MAP == contactName.getValue().getValueType())
                                .map(contactName -> {
                                    System.out.printf("Contact name: %s%n", contactName.getValueData().getText());
                                    return contactName.getValue().asMap();
                                })
                                .forEach(contactNamesMap -> contactNamesMap.forEach((key, contactName) -> {
                                    if ("FirstName".equals(key)) {
                                        if (FieldValueType.STRING == contactName.getValue().getValueType()) {
                                            String firstName = contactName.getValue().asString();
                                            System.out.printf("\tFirst Name: %s, confidence: %.2f%n",
                                                firstName, contactName.getConfidence());
                                        }
                                    }
                                    if ("LastName".equals(key)) {
                                        if (FieldValueType.STRING == contactName.getValue().getValueType()) {
                                            String lastName = contactName.getValue().asString();
                                            System.out.printf("\tLast Name: %s, confidence: %.2f%n",
                                                lastName, contactName.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                    FormField jobTitles = recognizedFields.get("JobTitles");
                    if (jobTitles != null) {
                        if (FieldValueType.LIST == jobTitles.getValue().getValueType()) {
                            List<FormField> jobTitlesItems = jobTitles.getValue().asList();
                            jobTitlesItems.forEach(jobTitlesItem -> {
                                if (FieldValueType.STRING == jobTitlesItem.getValue().getValueType()) {
                                    String jobTitle = jobTitlesItem.getValue().asString();
                                    System.out.printf("Job Title: %s, confidence: %.2f%n",
                                        jobTitle, jobTitlesItem.getConfidence());
                                }
                            });
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeBusinessCards(Flux, long, RecognizeBusinessCardsOptions)} with
     * options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeBusinessCardsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long-RecognizeBusinessCardsOptions
        File businessCard = new File("{local/file_path/fileName.jpg}");
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer = toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(businessCard.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeBusinessCards(buffer, businessCard.length(),
            new RecognizeBusinessCardsOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setFieldElementsIncluded(includeFieldElements))
            .setPollInterval(Duration.ofSeconds(5))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedBusinessCards -> {
                for (int i = 0; i < recognizedBusinessCards.size(); i++) {
                    RecognizedForm recognizedForm = recognizedBusinessCards.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized Business Card page %d -----------%n", i);
                    FormField contactNamesFormField = recognizedFields.get("ContactNames");
                    if (contactNamesFormField != null) {
                        if (FieldValueType.LIST == contactNamesFormField.getValue().getValueType()) {
                            List<FormField> contactNamesList = contactNamesFormField.getValue().asList();
                            contactNamesList.stream()
                                .filter(contactName -> FieldValueType.MAP == contactName.getValue().getValueType())
                                .map(contactName -> {
                                    System.out.printf("Contact name: %s%n", contactName.getValueData().getText());
                                    return contactName.getValue().asMap();
                                })
                                .forEach(contactNamesMap -> contactNamesMap.forEach((key, contactName) -> {
                                    if ("FirstName".equals(key)) {
                                        if (FieldValueType.STRING == contactName.getValue().getValueType()) {
                                            String firstName = contactName.getValue().asString();
                                            System.out.printf("\tFirst Name: %s, confidence: %.2f%n",
                                                firstName, contactName.getConfidence());
                                        }
                                    }
                                    if ("LastName".equals(key)) {
                                        if (FieldValueType.STRING == contactName.getValue().getValueType()) {
                                            String lastName = contactName.getValue().asString();
                                            System.out.printf("\tLast Name: %s, confidence: %.2f%n",
                                                lastName, contactName.getConfidence());
                                        }
                                    }
                                }));
                        }
                    }
                    FormField jobTitles = recognizedFields.get("JobTitles");
                    if (jobTitles != null) {
                        if (FieldValueType.LIST == jobTitles.getValue().getValueType()) {
                            List<FormField> jobTitlesItems = jobTitles.getValue().asList();
                            jobTitlesItems.forEach(jobTitlesItem -> {
                                if (FieldValueType.STRING == jobTitlesItem.getValue().getValueType()) {
                                    String jobTitle = jobTitlesItem.getValue().asString();
                                    System.out.printf("Job Title: %s, confidence: %.2f%n",
                                        jobTitle, jobTitlesItem.getConfidence());
                                }
                            });
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeBusinessCards#Flux-long-RecognizeBusinessCardsOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeInvoicesFromUrl(String)}
     */
    public void beginRecognizeInvoicesFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string
        String invoiceUrl = "invoice_url";
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeInvoicesFromUrl(invoiceUrl)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedInvoices -> {
                for (int i = 0; i < recognizedInvoices.size(); i++) {
                    RecognizedForm recognizedForm = recognizedInvoices.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    FormField customAddrFormField = recognizedFields.get("CustomerAddress");
                    if (customAddrFormField != null) {
                        if (FieldValueType.STRING == customAddrFormField.getValue().getValueType()) {
                            System.out.printf("Customer Address: %s%n", customAddrFormField.getValue().asString());
                        }
                    }
                    FormField invoiceDateFormField = recognizedFields.get("InvoiceDate");
                    if (invoiceDateFormField != null) {
                        if (FieldValueType.DATE == invoiceDateFormField.getValue().getValueType()) {
                            LocalDate invoiceDate = invoiceDateFormField.getValue().asDate();
                            System.out.printf("Invoice Date: %s, confidence: %.2f%n",
                                invoiceDate, invoiceDateFormField.getConfidence());
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeInvoicesFromUrl(String, RecognizeInvoicesOptions)}
     */
    public void beginRecognizeInvoicesFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string-RecognizeInvoicesOptions
        String invoiceUrl = "invoice_url";
        boolean includeFieldElements = true;
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeInvoicesFromUrl(invoiceUrl,
            new RecognizeInvoicesOptions()
                .setFieldElementsIncluded(includeFieldElements))
            .setPollInterval(Duration.ofSeconds(5))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedInvoices -> {
                for (int i = 0; i < recognizedInvoices.size(); i++) {
                    RecognizedForm recognizedForm = recognizedInvoices.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    FormField customAddrFormField = recognizedFields.get("CustomerAddress");
                    if (customAddrFormField != null) {
                        if (FieldValueType.STRING == customAddrFormField.getValue().getValueType()) {
                            System.out.printf("Customer Address: %s%n", customAddrFormField.getValue().asString());
                        }
                    }
                    FormField invoiceDateFormField = recognizedFields.get("InvoiceDate");
                    if (invoiceDateFormField != null) {
                        if (FieldValueType.DATE == invoiceDateFormField.getValue().getValueType()) {
                            LocalDate invoiceDate = invoiceDateFormField.getValue().asDate();
                            System.out.printf("Invoice Date: %s, confidence: %.2f%n",
                                invoiceDate, invoiceDateFormField.getConfidence());
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoicesFromUrl#string-RecognizeInvoicesOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeInvoices(Flux, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeInvoices() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long
        File invoice = new File("local/file_path/invoice.jpg");
        Flux<ByteBuffer> buffer =
            toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(invoice.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeInvoices(buffer, invoice.length())
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedInvoices -> {
                for (int i = 0; i < recognizedInvoices.size(); i++) {
                    RecognizedForm recognizedForm = recognizedInvoices.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    FormField customAddrFormField = recognizedFields.get("CustomerAddress");
                    if (customAddrFormField != null) {
                        if (FieldValueType.STRING == customAddrFormField.getValue().getValueType()) {
                            System.out.printf("Customer Address: %s%n", customAddrFormField.getValue().asString());
                        }
                    }
                    FormField invoiceDateFormField = recognizedFields.get("InvoiceDate");
                    if (invoiceDateFormField != null) {
                        if (FieldValueType.DATE == invoiceDateFormField.getValue().getValueType()) {
                            LocalDate invoiceDate = invoiceDateFormField.getValue().asDate();
                            System.out.printf("Invoice Date: %s, confidence: %.2f%n",
                                invoiceDate, invoiceDateFormField.getConfidence());
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeInvoices(Flux, long, RecognizeInvoicesOptions)} with
     * options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeInvoicesWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long-RecognizeInvoicesOptions
        File invoice = new File("local/file_path/invoice.jpg");
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer =
            toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(invoice.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeInvoices(buffer,
            invoice.length(),
            new RecognizeInvoicesOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setFieldElementsIncluded(includeFieldElements))
            .setPollInterval(Duration.ofSeconds(5))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedInvoices -> {
                for (int i = 0; i < recognizedInvoices.size(); i++) {
                    RecognizedForm recognizedForm = recognizedInvoices.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    FormField customAddrFormField = recognizedFields.get("CustomerAddress");
                    if (customAddrFormField != null) {
                        if (FieldValueType.STRING == customAddrFormField.getValue().getValueType()) {
                            System.out.printf("Customer Address: %s%n", customAddrFormField.getValue().asString());
                        }
                    }
                    FormField invoiceDateFormField = recognizedFields.get("InvoiceDate");
                    if (invoiceDateFormField != null) {
                        if (FieldValueType.DATE == invoiceDateFormField.getValue().getValueType()) {
                            LocalDate invoiceDate = invoiceDateFormField.getValue().asDate();
                            System.out.printf("Invoice Date: %s, confidence: %.2f%n",
                                invoiceDate, invoiceDateFormField.getConfidence());
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeInvoices#Flux-long-RecognizeInvoicesOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeIdentityDocumentsFromUrl(String)}
     */
    public void beginRecognizeIDDocumentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string
        String idDocumentUrl = "idDocumentUrl";
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl(idDocumentUrl)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedIDDocumentResult -> {
                for (int i = 0; i < recognizedIDDocumentResult.size(); i++) {
                    RecognizedForm recognizedForm = recognizedIDDocumentResult.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized license info for page %d -----------%n", i);

                    FormField firstNameField = recognizedFields.get("FirstName");
                    if (firstNameField != null) {
                        if (FieldValueType.STRING == firstNameField.getValue().getValueType()) {
                            String firstName = firstNameField.getValue().asString();
                            System.out.printf("First Name: %s, confidence: %.2f%n",
                                firstName, firstNameField.getConfidence());
                        }
                    }

                    FormField lastNameField = recognizedFields.get("LastName");
                    if (lastNameField != null) {
                        if (FieldValueType.STRING == lastNameField.getValue().getValueType()) {
                            String lastName = lastNameField.getValue().asString();
                            System.out.printf("Last name: %s, confidence: %.2f%n",
                                lastName, lastNameField.getConfidence());
                        }
                    }

                    FormField countryRegionFormField = recognizedFields.get("CountryRegion");
                    if (countryRegionFormField != null) {
                        if (FieldValueType.STRING == countryRegionFormField.getValue().getValueType()) {
                            String countryRegion = countryRegionFormField.getValue().asCountryRegion();
                            System.out.printf("Country or region: %s, confidence: %.2f%n",
                                countryRegion, countryRegionFormField.getConfidence());
                        }
                    }

                    FormField dateOfExpirationField = recognizedFields.get("DateOfExpiration");
                    if (dateOfExpirationField != null) {
                        if (FieldValueType.DATE == dateOfExpirationField.getValue().getValueType()) {
                            LocalDate expirationDate = dateOfExpirationField.getValue().asDate();
                            System.out.printf("Document date of expiration: %s, confidence: %.2f%n",
                                expirationDate, dateOfExpirationField.getConfidence());
                        }
                    }

                    FormField documentNumberField = recognizedFields.get("DocumentNumber");
                    if (documentNumberField != null) {
                        if (FieldValueType.STRING == documentNumberField.getValue().getValueType()) {
                            String documentNumber = documentNumberField.getValue().asString();
                            System.out.printf("Document number: %s, confidence: %.2f%n",
                                documentNumber, documentNumberField.getConfidence());
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeIdentityDocumentsFromUrl(String, RecognizeIdentityDocumentOptions)}
     */
    public void beginRecognizeIdentityDocumentsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string-RecognizeIdentityDocumentOptions
        String licenseDocumentUrl = "licenseDocumentUrl";
        boolean includeFieldElements = true;
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl(licenseDocumentUrl,
            new RecognizeIdentityDocumentOptions()
                .setFieldElementsIncluded(includeFieldElements))
            .setPollInterval(Duration.ofSeconds(5))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedIDDocumentResult -> {
                for (int i = 0; i < recognizedIDDocumentResult.size(); i++) {
                    RecognizedForm recognizedForm = recognizedIDDocumentResult.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized license info for page %d -----------%n", i);

                    FormField firstNameField = recognizedFields.get("FirstName");
                    if (firstNameField != null) {
                        if (FieldValueType.STRING == firstNameField.getValue().getValueType()) {
                            String firstName = firstNameField.getValue().asString();
                            System.out.printf("First Name: %s, confidence: %.2f%n",
                                firstName, firstNameField.getConfidence());
                        }
                    }

                    FormField lastNameField = recognizedFields.get("LastName");
                    if (lastNameField != null) {
                        if (FieldValueType.STRING == lastNameField.getValue().getValueType()) {
                            String lastName = lastNameField.getValue().asString();
                            System.out.printf("Last name: %s, confidence: %.2f%n",
                                lastName, lastNameField.getConfidence());
                        }
                    }

                    FormField countryRegionFormField = recognizedFields.get("CountryRegion");
                    if (countryRegionFormField != null) {
                        if (FieldValueType.STRING == countryRegionFormField.getValue().getValueType()) {
                            String countryRegion = countryRegionFormField.getValue().asCountryRegion();
                            System.out.printf("Country or region: %s, confidence: %.2f%n",
                                countryRegion, countryRegionFormField.getConfidence());
                        }
                    }

                    FormField dateOfExpirationField = recognizedFields.get("DateOfExpiration");
                    if (dateOfExpirationField != null) {
                        if (FieldValueType.DATE == dateOfExpirationField.getValue().getValueType()) {
                            LocalDate expirationDate = dateOfExpirationField.getValue().asDate();
                            System.out.printf("Document date of expiration: %s, confidence: %.2f%n",
                                expirationDate, dateOfExpirationField.getConfidence());
                        }
                    }

                    FormField documentNumberField = recognizedFields.get("DocumentNumber");
                    if (documentNumberField != null) {
                        if (FieldValueType.STRING == documentNumberField.getValue().getValueType()) {
                            String documentNumber = documentNumberField.getValue().asString();
                            System.out.printf("Document number: %s, confidence: %.2f%n",
                                documentNumber, documentNumberField.getConfidence());
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocumentsFromUrl#string-RecognizeIdentityDocumentOptions
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeIdentityDocuments(Flux, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeIdentityDocuments() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long
        File license = new File("local/file_path/license.jpg");
        Flux<ByteBuffer> buffer =
            toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(license.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeIdentityDocuments(buffer, license.length())
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedIDDocumentResult -> {
                for (int i = 0; i < recognizedIDDocumentResult.size(); i++) {
                    RecognizedForm recognizedForm = recognizedIDDocumentResult.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized license info for page %d -----------%n", i);

                    FormField firstNameField = recognizedFields.get("FirstName");
                    if (firstNameField != null) {
                        if (FieldValueType.STRING == firstNameField.getValue().getValueType()) {
                            String firstName = firstNameField.getValue().asString();
                            System.out.printf("First Name: %s, confidence: %.2f%n",
                                firstName, firstNameField.getConfidence());
                        }
                    }

                    FormField lastNameField = recognizedFields.get("LastName");
                    if (lastNameField != null) {
                        if (FieldValueType.STRING == lastNameField.getValue().getValueType()) {
                            String lastName = lastNameField.getValue().asString();
                            System.out.printf("Last name: %s, confidence: %.2f%n",
                                lastName, lastNameField.getConfidence());
                        }
                    }

                    FormField countryRegionFormField = recognizedFields.get("CountryRegion");
                    if (countryRegionFormField != null) {
                        if (FieldValueType.STRING == countryRegionFormField.getValue().getValueType()) {
                            String countryRegion = countryRegionFormField.getValue().asCountryRegion();
                            System.out.printf("Country or region: %s, confidence: %.2f%n",
                                countryRegion, countryRegionFormField.getConfidence());
                        }
                    }

                    FormField dateOfExpirationField = recognizedFields.get("DateOfExpiration");
                    if (dateOfExpirationField != null) {
                        if (FieldValueType.DATE == dateOfExpirationField.getValue().getValueType()) {
                            LocalDate expirationDate = dateOfExpirationField.getValue().asDate();
                            System.out.printf("Document date of expiration: %s, confidence: %.2f%n",
                                expirationDate, dateOfExpirationField.getConfidence());
                        }
                    }

                    FormField documentNumberField = recognizedFields.get("DocumentNumber");
                    if (documentNumberField != null) {
                        if (FieldValueType.STRING == documentNumberField.getValue().getValueType()) {
                            String documentNumber = documentNumberField.getValue().asString();
                            System.out.printf("Document number: %s, confidence: %.2f%n",
                                documentNumber, documentNumberField.getConfidence());
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeIdentityDocuments(Flux, long, RecognizeIdentityDocumentOptions)} with
     * options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeIdentityDocumentsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long-RecognizeIdentityDocumentOptions
        File licenseDocument = new File("local/file_path/license.jpg");
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer =
            toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(licenseDocument.toPath())));
        // if training polling operation completed, retrieve the final result.
        formRecognizerAsyncClient.beginRecognizeIdentityDocuments(buffer,
            licenseDocument.length(),
            new RecognizeIdentityDocumentOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setFieldElementsIncluded(includeFieldElements))
            .setPollInterval(Duration.ofSeconds(5))
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(recognizedIDDocumentResult -> {
                for (int i = 0; i < recognizedIDDocumentResult.size(); i++) {
                    RecognizedForm recognizedForm = recognizedIDDocumentResult.get(i);
                    Map<String, FormField> recognizedFields = recognizedForm.getFields();
                    System.out.printf("----------- Recognized license info for page %d -----------%n", i);

                    FormField firstNameField = recognizedFields.get("FirstName");
                    if (firstNameField != null) {
                        if (FieldValueType.STRING == firstNameField.getValue().getValueType()) {
                            String firstName = firstNameField.getValue().asString();
                            System.out.printf("First Name: %s, confidence: %.2f%n",
                                firstName, firstNameField.getConfidence());
                        }
                    }

                    FormField lastNameField = recognizedFields.get("LastName");
                    if (lastNameField != null) {
                        if (FieldValueType.STRING == lastNameField.getValue().getValueType()) {
                            String lastName = lastNameField.getValue().asString();
                            System.out.printf("Last name: %s, confidence: %.2f%n",
                                lastName, lastNameField.getConfidence());
                        }
                    }

                    FormField countryRegionFormField = recognizedFields.get("CountryRegion");
                    if (countryRegionFormField != null) {
                        if (FieldValueType.STRING == countryRegionFormField.getValue().getValueType()) {
                            String countryRegion = countryRegionFormField.getValue().asCountryRegion();
                            System.out.printf("Country or region: %s, confidence: %.2f%n",
                                countryRegion, countryRegionFormField.getConfidence());
                        }
                    }

                    FormField dateOfExpirationField = recognizedFields.get("DateOfExpiration");
                    if (dateOfExpirationField != null) {
                        if (FieldValueType.DATE == dateOfExpirationField.getValue().getValueType()) {
                            LocalDate expirationDate = dateOfExpirationField.getValue().asDate();
                            System.out.printf("Document date of expiration: %s, confidence: %.2f%n",
                                expirationDate, dateOfExpirationField.getConfidence());
                        }
                    }

                    FormField documentNumberField = recognizedFields.get("DocumentNumber");
                    if (documentNumberField != null) {
                        if (FieldValueType.STRING == documentNumberField.getValue().getValueType()) {
                            String documentNumber = documentNumberField.getValue().asString();
                            System.out.printf("Document number: %s, confidence: %.2f%n",
                                documentNumber, documentNumberField.getConfidence());
                        }
                    }
                }
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeIdentityDocuments#Flux-long-RecognizeIdentityDocumentOptions
    }
}
