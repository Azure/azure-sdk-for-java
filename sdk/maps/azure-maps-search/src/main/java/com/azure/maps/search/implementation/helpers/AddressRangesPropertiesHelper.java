package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.implementation.models.AddressRangesPrivate;
import com.azure.maps.search.models.AddressRanges;

/**
 * The helper class to set the non-public properties of an {@link AddressRangesPrivate} instance.
 */
public final class AddressRangesPropertiesHelper {
    private static AddressRangesAccessor accessor;

    private AddressRangesPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AddressRangesPrivate} instance.
     */
    public interface AddressRangesAccessor {
        void setFromAddressRangesPrivate(AddressRanges addressRanges, AddressRangesPrivate addressRangesPrivate);
    }

    /**
     * The method called from {@link AddressRanges} to set it's accessor.
     *
     * @param addressRangesAccessor The accessor.
     */
    public static void setAccessor(final AddressRangesAccessor addressRangesAccessor) {
        accessor = addressRangesAccessor;
    }

    /**
     * Sets properties of an {@link AddressRanges} object using a private model.
     *
     * @param addressRanges
     * @param addressRangesPrivate
     */
    public static void setFromAddressRangesPrivate(AddressRanges addressRanges, AddressRangesPrivate addressRangesPrivate) {
        accessor.setFromAddressRangesPrivate(addressRanges, addressRangesPrivate);
    }
}
