// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class ConnectionStringPropertiesTest {
    private static final String HOST = "foo.bar.windows.net";
    private static final String HOSTNAME_URI = "sb://" + HOST;
    private static final String EVENT_HUB = "event-hub-instance";
    private static final String SAS_KEY = "test-sas-key";
    private static final String SAS_VALUE = "some-secret-value";

    @Test(expected = NullPointerException.class)
    public void nullConnectionString() {
        new ConnectionStringProperties(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyConnectionString() {
        new ConnectionStringProperties("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidUri() {
        // Arrange
        final String connectionString = getConnectionString("invalid-uri-^ick", EVENT_HUB, SAS_KEY, SAS_VALUE);

        // Act
        new ConnectionStringProperties(connectionString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSasKeyName() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, EVENT_HUB, null, SAS_VALUE);

        // Act
        new ConnectionStringProperties(connectionString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSasKeyValue() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, EVENT_HUB, SAS_KEY, null);

        // Act
        new ConnectionStringProperties(connectionString);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEndpointScheme() {
        // Arrange
        final String connectionString = getConnectionString("http://" + HOST, EVENT_HUB, SAS_KEY, null);

        // Act
        new ConnectionStringProperties(connectionString);
    }

    /**
     * Verifies we can create ConnectionStringProperties even if there is an extraneous component.
     */
    @Test(expected = IllegalArgumentException.class)
    public void extraneousComponent() {
        // Arrange
        final String connectionString = getConnectionString(HOSTNAME_URI, null, SAS_KEY, SAS_VALUE)
            + "FakeKey=FakeValue";

        // Act
        new ConnectionStringProperties(connectionString);
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
        Assert.assertEquals(HOST, properties.getEndpoint().getHost());
        Assert.assertEquals(SAS_KEY, properties.getSharedAccessKeyName());
        Assert.assertEquals(SAS_VALUE, properties.getSharedAccessKey());
        Assert.assertNull(properties.getEventHubName());
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
        Assert.assertEquals(HOST, properties.getEndpoint().getHost());
        Assert.assertEquals(SAS_KEY, properties.getSharedAccessKeyName());
        Assert.assertEquals(SAS_VALUE, properties.getSharedAccessKey());
        Assert.assertEquals(EVENT_HUB, properties.getEventHubName());
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
