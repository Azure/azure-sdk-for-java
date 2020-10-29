// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.util.Option;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for {@link Option} that can represent tri-sate (non-null-value, null-value, or no-value).
 */
public class OptionSerializerTests {
    @Test
    public void canSerializeExplicitNull() throws IOException {
        PatchModel model = new PatchModel();

        model.setSku(Option.of(null));
        String serialized = new JacksonAdapter().serialize(model, SerializerEncoding.JSON);
        Assertions.assertEquals("{\"sku\":null}", serialized);

        model.setSku(Option.empty());
        serialized = new JacksonAdapter().serialize(model, SerializerEncoding.JSON);
        Assertions.assertEquals("{\"sku\":null}", serialized);
    }

    @Test
    public void shouldIgnoreImplicitNull() throws IOException {
        PatchModel model = new PatchModel();
        String serialized = new JacksonAdapter().serialize(model, SerializerEncoding.JSON);
        Assertions.assertEquals("{}", serialized);
    }

    @Test
    public void shouldIgnoreUninitialized() throws IOException {
        PatchModel model = new PatchModel();
        model.setSku(Option.uninitialized());
        String serialized = new JacksonAdapter().serialize(model, SerializerEncoding.JSON);
        Assertions.assertEquals("{}", serialized);
    }

    @Test
    public void canSerializeNonNullValue() throws IOException {
        PatchModel model = new PatchModel();
        model.setSku(Option.of("basic"));
        String serialized = new JacksonAdapter().serialize(model, SerializerEncoding.JSON);
        Assertions.assertEquals("{\"sku\":\"basic\"}", serialized);
    }

    @Test
    public void canSerializeRawType() throws IOException {
        @SuppressWarnings("rawtypes")
        final Option rawOption = Option.of(new RawModel().setName("test"));
        String serialized = new JacksonAdapter().serialize(rawOption, SerializerEncoding.JSON);
        Assertions.assertEquals("{\"name\":\"test\"}", serialized);

        @SuppressWarnings("rawtypes")
        final Option rawOption1 = Option.of("test");
        String serialized1 = new JacksonAdapter().serialize(rawOption1, SerializerEncoding.JSON);
        Assertions.assertEquals("\"test\"", serialized1);
    }

    private static class PatchModel {
        @JsonProperty("sku")
        private Option<String> sku;

        PatchModel setSku(Option<String> sku) {
            this.sku = sku;
            return this;
        }
    }

    private static class RawModel {
        @JsonProperty("name")
        private String name;

        RawModel setName(String name) {
            this.name = name;
            return this;
        }
    }
}
