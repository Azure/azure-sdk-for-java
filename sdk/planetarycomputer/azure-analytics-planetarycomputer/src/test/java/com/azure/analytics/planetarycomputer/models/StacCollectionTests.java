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
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link StacCollection}.
 */
public final class StacCollectionTests {

    @Test
    public void testDeserialize() throws Exception {
        StacCollection model
            = BinaryData.fromString("{\"id\":\"sentinel-2-l2a\",\"description\":\"Sentinel 2 Level-2A\","
                + "\"stac_version\":\"1.0.0\"," + "\"title\":\"Sentinel-2 Level 2A\"," + "\"type\":\"Collection\","
                + "\"license\":\"proprietary\"," + "\"extent\":{\"spatial\":{\"bbox\":[[-180,-90,180,90]]},"
                + "\"temporal\":{\"interval\":[[\"2015-06-27T10:25:31Z\",null]]}},"
                + "\"links\":[{\"rel\":\"self\",\"href\":\"https://example.com/collection\"}],"
                + "\"keywords\":[\"sentinel\",\"satellite\",\"imagery\"],"
                + "\"providers\":[{\"name\":\"ESA\",\"roles\":[\"producer\"]}],"
                + "\"summaries\":{\"platform\":[\"Sentinel-2A\",\"Sentinel-2B\"]},"
                + "\"stac_extensions\":[\"eo\",\"sat\"]," + "\"msft:short_description\":\"S2 L2A data\","
                + "\"custom_key\":\"custom_value\"}").toObject(StacCollection.class);
        Assertions.assertEquals("sentinel-2-l2a", model.getId());
        Assertions.assertEquals("Sentinel 2 Level-2A", model.getDescription());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertEquals("Sentinel-2 Level 2A", model.getTitle());
        Assertions.assertEquals("Collection", model.getType());
        Assertions.assertEquals("proprietary", model.getLicense());
        Assertions.assertNotNull(model.getExtent());
        Assertions.assertNotNull(model.getExtent().getSpatial());
        Assertions.assertNotNull(model.getExtent().getTemporal());
        Assertions.assertEquals(1, model.getLinks().size());
        Assertions.assertEquals("self", model.getLinks().get(0).getRel());
        Assertions.assertEquals(3, model.getKeywords().size());
        Assertions.assertEquals("sentinel", model.getKeywords().get(0));
        Assertions.assertNotNull(model.getProviders());
        Assertions.assertEquals(1, model.getProviders().size());
        Assertions.assertEquals("ESA", model.getProviders().get(0).getName());
        Assertions.assertNotNull(model.getSummaries());
        Assertions.assertNotNull(model.getStacExtensions());
        Assertions.assertEquals(2, model.getStacExtensions().size());
        Assertions.assertEquals("S2 L2A data", model.getShortDescription());
        Assertions.assertNotNull(model.getAdditionalProperties());
        Assertions.assertEquals("custom_value", model.getAdditionalProperties().get("custom_key"));
    }

    @Test
    public void testSerialize() throws Exception {
        List<List<Double>> bboxes = Arrays.asList(Arrays.asList(-180.0, -90.0, 180.0, 90.0));
        List<List<OffsetDateTime>> intervals
            = Arrays.asList(Arrays.asList(OffsetDateTime.of(2015, 6, 27, 10, 25, 31, 0, ZoneOffset.UTC), null));
        StacExtensionExtent extent = new StacExtensionExtent(new StacExtensionSpatialExtent().setBoundingBox(bboxes),
            new StacCollectionTemporalExtent(intervals));
        StacCollection model = new StacCollection("Sentinel 2 Level-2A",
            Arrays.asList(new StacLink().setRel("self").setHref("https://example.com/collection")), "proprietary",
            extent).setStacVersion("1.0.0")
                .setTitle("Sentinel-2 Level 2A")
                .setType("Collection")
                .setKeywords(Arrays.asList("sentinel", "satellite", "imagery"))
                .setProviders(Arrays.asList(new StacProvider().setName("ESA").setRoles(Arrays.asList("producer"))))
                .setSummaries(mapOf("platform", Arrays.asList("Sentinel-2A", "Sentinel-2B")))
                .setStacExtensions(Arrays.asList("eo", "sat"))
                .setShortDescription("S2 L2A data")
                .setAdditionalProperties(mapOf("custom_key", "custom_value"));
        model = BinaryData.fromObject(model).toObject(StacCollection.class);
        Assertions.assertEquals("Sentinel 2 Level-2A", model.getDescription());
        Assertions.assertEquals("1.0.0", model.getStacVersion());
        Assertions.assertEquals("Sentinel-2 Level 2A", model.getTitle());
        Assertions.assertEquals("Collection", model.getType());
        Assertions.assertEquals("proprietary", model.getLicense());
        Assertions.assertNotNull(model.getExtent());
        Assertions.assertEquals(1, model.getLinks().size());
        Assertions.assertEquals(3, model.getKeywords().size());
        Assertions.assertEquals(1, model.getProviders().size());
        Assertions.assertEquals(2, model.getStacExtensions().size());
        Assertions.assertEquals("S2 L2A data", model.getShortDescription());
    }

    @Test
    public void testDeserializeWithAssets() throws Exception {
        StacCollection model = BinaryData
            .fromString("{\"description\":\"Test\",\"license\":\"MIT\","
                + "\"extent\":{\"spatial\":{},\"temporal\":{\"interval\":[]}}," + "\"links\":[],"
                + "\"assets\":{\"thumbnail\":{\"href\":\"https://example.com/thumb.png\","
                + "\"type\":\"image/png\",\"title\":\"Thumbnail\"}},"
                + "\"item_assets\":{\"B04\":{\"title\":\"Band 4\"," + "\"description\":\"Red band\"}}}")
            .toObject(StacCollection.class);
        Assertions.assertEquals("Test", model.getDescription());
        Assertions.assertEquals("MIT", model.getLicense());
        Assertions.assertNotNull(model.getAssets());
        Assertions.assertEquals(1, model.getAssets().size());
        Assertions.assertNotNull(model.getAssets().get("thumbnail"));
        Assertions.assertEquals("https://example.com/thumb.png", model.getAssets().get("thumbnail").getHref());
        Assertions.assertNotNull(model.getItemAssets());
        Assertions.assertEquals(1, model.getItemAssets().size());
        Assertions.assertNotNull(model.getItemAssets().get("B04"));
        Assertions.assertEquals("Band 4", model.getItemAssets().get("B04").getTitle());
    }

    @Test
    public void testDeserializeWithTimestamps() throws Exception {
        StacCollection model = BinaryData
            .fromString("{\"description\":\"Test\",\"license\":\"MIT\","
                + "\"extent\":{\"spatial\":{},\"temporal\":{\"interval\":[]}}," + "\"links\":[],"
                + "\"msft:_created\":\"2021-01-01T00:00:00Z\"," + "\"msft:_updated\":\"2021-06-01T12:00:00Z\"}")
            .toObject(StacCollection.class);
        Assertions.assertEquals(OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), model.getCreatedOn());
        Assertions.assertEquals(OffsetDateTime.of(2021, 6, 1, 12, 0, 0, 0, ZoneOffset.UTC), model.getUpdatedOn());
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
