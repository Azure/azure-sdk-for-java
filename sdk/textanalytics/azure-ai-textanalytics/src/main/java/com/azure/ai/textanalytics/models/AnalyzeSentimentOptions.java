// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link AnalyzeSentimentOptions} model.
 */
@Fluent
public final class AnalyzeSentimentOptions {
    private TextAnalyticsRequestOptions requestOptions;

    /*
     * The boolean indicator to include opinion mining data in the returned result. If this
     * flag is specified, you'll get a {@code minedOpinions} property on SentenceSentiment. It's available start from
     * v3.1-preview.1 service version.
     */
    private boolean includeOpinionMining;

    /**
     * Get the value of {@code requestOptions}.
     *
     * @return the value of {@code requestOptions}.
     */
    public TextAnalyticsRequestOptions getRequestOptions() {
        return requestOptions;
    }

    /**
     * Set the value of {@code includeOpinionMining}.
     *
     * @param requestOptions It used to configure the scoring model for documents and show statistics.
     * @return the AnalyzeSentimentOptions object itself.
     */
    public AnalyzeSentimentOptions setRequestOptions(TextAnalyticsRequestOptions requestOptions) {
        this.requestOptions = requestOptions;
        return this;
    }

    /**
     * Get the value of {@code includeOpinionMining}.
     *
     * @return the value of {@code includeOpinionMining}.
     */
    public boolean isIncludeOpinionMining() {
        return includeOpinionMining;
    }

    /**
     * Set the value of {@code includeOpinionMining}.
     *
     * @param includeOpinionMining The boolean indicator to include opinion mining data in the returned result. If this
     * flag is specified, you'll get a {@code minedOpinions} property on SentenceSentiment. It's available start from
     * v3.1-preview.1 service version.
     *
     * @return the AnalyzeSentimentOptions object itself.
     */
    public AnalyzeSentimentOptions setIncludeOpinionMining(boolean includeOpinionMining) {
        this.includeOpinionMining = includeOpinionMining;
        return this;
    }
}
