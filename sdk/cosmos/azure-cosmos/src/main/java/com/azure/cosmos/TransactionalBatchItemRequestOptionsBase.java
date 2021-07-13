// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * Encapsulates options that can be specified for an operation within a {@link TransactionalBatch}.
 */
class TransactionalBatchItemRequestOptionsBase {
    private String ifMatchETag;
    private String ifNoneMatchETag;

    protected TransactionalBatchItemRequestOptionsBase(){
    }

    /**
     * Gets the If-Match (ETag) associated with the operation in TransactionalBatch.
     *
     * @return ifMatchETag the ifMatchETag associated with the request.
     */
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the operation in TransactionalBatch.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     */
    public void setIfMatchETagCore(final String ifMatchETag){
        this.ifMatchETag = ifMatchETag;
        return;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in operation in TransactionalBatch.
     *
     * @return the ifNoneMatchETag associated with the request.
     */
    public String getIfNoneMatchETag() {
        return this.ifNoneMatchETag;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in operation in TransactionalBatch.
     *
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     */
    public void setIfNoneMatchETagCore(final String ifNoneMatchEtag){
        this.ifNoneMatchETag = ifNoneMatchEtag;
        return;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(getIfMatchETag());
        requestOptions.setIfNoneMatchETag(getIfNoneMatchETag());
        return requestOptions;
    }
}
