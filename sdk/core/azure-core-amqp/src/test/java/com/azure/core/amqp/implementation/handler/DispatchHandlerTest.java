// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.engine.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class DispatchHandlerTest {
    @Test
    public void exceptionsConstructor() {
        assertThrows(NullPointerException.class, () -> {
            new DispatchHandler(null);
        });
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
        Assertions.assertTrue(hasSet.get());
    }
}
