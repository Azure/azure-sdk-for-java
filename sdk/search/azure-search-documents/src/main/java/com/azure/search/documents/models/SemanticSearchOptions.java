// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import java.util.List;

/**
 * Parameters for performing vector searches.
 */
public final class SemanticSearchOptions {
    /*
     * Enables a debugging tool that can be used to further explore your search results.
     */
    private QueryDebugMode debug;

    /*
     * The name of the semantic configuration that lists which fields should be
     * used for semantic ranking, captions, highlights, and answers
     */
    private String semanticConfigurationName;

    /*
     * Allows the user to choose whether a semantic call should fail completely, or to return partial results.
     */
    private SemanticErrorHandling semanticErrorHandling;

    /*
     * Allows the user to set an upper bound on the amount of time it takes for semantic enrichment to finish
     * processing before the request fails.
     */
    private Integer semanticMaxWaitInMilliseconds;

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
    private QueryAnswerType answers;

    /*
     * This parameter is only valid if the query type is 'semantic'. If set,
     * the query returns answers extracted from key passages in the highest
     * ranked documents. The number of answers returned can be configured by
     * appending the pipe character '|' followed by the 'count-<number of
     * answers>' option after the answers parameter value, such as
     * 'extractive|count-3'. Default count is 1.
     */
    private Integer answersCount;

    /*
     * This parameter is only valid if the query type is 'semantic'.
     * The confidence threshold can be configured by appending the pipe
     * character '|' followed by the 'threshold-<confidence threshold>'
     * option after the answers parameter value, such as
     * 'extractive|threshold-0.9'. Default threshold is 0.7.
     */
    private Double answerThreshold;

    /*
     * This parameter is only valid if the query type is 'semantic'. If set,
     * the query returns captions extracted from key passages in the highest
     * ranked documents. When Captions is set to 'extractive', highlighting is
     * enabled by default, and can be configured by appending the pipe
     * character '|' followed by the 'highlight-<true/false>' option, such as
     * 'extractive|highlight-true'. Defaults to 'None'.
     */
    private QueryCaptionType queryCaption;

    /*
     * This parameter is only valid if the query type is 'semantic'. If set,
     * the query returns captions extracted from key passages in the highest
     * ranked documents. When Captions is set to 'extractive', highlighting is
     * enabled by default, and can be configured by appending the pipe
     * character '|' followed by the 'highlight-<true/false>' option, such as
     * 'extractive|highlight-true'. Defaults to 'None'.
     */
    private Boolean queryCaptionHighlightEnabled;

    /*
     * The list of field names used for semantic search.
     */
    private List<String> semanticFields;

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
    public SemanticErrorHandling getSemanticErrorHandling() {
        return this.semanticErrorHandling;
    }

    /**
     * Set the semanticErrorHandling property: Allows the user to choose whether a semantic call should fail completely,
     * or to return partial results.
     *
     * @param semanticErrorHandling the semanticErrorHandling value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setSemanticErrorHandling(SemanticErrorHandling semanticErrorHandling) {
        this.semanticErrorHandling = semanticErrorHandling;
        return this;
    }

    /**
     * Get the semanticMaxWaitInMilliseconds property: Allows the user to set an upper bound on the amount of time it
     * takes for semantic enrichment to finish processing before the request fails.
     *
     * @return the semanticMaxWaitInMilliseconds value.
     */
    public Integer getSemanticMaxWaitInMilliseconds() {
        return this.semanticMaxWaitInMilliseconds;
    }

    /**
     * Set the semanticMaxWaitInMilliseconds property: Allows the user to set an upper bound on the amount of time it
     * takes for semantic enrichment to finish processing before the request fails.
     *
     * @param semanticMaxWaitInMilliseconds the semanticMaxWaitInMilliseconds value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setSemanticMaxWaitInMilliseconds(Integer semanticMaxWaitInMilliseconds) {
        this.semanticMaxWaitInMilliseconds = semanticMaxWaitInMilliseconds;
        return this;
    }

    /**
     * Get the debug property: Enables a debugging tool that can be used to further explore your search results.
     *
     * @return the debug value.
     */
    public QueryDebugMode getDebug() {
        return this.debug;
    }

    /**
     * Set the debug property: Enables a debugging tool that can be used to further explore your search results.
     *
     * @param debug the debug value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setDebug(QueryDebugMode debug) {
        this.debug = debug;
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
    public QueryAnswerType getQueryAnswer() {
        return this.answers;
    }

    /**
     * Set the answers property: This parameter is only valid if the query type is 'semantic'. If set, the query returns
     * answers extracted from key passages in the highest ranked documents. The number of answers returned can be
     * configured by appending the pipe character '|' followed by the 'count-&lt;number of answers&gt;' option after the
     * answers parameter value, such as 'extractive|count-3'. Default count is 1. The confidence threshold can be
     * configured by appending the pipe character '|' followed by the 'threshold-&lt;confidence threshold&gt;' option
     * after the answers parameter value, such as 'extractive|threshold-0.9'. Default threshold is 0.7.
     *
     * @param answers the answers value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setQueryAnswer(QueryAnswerType answers) {
        this.answers = answers;
        return this;
    }

    /**
     * Get the answers count property: This parameter is only valid if the query type is 'semantic'. If set, the query
     * returns answers extracted from key passages in the highest ranked documents. The number of answers returned can
     * be configured by appending the pipe character '|' followed by the 'count-&lt;number of answers&gt;' option after
     * the answers parameter value, such as 'extractive|count-3'. Default count is 1.
     *
     * @return the answers count value.
     */
    public Integer getAnswersCount() {
        return this.answersCount;
    }

    /**
     * Set the answers count property: This parameter is only valid if the query type is 'semantic'. If set, the query
     * returns answers extracted from key passages in the highest ranked documents. The number of answers returned can
     * be configured by appending the pipe character '|' followed by the 'count-&lt;number of answers&gt;' option after
     * the answers parameter value, such as 'extractive|count-3'. Default count is 1.
     *
     * @param answersCount the answers count value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setAnswersCount(Integer answersCount) {
        this.answersCount = answersCount;
        return this;
    }

    /**
     * Get the answer threshold property: This parameter is only valid if the query type is 'semantic'.
     * The confidence threshold can be configured by appending the pipe
     * character '|' followed by the 'threshold-&lt;confidence threshold&gt;'
     * option after the answers parameter value, such as
     * 'extractive|threshold-0.9'. Default threshold is 0.7.
     *
     * @return the answer threshold value.
     */
    public Double getAnswerThreshold() {
        return this.answerThreshold;
    }

    /**
     * Set the answer threshold property: This parameter is only valid if the query type is 'semantic'.
     * The confidence threshold can be configured by appending the pipe
     * character '|' followed by the 'threshold-&lt;confidence threshold&gt;'
     * option after the answers parameter value, such as
     * 'extractive|threshold-0.9'. Default threshold is 0.7.
     * @param answerThreshold the answer threshold value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setAnswerThreshold(Double answerThreshold) {
        this.answerThreshold = answerThreshold;
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
    public QueryCaptionType getQueryCaption() {
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
    public SemanticSearchOptions setQueryCaption(QueryCaptionType queryCaption) {
        this.queryCaption = queryCaption;
        return this;
    }

    /**
     * Get the query caption highlight property: This parameter is only valid if the query type is 'semantic'. If set,
     * the query returns captions extracted from key passages in the highest ranked documents. When Captions is set to
     * 'extractive', highlighting is enabled by default, and can be configured by appending the pipe character '|'
     * followed by the 'highlight-&lt;true/false&gt;' option, such as 'extractive|highlight-true'. Defaults to 'None'.
     *
     * @return the query caption highlight value.
     */
    public Boolean getQueryCaptionHighlightEnabled() {
        return this.queryCaptionHighlightEnabled;
    }

    /**
     * Set the query caption highlight property: This parameter is only valid if the query type is 'semantic'. If set,
     * the query returns captions extracted from key passages in the highest ranked documents. When Captions is set to
     * 'extractive', highlighting is enabled by default, and can be configured by appending the pipe character '|'
     * followed by the 'highlight-&lt;true/false&gt;' option, such as 'extractive|highlight-true'. Defaults to 'None'.
     *
     * @param queryCaptionHighlightEnabled the query caption highlight value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setQueryCaptionHighlightEnabled(Boolean queryCaptionHighlightEnabled) {
        this.queryCaptionHighlightEnabled = queryCaptionHighlightEnabled;
        return this;
    }

    /**
     * Get the semanticFields property: The list of field names used for semantic search.
     *
     * @return the semanticFields value.
     */
    public List<String> getSemanticFields() {
        return this.semanticFields;
    }

    /**
     * Set the semanticFields property: The list of field names used for semantic search.
     *
     * @param semanticFields the semanticFields value to set.
     * @return the SemanticSearchOptions object itself.
     */
    public SemanticSearchOptions setSemanticFields(List<String> semanticFields) {
        this.semanticFields = semanticFields;
        return this;
    }
}
