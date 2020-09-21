// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.ClientOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ServiceBusAdministrationClientBuilder}.
 */
public class ServiceBusAdministrationClientBuilderTest {
    private final String connectionString = "Endpoint=sb://foo.servicebus.windows.net;SharedAccessKeyName=dummyKey;SharedAccessKey=dummyAccessKey";

    @Test
    void buildClientDifferentApplicationIdTest() {
        // Arrange
        ServiceBusAdministrationClientBuilder builder = new ServiceBusAdministrationClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(new HttpLogOptions().setApplicationId("appid"))
            .clientOptions(new ClientOptions().setApplicationId("anotherAppId"));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> builder.buildAsyncClient());
    }
}
