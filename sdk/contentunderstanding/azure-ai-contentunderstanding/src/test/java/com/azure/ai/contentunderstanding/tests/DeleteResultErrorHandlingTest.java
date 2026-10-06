// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeleteResultErrorHandlingTest {
    private static final HttpHeaderName REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");
    private static final String NOT_FOUND_BODY
        = "{\"error\":{\"code\":\"NotFound\",\"message\":\"The analysis result does not exist.\"}}";

    @Test
    public void deleteResultMapsNotFoundResponse() {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingClient client = createBuilder(capture).buildClient();

        ResourceNotFoundException exception
            = assertThrows(ResourceNotFoundException.class, () -> client.deleteResult("missing-operation-id"));

        assertNotFoundException(exception);
        assertDeleteRequest(capture);
    }

    @Test
    public void deleteResultAsyncMapsNotFoundResponse() {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingAsyncClient client = createBuilder(capture).buildAsyncClient();

        ResourceNotFoundException exception
            = assertThrows(ResourceNotFoundException.class, () -> client.deleteResult("missing-operation-id").block());

        assertNotFoundException(exception);
        assertDeleteRequest(capture);
    }

    @Test
    public void deleteResultWithResponseMapsNotFoundResponse() {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingClient client = createBuilder(capture).buildClient();

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> client.deleteResultWithResponse("missing-operation-id", new RequestOptions()));

        assertNotFoundException(exception);
        assertDeleteRequest(capture);
    }

    @Test
    public void deleteResultWithResponseAsyncMapsNotFoundResponse() {
        RequestCapture capture = new RequestCapture();
        ContentUnderstandingAsyncClient client = createBuilder(capture).buildAsyncClient();

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> client.deleteResultWithResponse("missing-operation-id", new RequestOptions()).block());

        assertNotFoundException(exception);
        assertDeleteRequest(capture);
    }

    @Test
    public void deleteResultValidatesOperationIdBeforeSendingRequest() {
        ContentUnderstandingClient client = createValidationBuilder().buildClient();

        assertNullOperationId(() -> client.deleteResult(null));
        assertEmptyOperationId(() -> client.deleteResult(""));
    }

    @Test
    public void deleteResultAsyncValidatesOperationIdBeforeSendingRequest() {
        ContentUnderstandingAsyncClient client = createValidationBuilder().buildAsyncClient();

        assertNullOperationId(() -> client.deleteResult(null));
        assertEmptyOperationId(() -> client.deleteResult(""));
    }

    private static ContentUnderstandingClientBuilder createBuilder(RequestCapture capture) {
        HttpClient httpClient = request -> {
            capture.method.set(request.getHttpMethod());
            capture.path.set(request.getUrl().getPath());
            capture.query.set(request.getUrl().getQuery());
            HttpHeaders headers = new HttpHeaders().set(REQUEST_ID, "delete-request-id");
            return Mono
                .just(new MockHttpResponse(request, 404, headers, NOT_FOUND_BODY.getBytes(StandardCharsets.UTF_8)));
        };
        return new ContentUnderstandingClientBuilder().endpoint("https://example.com")
            .credential(new KeyCredential("fake-key"))
            .httpClient(httpClient);
    }

    private static ContentUnderstandingClientBuilder createValidationBuilder() {
        return new ContentUnderstandingClientBuilder().endpoint("https://example.com")
            .credential(new KeyCredential("fake-key"))
            .httpClient(request -> Mono.error(new AssertionError("Validation must occur before an HTTP request.")));
    }

    private static void assertNotFoundException(ResourceNotFoundException exception) {
        assertEquals(404, exception.getResponse().getStatusCode());
        assertEquals("delete-request-id", exception.getResponse().getHeaders().getValue(REQUEST_ID));
        String responseBody = exception.getResponse().getBodyAsString().block();
        assertEquals(NOT_FOUND_BODY, responseBody);
        assertTrue(responseBody.contains("\"code\":\"NotFound\""));
        assertTrue(responseBody.contains("The analysis result does not exist."));
    }

    private static void assertDeleteRequest(RequestCapture capture) {
        assertEquals(HttpMethod.DELETE, capture.method.get());
        assertEquals("/contentunderstanding/analyzerResults/missing-operation-id", capture.path.get());
        assertTrue(capture.query.get().contains("api-version=2026-06-01-preview"));
    }

    private static void assertNullOperationId(Executable executable) {
        NullPointerException exception = assertThrows(NullPointerException.class, executable);
        assertEquals("'operationId' cannot be null.", exception.getMessage());
    }

    private static void assertEmptyOperationId(Executable executable) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("'operationId' cannot be empty.", exception.getMessage());
    }

    private static final class RequestCapture {
        private final AtomicReference<HttpMethod> method = new AtomicReference<>();
        private final AtomicReference<String> path = new AtomicReference<>();
        private final AtomicReference<String> query = new AtomicReference<>();
    }
}
