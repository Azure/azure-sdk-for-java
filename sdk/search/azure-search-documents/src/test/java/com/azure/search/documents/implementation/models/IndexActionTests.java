// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.models;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.implementation.converters.IndexActionConverter;
import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.models.IndexActionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link IndexAction}.
 */
public class IndexActionTests {
    @Test
    public void nullIsIncludedInMapSerialization() throws IOException {
        ObjectSerializer nullIncludingSerializer = new JacksonJsonSerializerBuilder()
            .serializer(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS))
            .build();

        com.azure.search.documents.models.IndexAction<Map<String, Object>> action =
            new com.azure.search.documents.models.IndexAction<Map<String, Object>>()
                .setActionType(IndexActionType.MERGE)
                .setDocument(Collections.singletonMap("null", null));

        String json = Utility.getDefaultSerializerAdapter()
            .serialize(IndexActionConverter.map(action, nullIncludingSerializer), SerializerEncoding.JSON);

        assertEquals("{\"@search.action\":\"merge\",\"null\":null}", json);
    }

    @Test
    public void nullIsIncludedInTypedSerialization() throws IOException {
        ObjectSerializer nullIncludingSerializer = new JacksonJsonSerializerBuilder()
            .serializer(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.ALWAYS))
            .build();

        com.azure.search.documents.models.IndexAction<ClassWithNullableField> action =
            new com.azure.search.documents.models.IndexAction<ClassWithNullableField>()
                .setActionType(IndexActionType.MERGE)
                .setDocument(new ClassWithNullableField());

        String json = Utility.getDefaultSerializerAdapter()
            .serialize(IndexActionConverter.map(action, nullIncludingSerializer), SerializerEncoding.JSON);

        assertEquals("{\"@search.action\":\"merge\",\"null\":null}", json);
    }

    @Test
    public void nullIsExcludedInMapSerialization() throws IOException {
        com.azure.search.documents.models.IndexAction<Map<String, Object>> action =
            new com.azure.search.documents.models.IndexAction<Map<String, Object>>()
                .setActionType(IndexActionType.MERGE)
                .setDocument(Collections.singletonMap("null", null));

        String json = Utility.getDefaultSerializerAdapter()
            .serialize(IndexActionConverter.map(action, null), SerializerEncoding.JSON);

        assertEquals("{\"@search.action\":\"merge\"}", json);
    }

    @Test
    public void nullIsExcludedInTypedSerialization() throws IOException {
        com.azure.search.documents.models.IndexAction<ClassWithNullableField> action =
            new com.azure.search.documents.models.IndexAction<ClassWithNullableField>()
                .setActionType(IndexActionType.MERGE)
                .setDocument(new ClassWithNullableField());

        String json = Utility.getDefaultSerializerAdapter()
            .serialize(IndexActionConverter.map(action, null), SerializerEncoding.JSON);

        assertEquals("{\"@search.action\":\"merge\"}", json);
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
