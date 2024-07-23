// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.implementation.helpers;

import com.azure.core.models.ResponseError;
import com.azure.maps.search.models.ReverseSearchAddressBatchItem;
import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.SearchAddressResult;

/**
 * The helper class to set the non-public properties of an {@link ReverseSearchAddressBatchItem} instance.
 */
public final class ReverseSearchAddressBatchItemPropertiesHelper {
    private static ReverseSearchAddressBatchItemAccessor accessor;

    private ReverseSearchAddressBatchItemPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ReverseSearchAddressBatchItem} instance.
     */
    public interface ReverseSearchAddressBatchItemAccessor {
        void setStatusCode(ReverseSearchAddressBatchItem item, Integer statusCode);
        void setErrorDetail(ReverseSearchAddressBatchItem item, ResponseError detail);
        void setReverseSearchAddressResult(ReverseSearchAddressBatchItem item, ReverseSearchAddressResult result);
    }

    /**
     * The method called from {@link SearchAddressResult} to set it's accessor.
     *
     * @param batchResultAccessor The accessor.
     */
    public static void setAccessor(final ReverseSearchAddressBatchItemAccessor batchResultAccessor) {
        accessor = batchResultAccessor;
    }

    /**
     * Sets the status code of this {@link ReverseSearchAddressBatchItem}
     *
     * @param item
     * @param statusCode
     */
    public static void setStatusCode(ReverseSearchAddressBatchItem item, Integer statusCode) {
        accessor.setStatusCode(item, statusCode);
    }

    /**
     * Sets the error detail of this {@link ReverseSearchAddressBatchItem}
     *
     * @param item
     * @param detail
     */
    public static void setErrorDetail(ReverseSearchAddressBatchItem item, ResponseError detail) {
        accessor.setErrorDetail(item, detail);
    }

    /**
     * Sets the {@link ReverseSearchAddressResult} of this {@link ReverseSearchAddressBatchItem}.
     *
     * @param item
     * @param result
     */
    public static void setReverseSearchAddressResult(ReverseSearchAddressBatchItem item,
            ReverseSearchAddressResult result) {
        accessor.setReverseSearchAddressResult(item, result);
    }
}
