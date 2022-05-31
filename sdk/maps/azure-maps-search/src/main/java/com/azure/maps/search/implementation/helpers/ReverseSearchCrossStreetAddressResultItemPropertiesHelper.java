package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.implementation.models.ReverseSearchCrossStreetAddressResultItemPrivate;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressResultItem;

/**
 * The helper class to set the non-public properties of an {@link ReverseSearchCrossStreetAddressResultItem} instance.
 */
public final class ReverseSearchCrossStreetAddressResultItemPropertiesHelper {
    private static ReverseSearchCrossStreetAddressResultItemAccessor accessor;

    private ReverseSearchCrossStreetAddressResultItemPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ReverseSearchCrossStreetAddressResultItem} instance.
     */
    public interface ReverseSearchCrossStreetAddressResultItemAccessor {
        void setFromReverseSearchCrossStreetAddressResultItemPrivate(ReverseSearchCrossStreetAddressResultItem resultItem,
            ReverseSearchCrossStreetAddressResultItemPrivate privateResultItem);
    }

    /**
     * The method called from {@link ReverseSearchCrossStreetAddressResultItem} to set it's accessor.
     *
     * @param ReverseSearchCrossStreetAddressResultItemAccessor The accessor.
     */
    public static void setAccessor(final ReverseSearchCrossStreetAddressResultItemAccessor itemAccessor) {
        accessor = itemAccessor;
    }

    /**
     * Sets all properties of this {@link ReverseSearchCrossStreetAddressResultItem} using a private model.
     *
     * @param resultItem
     * @param privateResultItem
     */
    public static void setFromReverseSearchCrossStreetAddressResultItemPrivate(ReverseSearchCrossStreetAddressResultItem resultItem,
        ReverseSearchCrossStreetAddressResultItemPrivate privateResultItem) {
        accessor.setFromReverseSearchCrossStreetAddressResultItemPrivate(resultItem, privateResultItem);
    }
}

