// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UtilsTest {

    @Test
    public void testUtils() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Utils.fromCpuString("3090ti"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Utils.fromCpuString("0.5m"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Utils.fromMemoryString("0.5Mb"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Utils.fromMemoryString("0.5GB"));

        Assertions.assertEquals(0.5, Utils.fromCpuString("500m"));
        Assertions.assertEquals(1.0, Utils.fromCpuString("1"));
        Assertions.assertEquals(1.5, Utils.fromCpuString("1500m"));
        Assertions.assertEquals(0.5, Utils.fromMemoryString("512Mi"));
        Assertions.assertEquals(1.0, Utils.fromMemoryString("1Gi"));

        Assertions.assertEquals("500m", Utils.toCpuString(0.5));
        Assertions.assertEquals("1", Utils.toCpuString(1));
        Assertions.assertEquals("512Mi", Utils.toMemoryString(0.5));
        Assertions.assertEquals("1Gi", Utils.toMemoryString(1));
    }
}
