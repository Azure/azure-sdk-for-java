// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.eventgrid.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.eventgrid.models.EventDefinitionKind;
import com.azure.resourcemanager.eventgrid.models.EventTypeInfo;
import com.azure.resourcemanager.eventgrid.models.InlineEventProperties;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class EventTypeInfoTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        EventTypeInfo model = BinaryData.fromString(
            "{\"kind\":\"Inline\",\"inlineEventTypes\":{\"h\":{\"description\":\"napczwlokjy\",\"displayName\":\"kkvnipjox\",\"documentationUrl\":\"nchgej\",\"dataSchemaUrl\":\"odmailzyd\"},\"jozkrwfndiod\":{\"description\":\"wyahuxinpmqnja\",\"displayName\":\"ixjsprozvcputeg\",\"documentationUrl\":\"wmfdatscmdvpjhul\",\"dataSchemaUrl\":\"uvm\"},\"fdosyg\":{\"description\":\"slwejdpvw\",\"displayName\":\"oqpsoa\",\"documentationUrl\":\"tazak\",\"dataSchemaUrl\":\"lahbcryff\"},\"tfell\":{\"description\":\"paojakhmsbzjh\",\"displayName\":\"zevdphlx\",\"documentationUrl\":\"lthqtrgqjbp\",\"dataSchemaUrl\":\"fsinzgvfcjrwzoxx\"}}}")
            .toObject(EventTypeInfo.class);
        Assertions.assertEquals(EventDefinitionKind.INLINE, model.kind());
        Assertions.assertEquals("napczwlokjy", model.inlineEventTypes().get("h").description());
        Assertions.assertEquals("kkvnipjox", model.inlineEventTypes().get("h").displayName());
        Assertions.assertEquals("nchgej", model.inlineEventTypes().get("h").documentationUrl());
        Assertions.assertEquals("odmailzyd", model.inlineEventTypes().get("h").dataSchemaUrl());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        EventTypeInfo model = new EventTypeInfo().withKind(EventDefinitionKind.INLINE)
            .withInlineEventTypes(mapOf("h",
                new InlineEventProperties().withDescription("napczwlokjy")
                    .withDisplayName("kkvnipjox")
                    .withDocumentationUrl("nchgej")
                    .withDataSchemaUrl("odmailzyd"),
                "jozkrwfndiod",
                new InlineEventProperties().withDescription("wyahuxinpmqnja")
                    .withDisplayName("ixjsprozvcputeg")
                    .withDocumentationUrl("wmfdatscmdvpjhul")
                    .withDataSchemaUrl("uvm"),
                "fdosyg",
                new InlineEventProperties().withDescription("slwejdpvw")
                    .withDisplayName("oqpsoa")
                    .withDocumentationUrl("tazak")
                    .withDataSchemaUrl("lahbcryff"),
                "tfell",
                new InlineEventProperties().withDescription("paojakhmsbzjh")
                    .withDisplayName("zevdphlx")
                    .withDocumentationUrl("lthqtrgqjbp")
                    .withDataSchemaUrl("fsinzgvfcjrwzoxx")));
        model = BinaryData.fromObject(model).toObject(EventTypeInfo.class);
        Assertions.assertEquals(EventDefinitionKind.INLINE, model.kind());
        Assertions.assertEquals("napczwlokjy", model.inlineEventTypes().get("h").description());
        Assertions.assertEquals("kkvnipjox", model.inlineEventTypes().get("h").displayName());
        Assertions.assertEquals("nchgej", model.inlineEventTypes().get("h").documentationUrl());
        Assertions.assertEquals("odmailzyd", model.inlineEventTypes().get("h").dataSchemaUrl());
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
