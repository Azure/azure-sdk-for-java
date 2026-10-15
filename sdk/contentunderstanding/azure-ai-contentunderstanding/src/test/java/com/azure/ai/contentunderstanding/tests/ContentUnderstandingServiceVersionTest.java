// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentUnderstandingServiceVersionTest {
    @Test
    public void defaultBuilderUsesLatestPreviewVersion() {
        assertApiVersion(null, "2026-06-01-preview", ContentUnderstandingClient::getDefaults);
    }

    @Test
    public void defaultAsyncBuilderUsesLatestPreviewVersion() {
        assertAsyncApiVersion(null, "2026-06-01-preview", client -> client.getDefaults().block());
    }

    @Test
    public void builderUsesExplicitGaVersion() {
        assertApiVersion(ContentUnderstandingServiceVersion.V2025_11_01, "2025-11-01",
            ContentUnderstandingClient::getDefaults);
    }

    @Test
    public void asyncBuilderUsesExplicitGaVersion() {
        assertAsyncApiVersion(ContentUnderstandingServiceVersion.V2025_11_01, "2025-11-01",
            client -> client.getDefaults().block());
    }

    @Test
    public void builderUsesExplicitPreviewVersion() {
        assertApiVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, "2026-06-01-preview",
            ContentUnderstandingClient::getDefaults);
    }

    @Test
    public void asyncBuilderUsesExplicitPreviewVersion() {
        assertAsyncApiVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, "2026-06-01-preview",
            client -> client.getDefaults().block());
    }

    @Test
    public void gaAnalyzeBinaryUsesExplicitGaVersion() {
        assertApiVersion(ContentUnderstandingServiceVersion.V2025_11_01, "2025-11-01",
            client -> client.beginAnalyzeBinary("prebuilt-documentSearch", BinaryData.fromString("test")).poll());
    }

    @Test
    public void gaAsyncAnalyzeBinaryUsesExplicitGaVersion() {
        assertAsyncApiVersion(ContentUnderstandingServiceVersion.V2025_11_01, "2025-11-01",
            client -> client.beginAnalyzeBinary("prebuilt-documentSearch", BinaryData.fromString("test"))
                .take(1)
                .blockLast());
    }

    @Test
    public void previewAnalyzeInlineUsesExplicitPreviewVersion() {
        assertApiVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, "2026-06-01-preview",
            client -> client.analyzeInline("prebuilt-layout",
                Collections.singletonList(new AnalysisInput().setUrl("https://example.com/invoice.pdf"))));
    }

    @Test
    public void previewAsyncAnalyzeInlineUsesExplicitPreviewVersion() {
        assertAsyncApiVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, "2026-06-01-preview",
            client -> client
                .analyzeInline("prebuilt-layout",
                    Collections.singletonList(new AnalysisInput().setUrl("https://example.com/invoice.pdf")))
                .block());
    }

    @Test
    public void defaultAnalyzeOperationsUseLatestPreviewVersion() {
        assertApiVersion(null, "2026-06-01-preview",
            client -> client.beginAnalyzeBinary("prebuilt-documentSearch", BinaryData.fromString("test")).poll());
        assertApiVersion(null, "2026-06-01-preview", client -> client.analyzeInline("prebuilt-layout",
            Collections.singletonList(new AnalysisInput().setUrl("https://example.com/invoice.pdf"))));
    }

    @Test
    public void defaultAsyncAnalyzeOperationsUseLatestPreviewVersion() {
        assertAsyncApiVersion(null, "2026-06-01-preview",
            client -> client.beginAnalyzeBinary("prebuilt-documentSearch", BinaryData.fromString("test"))
                .take(1)
                .blockLast());
        assertAsyncApiVersion(null, "2026-06-01-preview",
            client -> client
                .analyzeInline("prebuilt-layout",
                    Collections.singletonList(new AnalysisInput().setUrl("https://example.com/invoice.pdf")))
                .block());
    }

    @Test
    public void gaCopyOperationsUseExplicitGaVersion() {
        assertCopyOperationApiVersions(ContentUnderstandingServiceVersion.V2025_11_01, "2025-11-01");
    }

    @Test
    public void previewCopyOperationsUseExplicitPreviewVersion() {
        assertCopyOperationApiVersions(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW, "2026-06-01-preview");
    }

    private static CapturedRequest assertApiVersion(ContentUnderstandingServiceVersion serviceVersion,
        String expectedApiVersion, Consumer<ContentUnderstandingClient> request) {
        return captureApiVersion(serviceVersion, expectedApiVersion, builder -> request.accept(builder.buildClient()));
    }

    private static CapturedRequest assertAsyncApiVersion(ContentUnderstandingServiceVersion serviceVersion,
        String expectedApiVersion, Consumer<ContentUnderstandingAsyncClient> request) {
        return captureApiVersion(serviceVersion, expectedApiVersion,
            builder -> request.accept(builder.buildAsyncClient()));
    }

    private static CapturedRequest captureApiVersion(ContentUnderstandingServiceVersion serviceVersion,
        String expectedApiVersion, Consumer<ContentUnderstandingClientBuilder> request) {
        AtomicReference<String> query = new AtomicReference<>();
        AtomicReference<String> path = new AtomicReference<>();
        AtomicReference<String> body = new AtomicReference<>();
        HttpPipelinePolicy capturePolicy = (context, next) -> {
            query.set(context.getHttpRequest().getUrl().getQuery());
            path.set(context.getHttpRequest().getUrl().getPath());
            BinaryData requestBody = context.getHttpRequest().getBodyAsBinaryData();
            body.set(requestBody == null ? null : requestBody.toString());
            return Mono.error(new RequestCapturedException());
        };
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).policies(capturePolicy).build();
        ContentUnderstandingClientBuilder builder
            = new ContentUnderstandingClientBuilder().endpoint("https://example.com").pipeline(pipeline);
        if (serviceVersion != null) {
            builder.serviceVersion(serviceVersion);
        }

        assertThrows(RequestCapturedException.class, () -> request.accept(builder));
        assertTrue(Arrays.asList(query.get().split("&")).contains("api-version=" + expectedApiVersion));
        return new CapturedRequest(query.get(), path.get(), body.get());
    }

    private static void assertCopyOperationApiVersions(ContentUnderstandingServiceVersion serviceVersion,
        String expectedApiVersion) {
        String sourceResourceId = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test"
            + "/providers/Microsoft.CognitiveServices/accounts/source";
        String targetResourceId = "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/test"
            + "/providers/Microsoft.CognitiveServices/accounts/target";

        CapturedRequest syncGrantRequest = assertApiVersion(serviceVersion, expectedApiVersion,
            client -> client.grantCopyAuthorization("source", targetResourceId, "eastus"));
        CapturedRequest syncCopyRequest = assertApiVersion(serviceVersion, expectedApiVersion,
            client -> client.beginCopyAnalyzer("target", "source", false, sourceResourceId, "eastus").poll());
        CapturedRequest asyncGrantRequest = assertAsyncApiVersion(serviceVersion, expectedApiVersion,
            client -> client.grantCopyAuthorization("source", targetResourceId, "eastus").block());
        CapturedRequest asyncCopyRequest = assertAsyncApiVersion(serviceVersion, expectedApiVersion,
            client -> client.beginCopyAnalyzer("target", "source", false, sourceResourceId, "eastus")
                .take(1)
                .blockLast());

        assertGrantCopyRequest(syncGrantRequest, targetResourceId);
        assertGrantCopyRequest(asyncGrantRequest, targetResourceId);
        assertCopyAnalyzerRequest(syncCopyRequest, sourceResourceId);
        assertCopyAnalyzerRequest(asyncCopyRequest, sourceResourceId);
    }

    private static void assertGrantCopyRequest(CapturedRequest request, String resourceId) {
        assertTrue(request.path.endsWith("/analyzers/source:grantCopyAuthorization"));
        assertTrue(request.body.contains("\"targetAzureResourceId\":\"" + resourceId + "\""));
        assertTrue(request.body.contains("\"targetRegion\":\"eastus\""));
    }

    private static void assertCopyAnalyzerRequest(CapturedRequest request, String resourceId) {
        assertTrue(request.path.endsWith("/analyzers/target:copy"));
        assertTrue(Arrays.asList(request.query.split("&")).contains("allowReplace=false"));
        assertTrue(request.body.contains("\"sourceAnalyzerId\":\"source\""));
        assertTrue(request.body.contains("\"sourceAzureResourceId\":\"" + resourceId + "\""));
        assertTrue(request.body.contains("\"sourceRegion\":\"eastus\""));
    }

    private static final class CapturedRequest {
        private final String query;
        private final String path;
        private final String body;

        private CapturedRequest(String query, String path, String body) {
            this.query = query;
            this.path = path;
            this.body = body;
        }
    }

    private static final class RequestCapturedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
