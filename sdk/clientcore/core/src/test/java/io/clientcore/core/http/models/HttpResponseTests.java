// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.clientcore.core.util.TestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpResponseTests {
    private static final int STATUS_CODE = 200;
    private static final HttpHeaders HEADERS = new HttpHeaders().add(HttpHeaderName.CONTENT_TYPE, "application/text");
    private static final String VALUE = "Response body";
    private static final BinaryData BODY = BinaryData.fromString(VALUE);
    private static final HttpRequest HTTP_REQUEST;

    static {
        try {
            HTTP_REQUEST = new HttpRequest(HttpMethod.POST, createUrl("http://request.url"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void constructor() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, VALUE)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());
            assertEquals(VALUE, response.getValue());
            assertNotNull(response.getBody());
            assertNotEquals(0, response.getBody().toBytes().length); // Non-empty body.
        }
    }

    @Test
    public void constructorWithNullValue() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, null)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());
            assertNull(response.getValue());
            assertNotNull(response.getBody());
            assertEquals(0, response.getBody().toBytes().length); // Empty body.
        }
    }

    @Test
    public void constructorWithNullValueAndBodySet() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, null)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());

            HttpResponseAccessHelper.setBody(response, BODY);

            // No body deserializer means we don't use the body and the value does not get populated.
            assertNull(response.getValue());
            assertEquals(BODY, response.getBody());
        }
    }

    @Test
    public void constructorWithNullValueBodySetAndBodyDeserializer() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, null)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());
            assertNull(response.getValue()); // null value.

            AtomicBoolean wasBodyDeserialized = new AtomicBoolean(false);

            HttpResponseAccessHelper.setBody(response, BODY);
            HttpResponseAccessHelper.setBodyDeserializer(response, binaryData -> {
                wasBodyDeserialized.set(true);

                return binaryData.toString();
            });

            assertFalse(wasBodyDeserialized.get());
            assertEquals(VALUE, response.getValue()); // Value got deserialized from body.
            assertTrue(wasBodyDeserialized.get());
        }
    }

    @Test
    public void constructorWithValueDoesNotDeserializeBody() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, VALUE)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());

            AtomicBoolean wasBodyDeserialized = new AtomicBoolean(false);

            HttpResponseAccessHelper.setBody(response, BODY);
            HttpResponseAccessHelper.setBodyDeserializer(response, binaryData -> {
                wasBodyDeserialized.set(true);

                return binaryData.toString();
            });

            assertEquals(VALUE, response.getValue()); // Return value passed to constructor, deserializer is not called.
            assertFalse(wasBodyDeserialized.get());
        }
    }
}
