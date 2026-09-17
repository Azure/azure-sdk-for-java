// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.ProcessingLocation;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalyzeBinaryOptionsRoutingTest {
    private static final HttpHeaderName OPERATION_LOCATION = HttpHeaderName.fromString("Operation-Location");

    @Test
    public void beginAnalyzeBinaryOptionsRouteExplicitFalseAndRequestMetadata() {
        RequestCapture capture = new RequestCapture();
        AnalyzeBinaryOptions options = new AnalyzeBinaryOptions().setContentRange(ContentRange.pages(2, 4))
            .setInputTruncationAllowed(false)
            .setContentType("application/pdf")
            .setProcessingLocation(ProcessingLocation.GEOGRAPHY);

        ContentUnderstandingClient client = createBuilder(capture).buildClient();
        assertNotNull(client.beginAnalyzeBinary("prebuilt-layout", BinaryData.fromString("pdf-data"), options));

        assertEquals(HttpMethod.POST, capture.method.get());
        assertEquals("/contentunderstanding/analyzers/prebuilt-layout:analyzeBinary", capture.path.get());
        assertTrue(capture.query.get().contains("range=2-4"));
        assertTrue(capture.query.get().contains("allowInputTruncation=false"));
        assertTrue(capture.query.get().contains("processingLocation=geography"));
        assertTrue(capture.query.get().contains("stringEncoding=utf16"));
        assertEquals("application/pdf", capture.contentType.get());
        assertEquals("pdf-data", capture.body.get());
    }

    @Test
    public void beginAnalyzeBinaryDefaultOptionsOmitOptionalQueryParametersAsync() {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingAsyncClient client = createBuilder(capture).buildAsyncClient();

        assertNotNull(client
            .beginAnalyzeBinary("prebuilt-layout", BinaryData.fromString("binary-data"), new AnalyzeBinaryOptions())
            .take(1)
            .blockLast());

        assertEquals(HttpMethod.POST, capture.method.get());
        assertEquals("/contentunderstanding/analyzers/prebuilt-layout:analyzeBinary", capture.path.get());
        assertFalse(capture.query.get().contains("range="));
        assertFalse(capture.query.get().contains("allowInputTruncation="));
        assertFalse(capture.query.get().contains("processingLocation="));
        assertTrue(capture.query.get().contains("stringEncoding=utf16"));
        assertEquals("application/octet-stream", capture.contentType.get());
        assertEquals("binary-data", capture.body.get());
    }

    @Test
    public void beginAnalyzeBinaryNullOptionsUseDefaults() {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingClient client = createBuilder(capture).buildClient();

        assertNotNull(client.beginAnalyzeBinary("custom-analyzer", BinaryData.fromString("binary-data"),
            (AnalyzeBinaryOptions) null));

        assertEquals(HttpMethod.POST, capture.method.get());
        assertEquals("/contentunderstanding/analyzers/custom-analyzer:analyzeBinary", capture.path.get());
        assertFalse(capture.query.get().contains("range="));
        assertFalse(capture.query.get().contains("allowInputTruncation="));
        assertFalse(capture.query.get().contains("processingLocation="));
        assertTrue(capture.query.get().contains("stringEncoding=utf16"));
        assertEquals("application/octet-stream", capture.contentType.get());
        assertEquals("binary-data", capture.body.get());
    }

    private static ContentUnderstandingClientBuilder createBuilder(RequestCapture capture) {
        HttpClient httpClient = request -> {
            if (request.getHttpMethod() == HttpMethod.POST) {
                capture.method.set(request.getHttpMethod());
                capture.path.set(request.getUrl().getPath());
                capture.query.set(request.getUrl().getQuery());
                capture.contentType.set(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
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

    private static final class RequestCapture {
        private final AtomicReference<HttpMethod> method = new AtomicReference<>();
        private final AtomicReference<String> path = new AtomicReference<>();
        private final AtomicReference<String> query = new AtomicReference<>();
        private final AtomicReference<String> contentType = new AtomicReference<>();
        private final AtomicReference<String> body = new AtomicReference<>();
    }
}
