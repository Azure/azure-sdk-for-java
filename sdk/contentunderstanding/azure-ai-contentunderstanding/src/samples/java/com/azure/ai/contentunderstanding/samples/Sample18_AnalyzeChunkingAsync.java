// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.SemanticChunkingStrategy;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Demonstrates semantic document chunking for retrieval-augmented generation (RAG) scenarios with the asynchronous
 * client.
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
 * <p>Configure model deployment defaults before running this sample; see {@link Sample00_UpdateDefaultsAsync}.</p>
 */
public class Sample18_AnalyzeChunkingAsync {
    public static void main(String[] args) throws Exception {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");
        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW);
        ContentUnderstandingAsyncClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildAsyncClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildAsyncClient();
        }
        String analyzerId = "semantic-chunking-" + UUID.randomUUID().toString().replace("-", "");
        String model = SampleModelConfiguration.getCompletionModel();
        BinaryData data = BinaryData.fromBytes(Files.readAllBytes(Sample18_AnalyzeChunking.resolveSamplePath()));
        ContentAnalyzer analyzer = new ContentAnalyzer().setBaseAnalyzerId("prebuilt-document")
            .setDescription("Analyzer with semantic chunking")
            .setConfig(new ContentAnalyzerConfig().setReturnDetails(true).setLayoutEnabled(true)
                .setChunkingStrategy(new SemanticChunkingStrategy().setMaxTokens(300)))
            .setModels(Collections.singletonMap("completion", model));

        Mono<Boolean> workflow = client.beginCreateAnalyzer(analyzerId, analyzer, true)
            .last()
            .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                "Semantic chunking analyzer creation"))
            .then(client.getAnalyzer(analyzerId))
            .switchIfEmpty(Mono.error(new IllegalStateException("Analyzer retrieval returned no result.")))
            .doOnNext(Sample18_AnalyzeChunking::verifyChunkingAnalyzer)
            .then(client.beginAnalyzeBinary(analyzerId, data)
                .last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Semantic chunking analysis")))
            .map(result -> {
                Sample18_AnalyzeChunking.printChunks(Sample18_AnalyzeChunking.getDocumentContent(result));
                return Boolean.TRUE;
            })
            .switchIfEmpty(Mono.error(new IllegalStateException("Semantic chunking workflow returned no result.")));

        Boolean completed = runWithCleanup(workflow, () -> deleteAnalyzer(client, analyzerId)).block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("Semantic chunking workflow returned no result.");
        }
    }

    private static Mono<Void> deleteAnalyzer(ContentUnderstandingAsyncClient client, String analyzerId) {
        return client.deleteAnalyzer(analyzerId).onErrorResume(error -> {
            System.out.println("Note: Failed to delete analyzer '" + analyzerId + "': " + error.getMessage());
            return Mono.empty();
        });
    }

    static <T> Mono<T> requireSuccessfulResult(LongRunningOperationStatus status, Mono<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            return Mono
                .error(new IllegalStateException(operationName + " completed unsuccessfully with status: " + status));
        }
        return finalResult
            .switchIfEmpty(Mono.error(new IllegalStateException(operationName + " completed without a final result.")));
    }

    static <T> Mono<T> runWithCleanup(Mono<T> workflow, Supplier<Mono<Void>> cleanup) {
        return Mono.usingWhen(Mono.just(Boolean.TRUE),
            ignored -> workflow
                .switchIfEmpty(Mono.error(new IllegalStateException("Workflow completed without a result."))),
            ignored -> cleanup.get(), (ignored, error) -> cleanup.get(), ignored -> cleanup.get());
    }
}
