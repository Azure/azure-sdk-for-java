// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.cpu;

import org.mockito.Mockito;

public class CpuMonitorMock {
    public static CpuMemoryMonitor getMock() {
        return Mockito.mock(CpuMemoryMonitor.class);
    }
}
