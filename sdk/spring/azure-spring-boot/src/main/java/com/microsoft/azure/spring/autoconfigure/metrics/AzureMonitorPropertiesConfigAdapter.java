/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.metrics;

import io.micrometer.azuremonitor.AzureMonitorConfig;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryPropertiesConfigAdapter;

/**
 * Adapter to convert {@link AzureMonitorProperties} to a {@link AzureMonitorConfig}.
 *
 * @author Dhaval Doshi
 */
public class AzureMonitorPropertiesConfigAdapter extends StepRegistryPropertiesConfigAdapter<AzureMonitorProperties>
        implements AzureMonitorConfig {

    AzureMonitorPropertiesConfigAdapter(AzureMonitorProperties properties) {
        super(properties);
    }

    @Override
    public String instrumentationKey() {
        return get(AzureMonitorProperties::getInstrumentationKey, AzureMonitorConfig.super::instrumentationKey);
    }
}
