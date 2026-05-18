// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.util.Beta;

/**
 * Represents a single scored document in the semantic rerank result.
 */
@Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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

    private void setIndex(int index) {
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

    private void setDocument(String document) {
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

    private void setScore(double score) {
        this.score = score;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.SemanticRerankScoreHelper.setSemanticRerankScoreAccessor(
            new ImplementationBridgeHelpers.SemanticRerankScoreHelper.SemanticRerankScoreAccessor() {
                @Override
                public void setIndex(SemanticRerankScore score, int index) {
                    score.setIndex(index);
                }

                @Override
                public void setScore(SemanticRerankScore score, double scoreValue) {
                    score.setScore(scoreValue);
                }

                @Override
                public void setDocument(SemanticRerankScore score, String document) {
                    score.setDocument(document);
                }
            }
        );
    }

    static { initialize(); }
}
