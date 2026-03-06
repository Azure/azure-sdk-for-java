// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link StacLandingPage}.
 * StacLandingPage is immutable with a private constructor, so can only be created via deserialization.
 */
public final class StacLandingPageTests {

    @Test
    public void testDeserialize() throws Exception {
        StacLandingPage model = BinaryData
            .fromString("{\"id\":\"stac-api\",\"description\":\"STAC API Landing Page\","
                + "\"title\":\"Planetary Computer STAC API\"," + "\"stac_version\":\"1.0.0\"," + "\"type\":\"Catalog\","
                + "\"conformsTo\":[\"https://api.stacspec.org/v1.0.0/core\","
                + "\"https://api.stacspec.org/v1.0.0/item-search\"],"
                + "\"links\":[{\"rel\":\"self\",\"href\":\"https://planetarycomputer.microsoft.com/api/stac/v1\","
                + "\"type\":\"application/json\"},"
                + "{\"rel\":\"root\",\"href\":\"https://planetarycomputer.microsoft.com/api/stac/v1\"}],"
                + "\"stac_extensions\":[\"item-search\"],"
                + "\"msft:short_description\":\"Microsoft Planetary Computer STAC API\"}")
            .toObject(StacLandingPage.class);
        Assertions.assertEquals("stac-api", model.getId());
        Assertions.assertEquals("STAC API Landing Page", model.getDescription());
        Assertions.assertEquals("Planetary Computer STAC API", model.getTitle());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertEquals("Catalog", model.getType());
        Assertions.assertNotNull(model.getConformsTo());
        Assertions.assertEquals(2, model.getConformsTo().size());
        Assertions.assertEquals("https://api.stacspec.org/v1.0.0/core", model.getConformsTo().get(0));
        Assertions.assertEquals("https://api.stacspec.org/v1.0.0/item-search", model.getConformsTo().get(1));
        Assertions.assertNotNull(model.getLinks());
        Assertions.assertEquals(2, model.getLinks().size());
        Assertions.assertEquals("self", model.getLinks().get(0).getRel());
        Assertions.assertEquals(StacLinkType.APPLICATION_JSON, model.getLinks().get(0).getType());
        Assertions.assertEquals("root", model.getLinks().get(1).getRel());
        Assertions.assertNotNull(model.getStacExtensions());
        Assertions.assertEquals(1, model.getStacExtensions().size());
        Assertions.assertEquals("Microsoft Planetary Computer STAC API", model.getShortDescription());
    }

    @Test
    public void testSerializeRoundTrip() throws Exception {
        // Since StacLandingPage has a private constructor, we must create it via deserialization first,
        // then test serialization by round-tripping.
        String json
            = "{\"id\":\"test-api\",\"description\":\"Test API\"," + "\"conformsTo\":[\"core-spec\",\"search-spec\"],"
                + "\"links\":[{\"rel\":\"self\",\"href\":\"https://example.com\"}],"
                + "\"title\":\"Test Title\",\"stac_version\":\"1.0.0\","
                + "\"stac_extensions\":[\"ext1\"],\"type\":\"Catalog\"," + "\"msft:short_description\":\"Short desc\"}";
        StacLandingPage model = BinaryData.fromString(json).toObject(StacLandingPage.class);
        model = BinaryData.fromObject(model).toObject(StacLandingPage.class);
        Assertions.assertEquals("test-api", model.getId());
        Assertions.assertEquals("Test API", model.getDescription());
        Assertions.assertEquals("Test Title", model.getTitle());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertEquals("Catalog", model.getType());
        Assertions.assertEquals(2, model.getConformsTo().size());
        Assertions.assertEquals(1, model.getLinks().size());
        Assertions.assertEquals(1, model.getStacExtensions().size());
        Assertions.assertEquals("Short desc", model.getShortDescription());
    }

    @Test
    public void testDeserializeMinimal() throws Exception {
        StacLandingPage model = BinaryData
            .fromString("{\"id\":\"min-api\",\"description\":\"Minimal\"," + "\"conformsTo\":[],\"links\":[]}")
            .toObject(StacLandingPage.class);
        Assertions.assertEquals("min-api", model.getId());
        Assertions.assertEquals("Minimal", model.getDescription());
        Assertions.assertNotNull(model.getConformsTo());
        Assertions.assertEquals(0, model.getConformsTo().size());
        Assertions.assertNotNull(model.getLinks());
        Assertions.assertEquals(0, model.getLinks().size());
        Assertions.assertNull(model.getTitle());
        Assertions.assertNull(model.getStacVersion());
        Assertions.assertNull(model.getType());
    }
}
