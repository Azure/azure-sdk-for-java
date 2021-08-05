// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class CpuLoadMonitorTest {
    @Test(groups = "unit")
    public void noInstance() throws Exception {
        assertThat(ReflectionUtils.getListeners()).hasSize(0);
        assertThat(ReflectionUtils.getFuture()).isNull();
    }

    @Test(groups = "unit")
    public void multipleInstances() throws Exception {
        List<CpuMemoryListener> cpuMonitorList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CpuMemoryListener listener = new CpuMemoryListener() {
            };
            CpuMemoryMonitor.register(listener);
            cpuMonitorList.add(listener);

            Future<?> workFuture = ReflectionUtils.getFuture();
            assertThat(workFuture).isNotNull();
            assertThat(workFuture.isCancelled()).isFalse();
            assertThat(ReflectionUtils.getListeners()).hasSize(cpuMonitorList.size());
            Thread.sleep(10);
        }

        for (int i = 0; i < 9; i++) {
            CpuMemoryListener cpuMemoryListener = cpuMonitorList.remove(0);
            assertThat(cpuMonitorList).hasSizeGreaterThan(0);

            CpuMemoryMonitor.unregister(cpuMemoryListener);

            assertThat(ReflectionUtils.getListeners()).hasSize(cpuMonitorList.size());

            Future<?> workFuture = ReflectionUtils.getFuture();
            assertThat(workFuture).isNotNull();
            assertThat(workFuture.isCancelled()).isFalse();
        }

        // register a new one here
        CpuMemoryListener newListener = new CpuMemoryListener() {
        };
        CpuMemoryMonitor.register(newListener);
        CpuMemoryMonitor.unregister(newListener);

        assertThat(ReflectionUtils.getListeners()).hasSize(cpuMonitorList.size());
        Future<?> workFuture = ReflectionUtils.getFuture();
        assertThat(workFuture).isNotNull();
        assertThat(workFuture.isCancelled()).isFalse();

        CpuMemoryListener cpuMemoryListener = cpuMonitorList.remove(0);
        CpuMemoryMonitor.unregister(cpuMemoryListener);

        assertThat(ReflectionUtils.getListeners()).hasSize(cpuMonitorList.size());

        workFuture = ReflectionUtils.getFuture();
        assertThat(workFuture).isNull();
    }

    @Test(groups = "unit")
    public void handleLeak() throws Throwable {
        TestMemoryListener listener = new TestMemoryListener();
        CpuMemoryMonitor.register(listener);
        listener.finalize();
        listener = null;
        System.gc();
        Thread.sleep(10000);

        assertThat(ReflectionUtils.getListeners()).hasSize(0);
        assertThat(ReflectionUtils.getFuture()).isNull();
    }

    class TestMemoryListener implements CpuMemoryListener {
        @Override
        public void finalize() throws Throwable {
            super.finalize();
        }
    }
}
