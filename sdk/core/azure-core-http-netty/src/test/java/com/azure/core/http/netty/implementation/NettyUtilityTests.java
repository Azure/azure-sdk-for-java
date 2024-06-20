// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class NettyUtilityTests {
    @Test
    public void validateNettyVersions() {
        NettyUtility.NettyVersionLogInformation logInformation = NettyUtility.createNettyVersionLogInformation();

        // Should never have version mismatches when running tests, that would mean either the version properties are
        // wrong or there is a dependency diamond within azure-core-http-netty. Either way, it should be fixed.
        assertFalse(logInformation.shouldLog());
    }
}
