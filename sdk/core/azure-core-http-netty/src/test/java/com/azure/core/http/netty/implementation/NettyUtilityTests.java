// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class NettyUtilityTests {
    @Test
    public void validateNettyVersions() {
        StringBuilder logger = new StringBuilder();
        NettyUtility.validateNettyVersions(logger::append);

        String logMessage = logger.toString();

        // Version information is always logged.
        assertFalse(logMessage.isEmpty(), "Version logs are always expected.");

        // But azure-core-http-netty shouldn't have version mismatches.
        assertFalse(logMessage.contains(NettyUtility.NETTY_VERSION_MISMATCH_LOG),
            "Unexpected Netty version mismatch logs.");
    }
}
