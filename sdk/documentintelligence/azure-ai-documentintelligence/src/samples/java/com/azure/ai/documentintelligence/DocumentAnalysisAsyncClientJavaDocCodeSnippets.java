// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AnalyzeDocumentRequest;
import com.azure.ai.documentintelligence.models.AnalyzeResultOperation;
import com.azure.ai.documentintelligence.models.ClassifyDocumentRequest;
import com.azure.ai.documentintelligence.models.ContentFormat;
import com.azure.ai.documentintelligence.models.SplitMode;
import com.azure.ai.documentintelligence.models.StringIndexType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Code snippet for {@link DocumentIntelligenceAsyncClient}
 */
public class DocumentAnalysisAsyncClientJavaDocCodeSnippets {
    private final DocumentIntelligenceAsyncClient documentIntelligenceAsyncClient
        = new DocumentIntelligenceClientBuilder().buildAsyncClient();

    /**
     * Code snippet for creating a {@link DocumentIntelligenceAsyncClient}
     */
    public void createDocumentAnalysisAsyncClient() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentAnalysisAsyncClient.instantiation
        DocumentIntelligenceAsyncClient documentIntelligenceAsyncClient = new DocumentIntelligenceClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: com.azure.ai.documentintelligence.DocumentAnalysisAsyncClient.instantiation
    }

    /**
     * Code snippet for creating a {@link DocumentIntelligenceAsyncClient} with pipeline
     */
    public void createDocumentAnalysisAsyncClientWithPipeline() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentAnalysisAsyncClient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        DocumentIntelligenceAsyncClient documentIntelligenceAsyncClient = new DocumentIntelligenceClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .pipeline(pipeline)
            .buildAsyncClient();
        // END: com.azure.ai.documentintelligence.DocumentAnalysisAsyncClient.pipeline.instantiation
    }

    /**
     * Code snippet for {@link DocumentIntelligenceAsyncClient#beginAnalyzeDocument(String, String, String, StringIndexType, List, List, ContentFormat, AnalyzeDocumentRequest)}
     */
    public void beginAnalyzeDocumentFromUrl() {
        // BEGIN: com.azure.ai.documentintelligence.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#String-String-String-StringIndexType-List-List-ContentFormat-AnalyzeDocumentRequest
        String documentUrl = "{document_url}";
        String modelId = "{model_id}";
        documentIntelligenceAsyncClient.beginAnalyzeDocument(modelId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new AnalyzeDocumentRequest().setUrlSource(documentUrl))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(analyzeResult ->
                analyzeResult.getDocuments()
                    .forEach(document ->
                        document.getFields()
                            .forEach((key, documentField) -> {
                                System.out.printf("Field text: %s%n", key);
                                System.out.printf("Field value data content: %s%n", documentField.getContent());
                                System.out.printf("Confidence score: %.2f%n", documentField.getConfidence());
                            })));
        // END: com.azure.ai.documentintelligence.DocumentAnalysisAsyncClient.beginAnalyzeDocumentFromUrl#String-String-String-StringIndexType-List-List-ContentFormat-AnalyzeDocumentRequest
    }

    /**
     * Code snippet for
     * {@link DocumentIntelligenceAsyncClient#beginClassifyDocument(String, ClassifyDocumentRequest, StringIndexType, SplitMode)}
     * with options
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginClassifyDocument() throws IOException {
        // BEGIN: com.azure.ai.documentintelligence.DocumentAnalysisAsyncClient.beginClassifyDocument#String-ClassifyDocumentRequest-StringIndexType-SplitMode
        File document = new File("{local/file_path/fileName.jpg}");
        String classifierId = "{model_id}";

        // Utility method to convert input stream to Binary Data
        BinaryData buffer = BinaryData.fromStream(new ByteArrayInputStream(Files.readAllBytes(document.toPath())));

        documentIntelligenceAsyncClient.beginClassifyDocument(classifierId, new ClassifyDocumentRequest().setBase64Source(Files.readAllBytes(document.toPath())))
            // if polling operation completed, retrieve the final result.
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(analyzeResult -> {
                System.out.println(analyzeResult.getModelId());
                analyzeResult.getDocuments()
                    .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
            });
        // END: com.azure.ai.documentintelligence.DocumentAnalysisAsyncClient.beginClassifyDocument#String-ClassifyDocumentRequest-StringIndexType-SplitMode
    }
}
