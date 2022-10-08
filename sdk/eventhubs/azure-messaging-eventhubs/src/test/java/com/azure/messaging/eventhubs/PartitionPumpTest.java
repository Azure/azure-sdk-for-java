// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link PartitionPump}.
 */
public class PartitionPumpTest {
    @Mock
    private Scheduler scheduler;
    @Mock
    private EventHubConsumerAsyncClient consumerAsyncClient;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void beforeEach() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (autoCloseable != null) {
            autoCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that we dispose of the client and scheduler when disposed.
     */
    @Test
    public void disposesResources() {
        // Arrange
        final String partitionId = "1";
        final PartitionPump partitionPump = new PartitionPump(partitionId, consumerAsyncClient, scheduler);
        when(scheduler.disposeGracefully()).thenReturn(Mono.empty());

        // Act
        partitionPump.close();

        // Assert
        verify(scheduler).disposeGracefully();
        verify(consumerAsyncClient).close();
    }

    /**
     * Tests getter.
     */
    @Test
    public void getClient() {
        // Arrange
        final String partitionId = "1";
        final PartitionPump partitionPump = new PartitionPump(partitionId, consumerAsyncClient, scheduler);

        // Act
        final EventHubConsumerAsyncClient actual = partitionPump.getClient();

        // Assert
        assertEquals(consumerAsyncClient, actual);
    }
}
