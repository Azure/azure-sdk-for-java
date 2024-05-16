// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Filters in {@link FeatureFlagConfigurationSetting} that can be returned from GET queries. A filter is a rule
 * for evaluating the state of a feature flag.
 */
public final class FeatureFlagFilter {
    private final String name;
    private Map<String, Object> parameters;

    /**
     * The constructor for a feature flag configuration setting.
     *
     * @param name the name of this feature flag filter.
     */
    public FeatureFlagFilter(String name) {
        this.name = name;
        this.parameters = new HashMap<>();
    }

    /**
     * Get the name of this filter.
     *
     * @return the name of this filter
     */
    public String getName() {
        return name;
    }

    /**
     * Add a parameter to the list of parameters.
     *
     * @param key A key of the parameter.
     * @param value A value of the parameter.
     *
     * @return The updated {@link FeatureFlagFilter} object.
     */
    public FeatureFlagFilter addParameter(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    /**
     * Get the parameters of this filter.
     *
     * @return the parameters of this filter.
     */
    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    /**
     * Set the parameters of this filter.
     *
     * @param parameters the parameters of this filter. It is a key-value pair parameters.
     *
     * @return The updated {@link FeatureFlagFilter} object.
     */
    public FeatureFlagFilter setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
        return this;
    }
}
