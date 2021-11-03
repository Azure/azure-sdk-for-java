package com.azure.monitor.opentelemetry.exporter.implementation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionTest {
    @Test
    void testArtifactName() {
        assertThat(Version.getArtifactName()).isEqualTo("azure-monitor-opentelemetry-exporter");
    }

    @Test
    void testArtifactVersion() {
        assertThat(Version.getArtifactVersion()).matches("[0-9].[0-9].[0-9].*");
    }

    @Test
    void testSdkVersion() {
        // OpenTelemetry added version.properties files in 1.3.0
        // but testing against OpenTelemetry 1.0.0, so it's unknown in this test
        assertThat(Version.getSdkVersion()).matches("java[0-9.]+:ot([0-9.]+|unknown):ext[0-9.]+.*");
    }
}
