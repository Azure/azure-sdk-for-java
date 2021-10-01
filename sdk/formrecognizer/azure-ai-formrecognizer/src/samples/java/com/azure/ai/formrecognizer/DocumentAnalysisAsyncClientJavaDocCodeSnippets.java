// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.util.Utility;
import com.azure.ai.formrecognizer.models.AnalyzeDocumentOptions;
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
import java.util.Arrays;

/**
 * Code snippet for {@link DocumentAnalysisAsyncClient}
 */
public class DocumentAnalysisAsyncClientJavaDocCodeSnippets {
    private final DocumentAnalysisAsyncClient documentAnalysisAsyncClient
        = new DocumentAnalysisClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link DocumentAnalysisAsyncClient}
     */
    public void createDocumentAnalysisAsyncClient() {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.instantiation
        DocumentAnalysisAsyncClient documentAnalysisAsyncClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.instantiation
    }

    /**
     * Code snippet for creating a {@link DocumentAnalysisAsyncClient} with pipeline
     */
    public void createDocumentAnalysisAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentAnalysisAsyncClient documentAnalysisAsyncClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildAsyncClient();
        // END: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for {@link DocumentAnalysisAsyncClient#beginAnalyzeDocumentFromUrl(String, String)}
     */
    public void beginAnalyzeDocumentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#string-string
        String documentUrl = "{document_url}";
        String modelId = "{model_id}";
        documentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl(modelId, documentUrl)
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(analyzeResult ->
                analyzeResult.getDocuments()
                    .stream()
                    .forEach(document ->
                        document.getFields()
                            .forEach((key, documentField) -> {
                                System.out.printf("Field text: %s%n", key);
                                System.out.printf("Field value data content: %s%n", documentField.getContent());
                                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
                            })));
        // END: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#string-string
    }

    /**
     * Code snippet for {@link DocumentAnalysisAsyncClient#beginAnalyzeDocumentFromUrl(String, String, AnalyzeDocumentOptions)}
     */
    public void beginAnalyzeDocumentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#string-string-AnalyzeDocumentOptions
        String documentUrl = "{document_url}";
        // analyze a receipt using prebuilt model
        String modelId = "prebuilt-receipt";

        documentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl(modelId, documentUrl,
                new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "3")))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(analyzeResult -> {
                System.out.println(analyzeResult.getModelId());
                analyzeResult.getDocuments()
                    .stream()
                    .forEach(document ->
                        document.getFields()
                            .forEach((key, documentField) -> {
                                System.out.printf("Field text: %s%n", key);
                                System.out.printf("Field value data content: %s%n", documentField.getContent());
                                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
                            }));
            });

        // END: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#string-string-AnalyzeDocumentOptions
    }

    /**
     * Code snippet for {@link DocumentAnalysisAsyncClient#beginAnalyzeDocument(String, Flux, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginAnalyzeDocument() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocument#string-Flux-long
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{model_id}";
        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer =
            Utility.toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        documentAnalysisAsyncClient.beginAnalyzeDocument(modelId, buffer, form.length())
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(analyzeResult ->
                analyzeResult.getDocuments()
                    .stream()
                    .forEach(document ->
                        document.getFields()
                            .forEach((key, documentField) -> {
                                System.out.printf("Field text: %s%n", key);
                                System.out.printf("Field value data content: %s%n", documentField.getContent());
                                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
                            })));
        // END: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocument#string-Flux-long
    }

    /**
     * Code snippet for
     * {@link DocumentAnalysisAsyncClient#beginAnalyzeDocument(String, Flux, long, AnalyzeDocumentOptions)}
     * with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginAnalyzeDocumentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocument#string-Flux-long-AnalyzeDocumentOptions
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{model_id}";

        // Utility method to convert input stream to Byte buffer
        Flux<ByteBuffer> buffer =
            Utility.toFluxByteBuffer(new ByteArrayInputStream(Files.readAllBytes(form.toPath())));

        documentAnalysisAsyncClient.beginAnalyzeDocument(modelId, buffer, form.length(),
                new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "3")))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(analyzeResult -> {
                System.out.println(analyzeResult.getModelId());
                analyzeResult.getDocuments()
                    .stream()
                    .forEach(document ->
                        document.getFields()
                            .forEach((key, documentField) -> {
                                System.out.printf("Field text: %s%n", key);
                                System.out.printf("Field value data content: %s%n", documentField.getContent());
                                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
                            }));
            });
        // END: com.azure.ai.formrecognizer.DocumentAnalysisAsyncClient.beginAnalyzeDocument#string-Flux-long-AnalyzeDocumentOptions
    }
}
