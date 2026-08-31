// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for VoiceResponseBase serialization, focusing on the maxOutputTokens union type handling.
 * maxOutputTokens is a union type: int32 | "inf".
 */
public class VoiceResponseBaseSerializationTests {

    @Test
    public void testSerializationWithoutMaxOutputTokens() throws IOException {
        String json = serializeToJson(new VoiceResponseBase());

        assertNotNull(json);
        assertFalse(json.contains("\"max_output_tokens\""));
    }

    @Test
    public void testSerializationWithMaxOutputTokensAsInt() throws IOException {
        VoiceResponseBase response = new VoiceResponseBase().setMaxOutputTokens(BinaryData.fromObject(4096));

        String json = serializeToJson(response);

        assertTrue(json.contains("\"max_output_tokens\":4096"), "Expected a raw JSON number, got: " + json);
    }

    @Test
    public void testSerializationWithMaxOutputTokensAsString() throws IOException {
        VoiceResponseBase response = new VoiceResponseBase().setMaxOutputTokens(BinaryData.fromObject("inf"));

        String json = serializeToJson(response);

        assertTrue(json.contains("\"max_output_tokens\":\"inf\""), "Expected a quoted JSON string, got: " + json);
    }

    @Test
    public void testDirectSetGetMaxOutputTokens() {
        VoiceResponseBase numeric = new VoiceResponseBase().setMaxOutputTokens(BinaryData.fromObject(4096));
        assertEquals(Integer.valueOf(4096), numeric.getMaxOutputTokensAsInteger());
        assertNull(numeric.getMaxOutputTokensAsString());

        VoiceResponseBase text = new VoiceResponseBase().setMaxOutputTokens(BinaryData.fromObject("inf"));
        assertEquals("inf", text.getMaxOutputTokensAsString());
        assertNull(text.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testMaxOutputTokensCrossVariantReturnsNullAfterDeserialization() throws IOException {
        VoiceResponseBase numeric = deserializeFromJson("{\"id\":\"resp_1234\",\"max_output_tokens\":2048}");
        assertNull(numeric.getMaxOutputTokensAsString());

        VoiceResponseBase text = deserializeFromJson("{\"id\":\"resp_1234\",\"max_output_tokens\":\"inf\"}");
        assertNull(text.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testDeserializationWithMaxOutputTokensAsInt() throws IOException {
        String json = "{\"id\":\"resp_1234\",\"max_output_tokens\":2048}";

        VoiceResponseBase response = deserializeFromJson(json);

        assertEquals(Integer.valueOf(2048), response.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testDeserializationWithMaxOutputTokensAsString() throws IOException {
        String json = "{\"id\":\"resp_1234\",\"max_output_tokens\":\"inf\"}";

        VoiceResponseBase response = deserializeFromJson(json);

        assertEquals("inf", response.getMaxOutputTokensAsString());
    }

    @Test
    public void testDeserializationWithoutMaxOutputTokens() throws IOException {
        String json = "{\"id\":\"resp_1234\"}";

        VoiceResponseBase response = deserializeFromJson(json);

        assertNull(response.getMaxOutputTokensAsInteger());
        assertNull(response.getMaxOutputTokensAsString());
    }

    @Test
    public void testRoundTripWithMaxOutputTokensAsInt() throws IOException {
        VoiceResponseBase original = new VoiceResponseBase().setMaxOutputTokens(BinaryData.fromObject(1024));

        VoiceResponseBase deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals(Integer.valueOf(1024), deserialized.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testRoundTripWithMaxOutputTokensAsString() throws IOException {
        VoiceResponseBase original = new VoiceResponseBase().setMaxOutputTokens(BinaryData.fromObject("inf"));

        VoiceResponseBase deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals("inf", deserialized.getMaxOutputTokensAsString());
    }

    @Test
    public void testNullArgumentClearsMaxOutputTokens() throws IOException {
        VoiceResponseBase response
            = new VoiceResponseBase().setMaxOutputTokens(BinaryData.fromObject("inf")).setMaxOutputTokens(null);

        assertNull(response.getMaxOutputTokensAsInteger());
        assertNull(response.getMaxOutputTokensAsString());
        assertFalse(serializeToJson(response).contains("\"max_output_tokens\""));
    }

    /**
     * The raw {@link BinaryData} accessor must not be part of the public API surface; only the typed getters are.
     */
    @Test
    public void testRawBinaryDataAccessorIsNotPublic() throws NoSuchMethodException {
        Method rawGetter = VoiceResponseBase.class.getDeclaredMethod("getMaxOutputTokens");

        assertFalse(Modifier.isPublic(rawGetter.getModifiers()),
            "getMaxOutputTokens() must not be public on VoiceResponseBase");
        assertEquals(BinaryData.class, rawGetter.getReturnType());
    }

    private String serializeToJson(VoiceResponseBase response) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            response.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    private VoiceResponseBase deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return VoiceResponseBase.fromJson(jsonReader);
        }
    }
}
