// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerSdkStatsTelemetryTypeTest {

    @Test
    public void testRequestMapping() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("Request")).isEqualTo("REQUEST");
    }

    @Test
    public void testDependencyMapping() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("RemoteDependency")).isEqualTo("DEPENDENCY");
    }

    @Test
    public void testTraceMapping() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("Message")).isEqualTo("TRACE");
    }

    @Test
    public void testExceptionMapping() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("Exception")).isEqualTo("EXCEPTION");
    }

    @Test
    public void testMetricMapping() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("Metric")).isEqualTo("CUSTOM_METRIC");
    }

    @Test
    public void testEventMapping() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("Event")).isEqualTo("CUSTOM_EVENT");
    }

    @Test
    public void testPageViewMapping() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("PageView")).isEqualTo("PAGE_VIEW");
    }

    @Test
    public void testAvailabilityMapping() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("Availability")).isEqualTo("AVAILABILITY");
    }

    @Test
    public void testStatsbeatReturnsNull() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("Statsbeat")).isNull();
    }

    @Test
    public void testUnknownTypeReturnsNull() {
        assertThat(CustomerSdkStatsTelemetryType.fromTelemetryItemName("UnknownType")).isNull();
    }
}
