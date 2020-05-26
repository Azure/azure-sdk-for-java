// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.TextWeights;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.TextWeights} and {@link TextWeights}.
 */
public final class TextWeightsConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.TextWeights} to {@link TextWeights}.
     */
    public static TextWeights map(com.azure.search.documents.implementation.models.TextWeights obj) {
        if (obj == null) {
            return null;
        }
        TextWeights textWeights = new TextWeights();

        if (obj.getWeights() != null) {
            Map<String, Double> weights =
                obj.getWeights().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            textWeights.setWeights(weights);
        }
        return textWeights;
    }

    /**
     * Maps from {@link TextWeights} to {@link com.azure.search.documents.implementation.models.TextWeights}.
     */
    public static com.azure.search.documents.implementation.models.TextWeights map(TextWeights obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.TextWeights textWeights =
            new com.azure.search.documents.implementation.models.TextWeights();

        if (obj.getWeights() != null) {
            Map<String, Double> weights =
                obj.getWeights().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            textWeights.setWeights(weights);
        }
        return textWeights;
    }

    private TextWeightsConverter() {
    }
}
