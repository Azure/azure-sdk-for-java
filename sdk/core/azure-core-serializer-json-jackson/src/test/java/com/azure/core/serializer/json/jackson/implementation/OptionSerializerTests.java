// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.core.implementation.Option;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests for {@link Option} that can represent tri-sate (non-null-value, null-value, or no-value).
 */
public class OptionSerializerTests {
    private static final ObjectMapperShim MAPPER = ObjectMapperShim.createJsonMapper(
        ObjectMapperShim.createSimpleMapper(), (outer, inner) -> outer.registerModule(new OptionModule()));

    @Test
    public void canSerializeExplicitNull() throws IOException {
        PatchModel model = new PatchModel();

        model.setSku(Option.of(null));
        String serialized = MAPPER.writeValueAsString(model);
        Assertions.assertEquals("{\"sku\":null}", serialized);

        model.setSku(Option.empty());
        serialized = MAPPER.writeValueAsString(model);
        Assertions.assertEquals("{\"sku\":null}", serialized);
    }

    @Test
    public void shouldIgnoreImplicitNull() throws IOException {
        PatchModel model = new PatchModel();
        String serialized = MAPPER.writeValueAsString(model);
        Assertions.assertEquals("{}", serialized);
    }

    @Test
    public void shouldIgnoreUninitialized() throws IOException {
        PatchModel model = new PatchModel();
        model.setSku(Option.uninitialized());
        String serialized = MAPPER.writeValueAsString(model);
        Assertions.assertEquals("{}", serialized);
    }

    @Test
    public void canSerializeNonNullValue() throws IOException {
        PatchModel model = new PatchModel();
        model.setSku(Option.of("basic"));
        String serialized = MAPPER.writeValueAsString(model);
        Assertions.assertEquals("{\"sku\":\"basic\"}", serialized);
    }

    @Test
    public void canSerializeRawType() throws IOException {
        @SuppressWarnings("rawtypes")
        final Option rawOption = Option.of(new RawModel().setName("test"));
        String serialized = MAPPER.writeValueAsString(rawOption);
        Assertions.assertEquals("{\"name\":\"test\"}", serialized);

        @SuppressWarnings("rawtypes")
        final Option rawOption1 = Option.of("test");
        String serialized1 = MAPPER.writeValueAsString(rawOption1);
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
