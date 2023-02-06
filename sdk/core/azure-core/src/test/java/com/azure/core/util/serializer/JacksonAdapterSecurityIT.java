// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
 * <p>
 * Tests need to be ordered where SecurityManager failing tests are ran first. If they aren't ran first these could fail
 * if Jackson was able to reflectively access and cache the inaccessible class.
 */
@SuppressWarnings({"removal", "unused", "try"})
@Execution(ExecutionMode.SAME_THREAD)
@Isolated("Mutates the global SecurityManager")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JacksonAdapterSecurityIT {
    private static final String A_PROPERTY_JSON = "{\"aProperty\":\"aValue\"}";
    private static final SimplePojo EXPECTED_SIMPLE_POJO = new SimplePojo("aValue");

    public AutoCloseable captureDefaultConfigurations() {
        return new RevertConfigurations(JacksonAdapter.isUseAccessHelper(), System.getSecurityManager(),
            java.security.Policy.getPolicy());
    }

    /**
     * Caller doesn't have the correct permissions to perform serialization.
     */
    @Test
    @Order(1)
    public void securityPreventsSerialization() throws Exception {
        try (AutoCloseable ignored = captureDefaultConfigurations()) {
            SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();

            java.security.Policy.setPolicy(java.security.Policy
                .getInstance("JavaPolicy", getUriParameter("basic-permissions.policy")));
            System.setSecurityManager(new SecurityManager());

            assertThrows(InvalidDefinitionException.class, () ->
                adapter.deserialize(A_PROPERTY_JSON, SimplePojo.class, SerializerEncoding.JSON));
        }
    }

    /**
     * Caller and JacksonAdapter's access helper doesn't have the correct permissions for serialization.
     */
    @Test
    @Order(2)
    public void securityAndAccessHelperNotMatchingPreventsSerialization() throws Exception {
        try (AutoCloseable ignored = captureDefaultConfigurations()) {
            SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();
            JacksonAdapter.setUseAccessHelper(true);

            java.security.Policy.setPolicy(java.security.Policy
                .getInstance("JavaPolicy", getUriParameter("basic-permissions.policy")));
            System.setSecurityManager(new SecurityManager());

            assertThrows(InvalidDefinitionException.class, () ->
                adapter.deserialize(A_PROPERTY_JSON, SimplePojo.class, SerializerEncoding.JSON));
        }
    }

    /**
     * Caller doesn't have the correct permissions to perform serialization but JacksonAdapter's access helper does.
     */
    @Test
    @Order(3)
    public void securityAndAccessHelperWorks() throws Exception {
        try (AutoCloseable ignored = captureDefaultConfigurations()) {
            SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();
            JacksonAdapter.setUseAccessHelper(true);

            java.security.Policy.setPolicy(java.security.Policy
                .getInstance("JavaPolicy", getUriParameter("access-helper-succeeds.policy")));
            System.setSecurityManager(new SecurityManager());

            SimplePojo actual = assertDoesNotThrow(() ->
                adapter.deserialize(A_PROPERTY_JSON, SimplePojo.class, SerializerEncoding.JSON));

            assertEquals(EXPECTED_SIMPLE_POJO.getAProperty(), actual.getAProperty());
        }
    }

    /**
     * Baseline test for when there is no SecurityManager.
     */
    @Test
    @Order(4)
    public void noSecurityRestrictionsWorks() throws Exception {
        try (AutoCloseable ignored = captureDefaultConfigurations()) {
            SerializerAdapter adapter = JacksonAdapter.createDefaultSerializerAdapter();
            SimplePojo actual = assertDoesNotThrow(() ->
                adapter.deserialize(A_PROPERTY_JSON, SimplePojo.class, SerializerEncoding.JSON));

            assertEquals(EXPECTED_SIMPLE_POJO.getAProperty(), actual.getAProperty());
        }
    }

    private static URIParameter getUriParameter(String policyFile) throws URISyntaxException {
        return new URIParameter(JacksonAdapterSecurityIT.class
            .getResource("/JacksonAdapterSecurityPolicies/" + policyFile)
            .toURI());
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

    static final class RevertConfigurations implements AutoCloseable {
        private final boolean originalUseAccessHelper;
        private final SecurityManager originalManager;
        private final java.security.Policy originalPolicy;

        RevertConfigurations(boolean originalUseAccessHelper, SecurityManager originalManager,
            java.security.Policy originalPolicy) {
            this.originalUseAccessHelper = originalUseAccessHelper;
            this.originalManager = originalManager;
            this.originalPolicy = originalPolicy;

            // Set the System property codebase.azure-core to the location of JacksonAdapter's codebase.
            // This gets picked up by the policy setting to prevent needing to hardcode the code base location.
            System.setProperty("codebase.azure-core", JacksonAdapter.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toString());

            // Same goes for Jackson Databind.
            System.setProperty("codebase.jackson-databind", ObjectMapper.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toString());
        }

        @Override
        public void close() {
            JacksonAdapter.setUseAccessHelper(originalUseAccessHelper);
            System.setSecurityManager(originalManager);
            java.security.Policy.setPolicy(originalPolicy);

            // Now that the properties have been used, clear them.
            System.clearProperty("codebase.azure-core");
            System.clearProperty("codebase.jackson-databind");

            // Reset accessibility after each test, otherwise they may pass unexpectedly.
            try {
                SimplePojo.class.getDeclaredConstructor(String.class).setAccessible(false);
                SimplePojo.class.getDeclaredField("aProperty").setAccessible(false);
                SimplePojo.class.getDeclaredMethod("getAProperty").setAccessible(false);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
