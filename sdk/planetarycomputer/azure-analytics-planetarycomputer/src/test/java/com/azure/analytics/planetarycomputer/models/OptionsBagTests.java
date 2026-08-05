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
 * Unit tests for options bag models:
 * {@link RegisterMosaicsSearchOptions}.
 */
public final class OptionsBagTests {

    @Test
    public void testRegisterMosaicsSearchOptions() {
        Map<String, BinaryData> query = new HashMap<>();
        Map<String, Object> ltMap = new HashMap<>();
        ltMap.put("lt", 20);
        query.put("cloud_cover", BinaryData.fromObject(ltMap));

        Map<String, BinaryData> filter = new HashMap<>();
        filter.put("op", BinaryData.fromString("\"<=\""));

        RegisterMosaicsSearchOptions options
            = new RegisterMosaicsSearchOptions().setCollections(Arrays.asList("sentinel-2-l2a"))
                .setIds(Arrays.asList("item-1", "item-2"))
                .setBoundingBox(Arrays.asList(-180.0, -90.0, 180.0, 90.0))
                .setDatetime("2021-01-01T00:00:00Z/2021-12-31T23:59:59Z")
                .setFilterLanguage(FilterLanguage.CQL2_JSON)
                .setQuery(query)
                .setFilter(filter)
                .setMetadata(new MosaicMetadata().setName("test-mosaic").setMinZoom(0).setMaxZoom(14));

        Assertions.assertEquals(1, options.getCollections().size());
        Assertions.assertEquals("sentinel-2-l2a", options.getCollections().get(0));
        Assertions.assertEquals(2, options.getIds().size());
        Assertions.assertEquals(4, options.getBoundingBox().size());
        Assertions.assertEquals(-180.0, options.getBoundingBox().get(0));
        Assertions.assertEquals(90.0, options.getBoundingBox().get(3));
        Assertions.assertEquals("2021-01-01T00:00:00Z/2021-12-31T23:59:59Z", options.getDatetime());
        Assertions.assertEquals(FilterLanguage.CQL2_JSON, options.getFilterLanguage());
        Assertions.assertNotNull(options.getQuery());
        Assertions.assertNotNull(options.getFilter());
        Assertions.assertNotNull(options.getMetadata());
        Assertions.assertEquals("test-mosaic", options.getMetadata().getName());
    }
}
