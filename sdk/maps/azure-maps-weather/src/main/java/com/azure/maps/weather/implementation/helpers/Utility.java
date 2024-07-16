// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather.implementation.helpers;

import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoPolygon;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.weather.implementation.models.GeoJsonGeometry;
import com.azure.maps.weather.implementation.models.GeoJsonGeometryCollection;

import java.io.ByteArrayOutputStream;

public class Utility {
    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);

    /**
     * Converts a {@link GeoCollection} into a private {@link GeoJsonGeometryCollection}.
     *
     * @param geometry
     * @return
     */
    public static GeoPolygon toGeoPolygon(GeoJsonGeometry geometry) {
        // serialize to GeoJson
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SERIALIZER.serialize(baos, geometry);

        // deserialize into GeoPolygon
        final TypeReference<GeoPolygon> typeReference = new TypeReference<GeoPolygon>(){};
        return SERIALIZER.deserializeFromBytes(baos.toByteArray(), typeReference);
    }
}
