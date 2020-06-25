// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Defines weights on index fields for which matches should boost scoring in
 * search queries.
 */
@Fluent
public final class TextWeights {
    /*
     * The dictionary of per-field weights to boost document scoring. The keys
     * are field names and the values are the weights for each field.
     */
    @JsonProperty(value = "weights", required = true)
    private Map<String, Double> weights;

    /**
     * Constructor of {@link TextWeights}.
     *
     * @param weights The dictionary of per-field weights to boost document scoring. The keys
     * are field names and the values are the weights for each field.
     */
    @JsonCreator
    public TextWeights(@JsonProperty(value = "weights", required = true) Map<String, Double> weights) {
        this.weights = weights;
    }

    /**
     * Get the weights property: The dictionary of per-field weights to boost
     * document scoring. The keys are field names and the values are the
     * weights for each field.
     *
     * @return the weights value.
     */
    public Map<String, Double> getWeights() {
        return this.weights;
    }
}
