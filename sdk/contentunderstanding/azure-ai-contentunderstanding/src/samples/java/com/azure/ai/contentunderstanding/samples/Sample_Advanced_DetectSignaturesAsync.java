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
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Demonstrates signature detection with the asynchronous client.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>Signature detection requires layout extraction. The {@code prebuilt-layout} analyzer used here enables that
 * capability. Each detected signature has an ID and source expression; role and Markdown span information can be
 * absent when the service cannot determine them. When a span is available, the corresponding Markdown uses an image
 * reference such as <code>![recognized signature text](signatures/{id})</code>. The alt text is OCR output and can be
 * incomplete or contain extra characters.</p>
 *
 * <p>The bundled image is a synthetic training acknowledgment containing participant and approver signatures. Its
 * names and other details are fake data.</p>
 */
public class Sample_Advanced_DetectSignaturesAsync {
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
        BinaryData data = BinaryData.fromBytes(
            Files.readAllBytes(Paths.get("src/samples/resources/sample_signature.png")));

        Boolean completed = client.beginAnalyzeBinary("prebuilt-layout", data)
            .last()
            .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                "Signature analysis"))
            .map(result -> {
                Sample_Advanced_DetectSignatures
                    .printSignatures(Sample_Advanced_DetectSignatures.getDocumentContent(result));
                return Boolean.TRUE;
            })
            .block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("Signature analysis workflow returned no result.");
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
