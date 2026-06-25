// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureMonitorExporterBuilderTest {

    @Test
    public void customerSdkStatsEnabledByDefault() {
        ConfigProperties config = DefaultConfigProperties.createFromMap(Collections.emptyMap());
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isTrue();
    }

    @Test
    public void customerSdkStatsDisabledByPublicProperty() {
        ConfigProperties config = DefaultConfigProperties
            .createFromMap(Collections.singletonMap("APPLICATIONINSIGHTS_SDKSTATS_DISABLED", "true"));
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isFalse();
    }

    @Test
    public void customerSdkStatsDisabledByAllProperty() {
        ConfigProperties config = DefaultConfigProperties
            .createFromMap(Collections.singletonMap("APPLICATIONINSIGHTS_SDKStats_DISABLED_ALL", "true"));
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isFalse();
    }

    @Test
    public void customerSdkStatsDisabledAllTakesPrecedence() {
        Map<String, String> props = new HashMap<>();
        props.put("APPLICATIONINSIGHTS_SDKSTATS_DISABLED", "false");
        props.put("APPLICATIONINSIGHTS_SDKStats_DISABLED_ALL", "true");
        ConfigProperties config = DefaultConfigProperties.createFromMap(props);
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isFalse();
    }

    @Test
    public void customerSdkStatsDisabledAllFalseLeavesEnabled() {
        ConfigProperties config = DefaultConfigProperties
            .createFromMap(Collections.singletonMap("APPLICATIONINSIGHTS_SDKStats_DISABLED_ALL", "false"));
        assertThat(AzureMonitorExporterBuilder.isCustomerSdkStatsEnabled(config)).isTrue();
    }
}
