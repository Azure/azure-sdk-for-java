// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.azure.core.http.netty.implementation.NettyUtility.NETTY_TCNATIVE_VERSION_PROPERTY;
import static com.azure.core.http.netty.implementation.NettyUtility.NETTY_VERSION_PROPERTY;
import static com.azure.core.http.netty.implementation.NettyUtility.PROPERTIES_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NettyUtilityTests {
    @Test
    public void validateNettyVersionsWithWhatThePomSpecifies() {
        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        NettyUtility.NettyVersionLogInformation logInformation = NettyUtility.createNettyVersionLogInformation(
            pomVersions.get(NETTY_VERSION_PROPERTY), pomVersions.get(NETTY_TCNATIVE_VERSION_PROPERTY));

        // Should never have version mismatches when running tests, that would mean either the version properties are
        // wrong or there is a dependency diamond within azure-core-http-netty. Either way, it should be fixed.
        assertFalse(logInformation.shouldLog());
        for (String artifactFullName : logInformation.classpathNettyVersions.keySet()) {
            assertTrue(artifactFullName.startsWith("io.netty:netty-"),
                "All artifact information should start with 'io.netty:netty-'");
        }
        for (String artifactFullName : logInformation.classPathNativeNettyVersions.keySet()) {
            assertTrue(artifactFullName.startsWith("io.netty:netty-"),
                "All artifact information should start with 'io.netty:netty-'");
        }
    }

    @Test
    public void validateNettyVersionsWithJunkVersions() {
        NettyUtility.NettyVersionLogInformation logInformation
            = NettyUtility.createNettyVersionLogInformation("4.0.0.Final", "2.0.0.Final");

        // Junk versions used should flag for logging.
        assertTrue(logInformation.shouldLog());
        for (String artifactFullName : logInformation.classpathNettyVersions.keySet()) {
            assertTrue(artifactFullName.startsWith("io.netty:netty-"),
                "All artifact information should start with 'io.netty:netty-'");
        }
        for (String artifactFullName : logInformation.classPathNativeNettyVersions.keySet()) {
            assertTrue(artifactFullName.startsWith("io.netty:netty-"),
                "All artifact information should start with 'io.netty:netty-'");
        }
    }
}
