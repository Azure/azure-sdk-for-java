package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.implementation.models.ReverseSearchAddressResultItemPrivate;
import com.azure.maps.search.models.ReverseSearchAddressResultItem;

/**
 * The helper class to set the non-public properties of an {@link ReverseSearchAddressResultItem} instance.
 */
public final class ReverseSearchAddressResultItemPropertiesHelper {
    private static ReverseSearchAddressResultItemAccessor accessor;

    private ReverseSearchAddressResultItemPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ReverseSearchAddressResultItem} instance.
     */
    public interface ReverseSearchAddressResultItemAccessor {
        void setFromReverseSearchAddressResultItemPrivate(ReverseSearchAddressResultItem resultItem,
            ReverseSearchAddressResultItemPrivate privateResultItem);
    }

    /**
     * The method called from {@link ReverseSearchAddressResultItem} to set it's accessor.
     *
     * @param reverseSearchAddressResultItemAccessor The accessor.
     */
    public static void setAccessor(final ReverseSearchAddressResultItemAccessor itemAccessor) {
        accessor = itemAccessor;
    }

    /**
     * Sets all properties of this {@link ReverseSearchAddressResultItem} using a private model.
     *
     * @param resultItem
     * @param privateResultItem
     */
    public static void setFromReverseSearchAddressResultItemPrivate(
            ReverseSearchAddressResultItem resultItem, ReverseSearchAddressResultItemPrivate privateResultItem) {
        accessor.setFromReverseSearchAddressResultItemPrivate(resultItem, privateResultItem);
    }
}

