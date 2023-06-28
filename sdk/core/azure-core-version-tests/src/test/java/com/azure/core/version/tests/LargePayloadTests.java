// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.version.tests;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that a large payload doesn't trigger StreamReadConstraints introduced in Jackson 2.15.0.
 */
public class LargePayloadTests {
    private static final String LARGE_JSON;
    private static final String TOO_LARGE_JSON;
    private static final String LARGE_XML;
    private static final String TOO_LARGE_XML;

    static {
        StringBuilder sb = new StringBuilder(2048);
        for (int i = 0; i < 128; i++) {
            sb.append("Hello big world!");
        }

        String intermediateString = sb.toString();

        StringBuilder bigString = new StringBuilder(40 * 1024 * 1024);
        for (int i = 0; i < 20480; i++) {
            bigString.append(intermediateString);
        }

        String oneBigString = bigString.toString(); // 40MB character string.

        LARGE_JSON = "\"" + oneBigString + "\"";
        TOO_LARGE_JSON = "\"" + oneBigString + oneBigString + "\"";
        LARGE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bigstring>" + oneBigString + "</bigstring>";
        TOO_LARGE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bigstring>" + oneBigString + oneBigString
            + "</bigstring>";
    }

    @Test
    public void largeJsonPayloadCore() {
        assertDoesNotThrow(() -> JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(LARGE_JSON, String.class, SerializerEncoding.JSON));
    }

    @Test
    public void largeXmlPayloadCore() {
        // While it seems Jackson doesn't validate this, this test passes and can be a smoke test if that changes
        // in the future.
        assertDoesNotThrow(() -> JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(LARGE_XML, String.class, SerializerEncoding.XML));
    }

    @Test
    public void largeJsonPayloadSerializerJackson() {
        assertDoesNotThrow(() -> com.azure.core.serializer.json.jackson.JacksonAdapter.defaultSerializerAdapter()
            .deserialize(LARGE_JSON, String.class, SerializerEncoding.JSON));
    }

    @Test
    public void largeXmlPayloadSerializerJackson() {
        // While it seems Jackson doesn't validate this, this test passes and can be a smoke test if that changes
        // in the future.
        assertDoesNotThrow(() -> com.azure.core.serializer.json.jackson.JacksonAdapter.defaultSerializerAdapter()
            .deserialize(LARGE_XML, String.class, SerializerEncoding.XML));
    }

    @Test
    public void tooLargeJsonPayload() {
        assertThrows(IOException.class, () -> JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(TOO_LARGE_JSON, String.class, SerializerEncoding.JSON));
    }

    // It seems Jackson doesn't validate this in XML.
//    @Test
//    public void tooLargeXmlPayload() {
//        assertThrows(IOException.class, () -> JacksonAdapter.createDefaultSerializerAdapter()
//            .deserialize(TOO_LARGE_XML, String.class, SerializerEncoding.XML));
//    }

    @Test
    public void tooLargeJsonPayloadSerializerJackson() {
        assertThrows(IOException.class,
            () -> com.azure.core.serializer.json.jackson.JacksonAdapter.defaultSerializerAdapter()
                .deserialize(TOO_LARGE_JSON, String.class, SerializerEncoding.JSON));
    }

    // It seems Jackson doesn't validate this in XML.
//    @Test
//    public void tooLargeXmlPayloadSerializerJackson() {
//        assertThrows(IOException.class,
//            () -> com.azure.core.serializer.json.jackson.JacksonAdapter.defaultSerializerAdapter()
//                .deserialize(TOO_LARGE_XML, String.class, SerializerEncoding.XML));
//    }
}
