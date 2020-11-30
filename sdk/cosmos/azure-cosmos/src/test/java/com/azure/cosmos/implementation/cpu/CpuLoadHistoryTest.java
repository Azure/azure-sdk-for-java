// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

public class CpuLoadHistoryTest {
    @Test(groups = "unit", expectedExceptions = NullPointerException.class)
    public void nullHistory() throws Exception {
        new CpuLoadHistory(null, Duration.ofSeconds(1));
    }

    @Test(groups = "unit")
    public void empty() throws Exception {
        CpuLoadHistory history = new CpuLoadHistory(new ArrayList<>(), Duration.ofSeconds(1));
        assertThat(history.toString()).isEqualTo("empty");
    }

    @Test(groups = "unit")
    public void one() throws Exception {
        CpuLoadHistory history = new CpuLoadHistory(ImmutableList.of(
            getCpuLoad("2020-09-09T00:34:19.863174Z", 30)), Duration.ofSeconds(1));
        assertThat(history.toString()).isEqualTo("(2020-09-09T00:34:19.863174Z 30.0%)");
    }

    @Test(groups = "unit")
    public void multiple() throws Exception {
        CpuLoadHistory history = new CpuLoadHistory(
            ImmutableList.of(getCpuLoad("2020-09-09T00:34:19.863174Z", 30),
                getCpuLoad("2020-09-09T00:40:19.863174Z", 20)), Duration.ofSeconds(1));
        assertThat(history.toString())
            .isEqualTo("(2020-09-09T00:34:19.863174Z 30.0%), (2020-09-09T00:40:19.863174Z 20.0%)");
    }

    private CpuLoad getCpuLoad(String instant, double cpu) {
        return new CpuLoad(Instant.parse(instant), (float) cpu);
    }
}
