// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = FeatureManagementConfigProperties.CONFIG_PREFIX)
public class FeatureManagementConfigProperties {

    public static final String CONFIG_PREFIX = "spring.cloud.azure.feature.management";

    private boolean failFast = true;

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }
}
