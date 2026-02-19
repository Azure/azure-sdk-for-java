// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link ImageParameters} model to increase branch coverage.
 */
public final class ImageParametersTests {

    @Test
    public void testImageParametersDeserializeAllFields() throws Exception {
        ImageParameters model
            = BinaryData
                .fromString("{\"cql\":{\"filter\":{\"op\":\"t_intersects\"}},"
                    + "\"render_params\":\"assets=B04&rescale=0,10000\"," + "\"cols\":256,\"rows\":256,"
                    + "\"zoom\":12.5," + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-73.99,40.71]},"
                    + "\"showBranding\":true," + "\"imageSize\":\"1024x1024\"," + "\"unknownField\":\"skip\"}")
                .toObject(ImageParameters.class);
        Assertions.assertNotNull(model.getCql());
        Assertions.assertEquals("assets=B04&rescale=0,10000", model.getRenderParameters());
        Assertions.assertEquals(256, model.getColumns());
        Assertions.assertEquals(256, model.getRows());
        Assertions.assertEquals(12.5, model.getZoom());
        Assertions.assertNotNull(model.getGeometry());
        Assertions.assertEquals(true, model.isShowBranding());
        Assertions.assertEquals("1024x1024", model.getImageSize());
    }

    @Test
    public void testImageParametersDeserializeNullableFieldsNull() throws Exception {
        // Tests nullable branches with explicit null
        ImageParameters model
            = BinaryData
                .fromString("{\"cql\":{\"op\":\"=\"}," + "\"render_params\":\"assets=B04\","
                    + "\"cols\":128,\"rows\":128," + "\"zoom\":null," + "\"showBranding\":null}")
                .toObject(ImageParameters.class);
        Assertions.assertNotNull(model.getCql());
        Assertions.assertEquals("assets=B04", model.getRenderParameters());
        Assertions.assertEquals(128, model.getColumns());
        Assertions.assertEquals(128, model.getRows());
        Assertions.assertNull(model.getZoom());
        Assertions.assertNull(model.isShowBranding());
        Assertions.assertNull(model.getImageSize());
    }

    @Test
    public void testImageParametersRoundTrip() throws Exception {
        Map<String, Object> cql = new HashMap<>();
        cql.put("op", "=");
        ImageParameters model = new ImageParameters(cql, "assets=B04", 256, 256).setZoom(10.0)
            .setShowBranding(false)
            .setImageSize("512x512");
        model = BinaryData.fromObject(model).toObject(ImageParameters.class);
        Assertions.assertEquals("assets=B04", model.getRenderParameters());
        Assertions.assertEquals(256, model.getColumns());
        Assertions.assertEquals(256, model.getRows());
        Assertions.assertEquals(10.0, model.getZoom());
        Assertions.assertEquals(false, model.isShowBranding());
        Assertions.assertEquals("512x512", model.getImageSize());
    }

    @Test
    public void testImageParametersMinimal() throws Exception {
        Map<String, Object> cql = new HashMap<>();
        cql.put("filter", "test");
        ImageParameters model = new ImageParameters(cql, "assets=B04", 64, 64);
        model = BinaryData.fromObject(model).toObject(ImageParameters.class);
        Assertions.assertEquals(64, model.getColumns());
        Assertions.assertEquals(64, model.getRows());
        Assertions.assertNull(model.getZoom());
        Assertions.assertNull(model.isShowBranding());
        Assertions.assertNull(model.getGeometry());
        Assertions.assertNull(model.getImageSize());
    }
}
