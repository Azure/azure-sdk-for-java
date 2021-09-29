// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Unit test for sync subscriber.
 */
public class SynchronousMessageSubscriberTest {

    private static final int PREFETCH = 1;

    @Mock
    private SynchronousReceiveWork work1;

    @Mock
    private SynchronousReceiveWork work2;

    @Mock
    private Subscription subscription;

    private SynchronousMessageSubscriber syncSubscriber;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(work1.getId()).thenReturn(1L);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Test that if prefetch is large value, it will be the one requested.
     */
    @Test
    void workAddedAndLargePrefetch() {
        // Arrange
        when(work1.getId()).thenReturn(1L);

        // Act
        syncSubscriber = new SynchronousMessageSubscriber(100, work1);

        // Assert
        Assertions.assertEquals(1, syncSubscriber.getWorkQueueSize());
        Assertions.assertEquals(100, syncSubscriber.getRequested());
    }

    /**
     * Test that if prefetch is small value than work, larger value be requested.
     */
    @Test
    void workAddedInQueueOnCreation() {
        // Arrange & Act
        when(work1.getNumberOfEvents()).thenReturn(3);
        syncSubscriber = new SynchronousMessageSubscriber(0, work1);

        // Assert
        Assertions.assertEquals(1, syncSubscriber.getWorkQueueSize());
        Assertions.assertEquals(3, syncSubscriber.getRequested());

    }

    /**
     * A work get queued in work queue.
     */
    @Test
    void queueWorkTest() {
        // Arrange
        syncSubscriber = new SynchronousMessageSubscriber(PREFETCH, work1);

        // Act
        syncSubscriber.queueWork(work2);

        // Assert
        Assertions.assertEquals(2, syncSubscriber.getWorkQueueSize());
        Assertions.assertEquals(1, syncSubscriber.getRequested());

    }

    /**
     * When we call hookOnSubscribe, the sync subscriber is initialised.
     */
    @Test
    void hookOnSubscribeTest() {
        // Arrange
        syncSubscriber = new SynchronousMessageSubscriber(PREFETCH, work1);
        when(work1.getTimeout()).thenReturn(Duration.ofSeconds(10));
        when(work1.isTerminal()).thenReturn(true);
        doNothing().when(subscription).request(1);

        // Act
        syncSubscriber.hookOnSubscribe(subscription);

        // Assert
        Assertions.assertTrue(syncSubscriber.isSubscriberInitialized());
        Assertions.assertEquals(0, syncSubscriber.getWorkQueueSize());
        Assertions.assertEquals(1, syncSubscriber.getRequested());

    }
}
