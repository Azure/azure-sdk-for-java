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
 * Unit tests for {@link StacItemProperties}.
 */
public final class StacItemPropertiesTests {

    @Test
    public void testDeserialize() throws Exception {
        StacItemProperties model
            = BinaryData.fromString("{\"platform\":\"sentinel-2\",\"instruments\":[\"msi\",\"sar\"],"
                + "\"constellation\":\"copernicus\",\"mission\":\"sentinel\","
                + "\"gsd\":10.0,\"created\":\"2021-01-15T10:00:00Z\","
                + "\"updated\":\"2021-03-20T12:30:00Z\",\"title\":\"Scene Properties\","
                + "\"description\":\"Satellite scene properties\"," + "\"datetime\":\"2021-06-15T14:30:00Z\","
                + "\"start_datetime\":\"2021-06-15T14:00:00Z\"," + "\"end_datetime\":\"2021-06-15T15:00:00Z\","
                + "\"custom_prop\":42}").toObject(StacItemProperties.class);
        Assertions.assertEquals("sentinel-2", model.getPlatform());
        Assertions.assertEquals(2, model.getInstruments().size());
        Assertions.assertEquals("msi", model.getInstruments().get(0));
        Assertions.assertEquals("sar", model.getInstruments().get(1));
        Assertions.assertEquals("copernicus", model.getConstellation());
        Assertions.assertEquals("sentinel", model.getMission());
        Assertions.assertEquals(10.0, model.getGsd());
        Assertions.assertEquals(OffsetDateTime.of(2021, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC), model.getCreated());
        Assertions.assertEquals(OffsetDateTime.of(2021, 3, 20, 12, 30, 0, 0, ZoneOffset.UTC), model.getUpdated());
        Assertions.assertEquals("Scene Properties", model.getTitle());
        Assertions.assertEquals("Satellite scene properties", model.getDescription());
        Assertions.assertEquals("2021-06-15T14:30:00Z", model.getDatetime());
        Assertions.assertEquals(OffsetDateTime.of(2021, 6, 15, 14, 0, 0, 0, ZoneOffset.UTC), model.getStartDatetime());
        Assertions.assertEquals(OffsetDateTime.of(2021, 6, 15, 15, 0, 0, 0, ZoneOffset.UTC), model.getEndDatetime());
        Assertions.assertNotNull(model.getAdditionalProperties());
        Assertions.assertEquals(42, ((Number) model.getAdditionalProperties().get("custom_prop")).intValue());
    }

    @Test
    public void testSerialize() throws Exception {
        StacItemProperties model = new StacItemProperties().setPlatform("sentinel-2")
            .setInstruments(Arrays.asList("msi", "sar"))
            .setConstellation("copernicus")
            .setMission("sentinel")
            .setGsd(10.0)
            .setCreated(OffsetDateTime.of(2021, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC))
            .setUpdated(OffsetDateTime.of(2021, 3, 20, 12, 30, 0, 0, ZoneOffset.UTC))
            .setTitle("Scene Properties")
            .setDescription("Satellite scene properties")
            .setDatetime("2021-06-15T14:30:00Z")
            .setStartDatetime(OffsetDateTime.of(2021, 6, 15, 14, 0, 0, 0, ZoneOffset.UTC))
            .setEndDatetime(OffsetDateTime.of(2021, 6, 15, 15, 0, 0, 0, ZoneOffset.UTC))
            .setAdditionalProperties(mapOf("custom_prop", 42));
        model = BinaryData.fromObject(model).toObject(StacItemProperties.class);
        Assertions.assertEquals("sentinel-2", model.getPlatform());
        Assertions.assertEquals(2, model.getInstruments().size());
        Assertions.assertEquals("copernicus", model.getConstellation());
        Assertions.assertEquals("sentinel", model.getMission());
        Assertions.assertEquals(10.0, model.getGsd());
        Assertions.assertEquals("Scene Properties", model.getTitle());
        Assertions.assertEquals("Satellite scene properties", model.getDescription());
        Assertions.assertEquals("2021-06-15T14:30:00Z", model.getDatetime());
        Assertions.assertNotNull(model.getStartDatetime());
        Assertions.assertNotNull(model.getEndDatetime());
    }

    @Test
    public void testDeserializeWithProviders() throws Exception {
        StacItemProperties model = BinaryData.fromString("{\"datetime\":\"2021-01-01T00:00:00Z\","
            + "\"providers\":[{\"name\":\"NASA\",\"url\":\"https://nasa.gov\","
            + "\"roles\":[\"host\",\"producer\"]}]}").toObject(StacItemProperties.class);
        Assertions.assertEquals("2021-01-01T00:00:00Z", model.getDatetime());
        Assertions.assertNotNull(model.getProviders());
        Assertions.assertEquals(1, model.getProviders().size());
        Assertions.assertEquals("NASA", model.getProviders().get(0).getName());
        Assertions.assertEquals("https://nasa.gov", model.getProviders().get(0).getUrl());
    }

    @Test
    public void testDeserializeMinimal() throws Exception {
        StacItemProperties model = BinaryData.fromString("{}").toObject(StacItemProperties.class);
        Assertions.assertNull(model.getPlatform());
        Assertions.assertNull(model.getDatetime());
        Assertions.assertNull(model.getTitle());
        Assertions.assertNull(model.getGsd());
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
