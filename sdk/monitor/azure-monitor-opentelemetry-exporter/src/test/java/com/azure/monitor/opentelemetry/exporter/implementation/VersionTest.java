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
    void testGetQualifiedSDKVersionString() {
        assertThat(Version.getSdkVersion()).matches("java[1-9.]+:ot.*:ext[1-9.]+.*");
    }
}
