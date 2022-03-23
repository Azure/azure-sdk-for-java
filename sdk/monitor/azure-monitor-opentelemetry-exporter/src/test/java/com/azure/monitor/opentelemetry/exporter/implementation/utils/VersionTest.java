package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionTest {
    @Test
    public void testArtifactName() {
        assertEquals("azure-monitor-opentelemetry-exporter", VersionGenerator.getArtifactName());
    }

    @Test
    public void testArtifactVersion() {
        assertTrue(VersionGenerator.getArtifactVersion().matches("[0-9].[0-9].[0-9].*"));
    }

    @Test
    public void testSdkVersion() {
        // OpenTelemetry added version.properties files in 1.3.0
        // but testing against OpenTelemetry 1.0.0, so it's unknown in this test
        assertTrue(VersionGenerator.getSdkVersion().matches("java[0-9._]+:ot([0-9.]+|unknown):ext[0-9.]+.*"));
    }
}
