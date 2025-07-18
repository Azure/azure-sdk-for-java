// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.URIParameter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Integration tests for {@link HttpLogOptions}.
 */
@SuppressWarnings("removal")
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Mutates the global SecurityManager")
@EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_23, disabledReason = "Security manager was removed in Java 24")
public class HttpLogOptionsIT {
    private java.lang.SecurityManager originalManager;
    private java.security.Policy originalPolicy;

    public void captureDefaultConfigurations() {
        originalManager = java.lang.System.getSecurityManager();
        originalPolicy = java.security.Policy.getPolicy();

        // Set the System property codebase.azure-core to the location of HttpLogOptions's codebase.
        // This gets picked up by the policy setting to prevent needing to hardcode the code base location.
        System.setProperty("codebase.azure-core",
            HttpLogOptions.class.getProtectionDomain().getCodeSource().getLocation().toString());
    }

    public void revertDefaultConfigurations() {
        java.lang.System.setSecurityManager(originalManager);
        java.security.Policy.setPolicy(originalPolicy);

        // Now that the properties have been used, clear them.
        System.clearProperty("codebase.azure-core");
    }

    /**
     * Validates a fix for <a href="https://github.com/Azure/azure-sdk-for-java/issues/42905">Issue #42905</a>, where
     * {@link HttpLogOptions} would throw a recursive exception when a {@code SecurityManager} is present.
     */
    @Test
    public void noRecursionExceptionWithSecurityManager() throws URISyntaxException, NoSuchAlgorithmException {
        captureDefaultConfigurations();

        try {
            java.security.Policy.setPolicy(java.security.Policy.getInstance("JavaPolicy",
                new URIParameter(HttpLogOptionsIT.class.getResource("/HttpLogOptionsPolicies/simple.policy").toURI())));
            System.setSecurityManager(new SecurityManager());

            assertDoesNotThrow(HttpLogOptions::new);
        } finally {
            revertDefaultConfigurations();
        }
    }
}
