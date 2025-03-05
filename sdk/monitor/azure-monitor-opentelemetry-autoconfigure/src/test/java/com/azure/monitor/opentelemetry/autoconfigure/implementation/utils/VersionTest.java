// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionTest {
    @Test
    public void testSdkVersion() {
        // OpenTelemetry added version.properties files in 1.3.0
        // but testing against OpenTelemetry 1.0.0, so it's unknown in this test
        assertTrue(VersionGenerator.getSdkVersion().matches(".._java[0-9._]+:otel([0-9.]+|unknown):ext[0-9.]+.*"));
    }
}
