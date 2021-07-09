// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * Encapsulates options that can be specified for an operation within a {@link TransactionalBatch}.
 */
public final class TransactionalBatchItemRequestOptions
        extends TransactionalBatchItemRequestOptionsBase {

    public TransactionalBatchItemRequestOptions(){
    }

    /**
     * Sets the If-Match (ETag) associated with the operation in TransactionalBatch.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    public TransactionalBatchItemRequestOptions setIfMatchETag(final String ifMatchETag) {
        super.setIfMatchETagCore(ifMatchETag);
        return this;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in operation in TransactionalBatch.
     *
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    public TransactionalBatchItemRequestOptions setIfNoneMatchETag(final String ifNoneMatchEtag) {
        super.setIfNoneMatchETagCore(ifNoneMatchEtag);
        return this;
    }
}
