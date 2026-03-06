// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link StacLink}.
 */
public final class StacLinkTests {

    @Test
    public void testDeserialize() throws Exception {
        StacLink model
            = BinaryData.fromString("{\"rel\":\"self\",\"title\":\"Self Link\",\"type\":\"application/json\","
                + "\"href\":\"https://example.com/collections\",\"hreflang\":\"en\","
                + "\"length\":1024,\"method\":\"GET\"," + "\"headers\":{\"Accept\":\"application/json\"},"
                + "\"body\":{\"key\":\"value\"},\"merge\":true}").toObject(StacLink.class);
        Assertions.assertEquals("self", model.getRel());
        Assertions.assertEquals("Self Link", model.getTitle());
        Assertions.assertEquals(StacLinkType.APPLICATION_JSON, model.getType());
        Assertions.assertEquals("https://example.com/collections", model.getHref());
        Assertions.assertEquals("en", model.getHreflang());
        Assertions.assertEquals(1024, model.getLength());
        Assertions.assertEquals(StacLinkMethod.GET, model.getMethod());
        Assertions.assertNotNull(model.getHeaders());
        Assertions.assertEquals("application/json", model.getHeaders().get("Accept"));
        Assertions.assertNotNull(model.getBody());
        Assertions.assertEquals("value", model.getBody().get("key"));
        Assertions.assertTrue(model.isMerge());
    }

    @Test
    public void testSerialize() throws Exception {
        StacLink model = new StacLink().setRel("self")
            .setTitle("Self Link")
            .setType(StacLinkType.APPLICATION_JSON)
            .setHref("https://example.com/collections")
            .setHreflang("en")
            .setLength(1024)
            .setMethod(StacLinkMethod.GET)
            .setHeaders(mapOf("Accept", "application/json"))
            .setBody(mapOf("key", "value"))
            .setMerge(true);
        model = BinaryData.fromObject(model).toObject(StacLink.class);
        Assertions.assertEquals("self", model.getRel());
        Assertions.assertEquals("Self Link", model.getTitle());
        Assertions.assertEquals(StacLinkType.APPLICATION_JSON, model.getType());
        Assertions.assertEquals("https://example.com/collections", model.getHref());
        Assertions.assertEquals("en", model.getHreflang());
        Assertions.assertEquals(1024, model.getLength());
        Assertions.assertEquals(StacLinkMethod.GET, model.getMethod());
        Assertions.assertTrue(model.isMerge());
    }

    @Test
    public void testDeserializeMinimal() throws Exception {
        StacLink model
            = BinaryData.fromString("{\"rel\":\"root\",\"href\":\"https://example.com\"}").toObject(StacLink.class);
        Assertions.assertEquals("root", model.getRel());
        Assertions.assertEquals("https://example.com", model.getHref());
        Assertions.assertNull(model.getTitle());
        Assertions.assertNull(model.getType());
        Assertions.assertNull(model.getLength());
        Assertions.assertNull(model.getMethod());
        Assertions.assertNull(model.getHeaders());
        Assertions.assertNull(model.getBody());
        Assertions.assertNull(model.isMerge());
    }

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
