// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import java.time.Duration;

/**
 * Parameters for performing vector searches.
 */
public final class SemanticSearchOptions {
    /*
     * The name of the semantic configuration that lists which fields should be
     * used for semantic ranking, captions, highlights, and answers
     */
    private String semanticConfigurationName;

    /*
     * Allows the user to choose whether a semantic call should fail completely, or to return partial results.
     */
    private SemanticErrorMode errorMode;

    /*
     * Allows the user to set an upper bound on the amount of time it takes for semantic enrichment to finish
     * processing before the request fails.
     */
    private Duration maxWaitDuration;

    /*
     * This parameter is only valid if the query type is 'semantic'. If set,
     * the query returns answers extracted from key passages in the highest
     * ranked documents. The number of answers returned can be configured by
     * appending the pipe character '|' followed by the 'count-<number of
     * answers>' option after the answers parameter value, such as
     * 'extractive|count-3'. Default count is 1. The confidence threshold can
     * be configured by appending the pipe character '|' followed by the
     * 'threshold-<confidence threshold>' option after the answers parameter
     * value, such as 'extractive|threshold-0.9'. Default threshold is 0.7.
     */
    private QueryAnswer queryAnswer;

    /*
     * This parameter is only valid if the query type is 'semantic'. If set,
     * the query returns captions extracted from key passages in the highest
     * ranked documents. When Captions is set to 'extractive', highlighting is
     * enabled by default, and can be configured by appending the pipe
     * character '|' followed by the 'highlight-<true/false>' option, such as
     * 'extractive|highlight-true'. Defaults to 'None'.
     */
    private QueryCaption queryCaption;

    /*
     * Allows setting a separate search query that will be solely used for semantic reranking, semantic captions and
     * semantic answers. Is useful for scenarios where there is a need to use different queries between the base
     * retrieval and ranking phase, and the L2 semantic phase.
     */
    private String semanticQuery;

    /**
     * Creates a new instance of {@link SemanticSearchOptions}.
     */
    public SemanticSearchOptions() {
    }

    /**
     * Get the semanticConfigurationName property: The name of the semantic configuration that lists which fields should
     * be used for semantic ranking, captions, highlights, and answers.
     *
     * @return the semanticConfigurationName value.
     */
    public String getSemanticConfigurationName() {
        return this.semanticConfigurationName;
    }

    /**
     * Set the semanticConfigurationName property: The name of the semantic configuration that lists which fields should
     * be used for semantic ranking, captions, highlights, and answers.
     *
     * @param semanticConfigurationName the semanticConfigurationName value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setSemanticConfigurationName(String semanticConfigurationName) {
        this.semanticConfigurationName = semanticConfigurationName;
        return this;
    }

    /**
     * Get the semanticErrorHandling property: Allows the user to choose whether a semantic call should fail completely,
     * or to return partial results.
     *
     * @return the semanticErrorHandling value.
     */
    public SemanticErrorMode getErrorMode() {
        return this.errorMode;
    }

    /**
     * Set the semanticErrorHandling property: Allows the user to choose whether a semantic call should fail completely,
     * or to return partial results.
     *
     * @param errorMode the semanticErrorHandling value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setErrorMode(SemanticErrorMode errorMode) {
        this.errorMode = errorMode;
        return this;
    }

    /**
     * Get the semanticMaxWaitInMilliseconds property: Allows the user to set an upper bound on the amount of time it
     * takes for semantic enrichment to finish processing before the request fails.
     *
     * @return the semanticMaxWaitDuration value.
     */
    public Duration getMaxWaitDuration() {
        return this.maxWaitDuration;
    }

    /**
     * Set the semanticMaxWaitDuration property: Allows the user to set an upper bound on the amount of time it
     * takes for semantic enrichment to finish processing before the request fails.
     *
     * @param maxWaitDuration the semanticMaxWaitInMilliseconds value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setMaxWaitDuration(Duration maxWaitDuration) {
        this.maxWaitDuration = maxWaitDuration;
        return this;
    }

    /**
     * Get the answers property: This parameter is only valid if the query type is 'semantic'. If set, the query returns
     * answers extracted from key passages in the highest ranked documents. The number of answers returned can be
     * configured by appending the pipe character '|' followed by the 'count-&lt;number of answers&gt;' option after the
     * answers parameter value, such as 'extractive|count-3'. Default count is 1. The confidence threshold can be
     * configured by appending the pipe character '|' followed by the 'threshold-&lt;confidence threshold&gt;' option
     * after the answers parameter value, such as 'extractive|threshold-0.9'. Default threshold is 0.7.
     *
     * @return the answers value.
     */
    public QueryAnswer getQueryAnswer() {
        return this.queryAnswer;
    }

    /**
     * Set the answers property: This parameter is only valid if the query type is 'semantic'. If set, the query returns
     * answers extracted from key passages in the highest ranked documents. The number of answers returned can be
     * configured by appending the pipe character '|' followed by the 'count-&lt;number of answers&gt;' option after the
     * answers parameter value, such as 'extractive|count-3'. Default count is 1. The confidence threshold can be
     * configured by appending the pipe character '|' followed by the 'threshold-&lt;confidence threshold&gt;' option
     * after the answers parameter value, such as 'extractive|threshold-0.9'. Default threshold is 0.7.
     *
     * @param queryAnswer the answers value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setQueryAnswer(QueryAnswer queryAnswer) {
        this.queryAnswer = queryAnswer;
        return this;
    }

    /**
     * Get the query caption property: This parameter is only valid if the query type is 'semantic'. If set, the query
     * returns captions extracted from key passages in the highest ranked documents. When Captions is set to
     * 'extractive', highlighting is enabled by default, and can be configured by appending the pipe character '|'
     * followed by the 'highlight-&lt;true/false&gt;' option, such as 'extractive|highlight-true'. Defaults to 'None'.
     *
     * @return the query caption value.
     */
    public QueryCaption getQueryCaption() {
        return this.queryCaption;
    }

    /**
     * Set the query caption property: This parameter is only valid if the query type is 'semantic'. If set, the query
     * returns captions extracted from key passages in the highest ranked documents. When Captions is set to
     * 'extractive', highlighting is enabled by default, and can be configured by appending the pipe character '|'
     * followed by the 'highlight-&lt;true/false&gt;' option, such as 'extractive|highlight-true'. Defaults to 'None'.
     *
     * @param queryCaption the query caption value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setQueryCaption(QueryCaption queryCaption) {
        this.queryCaption = queryCaption;
        return this;
    }

    /**
     * Get the semanticQuery property: Allows setting a separate search query that will be solely used for semantic
     * reranking, semantic captions and semantic answers. Is useful for scenarios where there is a need to use different
     * queries between the base retrieval and ranking phase, and the L2 semantic phase.
     *
     * @return the semanticQuery value.
     */
    public String getSemanticQuery() {
        return this.semanticQuery;
    }

    /**
     * Set the semanticQuery property: Allows setting a separate search query that will be solely used for semantic
     * reranking, semantic captions and semantic answers. Is useful for scenarios where there is a need to use different
     * queries between the base retrieval and ranking phase, and the L2 semantic phase.
     *
     * @param semanticQuery the semanticQuery value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setSemanticQuery(String semanticQuery) {
        this.semanticQuery = semanticQuery;
        return this;
    }
}
