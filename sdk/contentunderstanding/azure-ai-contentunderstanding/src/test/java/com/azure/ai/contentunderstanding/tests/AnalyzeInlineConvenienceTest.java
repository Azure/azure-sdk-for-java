// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.AnalyzeOptions;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerInlineResponse;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.OperationState;
import com.azure.ai.contentunderstanding.models.ProcessingLocation;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnalyzeInlineConvenienceTest {
    private static final HttpHeaderName REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");

    private static final String FAILED_INLINE_BODY = "{\"status\":\"Failed\",\"error\":{\"code\":"
        + "\"InternalServerError\",\"message\":\"An unexpected error occurred.\"}}";

    private static final String SUCCEEDED_INLINE_BODY = "{\"status\":\"Succeeded\",\"result\":{"
        + "\"analyzerId\":\"prebuilt-layout\",\"apiVersion\":\"2026-06-01-preview\","
        + "\"createdAt\":\"2026-07-29T00:00:00Z\",\"contents\":[{\"kind\":\"document\","
        + "\"markdown\":\"# Invoice\",\"startPageNumber\":1,\"endPageNumber\":1}]},"
        + "\"usage\":{\"documentPagesStandardInline\":1,\"contextualizationTokens\":500,"
        + "\"advancedContextualizationTokens\":25,\"tokens\":{\"gpt-5.2-input\":42}}}";

    private static final String SUCCEEDED_WITHOUT_USAGE_BODY = "{\"status\":\"Succeeded\",\"result\":{"
        + "\"analyzerId\":\"prebuilt-layout\",\"apiVersion\":\"2026-06-01-preview\","
        + "\"createdAt\":\"2026-07-29T00:00:00Z\",\"contents\":[{\"kind\":\"document\","
        + "\"markdown\":\"# Invoice\",\"startPageNumber\":1,\"endPageNumber\":1}]}}";

    private static final String SUCCEEDED_WITHOUT_RESULT_BODY = "{\"status\":\"Succeeded\"}";
    private static final String MALFORMED_INLINE_BODY = "{not-json";

    @Test
    public void analyzeInlineReturnsCompleteResponseWithUsage() {
        RequestCapture capture = new RequestCapture();
        ContentAnalyzerInlineResponse response = createSyncClient(SUCCEEDED_INLINE_BODY, capture)
            .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()));

        assertInlineResponseDetails(response);
        assertRoute(capture, "analyzeInline");
        assertFalse(capture.query.get().contains("allowInputTruncation="));
        assertFalse(capture.query.get().contains("processingLocation="));
        assertFalse(capture.requestBody.get().contains("\"modelDeployments\""));
    }

    @Test
    public void analyzeInlineAsyncReturnsCompleteResponseWithUsage() {
        ContentAnalyzerInlineResponse response = createAsyncClient(SUCCEEDED_INLINE_BODY)
            .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()))
            .block();

        assertInlineResponseDetails(response);
    }

    @Test
    public void analyzeBinaryInlineReturnsCompleteResponseWithUsage() {
        RequestCapture capture = new RequestCapture();
        ContentAnalyzerInlineResponse response = createSyncClient(SUCCEEDED_INLINE_BODY, capture)
            .analyzeBinaryInline("prebuilt-layout", BinaryData.fromString("fake-pdf-bytes"));

        assertInlineResponseDetails(response);
        assertRoute(capture, "analyzeBinaryInline");
        assertEquals("application/octet-stream", capture.contentType.get());
    }

    @Test
    public void analyzeBinaryInlineAsyncReturnsCompleteResponseWithUsage() {
        ContentAnalyzerInlineResponse response = createAsyncClient(SUCCEEDED_INLINE_BODY)
            .analyzeBinaryInline("prebuilt-layout", BinaryData.fromString("fake-pdf-bytes"))
            .block();

        assertInlineResponseDetails(response);
    }

    @Test
    public void analyzeInlinePreservesMissingUsageAsNull() {
        ContentAnalyzerInlineResponse response = createSyncClient(SUCCEEDED_WITHOUT_USAGE_BODY)
            .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), new AnalyzeOptions());

        assertNotNull(response);
        assertEquals(OperationState.SUCCEEDED, response.getStatus());
        assertNotNull(response.getResult());
        assertNull(response.getUsage());
    }

    @Test
    public void analyzeInlineThrowsWhenOperationStateFailed() {
        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> createSyncClient(FAILED_INLINE_BODY)
                .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), new AnalyzeOptions()));

        assertFailedInlineException(exception);
    }

    @Test
    public void analyzeInlineAsyncThrowsWhenOperationStateFailed() {
        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> createAsyncClient(FAILED_INLINE_BODY)
                .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), new AnalyzeOptions())
                .block());

        assertFailedInlineException(exception);
    }

    @Test
    public void analyzeBinaryInlineThrowsWhenOperationStateFailed() {
        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> createSyncClient(FAILED_INLINE_BODY).analyzeBinaryInline("prebuilt-layout",
                BinaryData.fromString("fake-pdf-bytes"), new AnalyzeBinaryOptions()));

        assertFailedInlineException(exception);
    }

    @Test
    public void analyzeBinaryInlineAsyncThrowsWhenOperationStateFailed() {
        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> createAsyncClient(FAILED_INLINE_BODY).analyzeBinaryInline("prebuilt-layout",
                BinaryData.fromString("fake-pdf-bytes"), new AnalyzeBinaryOptions()).block());

        assertFailedInlineException(exception);
    }

    @Test
    public void analyzeInlineThrowsWhenSucceededEnvelopeHasNoResult() {
        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> createSyncClient(SUCCEEDED_WITHOUT_RESULT_BODY)
                .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), new AnalyzeOptions()));

        assertEquals(200, exception.getResponse().getStatusCode());
        assertTrue(exception.getMessage().contains("succeeded without a result"));
    }

    @Test
    public void inlineMethodsThrowWhenResponseBodyIsEmpty() {
        HttpResponseException urlException = assertThrows(HttpResponseException.class, () -> createSyncClient("")
            .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), new AnalyzeOptions()));
        HttpResponseException binaryException = assertThrows(HttpResponseException.class,
            () -> createSyncClient("").analyzeBinaryInline("prebuilt-layout", BinaryData.fromString("fake-pdf-bytes"),
                new AnalyzeBinaryOptions()));

        assertEmptyBodyException(urlException);
        assertEmptyBodyException(binaryException);
    }

    @Test
    public void inlineAsyncMethodsThrowWhenResponseBodyIsEmpty() {
        HttpResponseException urlException = assertThrows(HttpResponseException.class,
            () -> createAsyncClient("")
                .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), new AnalyzeOptions())
                .block());
        HttpResponseException binaryException = assertThrows(HttpResponseException.class, () -> createAsyncClient("")
            .analyzeBinaryInline("prebuilt-layout", BinaryData.fromString("fake-pdf-bytes"), new AnalyzeBinaryOptions())
            .block());

        assertEmptyBodyException(urlException);
        assertEmptyBodyException(binaryException);
    }

    @Test
    public void analyzeInlinePreservesMalformedRawResponse() {
        HttpResponseException exception
            = assertThrows(HttpResponseException.class, () -> createSyncClient(MALFORMED_INLINE_BODY)
                .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), new AnalyzeOptions()));

        assertMalformedBodyException(exception);
    }

    @Test
    public void analyzeBinaryInlineAsyncPreservesMalformedRawResponse() {
        HttpResponseException exception
            = assertThrows(HttpResponseException.class,
                () -> createAsyncClient(MALFORMED_INLINE_BODY)
                    .analyzeBinaryInline("prebuilt-layout", BinaryData.fromString("fake-pdf-bytes"),
                        new AnalyzeBinaryOptions())
                    .block());

        assertMalformedBodyException(exception);
    }

    @Test
    public void analyzeBinaryInlineOptionsRouteQueryParameters() {
        RequestCapture capture = new RequestCapture();
        AnalyzeBinaryOptions options = new AnalyzeBinaryOptions().setContentRange(ContentRange.page(2))
            .setInputTruncationAllowed(true)
            .setProcessingLocation(ProcessingLocation.GEOGRAPHY);

        ContentAnalyzerInlineResponse response = createSyncClient(SUCCEEDED_INLINE_BODY, capture)
            .analyzeBinaryInline("prebuilt-layout", BinaryData.fromString("fake-pdf-bytes"), options);

        assertNotNull(response);
        assertRoute(capture, "analyzeBinaryInline");
        assertTrue(capture.query.get().contains("range=2"));
        assertTrue(capture.query.get().contains("allowInputTruncation=true"));
        assertTrue(capture.query.get().contains("processingLocation=geography"));
    }

    @Test
    public void analyzeInlineOptionsRouteQueryParametersAndRequestBody() {
        RequestCapture capture = new RequestCapture();
        AnalyzeOptions options
            = new AnalyzeOptions().setModelDeployments(Collections.singletonMap("gpt-5.2", "sync-gpt"))
                .setInputTruncationAllowed(true)
                .setProcessingLocation(ProcessingLocation.GEOGRAPHY);

        ContentAnalyzerInlineResponse response = createSyncClient(SUCCEEDED_INLINE_BODY, capture)
            .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), options);

        assertNotNull(response);
        assertRoute(capture, "analyzeInline");
        assertTrue(capture.query.get().contains("allowInputTruncation=true"));
        assertTrue(capture.query.get().contains("processingLocation=geography"));
        assertTrue(capture.requestBody.get().contains("\"modelDeployments\""));
        assertTrue(capture.requestBody.get().contains("\"gpt-5.2\":\"sync-gpt\""));
    }

    @Test
    public void analyzeInlineOptionsRouteExplicitFalseAsync() {
        RequestCapture capture = new RequestCapture();
        AnalyzeOptions options
            = new AnalyzeOptions().setModelDeployments(Collections.singletonMap("gpt-5.2", "async-gpt"))
                .setInputTruncationAllowed(false)
                .setProcessingLocation(ProcessingLocation.GLOBAL);

        ContentAnalyzerInlineResponse response = createAsyncClient(SUCCEEDED_INLINE_BODY, capture)
            .analyzeInline("prebuilt-layout", Collections.singletonList(createInput()), options)
            .block();

        assertNotNull(response);
        assertRoute(capture, "analyzeInline");
        assertTrue(capture.query.get().contains("allowInputTruncation=false"));
        assertTrue(capture.query.get().contains("processingLocation=global"));
        assertTrue(capture.requestBody.get().contains("\"gpt-5.2\":\"async-gpt\""));
    }

    @Test
    public void analyzeBinaryInlineNullOptionsUseDefaultContentType() {
        RequestCapture capture = new RequestCapture();
        ContentAnalyzerInlineResponse response = createSyncClient(SUCCEEDED_INLINE_BODY, capture)
            .analyzeBinaryInline("prebuilt-layout", BinaryData.fromString("fake-pdf-bytes"), null);

        assertNotNull(response);
        assertRoute(capture, "analyzeBinaryInline");
        assertEquals("application/octet-stream", capture.contentType.get());
        assertFalse(capture.query.get().contains("range="));
        assertFalse(capture.query.get().contains("allowInputTruncation="));
        assertFalse(capture.query.get().contains("processingLocation="));
    }

    @Test
    public void analyzeBinaryInlineNullOptionsUseDefaultContentTypeAsync() {
        RequestCapture capture = new RequestCapture();
        ContentAnalyzerInlineResponse response = createAsyncClient(SUCCEEDED_INLINE_BODY, capture)
            .analyzeBinaryInline("prebuilt-layout", BinaryData.fromString("fake-pdf-bytes"), null)
            .block();

        assertNotNull(response);
        assertRoute(capture, "analyzeBinaryInline");
        assertEquals("application/octet-stream", capture.contentType.get());
        assertFalse(capture.query.get().contains("range="));
        assertFalse(capture.query.get().contains("allowInputTruncation="));
        assertFalse(capture.query.get().contains("processingLocation="));
    }

    private static AnalysisInput createInput() {
        return new AnalysisInput().setUrl("https://example.com/doc.pdf");
    }

    private static ContentUnderstandingClient createSyncClient(String responseBody) {
        return createSyncClient(responseBody, new RequestCapture());
    }

    private static ContentUnderstandingClient createSyncClient(String responseBody, RequestCapture capture) {
        return createBuilder(responseBody, capture).buildClient();
    }

    private static ContentUnderstandingAsyncClient createAsyncClient(String responseBody) {
        return createAsyncClient(responseBody, new RequestCapture());
    }

    private static ContentUnderstandingAsyncClient createAsyncClient(String responseBody, RequestCapture capture) {
        return createBuilder(responseBody, capture).buildAsyncClient();
    }

    private static ContentUnderstandingClientBuilder createBuilder(String responseBody, RequestCapture capture) {
        HttpClient httpClient = request -> {
            capture.method.set(request.getHttpMethod());
            capture.path.set(request.getUrl().getPath());
            capture.query.set(request.getUrl().getQuery());
            BinaryData requestBody = request.getBodyAsBinaryData();
            capture.requestBody.set(requestBody == null ? null : requestBody.toString());
            capture.contentType.set(request.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
            HttpHeaders headers = new HttpHeaders().set(REQUEST_ID, "inline-request-id");
            return Mono
                .just(new MockHttpResponse(request, 200, headers, responseBody.getBytes(StandardCharsets.UTF_8)));
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
        private final AtomicReference<String> requestBody = new AtomicReference<>();
        private final AtomicReference<String> contentType = new AtomicReference<>();
    }

    private static void assertFailedInlineException(HttpResponseException exception) {
        assertTrue(exception.getMessage().contains("operation status 'Failed'"));
        assertRawResponse(exception, FAILED_INLINE_BODY);
        ContentAnalyzerInlineResponse response = (ContentAnalyzerInlineResponse) exception.getValue();
        assertEquals(OperationState.FAILED, response.getStatus());
    }

    private static void assertInlineResponseDetails(ContentAnalyzerInlineResponse response) {
        assertNotNull(response);
        assertEquals(OperationState.SUCCEEDED, response.getStatus());
        assertNotNull(response.getResult());
        assertEquals("prebuilt-layout", response.getResult().getAnalyzerId());
        assertNotNull(response.getUsage());
        assertEquals(1, response.getUsage().getDocumentPagesStandardInline());
        assertEquals(500, response.getUsage().getContextualizationTokens());
        assertEquals(25, response.getUsage().getAdvancedContextualizationTokens());
        assertNotNull(response.getUsage().getTokens());
        assertEquals(42, response.getUsage().getTokens().get("gpt-5.2-input"));
    }

    private static void assertRoute(RequestCapture capture, String operation) {
        assertEquals(HttpMethod.POST, capture.method.get());
        assertEquals("/contentunderstanding/analyzers/prebuilt-layout:" + operation, capture.path.get());
    }

    private static void assertEmptyBodyException(HttpResponseException exception) {
        assertTrue(exception.getMessage().contains("empty response body"));
        assertEquals(200, exception.getResponse().getStatusCode());
        assertEquals("inline-request-id", exception.getResponse().getHeaders().getValue(REQUEST_ID));
    }

    private static void assertMalformedBodyException(HttpResponseException exception) {
        assertTrue(exception.getMessage().contains("malformed response body"));
        assertNotNull(exception.getCause());
        assertRawResponse(exception, MALFORMED_INLINE_BODY);
    }

    private static void assertRawResponse(HttpResponseException exception, String expectedBody) {
        assertEquals(200, exception.getResponse().getStatusCode());
        assertEquals("inline-request-id", exception.getResponse().getHeaders().getValue(REQUEST_ID));
        assertEquals(expectedBody, exception.getResponse().getBodyAsString().block());
    }
}
