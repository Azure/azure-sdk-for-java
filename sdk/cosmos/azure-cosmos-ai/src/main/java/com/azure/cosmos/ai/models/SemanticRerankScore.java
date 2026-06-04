// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.ai.models;

/**
 * Represents a single scored document in the semantic rerank result.
 */
public final class SemanticRerankScore {
    private int index;
    private String document;
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
     * Gets the document text (if returnDocuments was true in the request).
     *
     * @return the document text, or null if not included.
     */
    public String getDocument() {
        return document;
    }

    /**
     * Gets the semantic relevance score for this document.
     *
     * @return the relevance score.
     */
    public double getScore() {
        return score;
    }

    // Package-private setters used by InferenceResponseParser in the same package
    void setIndex(int index) {
        this.index = index;
    }

    void setDocument(String document) {
        this.document = document;
    }

    void setScore(double score) {
        this.score = score;
    }
}
