// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.models.GeoObject;

import java.util.Map;

/**
 * Helper class to access private values of {@link GeoObject} across package boundaries.
 */
public final class GeoObjectHelper {
    private static GeoObjectAccessor accessor;

    private GeoObjectHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link GeoObjectHelper} instance.
     */
    public interface GeoObjectAccessor {
        /**
         * Gets the custom properties of the passed {@link GeoObject}.
         *
         * @param geoObject The {@link GeoObject} used to get custom properties.
         * @return The custom properties of the {@link GeoObject}.
         */
        Map<String, Object> getCustomProperties(GeoObject geoObject);
    }

    /**
     * The method called from {@link GeoObject} to set it's accessor.
     *
     * @param geoObjectAccessor The accessor.
     */
    public static void setAccessor(final GeoObjectAccessor geoObjectAccessor) {
        accessor = geoObjectAccessor;
    }

    /**
     * Gets the custom properties of the passed {@link GeoObject}.
     *
     * @param geoObject The {@link GeoObject} used to get custom properties.
     * @return The custom properties of the {@link GeoObject}.
     */
    public static Map<String, Object> getCustomProperties(GeoObject geoObject) {
        return accessor.getCustomProperties(geoObject);
    }
}
