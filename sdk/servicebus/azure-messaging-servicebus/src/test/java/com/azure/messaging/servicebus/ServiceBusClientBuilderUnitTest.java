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
            () -> new ServiceBusClientBuilder().credential(FAKE_TOKEN_CREDENTIAL)
                .sender()
                .queueName("fakequeue")
                .buildClient());
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder().credential(FAKE_TOKEN_CREDENTIAL)
                .receiver()
                .queueName("fakequeue")
                .buildClient());
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder().credential(FAKE_TOKEN_CREDENTIAL)
                .sessionProcessor()
                .queueName("fakequeue")
                .processMessage(x -> {
                })
                .processError(x -> {
                })
                .buildProcessorClient());
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder().credential(FAKE_TOKEN_CREDENTIAL)
                .sessionReceiver()
                .queueName("fakequeue")
                .buildClient());
        assertThrows(IllegalArgumentException.class,
            () -> new ServiceBusClientBuilder().credential(FAKE_TOKEN_CREDENTIAL)
                .processor()
                .queueName("fakequeue")
                .processMessage(x -> {
                })
                .processError(x -> {
                })
                .buildProcessorClient());
        assertThrows(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().sessionReceiver()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildAsyncClient());
        assertThrows(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().processor()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .processMessage(x -> {
                })
                .processError(x -> {
                })
                .buildProcessorClient());
        assertThrows(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().sessionReceiver()
                .sessionIdleTimeout(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildAsyncClient());
        assertThrows(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().processor()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .processMessage(x -> {
                })
                .processError(x -> {
                })
                .buildProcessorClient());
    }

    @Test
    public void testThrowsIfNegativeMaxLockDuration() {
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().sessionReceiver()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildAsyncClient(),
            "'maxAutoLockRenewDuration' cannot be negative.");
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().receiver()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildClient(),
            "'maxAutoLockRenewDuration' cannot be negative.");
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().processor()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .processMessage(x -> {
                })
                .processError(x -> {
                })
                .buildProcessorClient(),
            "'maxAutoLockRenewDuration' cannot be negative.");
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().sessionProcessor()
                .maxAutoLockRenewDuration(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildProcessorClient(),
            "'maxAutoLockRenewDuration' cannot be negative.");
    }

    @Test
    public void testThrowsIfNegativeSessionIdle() {
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().sessionReceiver()
                .sessionIdleTimeout(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildAsyncClient(),
            "'sessionIdleTimeout' cannot be negative.");
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().sessionProcessor()
                .sessionIdleTimeout(Duration.ofSeconds(-1))
                .queueName("fakequeue")
                .buildProcessorClient(),
            "'sessionIdleTimeout' cannot be negative.");
    }

    @Test
    public void testBuildsWithTokenCredentialIfFullyQualifiedNameIsProvided() {
        createMinimalValidClientBuilder().sender().queueName("fakequeue").buildClient();
        createMinimalValidClientBuilder().receiver().queueName("fakequeue").buildClient();
        createMinimalValidClientBuilder().sessionProcessor().queueName("fakequeue").processMessage(x -> {
        }).processError(x -> {
        }).buildProcessorClient();
        createMinimalValidClientBuilder().sessionReceiver().queueName("fakequeue").buildClient();
        createMinimalValidClientBuilder().processor().queueName("fakequeue").processMessage(x -> {
        }).processError(x -> {
        }).buildProcessorClient();
    }

    @Test
    public void testBuildsWithSessionIdle() {
        createMinimalValidClientBuilder().sessionReceiver()
            .sessionIdleTimeout(null)
            .queueName("fakequeue")
            .buildAsyncClient();
        createMinimalValidClientBuilder().sessionProcessor()
            .sessionIdleTimeout(null)
            .queueName("fakequeue")
            .processMessage(x -> {
            })
            .processError(x -> {
            })
            .buildProcessorClient();
        createMinimalValidClientBuilder().sessionReceiver()
            .sessionIdleTimeout(Duration.ZERO)
            .queueName("fakequeue")
            .buildAsyncClient();
        createMinimalValidClientBuilder().sessionProcessor()
            .sessionIdleTimeout(Duration.ZERO)
            .queueName("fakequeue")
            .processMessage(x -> {
            })
            .processError(x -> {
            })
            .buildProcessorClient();
        createMinimalValidClientBuilder().sessionReceiver()
            .sessionIdleTimeout(Duration.ofSeconds(1))
            .queueName("fakequeue")
            .buildAsyncClient();
        createMinimalValidClientBuilder().sessionProcessor()
            .sessionIdleTimeout(Duration.ofSeconds(1))
            .queueName("fakequeue")
            .processMessage(x -> {
            })
            .processError(x -> {
            })
            .buildProcessorClient();
    }

    @Test
    public void testEntityNameInConnectionString() {
        // Arrange
        final String connectionString = "Endpoint=sb://test.servicebus.windows.net/;"
            + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=sharedKey;EntityPath=testQueue";

        // Act
        final ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder
            = new ServiceBusClientBuilder().connectionString(connectionString).sender();

        // Assert
        assertNotNull(builder.buildAsyncClient());
    }

    /**
     * Verifies that {@code drainTimeout(Duration)} on both processor builders rejects null
     * (NullPointerException) and zero/negative values (IllegalArgumentException), matching the
     * documented contract on {@code ServiceBusProcessorClientOptions.setDrainTimeout(Duration)}.
     */
    @Test
    public void processorBuilderDrainTimeoutValidation() {
        // Non-session processor builder.
        assertThrowsExactly(NullPointerException.class,
            () -> createMinimalValidClientBuilder().processor().drainTimeout(null));
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().processor().drainTimeout(Duration.ZERO));
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().processor().drainTimeout(Duration.ofSeconds(-1)));

        // Session processor builder.
        assertThrowsExactly(NullPointerException.class,
            () -> createMinimalValidClientBuilder().sessionProcessor().drainTimeout(null));
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().sessionProcessor().drainTimeout(Duration.ZERO));
        assertThrowsExactly(IllegalArgumentException.class,
            () -> createMinimalValidClientBuilder().sessionProcessor().drainTimeout(Duration.ofSeconds(-1)));
    }

    /**
     * Verifies that a positive {@code drainTimeout(Duration)} is accepted on both processor
     * builders and propagates through to a buildable processor client (no validation exception
     * during build).
     */
    @Test
    public void processorBuilderDrainTimeoutPositiveAccepted() {
        // Non-session processor builder accepts a positive duration and builds successfully.
        final ServiceBusProcessorClient processorClient = createMinimalValidClientBuilder().processor()
            .queueName("fakequeue")
            .drainTimeout(Duration.ofSeconds(5))
            .processMessage(x -> {
            })
            .processError(x -> {
            })
            .buildProcessorClient();
        assertNotNull(processorClient);

        // Session processor builder accepts a positive duration and builds successfully.
        final ServiceBusProcessorClient sessionProcessorClient = createMinimalValidClientBuilder().sessionProcessor()
            .queueName("fakequeue")
            .drainTimeout(Duration.ofSeconds(5))
            .processMessage(x -> {
            })
            .processError(x -> {
            })
            .buildProcessorClient();
        assertNotNull(sessionProcessorClient);
    }

    private static ServiceBusClientBuilder createMinimalValidClientBuilder() {
        return new ServiceBusClientBuilder().credential(FAKE_TOKEN_CREDENTIAL).fullyQualifiedNamespace(NAMESPACE_NAME);
    }
}
