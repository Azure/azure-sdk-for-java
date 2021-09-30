// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.Context;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Code snippet for {@link DocumentAnalysisClient}
 */
public class DocumentAnalysisClientJavaDocCodeSnippets {
    private final DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder().buildClient();

    /**
     * Code snippet for creating a {@link DocumentAnalysisClient}
     */
    public void createDocumentAnalysisClient() {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisClient.instantiation
        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.ai.formrecognizer.DocumentAnalysisClient.instantiation
    }

    /**
     * Code snippet for creating a {@link DocumentAnalysisClient} with pipeline
     */
    public void createDocumentAnalysisClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildClient();
        // END:  com.azure.ai.formrecognizer.DocumentAnalysisClient.pipeline.instantiation
    }


    // Analyze Custom Form

    /**
     * Code snippet for {@link DocumentAnalysisClient#beginAnalyzeDocumentFromUrl(String, String)}
     */
    public void beginAnalyzeDocumentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string
        String documentUrl = "{document_url}";
        String modelId = "{custom_trained_model_id}";

        documentAnalysisClient.beginAnalyzeDocumentFromUrl(modelId, documentUrl).getFinalResult()
            .getDocuments().stream()
            .map(AnalyzedDocument::getFields)
            .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                System.out.printf("Field text: %s%n", key);
                System.out.printf("Field value data content: %s%n", documentField.getContent());
                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
            }));

        // END: com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string
    }

    /**
     * Code snippet for {@link DocumentAnalysisClient#beginAnalyzeDocumentFromUrl(String, String, AnalyzeDocumentOptions, Context)}
     */
    public void beginAnalyzeDocumentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string-AnalyzeDocumentOptions-Context
        String analyzeFilePath = "{file_source_url}";
        String modelId = "{model_id}";

        documentAnalysisClient.beginAnalyzeDocumentFromUrl(modelId, analyzeFilePath,
                new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "3")), Context.NONE)
            .getFinalResult()
            .getDocuments().stream()
            .map(AnalyzedDocument::getFields)
            .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                System.out.printf("Field text: %s%n", key);
                System.out.printf("Field value data content: %s%n", documentField.getContent());
                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string-AnalyzeDocumentOptions-Context
    }

    /**
     * Code snippet for
     * {@link DocumentAnalysisClient#beginAnalyzeDocument(String, InputStream, long)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginAnalyzeDocument() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-InputStream-long
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        byte[] fileContent = Files.readAllBytes(form.toPath());
        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {

            documentAnalysisClient.beginAnalyzeDocument(modelId, targetStream, form.length())
                .getFinalResult()
                .getDocuments().stream()
                .map(AnalyzedDocument::getFields)
                .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                    System.out.printf("Field text: %s%n", key);
                    System.out.printf("Field value data content: %s%n", documentField.getContent());
                    System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
                }));
        }
        // END: com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-InputStream-long
    }

    /**
     * Code snippet for
     * {@link DocumentAnalysisClient#beginAnalyzeDocument(String, InputStream, long, AnalyzeDocumentOptions, Context)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginAnalyzeDocumentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-InputStream-long-AnalyzeDocumentOptions-Context
        File form = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        byte[] fileContent = Files.readAllBytes(form.toPath());

        try (InputStream targetStream = new ByteArrayInputStream(fileContent)) {
            documentAnalysisClient.beginAnalyzeDocument(modelId, targetStream, form.length(),
                    new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "3")), Context.NONE)
                .getFinalResult()
                .getDocuments().stream()
                .map(AnalyzedDocument::getFields)
                .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                    System.out.printf("Field text: %s%n", key);
                    System.out.printf("Field value data content: %s%n", documentField.getContent());
                    System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
                }));
        }
        // END: com.azure.ai.formrecognizer.DocumentAnalysisClient.beginAnalyzeDocument#string-InputStream-long-AnalyzeDocumentOptions-Context
    }
}
