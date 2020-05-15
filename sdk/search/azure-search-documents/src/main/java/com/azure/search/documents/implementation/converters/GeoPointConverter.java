package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.models.GeoPoint;

/**
 * Auto generated code for default converter.
 * Update {@code convert} methods if {@link com.azure.search.documents.models.GeoPoint} and
 * {@link GeoPoint} mismatch.
 */
public final class GeoPointConverter {
    public static GeoPoint convert(com.azure.search.documents.models.GeoPoint obj) {
        return DefaultConverter.convert(obj, GeoPoint.class);
    }

    public static com.azure.search.documents.models.GeoPoint convert(GeoPoint obj) {
        return DefaultConverter.convert(obj, com.azure.search.documents.models.GeoPoint.class);
    }
}
