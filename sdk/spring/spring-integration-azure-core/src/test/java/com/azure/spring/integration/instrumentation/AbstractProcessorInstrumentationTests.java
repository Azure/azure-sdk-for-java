// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.instrumentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractProcessorInstrumentationTests<T> {

    private IllegalArgumentException exception;
    private T errorContext;
    private Duration window = Duration.ofSeconds(2);

    public abstract T getErrorContext(RuntimeException exception);
    public abstract AbstractProcessorInstrumentation<T> getProcessorInstrumentation(Instrumentation.Type type,
                                                                                    Duration window);

    @BeforeEach
    void setUp() {
        exception = new IllegalArgumentException();
        errorContext = getErrorContext(exception);
    }

    @ParameterizedTest
    @EnumSource(value = Instrumentation.Type.class)
    void instrumentationId(Instrumentation.Type type) {
        AbstractProcessorInstrumentation<T> instrumentation = getProcessorInstrumentation(type, window);
        assertEquals(type.name() + ":test", instrumentation.getId());
    }

    @ParameterizedTest
    @EnumSource(value = Instrumentation.Type.class)
    void isUp(Instrumentation.Type type) {
        AbstractProcessorInstrumentation<T> instrumentation = getProcessorInstrumentation(type, window);
        assertTrue(instrumentation.isUp());
    }

    @ParameterizedTest
    @EnumSource(value = Instrumentation.Type.class)
    void isDown(Instrumentation.Type type) {
        AbstractProcessorInstrumentation<T> instrumentation = getProcessorInstrumentation(type, window);
        instrumentation.markError(errorContext);
        assertTrue(instrumentation.isDown());
        sleepSeconds(1);
        assertTrue(instrumentation.isDown());
        sleepSeconds(1);
        assertFalse(instrumentation.isDown());
    }

    @ParameterizedTest
    @EnumSource(value = Instrumentation.Type.class)
    void makeError(Instrumentation.Type type) {
        AbstractProcessorInstrumentation<T> instrumentation = getProcessorInstrumentation(type, window);
        instrumentation.markError(errorContext);
        assertEquals(exception, instrumentation.getException());
        instrumentation.markError(null);
        assertNull(instrumentation.getException());
    }

    private void sleepSeconds(long sleep) {
        try {
            TimeUnit.SECONDS.sleep(sleep);
        } catch (InterruptedException e) {

        }
    }
}
