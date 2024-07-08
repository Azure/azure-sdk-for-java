// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import com.azure.core.v2.util.CoreUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.net.URISyntaxException;
import java.security.URIParameter;
import java.time.Duration;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link ImplUtils} functionality when there is a SecurityManager.
 */
@SuppressWarnings("removal")
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Mutates the global SecurityManager")
public class ImplUtilsSecurityIT {
    private static final Duration SHUTDOWN_TIMEOUT = Duration.ofSeconds(5);

    private boolean originalShutdownHookAccessHelper;
    private java.lang.SecurityManager originalManager;
    private java.security.Policy originalPolicy;

    public void captureDefaultConfigurations() {
        originalShutdownHookAccessHelper = ImplUtils.isShutdownHookAccessHelper();
        originalManager = java.lang.System.getSecurityManager();
        originalPolicy = java.security.Policy.getPolicy();

        // Set the System property codebase.azure-core to the location of CoreUtils's codebase.
        // This gets picked up by the policy setting to prevent needing to hardcode the code base location.
        System.setProperty("codebase.azure-core",
            CoreUtils.class.getProtectionDomain().getCodeSource().getLocation().toString());
    }

    public void revertDefaultConfigurations() {
        ImplUtils.setShutdownHookAccessHelper(originalShutdownHookAccessHelper);
        java.lang.System.setSecurityManager(originalManager);
        java.security.Policy.setPolicy(originalPolicy);

        // Now that the properties have been used, clear them.
        System.clearProperty("codebase.azure-core");
    }

    /**
     * Caller doesn't have the correct permissions to perform adding a shutdown hook.
     */
    @Test
    public void securityPreventsShutdownHook() throws Exception {
        captureDefaultConfigurations();

        try {
            java.security.Policy
                .setPolicy(java.security.Policy.getInstance("JavaPolicy", getUriParameter("basic-permissions.policy")));
            System.setSecurityManager(new SecurityManager());

            assertThrows(SecurityException.class,
                () -> CoreUtils.addShutdownHookSafely(Executors.newCachedThreadPool(), SHUTDOWN_TIMEOUT));
        } finally {
            revertDefaultConfigurations();
        }
    }

    /**
     * Caller and CoreUtils access helper doesn't have the correct permissions for serialization.
     */
    @Test
    public void securityAndAccessHelperNotMatchingPreventsSerialization() throws Exception {
        captureDefaultConfigurations();

        try {
            ImplUtils.setShutdownHookAccessHelper(true);

            java.security.Policy
                .setPolicy(java.security.Policy.getInstance("JavaPolicy", getUriParameter("basic-permissions.policy")));
            System.setSecurityManager(new SecurityManager());

            assertThrows(SecurityException.class,
                () -> CoreUtils.addShutdownHookSafely(Executors.newCachedThreadPool(), SHUTDOWN_TIMEOUT));
        } finally {
            revertDefaultConfigurations();
        }
    }

    /**
     * Caller doesn't have the correct permissions to perform serialization but CoreUtils access helper does.
     */
    @Test
    public void securityAndAccessHelperWorks() throws Exception {
        captureDefaultConfigurations();

        try {
            ImplUtils.setShutdownHookAccessHelper(true);

            java.security.Policy.setPolicy(
                java.security.Policy.getInstance("JavaPolicy", getUriParameter("access-helper-succeeds.policy")));
            System.setSecurityManager(new SecurityManager());

            assertDoesNotThrow(
                () -> CoreUtils.addShutdownHookSafely(Executors.newCachedThreadPool(), SHUTDOWN_TIMEOUT));
        } finally {
            revertDefaultConfigurations();
        }
    }

    /**
     * Baseline test for when there is no SecurityManager.
     */
    @Test
    public void noSecurityRestrictionsWorks() {
        captureDefaultConfigurations();

        try {
            ImplUtils.setShutdownHookAccessHelper(false);

            assertDoesNotThrow(
                () -> CoreUtils.addShutdownHookSafely(Executors.newCachedThreadPool(), SHUTDOWN_TIMEOUT));
        } finally {
            revertDefaultConfigurations();
        }
    }

    private static URIParameter getUriParameter(String policyFile) throws URISyntaxException {
        return new URIParameter(
            ImplUtilsSecurityIT.class.getResource("/CoreUtilsSecurityPolicies/" + policyFile).toURI());
    }
}
