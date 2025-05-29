// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import java.util.List;

/**
 * The document-level results for a {@link QueryType#SEMANTIC semantic} search.
 */
public final class SemanticSearchResult {
    /*
     * The relevance score computed by the semantic ranker for the top search results. Search results are sorted by the
     * RerankerScore first and then by the Score.
     */
    private final Double rerankerScore;

    /*
     * Captions are the most representative passages from the document relatively to the search query. They are often
     * used as document summary.
     */
    private final List<QueryCaptionResult> queryCaptions;

    /*
     * The relevance score computed by boosting the Reranker Score. Search results are sorted by the
     * RerankerScore/RerankerBoostedScore based on useScoringProfileBoostedRanking in the Semantic Config.
     * RerankerBoostedScore is only returned for queries of type 'semantic'
     */
    private final Double rerankerBoostedScore;

    SemanticSearchResult(Double rerankerScore, List<QueryCaptionResult> queryCaptions, Double rerankerBoostedScore) {
        this.rerankerBoostedScore = rerankerBoostedScore;
        this.rerankerScore = rerankerScore;
        this.queryCaptions = queryCaptions;
    }

    /**
     * Get the rerankerScore property: The relevance score computed by the semantic ranker for the top search results.
     * Search results are sorted by the RerankerScore first and then by the Score. RerankerScore is only returned for
     * queries of type 'semantic'.
     *
     * @return the rerankerScore value.
     */
    public Double getRerankerScore() {
        return this.rerankerScore;
    }

    /**
     * Get the queryCaptions property: Captions are the most representative passages from the document relatively to the
     * search query. They are often used as document summary. Captions are only returned for queries of type
     * 'semantic'.
     *
     * @return the captions value.
     */
    public List<QueryCaptionResult> getQueryCaptions() {
        return this.queryCaptions;
    }

    /**
     * Get the rerankerBoostedScore property: The relevance score computed by boosting the Reranker Score. Search
     * results are sorted by the RerankerScore/RerankerBoostedScore based on useScoringProfileBoostedRanking in the
     * Semantic Config. RerankerBoostedScore is only returned for queries of type 'semantic'.
     *
     * @return the rerankerBoostedScore value.
     */
    public Double getRerankerBoostedScore() {
        return this.rerankerBoostedScore;
    }

}
