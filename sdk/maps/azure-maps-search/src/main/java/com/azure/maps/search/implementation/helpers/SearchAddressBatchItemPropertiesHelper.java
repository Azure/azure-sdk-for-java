package com.azure.maps.search.implementation.helpers;

import com.azure.maps.search.models.ErrorDetail;
import com.azure.maps.search.models.SearchAddressBatchItem;
import com.azure.maps.search.models.SearchAddressResult;

/**
 * The helper class to set the non-public properties of an {@link SearchAddressBatchItem} instance.
 * @param <T>
 */
public final class SearchAddressBatchItemPropertiesHelper {
    private static SearchAddressBatchItemAccessor accessor;

    private SearchAddressBatchItemPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link SearchAddressBatchItem} instance.
     */
    public interface SearchAddressBatchItemAccessor {
        void setStatusCode(SearchAddressBatchItem item, Integer statusCode);
        void setErrorDetail(SearchAddressBatchItem item, ErrorDetail detail);
        void setSearchAddressResult(SearchAddressBatchItem item, SearchAddressResult result);
    }

    /**
     * The method called from {@link SearchAddressResult} to set it's accessor.
     *
     * @param searchAddressResultAccessor The accessor.
     */
    public static void setAccessor(final SearchAddressBatchItemAccessor batchResultAccessor) {
        accessor = batchResultAccessor;
    }

    /**
     * Sets the status code of this {@link SearchAddressBatchItem} from a private model.
     *
     * @param item
     * @param statusCode
     */
    public static void setStatusCode(SearchAddressBatchItem item, Integer statusCode) {
        accessor.setStatusCode(item, statusCode);
    }

    /**
     * Sets the error detail of this {@link SearchAddressBatchItem} from a private model.
     *
     * @param item
     * @param detail
     */
    public static void setErrorDetail(SearchAddressBatchItem item, ErrorDetail detail) {
        accessor.setErrorDetail(item, detail);
    }

    /**
     * Sets the search address results of this {@link SearchAddressBatchItem} from a private model.
     *
     * @param item
     * @param result
     */
    public static void setSearchAddressResult(SearchAddressBatchItem item, SearchAddressResult result) {
        accessor.setSearchAddressResult(item, result);
    }
}
