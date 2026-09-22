// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalyzeOptions;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.ProcessingLocation;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalyzeOptionsRoutingTest {
    private static final HttpHeaderName OPERATION_LOCATION = HttpHeaderName.fromString("Operation-Location");

    @Test
    public void beginAnalyzeOptionsRouteQueryParametersAndRequestBody() {
        RequestCapture capture = new RequestCapture();
        AnalyzeOptions options
            = new AnalyzeOptions().setModelDeployments(Collections.singletonMap("gpt-5.2", "sync-gpt"))
                .setInputTruncationAllowed(true)
                .setProcessingLocation(ProcessingLocation.GEOGRAPHY);

        ContentUnderstandingClient client = createBuilder(capture).buildClient();
        assertNotNull(client.beginAnalyze("prebuilt-layout", Collections.singletonList(createInput()), options));

        assertRoute(capture, "prebuilt-layout");
        assertTrue(capture.query.get().contains("allowInputTruncation=true"));
        assertTrue(capture.query.get().contains("processingLocation=geography"));
        assertTrue(capture.body.get().contains("\"modelDeployments\""));
        assertTrue(capture.body.get().contains("\"gpt-5.2\":\"sync-gpt\""));
    }

    @Test
    public void beginAnalyzeOptionsRouteExplicitFalseAsync() {
        RequestCapture capture = new RequestCapture();
        AnalyzeOptions options
            = new AnalyzeOptions().setModelDeployments(Collections.singletonMap("gpt-5.2", "async-gpt"))
                .setInputTruncationAllowed(false)
                .setProcessingLocation(ProcessingLocation.GLOBAL);

        ContentUnderstandingAsyncClient client = createBuilder(capture).buildAsyncClient();
        assertNotNull(client.beginAnalyze("prebuilt-layout", Collections.singletonList(createInput()), options)
            .take(1)
            .blockLast());

        assertRoute(capture, "prebuilt-layout");
        assertTrue(capture.query.get().contains("allowInputTruncation=false"));
        assertTrue(capture.query.get().contains("processingLocation=global"));
        assertTrue(capture.body.get().contains("\"gpt-5.2\":\"async-gpt\""));
    }

    @Test
    public void beginAnalyzeDefaultOverloadOmitsOptionalQueryParameters() {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingClient client = createBuilder(capture).buildClient();

        assertNotNull(client.beginAnalyze("prebuilt-layout", Collections.singletonList(createInput())));

        assertRoute(capture, "prebuilt-layout");
        assertFalse(capture.query.get().contains("allowInputTruncation="));
        assertFalse(capture.query.get().contains("processingLocation="));
        assertTrue(capture.query.get().contains("stringEncoding=utf16"));
        assertFalse(capture.body.get().contains("\"modelDeployments\""));
    }

    @Test
    public void beginAnalyzeScalarOptionsRouteDeploymentsAndDataZone() {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingClient client = createBuilder(capture).buildClient();

        assertNotNull(client.beginAnalyze("scalar-analyzer", Collections.singletonList(createInput()),
            Collections.singletonMap("text-embedding-3-large", "embed"), ProcessingLocation.DATA_ZONE));

        assertRoute(capture, "scalar-analyzer");
        assertTrue(capture.query.get().contains("processingLocation=dataZone"));
        assertFalse(capture.query.get().contains("allowInputTruncation="));
        assertTrue(capture.query.get().contains("stringEncoding=utf16"));
        assertTrue(capture.body.get().contains("\"modelDeployments\""));
        assertTrue(capture.body.get().contains("\"text-embedding-3-large\":\"embed\""));
    }

    @Test
    public void beginAnalyzeRawMediaRangesMatchTypedPreviewRequests() {
        assertEquivalentMediaRange("prebuilt-videoSearch", "https://example.com/video.mp4",
            ContentRange.timeRange(Duration.ZERO, Duration.ofSeconds(5)), new ContentRange("0-5000"), "0-5000");
        assertEquivalentMediaRange("prebuilt-audioSearch", "https://example.com/audio.mp3",
            ContentRange.timeRangeFrom(Duration.ofSeconds(5)), new ContentRange("5000-"), "5000-");
    }

    private static void assertEquivalentMediaRange(String analyzerId, String url, ContentRange typedRange,
        ContentRange rawRange, String expectedRange) {
        RequestCapture typedCapture = captureAnalyzeRequest(analyzerId, url, typedRange);
        RequestCapture rawCapture = captureAnalyzeRequest(analyzerId, url, rawRange);

        assertTrue(typedCapture.query.get().contains("api-version=2026-06-01-preview"));
        assertTrue(rawCapture.query.get().contains("api-version=2026-06-01-preview"));
        assertEquals(typedCapture.body.get(), rawCapture.body.get());
        assertTrue(typedCapture.body.get().contains("\"range\":\"" + expectedRange + "\""));
        assertTrue(rawCapture.body.get().contains("\"range\":\"" + expectedRange + "\""));
    }

    private static RequestCapture captureAnalyzeRequest(String analyzerId, String url, ContentRange range) {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingClient client = createBuilder(capture).buildClient();
        AnalysisInput input = new AnalysisInput().setUrl(url).setContentRange(range);

        assertNotNull(client.beginAnalyze(analyzerId, Collections.singletonList(input)));
        assertRoute(capture, analyzerId);
        return capture;
    }

    private static AnalysisInput createInput() {
        return new AnalysisInput().setUrl("https://example.com/doc.pdf");
    }

    private static ContentUnderstandingClientBuilder createBuilder(RequestCapture capture) {
        HttpClient httpClient = request -> {
            if (request.getHttpMethod() == HttpMethod.POST) {
                capture.method.set(request.getHttpMethod());
                capture.path.set(request.getUrl().getPath());
                capture.query.set(request.getUrl().getQuery());
                capture.body.set(request.getBodyAsBinaryData().toString());
                HttpHeaders headers = new HttpHeaders().set(OPERATION_LOCATION, "https://example.com/operations/123");
                return Mono.just(new MockHttpResponse(request, 202, headers, new byte[0]));
            }
            String pollingBody = "{\"id\":\"123\",\"status\":\"Running\"}";
            return Mono.just(new MockHttpResponse(request, 200, pollingBody.getBytes(StandardCharsets.UTF_8)));
        };
        return new ContentUnderstandingClientBuilder().endpoint("https://example.com")
            .credential(new KeyCredential("fake-key"))
            .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW)
            .httpClient(httpClient);
    }

    private static void assertRoute(RequestCapture capture, String analyzerId) {
        assertEquals(HttpMethod.POST, capture.method.get());
        assertEquals("/contentunderstanding/analyzers/" + analyzerId + ":analyze", capture.path.get());
    }

    private static final class RequestCapture {
        private final AtomicReference<HttpMethod> method = new AtomicReference<>();
        private final AtomicReference<String> path = new AtomicReference<>();
        private final AtomicReference<String> query = new AtomicReference<>();
        private final AtomicReference<String> body = new AtomicReference<>();
    }
}
