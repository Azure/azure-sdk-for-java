// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import static com.azure.spring.cloud.config.AppConfigurationConstants.EMPTY_LABEL;
import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_STORE_WATCH_KEY;

/**
 * Properties for what needs to be requested from Azure App Configuration for Feature Flags.
 */
public final class FeatureFlagStore {

    /**
     * Boolean for if feature flag loading is enabled.
     */
    private Boolean enabled = false;

    /**
     * App Configuration \0 empty label, when no label is set.
     */
    private String labelFilter = EMPTY_LABEL;

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
     * @return the keyFilter
     */
    public String getKeyFilter() {
        return FEATURE_STORE_WATCH_KEY;
    }

    /**
     * @return the labelFilter
     */
    public String getLabelFilter() {
        return labelFilter;
    }

    /**
     * @param labelFilter the labelFilter to set
     */
    public void setLabelFilter(String labelFilter) {
        this.labelFilter = labelFilter;
    }
}
