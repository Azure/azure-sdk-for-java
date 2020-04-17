// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.OperationResult;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.IterableStream;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Optional;

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
            .apiKey(new AzureKeyCredential("{api_key}"))
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
            .apiKey(new AzureKeyCredential("{api_key}"))
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
            trainingOperationResponse ->
                // training completed successfully, retrieving final result.
                trainingOperationResponse.getFinalResult().subscribe(recognizedForms ->
                    recognizedForms.forEach(extractedForm -> {
                        extractedForm.getFields().forEach((fieldText, fieldValue) -> {
                            System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                            System.out.printf("Field text: %s%n", fieldText);
                            System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                        });

                        // Page Information
                        extractedForm.getPages().forEach(formPage -> {
                            System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                            System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                            System.out.println();
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

        formRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl(analyzeFilePath, modelId, true,
            Duration.ofSeconds(5)).subscribe(trainingOperationResponse ->
                // training completed successfully, retrieving final result.
                trainingOperationResponse.getFinalResult().subscribe(recognizedForms ->
                    recognizedForms.forEach(extractedForm -> {
                        extractedForm.getFields().forEach((fieldText, fieldValue) -> {
                            System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                            System.out.printf("Field text: %s%n", fieldText);
                            System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                        });

                        // Page Information
                        extractedForm.getPages().forEach(formPage -> {
                            System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                            System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                            System.out.println();
                        });
                    })
                )
        );
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomFormsFromUrl#string-string-boolean-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomForms}
     */
    public void beginRecognizeCustomForms() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-string-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        String modelId = "{model_id}";
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        formRecognizerAsyncClient.beginRecognizeCustomForms(buffer, modelId, sourceFile.length(),
            FormContentType.IMAGE_JPEG).subscribe(trainingOperationResponse ->
                // training completed successfully, retrieving final result.
                trainingOperationResponse.getFinalResult().subscribe(recognizedForms ->
                    recognizedForms.forEach(extractedForm -> {
                        extractedForm.getFields().forEach((fieldText, fieldValue) -> {
                            System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                            System.out.printf("Field text: %s%n", fieldText);
                            System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                        });

                        // Page Information
                        extractedForm.getPages().forEach(formPage -> {
                            System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                            System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                            System.out.println();
                        });
                    })
                )
        );
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-string-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeCustomForms} with options
     */
    public void beginRecognizeCustomFormsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeCustomForms#Flux-string-long-FormContentType-boolean-Duration
        File sourceFile = new File("{file_source_url}");
        String modelId = "{model_id}";
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        formRecognizerAsyncClient.beginRecognizeCustomForms(buffer, modelId, sourceFile.length(),
            FormContentType.IMAGE_JPEG, true, Duration.ofSeconds(5))
            .subscribe(trainingOperationResponse ->
                // training completed successfully, retrieving final result.
                trainingOperationResponse.getFinalResult().subscribe(recognizedForms ->
                    recognizedForms.forEach(extractedForm -> {
                        extractedForm.getFields().forEach((fieldText, fieldValue) -> {
                            System.out.printf("Page number: %s%n", fieldValue.getPageNumber());
                            System.out.printf("Field text: %s%n", fieldText);
                            System.out.printf("Field value: %s%n", fieldValue.getFieldValue());
                        });

                        // Page Information
                        extractedForm.getPages().forEach(formPage -> {
                            System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                            System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                            System.out.println();
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
        formRecognizerAsyncClient.beginRecognizeContentFromUrl(sourceFilePath).subscribe(trainingOperationResponse ->
            trainingOperationResponse.getFinalResult().subscribe(layoutPageResults ->
                layoutPageResults.forEach(formPage -> {
                    System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                    System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                    // Table information
                    System.out.println("Recognized Tables: ");
                    formPage.getTables().forEach(formTable -> {
                        for (int i = 0; i < formTable.getRowCount(); i++) {
                            for (int j = 0; j < formTable.getColumnCount(); j++) {
                                int finalJ = j;
                                int finalI = i;
                                Optional<FormTableCell> optionalFormTableCell =
                                    formTable.getCells().stream().filter(formTableCell ->
                                        formTableCell.getRowIndex() == finalI
                                            && formTableCell.getColumnIndex() == finalJ)
                                        .findFirst();
                                FormTableCell recognizedTableCell = optionalFormTableCell.get();
                                if (recognizedTableCell.isHeader()) {
                                    System.out.println(recognizedTableCell.getText());
                                } else {
                                    System.out.printf("%s || ", recognizedTableCell.getText());
                                }
                            }}
                    });
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
            trainingOperationResponse ->
                trainingOperationResponse.getFinalResult().subscribe(layoutPageResults ->
                    layoutPageResults.forEach(formPage -> {
                        System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                        System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                        // Table information
                        System.out.println("Recognized Tables: ");
                        formPage.getTables().forEach(formTable -> {
                            for (int i = 0; i < formTable.getRowCount(); i++) {
                                for (int j = 0; j < formTable.getColumnCount(); j++) {
                                    int finalJ = j;
                                    int finalI = i;
                                    Optional<FormTableCell> optionalFormTableCell =
                                        formTable.getCells().stream().filter(formTableCell ->
                                            formTableCell.getRowIndex() == finalI
                                                && formTableCell.getColumnIndex() == finalJ)
                                            .findFirst();
                                    FormTableCell recognizedTableCell = optionalFormTableCell.get();
                                    if (recognizedTableCell.isHeader()) {
                                        System.out.println(recognizedTableCell.getText());
                                    } else {
                                        System.out.printf("%s || ", recognizedTableCell.getText());
                                    }
                                }}
                        });
                    })
                ));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContentFromUrl#string-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent}
     */
    public void beginRecognizeContent() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        formRecognizerAsyncClient.beginRecognizeContent(buffer, sourceFile.length(), FormContentType.APPLICATION_PDF)
            .subscribe(trainingOperationResponse ->
                trainingOperationResponse.getFinalResult().subscribe(layoutPageResults ->
                    layoutPageResults.forEach(formPage -> {
                        System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                        System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                        // Table information
                        System.out.println("Recognized Tables: ");
                        formPage.getTables().forEach(formTable -> {
                            for (int i = 0; i < formTable.getRowCount(); i++) {
                                for (int j = 0; j < formTable.getColumnCount(); j++) {
                                    int finalJ = j;
                                    int finalI = i;
                                    Optional<FormTableCell> optionalFormTableCell =
                                        formTable.getCells().stream().filter(formTableCell ->
                                            formTableCell.getRowIndex() == finalI
                                                && formTableCell.getColumnIndex() == finalJ)
                                            .findFirst();
                                    FormTableCell recognizedTableCell = optionalFormTableCell.get();
                                    if (recognizedTableCell.isHeader()) {
                                        System.out.println(recognizedTableCell.getText());
                                    } else {
                                        System.out.printf("%s || ", recognizedTableCell.getText());
                                    }
                                }}
                        });
                    })
            ));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-long-FormContentType
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeContent} with options
     */
    public void beginRecognizeContentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-FormContentType-long-Duration
        File sourceFile = new File("{file_source_url}");
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));

        formRecognizerAsyncClient.beginRecognizeContent(buffer, FormContentType.APPLICATION_PDF, sourceFile.length(),
            Duration.ofSeconds(5)).subscribe(trainingOperationResponse ->
                trainingOperationResponse.getFinalResult().subscribe(layoutPageResults ->
                    layoutPageResults.forEach(formPage -> {
                        System.out.printf("Page Angle: %s%n", formPage.getTextAngle());
                        System.out.printf("Page Dimension unit: %s%n", formPage.getUnit());
                        // Table information
                        System.out.println("Recognized Tables: ");
                        formPage.getTables().forEach(formTable -> {
                            for (int i = 0; i < formTable.getRowCount(); i++) {
                                for (int j = 0; j < formTable.getColumnCount(); j++) {
                                    int finalJ = j;
                                    int finalI = i;
                                    Optional<FormTableCell> optionalFormTableCell =
                                        formTable.getCells().stream().filter(formTableCell ->
                                            formTableCell.getRowIndex() == finalI
                                                && formTableCell.getColumnIndex() == finalJ)
                                            .findFirst();
                                    FormTableCell recognizedTableCell = optionalFormTableCell.get();
                                    if (recognizedTableCell.isHeader()) {
                                        System.out.println(recognizedTableCell.getText());
                                    } else {
                                        System.out.printf("%s || ", recognizedTableCell.getText());
                                    }
                                }}
                        });
                    })
                ));
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeContent#Flux-FormContentType-long-Duration
    }

    // Recognize Receipts
    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceiptsFromUrl}
     */
    public void beginRecognizeReceiptsFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string
        String receiptUrl = "{file_source_url}";
        formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptUrl).subscribe(trainingOperationResponse -> {
            System.out.println("Polling completed successfully");
            // training completed successfully, retrieving final result.
            trainingOperationResponse.getFinalResult().subscribe(recognizedReceipts ->
                recognizedReceipts.forEach(recognizedReceipt -> {
                    USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
                    System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
                    System.out.printf("Merchant Name %s%n", usReceipt.getMerchantName().getName());
                    System.out.printf("Merchant Name Value: %s%n", usReceipt.getMerchantName().getFieldValue());
                    System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
                    System.out.printf("Merchant Address Value: %s%n", usReceipt.getMerchantAddress().getFieldValue());
                    System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
                    System.out.printf("Total Value: %s%n", usReceipt.getTotal().getFieldValue());
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
        formRecognizerAsyncClient.beginRecognizeReceiptsFromUrl(receiptUrl, true, Duration.ofSeconds(5))
            .subscribe(trainingOperationResponse -> {
                System.out.println("Polling completed successfully");
                // training completed successfully, retrieving final result.
                trainingOperationResponse.getFinalResult().subscribe(recognizedReceipts ->
                    recognizedReceipts.forEach(recognizedReceipt -> {
                        USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
                        System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
                        System.out.printf("Merchant Name %s%n", usReceipt.getMerchantName().getName());
                        System.out.printf("Merchant Name Value: %s%n", usReceipt.getMerchantName().getFieldValue());
                        System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
                        System.out.printf("Merchant Address Value: %s%n", usReceipt.getMerchantAddress().getFieldValue());
                        System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
                        System.out.printf("Total Value: %s%n", usReceipt.getTotal().getFieldValue());
                    }));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceiptsFromUrl#string-boolean-Duration
    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts}
     */
    public void beginRecognizeReceipts() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType
        File sourceFile = new File("{file_source_url}");
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        formRecognizerAsyncClient.beginRecognizeReceipts(buffer, sourceFile.length(), FormContentType.IMAGE_JPEG)
            .subscribe(trainingOperationResponse -> {
                System.out.println("Polling completed successfully");
                // training completed successfully, retrieving final result.
                trainingOperationResponse.getFinalResult().subscribe(recognizedReceipts ->
                    recognizedReceipts.forEach(recognizedReceipt -> {
                        USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
                        System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
                        System.out.printf("Merchant Name %s%n", usReceipt.getMerchantName().getName());
                        System.out.printf("Merchant Name Value: %s%n", usReceipt.getMerchantName().getFieldValue());
                        System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
                        System.out.printf("Merchant Address Value: %s%n", usReceipt.getMerchantAddress().getFieldValue());
                        System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
                        System.out.printf("Total Value: %s%n", usReceipt.getTotal().getFieldValue());
                    }));
            });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType

    }

    /**
     * Code snippet for {@link FormRecognizerAsyncClient#beginRecognizeReceipts} with options
     */
    public void beginRecognizeReceiptsWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType-boolean-Duration
        File sourceFile = new File("{file_source_url}");
        Flux<ByteBuffer> buffer = Utility.convertStreamToByteBuffer(
            new ByteArrayInputStream(Files.readAllBytes(sourceFile.toPath())));
        formRecognizerAsyncClient.beginRecognizeReceipts(buffer, sourceFile.length(), FormContentType.IMAGE_JPEG, true,
            Duration.ofSeconds(5)).subscribe(trainingOperationResponse -> {
            System.out.println("Polling completed successfully");
            // training completed successfully, retrieving final result.
            trainingOperationResponse.getFinalResult().subscribe(recognizedReceipts ->
                recognizedReceipts.forEach(recognizedReceipt -> {
                    USReceipt usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
                    System.out.printf("Page Number: %s%n", usReceipt.getMerchantName().getPageNumber());
                    System.out.printf("Merchant Name %s%n", usReceipt.getMerchantName().getName());
                    System.out.printf("Merchant Name Value: %s%n", usReceipt.getMerchantName().getFieldValue());
                    System.out.printf("Merchant Address %s%n", usReceipt.getMerchantAddress().getName());
                    System.out.printf("Merchant Address Value: %s%n", usReceipt.getMerchantAddress().getFieldValue());
                    System.out.printf("Total: %s%n", usReceipt.getTotal().getName());
                    System.out.printf("Total Value: %s%n", usReceipt.getTotal().getFieldValue());
                }));
        });
        // END: com.azure.ai.formrecognizer.FormRecognizerAsyncClient.beginRecognizeReceipts#Flux-long-FormContentType-boolean-Duration
    }
}
