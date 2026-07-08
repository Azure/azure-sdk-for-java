// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link StacAsset}.
 */
public final class StacAssetTests {

    @Test
    public void testDeserialize() throws Exception {
        StacAsset model = BinaryData
            .fromString("{\"platform\":\"sentinel-2\",\"instruments\":[\"msi\"],\"constellation\":\"esa\","
                + "\"mission\":\"s2\",\"gsd\":10.0,\"created\":\"2021-06-01T00:00:00Z\","
                + "\"updated\":\"2021-07-01T00:00:00Z\",\"title\":\"Band 4\","
                + "\"description\":\"Red band\",\"href\":\"https://example.com/asset.tif\","
                + "\"type\":\"image/tiff\",\"roles\":[\"data\",\"visual\"]," + "\"extra_field\":\"extra_value\"}")
            .toObject(StacAsset.class);
        Assertions.assertEquals("sentinel-2", model.getPlatform());
        Assertions.assertEquals(1, model.getInstruments().size());
        Assertions.assertEquals("msi", model.getInstruments().get(0));
        Assertions.assertEquals("esa", model.getConstellation());
        Assertions.assertEquals("s2", model.getMission());
        Assertions.assertEquals(10.0, model.getGsd());
        Assertions.assertEquals(OffsetDateTime.of(2021, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC), model.getCreated());
        Assertions.assertEquals(OffsetDateTime.of(2021, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC), model.getUpdated());
        Assertions.assertEquals("Band 4", model.getTitle());
        Assertions.assertEquals("Red band", model.getDescription());
        Assertions.assertEquals("https://example.com/asset.tif", model.getHref());
        Assertions.assertEquals("image/tiff", model.getType());
        Assertions.assertEquals(2, model.getRoles().size());
        Assertions.assertEquals("data", model.getRoles().get(0));
        Assertions.assertEquals("visual", model.getRoles().get(1));
        Assertions.assertNotNull(model.getAdditionalProperties());
        Assertions.assertEquals("extra_value", model.getAdditionalProperties().get("extra_field"));
    }

    @Test
    public void testSerialize() throws Exception {
        StacAsset model = new StacAsset().setPlatform("sentinel-2")
            .setInstruments(Arrays.asList("msi"))
            .setConstellation("esa")
            .setMission("s2")
            .setGsd(10.0)
            .setCreated(OffsetDateTime.of(2021, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            .setUpdated(OffsetDateTime.of(2021, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            .setTitle("Band 4")
            .setDescription("Red band")
            .setHref("https://example.com/asset.tif")
            .setType("image/tiff")
            .setRoles(Arrays.asList("data", "visual"))
            .setAdditionalProperties(mapOf("extra_field", "extra_value"));
        model = BinaryData.fromObject(model).toObject(StacAsset.class);
        Assertions.assertEquals("sentinel-2", model.getPlatform());
        Assertions.assertEquals(1, model.getInstruments().size());
        Assertions.assertEquals("msi", model.getInstruments().get(0));
        Assertions.assertEquals("esa", model.getConstellation());
        Assertions.assertEquals("s2", model.getMission());
        Assertions.assertEquals(10.0, model.getGsd());
        Assertions.assertEquals(OffsetDateTime.of(2021, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC), model.getCreated());
        Assertions.assertEquals(OffsetDateTime.of(2021, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC), model.getUpdated());
        Assertions.assertEquals("Band 4", model.getTitle());
        Assertions.assertEquals("Red band", model.getDescription());
        Assertions.assertEquals("https://example.com/asset.tif", model.getHref());
        Assertions.assertEquals("image/tiff", model.getType());
        Assertions.assertEquals(2, model.getRoles().size());
    }

    @Test
    public void testDeserializeWithProviders() throws Exception {
        StacAsset model = BinaryData.fromString("{\"href\":\"https://example.com/asset.tif\","
            + "\"providers\":[{\"name\":\"ESA\",\"description\":\"European Space Agency\","
            + "\"roles\":[\"producer\"],\"url\":\"https://esa.int\"}]}").toObject(StacAsset.class);
        Assertions.assertEquals("https://example.com/asset.tif", model.getHref());
        Assertions.assertNotNull(model.getProviders());
        Assertions.assertEquals(1, model.getProviders().size());
        Assertions.assertEquals("ESA", model.getProviders().get(0).getName());
        Assertions.assertEquals("European Space Agency", model.getProviders().get(0).getDescription());
    }

    @Test
    public void testDeserializeMinimal() throws Exception {
        StacAsset model = BinaryData.fromString("{}").toObject(StacAsset.class);
        Assertions.assertNull(model.getPlatform());
        Assertions.assertNull(model.getHref());
        Assertions.assertNull(model.getTitle());
        Assertions.assertNull(model.getGsd());
        Assertions.assertNull(model.getRoles());
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
