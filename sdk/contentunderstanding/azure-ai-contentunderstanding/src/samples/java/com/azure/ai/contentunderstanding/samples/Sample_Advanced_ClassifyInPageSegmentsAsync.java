// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Demonstrates in-page classification with the asynchronous client.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>Segmentation normally separates documents only at page boundaries. Set both {@code segmentEnabled} and
 * {@code allowInPageSegments} on the analyzer configuration to allow boundaries within a page.</p>
 *
 * <p>In-page segmentation is useful when distinct documents can appear on one page, such as individual supplemental
 * statements that are often appended after the main form in a K-1 tax package. Segments can report the same page
 * range; their source expressions and spans locate each document within the page, while confidence combines
 * segmentation and classification confidence. See the
 * <a href="https://learn.microsoft.com/azure/ai-services/content-understanding/concepts/classifier">Content
 * Understanding classifier overview</a> for supported scenarios.</p>
 *
 * <p>The bundled sample is a simplified synthetic one-page PDF containing an invoice in the upper half and an account
 * statement in the lower half.</p>
 *
 * <p>Configure model deployment defaults before running this sample; see {@link Sample00_UpdateDefaultsAsync}.</p>
 */
public class Sample_Advanced_ClassifyInPageSegmentsAsync {
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
        String analyzerId = "in-page-classifier-" + UUID.randomUUID().toString().replace("-", "");
        BinaryData data = BinaryData.fromBytes(
            Files.readAllBytes(Paths.get("src/samples/resources/mixed_financial_docs_in_page.pdf")));

        Mono<AsyncPollResponse<ContentAnalyzerOperationStatus, ContentAnalyzer>> createdAnalyzer
            = client.beginCreateAnalyzer(analyzerId, Sample_Advanced_ClassifyInPageSegments.createClassifier(), true)
                .last()
                .flatMap(response -> {
                    if (response.getStatus() != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
                        return Mono.error(new IllegalStateException(
                            "In-page classifier creation completed unsuccessfully with status: "
                                + response.getStatus()));
                    }
                    return Mono.just(response);
                });

        Mono<Boolean> workflow = createdAnalyzer.flatMap(createResponse -> runWithCleanup(
            requireSuccessfulResult(createResponse.getStatus(), createResponse.getFinalResult(),
                "In-page classifier creation")
                .then(client.beginAnalyzeBinary(analyzerId, data)
                    .last()
                    .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                        "In-page classification")))
                .map(result -> {
                    DocumentContent document = Sample_Advanced_ClassifyInPageSegments.getDocumentContent(result);
                    Sample_Advanced_ClassifyInPageSegments.printSegments(document);
                    return Boolean.TRUE;
                }),
            () -> client.deleteAnalyzer(analyzerId)));

        Boolean completed = workflow.block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("In-page classification workflow returned no result.");
        }
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
