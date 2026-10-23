// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.ResponseError;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Demonstrates reading diagnostics returned with an analysis result.
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
public class Sample_Advanced_AnalysisDiagnostics {
    public static void main(String[] args) {
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
        AnalysisInput input = new AnalysisInput().setUrl(
            "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-dotnet/main/ContentUnderstanding.Common/data/invoice.pdf");
        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller
            = client.beginAnalyze("prebuilt-invoice", Collections.singletonList(input));
        AnalysisResult result = requireSuccessfulResult(poller.waitForCompletion().getStatus(), poller::getFinalResult,
            "Invoice analysis");
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
}
