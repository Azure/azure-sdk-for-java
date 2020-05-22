// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test for sync subscriber
 */
public class SynchronousMessageSubscriberTest {

    private static final String NAMESPACE = "my-namespace";
    private static final String ENTITY_NAME = "my-servicebus-entity";

    @Mock
    private SynchronousReceiveWork work1;

    @Mock
    private SynchronousReceiveWork work2;

    @Captor
    private ArgumentCaptor<ServiceBusMessage> singleMessageCaptor;

    @Captor
    private ArgumentCaptor<List<ServiceBusMessage>> messageListCaptor;

    @Captor
    private ArgumentCaptor<Instant> scheduleMessageCaptor;

    @Captor
    private ArgumentCaptor<Long> cancelScheduleMessageCaptor;

    private SynchronousMessageSubscriber syncSybscriber;

    private static final Duration RETRY_TIMEOUT = Duration.ofSeconds(10);
    private static final int prefetch = 1;
    private static final String TEST_CONTENTS = "My message for service bus queue!";

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
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void workAddedInQueueOnCreation() {

       syncSybscriber = new SynchronousMessageSubscriber(prefetch, work1);

        Assertions.assertEquals(1, syncSybscriber.getWorkQueueSize());
        Assertions.assertEquals(1, syncSybscriber.getRequested());

    }

    @Test
    void queueWork() {

        syncSybscriber = new SynchronousMessageSubscriber(prefetch, work1);
        syncSybscriber.queueWork(work2);
        Assertions.assertEquals(2, syncSybscriber.getWorkQueueSize());
        Assertions.assertEquals(1, syncSybscriber.getRequested());

    }
}
