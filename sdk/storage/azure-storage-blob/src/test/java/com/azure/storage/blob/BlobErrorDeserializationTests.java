// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.storage.blob.implementation.models.BlobStorageError;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
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
 * Tests cases where the service returns a response that would result in a {@link BlobStorageException} being thrown
 * with a response body that needs to be deserialized.
 */
public class BlobErrorDeserializationTests {
    private static final String JSON_ERROR
        = "{\"code\":\"ContainerNotFound\",\"message\":\"The specified container does not exist.\","
            + "\"headerName\":\"x-ms-version\"}";

    @Test
    public void xmlErrorResponseBody() {
        String errorResponse = "<?xml version=\"1.0\" encoding=\"utf-8\"?><Error><Code>ContainerAlreadyExists</Code>"
            + "<Message>The specified container already exists.</Message></Error>";
        BlobContainerClientBuilder builder = builder(409, "application/xml", errorResponse);

        BlobStorageException exception = assertThrows(BlobStorageException.class, () -> builder.buildClient().create());
        assertTrue(exception.getMessage().contains("The specified container already exists."));
        BlobStorageError error = assertInstanceOf(BlobStorageError.class, exception.getValue());
        assertEquals(BlobErrorCode.CONTAINER_ALREADY_EXISTS, error.getCode());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void blobStorageErrorFromJson(boolean wrapped) throws IOException {
        String json = wrapped ? "{\"error\":" + JSON_ERROR + "}" : JSON_ERROR;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            BlobStorageError error = BlobStorageError.fromJson(reader);

            assertEquals(BlobErrorCode.CONTAINER_NOT_FOUND, error.getCode());
            assertEquals("The specified container does not exist.", error.getMessage());
            assertEquals("x-ms-version", error.getHeaderName());
        }
    }

    // A wrapped JSON body is only populated by BlobStorageError.fromJson; azure-core's reflective fallback would leave
    // every field null (and fails outright on the module path).
    @Test
    public void jsonErrorResponseBody() {
        BlobContainerClientBuilder builder = builder(404, "application/json", "{\"error\":" + JSON_ERROR + "}");

        assertJsonError(assertThrows(BlobStorageException.class, () -> builder.buildClient().getProperties()));
        StepVerifier.create(builder.buildAsyncClient().getProperties())
            .verifyErrorSatisfies(e -> assertJsonError(assertInstanceOf(BlobStorageException.class, e)));
    }

    private static BlobContainerClientBuilder builder(int statusCode, String contentType, String body) {
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(request -> Mono.just(
            new MockHttpResponse(request, statusCode, new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType),
                body.getBytes(StandardCharsets.UTF_8))))
            .build();
        return new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/")
            .containerName("container")
            .credential(new MockTokenCredential())
            .pipeline(httpPipeline);
    }

    private static void assertJsonError(BlobStorageException exception) {
        BlobStorageError error = assertInstanceOf(BlobStorageError.class, exception.getValue());
        assertEquals(BlobErrorCode.CONTAINER_NOT_FOUND, error.getCode());
        assertEquals("x-ms-version", error.getHeaderName());
    }
}
