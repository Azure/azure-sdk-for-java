// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SingleIndexUtilizationEntity {

    @JsonProperty(value = "FilterExpression", access = JsonProperty.Access.WRITE_ONLY)
    private String filterExpression;
    @JsonProperty(value = "IndexSpec", access = JsonProperty.Access.WRITE_ONLY)
    private String indexDocumentExpression;
    @JsonProperty(value = "FilterPreciseSet", access = JsonProperty.Access.WRITE_ONLY)
    private boolean filterExpressionPrecision;
    @JsonProperty(value = "IndexPreciseSet", access = JsonProperty.Access.WRITE_ONLY)
    private boolean indexPlanFullFidelity;
    @JsonProperty(value = "IndexImpactScore", access = JsonProperty.Access.WRITE_ONLY)
    private String indexImpactScore;

    SingleIndexUtilizationEntity() {}

    /**
     * @param filterExpression          -> The filter expression.
     * @param indexDocumentExpression   -> The index representation of the filter expression.
     * @param filterExpressionPrecision -> The precision flag of the filter expression.
     * @param indexPlanFullFidelity     -> The index plan full fidelity.
     * @param indexImpactScore          -> The index impact score.
     */
    SingleIndexUtilizationEntity(String filterExpression, String indexDocumentExpression, boolean filterExpressionPrecision, boolean indexPlanFullFidelity, String indexImpactScore) {
        this.filterExpression = filterExpression;
        this.indexDocumentExpression = indexDocumentExpression;
        this.filterExpressionPrecision = filterExpressionPrecision;
        this.indexPlanFullFidelity = indexPlanFullFidelity;
        this.indexImpactScore = indexImpactScore;
    }

    /**
     * @return filterExpression
     */
    public String getFilterExpression() {
        return filterExpression;
    }

    /**
     * @return indexDocumentExpression
     */
    public String getIndexDocumentExpression() {
        return indexDocumentExpression;
    }

    /**
     * @return filterExpressionPrecision
     */
    public boolean isFilterExpressionPrecision() {
        return filterExpressionPrecision;
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
     * @param filterExpression -> The filter expression.
     */
    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    /**
     * @param indexDocumentExpression -> The index representation of the filter expression.
     */
    public void setIndexDocumentExpression(String indexDocumentExpression) {
        this.indexDocumentExpression = indexDocumentExpression;
    }

    /**
     * @param filterExpressionPrecision -> The precision flag of the filter expression.
     */
    public void setFilterExpressionPrecision(boolean filterExpressionPrecision) {
        this.filterExpressionPrecision = filterExpressionPrecision;
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
