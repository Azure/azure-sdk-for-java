// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class VersionGeneratorTest {

    @Test
    public void sdkVersionNumberIsAvailable() {
        assertThat(VersionGenerator.getSdkVersionNumber()).isNotEqualTo("unknown");
        assertThat(VersionGenerator.getSdkVersion()).endsWith(VersionGenerator.getSdkVersionNumber());
    }
}
