// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class TokenResourceProviderTest {
    private static final String HOST_NAME = "foobar.windows.net";

    @Test(expected = NullPointerException.class)
    public void constructorNullType() {
        new TokenResourceProvider(null, HOST_NAME);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullHost() {
        new TokenResourceProvider(CBSAuthorizationType.JSON_WEB_TOKEN, null);
    }

    @DataPoints
    public static CBSAuthorizationType[] getAuthorizationTypes() {
        return CBSAuthorizationType.values();
    }

    /**
     * Verifies that the correct resource string is returned when we pass in different authorization types.
     */
    @Theory
    public void getResourceString(CBSAuthorizationType authorizationType) {
        // Arrange
        final TokenResourceProvider provider = new TokenResourceProvider(authorizationType, HOST_NAME);
        final String entityPath = "event-hub-test-2/partition/2";

        // Act
        final String actual = provider.getResourceString(entityPath);

        // Assert
        switch (authorizationType) {
            case SHARED_ACCESS_SIGNATURE:
                final String expected = "amqp://" + HOST_NAME + "/" + entityPath;
                Assert.assertEquals(expected, actual);
                break;
            case JSON_WEB_TOKEN:
                Assert.assertEquals("https://eventhubs.azure.net//.default", actual);
                break;
            default:
                Assert.fail("This authorization type is unknown: " + authorizationType);
        }
    }
}
