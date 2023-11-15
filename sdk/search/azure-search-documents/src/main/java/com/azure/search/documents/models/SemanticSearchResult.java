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
     * Contains debugging information that can be used to further explore your search results.
     */
    private final List<DocumentDebugInfo> documentDebugInfo;

    SemanticSearchResult(Double rerankerScore, List<QueryCaptionResult> queryCaptions,
        List<DocumentDebugInfo> documentDebugInfo) {
        this.rerankerScore = rerankerScore;
        this.queryCaptions = queryCaptions;
        this.documentDebugInfo = documentDebugInfo;
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
     * Get the documentDebugInfo property: Contains debugging information that can be used to further explore your
     * search results.
     *
     * @return the documentDebugInfo value.
     */
    public List<DocumentDebugInfo> getDocumentDebugInfo() {
        return this.documentDebugInfo;
    }
}
