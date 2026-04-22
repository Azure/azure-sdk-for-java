// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link StacItemOrStacItemCollection} discriminated type dispatch.
 */
public final class StacItemOrStacItemCollectionTests {

    @Test
    public void testDeserializeAsStacItem() throws Exception {
        StacItemOrStacItemCollection model = BinaryData
            .fromString("{\"type\":\"Feature\",\"stac_version\":\"1.0.0\","
                + "\"id\":\"item-123\",\"collection\":\"sentinel-2-l2a\","
                + "\"properties\":{\"datetime\":\"2021-06-15T14:30:00Z\"},"
                + "\"links\":[{\"rel\":\"self\",\"href\":\"https://example.com/item\"}],"
                + "\"stac_extensions\":[\"eo\"]," + "\"msft:short_description\":\"Test feature\"}")
            .toObject(StacItemOrStacItemCollection.class);
        Assertions.assertEquals(StacModelType.FEATURE, model.getType());
        Assertions.assertTrue(model instanceof StacItem);
        StacItem item = (StacItem) model;
        Assertions.assertEquals("item-123", item.getId());
        Assertions.assertEquals("sentinel-2-l2a", item.getCollection());
        Assertions.assertNotNull(item.getProperties());
        Assertions.assertEquals("2021-06-15T14:30:00Z", item.getProperties().getDatetime());
        Assertions.assertEquals("1.0.0", item.getStacVersion());
        Assertions.assertEquals(1, item.getLinks().size());
        Assertions.assertEquals(1, item.getStacExtensions().size());
        Assertions.assertEquals("Test feature", item.getShortDescription());
    }

    @Test
    public void testDeserializeAsStacItemCollection() throws Exception {
        StacItemOrStacItemCollection model = BinaryData
            .fromString("{\"type\":\"FeatureCollection\",\"stac_version\":\"1.0.0\","
                + "\"features\":[{\"type\":\"Feature\",\"properties\":{\"datetime\":\"2021-01-01T00:00:00Z\"}}],"
                + "\"links\":[{\"rel\":\"root\",\"href\":\"https://example.com\"}],"
                + "\"stac_extensions\":[\"context\"]," + "\"msft:short_description\":\"Test collection\"}")
            .toObject(StacItemOrStacItemCollection.class);
        Assertions.assertEquals(StacModelType.FEATURE_COLLECTION, model.getType());
        Assertions.assertTrue(model instanceof StacItemCollection);
        StacItemCollection collection = (StacItemCollection) model;
        Assertions.assertNotNull(collection.getFeatures());
        Assertions.assertEquals(1, collection.getFeatures().size());
        Assertions.assertEquals("1.0.0", collection.getStacVersion());
        Assertions.assertEquals(1, collection.getLinks().size());
        Assertions.assertEquals(1, collection.getStacExtensions().size());
        Assertions.assertEquals("Test collection", collection.getShortDescription());
    }

    @Test
    public void testSerializeStacItem() throws Exception {
        StacItemOrStacItemCollection model = new StacItem().setCollection("test-col")
            .setProperties(new StacItemProperties().setDatetime("2021-01-01T00:00:00Z"))
            .setStacVersion("1.0.0")
            .setLinks(Arrays.asList(new StacLink().setRel("self").setHref("https://example.com")))
            .setShortDescription("Test item");
        StacItemOrStacItemCollection result = BinaryData.fromObject(model).toObject(StacItemOrStacItemCollection.class);
        Assertions.assertEquals(StacModelType.FEATURE, result.getType());
        Assertions.assertTrue(result instanceof StacItem);
        Assertions.assertEquals("Test item", result.getShortDescription());
    }

    @Test
    public void testSerializeStacItemCollection() throws Exception {
        StacItemOrStacItemCollection model = new StacItemCollection()
            .setFeatures(Arrays
                .asList(new StacItem().setProperties(new StacItemProperties().setDatetime("2021-01-01T00:00:00Z"))))
            .setStacVersion("1.0.0")
            .setLinks(Arrays.asList(new StacLink().setRel("root").setHref("https://example.com")))
            .setShortDescription("Test collection");
        StacItemOrStacItemCollection result = BinaryData.fromObject(model).toObject(StacItemOrStacItemCollection.class);
        Assertions.assertEquals(StacModelType.FEATURE_COLLECTION, result.getType());
        Assertions.assertTrue(result instanceof StacItemCollection);
        Assertions.assertEquals("Test collection", result.getShortDescription());
    }
}
