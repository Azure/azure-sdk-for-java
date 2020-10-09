// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CpuReaderTest {
    @Test(groups = "unit")
    public void range() {
        double cpuLoad = new CpuMemoryReader().getSystemWideCpuUsage();

        if (!Double.isNaN(cpuLoad)) {
            assertThat(cpuLoad).isBetween(0.0, 1.0);
        }
    }
}
