// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;
import java.util.List;

/**
 * Additional parameters for searchGet operation.
 */
@Fluent
public final class SearchOptions {
    /*
     * A value that specifies whether to fetch the total count of results.
     * Default is false. Setting this value to true may have a performance
     * impact. Note that the count returned is an approximation.
     */
    private Boolean includeTotalCount;

    /*
     * The list of facet expressions to apply to the search query. Each facet
     * expression contains a field name, optionally followed by a
     * comma-separated list of name:value pairs.
     */
    private List<String> facets;

    /*
     * The OData $filter expression to apply to the search query.
     */
    private String filter;

    /*
     * The list of field names to use for hit highlights. Only searchable
     * fields can be used for hit highlighting.
     */
    private List<String> highlightFields;

    /*
     * A string tag that is appended to hit highlights. Must be set with
     * highlightPreTag. Default is &lt;/em&gt;.
     */
    private String highlightPostTag;

    /*
     * A string tag that is prepended to hit highlights. Must be set with
     * highlightPostTag. Default is &lt;em&gt;.
     */
    private String highlightPreTag;

    /*
     * A number between 0 and 100 indicating the percentage of the index that
     * must be covered by a search query in order for the query to be reported
     * as a success. This parameter can be useful for ensuring search
     * availability even for services with only one replica. The default is
     * 100.
     */
    private Double minimumCoverage;

    /*
     * The list of OData $orderby expressions by which to sort the results.
     * Each expression can be either a field name or a call to either the
     * geo.distance() or the search.score() functions. Each expression can be
     * followed by asc to indicate ascending, and desc to indicate descending.
     * The default is ascending order. Ties will be broken by the match scores
     * of documents. If no OrderBy is specified, the default sort order is
     * descending by document match score. There can be at most 32 $orderby
     * clauses.
     */
    private List<String> orderBy;

    /*
     * A value that specifies the syntax of the search query. The default is
     * 'simple'. Use 'full' if your query uses the Lucene query syntax.
     */
    private QueryType queryType;

    /*
     * The list of parameter values to be used in scoring functions (for
     * example, referencePointParameter) using the format name-values. For
     * example, if the scoring profile defines a function with a parameter
     * called 'mylocation' the parameter string would be
     * "mylocation--122.2,44.8" (without the quotes).
     */
    private List<ScoringParameter> scoringParameters;

    /*
     * The name of a scoring profile to evaluate match scores for matching
     * documents in order to sort the results.
     */
    private String scoringProfile;

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
     * Enables a debugging tool that can be used to further explore your search results.
     */
    private QueryDebugMode debug;

    /*
     * The list of field names to which to scope the full-text search. When
     * using fielded search (fieldName:searchExpression) in a full Lucene
     * query, the field names of each fielded search expression take precedence
     * over any field names listed in this parameter.
     */
    private List<String> searchFields;

    /*
     * The language of the query.
     */
    private QueryLanguage queryLanguage;

    /*
     * Improve search recall by spell-correcting individual search query terms.
     */
    private QuerySpellerType speller;

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
     * A value that specifies whether any or all of the search terms must be
     * matched in order to count the document as a match.
     */
    private SearchMode searchMode;

    /*
     * A value that specifies whether we want to calculate scoring statistics
     * (such as document frequency) globally for more consistent scoring, or
     * locally, for lower latency.
     */
    private ScoringStatistics scoringStatistics;

    /*
     * A value to be used to create a sticky session, which can help to get
     * more consistent results. As long as the same sessionId is used, a
     * best-effort attempt will be made to target the same replica set. Be wary
     * that reusing the same sessionID values repeatedly can interfere with the
     * load balancing of the requests across replicas and adversely affect the
     * performance of the search service. The value used as sessionId cannot
     * start with a '_' character.
     */
    private String sessionId;

    /*
     * The list of fields to retrieve. If unspecified, all fields marked as
     * retrievable in the schema are included.
     */
    private List<String> select;

    /*
     * The number of search results to skip. This value cannot be greater than
     * 100,000. If you need to scan documents in sequence, but cannot use $skip
     * due to this limitation, consider using $orderby on a totally-ordered key
     * and $filter with a range query instead.
     */
    private Integer skip;

    /*
     * The number of search results to retrieve. This can be used in
     * conjunction with $skip to implement client-side paging of search
     * results. If results are truncated due to server-side paging, the
     * response will include a continuation token that can be used to issue
     * another Search request for the next page of results.
     */
    private Integer top;

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
     * The query parameters for vector and hybrid search queries.
     */
    private SearchQueryVector vector;

    /**
     * Creates an instance of {@link SearchOptions}.
     */
    public SearchOptions() {
    }

    /**
     * Get the includeTotalCount property: A value that specifies whether to fetch the total count of results. Default
     * is false. Setting this value to true may have a performance impact. Note that the count returned is an
     * approximation.
     *
     * @return the includeTotalCount value.
     */
    public Boolean isTotalCountIncluded() {
        return this.includeTotalCount;
    }

    /**
     * Set the includeTotalCount property: A value that specifies whether to fetch the total count of results. Default
     * is false. Setting this value to true may have a performance impact. Note that the count returned is an
     * approximation.
     *
     * @param includeTotalCount the includeTotalCount value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setIncludeTotalCount(Boolean includeTotalCount) {
        this.includeTotalCount = includeTotalCount;
        return this;
    }

    /**
     * Get the facets property: The list of facet expressions to apply to the search query. Each facet expression
     * contains a field name, optionally followed by a comma-separated list of name:value pairs.
     *
     * @return the facets value.
     */
    public List<String> getFacets() {
        return this.facets;
    }

    /**
     * Set the facets property: The list of facet expressions to apply to the search query. Each facet expression
     * contains a field name, optionally followed by a comma-separated list of name:value pairs.
     *
     * @param facets the facets value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setFacets(String... facets) {
        this.facets = (facets == null) ? null : java.util.Arrays.asList(facets);
        return this;
    }

    /**
     * Get the filter property: The OData $filter expression to apply to the search query.
     *
     * @return the filter value.
     */
    public String getFilter() {
        return this.filter;
    }

    /**
     * Set the filter property: The OData $filter expression to apply to the search query.
     *
     * @param filter the filter value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Get the highlightFields property: The list of field names to use for hit highlights. Only searchable fields can
     * be used for hit highlighting.
     *
     * @return the highlightFields value.
     */
    public List<String> getHighlightFields() {
        return this.highlightFields;
    }

    /**
     * Set the highlightFields property: The list of field names to use for hit highlights. Only searchable fields can
     * be used for hit highlighting.
     *
     * @param highlightFields the highlightFields value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setHighlightFields(String... highlightFields) {
        this.highlightFields = (highlightFields == null) ? null : Arrays.asList(highlightFields);
        return this;
    }

    /**
     * Get the highlightPostTag property: A string tag that is appended to hit highlights. Must be set with
     * highlightPreTag. Default is &amp;lt;/em&amp;gt;.
     *
     * @return the highlightPostTag value.
     */
    public String getHighlightPostTag() {
        return this.highlightPostTag;
    }

    /**
     * Set the highlightPostTag property: A string tag that is appended to hit highlights. Must be set with
     * highlightPreTag. Default is &amp;lt;/em&amp;gt;.
     *
     * @param highlightPostTag the highlightPostTag value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setHighlightPostTag(String highlightPostTag) {
        this.highlightPostTag = highlightPostTag;
        return this;
    }

    /**
     * Get the highlightPreTag property: A string tag that is prepended to hit highlights. Must be set with
     * highlightPostTag. Default is &amp;lt;em&amp;gt;.
     *
     * @return the highlightPreTag value.
     */
    public String getHighlightPreTag() {
        return this.highlightPreTag;
    }

    /**
     * Set the highlightPreTag property: A string tag that is prepended to hit highlights. Must be set with
     * highlightPostTag. Default is &amp;lt;em&amp;gt;.
     *
     * @param highlightPreTag the highlightPreTag value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setHighlightPreTag(String highlightPreTag) {
        this.highlightPreTag = highlightPreTag;
        return this;
    }

    /**
     * Get the minimumCoverage property: A number between 0 and 100 indicating the percentage of the index that must be
     * covered by a search query in order for the query to be reported as a success. This parameter can be useful for
     * ensuring search availability even for services with only one replica. The default is 100.
     *
     * @return the minimumCoverage value.
     */
    public Double getMinimumCoverage() {
        return this.minimumCoverage;
    }

    /**
     * Set the minimumCoverage property: A number between 0 and 100 indicating the percentage of the index that must be
     * covered by a search query in order for the query to be reported as a success. This parameter can be useful for
     * ensuring search availability even for services with only one replica. The default is 100.
     *
     * @param minimumCoverage the minimumCoverage value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setMinimumCoverage(Double minimumCoverage) {
        this.minimumCoverage = minimumCoverage;
        return this;
    }

    /**
     * Get the orderBy property: The list of OData $orderby expressions by which to sort the results. Each expression
     * can be either a field name or a call to either the geo.distance() or the search.score() functions. Each
     * expression can be followed by asc to indicate ascending, and desc to indicate descending. The default is
     * ascending order. Ties will be broken by the match scores of documents. If no OrderBy is specified, the default
     * sort order is descending by document match score. There can be at most 32 $orderby clauses.
     *
     * @return the orderBy value.
     */
    public List<String> getOrderBy() {
        return this.orderBy;
    }

    /**
     * Set the orderBy property: The list of OData $orderby expressions by which to sort the results. Each expression
     * can be either a field name or a call to either the geo.distance() or the search.score() functions. Each
     * expression can be followed by asc to indicate ascending, and desc to indicate descending. The default is
     * ascending order. Ties will be broken by the match scores of documents. If no OrderBy is specified, the default
     * sort order is descending by document match score. There can be at most 32 $orderby clauses.
     *
     * @param orderBy the orderBy value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setOrderBy(String... orderBy) {
        this.orderBy = (orderBy == null) ? null : java.util.Arrays.asList(orderBy);
        return this;
    }

    /**
     * Get the queryType property: A value that specifies the syntax of the search query. The default is 'simple'. Use
     * 'full' if your query uses the Lucene query syntax.
     *
     * @return the queryType value.
     */
    public QueryType getQueryType() {
        return this.queryType;
    }

    /**
     * Set the queryType property: A value that specifies the syntax of the search query. The default is 'simple'. Use
     * 'full' if your query uses the Lucene query syntax.
     *
     * @param queryType the queryType value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setQueryType(QueryType queryType) {
        this.queryType = queryType;
        return this;
    }

    /**
     * Get the scoringParameters property: The list of parameter values to be used in scoring functions (for example,
     * referencePointParameter) using the format name-values. For example, if the scoring profile defines a function
     * with a parameter called 'mylocation' the parameter string would be "mylocation--122.2,44.8" (without the
     * quotes).
     *
     * @return the scoringParameters value.
     */
    public List<ScoringParameter> getScoringParameters() {
        return this.scoringParameters;
    }

    /**
     * Set the scoringParameters property: The list of parameter values to be used in scoring functions (for example,
     * referencePointParameter) using the format name-values. For example, if the scoring profile defines a function
     * with a parameter called 'mylocation' the parameter string would be "mylocation--122.2,44.8" (without the
     * quotes).
     *
     * @param scoringParameters the scoringParameters value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setScoringParameters(ScoringParameter... scoringParameters) {
        this.scoringParameters = (scoringParameters == null) ? null : Arrays.asList(scoringParameters);
        return this;
    }

    /**
     * Get the scoringProfile property: The name of a scoring profile to evaluate match scores for matching documents in
     * order to sort the results.
     *
     * @return the scoringProfile value.
     */
    public String getScoringProfile() {
        return this.scoringProfile;
    }

    /**
     * Set the scoringProfile property: The name of a scoring profile to evaluate match scores for matching documents in
     * order to sort the results.
     *
     * @param scoringProfile the scoringProfile value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setScoringProfile(String scoringProfile) {
        this.scoringProfile = scoringProfile;
        return this;
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSemanticConfigurationName(String semanticConfigurationName) {
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSemanticErrorHandling(SemanticErrorHandling semanticErrorHandling) {
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSemanticMaxWaitInMilliseconds(Integer semanticMaxWaitInMilliseconds) {
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setDebug(QueryDebugMode debug) {
        this.debug = debug;
        return this;
    }

    /**
     * Get the searchFields property: The list of field names to which to scope the full-text search. When using fielded
     * search (fieldName:searchExpression) in a full Lucene query, the field names of each fielded search expression
     * take precedence over any field names listed in this parameter.
     *
     * @return the searchFields value.
     */
    public List<String> getSearchFields() {
        return this.searchFields;
    }

    /**
     * Set the searchFields property: The list of field names to which to scope the full-text search. When using fielded
     * search (fieldName:searchExpression) in a full Lucene query, the field names of each fielded search expression
     * take precedence over any field names listed in this parameter.
     *
     * @param searchFields the searchFields value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSearchFields(String... searchFields) {
        this.searchFields = (searchFields == null) ? null : java.util.Arrays.asList(searchFields);
        return this;
    }

    /**
     * Get the queryLanguage property: The language of the query.
     *
     * @return the queryLanguage value.
     */
    public QueryLanguage getQueryLanguage() {
        return this.queryLanguage;
    }

    /**
     * Set the queryLanguage property: The language of the query.
     *
     * @param queryLanguage the queryLanguage value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setQueryLanguage(QueryLanguage queryLanguage) {
        this.queryLanguage = queryLanguage;
        return this;
    }

    /**
     * Get the speller property: Improve search recall by spell-correcting individual search query terms.
     *
     * @return the speller value.
     */
    public QuerySpellerType getSpeller() {
        return this.speller;
    }

    /**
     * Set the speller property: Improve search recall by spell-correcting individual search query terms.
     *
     * @param speller the speller value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSpeller(QuerySpellerType speller) {
        this.speller = speller;
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setQueryAnswer(QueryAnswerType answers) {
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setAnswersCount(Integer answersCount) {
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setAnswerThreshold(Double answerThreshold) {
        this.answerThreshold = answerThreshold;
        return this;
    }

    /**
     * Get the searchMode property: A value that specifies whether any or all of the search terms must be matched in
     * order to count the document as a match.
     *
     * @return the searchMode value.
     */
    public SearchMode getSearchMode() {
        return this.searchMode;
    }

    /**
     * Set the searchMode property: A value that specifies whether any or all of the search terms must be matched in
     * order to count the document as a match.
     *
     * @param searchMode the searchMode value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSearchMode(SearchMode searchMode) {
        this.searchMode = searchMode;
        return this;
    }

    /**
     * Get the scoringStatistics property: A value that specifies whether we want to calculate scoring statistics (such
     * as document frequency) globally for more consistent scoring, or locally, for lower latency.
     *
     * @return the scoringStatistics value.
     */
    public ScoringStatistics getScoringStatistics() {
        return this.scoringStatistics;
    }

    /**
     * Set the scoringStatistics property: A value that specifies whether we want to calculate scoring statistics (such
     * as document frequency) globally for more consistent scoring, or locally, for lower latency.
     *
     * @param scoringStatistics the scoringStatistics value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setScoringStatistics(ScoringStatistics scoringStatistics) {
        this.scoringStatistics = scoringStatistics;
        return this;
    }

    /**
     * Get the sessionId property: A value to be used to create a sticky session, which can help to get more consistent
     * results. As long as the same sessionId is used, a best-effort attempt will be made to target the same replica
     * set. Be wary that reusing the same sessionID values repeatedly can interfere with the load balancing of the
     * requests across replicas and adversely affect the performance of the search service. The value used as sessionId
     * cannot start with a '_' character.
     *
     * @return the sessionId value.
     */
    public String getSessionId() {
        return this.sessionId;
    }

    /**
     * Set the sessionId property: A value to be used to create a sticky session, which can help to get more consistent
     * results. As long as the same sessionId is used, a best-effort attempt will be made to target the same replica
     * set. Be wary that reusing the same sessionID values repeatedly can interfere with the load balancing of the
     * requests across replicas and adversely affect the performance of the search service. The value used as sessionId
     * cannot start with a '_' character.
     *
     * @param sessionId the sessionId value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Get the select property: The list of fields to retrieve. If unspecified, all fields marked as retrievable in the
     * schema are included.
     *
     * @return the select value.
     */
    public List<String> getSelect() {
        return this.select;
    }

    /**
     * Set the select property: The list of fields to retrieve. If unspecified, all fields marked as retrievable in the
     * schema are included.
     *
     * @param select the select value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSelect(String... select) {
        this.select = (select == null) ? null : java.util.Arrays.asList(select);
        return this;
    }

    /**
     * Get the skip property: The number of search results to skip. This value cannot be greater than 100,000. If you
     * need to scan documents in sequence, but cannot use $skip due to this limitation, consider using $orderby on a
     * totally-ordered key and $filter with a range query instead.
     *
     * @return the skip value.
     */
    public Integer getSkip() {
        return this.skip;
    }

    /**
     * Set the skip property: The number of search results to skip. This value cannot be greater than 100,000. If you
     * need to scan documents in sequence, but cannot use $skip due to this limitation, consider using $orderby on a
     * totally-ordered key and $filter with a range query instead.
     *
     * @param skip the skip value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Get the top property: The number of search results to retrieve. This can be used in conjunction with $skip to
     * implement client-side paging of search results. If results are truncated due to server-side paging, the response
     * will include a continuation token that can be used to issue another Search request for the next page of results.
     *
     * @return the top value.
     */
    public Integer getTop() {
        return this.top;
    }

    /**
     * Set the top property: The number of search results to retrieve. This can be used in conjunction with $skip to
     * implement client-side paging of search results. If results are truncated due to server-side paging, the response
     * will include a continuation token that can be used to issue another Search request for the next page of results.
     *
     * @param top the top value to set.
     * @return the SearchOptions object itself.
     */
    public SearchOptions setTop(Integer top) {
        this.top = top;
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setQueryCaption(QueryCaptionType queryCaption) {
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setQueryCaptionHighlightEnabled(Boolean queryCaptionHighlightEnabled) {
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
     * @return the SearchOptions object itself.
     */
    public SearchOptions setSemanticFields(List<String> semanticFields) {
        this.semanticFields = semanticFields;
        return this;
    }

    /**
     * Get the vector property: The query parameters for vector and hybrid search queries.
     *
     * @return the vector value.
     */
    public SearchQueryVector getVector() {
        return this.vector;
    }

    /**
     * Set the vector property: The query parameters for vector and hybrid search queries.
     *
     * @param vector the vector value to set.
     * @return the SearchRequest object itself.
     */
    public SearchOptions setVector(SearchQueryVector vector) {
        this.vector = vector;
        return this;
    }
}
