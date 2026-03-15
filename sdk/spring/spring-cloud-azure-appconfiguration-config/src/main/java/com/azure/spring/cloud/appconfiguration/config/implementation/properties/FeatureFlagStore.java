// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

/**
 * Configuration properties for loading feature flags from an Azure App
 * Configuration store.
 */
public final class FeatureFlagStore {

    /**
     * Enables or disables feature flag loading from the store.
     */
    private Boolean enabled = false;

    private List<FeatureFlagKeyValueSelector> selects = new ArrayList<>();

    /**
     * Returns whether feature flag loading is enabled.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets whether feature flag loading is enabled.
     *
     * @param enabled {@code true} to enable, {@code false} to disable
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the feature flag selectors.
     *
     * @return the list of {@link FeatureFlagKeyValueSelector} instances
     */
    public List<FeatureFlagKeyValueSelector> getSelects() {
        return selects;
    }

    /**
     * Sets the feature flag selectors.
     *
     * @param selects the list of {@link FeatureFlagKeyValueSelector} instances
     */
    public void setSelects(List<FeatureFlagKeyValueSelector> selects) {
        this.selects = selects;
    }
    
    /**
     * Adds a default selector when enabled with none configured, then
     * validates all selectors.
     */
    @PostConstruct
    void validateAndInit() {
        if (enabled && selects.size() == 0) {
            selects.add(new FeatureFlagKeyValueSelector());
        }
        selects.forEach(FeatureFlagKeyValueSelector::validateAndInit);
    }

}
