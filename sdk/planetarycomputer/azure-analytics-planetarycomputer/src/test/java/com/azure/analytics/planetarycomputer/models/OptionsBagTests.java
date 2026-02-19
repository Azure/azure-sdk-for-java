// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for options bag models with 0% coverage:
 * {@link GetMosaicTileOptions}, {@link GetWmtsCapabilitiesOptions},
 * {@link GetMosaicWmtsCapabilitiesOptions}, {@link GetPartOptions},
 * {@link RegisterMosaicsSearchOptions}.
 */
public final class OptionsBagTests {

    @Test
    public void testGetMosaicTileOptions() {
        GetMosaicTileOptions options = new GetMosaicTileOptions().setAssets(Arrays.asList("B04", "B03", "B02"))
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
            .setBuffer("64")
            .setColorFormula("gamma RGB 3.5")
            .setCollection("sentinel-2-l2a")
            .setResampling(Resampling.NEAREST)
            .setPixelSelection(PixelSelection.FIRST)
            .setRescale(Arrays.asList("0,10000"))
            .setColorMapName(ColorMapNames.VIRIDIS)
            .setColorMap("{\"0\":[0,0,0,255]}")
            .setReturnMask(true);

        Assertions.assertEquals(3, options.getAssets().size());
        Assertions.assertEquals("B04/B03", options.getExpression());
        Assertions.assertEquals("0,1,2", options.getAssetBandIndices());
        Assertions.assertEquals(0.0, options.getNoData());
        Assertions.assertEquals(10000, options.getScanLimit());
        Assertions.assertEquals(100, options.getItemsLimit());
        Assertions.assertEquals(30, options.getTimeLimit());
        Assertions.assertEquals(TerrainAlgorithm.TERRARIUM, options.getAlgorithm());
        Assertions.assertEquals("{\"format\":\"png\"}", options.getAlgorithmParams());
        Assertions.assertEquals("64", options.getBuffer());
        Assertions.assertEquals("gamma RGB 3.5", options.getColorFormula());
        Assertions.assertEquals("sentinel-2-l2a", options.getCollection());
        Assertions.assertEquals(Resampling.NEAREST, options.getResampling());
        Assertions.assertEquals(PixelSelection.FIRST, options.getPixelSelection());
        Assertions.assertEquals(1, options.getRescale().size());
        Assertions.assertEquals(ColorMapNames.VIRIDIS, options.getColorMapName());
        Assertions.assertEquals("{\"0\":[0,0,0,255]}", options.getColorMap());
    }

    @Test
    public void testGetWmtsCapabilitiesOptions() {
        GetWmtsCapabilitiesOptions options = new GetWmtsCapabilitiesOptions().setAssets(Arrays.asList("B04"))
            .setExpression("B04")
            .setAssetBandIndices("0")
            .setAssetAsBand(false)
            .setNoData(-9999.0)
            .setUnscale(true)
            .setAlgorithm(TerrainAlgorithm.TERRARIUM)
            .setAlgorithmParams("{}")
            .setTileFormat(TilerImageFormat.PNG)
            .setTileScale(2)
            .setMinZoom(0)
            .setMaxZoom(14)
            .setBuffer("32")
            .setColorFormula("sigmoidal RGB 15 0.35")
            .setResampling(Resampling.BILINEAR)
            .setRescale(Arrays.asList("0,3000"))
            .setColorMapName(ColorMapNames.PLASMA)
            .setColorMap("{}")
            .setReturnMask(false);

        Assertions.assertEquals(1, options.getAssets().size());
        Assertions.assertEquals("B04", options.getExpression());
        Assertions.assertEquals(-9999.0, options.getNoData());
        Assertions.assertEquals(TerrainAlgorithm.TERRARIUM, options.getAlgorithm());
        Assertions.assertEquals(TilerImageFormat.PNG, options.getTileFormat());
        Assertions.assertEquals(2, options.getTileScale());
        Assertions.assertEquals(0, options.getMinZoom());
        Assertions.assertEquals(14, options.getMaxZoom());
        Assertions.assertEquals(Resampling.BILINEAR, options.getResampling());
        Assertions.assertEquals(ColorMapNames.PLASMA, options.getColorMapName());
    }

    @Test
    public void testGetMosaicWmtsCapabilitiesOptions() {
        GetMosaicWmtsCapabilitiesOptions options
            = new GetMosaicWmtsCapabilitiesOptions().setAssets(Arrays.asList("B04", "B03"))
                .setExpression("(B04-B03)/(B04+B03)")
                .setAssetBandIndices("0,1")
                .setAssetAsBand(true)
                .setNoData(0.0)
                .setUnscale(false)
                .setAlgorithm(TerrainAlgorithm.TERRARIUM)
                .setAlgorithmParams("{}")
                .setTileFormat(TilerImageFormat.JPEG)
                .setTileScale(1)
                .setMinZoom(5)
                .setMaxZoom(18)
                .setBuffer("0")
                .setColorFormula("gamma RGB 2.5")
                .setResampling(Resampling.NEAREST)
                .setRescale(Arrays.asList("-1,1"))
                .setColorMapName(ColorMapNames.INFERNO)
                .setColorMap("{}")
                .setReturnMask(true);

        Assertions.assertEquals(2, options.getAssets().size());
        Assertions.assertEquals("(B04-B03)/(B04+B03)", options.getExpression());
        Assertions.assertEquals(TerrainAlgorithm.TERRARIUM, options.getAlgorithm());
        Assertions.assertEquals(TilerImageFormat.JPEG, options.getTileFormat());
        Assertions.assertEquals(5, options.getMinZoom());
        Assertions.assertEquals(18, options.getMaxZoom());
        Assertions.assertEquals(ColorMapNames.INFERNO, options.getColorMapName());
    }

    @Test
    public void testGetPartOptions() {
        GetPartOptions options = new GetPartOptions().setAssets(Arrays.asList("B04"))
            .setExpression("B04")
            .setAssetBandIndices("0")
            .setAssetAsBand(true)
            .setNoData(-9999.0)
            .setUnscale(false)
            .setAlgorithm(TerrainAlgorithm.TERRARIUM)
            .setAlgorithmParams("{}")
            .setColorFormula("gamma RGB 3.0")
            .setCoordinateReferenceSystem("EPSG:4326")
            .setDstCrs("EPSG:3857")
            .setResampling(Resampling.CUBIC)
            .setMaxSize(1024)
            .setRescale(Arrays.asList("0,10000"))
            .setColorMapName(ColorMapNames.GREENS)
            .setColorMap("{}")
            .setReturnMask(false);

        Assertions.assertEquals(1, options.getAssets().size());
        Assertions.assertEquals("B04", options.getExpression());
        Assertions.assertEquals(-9999.0, options.getNoData());
        Assertions.assertEquals(TerrainAlgorithm.TERRARIUM, options.getAlgorithm());
        Assertions.assertEquals("gamma RGB 3.0", options.getColorFormula());
        Assertions.assertEquals("EPSG:4326", options.getCoordinateReferenceSystem());
        Assertions.assertEquals("EPSG:3857", options.getDstCrs());
        Assertions.assertEquals(Resampling.CUBIC, options.getResampling());
        Assertions.assertEquals(1024, options.getMaxSize());
        Assertions.assertEquals(ColorMapNames.GREENS, options.getColorMapName());
    }

    @Test
    public void testRegisterMosaicsSearchOptions() {
        RegisterMosaicsSearchOptions options
            = new RegisterMosaicsSearchOptions().setCollections(Arrays.asList("sentinel-2-l2a"))
                .setIds(Arrays.asList("item-1", "item-2"))
                .setBoundingBox(-180.0)
                .setDatetime("2021-01-01T00:00:00Z/2021-12-31T23:59:59Z")
                .setFilterLanguage(FilterLanguage.CQL2_JSON)
                .setQuery(mapOf("cloud_cover", mapOf("lt", 20)))
                .setFilter(mapOf("op", "<="))
                .setMetadata(new MosaicMetadata().setName("test-mosaic").setMinZoom(0).setMaxZoom(14));

        Assertions.assertEquals(1, options.getCollections().size());
        Assertions.assertEquals("sentinel-2-l2a", options.getCollections().get(0));
        Assertions.assertEquals(2, options.getIds().size());
        Assertions.assertEquals(-180.0, options.getBoundingBox());
        Assertions.assertEquals("2021-01-01T00:00:00Z/2021-12-31T23:59:59Z", options.getDatetime());
        Assertions.assertEquals(FilterLanguage.CQL2_JSON, options.getFilterLanguage());
        Assertions.assertNotNull(options.getQuery());
        Assertions.assertNotNull(options.getFilter());
        Assertions.assertNotNull(options.getMetadata());
        Assertions.assertEquals("test-mosaic", options.getMetadata().getName());
    }

    @Test
    public void testGetMosaicTileOptionsDefaults() {
        GetMosaicTileOptions options = new GetMosaicTileOptions();
        Assertions.assertNull(options.getAssets());
        Assertions.assertNull(options.getExpression());
        Assertions.assertNull(options.getNoData());
        Assertions.assertNull(options.getScanLimit());
        Assertions.assertNull(options.getAlgorithm());
        Assertions.assertNull(options.getCollection());
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
