// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

/**
 * Tests for {@link SerializerEncoding}.
 */
class SerializerEncodingTests {
    private static final String CONTENT_TYPE = "Content-Type";

    @ParameterizedTest
    @ValueSource(strings = {"application/xml", "application/atom+xml", "text/xml", "application/foo+XML", "TEXT/XML",
        "application/xml;charset=utf-8", "application/atom+xml; charset=utf-32"})
    void recognizeXml(String mimeType) {
        // Arrange
        HttpHeaders headers = new HttpHeaders(Collections.singletonMap(CONTENT_TYPE, mimeType));

        // Act & Assert
        Assertions.assertEquals(SerializerEncoding.XML, SerializerEncoding.fromHeaders(headers));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "application/kv+json", "APPLICATION/JSON", "application/FOO+JSON",
        "application/json;charset=utf-8", "application/config+json; charset=utf-32"})
    void recognizeJson(String mimeType) {
        // Arrange
        HttpHeaders headers = new HttpHeaders(Collections.singletonMap(CONTENT_TYPE, mimeType));

        // Act & Assert
        Assertions.assertEquals(SerializerEncoding.JSON, SerializerEncoding.fromHeaders(headers));
    }

    @Test
    void defaultNoContentType() {
        // Arrange
        HttpHeaders headers = new HttpHeaders(Collections.singletonMap("Http-Method", "GET"));

        // Act & Assert
        Assertions.assertEquals(SerializerEncoding.JSON, SerializerEncoding.fromHeaders(headers));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/binary", "invalid-mime-type"})
    void defaultUnsupportedType(String mimeType) {
        // Arrange
        HttpHeaders headers = new HttpHeaders(Collections.singletonMap(CONTENT_TYPE, mimeType));

        // Act & Assert
        Assertions.assertEquals(SerializerEncoding.JSON, SerializerEncoding.fromHeaders(headers));
    }
}
