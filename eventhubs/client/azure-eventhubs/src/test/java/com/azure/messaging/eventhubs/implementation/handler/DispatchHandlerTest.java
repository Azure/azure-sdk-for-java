// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.handler;

import org.apache.qpid.proton.engine.Event;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;

public class DispatchHandlerTest {
    @Test(expected = NullPointerException.class)
    public void exceptionsConstructor() {
        new DispatchHandler(null);
    }

    @Test
    public void runsWork() {
        // Arrange
        final AtomicBoolean hasSet = new AtomicBoolean();
        final DispatchHandler handler = new DispatchHandler(() -> {
            hasSet.compareAndSet(false, true);
        });
        final Event event = mock(Event.class);

        // Act
        handler.onTimerTask(event);

        // Assert
        Assert.assertTrue(hasSet.get());
    }
}
