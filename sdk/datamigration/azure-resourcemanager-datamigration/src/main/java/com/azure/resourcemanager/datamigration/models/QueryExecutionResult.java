// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datamigration.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Describes query analysis results for execution in source and target. */
@Immutable
public final class QueryExecutionResult {
    /*
     * Query text retrieved from the source server
     */
    @JsonProperty(value = "queryText", access = JsonProperty.Access.WRITE_ONLY)
    private String queryText;

    /*
     * Total no. of statements in the batch
     */
    @JsonProperty(value = "statementsInBatch", access = JsonProperty.Access.WRITE_ONLY)
    private Long statementsInBatch;

    /*
     * Query analysis result from the source
     */
    @JsonProperty(value = "sourceResult", access = JsonProperty.Access.WRITE_ONLY)
    private ExecutionStatistics sourceResult;

    /*
     * Query analysis result from the target
     */
    @JsonProperty(value = "targetResult", access = JsonProperty.Access.WRITE_ONLY)
    private ExecutionStatistics targetResult;

    /** Creates an instance of QueryExecutionResult class. */
    public QueryExecutionResult() {
    }

    /**
     * Get the queryText property: Query text retrieved from the source server.
     *
     * @return the queryText value.
     */
    public String queryText() {
        return this.queryText;
    }

    /**
     * Get the statementsInBatch property: Total no. of statements in the batch.
     *
     * @return the statementsInBatch value.
     */
    public Long statementsInBatch() {
        return this.statementsInBatch;
    }

    /**
     * Get the sourceResult property: Query analysis result from the source.
     *
     * @return the sourceResult value.
     */
    public ExecutionStatistics sourceResult() {
        return this.sourceResult;
    }

    /**
     * Get the targetResult property: Query analysis result from the target.
     *
     * @return the targetResult value.
     */
    public ExecutionStatistics targetResult() {
        return this.targetResult;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (sourceResult() != null) {
            sourceResult().validate();
        }
        if (targetResult() != null) {
            targetResult().validate();
        }
    }
}
