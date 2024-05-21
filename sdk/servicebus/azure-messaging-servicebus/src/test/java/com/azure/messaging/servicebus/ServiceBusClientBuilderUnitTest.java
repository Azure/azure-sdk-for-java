// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenCredential;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

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
        assertThrows(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .sessionReceiver()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildAsyncClient());
        assertThrows(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .processor()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient());
        assertThrows(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .sessionReceiver()
                .sessionIdleTimeout(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildAsyncClient());
        assertThrows(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .processor()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient());
    }

    @Test
    public void testThrowsIfNegativeMaxLockDuration() {
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .sessionReceiver()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildAsyncClient(),
                "'maxAutoLockRenewDuration' cannot be negative.");
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .receiver()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildClient(),
            "'maxAutoLockRenewDuration' cannot be negative.");
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .processor()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient(),
            "'maxAutoLockRenewDuration' cannot be negative.");
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .sessionProcessor()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildProcessorClient(),
            "'maxAutoLockRenewDuration' cannot be negative.");
    }

    @Test
    public void testThrowsIfNegativeSessionIdle() {
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .sessionReceiver()
                .sessionIdleTimeout(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildAsyncClient(),
            "'sessionIdleTimeout' cannot be negative.");
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder()
                .sessionProcessor()
                .sessionIdleTimeout(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildProcessorClient(),
            "'sessionIdleTimeout' cannot be negative.");
    }

    @Test
    public void testBuildsWithTokenCredentialIfFullyQualifiedNameIsProvided() {
        createMinimalValidClientBuilder()
                .sender()
                .queueName("fakequeue")
                .buildClient();
        createMinimalValidClientBuilder()
                .receiver()
                .queueName("fakequeue")
                .buildClient();
        createMinimalValidClientBuilder()
                .sessionProcessor()
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient();
        createMinimalValidClientBuilder()
                .sessionReceiver()
                .queueName("fakequeue")
                .buildClient();
        createMinimalValidClientBuilder()
                .processor()
                .queueName("fakequeue")
                .processMessage(x -> { })
                .processError(x -> { })
                .buildProcessorClient();
    }

    @Test
    public void testBuildsWithSessionIdle() {
        createMinimalValidClientBuilder()
            .sessionReceiver()
            .sessionIdleTimeout(null)
            .queueName("fakequeue")
            .buildAsyncClient();
        createMinimalValidClientBuilder()
            .sessionProcessor()
            .sessionIdleTimeout(null)
            .queueName("fakequeue")
            .processMessage(x -> { })
            .processError(x -> { })
            .buildProcessorClient();
        createMinimalValidClientBuilder()
            .sessionReceiver()
            .sessionIdleTimeout(Duration.ZERO)
            .queueName("fakequeue")
            .buildAsyncClient();
        createMinimalValidClientBuilder()
            .sessionProcessor()
            .sessionIdleTimeout(Duration.ZERO)
            .queueName("fakequeue")
            .processMessage(x -> { })
            .processError(x -> { })
            .buildProcessorClient();
        createMinimalValidClientBuilder()
            .sessionReceiver()
            .sessionIdleTimeout(Duration.ofSeconds(1))
            .queueName("fakequeue")
            .buildAsyncClient();
        createMinimalValidClientBuilder()
            .sessionProcessor()
            .sessionIdleTimeout(Duration.ofSeconds(1))
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

    private static ServiceBusClientBuilder createMinimalValidClientBuilder() {
        return new ServiceBusClientBuilder()
            .credential(FAKE_TOKEN_CREDENTIAL)
            .fullyQualifiedNamespace(NAMESPACE_NAME);
    }
}
