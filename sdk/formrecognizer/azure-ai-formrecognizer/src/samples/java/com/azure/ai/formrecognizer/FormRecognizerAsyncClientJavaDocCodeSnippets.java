// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.USReceipt;
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
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomFormsFromUrl}
     */
    public void beginRecognizeCustomFormsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string
        String analyzeFilePath = "{file_source_url}";
        String modelId = "{model_id}";

        formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId).subscribe(
            recognizePollingOperation ->
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedForms ->
                    recognizedForms.forEach(recognizedForm -> {
                        recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                            System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                            System.out.printf("Field text: %s%n", fieldText);
                            System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                            System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                        });
                    })
                )
        );
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomFormsFromUrl} with options
     */
    public void beginRecognizeCustomFormsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-boolean-Duration
        String analyzeFilePath = "{file_source_url}";
        String modelId = "{model_id}";
        boolean includeTextDetails = true;
        formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId, includeTextDetails,
            Duration.ofSeconds(5)).subscribe(recognizePollingOperation ->
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedForms ->
                    recognizedForms.forEach(recognizedForm -> {
                        recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                            System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                            System.out.printf("Field text: %s%n", fieldText);
                            System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                            System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                        });
                    })
                )
        );
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-boolean-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomForms}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-string-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        String modelId = "{model_id}";
        Flux<ByteBuffer> buffer = toFluxByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        formRecognizerAsyncClient.beginRecognizeCustomForms(buffer, modelId, sourceFile.length(),
            FormContentType.IMAGE_JPEG).subscribe(recognizePollingOperation ->
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedForms ->
                    recognizedForms.forEach(recognizedForm -> {
                        recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                            System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                            System.out.printf("Field text: %s%n", fieldText);
                            System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                            System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                        });
                    })
                )
        );
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-string-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomForms} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-string-long-FormContentType-boolean-Duration
        File sourceFile = new File("{file_source_url}");
        String modelId = "{model_id}";
        boolean includeTextDetails = true;
        Flux<ByteBuffer> buffer = toFluxByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        formRecognizerAsyncClient.beginRecognizeCustomForms(buffer, modelId, sourceFile.length(),
            FormContentType.IMAGE_JPEG, includeTextDetails, Duration.ofSeconds(5))
            .subscribe(recognizePollingOperation ->
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedForms ->
                    recognizedForms.forEach(recognizedForm -> {
                        recognizedForm.getFields().forEach((fieldText, fieldValue) -> {
                            System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                            System.out.printf("Field text: %s%n", fieldText);
                            System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                            System.out.printf("Confidence score: %.2f%n", fieldValue.getConfidence());
                        });
                    })
                )
            );
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-string-long-FormContentType-boolean-Duration
    }

    // Recognize Content
    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContentFromUrl}
     */
    public void beginRecognizeContentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string
        String sourceFilePath = "{file_source_url}";
        formRecognizerAsyncClient.beginRecognizeContentFromUrl(sourceFilePath).subscribe(
            recognizePollingOperation ->
                recognizePollingOperation.getFinalResult().subscribe(layoutPageResults ->
                    layoutPageResults.forEach(recognizedForm -> {
                        System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                        System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                        // Table information
                        System.out.println("Recognized Tables: ");
                        recognizedForm.getTables().forEach(formTable ->
                            formTable.getCells().forEach(recognizedTableCell ->
                                System.out.printf("%s ", recognizedTableCell.getText())));
                    })
                ));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContentFromUrl} with options
     */
    public void beginRecognizeContentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-Duration
        String sourceFilePath = "{file_source_url}";
        formRecognizerAsyncClient.beginRecognizeContentFromUrl(sourceFilePath, Duration.ofSeconds(5)).subscribe(
            recognizePollingOperation ->
                recognizePollingOperation.getFinalResult().subscribe(layoutPageResults ->
                    layoutPageResults.forEach(recognizedForm -> {
                        System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                        System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                        // Table information
                        System.out.println("Recognized Tables: ");
                        recognizedForm.getTables().forEach(formTable ->
                            formTable.getCells().forEach(recognizedTableCell ->
                                System.out.printf("%s ", recognizedTableCell.getText())));
                    })
                ));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContent() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        formRecognizerAsyncClient.beginRecognizeContent(buffer, sourceFile.length(), FormContentType.APPLICATION_PDF)
            .subscribe(recognizePollingOperation ->
                recognizePollingOperation.getFinalResult().subscribe(layoutPageResults ->
                    layoutPageResults.forEach(recognizedForm -> {
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
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-FormContentType-Duration
        File sourceFile = new File("{file_source_url}");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        formRecognizerAsyncClient.beginRecognizeContent(buffer, sourceFile.length(), FormContentType.APPLICATION_PDF,
                Duration.ofSeconds(5)).subscribe(recognizePollingOperation ->
                recognizePollingOperation.getFinalResult().subscribe(layoutPageResults ->
                    layoutPageResults.forEach(recognizedForm -> {
                        System.out.printf("Page Angle: %s%n", recognizedForm.getTextAngle());
                        System.out.printf("Page Dimension unit: %s%n", recognizedForm.getUnit());
                        // Table information
                        System.out.println("Recognized Tables: ");
                        recognizedForm.getTables().forEach(formTable ->
                            formTable.getCells().forEach(recognizedTableCell ->
                                System.out.printf("%s ", recognizedTableCell.getText())));
                    })
                ));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-FormContentType-Duration
    }

    // Recognize Receipts
    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceiptsFromUrl}
     */
    public void beginRecognizeReceiptsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string
        String receiptUrl = "{file_source_url}";
        formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptUrl).subscribe(recognizePollingOperation -> {
            // if training polling operation completed, retrieve the final result.
            recognizePollingOperation.getFinalResult().subscribe(recognizedReceipts ->
                recognizedReceipts.forEach(recognizedReceipt -> {
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
                }));
        });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceiptsFromUrl} with options
     */
    public void beginRecognizeReceiptsFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-boolean-Duration
        String receiptUrl = "{file_source_url}";
        boolean includeTextDetails = true;
        formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptUrl, includeTextDetails, Duration.ofSeconds(5))
            .subscribe(recognizePollingOperation -> {
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedReceipts ->
                    recognizedReceipts.forEach(recognizedReceipt -> {
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
                    }));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-boolean-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceipts() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        Flux<ByteBuffer> buffer = toFluxByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        formRecognizerAsyncClient.beginRecognizeReceipts(buffer, sourceFile.length(), FormContentType.IMAGE_JPEG)
            .subscribe(recognizePollingOperation -> {
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedReceipts ->
                    recognizedReceipts.forEach(recognizedReceipt -> {
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
                    }));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType

    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginRecognizeReceiptsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType-boolean-Duration
        File sourceFile = new File("{file_source_url}");
        boolean includeTextDetails = true;
        Flux<ByteBuffer> buffer = toFluxByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        formRecognizerAsyncClient.beginRecognizeReceipts(buffer, sourceFile.length(), FormContentType.IMAGE_JPEG,
            includeTextDetails, Duration.ofSeconds(5)).subscribe(recognizePollingOperation -> {
                // if training polling operation completed, retrieve the final result.
                recognizePollingOperation.getFinalResult().subscribe(recognizedReceipts ->
                    recognizedReceipts.forEach(recognizedReceipt -> {
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
                    }));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType-boolean-Duration
    }
}
