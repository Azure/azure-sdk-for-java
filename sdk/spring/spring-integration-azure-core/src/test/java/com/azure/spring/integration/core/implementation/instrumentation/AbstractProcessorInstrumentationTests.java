// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.implementation.instrumentation;

import com.azure.spring.integration.core.instrumentation.Instrumentation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractProcessorInstrumentationTests<T> {

    private IllegalArgumentException exception;
    private T errorContext;
    private final Duration window = Duration.ofSeconds(2);

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
        assertEquals(Instrumentation.Status.UP, instrumentation.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = Instrumentation.Type.class)
    void isDown(Instrumentation.Type type) throws InterruptedException {
        AbstractProcessorInstrumentation<T> instrumentation = getProcessorInstrumentation(type, window);
        instrumentation.markError(errorContext);
        assertEquals(Instrumentation.Status.DOWN, instrumentation.getStatus());
        Thread.sleep(window.toMillis() + 1_000);
        assertEquals(Instrumentation.Status.UP, instrumentation.getStatus());
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
}
