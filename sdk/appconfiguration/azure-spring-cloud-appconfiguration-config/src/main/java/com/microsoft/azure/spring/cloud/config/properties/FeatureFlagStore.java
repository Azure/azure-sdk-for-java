// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.properties;

import java.time.Duration;

public class FeatureFlagStore {
    
    private static final String EMPTY_LABEL = "\0";
    
    private static final String KEY_FILTER = ".appconfig*";

    private Boolean enabled = false;    

    private String labelFilter = EMPTY_LABEL;

    private Duration cacheExpiration = Duration.ofSeconds(30);

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

    public Duration getCacheExpiration() {
        return cacheExpiration;
    }

    public void setCacheExpiration(Duration cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }

}