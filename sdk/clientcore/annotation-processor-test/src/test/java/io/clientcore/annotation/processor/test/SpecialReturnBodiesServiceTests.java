// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.SpecialReturnBodiesService;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Tests {@link SpecialReturnBodiesService} APIs with a response payload that would've cause invalid deserialization if
 * the return types didn't have special handling.
 */
public class SpecialReturnBodiesServiceTests {
    private static final byte[] RESPONSE_BODY_BYTES;
    private static final HttpClient HTTP_CLIENT;

    static {
        RESPONSE_BODY_BYTES = new byte[8192];
        new SecureRandom().nextBytes(RESPONSE_BODY_BYTES);

        HTTP_CLIENT = request -> {
            // Returns a response with a Content-Type that would fail if the response was deserialized.
            HttpHeaders responseHeaders = new HttpHeaders()
                .add(HttpHeaderName.CONTENT_TYPE, "application/json")
                .add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(RESPONSE_BODY_BYTES.length));

            return new Response<>(request, 200, responseHeaders,
                BinaryData.fromBytes(CoreUtils.arrayCopy(RESPONSE_BODY_BYTES)));
        };
    }

    private static SpecialReturnBodiesService getService() {
        return SpecialReturnBodiesService.getNewInstance(new HttpPipelineBuilder().httpClient(HTTP_CLIENT).build());
    }

    @Test
    public void getBinaryData() {
        assertArrayEquals(RESPONSE_BODY_BYTES, getService().getBinaryData("https://localhost").toBytes());
    }

    @Test
    public void getBinaryDataWithResponse() {
        try (Response<BinaryData> response = getService().getBinaryDataWithResponse("https://localhost")) {
            assertArrayEquals(RESPONSE_BODY_BYTES, response.getValue().toBytes());
        }
    }

    @Test
    public void getByteArray() {
        assertArrayEquals(RESPONSE_BODY_BYTES, getService().getByteArray("https://localhost"));
    }

    @Test
    public void getByteArrayWithResponse() {
        try (Response<byte[]> response = getService().getByteArrayWithResponse("https://localhost")) {
            assertArrayEquals(RESPONSE_BODY_BYTES, response.getValue());
        }
    }

    @Test
    public void getInputStream() {
        assertArrayEquals(RESPONSE_BODY_BYTES, fullyReadInputStream(getService().getInputStream("https://localhost")));
    }

    @Test
    public void getInputStreamWithResponse() {
        try (Response<InputStream> response = getService().getInputStreamWithResponse("https://localhost")) {
            assertArrayEquals(RESPONSE_BODY_BYTES, fullyReadInputStream(response.getValue()));
        }
    }

    private static byte[] fullyReadInputStream(InputStream inputStream) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
