// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for
// license information.

package com.azure.maps.search.models;

import com.azure.core.annotation.Immutable;
import com.azure.maps.search.implementation.helpers.ReverseSearchAddressBatchItemPropertiesHelper;

/** An item returned from Search Address Batch service call. */
@Immutable
public final class ReverseSearchAddressBatchItem {
    private Integer statusCode;
    private ErrorDetail error;
    private ReverseSearchAddressResult result;

    static {
        ReverseSearchAddressBatchItemPropertiesHelper.setAccessor(
                new ReverseSearchAddressBatchItemPropertiesHelper.ReverseSearchAddressBatchItemAccessor() {
            @Override
            public void setErrorDetail(ReverseSearchAddressBatchItem item, ErrorDetail detail) {
                item.setErrorDetail(detail);
            }

            @Override
            public void setReverseSearchAddressResult(ReverseSearchAddressBatchItem item,
                    ReverseSearchAddressResult result) {
                item.setReverseSearchAddressResult(result);
            }

            @Override
            public void setStatusCode(ReverseSearchAddressBatchItem item, Integer statusCode) {
                item.setStatusCode(statusCode);
            }
        });
    }

    /**
     * Get the statusCode property: HTTP request status code.
     *
     * @return the statusCode value.
     */
    public Integer getStatusCode() {
        return this.statusCode;
    }

    /**
     * Get the error property: The error object.
     *
     * @return the error value.
     */
    public ErrorDetail getError() {
        return this.error;
    }

    /**
     * Results of this search.
     * @return
     */
    public ReverseSearchAddressResult getResult() {
        return result;
    }

    // private setters
    private void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    private void setErrorDetail(ErrorDetail error) {
        this.error = error;
    }

    private void setReverseSearchAddressResult(ReverseSearchAddressResult result) {
        this.result = result;
    }
}
