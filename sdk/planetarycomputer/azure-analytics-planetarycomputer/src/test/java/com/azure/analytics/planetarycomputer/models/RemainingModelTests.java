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
 * Unit tests for remaining JsonSerializable models with 0% or low coverage:
 * {@link MosaicMetadata}, {@link TilerStacSearchDefinition},
 * {@link RenderOptionVectorOptions}.
 */
public final class RemainingModelTests {

    @Test
    public void testMosaicMetadataDeserialize() throws Exception {
        MosaicMetadata model = BinaryData
            .fromString("{\"type\":\"mosaic\",\"bounds\":\"[-180,-90,180,90]\",\"minzoom\":0,\"maxzoom\":14,"
                + "\"name\":\"test-mosaic\",\"assets\":[\"B04\",\"B03\"]," + "\"defaults\":{\"rescale\":\"0,10000\"}}")
            .toObject(MosaicMetadata.class);
        Assertions.assertEquals(MosaicMetadataType.MOSAIC, model.getType());
        Assertions.assertEquals("[-180,-90,180,90]", model.getBounds());
        Assertions.assertEquals(0, model.getMinZoom());
        Assertions.assertEquals(14, model.getMaxZoom());
        Assertions.assertEquals("test-mosaic", model.getName());
        Assertions.assertEquals(2, model.getAssets().size());
        Assertions.assertNotNull(model.getDefaults());
        Assertions.assertEquals("0,10000", model.getDefaults().get("rescale"));
    }

    @Test
    public void testMosaicMetadataRoundTrip() throws Exception {
        MosaicMetadata model = new MosaicMetadata().setType(MosaicMetadataType.MOSAIC)
            .setBounds("[-180,-90,180,90]")
            .setMinZoom(0)
            .setMaxZoom(14)
            .setName("test-mosaic")
            .setAssets(Arrays.asList("B04"))
            .setDefaults(mapOf("rescale", "0,10000"));
        model = BinaryData.fromObject(model).toObject(MosaicMetadata.class);
        Assertions.assertEquals(MosaicMetadataType.MOSAIC, model.getType());
        Assertions.assertEquals("[-180,-90,180,90]", model.getBounds());
        Assertions.assertEquals(0, model.getMinZoom());
        Assertions.assertEquals(14, model.getMaxZoom());
        Assertions.assertEquals("test-mosaic", model.getName());
        Assertions.assertEquals(1, model.getAssets().size());
    }

    @Test
    public void testTilerStacSearchDefinitionDeserialize() throws Exception {
        TilerStacSearchDefinition model = BinaryData
            .fromString("{\"hash\":\"abc123\"," + "\"search\":{\"collections\":[\"sentinel-2-l2a\"]},"
                + "\"_where\":\"cloud_cover < 20\"," + "\"orderby\":\"datetime DESC\","
                + "\"lastused\":\"2021-06-15T14:30:00Z\"," + "\"usecount\":42,"
                + "\"metadata\":{\"name\":\"test\",\"minzoom\":0,\"maxzoom\":14}}")
            .toObject(TilerStacSearchDefinition.class);
        Assertions.assertEquals("abc123", model.getHash());
        Assertions.assertNotNull(model.getSearch());
        Assertions.assertEquals("cloud_cover < 20", model.getWhere());
        Assertions.assertEquals("datetime DESC", model.getOrderBy());
        Assertions.assertNotNull(model.getLastUsed());
        Assertions.assertEquals(42, model.getUseCount());
        Assertions.assertNotNull(model.getMetadata());
        Assertions.assertEquals("test", model.getMetadata().getName());
    }

    @Test
    public void testTilerStacSearchDefinitionMinimal() throws Exception {
        TilerStacSearchDefinition model = BinaryData.fromString("{}").toObject(TilerStacSearchDefinition.class);
        Assertions.assertNull(model.getHash());
        Assertions.assertNull(model.getSearch());
        Assertions.assertNull(model.getWhere());
        Assertions.assertNull(model.getOrderBy());
    }

    @Test
    public void testRenderOptionVectorOptionsDeserialize() throws Exception {
        RenderOptionVectorOptions model = BinaryData
            .fromString("{\"tilejsonKey\":\"buildings\",\"sourceLayer\":\"footprints\","
                + "\"fillColor\":\"#ff0000\",\"strokeColor\":\"#000000\","
                + "\"strokeWidth\":2,\"filter\":[\"all\",\"type==residential\"]}")
            .toObject(RenderOptionVectorOptions.class);
        Assertions.assertEquals("buildings", model.getTilejsonKey());
        Assertions.assertEquals("footprints", model.getSourceLayer());
        Assertions.assertEquals("#ff0000", model.getFillColor());
        Assertions.assertEquals("#000000", model.getStrokeColor());
        Assertions.assertEquals(2, model.getStrokeWidth());
        Assertions.assertNotNull(model.getFilter());
        Assertions.assertEquals(2, model.getFilter().size());
    }

    @Test
    public void testRenderOptionVectorOptionsRoundTrip() throws Exception {
        RenderOptionVectorOptions model
            = new RenderOptionVectorOptions("buildings", "footprints").setFillColor("#ff0000")
                .setStrokeColor("#000000")
                .setStrokeWidth(2)
                .setFilter(Arrays.asList("all"));
        model = BinaryData.fromObject(model).toObject(RenderOptionVectorOptions.class);
        Assertions.assertEquals("buildings", model.getTilejsonKey());
        Assertions.assertEquals("footprints", model.getSourceLayer());
        Assertions.assertEquals("#ff0000", model.getFillColor());
        Assertions.assertEquals("#000000", model.getStrokeColor());
        Assertions.assertEquals(2, model.getStrokeWidth());
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
