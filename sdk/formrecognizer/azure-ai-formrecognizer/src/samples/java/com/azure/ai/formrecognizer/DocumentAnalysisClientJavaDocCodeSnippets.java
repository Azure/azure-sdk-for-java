// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

import java.io.File;
import java.io.IOException;
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
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.instantiation
        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.instantiation
    }

    /**
     * Code snippet for creating a {@link DocumentAnalysisClient} with pipeline
     */
    public void createDocumentAnalysisClientWithPipeline() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildClient();
        // END:  com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.pipeline.instantiation
    }


    // Analyze Custom Form

    /**
     * Code snippet for {@link DocumentAnalysisClient#beginAnalyzeDocumentFromUrl(String, String)}
     */
    public void beginAnalyzeDocumentFromUrl() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string
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

        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string
    }

    /**
     * Code snippet for {@link DocumentAnalysisClient#beginAnalyzeDocumentFromUrl(String, String, AnalyzeDocumentOptions, Context)}
     */
    public void beginAnalyzeDocumentFromUrlWithOptions() {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string-Options-Context
        String documentUrl = "{file_source_url}";
        String modelId = "{model_id}";

        documentAnalysisClient.beginAnalyzeDocumentFromUrl(modelId, documentUrl,
                new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "3")), Context.NONE)
            .getFinalResult()
            .getDocuments().stream()
            .map(AnalyzedDocument::getFields)
            .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                System.out.printf("Field text: %s%n", key);
                System.out.printf("Field value data content: %s%n", documentField.getContent());
                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocumentFromUrl#string-string-Options-Context
    }

    /**
     * Code snippet for
     * {@link DocumentAnalysisClient#beginAnalyzeDocument(String, BinaryData)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginAnalyzeDocument() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData
        File document = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        byte[] fileContent = Files.readAllBytes(document.toPath());

        documentAnalysisClient.beginAnalyzeDocument(modelId, BinaryData.fromBytes(fileContent))
            .getFinalResult()
            .getDocuments().stream()
            .map(AnalyzedDocument::getFields)
            .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                System.out.printf("Field text: %s%n", key);
                System.out.printf("Field value data content: %s%n", documentField.getContent());
                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
            }));
    }
    // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData


    /**
     * Code snippet for
     * {@link DocumentAnalysisClient#beginAnalyzeDocument(String, BinaryData, AnalyzeDocumentOptions, Context)} with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginAnalyzeDocumentWithOptions() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData-Options-Context
        File document = new File("{local/file_path/fileName.jpg}");
        String modelId = "{custom_trained_model_id}";
        byte[] fileContent = Files.readAllBytes(document.toPath());

        documentAnalysisClient.beginAnalyzeDocument(modelId, BinaryData.fromBytes(fileContent),
                new AnalyzeDocumentOptions().setPages(Arrays.asList("1", "3")), Context.NONE)
            .getFinalResult()
            .getDocuments().stream()
            .map(AnalyzedDocument::getFields)
            .forEach(documentFieldMap -> documentFieldMap.forEach((key, documentField) -> {
                System.out.printf("Field text: %s%n", key);
                System.out.printf("Field value data content: %s%n", documentField.getContent());
                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
            }));
        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginAnalyzeDocument#string-BinaryData-Options-Context
    }

    /**
     * Code snippet for
     * {@link DocumentAnalysisClient#beginClassifyDocument(String, BinaryData, Context)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginClassifyDocument() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData
        File document = new File("{local/file_path/fileName.jpg}");
        String classifierId = "{custom_trained_classifier_id}";
        byte[] fileContent = Files.readAllBytes(document.toPath());

        documentAnalysisClient.beginClassifyDocument(classifierId, BinaryData.fromBytes(fileContent), Context.NONE)
            .getFinalResult()
            .getDocuments()
            .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData
    }

    /**
     * Code snippet for
     * {@link DocumentAnalysisClient#beginClassifyDocument(String, BinaryData, Context)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginClassifyDocumentFromUrl() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string-Context
        String documentUrl = "{file_source_url}";
        String classifierId = "{custom_trained_classifier_id}";

        documentAnalysisClient.beginClassifyDocumentFromUrl(classifierId, documentUrl, Context.NONE)
            .getFinalResult()
            .getDocuments()
            .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string-Context
    }
}
