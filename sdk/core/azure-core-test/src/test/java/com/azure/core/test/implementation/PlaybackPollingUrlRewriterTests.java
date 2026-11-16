// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link PlaybackPollingUrlRewriter}.
 */
public class PlaybackPollingUrlRewriterTests {
    private static final String REQUEST_URL
        = "https://localhost:8080/documentintelligence/documentModels:build?api-version=2024-11-30";

    @ParameterizedTest
    @ValueSource(strings = { "Operation-Location", "operation-location", "Azure-AsyncOperation", "Location" })
    public void rewritesPollingHeaderOntoRequestHost(String headerName) {
        HttpHeaderName name = HttpHeaderName.fromString(headerName);
        HttpResponse response = createResponse(REQUEST_URL, 202, new HttpHeaders().set(name,
            "https://REDACTED.cognitiveservices.azure.com/documentintelligence/operations/31?api-version=2024-11-30"),
            new byte[0]);

        HttpResponse rewritten = PlaybackPollingUrlRewriter.rewritePollingUrls(response);

        String expected = "https://localhost:8080/documentintelligence/operations/31?api-version=2024-11-30";
        assertEquals(expected, rewritten.getHeaderValue(name));
        assertEquals(expected, rewritten.getHeaders().getValue(name));
    }

    @ParameterizedTest
    @MethodSource("rewriteSupplier")
    public void rewritesOnlyHostAndPort(String requestUrl, String recordedUrl, String expectedUrl) {
        HttpResponse response = createResponse(requestUrl, 202,
            new HttpHeaders().set(HttpHeaderName.OPERATION_LOCATION, recordedUrl), new byte[0]);

        assertEquals(expectedUrl,
            PlaybackPollingUrlRewriter.rewritePollingUrls(response).getHeaderValue(HttpHeaderName.OPERATION_LOCATION));
    }

    private static Stream<Arguments> rewriteSupplier() {
        return Stream.of(
            // Playback endpoint without a port.
            Arguments.of("https://localhost/contentunderstanding/analyzers/a:analyze",
                "https://REDACTED.services.ai.azure.com/contentunderstanding/analyzerResults/1?api-version=2025-11-01",
                "https://localhost/contentunderstanding/analyzerResults/1?api-version=2025-11-01"),
            // Unsanitized ARM host.
            Arguments.of("https://localhost:1234/subscriptions/0/resourceGroups/rg?api-version=2021-04-01",
                "https://management.azure.com/subscriptions/0/operationresults/op?api-version=2021-04-01",
                "https://localhost:1234/subscriptions/0/operationresults/op?api-version=2021-04-01"),
            // The scheme is kept as recorded, as is the path, including empty segments.
            Arguments.of("http://localhost:5000/fr/path/1", "https://REDACTED/fr/models//905a#fragment",
                "https://localhost:5000/fr/models//905a#fragment"),
            Arguments.of("https://localhost:8080/path", "http://REDACTED.example.com/status",
                "http://localhost:8080/status"),
            // User information and the recorded port aren't kept.
            Arguments.of("https://localhost:8080/path", "https://user:secret@REDACTED.example.com:8443/status",
                "https://localhost:8080/status"),
            // URL without a path, such as one replaced by a default test proxy sanitizer.
            Arguments.of("https://localhost:8080/path", "https://example.com", "https://localhost:8080"));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "/operations/1",
            "REDACTED",
            "Sanitized",
            "https://REDACTED/not a uri",
            "ftp://REDACTED/file",
            "urn:operation:1" })
    public void leavesRelativeInvalidAndNonHttpUrlsAsRecorded(String recordedUrl) {
        HttpResponse response = createResponse(REQUEST_URL, 202,
            new HttpHeaders().set(HttpHeaderName.OPERATION_LOCATION, recordedUrl), new byte[0]);

        assertSame(response, PlaybackPollingUrlRewriter.rewritePollingUrls(response));
    }

    @ParameterizedTest
    @ValueSource(ints = { 301, 302, 303, 307, 308 })
    public void leavesRedirectLocationAsRecorded(int statusCode) {
        HttpResponse response = createResponse(REQUEST_URL, statusCode,
            new HttpHeaders().set(HttpHeaderName.LOCATION, "https://REDACTED.blob.core.windows.net/container/blob"),
            new byte[0]);

        assertSame(response, PlaybackPollingUrlRewriter.rewritePollingUrls(response));
    }

    @Test
    public void returnsResponseWhenNoPollingUrlNeedsRewriting() {
        HttpResponse noPollingUrls = createResponse(REQUEST_URL, 200,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"), new byte[0]);
        assertSame(noPollingUrls, PlaybackPollingUrlRewriter.rewritePollingUrls(noPollingUrls));

        HttpResponse alreadyOnRequestHost
            = createResponse(REQUEST_URL, 202, new HttpHeaders().set(HttpHeaderName.OPERATION_LOCATION,
                "https://localhost:8080/documentintelligence/operations/31?api-version=2024-11-30"), new byte[0]);
        assertSame(alreadyOnRequestHost, PlaybackPollingUrlRewriter.rewritePollingUrls(alreadyOnRequestHost));
    }

    @Test
    public void rewrittenResponseKeepsRequestStatusBodyAndOtherHeaders() {
        byte[] body = "{\"status\":\"running\"}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders()
            .set(HttpHeaderName.AZURE_ASYNCOPERATION, "https://management.azure.com/operationStatuses/1")
            .set(HttpHeaderName.LOCATION, "https://REDACTED.azure.com/operationResults/1")
            .set(HttpHeaderName.RETRY_AFTER, "0");
        HttpResponse response = createResponse(REQUEST_URL, 202, headers, body);

        HttpResponse rewritten = PlaybackPollingUrlRewriter.rewritePollingUrls(response);

        assertSame(response.getRequest(), rewritten.getRequest());
        assertEquals(202, rewritten.getStatusCode());
        assertEquals("https://localhost:8080/operationStatuses/1",
            rewritten.getHeaderValue(HttpHeaderName.AZURE_ASYNCOPERATION));
        assertEquals("https://localhost:8080/operationResults/1", rewritten.getHeaderValue(HttpHeaderName.LOCATION));
        assertEquals("0", rewritten.getHeaderValue(HttpHeaderName.RETRY_AFTER));
        assertArrayEquals(body, rewritten.getBodyAsBinaryData().toBytes());

        // The recorded response isn't modified.
        assertEquals("https://management.azure.com/operationStatuses/1",
            response.getHeaderValue(HttpHeaderName.AZURE_ASYNCOPERATION));
    }

    private static HttpResponse createResponse(String requestUrl, int statusCode, HttpHeaders headers, byte[] body) {
        return new MockHttpResponse(new HttpRequest(HttpMethod.POST, requestUrl), statusCode, headers, body);
    }
}
