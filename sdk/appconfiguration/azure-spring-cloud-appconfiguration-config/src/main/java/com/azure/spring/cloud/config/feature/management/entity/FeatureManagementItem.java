// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.feature.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Feature Flag, used for converting from Azure App Configuration format to Client format.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureManagementItem {

    private boolean enabled;

    private ConditionsItem conditions;

    /**
     * @return the enabled
     */
    public boolean getEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the conditions
     */
    public ConditionsItem getConditions() {
        return conditions;
    }

    /**
     * @param conditions the conditions to set
     */
    public void setConditions(ConditionsItem conditions) {
        this.conditions = conditions;
    }
}
