// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation;

import com.azure.spring.cloud.feature.management.implementation.models.Feature;

import java.util.HashMap;
import java.util.Map;

public class FeatureManagementProperties extends HashMap<String, Object> {
    private static final long serialVersionUID = -1642032123104805346L;

    /**
     * Map of all Feature Flags that use Feature Filters.
     */
    protected transient Map<String, Feature> featureManagement;

    /**
     * Map of all Feature Flags that are just enabled/disabled.
     */
    protected Map<String, Boolean> onOff;

    public FeatureManagementProperties() {
        featureManagement = new HashMap<>();
        onOff = new HashMap<>();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        if (m == null) {
            return;
        }

        // Need to reset or switch between on/off to conditional doesn't work
        featureManagement = new HashMap<>();
        onOff = new HashMap<>();

        Map<? extends String, ? extends Object> features = removePrefixes(m, "featureManagement");
        if (!features.isEmpty()) {
            m = features;
        }
        for (String key : m.keySet()) {
            addToFeatures(m, key, "");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<? extends String, ? extends Object> removePrefixes(Map<? extends String, ? extends Object> m,
                                                                   String prefix) {
        Map<? extends String, ? extends Object> removedPrefix = new HashMap<>();
        if (m.containsKey(prefix)) {
            removedPrefix = (Map<? extends String, ? extends Object>) m.get(prefix);
        }
        return removedPrefix;
    }

    protected void addToFeatures(Map<? extends String, ? extends Object> features, String key, String combined) {

    }

    /**
     * @return the featureManagement
     */
    public Map<String, Feature> getFeatureManagement() {
        return featureManagement;
    }

    /**
     * @return the onOff
     */
    public Map<String, Boolean> getOnOff() {
        return onOff;
    }
}
