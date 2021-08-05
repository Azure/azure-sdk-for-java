// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class CpuLoadTest {

    @DataProvider(name = "params")
    public Object[][] params() {
        return new Object[][] {
            { 0, "0.0" },
            { 10.0, "10.0" },
            { 31.23, "31.2" },
            { 100, "100.0" },
        };
    }

    @Test(groups = "unit", dataProvider = "params")
    public void cpuLoad(double cpu, String expectedCpu) throws Exception {
        String instant = "2020-09-09T00:34:19.863174Z";
        CpuLoad cpuLoad = new CpuLoad(Instant.parse(instant), (float) cpu);

        assertThat(cpuLoad.toString()).isEqualTo("(2020-09-09T00:34:19.863174Z " + expectedCpu + "%)");
    }
}
