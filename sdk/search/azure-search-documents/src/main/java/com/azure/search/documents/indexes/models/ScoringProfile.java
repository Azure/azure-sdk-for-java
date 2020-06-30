// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Defines parameters for a search index that influence scoring in search
 * queries.
 */
@Fluent
public final class ScoringProfile {
    /*
     * The name of the scoring profile.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /*
     * Parameters that boost scoring based on text matches in certain index
     * fields.
     */
    @JsonProperty(value = "text")
    private TextWeights textWeights;

    /*
     * The collection of functions that influence the scoring of documents.
     */
    @JsonProperty(value = "functions")
    private List<ScoringFunction> functions;

    /*
     * A value indicating how the results of individual scoring functions
     * should be combined. Defaults to "Sum". Ignored if there are no scoring
     * functions. Possible values include: 'Sum', 'Average', 'Minimum',
     * 'Maximum', 'FirstMatching'
     */
    @JsonProperty(value = "functionAggregation")
    private ScoringFunctionAggregation functionAggregation;

    /**
     * Constructor of {@link ScoringProfile}.
     *
     * @param name The name of the scoring profile.
     */
    @JsonCreator
    public ScoringProfile(@JsonProperty(value = "name", required = true) String name) {
        this.name = name;
    }

    /**
     * Get the name property: The name of the scoring profile.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the textWeights property: Parameters that boost scoring based on
     * text matches in certain index fields.
     *
     * @return the textWeights value.
     */
    public TextWeights getTextWeights() {
        return this.textWeights;
    }

    /**
     * Set the textWeights property: Parameters that boost scoring based on
     * text matches in certain index fields.
     *
     * @param textWeights the textWeights value to set.
     * @return the ScoringProfile object itself.
     */
    public ScoringProfile setTextWeights(TextWeights textWeights) {
        this.textWeights = textWeights;
        return this;
    }

    /**
     * Get the functions property: The collection of functions that influence
     * the scoring of documents.
     *
     * @return the functions value.
     */
    public List<ScoringFunction> getFunctions() {
        return this.functions;
    }

    /**
     * Set the functions property: The collection of functions that influence
     * the scoring of documents.
     *
     * @param functions the functions value to set.
     * @return the ScoringProfile object itself.
     */
    public ScoringProfile setFunctions(List<ScoringFunction> functions) {
        this.functions = functions;
        return this;
    }

    /**
     * Get the functionAggregation property: A value indicating how the results
     * of individual scoring functions should be combined. Defaults to "Sum".
     * Ignored if there are no scoring functions. Possible values include:
     * 'Sum', 'Average', 'Minimum', 'Maximum', 'FirstMatching'.
     *
     * @return the functionAggregation value.
     */
    public ScoringFunctionAggregation getFunctionAggregation() {
        return this.functionAggregation;
    }

    /**
     * Set the functionAggregation property: A value indicating how the results
     * of individual scoring functions should be combined. Defaults to "Sum".
     * Ignored if there are no scoring functions. Possible values include:
     * 'Sum', 'Average', 'Minimum', 'Maximum', 'FirstMatching'.
     *
     * @param functionAggregation the functionAggregation value to set.
     * @return the ScoringProfile object itself.
     */
    public ScoringProfile setFunctionAggregation(ScoringFunctionAggregation functionAggregation) {
        this.functionAggregation = functionAggregation;
        return this;
    }
}
