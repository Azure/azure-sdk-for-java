// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AudioVisualSource;
import com.azure.ai.contentunderstanding.models.ContentSource;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentSource;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Async version of {@link Sample_Advanced_ContentSource}. Demonstrates how to access
 * and use {@link ContentSource} grounding references from analysis results using the
 * async client.
 *
 * <p>For document/image content, sources are {@link DocumentSource} instances
 * with page number, polygon coordinates, and a computed bounding box.</p>
 *
 * <p>For audio/video content, sources are {@link AudioVisualSource} instances
 * with a timestamp and an optional bounding box.</p>
 *
 * <p>Document sources use {@code D(page,x1,y1,...,xN,yN)} and audio/video sources use
 * {@code AV(timeMs[,x,y,w,h])}; semicolons separate multiple regions. Document coordinates use
 * {@link DocumentContent#getUnit()}, which is commonly inches for document input.</p>
 *
 * <p><b>Supported service API version:</b> {@code 2025-11-01}.</p>
 *
 * <p>For client and model deployment setup, see {@link Sample00_UpdateDefaultsAsync}. API key authentication is
 * intended for local testing; prefer {@link DefaultAzureCredentialBuilder} for production applications.</p>
 */
public class Sample_Advanced_ContentSourceAsync {

    public static void main(String[] args) {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");

        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder().endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2025_11_01);

        ContentUnderstandingAsyncClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildAsyncClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildAsyncClient();
        }

        // Analyze an invoice once — reuse the result for all demonstrations.
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        AnalysisInput input = new AnalysisInput();
        input.setUrl(invoiceUrl);

        Boolean completed = client.beginAnalyze("prebuilt-invoice", Arrays.asList(input))
            .last()
            .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                "Invoice analysis"))
            .map(result -> {
                DocumentContent documentContent = Sample_Advanced_ContentSource.getDocumentContent(result);

                Sample_Advanced_ContentSource.documentContentSourceFromAnalysis(documentContent);
                Sample_Advanced_ContentSource.contentSourceParseRoundTrip(documentContent);
                return Boolean.TRUE;
            })
            .block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("Content source workflow returned no result.");
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

    // AudioVisualSource grounding is not yet returned for AI-generated audio/video fields. Once supported, the
    // corresponding timestamp and optional bounding-box flow can use the same parsing APIs demonstrated above.
}
