package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.implementation.models.GeoJsonObject;
import com.azure.maps.search.implementation.models.PolygonPrivate;
import com.azure.maps.search.models.Polygon;

/**
 * The helper class to set the non-public properties of an {@link PolygonPrivate} instance.
 */
public final class PolygonPropertiesHelper {
    private static PolygonAccessor accessor;

    private PolygonPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link Polygon} instance.
     */
    public interface PolygonAccessor {
        void setProviderID(Polygon result, String providerId);
        void setGeometry(Polygon result, GeoJsonObject geometry);
    }

    /**
     * The method called from {@link Polygon} to set it's accessor.
     *
     * @param PolygonAccessor The accessor.
     */
    public static void setAccessor(final PolygonAccessor PolygonAccessor) {
        accessor = PolygonAccessor;
    }

    /**
     * Sets the provider id of this {@link Polygon}
     *
     * @param result
     * @param providerId
     */
    public static void setProviderID(Polygon result, String providerId) {
        accessor.setProviderID(result, providerId);
    }

    /**
     * Sets the geometry of this {@link Polygon}
     *
     * @param result
     * @param geometry
     */
    public static void setGeometry(Polygon result, GeoJsonObject geometry) {
        accessor.setGeometry(result, geometry);
    }
}

