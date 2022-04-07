// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenCredential;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServiceBusClientBuilderUnitTest {

    private static final String NAMESPACE_NAME = "dummyNamespaceName";
    private static final TokenCredential FAKE_TOKEN_CREDENTIAL = new BasicAuthenticationCredential("foo", "bar");

    @Test
    public void testThrowsWithTokenCredentialIfFullyQualifiedNameIsMissing() {
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .sender()
                .queueName("fakequeue")
                .buildClient());
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .receiver()
                .queueName("fakequeue")
                .buildClient());
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .sessionProcessor()
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient());
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .sessionReceiver()
                .queueName("fakequeue")
                .buildClient());
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .processor()
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient());
    }

    @Test
    public void testBuildsWithTokenCredentialIfFullyQualifiedNameIsProvided() {
        new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .fullyQualifiedNamespace(NAMESPACE_NAME)
                .sender()
                .queueName("fakequeue")
                .buildClient();
        new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .fullyQualifiedNamespace(NAMESPACE_NAME)
                .receiver()
                .queueName("fakequeue")
                .buildClient();
        new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .fullyQualifiedNamespace(NAMESPACE_NAME)
                .sessionProcessor()
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient();
        new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .fullyQualifiedNamespace(NAMESPACE_NAME)
                .sessionReceiver()
                .queueName("fakequeue")
                .buildClient();
        new ServiceBusClientBuilder()
                .credential(FAKE_TOKEN_CREDENTIAL)
                .fullyQualifiedNamespace(NAMESPACE_NAME)
                .processor()
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient();
    }

    @Test
    public void testEntityNameInConnectionString() {
        // Arrange
        final String connectionString = "Endpoint=sb://test.servicebus.windows.net/;"
            + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=sharedKey;EntityPath=testQueue";

        // Act
        final ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender();

        // Assert
        assertNotNull(builder.buildAsyncClient());
    }
}
