// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.util.Locale;

@RunWith(Theories.class)
public class ConnectionStringPropertiesTest {
    private static final String HOST = "foo.bar.windows.net";
    private static final String HOSTNAME_URI = "sb://" + HOST;
    private static final String EVENT_HUB = "event-hub-instance";
    private static final String SAS_KEY = "test-sas-key";
    private static final String SAS_VALUE = "some-secret-value";

    @DataPoints
    public static String[] getInvalidArguments() {
        return new String[]{"", null};
    }

    @Theory
    public void nullConnectionString(String argument) {
        try {
            new ConnectionStringProperties(argument);
            Assert.fail("Expected an exception.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }
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
        Assert.assertEquals(HOST, properties.endpoint().getHost());
        Assert.assertEquals(SAS_KEY, properties.sharedAccessKeyName());
        Assert.assertEquals(SAS_VALUE, properties.sharedAccessKey());
        Assert.assertNull(properties.eventHubName());
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
        Assert.assertEquals(HOST, properties.endpoint().getHost());
        Assert.assertEquals(SAS_KEY, properties.sharedAccessKeyName());
        Assert.assertEquals(SAS_VALUE, properties.sharedAccessKey());
        Assert.assertEquals(EVENT_HUB, properties.eventHubName());
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
