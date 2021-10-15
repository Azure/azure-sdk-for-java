// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import static com.azure.spring.cloud.config.AppConfigurationConstants.EMPTY_LABEL;

/**
 * Properties for what needs to be requested from Azure App Configuration for Feature Flags.
 */
public final class FeatureFlagStore {

    private static final String KEY_FILTER = ".appconfig*";

    private Boolean enabled = false;

    private String labelFilter = EMPTY_LABEL;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyFilter() {
        return KEY_FILTER;
    }

    public String getLabelFilter() {
        return labelFilter;
    }

    public void setLabelFilter(String labelFilter) {
        this.labelFilter = labelFilter;
    }

}
