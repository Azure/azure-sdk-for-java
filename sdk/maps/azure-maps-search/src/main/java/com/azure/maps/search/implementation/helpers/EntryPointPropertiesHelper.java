package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.implementation.models.EntryPointPrivate;
import com.azure.maps.search.implementation.models.LatLongPairAbbreviated;
import com.azure.maps.search.models.EntryPoint;
import com.azure.maps.search.models.EntryPointType;

public class EntryPointPropertiesHelper {
    private static EntryPointAccessor accessor;

    private EntryPointPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link EntryPointPrivate} instance.
     */
    public interface EntryPointAccessor {
        void setType(EntryPoint entryPoint, EntryPointType type);
        void setPosition(EntryPoint entryPoint, LatLongPairAbbreviated position);
    }

    /**
     * The method called from {@link EntryPoint} to set it's accessor.
     *
     * @param entryPointAccessor The accessor.
     */
    public static void setAccessor(final EntryPointAccessor entryPointAccessor) {
        accessor = entryPointAccessor;
    }

    /**
     * Sets the type of {@link EntryPoint}.
     *
     * @param entryPoint
     * @param type
     */
    public static void setType(EntryPoint entryPoint, EntryPointType type) {
        accessor.setType(entryPoint, type);
    }

    /**
     * Sets the position of this {@link EntryPoint}
     * @param entryPoint
     * @param position
     */
    public static void setPosition(EntryPoint entryPoint, LatLongPairAbbreviated position) {
        accessor.setPosition(entryPoint, position);
    }
}
