// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConnectionStringPropertiesTest {
    private static final String HOST = "foo.bar.windows.net";
    private static final String HOSTNAME_URI = "sb://" + HOST;
    private static final String EVENT_HUB = "event-hub-instance";
    private static final String SAS_KEY = "test-sas-key";
    private static final String SAS_VALUE = "some-secret-value";
    private static final String SHARED_ACCESS_SIGNATURE = "SharedAccessSignature "
        + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
        + "&sig=encodedsignature%3D"
        + "&se=100000"
        + "&skn=test-sas-key";

    @Test
    public void nullConnectionString() {
        assertThrows(NullPointerException.class, () -> new ConnectionStringProperties(null));
    }

    @Test
    public void emptyConnectionString() {
        assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(""));
    }

    @Test
    public void invalidUri() {
        // Arrange
        final String connectionString = getConnectionString("invalid-uri-^ick", EVENT_HUB, SAS_KEY, SAS_VALUE);

        // Act
        assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(connectionString));
    }

    @Test
    public void invalidSasKeyName() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, EVENT_HUB, null, SAS_VALUE);

        // Act
        assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(connectionString));
    }

    @Test
    public void invalidSasKeyValue() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, EVENT_HUB, SAS_KEY, null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(connectionString));
    }

    /**
     * Verifies sdk do not expose secret in exception string when SharedAccessKeyName is not present.
     */
    @Test
    public void invalidConnectionStringNoSecretExposed() {
        // Arrange
        final String invalidConnectionString = getConnectionString(HOSTNAME_URI, EVENT_HUB, SAS_KEY, SAS_VALUE)
            .replace(String.format(Locale.US, "SharedAccessKeyName=%s;", SAS_KEY), "");

        // Act & Assert
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(invalidConnectionString));

        final String actualMessage = exception.getMessage();

        assertFalse(actualMessage.contains(SAS_VALUE));
        assertFalse(actualMessage.contains(HOSTNAME_URI));
    }

    /**
     * Verifies sdk do not expose secret in exception string when 'Endpoint' is not present.
     */
    @Test
    public void invalidEndpointNoSecretExposed() {
        // Arrange
        final String invalidConnectionString = getConnectionString(HOSTNAME_URI, EVENT_HUB, SAS_KEY, SAS_VALUE)
            .replace(String.format(Locale.US, "Endpoint=%s;", HOSTNAME_URI), "");

        // Act & Assert
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(invalidConnectionString));

        final String actualMessage = exception.getMessage();

        assertFalse(actualMessage.contains(SAS_VALUE));
        assertFalse(actualMessage.contains(SAS_KEY));
    }

    @Test
    public void differentEndpointScheme() {
        // Arrange
        final String connectionString = getConnectionString("http://" + HOST, EVENT_HUB, SAS_KEY, SAS_VALUE);

        // Act
        ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);

        // Assert
        Assertions.assertEquals(HOST, properties.getEndpoint().getHost());
        Assertions.assertEquals(SAS_KEY, properties.getSharedAccessKeyName());
        Assertions.assertEquals(SAS_VALUE, properties.getSharedAccessKey());
        Assertions.assertEquals(EVENT_HUB, properties.getEntityPath());
    }

    @Test
    public void noEndpointSchemeDefault() {
        // Arrange
        final String connectionString = getConnectionString(HOST, EVENT_HUB, SAS_KEY, SAS_VALUE);

        // Act
        ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);

        // Assert
        Assertions.assertEquals("sb", properties.getEndpoint().getScheme());
    }

    /**
     * Verifies we can create ConnectionStringProperties even if there is an extraneous component.
     */
    @Test
    public void invalidExtraneousComponent() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, EVENT_HUB, SAS_KEY, SAS_VALUE)
            + "FakeKey=FakeValue";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(connectionString));
    }

    /**
     * Verifies we can create ConnectionStringProperties without "EntityPath" specified. This is a namespace SAS key.
     */
    @Test
    public void namespaceConnectionString() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, null, SAS_KEY, SAS_VALUE);

        // Act
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);

        // Assert
        Assertions.assertEquals(HOST, properties.getEndpoint().getHost());
        Assertions.assertEquals(SAS_KEY, properties.getSharedAccessKeyName());
        Assertions.assertEquals(SAS_VALUE, properties.getSharedAccessKey());
        Assertions.assertNull(properties.getEntityPath());
    }

    /**
     * Verifies we can create ConnectionStringProperties with "EntityPath" specified.
     */
    @Test
    public void parseConnectionString() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, EVENT_HUB, SAS_KEY, SAS_VALUE);

        // Act
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);

        // Assert
        Assertions.assertEquals(HOST, properties.getEndpoint().getHost());
        Assertions.assertEquals(SAS_KEY, properties.getSharedAccessKeyName());
        Assertions.assertEquals(SAS_VALUE, properties.getSharedAccessKey());
        Assertions.assertEquals(EVENT_HUB, properties.getEntityPath());
    }

    @ParameterizedTest
    @MethodSource("getInvalidConnectionString")
    public void testConnectionStringWithSas(String invalidConnectionString) {
        assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(invalidConnectionString));
    }

    @ParameterizedTest
    @MethodSource("getSharedAccessSignature")
    public void testInvalidSharedAccessSignature(String sas) {
        assertThrows(IllegalArgumentException.class, () ->
            new ConnectionStringProperties(getConnectionString(HOSTNAME_URI, null, null, null, sas)));
    }

    private static Stream<String> getInvalidConnectionString() {
        String keyNameWithSas = getConnectionString(HOSTNAME_URI, EVENT_HUB, SAS_KEY, null, SHARED_ACCESS_SIGNATURE);
        String keyValueWithSas = getConnectionString(HOSTNAME_URI, EVENT_HUB, null, SAS_VALUE, SHARED_ACCESS_SIGNATURE);
        String keyNameAndValueWithSas = getConnectionString(HOSTNAME_URI, EVENT_HUB, SAS_KEY, SAS_VALUE,
            SHARED_ACCESS_SIGNATURE);
        String nullHostName = getConnectionString(null, EVENT_HUB, SAS_KEY, SAS_VALUE, SHARED_ACCESS_SIGNATURE);
        String nullHostNameValidSas = getConnectionString(null, EVENT_HUB, null, null, SHARED_ACCESS_SIGNATURE);
        String nullHostNameValidKey = getConnectionString(null, EVENT_HUB, SAS_KEY, SAS_VALUE, null);
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
