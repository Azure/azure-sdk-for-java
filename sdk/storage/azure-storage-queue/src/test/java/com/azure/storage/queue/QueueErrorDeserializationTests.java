// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.storage.queue.implementation.models.QueueStorageError;
import com.azure.storage.queue.models.QueueErrorCode;
import com.azure.storage.queue.models.QueueStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests cases where the service returns a response that would result in a {@link QueueStorageException} being thrown
 * with a response body that needs to be deserialized.
 */
public class QueueErrorDeserializationTests {
    @Test
    public void errorResponseBody() {
        String errorResponse = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Error><Code>ContainerAlreadyExists</Code>"
            + "<Message>The specified container already exists.</Message></Error>";
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(request -> Mono.just(
            new MockHttpResponse(request, 409, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml"),
                errorResponse.getBytes(StandardCharsets.UTF_8))))
            .build();
        QueueClient fileClient = new QueueClientBuilder().endpoint("https://account.blob.core.windows.net/")
            .queueName("queue")
            .credential(new MockTokenCredential())
            .pipeline(httpPipeline)
            .buildClient();

        QueueStorageException exception = assertThrows(QueueStorageException.class, fileClient::create);
        assertTrue(exception.getMessage().contains("The specified container already exists."));
        // assertEquals(BlobErrorCode.CONTAINER_ALREADY_EXISTS, exception.getErrorCode());
    }

    private static final String JSON_ERROR
        = "{\"code\":\"QueueNotFound\",\"message\":\"The specified queue does not exist.\","
            + "\"headerName\":\"x-ms-version\"}";

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void queueStorageErrorFromJson(boolean wrapped) throws IOException {
        String json = wrapped ? "{\"error\":" + JSON_ERROR + "}" : JSON_ERROR;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            QueueStorageError error = QueueStorageError.fromJson(reader);

            assertEquals(QueueErrorCode.QUEUE_NOT_FOUND, error.getCode());
            assertEquals("The specified queue does not exist.", error.getMessage());
            assertEquals("x-ms-version", error.getHeaderName());
        }
    }

    // A wrapped JSON body is only populated by QueueStorageError.fromJson; azure-core's reflective fallback would leave
    // every field null (and fails outright on the module path).
    @Test
    public void jsonErrorResponseBody() {
        String errorResponse = "{\"error\":" + JSON_ERROR + "}";
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(request -> Mono.just(
            new MockHttpResponse(request, 404, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                errorResponse.getBytes(StandardCharsets.UTF_8))))
            .build();
        QueueClientBuilder builder = new QueueClientBuilder().endpoint("https://account.queue.core.windows.net/")
            .queueName("queue")
            .credential(new MockTokenCredential())
            .pipeline(httpPipeline);

        assertJsonError(assertThrows(QueueStorageException.class, () -> builder.buildClient().getProperties()));
        StepVerifier.create(builder.buildAsyncClient().getProperties())
            .verifyErrorSatisfies(e -> assertJsonError(assertInstanceOf(QueueStorageException.class, e)));
    }

    private static void assertJsonError(QueueStorageException exception) {
        QueueStorageError error = assertInstanceOf(QueueStorageError.class, exception.getValue());
        assertEquals(QueueErrorCode.QUEUE_NOT_FOUND, error.getCode());
        assertEquals("x-ms-version", error.getHeaderName());
    }
}
