// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Encapsulates options for semantic reranking requests.
 */
@Beta(value = Beta.SinceVersion.V4_69_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class SemanticRerankRequestOptions {
    private Boolean returnDocuments;
    private Integer topK;
    private Integer batchSize;
    private Boolean sort;

    /**
     * Creates a new instance of SemanticRerankRequestOptions.
     */
    public SemanticRerankRequestOptions() {
    }

    /**
     * Gets whether to include original document text in results.
     *
     * @return true if documents should be returned, false otherwise. Default is true if not set.
     */
    public Boolean getReturnDocuments() {
        return returnDocuments;
    }

    /**
     * Sets whether to include original document text in results.
     *
     * @param returnDocuments true to include documents in results, false otherwise.
     * @return the SemanticRerankRequestOptions.
     */
    public SemanticRerankRequestOptions setReturnDocuments(Boolean returnDocuments) {
        this.returnDocuments = returnDocuments;
        return this;
    }

    /**
     * Gets the maximum number of scored documents to return.
     *
     * @return the maximum number of documents to return, or null if not set.
     */
    public Integer getTopK() {
        return topK;
    }

    /**
     * Sets the maximum number of scored documents to return.
     *
     * @param topK the maximum number of documents to return.
     * @return the SemanticRerankRequestOptions.
     */
    public SemanticRerankRequestOptions setTopK(Integer topK) {
        if (topK != null && topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }
        this.topK = topK;
        return this;
    }

    /**
     * Gets the batch size for internal scoring operations.
     *
     * @return the batch size, or null if not set.
     */
    public Integer getBatchSize() {
        return batchSize;
    }

    /**
     * Sets the batch size for internal scoring operations.
     *
     * @param batchSize the batch size for scoring operations.
     * @return the SemanticRerankRequestOptions.
     */
    public SemanticRerankRequestOptions setBatchSize(Integer batchSize) {
        if (batchSize != null && batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be greater than 0");
        }
        this.batchSize = batchSize;
        return this;
    }

    /**
     * Gets whether results should be ordered by descending score.
     *
     * @return true if results should be sorted, false otherwise. Default is true if not set.
     */
    public Boolean getSort() {
        return sort;
    }

    /**
     * Sets whether results should be ordered by descending score.
     *
     * @param sort true to sort results by descending score, false otherwise.
     * @return the SemanticRerankRequestOptions.
     */
    public SemanticRerankRequestOptions setSort(Boolean sort) {
        this.sort = sort;
        return this;
    }
}
