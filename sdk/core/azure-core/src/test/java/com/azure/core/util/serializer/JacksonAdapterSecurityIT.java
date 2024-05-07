// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.net.URISyntaxException;
import java.security.URIParameter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link JacksonAdapter} functionality when there is a SecurityManager.
 */
@SuppressWarnings("removal")
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Mutates the global SecurityManager")
public class JacksonAdapterSecurityIT {
    private static final String A_PROPERTY_JSON = "{\"aProperty\":\"aValue\"}";
    private static final SimplePojo EXPECTED_SIMPLE_POJO = new SimplePojo("aValue");

    private boolean originalUseAccessHelper;
    private java.lang.SecurityManager originalManager;
    private java.security.Policy originalPolicy;

    public void captureDefaultConfigurations() {
        originalUseAccessHelper = JacksonAdapter.isUseAccessHelper();
        originalManager = java.lang.System.getSecurityManager();
        originalPolicy = java.security.Policy.getPolicy();

        // Set the System property codebase.azure-core to the location of JacksonAdapter's codebase.
        // This gets picked up by the policy setting to prevent needing to hardcode the code base location.
        System.setProperty("codebase.azure-core",
            JacksonAdapter.class.getProtectionDomain().getCodeSource().getLocation().toString());

        // Same goes for Jackson Databind.
        System.setProperty("codebase.jackson-databind",
            ObjectMapper.class.getProtectionDomain().getCodeSource().getLocation().toString());
    }

    public void revertDefaultConfigurations() throws NoSuchMethodException, NoSuchFieldException {
        JacksonAdapter.setUseAccessHelper(originalUseAccessHelper);
        java.lang.System.setSecurityManager(originalManager);
        java.security.Policy.setPolicy(originalPolicy);

        // Now that the properties have been used, clear them.
        System.clearProperty("codebase.azure-core");
        System.clearProperty("codebase.jackson-databind");

        // Reset accessibility after each test, otherwise they may pass unexpectedly.
        SimplePojo.class.getDeclaredConstructor(String.class).setAccessible(false);
        SimplePojo.class.getDeclaredField("aProperty").setAccessible(false);
        SimplePojo.class.getDeclaredMethod("getAProperty").setAccessible(false);
    }

    /**
     * Caller doesn't have the correct permissions to perform serialization.
     */
    @Test
    public void securityPreventsSerialization() throws Exception {
        captureDefaultConfigurations();

        try {
            JacksonAdapter adapter = new JacksonAdapter();

            java.security.Policy
                .setPolicy(java.security.Policy.getInstance("JavaPolicy", getUriParameter("basic-permissions.policy")));
            java.lang.System.setSecurityManager(new java.lang.SecurityManager());

            assertThrows(InvalidDefinitionException.class,
                () -> adapter.deserialize(A_PROPERTY_JSON, SimplePojo.class, SerializerEncoding.JSON));
        } finally {
            revertDefaultConfigurations();
        }
    }

    /**
     * Caller and JacksonAdapter's access helper doesn't have the correct permissions for serialization.
     */
    @Test
    public void securityAndAccessHelperNotMatchingPreventsSerialization() throws Exception {
        captureDefaultConfigurations();

        try {
            JacksonAdapter adapter = new JacksonAdapter();
            JacksonAdapter.setUseAccessHelper(true);

            java.security.Policy
                .setPolicy(java.security.Policy.getInstance("JavaPolicy", getUriParameter("basic-permissions.policy")));
            java.lang.System.setSecurityManager(new java.lang.SecurityManager());

            assertThrows(InvalidDefinitionException.class,
                () -> adapter.deserialize(A_PROPERTY_JSON, SimplePojo.class, SerializerEncoding.JSON));
        } finally {
            revertDefaultConfigurations();
        }
    }

    /**
     * Caller doesn't have the correct permissions to perform serialization but JacksonAdapter's access helper does.
     */
    @Test
    public void securityAndAccessHelperWorks() throws Exception {
        captureDefaultConfigurations();

        try {
            JacksonAdapter adapter = new JacksonAdapter();
            JacksonAdapter.setUseAccessHelper(true);

            java.security.Policy.setPolicy(
                java.security.Policy.getInstance("JavaPolicy", getUriParameter("access-helper-succeeds.policy")));
            java.lang.System.setSecurityManager(new java.lang.SecurityManager());

            SimplePojo actual = assertDoesNotThrow(
                () -> adapter.deserialize(A_PROPERTY_JSON, SimplePojo.class, SerializerEncoding.JSON));

            assertEquals(EXPECTED_SIMPLE_POJO.getAProperty(), actual.getAProperty());
        } finally {
            revertDefaultConfigurations();
        }
    }

    /**
     * Baseline test for when there is no SecurityManager.
     */
    @Test
    public void noSecurityRestrictionsWorks() throws Exception {
        captureDefaultConfigurations();

        try {
            JacksonAdapter adapter = new JacksonAdapter();
            SimplePojo actual = assertDoesNotThrow(
                () -> adapter.deserialize(A_PROPERTY_JSON, SimplePojo.class, SerializerEncoding.JSON));

            assertEquals(EXPECTED_SIMPLE_POJO.getAProperty(), actual.getAProperty());
        } finally {
            revertDefaultConfigurations();
        }
    }

    private static URIParameter getUriParameter(String policyFile) throws URISyntaxException {
        return new URIParameter(
            JacksonAdapterSecurityIT.class.getResource("/JacksonAdapterSecurityPolicies/" + policyFile).toURI());
    }

    private static final class SimplePojo {
        @JsonProperty("aProperty")
        private final String aProperty;

        @JsonCreator
        private SimplePojo(@JsonProperty("aProperty") String aProperty) {
            this.aProperty = aProperty;
        }

        public String getAProperty() {
            return aProperty;
        }
    }
}
