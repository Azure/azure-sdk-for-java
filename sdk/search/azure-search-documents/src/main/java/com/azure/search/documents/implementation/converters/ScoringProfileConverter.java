// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ScoringProfile;

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
        ScoringProfile scoringProfile = new ScoringProfile(obj.getName());

        if (obj.getFunctions() != null) {
            scoringProfile.setFunctions(obj.getFunctions().stream()
                .map(ScoringFunctionConverter::map)
                .collect(Collectors.toList()));
        }

        if (obj.getTextWeights() != null) {
            scoringProfile.setTextWeights(obj.getTextWeights());
        }

        if (obj.getFunctionAggregation() != null) {
            scoringProfile.setFunctionAggregation(obj.getFunctionAggregation());
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
            new com.azure.search.documents.indexes.implementation.models.ScoringProfile(obj.getName());

        if (obj.getFunctions() != null) {
            List<com.azure.search.documents.indexes.implementation.models.ScoringFunction> functions =
                obj.getFunctions().stream().map(ScoringFunctionConverter::map).collect(Collectors.toList());
            scoringProfile.setFunctions(functions);
        }

        if (obj.getTextWeights() != null) {
            scoringProfile.setTextWeights(obj.getTextWeights());
        }

        if (obj.getFunctionAggregation() != null) {
            scoringProfile.setFunctionAggregation(obj.getFunctionAggregation());
        }
        return scoringProfile;
    }

    private ScoringProfileConverter() {
    }
}
