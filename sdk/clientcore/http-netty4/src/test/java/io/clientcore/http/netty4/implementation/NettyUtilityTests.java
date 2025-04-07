// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.utils.CoreUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.clientcore.http.netty4.implementation.NettyUtility.NETTY_VERSION_PROPERTY;
import static io.clientcore.http.netty4.implementation.NettyUtility.PROPERTIES_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 1, unit = TimeUnit.MINUTES)
public class NettyUtilityTests {
    @Test
    public void validateNettyVersionsWithWhatThePomSpecifies() {
        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        NettyUtility.NettyVersionLogInformation logInformation
            = NettyUtility.createNettyVersionLogInformation(pomVersions.get(NETTY_VERSION_PROPERTY));

        // Should never have version mismatches when running tests, that would mean either the version properties are
        // wrong or there is a dependency diamond within http-netty4. Either way, it should be fixed.
        assertFalse(logInformation.shouldLog());
        for (String artifactFullName : logInformation.classpathNettyVersions.keySet()) {
            assertTrue(artifactFullName.startsWith("io.netty:netty-"),
                "All artifact information should start with 'io.netty:netty-'");
        }
    }

    @Test
    public void validateNettyVersionsWithJunkVersions() {
        NettyUtility.NettyVersionLogInformation logInformation
            = NettyUtility.createNettyVersionLogInformation("4.0.0.Final");

        // Junk versions used should flag for logging.
        assertTrue(logInformation.shouldLog());
        for (String artifactFullName : logInformation.classpathNettyVersions.keySet()) {
            assertTrue(artifactFullName.startsWith("io.netty:netty-"),
                "All artifact information should start with 'io.netty:netty-'");
        }
    }
}
