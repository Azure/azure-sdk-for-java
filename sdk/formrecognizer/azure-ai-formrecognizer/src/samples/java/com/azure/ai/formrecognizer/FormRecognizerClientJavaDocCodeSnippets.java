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
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.RecognizeCustomFormsOptions;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Context;
import reactor.core.publisher.Flux;

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
    private final FormRecognizerClient formRecognizerClient = new FormRecognizerClientBuilder().buildClient();

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

        formRecognizerClient.beginRecognizeCustomFormsFromUrl(modelId, formUrl).getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(formFieldMap -> formFieldMap.forEach((fieldText, formField) -> {
                System.out.printf("Field text: %s%n", fieldText);
                System.out.printf("Field value data text: %s%n", formField.getValueData().getText());
                System.out.printf("Confidence score: %.2f%n", formField.getConfidence());
            }));

        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeCustomFormsFromUrl(String, String, RecognizeCustomFormsOptions, Context)}
     */
    public void beginRecognizeCustomFormsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-RecognizeCustomFormsOptions-Context
        String analyzeFilePath = "{file_source_url}";
        String modelId = "{model_id}";
        boolean includeFieldElements = true;

        formRecognizerClient.beginRecognizeCustomFormsFromUrl(modelId, analyzeFilePath,
            new RecognizeCustomFormsOptions()
                .setFieldElementsIncluded(includeFieldElements)
                .setPollInterval(Duration.ofSeconds(10)), Context.NONE)
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(formFieldMap -> formFieldMap.forEach((fieldText, formField) -> {
                System.out.printf("Field text: %s%n", fieldText);
                System.out.printf("Field value data text: %s%n", formField.getValueData().getText());
                System.out.printf("Confidence score: %.2f%n", formField.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-RecognizeCustomFormsOptions-Context
    }

    /**
     * Code snippet for
     * {@link FormRecognizerClient#beginRecognizeCustomForms(String, InputStream, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#string-InputStream-long
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        byte[] fileContent = Files.readAllBytes(form.toPath());
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {

            formRecognizerClient.beginRecognizeCustomForms(modelId, targetStream, form.length())
                .getFinalResult()
                .stream()
                .map(RecognizedForm::getFields)
                .forEach(formFieldMap -> formFieldMap.forEach((fieldText, formField) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value data text: %s%n", formField.getValueData().getText());
                    System.out.printf("Confidence score: %.2f%n", formField.getConfidence());
                }));
        }
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#string-InputStream-long
    }

    /**
     * Code snippet for
     * {@link FormRecognizerClient#beginRecognizeCustomForms(String, InputStream, long, RecognizeCustomFormsOptions, Context)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#string-InputStream-long-RecognizeCustomFormsOptions-Context
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        boolean includeFieldElements = true;
        byte[] fileContent = Files.readAllBytes(form.toPath());

        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {
            formRecognizerClient.beginRecognizeCustomForms(modelId, targetStream, form.length(),
                new RecognizeCustomFormsOptions()
                    .setContentType(FormContentType.IMAGE_JPEG)
                    .setFieldElementsIncluded(includeFieldElements)
                    .setPollInterval(Duration.ofSeconds(10)), Context.NONE)
                .getFinalResult()
                .stream()
                .map(RecognizedForm::getFields)
                .forEach(formFieldMap -> formFieldMap.forEach((fieldText, formField) -> {
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value data text: %s%n", formField.getValueData().getText());
                    System.out.printf("Confidence score: %.2f%n", formField.getConfidence());
                }));
        }
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#string-InputStream-long-RecognizeCustomFormsOptions-Context
    }

    // Recognize Content

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContentFromUrl(String)}
     */
    public void beginRecognizeContentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string
        String formUrl = "{form_url}";
        formRecognizerClient.beginRecognizeContentFromUrl(formUrl)
            .getFinalResult()
            .forEach(formPage -> {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables()
                    .stream()
                    .flatMap(formTable -> formTable.getCells().stream())
                    .forEach(recognizedTableCell -> System.out.printf("%s ", recognizedTableCell.getText()));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContentFromUrl(String, RecognizeContentOptions, Context)} with
     * options.
     */
    public void beginRecognizeContentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-RecognizeContentOptions-Context
        String formPath = "{file_source_url}";
        formRecognizerClient.beginRecognizeContentFromUrl(formPath,
            new RecognizeContentOptions()
                .setPollInterval(Duration.ofSeconds(5)), Context.NONE)
            .getFinalResult()
            .forEach(formPage -> {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables()
                    .stream()
                    .flatMap(formTable -> formTable.getCells().stream())
                    .forEach(recognizedTableCell -> System.out.printf("%s ", recognizedTableCell.getText()));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-RecognizeContentOptions-Context
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
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {
            formRecognizerClient.beginRecognizeContent(targetStream, form.length())
                .getFinalResult()
                .forEach(formPage -> {
                    System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                    System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                    // Table information
                    System.out.println("Recognized Tables: ");
                    formPage.getTables()
                        .stream()
                        .flatMap(formTable -> formTable.getCells().stream())
                        .forEach(recognizedTableCell -> System.out.printf("%s ", recognizedTableCell.getText()));
                });
        }
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContent(InputStream, long, RecognizeContentOptions, Context)} with
     * options.
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-RecognizeContentOptions-Context
        File form = new File("{file_source_url}");
        byte[] fileContent = Files.readAllBytes(form.toPath());
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {

            for (FormPage formPage : formRecognizerClient.beginRecognizeContent(targetStream, form.length(),
                new RecognizeContentOptions()
                    .setPollInterval(Duration.ofSeconds(5)), Context.NONE)
                .getFinalResult()) {
                System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                formPage.getTables()
                    .stream()
                    .flatMap(formTable -> formTable.getCells().stream())
                    .forEach(recognizedTableCell -> System.out.printf("%s ", recognizedTableCell.getText()));
            }
        }
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-RecognizeContentOptions-Context
    }

    // Recognize Receipts

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceiptsFromUrl(String)}
     */
    public void beginRecognizeReceiptsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string
        String receiptUrl = "{file_source_url}";
        formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl)
            .getFinalResult()
            .forEach(recognizedReceipt -> {
                Map<String, FormField> recognizedFields = recognizedReceipt.getFields();
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceiptsFromUrl(String, RecognizeReceiptsOptions, Context)}
     */
    public void beginRecognizeReceiptsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-RecognizeReceiptsOptions-Context
        String receiptUrl = "{receipt_url}";
        formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl,
            new RecognizeReceiptsOptions()
                .setLocale(FormRecognizerLocale.EN_US)
                .setPollInterval(Duration.ofSeconds(5))
                .setFieldElementsIncluded(true), Context.NONE)
            .getFinalResult()
            .forEach(recognizedReceipt -> {
                Map<String, FormField> recognizedFields = recognizedReceipt.getFields();
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-RecognizeReceiptsOptions-Context
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceipts(InputStream, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceipts() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long
        File receipt = new File("{receipt_url}");
        byte[] fileContent = Files.readAllBytes(receipt.toPath());
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {

            formRecognizerClient.beginRecognizeReceipts(targetStream, receipt.length()).getFinalResult()
                .forEach(recognizedReceipt -> {
                    Map<String, FormField> recognizedFields = recognizedReceipt.getFields();
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
                });
        }
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceipts(InputStream, long, RecognizeReceiptsOptions, Context)}
     * with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceiptsWithOptions() throws IOException {

        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-RecognizeReceiptsOptions-Context
        File receipt = new File("{local/file_path/fileName.jpg}");
        boolean includeFieldElements = true;
        byte[] fileContent = Files.readAllBytes(receipt.toPath());
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {
            for (RecognizedForm recognizedForm : formRecognizerClient
                .beginRecognizeReceipts(targetStream, receipt.length(),
                    new RecognizeReceiptsOptions()
                        .setContentType(FormContentType.IMAGE_JPEG)
                        .setFieldElementsIncluded(includeFieldElements)
                        .setLocale(FormRecognizerLocale.EN_US)
                        .setPollInterval(Duration.ofSeconds(5)), Context.NONE)
                .getFinalResult()) {
                Map<String, FormField> recognizedFields = recognizedForm.getFields();
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
        }
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-RecognizeReceiptsOptions-Context
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeBusinessCardsFromUrl(String)}
     */
    public void beginRecognizeBusinessCardsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeBusinessCardsFromUrl#string
        String businessCardUrl = "{business_card_url}";
        formRecognizerClient.beginRecognizeBusinessCardsFromUrl(businessCardUrl)
            .getFinalResult()
            .forEach(recognizedBusinessCard -> {
                Map<String, FormField> recognizedFields = recognizedBusinessCard.getFields();
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeBusinessCardsFromUrl#string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerClient#beginRecognizeBusinessCardsFromUrl(String, RecognizeBusinessCardsOptions, Context)}
     */
    public void beginRecognizeBusinessCardsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeBusinessCardsFromUrl#string-RecognizeBusinessCardsOptions-Context
        String businessCardUrl = "{business_card_url}";
        formRecognizerClient.beginRecognizeBusinessCardsFromUrl(businessCardUrl,
            new RecognizeBusinessCardsOptions()
                .setFieldElementsIncluded(true), Context.NONE)
            .setPollInterval(Duration.ofSeconds(5)).getFinalResult()
            .forEach(recognizedBusinessCard -> {
                Map<String, FormField> recognizedFields = recognizedBusinessCard.getFields();
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeBusinessCardsFromUrl#string-RecognizeBusinessCardsOptions-Context
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeBusinessCards(InputStream, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeBusinessCards() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeBusinessCards#InputStream-long
        File businessCard = new File("{local/file_path/fileName.jpg}");
        byte[] fileContent = Files.readAllBytes(businessCard.toPath());
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {
            formRecognizerClient.beginRecognizeBusinessCards(targetStream, businessCard.length()).getFinalResult()
                .forEach(recognizedBusinessCard -> {
                    Map<String, FormField> recognizedFields = recognizedBusinessCard.getFields();
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
                });
        }
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeBusinessCards#InputStream-long
    }

    /**
     * Code snippet for
     * {@link FormRecognizerClient#beginRecognizeBusinessCards(InputStream, long, RecognizeBusinessCardsOptions,
     * Context)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeBusinessCardsWithOptions() throws IOException {

        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeBusinessCards#InputStream-long-RecognizeBusinessCardsOptions-Context
        File businessCard = new File("{local/file_path/fileName.jpg}");
        boolean includeFieldElements = true;
        byte[] fileContent = Files.readAllBytes(businessCard.toPath());
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {
            for (RecognizedForm recognizedForm : formRecognizerClient.beginRecognizeBusinessCards(targetStream,
                businessCard.length(),
                new RecognizeBusinessCardsOptions()
                    .setContentType(FormContentType.IMAGE_JPEG)
                    .setFieldElementsIncluded(includeFieldElements),
                Context.NONE).setPollInterval(Duration.ofSeconds(5))
                .getFinalResult()) {
                Map<String, FormField> recognizedFields = recognizedForm.getFields();
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
        }
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeBusinessCards#InputStream-long-RecognizeBusinessCardsOptions-Context
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeInvoicesFromUrl(String)}
     */
    public void beginRecognizeInvoicesFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeInvoicesFromUrl#string
        String invoiceUrl = "invoice_url";
        // if training polling operation completed, retrieve the final result.
        formRecognizerClient.beginRecognizeInvoicesFromUrl(invoiceUrl)
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(recognizedFields -> {
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeInvoicesFromUrl#string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeInvoicesFromUrl(String, RecognizeInvoicesOptions)}
     */
    public void beginRecognizeInvoicesFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeInvoicesFromUrl#string-RecognizeInvoicesOptions-Context
        String invoiceUrl = "invoice_url";
        boolean includeFieldElements = true;
        // if training polling operation completed, retrieve the final result.
        formRecognizerClient.beginRecognizeInvoicesFromUrl(invoiceUrl,
            new RecognizeInvoicesOptions()
                .setFieldElementsIncluded(includeFieldElements),
            Context.NONE).setPollInterval(Duration.ofSeconds(5))
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(recognizedFields -> {
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeInvoicesFromUrl#string-RecognizeInvoicesOptions-Context
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeInvoices(Flux, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeInvoices() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeInvoices#InputStream-long
        File invoice = new File("local/file_path/invoice.jpg");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(invoice.toPath()));
        // if training polling operation completed, retrieve the final result.
        formRecognizerClient.beginRecognizeInvoices(inputStream, invoice.length())
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(recognizedFields -> {
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeInvoices#InputStream-long
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeInvoices(Flux, long, RecognizeInvoicesOptions)} with
     * options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeInvoicesWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeInvoices#InputStream-long-RecognizeInvoicesOptions-Context
        File invoice = new File("local/file_path/invoice.jpg");
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(invoice.toPath()));
        // if training polling operation completed, retrieve the final result.
        formRecognizerClient.beginRecognizeInvoices(inputStream,
            invoice.length(),
            new RecognizeInvoicesOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setFieldElementsIncluded(includeFieldElements),
            Context.NONE)
            .setPollInterval(Duration.ofSeconds(5))
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(recognizedFields -> {
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeInvoices#InputStream-long-RecognizeInvoicesOptions-Context
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeIdentityDocumentsFromUrl(String)}
     */
    public void beginRecognizeIdentityDocumentsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeIdentityDocumentsFromUrl#string
        String licenseDocumentUrl = "licenseDocumentUrl";
        // if training polling operation completed, retrieve the final result.
        formRecognizerClient.beginRecognizeIdentityDocumentsFromUrl(licenseDocumentUrl)
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(recognizedFields -> {
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

                FormField dateOfBirthField = recognizedFields.get("DateOfBirth");
                if (dateOfBirthField != null) {
                    if (FieldValueType.DATE == dateOfBirthField.getValue().getValueType()) {
                        LocalDate dateOfBirth = dateOfBirthField.getValue().asDate();
                        System.out.printf("Date of Birth: %s, confidence: %.2f%n",
                            dateOfBirth, dateOfBirthField.getConfidence());
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeIdentityDocumentsFromUrl#string
    }

    /**
     * Code snippet for
     * {@link FormRecognizerAsyncClient#beginRecognizeIdentityDocumentsFromUrl(String, RecognizeIdentityDocumentOptions, Context)}
     */
    public void beginRecognizeIdentityDocumentsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeIdentityDocumentsFromUrl#string-RecognizeIdentityDocumentOptions-Context
        String licenseDocumentUrl = "licenseDocumentUrl";
        boolean includeFieldElements = true;
        // if training polling operation completed, retrieve the final result.
        formRecognizerClient.beginRecognizeIdentityDocumentsFromUrl(licenseDocumentUrl,
            new RecognizeIdentityDocumentOptions()
                .setFieldElementsIncluded(includeFieldElements),
            Context.NONE).setPollInterval(Duration.ofSeconds(5))
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(recognizedFields -> {
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeIdentityDocumentsFromUrl#string-RecognizeIdentityDocumentOptions-Context
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeIdentityDocuments(InputStream, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeIdentityDocuments() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeIdentityDocuments#InputStream-long
        File license = new File("local/file_path/license.jpg");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(license.toPath()));
        // if training polling operation completed, retrieve the final result.
        formRecognizerClient.beginRecognizeIdentityDocuments(inputStream, license.length())
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(recognizedFields -> {
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeIdentityDocuments#InputStream-long
    }

    /**
     * Code snippet for
     * {@link FormRecognizerClient#beginRecognizeIdentityDocuments(InputStream, long, RecognizeIdentityDocumentOptions, Context)}
     * with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeIdentityDocumentsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeIdentityDocuments#InputStream-long-RecognizeIdentityDocumentOptions-Context
        File licenseDocument = new File("local/file_path/license.jpg");
        boolean includeFieldElements = true;
        // Utility method to convert input stream to Byte buffer
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(licenseDocument.toPath()));
        // if training polling operation completed, retrieve the final result.
        formRecognizerClient.beginRecognizeIdentityDocuments(inputStream,
            licenseDocument.length(),
            new RecognizeIdentityDocumentOptions()
                .setContentType(FormContentType.IMAGE_JPEG)
                .setFieldElementsIncluded(includeFieldElements),
            Context.NONE)
            .setPollInterval(Duration.ofSeconds(5))
            .getFinalResult()
            .stream()
            .map(RecognizedForm::getFields)
            .forEach(recognizedFields -> {
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

                FormField dateOfBirthField = recognizedFields.get("DateOfBirth");
                if (dateOfBirthField != null) {
                    if (FieldValueType.DATE == dateOfBirthField.getValue().getValueType()) {
                        LocalDate dateOfBirth = dateOfBirthField.getValue().asDate();
                        System.out.printf("Date of Birth: %s, confidence: %.2f%n",
                            dateOfBirth, dateOfBirthField.getConfidence());
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
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeIdentityDocuments#InputStream-long-RecognizeIdentityDocumentOptions-Context
    }
}
