package com.azure.cosmos;

import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.util.Beta;

@Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public class TransactionalBatchPatchItemRequestOptions extends TransactionalBatchItemRequestOptionsBase {
    private String filterPredicate;

    /**
     * Gets the FilterPredicate associated with the request in the Azure Cosmos DB service.
     *
     * @return the FilterPredicate associated with the request.
     */
    public String getFilterPredicate() {
        return this.filterPredicate;
    }

    /**
     * Sets the FilterPredicate associated with the request in the Azure Cosmos DB service.
     *
     * @param filterPredicate the filterPredicate associated with the request.
     * @return the current request options
     */
    public TransactionalBatchPatchItemRequestOptions setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
        return this;
    }

    /**
     * Sets the If-Match (ETag) associated with the operation in TransactionalBatch.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public TransactionalBatchPatchItemRequestOptions setIfMatchETag(final String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in operation in TransactionalBatch.
     *
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    @Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public TransactionalBatchPatchItemRequestOptions setIfNoneMatchETag(final String ifNoneMatchEtag) {
        this.ifNoneMatchETag = ifNoneMatchEtag;
        return this;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = super.toRequestOptions();
        requestOptions.setFilterPredicate(getFilterPredicate());
        return requestOptions;
    }
}
