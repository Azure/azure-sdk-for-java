// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

/**
 * Properties for what needs to be requested from Azure App Configuration for Feature Flags.
 */
public final class FeatureFlagStore {

    /**
     * Boolean for if feature flag loading is enabled.
     */
    private Boolean enabled = false;

    private List<FeatureFlagKeyValueSelector> selects = new ArrayList<>();

    /**
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the selects
     */
    public List<FeatureFlagKeyValueSelector> getSelects() {
        return selects;
    }

    /**
     * @param selects the selects to set
     */
    public void setSelects(List<FeatureFlagKeyValueSelector> selects) {
        this.selects = selects;
    }
    
    @PostConstruct
    public void validateAndInit() {
        if (enabled && selects.size() == 0) {
            selects.add(new FeatureFlagKeyValueSelector());
        }
        selects.forEach(FeatureFlagKeyValueSelector::validateAndInit);
    }

}
