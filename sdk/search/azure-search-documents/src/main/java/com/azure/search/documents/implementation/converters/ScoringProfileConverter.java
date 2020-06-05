// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ScoringFunction;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.TextWeights;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ScoringProfile} and
 * {@link ScoringProfile}.
 */
public final class ScoringProfileConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ScoringProfile} to {@link ScoringProfile}.
     */
    public static ScoringProfile map(com.azure.search.documents.indexes.implementation.models.ScoringProfile obj) {
        if (obj == null) {
            return null;
        }
        ScoringProfile scoringProfile = new ScoringProfile();

        if (obj.getFunctions() != null) {
            List<ScoringFunction> functions =
                obj.getFunctions().stream().map(ScoringFunctionConverter::map).collect(Collectors.toList());
            scoringProfile.setFunctions(functions);
        }

        String name = obj.getName();
        scoringProfile.setName(name);

        if (obj.getTextWeights() != null) {
            TextWeights textWeights = TextWeightsConverter.map(obj.getTextWeights());
            scoringProfile.setTextWeights(textWeights);
        }

        if (obj.getFunctionAggregation() != null) {
            ScoringFunctionAggregation functionAggregation =
                ScoringFunctionAggregationConverter.map(obj.getFunctionAggregation());
            scoringProfile.setFunctionAggregation(functionAggregation);
        }
        return scoringProfile;
    }

    /**
     * Maps from {@link ScoringProfile} to {@link com.azure.search.documents.indexes.implementation.models.ScoringProfile}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ScoringProfile map(ScoringProfile obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.ScoringProfile scoringProfile =
            new com.azure.search.documents.indexes.implementation.models.ScoringProfile();

        if (obj.getFunctions() != null) {
            List<com.azure.search.documents.indexes.implementation.models.ScoringFunction> functions =
                obj.getFunctions().stream().map(ScoringFunctionConverter::map).collect(Collectors.toList());
            scoringProfile.setFunctions(functions);
        }

        String name = obj.getName();
        scoringProfile.setName(name);

        if (obj.getTextWeights() != null) {
            com.azure.search.documents.indexes.implementation.models.TextWeights textWeights =
                TextWeightsConverter.map(obj.getTextWeights());
            scoringProfile.setTextWeights(textWeights);
        }

        if (obj.getFunctionAggregation() != null) {
            com.azure.search.documents.indexes.implementation.models.ScoringFunctionAggregation functionAggregation =
                ScoringFunctionAggregationConverter.map(obj.getFunctionAggregation());
            scoringProfile.setFunctionAggregation(functionAggregation);
        }
        return scoringProfile;
    }

    private ScoringProfileConverter() {
    }
}
