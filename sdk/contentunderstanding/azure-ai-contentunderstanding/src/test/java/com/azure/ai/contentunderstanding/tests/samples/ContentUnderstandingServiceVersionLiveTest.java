// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerInlineResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@LiveOnly
public class ContentUnderstandingServiceVersionLiveTest extends ContentUnderstandingClientTestBase {
    private static final String PREVIEW_API_VERSION = "2026-06-01-preview";

    @Test
    public void defaultClientUsesLatestPreviewServiceVersion() throws Exception {
        List<String> capturedApiVersions = Collections.synchronizedList(new ArrayList<>());
        ContentUnderstandingClient client = createLiveClient(capturedApiVersions);
        BinaryData data = PreviewSampleTestSupport.readSample("sample_invoice.pdf");
        String operationId = null;
        try {
            SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller
                = client.beginAnalyzeBinary("prebuilt-documentSearch", data);
            PollResponse<ContentAnalyzerAnalyzeOperationStatus> completion = poller.waitForCompletion();
            AnalysisResult lroResult = ContentUnderstandingCommonApiTestBase.requireSuccessfulResult(
                completion.getStatus(), poller::getFinalResult, "Default-client binary analysis");
            operationId = ContentUnderstandingCommonApiTestBase.requireOperationId(completion.getValue(),
                "Default-client binary analysis");
            assertAnalysisResult(lroResult, PREVIEW_API_VERSION);

            ContentAnalyzerInlineResponse inlineResponse = client.analyzeBinaryInline("prebuilt-layout", data);
            assertAnalysisResult(inlineResponse.getResult(), PREVIEW_API_VERSION);

            client.deleteResult(operationId);
            operationId = null;

            assertFalse(capturedApiVersions.isEmpty(), "Expected at least one request to be captured");
            assertTrue(capturedApiVersions.stream().allMatch(PREVIEW_API_VERSION::equals),
                "Every request should use the latest preview API version: " + capturedApiVersions);
        } finally {
            if (operationId != null) {
                try {
                    client.deleteResult(operationId);
                } catch (RuntimeException ignored) {
                    // Preserve the primary live-test failure.
                }
            }
        }
    }

    private static ContentUnderstandingClient createLiveClient(List<String> capturedApiVersions) {
        String endpoint = Configuration.getGlobalConfiguration().get("CONTENTUNDERSTANDING_ENDPOINT");
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalStateException("CONTENTUNDERSTANDING_ENDPOINT must be configured for live tests.");
        }
        HttpPipelinePolicy capturePolicy = (context, next) -> {
            String query = context.getHttpRequest().getUrl().getQuery();
            String apiVersion = query == null
                ? null
                : Arrays.stream(query.split("&"))
                    .filter(segment -> segment.startsWith("api-version="))
                    .map(segment -> segment.substring("api-version=".length()))
                    .findFirst()
                    .orElse(null);
            capturedApiVersions.add(apiVersion);
            return next.process();
        };
        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .addPolicy(capturePolicy);
        return builder.buildClient();
    }

    private static void assertAnalysisResult(AnalysisResult result, String expectedApiVersion) {
        assertNotNull(result);
        assertEquals(expectedApiVersion, result.getApiVersion());
        assertNotNull(result.getContents());
        assertFalse(result.getContents().isEmpty());
    }
}
