// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.serializer;

import com.typespec.core.annotation.HeaderCollection;
import com.typespec.core.exception.HttpResponseException;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.MockHttpResponse;
import com.typespec.core.util.mocking.MockSerializerAdapter;
import com.typespec.core.util.serializer.JacksonAdapter;
import com.typespec.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link HttpResponseHeaderDecoder}.
 */
public class HttpResponseHeaderDecoderTests {
    @Test
    public void nullHeaderTypeReturnsMonoEmpty() {
        assertNull(HttpResponseHeaderDecoder.decode(null, null, null));
    }

    @Test
    public void ioExceptionIsMappedToHttpResponseException() {
        SerializerAdapter serializer = new MockSerializerAdapter() {
            @Override
            public <T> T deserialize(HttpHeaders headers, Type type) throws IOException {
                throw new IOException();
            }
        };

        try (HttpResponse response = new MockHttpResponse(null, 200)) {
            assertThrows(HttpResponseException.class,
                () -> HttpResponseHeaderDecoder.decode(response, serializer, MockHeaders.class));
        }
    }

    @Test
    public void headersAreDeserializedToType() {
        try (HttpResponse response = new MockHttpResponse(null, 200, new HttpHeaders()
            .set(HttpHeaderName.fromString("mock-a"), "a"))) {

            Object actual = assertDoesNotThrow(() -> HttpResponseHeaderDecoder.decode(response, new JacksonAdapter(),
                MockHeaders.class));
            assertTrue(actual instanceof MockHeaders);
            MockHeaders mockHeaders = (MockHeaders) actual;
            assertEquals(Collections.singletonMap("a", "a"), mockHeaders.getHeaderCollection());
        }
    }

    public static final class MockHeaders {
        @HeaderCollection("mock-")
        private Map<String, String> headerCollection;

        public Map<String, String> getHeaderCollection() {
            return headerCollection;
        }

        public void setHeaderCollection(Map<String, String> headerCollection) {
            this.headerCollection = headerCollection;
        }
    }
}
