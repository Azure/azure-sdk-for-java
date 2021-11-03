package com.azure.monitor.opentelemetry.exporter;

import com.azure.monitor.opentelemetry.exporter.utils.PropertyHelper;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertyHelperTest {
    @Test
    void testGetSDKVersionNumber() {
        assertThat(PropertyHelper.getSdkVersionNumber()).matches("[0-9].[0-9].[0-9].*");
    }

    @Test
    void testGetQualifiedSDKVersionString() {
        assertThat(PropertyHelper.getQualifiedSdkVersionString()).matches("java[0-9].?[0-9]:ot.*:ext.*");
    }
}
