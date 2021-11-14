// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options that can be specified for an operation within a {@link CosmosBatch}.
 */
public final class CosmosBatchItemRequestOptions {

    private String ifMatchETag;
    private String ifNoneMatchETag;

    public CosmosBatchItemRequestOptions() {
    }

    /**
     * Gets the If-Match (ETag) associated with the operation in CosmosBatch.
     *
     * @return ifMatchETag the ifMatchETag associated with the request.
     */
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the operation in CosmosBatch.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosBatchItemRequestOptions setIfMatchETag(final String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in operation in CosmosBatch.
     *
     * @return the ifNoneMatchETag associated with the request.
     */
    public String getIfNoneMatchETag() {
        return this.ifNoneMatchETag;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in operation in CosmosBatch.
     *
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    public CosmosBatchItemRequestOptions setIfNoneMatchETag(final String ifNoneMatchEtag) {
        this.ifNoneMatchETag = ifNoneMatchEtag;
        return this;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(this.ifMatchETag);
        requestOptions.setIfNoneMatchETag(this.ifNoneMatchETag);
        return requestOptions;
    }
}
