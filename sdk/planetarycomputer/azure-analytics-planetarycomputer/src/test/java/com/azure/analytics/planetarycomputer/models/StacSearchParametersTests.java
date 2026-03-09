// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link StacSearchParameters}.
 */
public final class StacSearchParametersTests {

    @Test
    public void testDeserialize() throws Exception {
        StacSearchParameters model = BinaryData
            .fromString("{\"collections\":[\"sentinel-2-l2a\",\"landsat-c2-l2\"]," + "\"ids\":[\"item-1\",\"item-2\"],"
                + "\"bbox\":[-180.0,-90.0,180.0,90.0]," + "\"datetime\":\"2021-01-01T00:00:00Z/2021-12-31T23:59:59Z\","
                + "\"limit\":50," + "\"conf\":{\"core-item\":{}}," + "\"query\":{\"eo:cloud_cover\":{\"lt\":20}},"
                + "\"filter\":{\"op\":\"<=\",\"args\":[{\"property\":\"cloud_cover\"},20]},"
                + "\"filter-crs\":\"http://www.opengis.net/def/crs/OGC/1.3/CRS84\"," + "\"filter-lang\":\"cql2-json\","
                + "\"token\":\"next:abc123\"}")
            .toObject(StacSearchParameters.class);
        Assertions.assertEquals(2, model.getCollections().size());
        Assertions.assertEquals("sentinel-2-l2a", model.getCollections().get(0));
        Assertions.assertEquals("landsat-c2-l2", model.getCollections().get(1));
        Assertions.assertEquals(2, model.getIds().size());
        Assertions.assertEquals("item-1", model.getIds().get(0));
        Assertions.assertEquals(4, model.getBoundingBox().size());
        Assertions.assertEquals(-180.0, model.getBoundingBox().get(0));
        Assertions.assertEquals("2021-01-01T00:00:00Z/2021-12-31T23:59:59Z", model.getDatetime());
        Assertions.assertEquals(50, model.getLimit());
        Assertions.assertNotNull(model.getConformanceClass());
        Assertions.assertNotNull(model.getQuery());
        Assertions.assertNotNull(model.getFilter());
        Assertions.assertEquals("http://www.opengis.net/def/crs/OGC/1.3/CRS84",
            model.getFilterCoordinateReferenceSystem());
        Assertions.assertEquals(FilterLanguage.CQL2_JSON, model.getFilterLang());
        Assertions.assertEquals("next:abc123", model.getToken());
    }

    @Test
    public void testSerialize() throws Exception {
        StacSearchParameters model
            = new StacSearchParameters().setCollections(Arrays.asList("sentinel-2-l2a", "landsat-c2-l2"))
                .setIds(Arrays.asList("item-1", "item-2"))
                .setBoundingBox(Arrays.asList(-180.0, -90.0, 180.0, 90.0))
                .setDatetime("2021-01-01T00:00:00Z/2021-12-31T23:59:59Z")
                .setLimit(50)
                .setConformanceClass(mapOf("core-item", new HashMap<>()))
                .setQuery(mapOf("eo:cloud_cover", mapOf("lt", 20)))
                .setFilter(mapOf("op", "<="))
                .setFilterCoordinateReferenceSystem("http://www.opengis.net/def/crs/OGC/1.3/CRS84")
                .setFilterLang(FilterLanguage.CQL2_JSON)
                .setToken("next:abc123");
        model = BinaryData.fromObject(model).toObject(StacSearchParameters.class);
        Assertions.assertEquals(2, model.getCollections().size());
        Assertions.assertEquals("sentinel-2-l2a", model.getCollections().get(0));
        Assertions.assertEquals(2, model.getIds().size());
        Assertions.assertEquals(4, model.getBoundingBox().size());
        Assertions.assertEquals("2021-01-01T00:00:00Z/2021-12-31T23:59:59Z", model.getDatetime());
        Assertions.assertEquals(50, model.getLimit());
        Assertions.assertNotNull(model.getConformanceClass());
        Assertions.assertNotNull(model.getQuery());
        Assertions.assertNotNull(model.getFilter());
        Assertions.assertEquals("http://www.opengis.net/def/crs/OGC/1.3/CRS84",
            model.getFilterCoordinateReferenceSystem());
        Assertions.assertEquals(FilterLanguage.CQL2_JSON, model.getFilterLang());
        Assertions.assertEquals("next:abc123", model.getToken());
    }

    @Test
    public void testDeserializeWithGeometry() throws Exception {
        StacSearchParameters model = BinaryData
            .fromString("{\"intersects\":{\"type\":\"Point\",\"bbox\":[-73.99,40.71]},"
                + "\"sortby\":[{\"field\":\"datetime\",\"direction\":\"desc\"}],"
                + "\"fields\":[{\"include\":[\"id\",\"properties.datetime\"]," + "\"exclude\":[\"assets\"]}]}")
            .toObject(StacSearchParameters.class);
        Assertions.assertNotNull(model.getIntersects());
        Assertions.assertEquals(GeometryType.POINT, model.getIntersects().getType());
        Assertions.assertNotNull(model.getSortBy());
        Assertions.assertEquals(1, model.getSortBy().size());
        Assertions.assertEquals("datetime", model.getSortBy().get(0).getField());
        Assertions.assertEquals(StacSearchSortingDirection.DESC, model.getSortBy().get(0).getDirection());
        Assertions.assertNotNull(model.getFields());
        Assertions.assertEquals(1, model.getFields().size());
        Assertions.assertEquals(2, model.getFields().get(0).getInclude().size());
        Assertions.assertEquals(1, model.getFields().get(0).getExclude().size());
    }

    @Test
    public void testDeserializeMinimal() throws Exception {
        StacSearchParameters model = BinaryData.fromString("{}").toObject(StacSearchParameters.class);
        Assertions.assertNull(model.getCollections());
        Assertions.assertNull(model.getIds());
        Assertions.assertNull(model.getBoundingBox());
        Assertions.assertNull(model.getDatetime());
        Assertions.assertNull(model.getLimit());
        Assertions.assertNull(model.getToken());
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
