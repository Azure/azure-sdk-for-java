// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link StacItemCollection}.
 */
public final class StacItemCollectionTests {

    @Test
    public void testDeserialize() throws Exception {
        StacItemCollection model = BinaryData
            .fromString("{\"type\":\"FeatureCollection\",\"stac_version\":\"1.0.0\","
                + "\"features\":[{\"type\":\"Feature\",\"id\":\"item-1\"," + "\"collection\":\"test-collection\","
                + "\"properties\":{\"datetime\":\"2021-01-01T00:00:00Z\"}}]," + "\"bbox\":[-180.0,-90.0,180.0,90.0],"
                + "\"context\":{\"returned\":1,\"limit\":10,\"matched\":100},"
                + "\"links\":[{\"rel\":\"next\",\"href\":\"https://example.com/next\"}],"
                + "\"stac_extensions\":[\"context\"]," + "\"msft:short_description\":\"A test collection\"}")
            .toObject(StacItemCollection.class);
        Assertions.assertEquals(StacModelType.FEATURE_COLLECTION, model.getType());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertNotNull(model.getFeatures());
        Assertions.assertEquals(1, model.getFeatures().size());
        Assertions.assertEquals("item-1", model.getFeatures().get(0).getId());
        Assertions.assertEquals("test-collection", model.getFeatures().get(0).getCollection());
        Assertions.assertEquals(4, model.getBoundingBox().size());
        Assertions.assertNotNull(model.getContext());
        Assertions.assertEquals(1, model.getContext().getReturned());
        Assertions.assertEquals(10, model.getContext().getLimit());
        Assertions.assertEquals(100, model.getContext().getMatched());
        Assertions.assertEquals(1, model.getLinks().size());
        Assertions.assertEquals("next", model.getLinks().get(0).getRel());
        Assertions.assertEquals(1, model.getStacExtensions().size());
        Assertions.assertEquals("A test collection", model.getShortDescription());
    }

    @Test
    public void testSerialize() throws Exception {
        StacItemCollection model = new StacItemCollection()
            .setFeatures(Arrays.asList(new StacItem().setCollection("test-collection")
                .setProperties(new StacItemProperties().setDatetime("2021-01-01T00:00:00Z"))))
            .setBoundingBox(Arrays.asList(-180.0, -90.0, 180.0, 90.0))
            .setContext(new StacContextExtension().setReturned(1).setLimit(10).setMatched(100))
            .setStacVersion("1.0.0")
            .setLinks(Arrays.asList(new StacLink().setRel("next").setHref("https://example.com/next")))
            .setStacExtensions(Arrays.asList("context"))
            .setShortDescription("A test collection");
        model = BinaryData.fromObject(model).toObject(StacItemCollection.class);
        Assertions.assertEquals(StacModelType.FEATURE_COLLECTION, model.getType());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertNotNull(model.getFeatures());
        Assertions.assertEquals(1, model.getFeatures().size());
        Assertions.assertEquals(4, model.getBoundingBox().size());
        Assertions.assertNotNull(model.getContext());
        Assertions.assertEquals(1, model.getContext().getReturned());
        Assertions.assertEquals(10, model.getContext().getLimit());
        Assertions.assertEquals(100, model.getContext().getMatched());
        Assertions.assertEquals(1, model.getLinks().size());
        Assertions.assertEquals(1, model.getStacExtensions().size());
    }

    @Test
    public void testDeserializeMinimal() throws Exception {
        StacItemCollection model
            = BinaryData.fromString("{\"type\":\"FeatureCollection\"}").toObject(StacItemCollection.class);
        Assertions.assertEquals(StacModelType.FEATURE_COLLECTION, model.getType());
        Assertions.assertNull(model.getFeatures());
        Assertions.assertNull(model.getBoundingBox());
        Assertions.assertNull(model.getContext());
    }
}
