// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class FeatureFlagTracingTest {

    @Test
    public void usesPercentageFilter() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        tracing.updateFeatureFilterTelemetry("Percentage");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("PRCNT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Microsoft.Percentage");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("PRCNT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("PercentageFilter");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("PRCNT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Microsoft.PercentageFilter");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("PRCNT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());
    }

    @Test
    public void usesTimeWindowFilter() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        tracing.updateFeatureFilterTelemetry("TimeWindow");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("TIME", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Microsoft.TimeWindow");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("TIME", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("TimeWindowFilter");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("TIME", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Microsoft.TimeWindowFilter");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("TIME", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());
    }

    @Test
    public void usesTargetingFilter() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        tracing.updateFeatureFilterTelemetry("Targeting");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("TRGT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Microsoft.Targeting");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("TRGT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("TargetingFilter");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("TRGT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Microsoft.TargetingFilter");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("TRGT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());
    }

    @Test
    public void usesCustomFilter() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        tracing.updateFeatureFilterTelemetry("Random");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("CSTM", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Microsoft.Random");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("CSTM", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("ABTest");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("CSTM", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("TargingFilter");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("CSTM", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());
    }

    @Test
    public void usesMultipleFilters() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        assertFalse(tracing.usesAnyFilter());

        tracing.updateFeatureFilterTelemetry("Percentage");
        tracing.updateFeatureFilterTelemetry("TimeWindow");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("PRCNT+TIME", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Percentage");
        tracing.updateFeatureFilterTelemetry("Targeting");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("PRCNT+TRGT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());

        tracing.updateFeatureFilterTelemetry("Percentage");
        tracing.updateFeatureFilterTelemetry("Random");

        assertTrue(tracing.usesAnyFilter());
        assertEquals("CSTM+PRCNT", tracing.toString());
        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.toString());
    }

    @Test
    public void usesTelemetryFlag() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        assertEquals("", tracing.createFFFeaturesString());

        tracing.setUsesTelemetry();
        assertEquals("Telemetry", tracing.createFFFeaturesString());

        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.createFFFeaturesString());
    }

    @Test
    public void usesSeedFlag() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        assertEquals("", tracing.createFFFeaturesString());

        tracing.setUsesSeed();
        assertEquals("Seed", tracing.createFFFeaturesString());

        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.createFFFeaturesString());
    }

    @Test
    public void usesSeedAndTelemetry() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        tracing.setUsesSeed();
        tracing.setUsesTelemetry();
        assertEquals("Seed+Telemetry", tracing.createFFFeaturesString());

        tracing.resetFeatureFilterTelemetry();
        assertEquals("", tracing.createFFFeaturesString());
    }

    @Test
    public void maxVariantsTracking() {
        FeatureFlagTracing tracing = new FeatureFlagTracing();
        assertNull(tracing.getMaxVariants());

        tracing.updateMaxVariants(3);
        assertEquals(3, tracing.getMaxVariants());

        // Should only update if larger
        tracing.updateMaxVariants(2);
        assertEquals(3, tracing.getMaxVariants());

        tracing.updateMaxVariants(5);
        assertEquals(5, tracing.getMaxVariants());

        tracing.resetFeatureFilterTelemetry();
        assertNull(tracing.getMaxVariants());
    }

}
