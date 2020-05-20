package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.TextWeights;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.TextWeights} and {@link TextWeights}.
 */
public final class TextWeightsConverter {
    private static final ClientLogger LOGGER = new ClientLogger(TextWeightsConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.TextWeights} to {@link TextWeights}.
     */
    public static TextWeights map(com.azure.search.documents.implementation.models.TextWeights obj) {
        if (obj == null) {
            return null;
        }
        TextWeights textWeights = new TextWeights();

        if (obj.getWeights() != null) {
            Map<String, Double> _weights =
                obj.getWeights().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            textWeights.setWeights(_weights);
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
            Map<String, Double> _weights =
                obj.getWeights().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
            textWeights.setWeights(_weights);
        }
        return textWeights;
    }
}
