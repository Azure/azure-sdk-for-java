// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CompositeIndexUtilizationEntity {

    @JsonProperty(value = "IndexSpecs", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> indexDocumentExpressions;
    @JsonProperty(value = "IndexPreciseSet", access = JsonProperty.Access.WRITE_ONLY)
    private boolean indexPlanFullFidelity;
    @JsonProperty(value = "IndexImpactScore", access = JsonProperty.Access.WRITE_ONLY)
    private String indexImpactScore;

    CompositeIndexUtilizationEntity() {
        super();
    }

    /**
     * @param indexDocumentExpressions -> The index representation of the filter expression.
     * @param indexPlanFullFidelity    -> The index plan full fidelity.
     * @param indexImpactScore         -> The index impact score.
     */
    CompositeIndexUtilizationEntity(List<String> indexDocumentExpressions, boolean indexPlanFullFidelity, String indexImpactScore) {
        this.indexDocumentExpressions = indexDocumentExpressions;
        this.indexPlanFullFidelity = indexPlanFullFidelity;
        this.indexImpactScore = indexImpactScore;
    }

    /**
     * @return indexDocumentExpressions
     */
    public List<String> getIndexDocumentExpressions() {
        return indexDocumentExpressions;
    }

    /**
     * @return indexPlanFullFidelity
     */
    public boolean isIndexPlanFullFidelity() {
        return indexPlanFullFidelity;
    }

    /**
     * @return indexImpactScore
     */
    public String getIndexImpactScore() {
        return indexImpactScore;
    }

    /**
     * @param indexDocumentExpressions -> The index representation of the filter expression.
     */
    public void setIndexDocumentExpressions(List<String> indexDocumentExpressions) {
        this.indexDocumentExpressions = indexDocumentExpressions;
    }

    /**
     * @param indexPlanFullFidelity -> The index plan full fidelity.
     */
    public void setIndexPlanFullFidelity(boolean indexPlanFullFidelity) {
        this.indexPlanFullFidelity = indexPlanFullFidelity;
    }

    /**
     * @param indexImpactScore -> The index impact score.
     */
    public void setIndexImpactScore(String indexImpactScore) {
        this.indexImpactScore = indexImpactScore;
    }
}
