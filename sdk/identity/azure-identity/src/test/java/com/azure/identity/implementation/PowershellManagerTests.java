// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PowershellManagerTests {

    @Test
    public void testGetCommandLine() {
        PowershellManager manager = new PowershellManager(false);
        String[] actual = manager.getCommandLine("Write-Output 'Hello World'");
        assertTrue(Arrays.stream(actual).anyMatch(s -> s.contains("-NoProfile")));
    }
}
