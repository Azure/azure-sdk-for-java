// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.models;

import com.generic.core.implementation.http.HttpResponseAccessHelper;
import com.generic.core.models.BinaryData;
import com.generic.core.models.HeaderName;
import com.generic.core.models.Headers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;

import static com.generic.core.util.TestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HttpResponseTests {
    private static final int STATUS_CODE = 200;
    private static final Headers HEADERS = new Headers().add(HeaderName.CONTENT_TYPE, "application/text");
    private static final String VALUE = "Response body";
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
            assertNotEquals(0, response.getBody().toBytes().length);
        }
    }

    @Test
    public void constructorWithNullValue() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, null)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());
            assertNull(response.getValue());

            BinaryData responseBody = response.getBody();

            assertNotNull(responseBody);
            assertEquals(0, responseBody.toBytes().length);
            assertNull(response.getValue());
        }
    }

    @Test
    public void constructorWithNullValueAndBodySet() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, null)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());
            assertNull(response.getValue());

            HttpResponseAccessHelper.setBody(response, BinaryData.fromString("Response body"));

            BinaryData responseBody = response.getBody();

            assertNotNull(responseBody);
            assertNotEquals(0, responseBody.toBytes().length);
            assertNull(response.getValue()); // No body deserializer means the value does not get populated.
        }
    }

    @Test
    public void constructorWithNullValueAndBodySupplier() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, null)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());
            assertNull(response.getValue());

            HttpResponseAccessHelper.setBodySupplier(response, () -> BinaryData.fromString("Response body"));

            BinaryData responseBody = response.getBody();

            assertNotNull(responseBody);
            assertNotEquals(0, responseBody.toBytes().length);
            assertNull(response.getValue()); // No body deserializer means the value does not get populated.
        }
    }

    @Test
    public void constructorWithNullValueBodySetAndBodyDeserializer() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, null)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());
            assertNull(response.getValue());

            HttpResponseAccessHelper.setBody(response, BinaryData.fromString(VALUE));
            HttpResponseAccessHelper.setBodyDeserializer(response, BinaryData::toString);

            BinaryData responseBody = response.getBody();

            assertNotNull(responseBody);
            assertNotEquals(0, responseBody.toBytes().length);
            assertEquals(VALUE, response.getValue());
        }
    }

    @Test
    public void constructorWithNullValueBodySupplierAndBodyDeserializer() throws IOException {
        try (HttpResponse<?> response = new HttpResponse<>(HTTP_REQUEST, STATUS_CODE, HEADERS, null)) {
            assertEquals(HTTP_REQUEST, response.getRequest());
            assertEquals(STATUS_CODE, response.getStatusCode());
            assertEquals(HEADERS, response.getHeaders());
            assertNull(response.getValue());

            HttpResponseAccessHelper.setBodySupplier(response, () -> BinaryData.fromString(VALUE));
            HttpResponseAccessHelper.setBodyDeserializer(response, BinaryData::toString);

            BinaryData responseBody = response.getBody();

            assertNotNull(responseBody);
            assertNotEquals(0, responseBody.toBytes().length);
            assertEquals(VALUE, response.getValue());
        }
    }
}
