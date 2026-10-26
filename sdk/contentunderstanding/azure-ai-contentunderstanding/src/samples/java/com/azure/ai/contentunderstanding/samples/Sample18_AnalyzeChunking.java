// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentSpan;
import com.azure.ai.contentunderstanding.models.DocumentChunk;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.SemanticChunkingStrategy;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Demonstrates semantic document chunking for retrieval-augmented generation (RAG) scenarios.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>The sample creates a custom document analyzer with {@link SemanticChunkingStrategy} and a maximum chunk size of
 * 300 tokens. After analysis, each {@link DocumentChunk} exposes a source expression for visual grounding and
 * {@link ContentSpan} values that identify portions of {@link DocumentContent#getMarkdown()}. The sample resolves
 * those spans and prints each chunk as Markdown.</p>
 *
 * <p>Typical output for the included invoice is three chunks containing header and party details, line items, and
 * totals. Chunk boundaries and the exact count can vary slightly by model and maximum token setting. They are
 * semantic retrieval units, not fixed page boundaries.</p>
 *
 * <p>Configure model deployment defaults before running this sample; see {@link Sample00_UpdateDefaults}.</p>
 */
public class Sample18_AnalyzeChunking {
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
        String analyzerId = "semantic-chunking-" + UUID.randomUUID().toString().replace("-", "");
        String model = SampleModelConfiguration.getCompletionModel();

        try {
            ContentAnalyzer analyzer = new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
                .setDescription("Analyzer with semantic chunking")
                .setConfig(new ContentAnalyzerConfig().setReturnDetails(true).setLayoutEnabled(true)
                    .setChunkingStrategy(new SemanticChunkingStrategy().setMaxTokens(300)))
                .setModels(Collections.singletonMap("completion", model));
            SyncPoller<ContentAnalyzerOperationStatus, ContentAnalyzer> createPoller
                = client.beginCreateAnalyzer(analyzerId, analyzer, true);
            requireSuccessfulResult(createPoller.waitForCompletion().getStatus(), createPoller::getFinalResult,
                "Semantic chunking analyzer creation");
            verifyChunkingAnalyzer(client.getAnalyzer(analyzerId));

            BinaryData data = BinaryData.fromBytes(Files.readAllBytes(resolveSamplePath()));
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus, AnalysisResult>
                analyzePoller = client.beginAnalyzeBinary(analyzerId, data);
            AnalysisResult result
                = requireSuccessfulResult(analyzePoller.waitForCompletion().getStatus(), analyzePoller::getFinalResult,
                    "Semantic chunking analysis");
            printChunks(getDocumentContent(result));
        } finally {
            try {
                client.deleteAnalyzer(analyzerId);
            } catch (RuntimeException error) {
                System.out.println("Note: Failed to delete analyzer '" + analyzerId + "': " + error.getMessage());
            }
        }
    }

    static void printChunks(DocumentContent document) {
        List<String> chunkMarkdowns = renderChunks(document);
        System.out.println("Chunk count: " + chunkMarkdowns.size());
        for (int index = 0; index < chunkMarkdowns.size(); index++) {
            System.out.println("--- Chunk " + (index + 1) + " ---");
            System.out.println(chunkMarkdowns.get(index));
        }
    }

    public static List<String> renderChunks(DocumentContent document) {
        if (document == null) {
            throw new IllegalStateException("Analysis did not return document content.");
        }
        String markdown = document.getMarkdown();
        if (markdown == null) {
            throw new IllegalStateException("Document content did not include Markdown.");
        }
        List<DocumentChunk> chunks = document.getChunks();
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalStateException("Document content did not include semantic chunks.");
        }

        List<String> renderedChunks = new ArrayList<>(chunks.size());
        for (int chunkIndex = 0; chunkIndex < chunks.size(); chunkIndex++) {
            DocumentChunk chunk = chunks.get(chunkIndex);
            if (chunk == null || chunk.getSpans() == null || chunk.getSpans().isEmpty()) {
                throw new IllegalStateException("Chunk " + (chunkIndex + 1) + " did not include spans.");
            }
            StringBuilder chunkMarkdown = new StringBuilder();
            for (int spanIndex = 0; spanIndex < chunk.getSpans().size(); spanIndex++) {
                ContentSpan span = chunk.getSpans().get(spanIndex);
                validateSpan(markdown, span, chunkIndex, spanIndex);
                if (spanIndex > 0) {
                    chunkMarkdown.append(System.lineSeparator());
                }
                chunkMarkdown.append(markdown, span.getOffset(), span.getOffset() + span.getLength());
            }
            renderedChunks.add(chunkMarkdown.toString());
        }
        return renderedChunks;
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

    private static void validateSpan(String markdown, ContentSpan span, int chunkIndex, int spanIndex) {
        if (span == null || span.getOffset() < 0 || span.getLength() <= 0) {
            throw new IllegalStateException("Chunk " + (chunkIndex + 1) + " span " + (spanIndex + 1)
                + " has an invalid offset or length.");
        }
        long end = (long) span.getOffset() + span.getLength();
        if (end > markdown.length()) {
            throw new IllegalStateException("Chunk " + (chunkIndex + 1) + " span " + (spanIndex + 1)
                + " exceeds the Markdown length.");
        }
    }

    static DocumentContent getDocumentContent(AnalysisResult result) {
        if (result.getContents() == null || result.getContents().isEmpty()
            || !(result.getContents().get(0) instanceof DocumentContent)) {
            throw new IllegalStateException("Semantic chunking analysis did not return DocumentContent.");
        }
        return (DocumentContent) result.getContents().get(0);
    }

    static void verifyChunkingAnalyzer(ContentAnalyzer analyzer) {
        if (analyzer == null || analyzer.getConfig() == null
            || !(analyzer.getConfig().getChunkingStrategy() instanceof SemanticChunkingStrategy)) {
            throw new IllegalStateException("Analyzer did not preserve the semantic chunking strategy.");
        }
        SemanticChunkingStrategy strategy
            = (SemanticChunkingStrategy) analyzer.getConfig().getChunkingStrategy();
        if (!Integer.valueOf(300).equals(strategy.getMaxTokens())) {
            throw new IllegalStateException("Semantic chunk maxTokens should be 300.");
        }
    }

    static Path resolveSamplePath() {
        Path packagePath = Paths.get("src/samples/resources/sample_invoice.pdf");
        if (Files.exists(packagePath)) {
            return packagePath;
        }
        Path repositoryPath = Paths.get("sdk/contentunderstanding/azure-ai-contentunderstanding")
            .resolve(packagePath);
        if (Files.exists(repositoryPath)) {
            return repositoryPath;
        }
        throw new IllegalStateException("Could not locate sample_invoice.pdf.");
    }
}
