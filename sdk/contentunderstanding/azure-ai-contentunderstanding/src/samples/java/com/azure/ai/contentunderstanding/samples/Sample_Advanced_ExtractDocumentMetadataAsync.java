// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;

/**
 * Demonstrates extracting embedded metadata with the asynchronous client.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>{@link DocumentContent#getMetadata()} returns the metadata the service could extract as string key-value pairs.
 * PDF and DOCX inputs can expose different keys, and no individual key is guaranteed to be present. Applications
 * should enumerate the returned map and tolerate additional keys introduced by future service versions. PDF metadata
 * commonly includes {@code author}, {@code contentType}, {@code createdAt}, {@code language}, {@code pageCount}, and
 * {@code title}. DOCX can also include {@code lastModifiedBy}, {@code lastModifiedAt}, {@code characterCount}, and
 * {@code wordCount}; every key is optional.</p>
 *
 * <p>The bundled PDF contains an author, creation timestamp, language, title, and one page; the service also returns
 * its detected content type and page count. The bundled DOCX demonstrates Office-specific last-modified metadata and
 * application-maintained character and word counts.</p>
 */
public class Sample_Advanced_ExtractDocumentMetadataAsync {
    public static void main(String[] args) {
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

        Boolean completed = Flux.just("sample_metadata.pdf", "sample_metadata.docx")
            .concatMap(fileName -> Mono.fromCallable(
                () -> BinaryData.fromFile(Paths.get("src/samples/resources", fileName)))
                .flatMap(data -> client.beginAnalyzeBinary("prebuilt-layout", data).last())
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    fileName + " metadata analysis"))
                .map(result -> {
                    Sample_Advanced_ExtractDocumentMetadata.printMetadata(fileName,
                        Sample_Advanced_ExtractDocumentMetadata.getDocumentContent(result));
                    return Boolean.TRUE;
                }))
            .collectList()
            .map(results -> {
                if (results.size() != 2) {
                    throw new IllegalStateException("Metadata workflow did not analyze both documents.");
                }
                return Boolean.TRUE;
            })
            .block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("Metadata workflow returned no result.");
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
}
