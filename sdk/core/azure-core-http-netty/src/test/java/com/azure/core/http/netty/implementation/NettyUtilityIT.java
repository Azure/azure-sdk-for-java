// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.URIParameter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration tests for {@link NettyUtility}.
 */
@SuppressWarnings("removal")
@Isolated("Mutates the global SecurityManager")
public class NettyUtilityIT {
    private java.lang.SecurityManager originalManager;
    private java.security.Policy originalPolicy;

    public void captureDefaultConfigurations() {
        originalManager = java.lang.System.getSecurityManager();
        originalPolicy = java.security.Policy.getPolicy();

        // Set the System property codebase.azure-core-netty-http to the location of NettyUtility's codebase.
        // This gets picked up by the policy setting to prevent needing to hardcode the code base location.
        System.setProperty("codebase.azure-core-http-netty",
            NettyUtility.class.getProtectionDomain().getCodeSource().getLocation().toString());
    }

    public void revertDefaultConfigurations() {
        java.lang.System.setSecurityManager(originalManager);
        java.security.Policy.setPolicy(originalPolicy);

        // Now that the properties have been used, clear them.
        System.clearProperty("codebase.azure-core-http-netty");
    }

    @Test
    public void validateVersionLoggingWithSecurityManager() throws URISyntaxException, NoSuchAlgorithmException {
        captureDefaultConfigurations();

        try {
            java.security.Policy.setPolicy(java.security.Policy.getInstance("JavaPolicy", getUriParameter()));
            java.lang.System.setSecurityManager(new java.lang.SecurityManager());

            assertDoesNotThrow(NettyUtility::validateNettyVersionsInternal);
        } finally {
            revertDefaultConfigurations();
        }
    }

    private static URIParameter getUriParameter() throws URISyntaxException {
        return new URIParameter(NettyUtilityIT.class.getResource("/version-logging.policy").toURI());
    }
}
