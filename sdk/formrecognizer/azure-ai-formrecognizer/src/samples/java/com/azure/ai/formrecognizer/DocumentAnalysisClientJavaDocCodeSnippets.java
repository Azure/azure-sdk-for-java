// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisAsyncClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClient;
import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeDocumentOptions;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLanguage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentStyle;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTable;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Code snippet for {@link DocumentAnalysisClient}
 */
public class DocumentAnalysisClientJavaDocCodeSnippets {
    private final DocumentAnalysisClient documentAnalysisClient = new DocumentAnalysisClientBuilder().buildClient();
    private final DocumentModelAdministrationClient documentModelAdminClient =
        new DocumentModelAdministrationClientBuilder().buildClient();

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

    public void useAadAsyncClient() {
        // BEGIN: readme-sample-createDocumentAnalysisAsyncClientWithAAD
        DocumentAnalysisAsyncClient documentAnalysisAsyncClient = new DocumentAnalysisClientBuilder()
            .endpoint("{endpoint}")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: readme-sample-createDocumentAnalysisAsyncClientWithAAD
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
    public void beginClassifyDocumentContext() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData-Context
        File document = new File("{local/file_path/fileName.jpg}");
        String classifierId = "{custom_trained_classifier_id}";
        byte[] fileContent = Files.readAllBytes(document.toPath());

        documentAnalysisClient.beginClassifyDocument(classifierId, BinaryData.fromBytes(fileContent), Context.NONE)
            .getFinalResult()
            .getDocuments()
            .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData-Context
    }

    /**
     * Code snippet for
     * {@link DocumentAnalysisClient#beginClassifyDocument(String, BinaryData)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginClassifyDocument() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocument#string-BinaryData
        File document = new File("{local/file_path/fileName.jpg}");
        String classifierId = "{custom_trained_classifier_id}";
        byte[] fileContent = Files.readAllBytes(document.toPath());

        documentAnalysisClient.beginClassifyDocument(classifierId, BinaryData.fromBytes(fileContent))
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
    public void beginClassifyDocumentFromUrlContext() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string-Context
        String documentUrl = "{file_source_url}";
        String classifierId = "{custom_trained_classifier_id}";

        documentAnalysisClient.beginClassifyDocumentFromUrl(classifierId, documentUrl, Context.NONE)
            .getFinalResult()
            .getDocuments()
            .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string-Context
    }

    /**
     * Code snippet for
     * {@link DocumentAnalysisClient#beginClassifyDocument(String, BinaryData, Context)}
     *
     * @throws IOException Exception thrown when there is an error in reading all the bytes from the File.
     */
    public void beginClassifyDocumentFromUrl() throws IOException {
        // BEGIN: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string
        String documentUrl = "{file_source_url}";
        String classifierId = "{custom_trained_classifier_id}";

        documentAnalysisClient.beginClassifyDocumentFromUrl(classifierId, documentUrl)
            .getFinalResult()
            .getDocuments()
            .forEach(analyzedDocument -> System.out.printf("Doc Type: %s%n", analyzedDocument.getDocType()));
        // END: com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient.beginClassifyDocumentFromUrl#string-string
    }

    /**
     * Code snippet for analyzing data using prebuilt read model.
     */
    public void prebuiltRead() {
        // BEGIN: readme-sample-prebuiltRead-url
        String documentUrl = "documentUrl";

        SyncPoller<OperationResult, AnalyzeResult> analyzeResultPoller =
            documentAnalysisClient.beginAnalyzeDocumentFromUrl("prebuilt-read", documentUrl);
        AnalyzeResult analyzeResult = analyzeResultPoller.getFinalResult();

        System.out.println("Detected Languages: ");
        for (DocumentLanguage language : analyzeResult.getLanguages()) {
            System.out.printf("Found language with locale %s and confidence %.2f",
                language.getLocale(),
                language.getConfidence());
        }

        System.out.println("Detected Styles: ");
        for (DocumentStyle style: analyzeResult.getStyles()) {
            if (style.isHandwritten()) {
                System.out.printf("Found handwritten content %s with confidence %.2f",
                    style.getSpans().stream().map(span -> analyzeResult.getContent()
                        .substring(span.getOffset(), span.getLength())),
                    style.getConfidence());
            }
        }

        // pages
        analyzeResult.getPages().forEach(documentPage -> {
            System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                documentPage.getWidth(),
                documentPage.getHeight(),
                documentPage.getUnit());

            // lines
            documentPage.getLines().forEach(documentLine ->
                System.out.printf("Line '%s' is within a bounding polygon %s.%n",
                    documentLine.getContent(),
                    documentLine.getBoundingPolygon().stream().map(point -> String.format("[%.2f, %.2f]", point.getX(),
                        point.getY())).collect(Collectors.joining(", "))));
        });
        // END: readme-sample-prebuiltRead-url
    }

    /**
     * Code snippet for analyzing data using prebuilt read model.
     */
    public void prebuiltReadFile() {
        // BEGIN: readme-sample-prebuiltRead-file
        File document = new File("{local/file_path/fileName.jpg}");
        SyncPoller<OperationResult, AnalyzeResult> analyzeResultPoller =
            documentAnalysisClient.beginAnalyzeDocument("prebuilt-read",
                BinaryData.fromFile(document.toPath(),
                    (int) document.length()));
        AnalyzeResult analyzeResult = analyzeResultPoller.getFinalResult();

        System.out.println("Detected Languages: ");
        for (DocumentLanguage language : analyzeResult.getLanguages()) {
            System.out.printf("Found language with locale %s and confidence %.2f",
                language.getLocale(),
                language.getConfidence());
        }

        System.out.println("Detected Styles: ");
        for (DocumentStyle style: analyzeResult.getStyles()) {
            if (style.isHandwritten()) {
                System.out.printf("Found handwritten content %s with confidence %.2f",
                    style.getSpans().stream().map(span -> analyzeResult.getContent()
                        .substring(span.getOffset(), span.getLength())),
                    style.getConfidence());
            }
        }

        // pages
        analyzeResult.getPages().forEach(documentPage -> {
            System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                documentPage.getWidth(),
                documentPage.getHeight(),
                documentPage.getUnit());

            // lines
            documentPage.getLines().forEach(documentLine ->
                System.out.printf("Line '%s' is within a bounding polygon %s.%n",
                    documentLine.getContent(),
                    documentLine.getBoundingPolygon().stream().map(point -> String.format("[%.2f, %.2f]", point.getX(),
                        point.getY())).collect(Collectors.joining(", "))));
        });
        // END: readme-sample-prebuiltRead-file
    }

    private void buildAndAnalyzeCustomDocument() {
        // BEGIN: readme-sample-build-analyze
        String blobContainerUrl = "{SAS_URL_of_your_container_in_blob_storage}";
        // The shared access signature (SAS) Url of your Azure Blob Storage container with your custom documents.
        String prefix = "{blob_name_prefix}}";
        // Build custom document analysis model
        SyncPoller<OperationResult, DocumentModelDetails> buildOperationPoller =
            documentModelAdminClient.beginBuildDocumentModel(blobContainerUrl,
                DocumentModelBuildMode.TEMPLATE,
                prefix,
                new BuildDocumentModelOptions().setModelId("my-custom-built-model").setDescription("model desc"),
                Context.NONE);

        DocumentModelDetails customBuildModel = buildOperationPoller.getFinalResult();

        // analyze using custom-built model
        String modelId = customBuildModel.getModelId();
        String documentUrl = "documentUrl";
        SyncPoller<OperationResult, AnalyzeResult> analyzeDocumentPoller =
            documentAnalysisClient.beginAnalyzeDocumentFromUrl(modelId, documentUrl);

        AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult();

        for (int i = 0; i < analyzeResult.getDocuments().size(); i++) {
            final AnalyzedDocument analyzedDocument = analyzeResult.getDocuments().get(i);
            System.out.printf("----------- Analyzing custom document %d -----------%n", i);
            System.out.printf("Analyzed document has doc type %s with confidence : %.2f%n",
                analyzedDocument.getDocType(), analyzedDocument.getConfidence());
            analyzedDocument.getFields().forEach((key, documentField) -> {
                System.out.printf("Document Field content: %s%n", documentField.getContent());
                System.out.printf("Document Field confidence: %.2f%n", documentField.getConfidence());
                System.out.printf("Document Field Type: %s%n", documentField.getType());
                System.out.printf("Document Field found within bounding region: %s%n",
                    documentField.getBoundingRegions().toString());
            });
        }

        analyzeResult.getPages().forEach(documentPage -> {
            System.out.printf("Page has width: %.2f and height: %.2f, measured with unit: %s%n",
                documentPage.getWidth(),
                documentPage.getHeight(),
                documentPage.getUnit());

            // lines
            documentPage.getLines().forEach(documentLine ->
                System.out.printf("Line '%s' is within a bounding box %s.%n",
                    documentLine.getContent(),
                    documentLine.getBoundingPolygon().toString()));

            // words
            documentPage.getWords().forEach(documentWord ->
                System.out.printf("Word '%s' has a confidence score of %.2f.%n",
                    documentWord.getContent(),
                    documentWord.getConfidence()));
        });

        // tables
        List<DocumentTable> tables = analyzeResult.getTables();
        for (int i = 0; i < tables.size(); i++) {
            DocumentTable documentTable = tables.get(i);
            System.out.printf("Table %d has %d rows and %d columns.%n", i, documentTable.getRowCount(),
                documentTable.getColumnCount());
            documentTable.getCells().forEach(documentTableCell -> {
                System.out.printf("Cell '%s', has row index %d and column index %d.%n",
                    documentTableCell.getContent(),
                    documentTableCell.getRowIndex(), documentTableCell.getColumnIndex());
            });
            System.out.println();
        }
        // END: readme-sample-build-analyze
    }
}
