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
 * Tests for VoiceResponse serialization, focusing on the maxOutputTokens union type handling. VoiceResponse shadows
 * the maxOutputTokens field declared by {@link VoiceResponseBase}, so the typed getters inherited from the base class
 * must observe the subclass value.
 * maxOutputTokens is a union type: int32 | "inf".
 */
public class VoiceResponseSerializationTests {

    @Test
    public void testDeserializationWithMaxOutputTokensAsInt() throws IOException {
        String json = "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\",\"max_output_tokens\":2048}";

        VoiceResponse response = deserializeFromJson(json);

        assertEquals("resp_1234", response.getId());
        assertEquals(Integer.valueOf(2048), response.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testDeserializationWithMaxOutputTokensAsString() throws IOException {
        String json = "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\",\"max_output_tokens\":\"inf\"}";

        VoiceResponse response = deserializeFromJson(json);

        assertEquals("inf", response.getMaxOutputTokensAsString());
    }

    @Test
    public void testDeserializationWithoutMaxOutputTokens() throws IOException {
        String json = "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\"}";

        VoiceResponse response = deserializeFromJson(json);

        assertNull(response.getMaxOutputTokensAsInteger());
        assertNull(response.getMaxOutputTokensAsString());
    }

    @Test
    public void testSerializationWithMaxOutputTokensAsInt() throws IOException {
        VoiceResponse response = deserializeFromJson(
            "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\",\"max_output_tokens\":2048}");

        String json = serializeToJson(response);

        assertNotNull(json);
        assertTrue(json.contains("\"max_output_tokens\":2048"), "Expected a raw JSON number, got: " + json);
    }

    @Test
    public void testSerializationWithMaxOutputTokensAsString() throws IOException {
        VoiceResponse response = deserializeFromJson(
            "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\",\"max_output_tokens\":\"inf\"}");

        String json = serializeToJson(response);

        assertTrue(json.contains("\"max_output_tokens\":\"inf\""), "Expected a quoted JSON string, got: " + json);
    }

    @Test
    public void testSerializationWithoutMaxOutputTokens() throws IOException {
        VoiceResponse response = deserializeFromJson("{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\"}");

        String json = serializeToJson(response);

        assertFalse(json.contains("\"max_output_tokens\""));
    }

    @Test
    public void testRoundTripWithMaxOutputTokensAsInt() throws IOException {
        VoiceResponse original = deserializeFromJson(
            "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\",\"max_output_tokens\":1024}");

        VoiceResponse deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals(Integer.valueOf(1024), deserialized.getMaxOutputTokensAsInteger());
    }

    @Test
    public void testRoundTripWithMaxOutputTokensAsString() throws IOException {
        VoiceResponse original = deserializeFromJson(
            "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\",\"max_output_tokens\":\"inf\"}");

        VoiceResponse deserialized = deserializeFromJson(serializeToJson(original));

        assertEquals("inf", deserialized.getMaxOutputTokensAsString());
    }

    @Test
    public void testMaxOutputTokensCrossVariantReturnsNull() throws IOException {
        VoiceResponse numeric = deserializeFromJson(
            "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\",\"max_output_tokens\":2048}");
        assertNull(numeric.getMaxOutputTokensAsString());

        VoiceResponse text = deserializeFromJson(
            "{\"id\":\"resp_1234\",\"conversation_id\":\"conv_1234\",\"max_output_tokens\":\"inf\"}");
        assertNull(text.getMaxOutputTokensAsInteger());
    }

    /**
     * VoiceResponse shadows the base-class field, so the raw accessor is overridden. The override must stay
     * non-public, and the typed getters must not be duplicated on the subclass.
     */
    @Test
    public void testShadowedAccessorsDoNotDuplicatePublicApi() throws NoSuchMethodException {
        Method rawGetter = VoiceResponse.class.getDeclaredMethod("getMaxOutputTokens");
        assertFalse(Modifier.isPublic(rawGetter.getModifiers()),
            "getMaxOutputTokens() must not be public on VoiceResponse");
        assertEquals(BinaryData.class, rawGetter.getReturnType());

        assertEquals(1, countPublicMethods("getMaxOutputTokensAsInteger"),
            "getMaxOutputTokensAsInteger() must be declared once, on VoiceResponseBase");
        assertEquals(1, countPublicMethods("getMaxOutputTokensAsString"),
            "getMaxOutputTokensAsString() must be declared once, on VoiceResponseBase");
    }

    private static int countPublicMethods(String methodName) {
        int count = 0;
        for (Method method : VoiceResponse.class.getMethods()) {
            if (methodName.equals(method.getName())) {
                count++;
            }
        }
        return count;
    }

    private String serializeToJson(VoiceResponse response) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            response.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    private VoiceResponse deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return VoiceResponse.fromJson(jsonReader);
        }
    }
}
