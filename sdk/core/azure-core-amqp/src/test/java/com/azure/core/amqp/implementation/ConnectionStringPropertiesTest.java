// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConnectionStringPropertiesTest {
    private static final String HOST = "foo.bar.windows.net";
    private static final String HOSTNAME_URI = "sb://" + HOST;
    private static final String EVENT_HUB = "event-hub-instance";
    private static final String SAS_KEY = "test-sas-key";
    private static final String SAS_VALUE = "some-secret-value";

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

        // Act
        assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(connectionString));
    }

    @Test
    public void invalidEndpointScheme() {
        // Arrange
        final String connectionString = getConnectionString("http://" + HOST, EVENT_HUB, SAS_KEY, null);

        // Act
        assertThrows(IllegalArgumentException.class, () -> new ConnectionStringProperties(connectionString));
    }

    /**
     * Verifies we can create ConnectionStringProperties even if there is an extraneous component.
     */
    @Test
    public void extraneousComponent() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, null, SAS_KEY, SAS_VALUE)
            + "FakeKey=FakeValue";

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

    private static String getConnectionString(String hostname, String eventHubName, String sasKeyName, String sasKeyValue) {
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

        return builder.toString();
    }
}
