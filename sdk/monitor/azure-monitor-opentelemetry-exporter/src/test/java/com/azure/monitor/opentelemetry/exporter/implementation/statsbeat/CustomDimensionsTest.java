// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.builders.StatsbeatTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MetricsData;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.PropertyHelper;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.SystemInformation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomDimensionsTest {

    @Test
    public void testResourceProvider() {
        CustomDimensions customDimensions = new CustomDimensions();

        assertThat(customDimensions.getResourceProvider()).isEqualTo(ResourceProvider.UNKNOWN);
    }

    @Test
    public void testOperatingSystem() {
        CustomDimensions customDimensions = new CustomDimensions();

        OperatingSystem os = OperatingSystem.OS_UNKNOWN;
        if (SystemInformation.isWindows()) {
            os = OperatingSystem.OS_WINDOWS;
        } else if (SystemInformation.isLinux()) {
            os = OperatingSystem.OS_LINUX;
        }
        assertThat(customDimensions.getOperatingSystem()).isEqualTo(os);
    }

    @Test
    public void testCustomerIkey() {
        CustomDimensions customDimensions = new CustomDimensions();

        StatsbeatTelemetryBuilder telemetryBuilder = StatsbeatTelemetryBuilder.create("test", 1);
        customDimensions.populateProperties(telemetryBuilder, null);
        MetricsData data = (MetricsData) telemetryBuilder.build().getData().getBaseData();
        assertThat(data.getProperties()).doesNotContainKey("cikey");
    }

    @Test
    public void testVersion() {
        CustomDimensions customDimensions = new CustomDimensions();

        StatsbeatTelemetryBuilder telemetryBuilder = StatsbeatTelemetryBuilder.create("test", 1);
        customDimensions.populateProperties(telemetryBuilder, null);

        MetricsData data = (MetricsData) telemetryBuilder.build().getData().getBaseData();
        assertThat(data.getProperties()).containsKeys("version");
    }

    @Test
    public void testRuntimeVersion() {
        CustomDimensions customDimensions = new CustomDimensions();

        StatsbeatTelemetryBuilder telemetryBuilder = StatsbeatTelemetryBuilder.create("test", 1);
        customDimensions.populateProperties(telemetryBuilder, null);

        MetricsData data = (MetricsData) telemetryBuilder.build().getData().getBaseData();
        assertThat(data.getProperties().get("runtimeVersion")).isEqualTo(System.getProperty("java.version"));
    }
}
