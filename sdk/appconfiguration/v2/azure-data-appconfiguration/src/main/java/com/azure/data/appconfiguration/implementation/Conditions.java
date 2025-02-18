// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.FeatureFlagFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Conditions represents the conditions of a feature flag or unknown user-defined condition.
 */
public class Conditions {
    // Unknown condition is a list of objects because we don't know what the condition user can put in portal or 'value'.
    private Map<String, Object> unknownConditions;

    // Only condition we know is a list of FeatureFlagFilter. It represents one kind of condition.
    private List<FeatureFlagFilter> featureFlagFilters;

    public Conditions() {
        unknownConditions = new LinkedHashMap<>();
        featureFlagFilters = new ArrayList<>();
    }

    public Map<String, Object> getUnknownConditions() {
        return unknownConditions;
    }

    public List<FeatureFlagFilter> getFeatureFlagFilters() {
        return featureFlagFilters;
    }

    public Conditions setFeatureFlagFilters(final List<FeatureFlagFilter> featureFlagFilters) {
        this.featureFlagFilters = featureFlagFilters;
        return this;
    }

    public Conditions setUnknownConditions(final Map<String, Object> unknownConditions) {
        this.unknownConditions = unknownConditions;
        return this;
    }
}
