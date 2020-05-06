// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;

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
     * Code snippet for {@link FormRecognizerClient#beginRecognizeCustomFormsFromUrl}
     */
    public void beginRecognizeCustomFormsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string
        String analyzeFilePath = "{file_source_url}";
        String modelId = "{model_id}";

        formRecognizerClient.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId).getFinalResult()
            .forEach(recognizedForm -> {
                recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                    System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                });
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeCustomFormsFromUrl} with options
     */
    public void beginRecognizeCustomFormsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-boolean-Duration
        String analyzeFilePath = "{file_source_url}";
        String modelId = "{model_id}";
        boolean includeTextDetails = true;

        formRecognizerClient.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId, includeTextDetails,
            Duration.ofSeconds(5)).getFinalResult().forEach(recognizedForm -> {
                recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                    System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                });
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomFormsFromUrl#string-string-boolean-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeCustomForms}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-string-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        String modelId = "{model_id}";
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeCustomForms(targetStream, modelId, sourceFile.length(),
            FormContentType.IMAGE_JPEG).getFinalResult().forEach(recognizedForm -> {
                recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                    System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                    System.out.printf("Field text: %s%n", fieldText);
                    System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                    System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                });
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-string-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeCustomForms} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-string-long-FormContentType-boolean-Duration
        File sourceFile = new File("{file_source_url}");
        String modelId = "{model_id}";
        boolean includeTextDetails = true;

        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeCustomForms(targetStream, modelId, sourceFile.length(),
            FormContentType.IMAGE_JPEG, includeTextDetails, Duration.ofSeconds(5)).getFinalResult()
                .forEach(recognizedForm -> {
                    recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                        System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                        System.out.printf("Field text: %s%n", fieldText);
                        System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                        System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                    });
                });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeCustomForms#InputStream-string-long-FormContentType-boolean-Duration
    }

    // Recognize Content
    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContentFromUrl}
     */
    public void beginRecognizeContentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string
        String sourceFilePath = "{file_source_url}";
        formRecognizerClient.beginRecognizeContentFromUrl(sourceFilePath).getFinalResult()
            .forEach(recognizedForm -> {
                System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable ->
                    formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContentFromUrl} with options
     */
    public void beginRecognizeContentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-Duration
        String sourceFilePath = "{file_source_url}";
        formRecognizerClient.beginRecognizeContentFromUrl(sourceFilePath, Duration.ofSeconds(5)).getFinalResult()
            .forEach(recognizedForm -> {
                System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable ->
                    formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContentFromUrl#string-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContent}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContent() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeContent(targetStream, sourceFile.length(), FormContentType.APPLICATION_PDF)
            .getFinalResult().forEach(recognizedForm -> {
                System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable ->
                    formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeContent} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType-Duration
        File sourceFile = new File("{file_source_url}");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);

        formRecognizerClient.beginRecognizeContent(targetStream, sourceFile.length(), FormContentType.APPLICATION_PDF,
            Duration.ofSeconds(5)).getFinalResult().forEach(recognizedForm -> {
                System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                // Table information
                System.out.println("Recognized Tables: ");
                recognizedForm.getTables().forEach(formTable ->
                    formTable.getCells().forEach(recognizedTableCell ->
                        System.out.printf("%s ", recognizedTableCell.getText())));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeContent#InputStream-long-FormContentType-Duration
    }

    // Recognize Receipts
    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceiptsFromUrl}
     */
    public void beginRecognizeReceiptsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string
        String receiptUrl = "{file_source_url}";
        formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl).getFinalResult()
            .forEach(recognizedReceipt -> {
                USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
                System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
                System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                    usReceipt.getMerchantName().getFieldValue(),
                    usReceipt.getMerchantName().getConfidence());
                System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                    usReceipt.getMerchantAddress().getFieldValue(),
                    usReceipt.getMerchantAddress().getConfidence());
                System.out.printf("Merchant Phone Number %s, confidence: %.2f%n",
                    usReceipt.getMerchantPhoneNumber().getFieldValue(),
                    usReceipt.getMerchantPhoneNumber().getConfidence());
                System.out.printf("Total: %.2f, confidence: %.2f%n",
                    usReceipt.getTotal().getFieldValue(),
                    usReceipt.getTotal().getConfidence());
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceiptsFromUrl} with options
     */
    public void beginRecognizeReceiptsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-boolean-Duration
        String receiptUrl = "{file_source_url}";
        boolean includeTextDetails = true;

        formRecognizerClient.beginRecognizeReceiptsFromUrl(receiptUrl, includeTextDetails, Duration.ofSeconds(5))
            .getFinalResult().forEach(recognizedReceipt -> {
                USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
                System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
                System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                    usReceipt.getMerchantName().getFieldValue(),
                    usReceipt.getMerchantName().getConfidence());
                System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                    usReceipt.getMerchantAddress().getFieldValue(),
                    usReceipt.getMerchantAddress().getConfidence());
                System.out.printf("Merchant Phone Number %s, confidence: %.2f%n",
                    usReceipt.getMerchantPhoneNumber().getFieldValue(),
                    usReceipt.getMerchantPhoneNumber().getConfidence());
                System.out.printf("Total: %.2f, confidence: %.2f%n",
                    usReceipt.getTotal().getFieldValue(),
                    usReceipt.getTotal().getConfidence());
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceiptsFromUrl#string-boolean-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceipts}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceipts() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);
        formRecognizerClient.beginRecognizeReceipts(targetStream, sourceFile.length(), FormContentType.IMAGE_JPEG)
            .getFinalResult().forEach(recognizedReceipt -> {
                USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
                System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
                System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                    usReceipt.getMerchantName().getFieldValue(),
                    usReceipt.getMerchantName().getConfidence());
                System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                    usReceipt.getMerchantAddress().getFieldValue(),
                    usReceipt.getMerchantAddress().getConfidence());
                System.out.printf("Merchant Phone Number %s, confidence: %.2f%n",
                    usReceipt.getMerchantPhoneNumber().getFieldValue(),
                    usReceipt.getMerchantPhoneNumber().getConfidence());
                System.out.printf("Total: %.2f, confidence: %.2f%n",
                    usReceipt.getTotal().getFieldValue(),
                    usReceipt.getTotal().getConfidence());
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType

    }

    /**
     * Code snippet for {@link FormRecognizerClient#beginRecognizeReceipts} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceiptsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType-boolean-Duration
        File sourceFile = new File("{file_source_url}");
        boolean includeTextDetails = true;
        byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
        InputStream targetStream = new ByteArrayInputStream(fileContent);
        formRecognizerClient.beginRecognizeReceipts(targetStream, sourceFile.length(), FormContentType.IMAGE_JPEG,
            includeTextDetails, Duration.ofSeconds(5)).getFinalResult().forEach(recognizedReceipt -> {
                USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
                System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
                System.out.printf("Merchant Name: %s, confidence: %.2f%n",
                    usReceipt.getMerchantName().getFieldValue(),
                    usReceipt.getMerchantName().getConfidence());
                System.out.printf("Merchant Address: %s, confidence: %.2f%n",
                    usReceipt.getMerchantAddress().getFieldValue(),
                    usReceipt.getMerchantAddress().getConfidence());
                System.out.printf("Merchant Phone Number %s, confidence: %.2f%n",
                    usReceipt.getMerchantPhoneNumber().getFieldValue(),
                    usReceipt.getMerchantPhoneNumber().getConfidence());
                System.out.printf("Total: %.2f, confidence: %.2f%n",
                    usReceipt.getTotal().getFieldValue(),
                    usReceipt.getTotal().getConfidence());
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerClient.beginRecognizeReceipts#InputStream-long-FormContentType-boolean-Duration
    }
}
