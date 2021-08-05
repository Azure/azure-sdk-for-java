// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceBusConnectionStringPropertiesTest {
    private static final String HOST = "foo.bar.windows.net";
    private static final String HOSTNAME_URI = "sb://" + HOST;
    private static final String ENTITY_PATH = "entity-instance";
    private static final String SAS_KEY = "test-sas-key";
    private static final String SAS_VALUE = "some-secret-value";
    private static final String SHARED_ACCESS_SIGNATURE = "SharedAccessSignature "
        + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
        + "&sig=encodedsignature%3D"
        + "&se=100000"
        + "&skn=test-sas-key";

    @Test
    public void nullConnectionString() {
        assertThrows(NullPointerException.class, () -> ServiceBusConnectionStringProperties.parse(null));
    }

    @Test
    public void emptyConnectionString() {
        assertThrows(IllegalArgumentException.class, () -> ServiceBusConnectionStringProperties.parse(""));
    }

    @Test
    public void invalidUri() {
        // Arrange
        final String connectionString = getConnectionString("invalid-uri-^ick", ENTITY_PATH, SAS_KEY, SAS_VALUE);

        // Act
        assertThrows(IllegalArgumentException.class, () -> ServiceBusConnectionStringProperties.parse(connectionString));
    }

    @Test
    public void invalidSasKeyName() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, ENTITY_PATH, null, SAS_VALUE);

        // Act
        assertThrows(IllegalArgumentException.class, () -> ServiceBusConnectionStringProperties.parse(connectionString));
    }

    @Test
    public void invalidSasKeyValue() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, ENTITY_PATH, SAS_KEY, null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ServiceBusConnectionStringProperties.parse(connectionString));
    }

    @Test
    public void differentEndpointScheme() {
        // Arrange
        final String connectionString = getConnectionString("http://" + HOST, ENTITY_PATH, SAS_KEY, SAS_VALUE);

        // Act
        ServiceBusConnectionStringProperties properties = ServiceBusConnectionStringProperties.parse(connectionString);

        // Assert
        assertEquals("http://" + HOST, properties.getEndpoint());
        assertEquals(HOST, properties.getFullyQualifiedNamespace());
        assertEquals(SAS_KEY, properties.getSharedAccessKeyName());
        assertEquals(SAS_VALUE, properties.getSharedAccessKey());
        assertEquals(ENTITY_PATH, properties.getEntityPath());
    }

    @Test
    public void noEndpointScheme() {
        // Arrange
        final String connectionString = getConnectionString(HOST, ENTITY_PATH, SAS_KEY, SAS_VALUE);

        // Act
        ServiceBusConnectionStringProperties properties = ServiceBusConnectionStringProperties.parse(connectionString);

        // Assert
        assertTrue(properties.getEndpoint().startsWith("sb://"));
        assertEquals(HOST, properties.getFullyQualifiedNamespace());
    }

    /**
     * Verifies we can create ServiceBusConnectionStringProperties even if there is an extraneous component.
     */
    @Test
    public void invalidExtraneousComponent() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, ENTITY_PATH, SAS_KEY, SAS_VALUE)
            + "FakeKey=FakeValue";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ServiceBusConnectionStringProperties.parse(connectionString));
    }

    /**
     * Verifies we can create ServiceBusConnectionStringProperties without "EntityPath" specified.
     * This is a namespace SAS key.
     */
    @Test
    public void namespaceConnectionString() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, null, SAS_KEY, SAS_VALUE);

        // Act
        final ServiceBusConnectionStringProperties properties = ServiceBusConnectionStringProperties.parse(
            connectionString);

        // Assert
        assertEquals(HOSTNAME_URI, properties.getEndpoint());
        assertEquals(HOST, properties.getFullyQualifiedNamespace());
        assertEquals(SAS_KEY, properties.getSharedAccessKeyName());
        assertEquals(SAS_VALUE, properties.getSharedAccessKey());
        assertNull(properties.getEntityPath());
    }

    /**
     * Verifies we can create ServiceBusConnectionStringProperties with "EntityPath" specified.
     */
    @Test
    public void parseConnectionString() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, ENTITY_PATH, SAS_KEY, SAS_VALUE);

        // Act
        final ServiceBusConnectionStringProperties properties = ServiceBusConnectionStringProperties.parse(
            connectionString);

        // Assert
        assertEquals(HOSTNAME_URI, properties.getEndpoint());
        assertEquals(HOST, properties.getFullyQualifiedNamespace());
        assertEquals(SAS_KEY, properties.getSharedAccessKeyName());
        assertEquals(SAS_VALUE, properties.getSharedAccessKey());
        assertEquals(ENTITY_PATH, properties.getEntityPath());
    }

    @ParameterizedTest
    @MethodSource("getInvalidConnectionString")
    public void testConnectionStringWithSas(String invalidConnectionString) {
        assertThrows(IllegalArgumentException.class, () -> ServiceBusConnectionStringProperties.parse(
            invalidConnectionString));
    }

    @ParameterizedTest
    @MethodSource("getSharedAccessSignature")
    public void testInvalidSharedAccessSignature(String sas) {
        assertThrows(IllegalArgumentException.class, () ->
            ServiceBusConnectionStringProperties.parse(getConnectionString(HOSTNAME_URI, null, null, null, sas)));
    }

    private static Stream<String> getInvalidConnectionString() {
        String keyNameWithSas = getConnectionString(HOSTNAME_URI, ENTITY_PATH, SAS_KEY, null, SHARED_ACCESS_SIGNATURE);
        String keyValueWithSas = getConnectionString(HOSTNAME_URI, ENTITY_PATH, null, SAS_VALUE, SHARED_ACCESS_SIGNATURE);
        String keyNameAndValueWithSas = getConnectionString(HOSTNAME_URI, ENTITY_PATH, SAS_KEY, SAS_VALUE,
            SHARED_ACCESS_SIGNATURE);
        String nullHostName = getConnectionString(null, ENTITY_PATH, SAS_KEY, SAS_VALUE, SHARED_ACCESS_SIGNATURE);
        String nullHostNameValidSas = getConnectionString(null, ENTITY_PATH, null, null, SHARED_ACCESS_SIGNATURE);
        String nullHostNameValidKey = getConnectionString(null, ENTITY_PATH, SAS_KEY, SAS_VALUE, null);
        return Stream.of(keyNameWithSas, keyValueWithSas, keyNameAndValueWithSas, nullHostName, nullHostNameValidSas,
            nullHostNameValidKey);
    }

    private static Stream<String> getSharedAccessSignature() {
        String nullSas = null;
        String sasInvalidPrefix = "AccessSignature " // invalid prefix
            + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
            + "&sig=encodedsignature%3D"
            + "&se=100000"
            + "&skn=test-sas-key";
        String sasWithoutSpace = "SharedAccessSignature" // no space after prefix
            + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
            + "&sig=encodedsignature%3D"
            + "&se=100000"
            + "&skn=test-sas-key";

        return Stream.of(nullSas, sasInvalidPrefix, sasWithoutSpace);
    }

    private static String getConnectionString(String hostname, String eventHubName, String sasKeyName,
        String sasKeyValue) {
        return getConnectionString(hostname, eventHubName, sasKeyName, sasKeyValue, null);
    }

    private static String getConnectionString(String hostname, String eventHubName, String sasKeyName,
        String sasKeyValue, String sharedAccessSignature) {
        final StringBuilder builder = new StringBuilder();
        if (hostname != null) {
            builder.append(String.format(Locale.US, "Endpoint=%s;", hostname));
        }
        if (eventHubName != null) {
            builder.append(String.format(Locale.US, "EntityPath=%s;", eventHubName));
        }
        if (sasKeyName != null) {
            builder.append(String.format(Locale.US, "SharedAccessKeyName=%s;", sasKeyName));
        }
        if (sasKeyValue != null) {
            builder.append(String.format(Locale.US, "SharedAccessKey=%s;", sasKeyValue));
        }
        if (sharedAccessSignature != null) {
            builder.append(String.format(Locale.US, "SharedAccessSignature=%s;", sharedAccessSignature));
        }

        return builder.toString();
    }
}
