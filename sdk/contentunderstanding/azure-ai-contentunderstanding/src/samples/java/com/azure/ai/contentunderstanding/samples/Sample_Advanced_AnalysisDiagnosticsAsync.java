// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.ResponseError;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Demonstrates reading analysis diagnostics with the asynchronous client.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>The optional {@link AnalysisResult#getInfos()} list can include entries such as {@code LLMStats}, which currently
 * reports completion and embedding call counts together with average and total latency. Diagnostic codes are
 * extensible, so applications should tolerate codes introduced by future service versions.</p>
 *
 * <p>Diagnostic codes and messages are human-readable troubleshooting information and can change as the service
 * evolves. Use OpenTelemetry rather than parsing these messages when structured monitoring data is required.</p>
 */
public class Sample_Advanced_AnalysisDiagnosticsAsync {
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
        AnalysisInput input = new AnalysisInput().setUrl(
            "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-dotnet/main/ContentUnderstanding.Common/data/invoice.pdf");

        Boolean completed = client.beginAnalyze("prebuilt-invoice", Collections.singletonList(input))
            .last()
            .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                "Invoice analysis"))
            .map(result -> {
                if (result.getContents() == null || result.getContents().isEmpty()) {
                    throw new IllegalStateException("Invoice analysis did not return content.");
                }

                List<ResponseError> infos = result.getInfos();
                if (infos == null || infos.isEmpty()) {
                    throw new IllegalStateException("Invoice analysis did not return diagnostic information.");
                }

                boolean llmStatsFound = false;
                for (ResponseError info : infos) {
                    if (info == null) {
                        throw new IllegalStateException("Invoice analysis returned an empty diagnostic entry.");
                    }
                    System.out.println(info.getCode() + ": " + info.getMessage());
                    if ("LLMStats".equals(info.getCode())) {
                        if (info.getMessage() == null || info.getMessage().trim().isEmpty()) {
                            throw new IllegalStateException("Invoice analysis returned LLMStats without a message.");
                        }
                        llmStatsFound = true;
                    }
                }
                if (!llmStatsFound) {
                    throw new IllegalStateException("Invoice analysis diagnostics did not include LLMStats.");
                }
                return Boolean.TRUE;
            })
            .block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("Invoice analysis diagnostics workflow returned no result.");
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
