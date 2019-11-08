// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzureTokenManagerProviderTest {
    private static final String HOST_NAME = "foobar.windows.net";

    @Test
    public void constructorNullType() {
        assertThrows(NullPointerException.class, () -> new AzureTokenManagerProvider(null, HOST_NAME, "something."));
    }

    @Test
    public void constructorNullHost() {
        assertThrows(NullPointerException.class, () -> new AzureTokenManagerProvider(CBSAuthorizationType.JSON_WEB_TOKEN, null, "some-scope"));
    }

    @Test
    public void constructorNullScope() {
        assertThrows(NullPointerException.class, () -> new AzureTokenManagerProvider(CBSAuthorizationType.JSON_WEB_TOKEN, HOST_NAME, null));
    }

    /**
     * Verifies that the correct resource string is returned when we pass in different authorization types.
     */
    @ParameterizedTest
    @EnumSource(CBSAuthorizationType.class)
    public void getResourceString(CBSAuthorizationType authorizationType) {
        // Arrange
        final String scope = "some-scope";
        final AzureTokenManagerProvider provider = new AzureTokenManagerProvider(authorizationType, HOST_NAME, scope);
        final String entityPath = "event-hub-test-2/partition/2";

        // Act
        final String actual = provider.getResourceString(entityPath);

        // Assert
        switch (authorizationType) {
            case SHARED_ACCESS_SIGNATURE:
                final String expected = "amqp://" + HOST_NAME + "/" + entityPath;
                Assertions.assertEquals(expected, actual);
                break;
            case JSON_WEB_TOKEN:
                Assertions.assertEquals(scope, actual);
                break;
            default:
                Assertions.fail("This authorization type is unknown: " + authorizationType);
        }
    }
}
