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
        List<CpuListener> cpuMonitorList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CpuListener listener = new CpuListener() {
            };
            CpuMonitor.register(listener);
            cpuMonitorList.add(listener);

            Future<?> workFuture = ReflectionUtils.getFuture();
            assertThat(workFuture).isNotNull();
            assertThat(workFuture.isCancelled()).isFalse();
            assertThat(ReflectionUtils.getListeners()).hasSize(cpuMonitorList.size());
            Thread.sleep(10);
        }

        for (int i = 0; i < 9; i++) {
            CpuListener cpuListener = cpuMonitorList.remove(0);
            assertThat(cpuMonitorList).hasSizeGreaterThan(0);

            CpuMonitor.unregister(cpuListener);

            assertThat(ReflectionUtils.getListeners()).hasSize(cpuMonitorList.size());

            Future<?> workFuture = ReflectionUtils.getFuture();
            assertThat(workFuture).isNotNull();
            assertThat(workFuture.isCancelled()).isFalse();
        }

        // register a new one here
        CpuListener newListener = new CpuListener() {
        };
        CpuMonitor.register(newListener);
        CpuMonitor.unregister(newListener);

        assertThat(ReflectionUtils.getListeners()).hasSize(cpuMonitorList.size());
        Future<?> workFuture = ReflectionUtils.getFuture();
        assertThat(workFuture).isNotNull();
        assertThat(workFuture.isCancelled()).isFalse();

        CpuListener cpuListener = cpuMonitorList.remove(0);
        CpuMonitor.unregister(cpuListener);

        assertThat(ReflectionUtils.getListeners()).hasSize(cpuMonitorList.size());

        workFuture = ReflectionUtils.getFuture();
        assertThat(workFuture).isNull();
    }

    @Test(groups = "unit")
    public void handleLeak() throws Throwable {
        TestListener listener = new TestListener();
        CpuMonitor.register(listener);
        listener.finalize();
        listener = null;
        System.gc();
        Thread.sleep(10000);

        assertThat(ReflectionUtils.getListeners()).hasSize(0);
        assertThat(ReflectionUtils.getFuture()).isNull();
    }

    class TestListener implements CpuListener {
        @Override
        public void finalize() throws Throwable {
            super.finalize();
        }
    }
}
