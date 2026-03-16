// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single scored document in the semantic rerank result.
 */
@Beta(value = Beta.SinceVersion.V4_78_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class SemanticRerankScore {
    @JsonProperty("index")
    private int index;

    @JsonProperty("document")
    private String document;

    @JsonProperty("score")
    private double score;

    /**
     * Creates a new instance of SemanticRerankScore.
     */
    public SemanticRerankScore() {
    }

    /**
     * Gets the index of the document in the original input list.
     *
     * @return the document index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index of the document in the original input list.
     *
     * @param index the document index.
     */
    void setIndex(int index) {
        this.index = index;
    }

    /**
     * Gets the document text (if returnDocuments was true in the request).
     *
     * @return the document text, or null if not included.
     */
    public String getDocument() {
        return document;
    }

    /**
     * Sets the document text.
     *
     * @param document the document text.
     */
    void setDocument(String document) {
        this.document = document;
    }

    /**
     * Gets the semantic relevance score for this document.
     *
     * @return the relevance score.
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the semantic relevance score for this document.
     *
     * @param score the relevance score.
     */
    void setScore(double score) {
        this.score = score;
    }
}
