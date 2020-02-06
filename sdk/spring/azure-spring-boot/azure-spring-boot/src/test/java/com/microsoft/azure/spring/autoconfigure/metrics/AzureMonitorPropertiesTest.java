/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.metrics;

import io.micrometer.azuremonitor.AzureMonitorConfig;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import org.junit.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.export.properties.StepRegistryProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AzureMonitorProperties}.
 *
 * @author Dhaval Doshi
 */

public class AzureMonitorPropertiesTest {

    @SuppressWarnings("depreciation")
    private void assertStepRegistryDefaultValues(StepRegistryProperties properties,
                                                 StepRegistryConfig config) {

        assertThat(properties.getStep()).isEqualTo(config.step());
        assertThat(properties.isEnabled()).isEqualTo(config.enabled());
        assertThat(properties.getConnectTimeout()).isEqualTo(config.connectTimeout());
        assertThat(properties.getReadTimeout()).isEqualTo(config.readTimeout());
        assertThat(properties.getNumThreads()).isEqualTo(config.numThreads());
        assertThat(properties.getBatchSize()).isEqualTo(config.batchSize());
    }

    @Test
    public void defaultValuesAreConsistent() {
        final AzureMonitorProperties properties = new AzureMonitorProperties();
        final AzureMonitorConfig config = (key) -> null;
        assertStepRegistryDefaultValues(properties, config);
    }

}
