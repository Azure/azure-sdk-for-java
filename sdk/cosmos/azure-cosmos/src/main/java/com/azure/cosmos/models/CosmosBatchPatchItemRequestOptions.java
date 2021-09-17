// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.util.Beta;

/**
 * Encapsulates options that can be specified for an operation within a {@link CosmosBatch}.
 */
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosBatchPatchItemRequestOptions {

    private String ifMatchETag;
    private String ifNoneMatchETag;
    private String filterPredicate;

    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBatchPatchItemRequestOptions() {
        super();
    }

    /**
     * Gets the FilterPredicate associated with the request in the Azure Cosmos DB service.
     *
     * @return the FilterPredicate associated with the request.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getFilterPredicate() {
        return this.filterPredicate;
    }

    /**
     * Sets the FilterPredicate associated with the request in the Azure Cosmos DB service. for example: {@code setFilterPredicate("from c where c.taskNum = 3")}.
     *
     * @param filterPredicate the filterPredicate associated with the request.
     * @return the current request options
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBatchPatchItemRequestOptions setFilterPredicate(String filterPredicate) {
        this.filterPredicate = filterPredicate;
        return this;
    }

    /**
     * Gets the If-Match (ETag) associated with the operation in CosmosBatch.
     *
     * @return ifMatchETag the ifMatchETag associated with the request.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getIfMatchETag() {
        return this.ifMatchETag;
    }

    /**
     * Sets the If-Match (ETag) associated with the operation in CosmosBatch.
     *
     * @param ifMatchETag the ifMatchETag associated with the request.
     * @return the current request options
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBatchPatchItemRequestOptions setIfMatchETag(final String ifMatchETag) {
        this.ifMatchETag = ifMatchETag;
        return this;
    }

    /**
     * Gets the If-None-Match (ETag) associated with the request in operation in CosmosBatch.
     *
     * @return the ifNoneMatchETag associated with the request.
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getIfNoneMatchETag() {
        return this.ifNoneMatchETag;
    }

    /**
     * Sets the If-None-Match (ETag) associated with the request in operation in CosmosBatch.
     *
     * @param ifNoneMatchEtag the ifNoneMatchETag associated with the request.
     * @return the current request options
     */
    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosBatchPatchItemRequestOptions setIfNoneMatchETag(final String ifNoneMatchEtag) {
        this.ifNoneMatchETag = ifNoneMatchEtag;
        return this;
    }

    RequestOptions toRequestOptions() {
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.setIfMatchETag(this.ifMatchETag);
        requestOptions.setIfNoneMatchETag(this.ifNoneMatchETag);
        requestOptions.setFilterPredicate(this.filterPredicate);
        return requestOptions;
    }
}
