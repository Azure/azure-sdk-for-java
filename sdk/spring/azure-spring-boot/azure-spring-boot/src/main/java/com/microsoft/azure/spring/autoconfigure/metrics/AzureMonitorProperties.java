/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.metrics;

import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link ConfigurationProperties} for configuring Azure Application Insights metrics export.
 *
 * @author Dhaval Doshi
 */
@ConfigurationProperties(prefix = "management.metrics.export.azuremonitor")
public class AzureMonitorProperties extends StepRegistryProperties {
    private String instrumentationKey;

    public String getInstrumentationKey() {
        return this.instrumentationKey;
    }

    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }
}
