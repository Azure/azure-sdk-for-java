// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.models;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.json.JsonProviders;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.models.IndexActionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link IndexAction}.
 */
public class IndexActionTests {
    @Test
    public void nullIsIncludedInMapSerialization() {
        ObjectSerializer nullIncludingSerializer = new JacksonJsonSerializerBuilder()
            .serializer(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS))
            .build();

        com.azure.search.documents.models.IndexAction<Map<String, Object>> action =
            new com.azure.search.documents.models.IndexAction<Map<String, Object>>()
                .setActionType(IndexActionType.MERGE)
                .setDocument(Collections.singletonMap("null", null));

        String json = convertToJson(IndexActionConverter.map(action, nullIncludingSerializer));

        assertEquals("{\"@search.action\":\"merge\",\"null\":null}", json);
    }

    @Test
    public void nullIsIncludedInTypedSerialization() {
        ObjectSerializer nullIncludingSerializer = new JacksonJsonSerializerBuilder()
            .serializer(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS))
            .build();

        com.azure.search.documents.models.IndexAction<ClassWithNullableField> action =
            new com.azure.search.documents.models.IndexAction<ClassWithNullableField>()
                .setActionType(IndexActionType.MERGE)
                .setDocument(new ClassWithNullableField());

        String json = convertToJson(IndexActionConverter.map(action, nullIncludingSerializer));

        assertEquals("{\"@search.action\":\"merge\",\"null\":null}", json);
    }

    @Test
    public void nullIsExcludedInMapSerialization() {
        ObjectSerializer nullExcludingSerializer = new JacksonJsonSerializerBuilder()
            .serializer(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL))
            .build();

        com.azure.search.documents.models.IndexAction<Map<String, Object>> action =
            new com.azure.search.documents.models.IndexAction<Map<String, Object>>()
                .setActionType(IndexActionType.MERGE)
                .setDocument(Collections.singletonMap("null", null));

        String json = convertToJson(IndexActionConverter.map(action, nullExcludingSerializer));

        assertEquals("{\"@search.action\":\"merge\"}", json);
    }

    @Test
    public void nullIsExcludedInTypedSerialization() {
        ObjectSerializer nullExcludingSerializer = new JacksonJsonSerializerBuilder()
            .serializer(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL))
            .build();

        com.azure.search.documents.models.IndexAction<ClassWithNullableField> action =
            new com.azure.search.documents.models.IndexAction<ClassWithNullableField>()
                .setActionType(IndexActionType.MERGE)
                .setDocument(new ClassWithNullableField());

        String json = convertToJson(IndexActionConverter.map(action, nullExcludingSerializer));

        assertEquals("{\"@search.action\":\"merge\"}", json);
    }

    private static String convertToJson(JsonSerializable<?> jsonSerializable) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            writer.writeJson(jsonSerializable).flush();
            return outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static final class ClassWithNullableField {
        @JsonProperty("null")
        private String nullable;

        public String getNullable() {
            return nullable;
        }

        public ClassWithNullableField setNullable(String nullable) {
            this.nullable = nullable;
            return this;
        }
    }
}
