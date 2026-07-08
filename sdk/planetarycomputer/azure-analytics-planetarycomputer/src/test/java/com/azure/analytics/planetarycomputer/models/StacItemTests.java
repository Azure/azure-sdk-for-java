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
 * Unit tests for {@link StacItem}.
 */
public final class StacItemTests {

    @Test
    public void testDeserialize() throws Exception {
        StacItem model = BinaryData.fromString("{\"type\":\"Feature\",\"stac_version\":\"1.0.0\","
            + "\"id\":\"test-item-1\",\"collection\":\"sentinel-2-l2a\"," + "\"bbox\":[-180.0,-90.0,180.0,90.0],"
            + "\"geometry\":{\"type\":\"Point\",\"bbox\":[-73.99,40.71,-73.99,40.71]},"
            + "\"properties\":{\"datetime\":\"2021-06-15T14:30:00Z\"," + "\"platform\":\"sentinel-2\"},"
            + "\"assets\":{\"B04\":{\"href\":\"https://example.com/B04.tif\",\"type\":\"image/tiff\"}},"
            + "\"links\":[{\"rel\":\"self\",\"href\":\"https://example.com/item\"}],"
            + "\"stac_extensions\":[\"eo\",\"view\"]," + "\"msft:short_description\":\"A test item\","
            + "\"_msft:etag\":\"abc123\"}").toObject(StacItem.class);
        Assertions.assertEquals(StacModelType.FEATURE, model.getType());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertEquals("test-item-1", model.getId());
        Assertions.assertEquals("sentinel-2-l2a", model.getCollection());
        Assertions.assertEquals(4, model.getBoundingBox().size());
        Assertions.assertEquals(-180.0, model.getBoundingBox().get(0));
        Assertions.assertNotNull(model.getGeometry());
        Assertions.assertNotNull(model.getProperties());
        Assertions.assertEquals("2021-06-15T14:30:00Z", model.getProperties().getDatetime());
        Assertions.assertEquals("sentinel-2", model.getProperties().getPlatform());
        Assertions.assertNotNull(model.getAssets());
        Assertions.assertEquals(1, model.getAssets().size());
        Assertions.assertNotNull(model.getAssets().get("B04"));
        Assertions.assertEquals("https://example.com/B04.tif", model.getAssets().get("B04").getHref());
        Assertions.assertEquals(1, model.getLinks().size());
        Assertions.assertEquals("self", model.getLinks().get(0).getRel());
        Assertions.assertEquals(2, model.getStacExtensions().size());
        Assertions.assertEquals("A test item", model.getShortDescription());
        Assertions.assertEquals("abc123", model.getETag());
    }

    @Test
    public void testSerialize() throws Exception {
        StacItem model = new StacItem().setCollection("sentinel-2-l2a")
            .setBoundingBox(Arrays.asList(-180.0, -90.0, 180.0, 90.0))
            .setGeometry(new Geometry().setBoundingBox(Arrays.asList(-73.99, 40.71)))
            .setProperties(new StacItemProperties().setDatetime("2021-06-15T14:30:00Z").setPlatform("sentinel-2"))
            .setAssets(mapOf("B04", new StacAsset().setHref("https://example.com/B04.tif").setType("image/tiff")))
            .setStacVersion("1.0.0")
            .setLinks(Arrays.asList(new StacLink().setRel("self").setHref("https://example.com/item")))
            .setStacExtensions(Arrays.asList("eo", "view"))
            .setShortDescription("A test item")
            .setETag("abc123")
            .setTimestamp(OffsetDateTime.of(2021, 6, 15, 14, 30, 0, 0, ZoneOffset.UTC))
            .setCreatedOn(OffsetDateTime.of(2021, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            .setUpdatedOn(OffsetDateTime.of(2021, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        model = BinaryData.fromObject(model).toObject(StacItem.class);
        Assertions.assertEquals(StacModelType.FEATURE, model.getType());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertEquals("sentinel-2-l2a", model.getCollection());
        Assertions.assertEquals(4, model.getBoundingBox().size());
        Assertions.assertNotNull(model.getProperties());
        Assertions.assertEquals("sentinel-2", model.getProperties().getPlatform());
        Assertions.assertNotNull(model.getAssets());
        Assertions.assertEquals(1, model.getAssets().size());
        Assertions.assertEquals(1, model.getLinks().size());
        Assertions.assertEquals(2, model.getStacExtensions().size());
        Assertions.assertEquals("A test item", model.getShortDescription());
        Assertions.assertEquals("abc123", model.getETag());
    }

    @Test
    public void testDeserializeMinimal() throws Exception {
        StacItem model = BinaryData.fromString("{\"type\":\"Feature\"}").toObject(StacItem.class);
        Assertions.assertEquals(StacModelType.FEATURE, model.getType());
        Assertions.assertNull(model.getId());
        Assertions.assertNull(model.getCollection());
        Assertions.assertNull(model.getBoundingBox());
        Assertions.assertNull(model.getProperties());
        Assertions.assertNull(model.getAssets());
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
