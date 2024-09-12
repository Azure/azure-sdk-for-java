// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.implementation.helpers;

import com.azure.core.models.GeoObject;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.search.implementation.models.GeoJsonFeatureCollection;
import com.azure.maps.search.models.GeoJsonObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Utility method class.
 */
public class Utility {
    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);

    /**
     * Returns a GeoPosition from a comma-separated position string.
     * @param position
     * @return
     */
    public static GeoPosition fromCommaSeparatedString(String position) {
        final String[] coords = position.split(",");

        if (coords.length == 2) {
            return new GeoPosition(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
        }

        return null;
    }

    public static GeoPosition fromDoubleList(List<Double> position) {
        if (position.size() == 2) {
            return new GeoPosition(position.get(0), position.get(1));
        }
        return null;
    }

    public static GeoObject toGeoObject(GeoJsonObject object) {
        // serialize to GeoJson
        if (object instanceof GeoJsonFeatureCollection) {
            GeoJsonFeatureCollection fc = (GeoJsonFeatureCollection) object;
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SERIALIZER.serialize(baos, fc.getFeatures().get(0).getGeometry());

            // deserialize into GeoObject
            final TypeReference<GeoObject> typeReference = new TypeReference<GeoObject>() { };
            return SERIALIZER.deserializeFromBytes(baos.toByteArray(), typeReference);
        }

        return null;
    }

}
