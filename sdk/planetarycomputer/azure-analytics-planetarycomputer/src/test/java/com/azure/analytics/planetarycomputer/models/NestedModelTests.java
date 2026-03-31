// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for nested model types used across the STAC API:
 * {@link StacProvider}, {@link StacContextExtension}, {@link StacExtensionExtent},
 * {@link StacExtensionSpatialExtent}, {@link StacCollectionTemporalExtent},
 * {@link Geometry}, {@link StacSortExtension}, {@link SearchOptionsFields},
 * {@link StacItemAsset}.
 */
public final class NestedModelTests {

    @Test
    public void testStacProviderRoundTrip() throws Exception {
        StacProvider model = new StacProvider().setName("Microsoft")
            .setDescription("Microsoft Planetary Computer")
            .setRoles(Arrays.asList("host", "processor"))
            .setUrl("https://planetarycomputer.microsoft.com");
        model = BinaryData.fromObject(model).toObject(StacProvider.class);
        Assertions.assertEquals("Microsoft", model.getName());
        Assertions.assertEquals("Microsoft Planetary Computer", model.getDescription());
        Assertions.assertEquals(2, model.getRoles().size());
        Assertions.assertEquals("host", model.getRoles().get(0));
        Assertions.assertEquals("https://planetarycomputer.microsoft.com", model.getUrl());
    }

    @Test
    public void testStacContextExtensionRoundTrip() throws Exception {
        StacContextExtension model = new StacContextExtension().setReturned(25).setLimit(50).setMatched(1000);
        model = BinaryData.fromObject(model).toObject(StacContextExtension.class);
        Assertions.assertEquals(25, model.getReturned());
        Assertions.assertEquals(50, model.getLimit());
        Assertions.assertEquals(1000, model.getMatched());
    }

    @Test
    public void testStacContextExtensionDeserialize() throws Exception {
        StacContextExtension model = BinaryData.fromString("{\"returned\":10,\"limit\":100,\"matched\":500}")
            .toObject(StacContextExtension.class);
        Assertions.assertEquals(10, model.getReturned());
        Assertions.assertEquals(100, model.getLimit());
        Assertions.assertEquals(500, model.getMatched());
    }

    @Test
    public void testStacExtensionExtentRoundTrip() throws Exception {
        List<List<Double>> bbox = Arrays.asList(Arrays.asList(-180.0, -90.0, 180.0, 90.0));
        List<List<OffsetDateTime>> interval
            = Arrays.asList(Arrays.asList(OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null));
        StacExtensionExtent model = new StacExtensionExtent(new StacExtensionSpatialExtent().setBoundingBox(bbox),
            new StacCollectionTemporalExtent(interval));
        model = BinaryData.fromObject(model).toObject(StacExtensionExtent.class);
        Assertions.assertNotNull(model.getSpatial());
        Assertions.assertNotNull(model.getTemporal());
    }

    @Test
    public void testGeometryRoundTrip() throws Exception {
        Geometry model = new Geometry().setBoundingBox(Arrays.asList(-73.99, 40.71, -73.98, 40.72));
        model = BinaryData.fromObject(model).toObject(Geometry.class);
        Assertions.assertNotNull(model.getBoundingBox());
        Assertions.assertEquals(4, model.getBoundingBox().size());
    }

    @Test
    public void testGeometryDeserialize() throws Exception {
        Geometry model = BinaryData.fromString("{\"type\":\"Point\",\"bbox\":[-73.99,40.71]}").toObject(Geometry.class);
        Assertions.assertEquals(GeometryType.POINT, model.getType());
        Assertions.assertNotNull(model.getBoundingBox());
        Assertions.assertEquals(2, model.getBoundingBox().size());
    }

    @Test
    public void testSearchOptionsFieldsRoundTrip() throws Exception {
        SearchOptionsFields model
            = new SearchOptionsFields().setInclude(Arrays.asList("id", "properties.datetime", "geometry"))
                .setExclude(Arrays.asList("assets", "links"));
        model = BinaryData.fromObject(model).toObject(SearchOptionsFields.class);
        Assertions.assertEquals(3, model.getInclude().size());
        Assertions.assertEquals("id", model.getInclude().get(0));
        Assertions.assertEquals(2, model.getExclude().size());
        Assertions.assertEquals("assets", model.getExclude().get(0));
    }

    @Test
    public void testStacSortExtensionDeserialize() throws Exception {
        StacSortExtension model = BinaryData.fromString("{\"field\":\"datetime\",\"direction\":\"desc\"}")
            .toObject(StacSortExtension.class);
        Assertions.assertEquals("datetime", model.getField());
        Assertions.assertEquals(StacSearchSortingDirection.DESC, model.getDirection());
    }

    @Test
    public void testStacItemAssetDeserialize() throws Exception {
        StacItemAsset model = BinaryData
            .fromString(
                "{\"title\":\"Band 4\",\"description\":\"Red band\"," + "\"type\":\"image/tiff\",\"roles\":[\"data\"]}")
            .toObject(StacItemAsset.class);
        Assertions.assertEquals("Band 4", model.getTitle());
        Assertions.assertEquals("Red band", model.getDescription());
        Assertions.assertEquals("image/tiff", model.getType());
        Assertions.assertEquals(1, model.getRoles().size());
        Assertions.assertEquals("data", model.getRoles().get(0));
    }

    @Test
    public void testStacItemAssetRoundTrip() throws Exception {
        StacItemAsset model
            = new StacItemAsset("Band 8A", "image/tiff; application=geotiff").setDescription("Near-Infrared band")
                .setRoles(Arrays.asList("data", "reflectance"));
        model = BinaryData.fromObject(model).toObject(StacItemAsset.class);
        Assertions.assertEquals("Band 8A", model.getTitle());
        Assertions.assertEquals("Near-Infrared band", model.getDescription());
        Assertions.assertEquals("image/tiff; application=geotiff", model.getType());
        Assertions.assertEquals(2, model.getRoles().size());
    }
}
