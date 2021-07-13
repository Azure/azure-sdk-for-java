// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 *
 */
@Fluent
public final class ExtractSummarizedSentencesAction {
    private String modelVersion;
    private int summarizedSentenceCount;
    private SummarizedSentencesOrder summarizedSentenceOrder;


    /**
     * Gets the version of the text analytics model used by this operation.
     *
     * @return The model version.
     */
    public String getModelVersion() {
        return modelVersion;
    }

    /**
     * Set the model version. This value indicates which model will be used for scoring, e.g. "latest", "2019-10-01".
     * If a model-version is not specified, the API will default to the latest, non-preview version.
     *
     * @param modelVersion The model version.
     *
     * @return The {@link ExtractSummarizedSentencesAction} object itself.
     */
    public ExtractSummarizedSentencesAction setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    /**
     *
     * @return
     */
    public int getSummarizedSentenceCount() {
        return this.summarizedSentenceCount;
    }

    /**
     *
     * @param summarizedSentenceCount
     * @return
     */
    public ExtractSummarizedSentencesAction setSummarizedSentenceCount(int summarizedSentenceCount) {
        this.summarizedSentenceCount = summarizedSentenceCount;
        return this;
    }

    /**
     *
     * @return
     */
    public SummarizedSentencesOrder getSummarizedSentenceOrder() {
        return summarizedSentenceOrder;
    }

    /**
     *
     * @param summarizedSentenceOrder
     * @return
     */
    public ExtractSummarizedSentencesAction setSummarizedSentenceOrder(SummarizedSentencesOrder summarizedSentenceOrder) {
        this.summarizedSentenceOrder = summarizedSentenceOrder;
        return this;
    }
}
