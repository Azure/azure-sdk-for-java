// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import com.azure.storage.blob.models.PageBlobGetPageRangesDiffHeaders;
import com.azure.storage.blob.models.PageList;

/**
 * Contains all response data for the getPageRangesDiff operation.
 */
public final class PageBlobsGetPageRangesDiffResponse extends ResponseBase<com.azure.storage.blob.models.PageBlobGetPageRangesDiffHeaders, com.azure.storage.blob.models.PageList> {
    /**
     * Creates an instance of PageBlobsGetPageRangesDiffResponse.
     *
     * @param request the request which resulted in this PageBlobsGetPageRangesDiffResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public PageBlobsGetPageRangesDiffResponse(HttpRequest request, int statusCode, HttpHeaders rawHeaders, com.azure.storage.blob.models.PageList value, PageBlobGetPageRangesDiffHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /**
     * @return the deserialized response body.
     */
    @Override
    public PageList value() {
        return super.value();
    }
}
