package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.FeatureFlagFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Conditions represents the conditions of a feature flag or unknown user-defined condition.
 */
public class Conditions {
    // Unknown condition is a list of objects because we don't know what the condition user can put in portal or 'value'.
    private List<Object> unknownConditions;

    // Only condition we know is a list of FeatureFlagFilter. It represents one kind of condition.
    private List<FeatureFlagFilter> featureFlagFilters;

    public Conditions() {
        unknownConditions = new ArrayList<>();
        featureFlagFilters = new ArrayList<>();
    }

    public List<Object> getUnknownConditions() {
        return unknownConditions;
    }

    public List<FeatureFlagFilter> getFeatureFlagFilters() {
        return featureFlagFilters;
    }

    public Conditions setFeatureFlagFilters(final List<FeatureFlagFilter> featureFlagFilters) {
        this.featureFlagFilters = featureFlagFilters;
        return this;
    }

    public Conditions setUnknownConditions(final List<Object> unknownConditions) {
        this.unknownConditions = unknownConditions;
        return this;
    }
}
