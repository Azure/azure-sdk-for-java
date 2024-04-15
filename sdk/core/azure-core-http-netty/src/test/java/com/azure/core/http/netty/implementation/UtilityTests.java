// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilityTests {
    @Test
    public void validateNettyVersions() {
        StringBuilder logger = new StringBuilder();
        Utility.validateNettyVersions(logger::append);

        assertEquals(0, logger.length(), "Unexpected Netty version mismatch logs.");
    }
}
