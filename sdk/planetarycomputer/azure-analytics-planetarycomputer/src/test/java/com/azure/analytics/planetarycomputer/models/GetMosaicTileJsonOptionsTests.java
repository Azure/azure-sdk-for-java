// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Unit tests for {@link GetMosaicTileJsonOptions}.
 * This model is a pure options bag (query params) — no JSON serialization.
 * Tests verify getter/setter round-trips.
 */
public final class GetMosaicTileJsonOptionsTests {

    @Test
    public void testSettersAndGetters() {
        GetMosaicTileJsonOptions options = new GetMosaicTileJsonOptions().setAssets(Arrays.asList("B04", "B03", "B02"))
            .setExpression("B04/B03")
            .setAssetBandIndices("0,1,2")
            .setAssetAsBand(true)
            .setNoData(0.0)
            .setUnscale(false)
            .setScanLimit(10000)
            .setItemsLimit(100)
            .setTimeLimit(30)
            .setExitWhenFull(true)
            .setSkipCovered(false)
            .setAlgorithm(TerrainAlgorithm.TERRARIUM)
            .setAlgorithmParams("{\"format\":\"png\"}")
            .setMinZoom(0)
            .setMaxZoom(18)
            .setTileScale(1)
            .setBuffer("64")
            .setColorFormula("gamma RGB 3.5 saturation 1.7 sigmoidal RGB 15 0.35")
            .setCollection("sentinel-2-l2a")
            .setResampling(Resampling.NEAREST)
            .setPixelSelection(PixelSelection.FIRST)
            .setRescale(Arrays.asList("0,10000"))
            .setColorMapName(ColorMapNames.VIRIDIS)
            .setColorMap("{\"0\":[0,0,0,255]}")
            .setReturnMask(true);

        Assertions.assertEquals(3, options.getAssets().size());
        Assertions.assertEquals("B04", options.getAssets().get(0));
        Assertions.assertEquals("B04/B03", options.getExpression());
        Assertions.assertEquals("0,1,2", options.getAssetBandIndices());
        Assertions.assertEquals(0.0, options.getNoData());
        Assertions.assertEquals(10000, options.getScanLimit());
        Assertions.assertEquals(100, options.getItemsLimit());
        Assertions.assertEquals(30, options.getTimeLimit());
        Assertions.assertEquals(TerrainAlgorithm.TERRARIUM, options.getAlgorithm());
        Assertions.assertEquals("{\"format\":\"png\"}", options.getAlgorithmParams());
        Assertions.assertEquals(0, options.getMinZoom());
        Assertions.assertEquals(18, options.getMaxZoom());
        Assertions.assertEquals(1, options.getTileScale());
        Assertions.assertEquals("64", options.getBuffer());
        Assertions.assertNotNull(options.getColorFormula());
        Assertions.assertEquals("sentinel-2-l2a", options.getCollection());
        Assertions.assertEquals(Resampling.NEAREST, options.getResampling());
        Assertions.assertEquals(PixelSelection.FIRST, options.getPixelSelection());
        Assertions.assertEquals(1, options.getRescale().size());
        Assertions.assertEquals(ColorMapNames.VIRIDIS, options.getColorMapName());
        Assertions.assertEquals("{\"0\":[0,0,0,255]}", options.getColorMap());
    }

    @Test
    public void testDefaultValues() {
        GetMosaicTileJsonOptions options = new GetMosaicTileJsonOptions();
        Assertions.assertNull(options.getAssets());
        Assertions.assertNull(options.getExpression());
        Assertions.assertNull(options.getNoData());
        Assertions.assertNull(options.getScanLimit());
        Assertions.assertNull(options.getItemsLimit());
        Assertions.assertNull(options.getTimeLimit());
        Assertions.assertNull(options.getAlgorithm());
        Assertions.assertNull(options.getMinZoom());
        Assertions.assertNull(options.getMaxZoom());
        Assertions.assertNull(options.getTileScale());
        Assertions.assertNull(options.getCollection());
        Assertions.assertNull(options.getResampling());
        Assertions.assertNull(options.getPixelSelection());
        Assertions.assertNull(options.getRescale());
        Assertions.assertNull(options.getColorMapName());
        Assertions.assertNull(options.getColorMap());
    }
}
