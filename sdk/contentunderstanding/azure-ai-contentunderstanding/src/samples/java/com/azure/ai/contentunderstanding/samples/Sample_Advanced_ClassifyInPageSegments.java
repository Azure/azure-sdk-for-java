// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisContent;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentCategoryDefinition;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentContentSegment;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Demonstrates classifying multiple documents that share a page.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>Segmentation normally separates documents only at page boundaries. To allow boundaries within a page, enable
 * both {@link ContentAnalyzerConfig#setSegmentEnabled(Boolean)} and
 * {@link ContentAnalyzerConfig#setAllowInPageSegments(Boolean)}.</p>
 *
 * <p>In-page segmentation is useful when distinct documents can appear on one page, such as individual supplemental
 * statements that are often appended after the main form in a K-1 tax package. Both segments can report the same page
 * range while their source expressions and spans locate each document within that page. Segment confidence combines
 * the service's segmentation and classification confidence. See the
 * <a href="https://learn.microsoft.com/azure/ai-services/content-understanding/concepts/classifier">Content
 * Understanding classifier overview</a> for supported scenarios.</p>
 *
 * <p>The bundled sample is a simplified synthetic one-page PDF containing an invoice in the upper half and an account
 * statement in the lower half.</p>
 *
 * <p>Configure model deployment defaults before running this sample; see {@link Sample00_UpdateDefaults}.</p>
 */
public class Sample_Advanced_ClassifyInPageSegments {
    public static void main(String[] args) throws Exception {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");
        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW);
        ContentUnderstandingClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }
        String analyzerId = "in-page-classifier-" + UUID.randomUUID().toString().replace("-", "");

        SyncPoller<ContentAnalyzerOperationStatus, ContentAnalyzer> createPoller
            = client.beginCreateAnalyzer(analyzerId, createClassifier(), true);
        LongRunningOperationStatus createStatus = createPoller.waitForCompletion().getStatus();
        if (createStatus != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            requireSuccessfulResult(createStatus, createPoller::getFinalResult, "In-page classifier creation");
        }
        try {
            requireSuccessfulResult(createStatus, createPoller::getFinalResult, "In-page classifier creation");
            BinaryData data = BinaryData.fromBytes(
                Files.readAllBytes(Paths.get("src/samples/resources/mixed_financial_docs_in_page.pdf")));
            SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> analyzePoller
                = client.beginAnalyzeBinary(analyzerId, data);
            AnalysisResult result = requireSuccessfulResult(analyzePoller.waitForCompletion().getStatus(),
                analyzePoller::getFinalResult, "In-page classification");
            printSegments(getDocumentContent(result));
        } finally {
            client.deleteAnalyzer(analyzerId);
        }
    }

    static ContentAnalyzer createClassifier() {
        Map<String, ContentCategoryDefinition> categories = new HashMap<>();
        categories.put("Invoice",
            new ContentCategoryDefinition().setDescription(
                "An invoice requesting payment for goods or services, with line items, totals, and payment terms."));
        categories.put("BankStatement", new ContentCategoryDefinition()
            .setDescription(
                "A bank account statement listing balances, deposits, withdrawals, fees, and transactions."));
        ContentAnalyzerConfig config = new ContentAnalyzerConfig().setReturnDetails(true)
            .setSegmentEnabled(true).setAllowInPageSegments(true)
            .setEstimateFieldSourceAndConfidence(true).setContentCategories(categories);
        String model = SampleModelConfiguration.getCompletionModel();
        Map<String, String> models = new HashMap<>();
        models.put("completion", model);
        return new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
            .setDescription("Classify financial documents that may share a page.")
            .setConfig(config).setModels(models);
    }

    static <T> T requireSuccessfulResult(LongRunningOperationStatus status, Supplier<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            throw new IllegalStateException(operationName + " completed unsuccessfully with status: " + status);
        }
        T result = finalResult.get();
        if (result == null) {
            throw new IllegalStateException(operationName + " completed without a final result.");
        }
        return result;
    }

    static DocumentContent getDocumentContent(AnalysisResult result) {
        if (result.getContents() != null) {
            for (AnalysisContent content : result.getContents()) {
                if (content instanceof DocumentContent) {
                    return (DocumentContent) content;
                }
            }
        }
        throw new IllegalStateException("In-page classification did not return document content.");
    }

    static void printSegments(DocumentContent document) {
        if (document.getSegments() == null || document.getSegments().isEmpty()) {
            throw new IllegalStateException("In-page classification did not return any segments.");
        }
        for (DocumentContentSegment segment : document.getSegments()) {
            if (segment == null || segment.getSpan() == null) {
                throw new IllegalStateException("In-page classification returned a segment without a span.");
            }
            System.out.println("Category: " + segment.getCategory());
            System.out.println("  Pages: " + segment.getStartPageNumber() + "-" + segment.getEndPageNumber());
            System.out.println("  Confidence: " + segment.getConfidence());
            System.out.println("  Source: " + segment.getSource());
            System.out.println("  Span: " + segment.getSpan().getOffset() + ", " + segment.getSpan().getLength());
        }
    }
}
