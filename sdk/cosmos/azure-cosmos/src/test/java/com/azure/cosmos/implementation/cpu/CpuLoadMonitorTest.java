// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CpuLoadMonitorTest {
    @Test(groups = "unit")
    public void noInstance() throws Exception {
        assertThat(ReflectionUtils.getCpuMonitorReferenceCounter().get()).isEqualTo(0);
        assertThat(ReflectionUtils.getCpuMonitorInstance()).isNull();
    }

    @Test(groups = "unit")
    public void multipleInstances() throws Exception {
        List<CpuMonitor> cpuMonitorList = new ArrayList<>();
        for ( int i = 0 ; i < 10; i++) {
            CpuMonitor cpuMonitor = CpuMonitor.initializeAndGet();
            cpuMonitorList.add(cpuMonitor);

            assertThat(ReflectionUtils.getCpuMonitorInstance()).isNotNull();
            assertThat(ReflectionUtils.getCpuMonitorReferenceCounter().get()).isEqualTo(cpuMonitorList.size());
            Thread.sleep(1000);
        }

        for ( int i = 0 ; i < 9; i++) {
            CpuMonitor cpuMonitor = cpuMonitorList.remove(0);
            cpuMonitor.close();

            assertThat(cpuMonitorList).hasSizeGreaterThan(0);

            assertThat(ReflectionUtils.getCpuMonitorReferenceCounter().get()).isEqualTo(cpuMonitorList.size());

            assertThat(ReflectionUtils.getCpuMonitorInstance()).isNotNull();
        }

        CpuMonitor cpuMonitor = cpuMonitorList.remove(0);
        cpuMonitor.close();
        assertThat(ReflectionUtils.getCpuMonitorReferenceCounter().get()).isEqualTo(cpuMonitorList.size());
        assertThat(ReflectionUtils.getCpuMonitorInstance()).isNull();
    }

    @Test(groups = "unit")
    public void multipleClose() throws Exception {
        assertThat(ReflectionUtils.getCpuMonitorReferenceCounter().get()).isEqualTo(0);
        assertThat(ReflectionUtils.getCpuMonitorInstance()).isNull();

        CpuMonitor cpuMonitor = CpuMonitor.initializeAndGet();

        cpuMonitor.close();
        assertThat(ReflectionUtils.getCpuMonitorReferenceCounter().get()).isEqualTo(0);
        assertThat(ReflectionUtils.getCpuMonitorInstance()).isNull();

        cpuMonitor.close();

        assertThat(ReflectionUtils.getCpuMonitorReferenceCounter().get()).isEqualTo(0);
        assertThat(ReflectionUtils.getCpuMonitorInstance()).isNull();
    }
}
