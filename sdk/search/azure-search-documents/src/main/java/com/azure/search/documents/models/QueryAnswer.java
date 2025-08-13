// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import java.util.Objects;

/**
 * Configuration for how semantic search returns answers to the search.
 */
public final class QueryAnswer {
    private final QueryAnswerType answerType;
    private Integer count;
    private Double threshold;

    /**
     * Creates a new instance of {@link QueryAnswer}.
     *
     * @param answerType The type of answers to generate.
     */
    public QueryAnswer(QueryAnswerType answerType) {
        this.answerType = Objects.requireNonNull(answerType, "'answerType' cannot be null.");
    }

    /**
     * Gets the type of answers to generate.
     *
     * @return The type of answers to generate.
     */
    public QueryAnswerType getAnswerType() {
        return answerType;
    }

    /**
     * Gets the number of answers to generate.
     * <p>
     * The number of answers to return is optional and will default to 1.
     * <p>
     * The value only takes effect when {@link #getAnswerType()} is {@link QueryAnswerType#EXTRACTIVE}.
     *
     * @return The number of answers to generate.
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Sets the number of answers to generate.
     * <p>
     * The number of answers to return is optional and will default to 1.
     * <p>
     * The value only takes effect when {@link #getAnswerType()} is {@link QueryAnswerType#EXTRACTIVE}.
     *
     * @param count The number of answers to generate.
     * @return The QueryAnswer object itself.
     */
    public QueryAnswer setCount(Integer count) {
        this.count = count;
        return this;
    }

    /**
     * Gets the confidence threshold an answer must match to be included as an answer to the query of answers.
     * <p>
     * The threshold is optional and will default to 0.7.
     * <p>
     * The value only takes effect when {@link #getAnswerType()} is {@link QueryAnswerType#EXTRACTIVE}.
     *
     * @return The confidence threshold an answer must match to be included as an answer to the query of answers.
     */
    public Double getThreshold() {
        return threshold;
    }

    /**
     * Sets the confidence threshold an answer must match to be included as an answer to the query of answers.
     * <p>
     * The threshold is optional and will default to 0.7.
     * <p>
     * The value only takes effect when {@link #getAnswerType()} is {@link QueryAnswerType#EXTRACTIVE}.
     *
     * @param threshold The confidence threshold an answer must match to be included as an answer to the query of
     * answers.
     * @return The QueryAnswer object itself.
     */
    public QueryAnswer setThreshold(Double threshold) {
        this.threshold = threshold;
        return this;
    }
}
