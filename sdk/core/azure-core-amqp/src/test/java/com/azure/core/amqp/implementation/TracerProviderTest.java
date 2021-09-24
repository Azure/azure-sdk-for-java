// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Signal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TracerProviderTest {
    private static final String SERVICE_BASE_NAME = "serviceBaseName";
    private static final String METHOD_NAME = SERVICE_BASE_NAME + "send";

    @Mock
    private Tracer tracer;

    private List<Tracer> tracers;
    private TracerProvider tracerProvider;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);

        tracers = Collections.singletonList(tracer);
        tracerProvider = new TracerProvider(tracers);
    }

    @AfterEach
    public void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void startSpan() {
        // Act
        tracerProvider.startSpan(SERVICE_BASE_NAME, Context.NONE, ProcessKind.SEND);

        // Assert
        for (Tracer t : tracers) {
            verify(t, times(1))
                .start(eq(METHOD_NAME), any(), eq(ProcessKind.SEND));
        }
    }

    @Test
    public void notEnabledWhenNoTracers() {
        // Arrange
        final TracerProvider provider = new TracerProvider(Collections.emptyList());

        // Act & Assert
        Assertions.assertFalse(provider.isEnabled());
    }

    @Test
    public void startSpanReturnsUpdatedContext() {
        // Arrange
        final String parentKey = "parent-key";
        final String parentValue = "parent-value";
        final Context startingContext = Context.NONE;
        when(tracer.start(METHOD_NAME, startingContext, ProcessKind.SEND)).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(parentKey, parentValue);
            }
        );

        // Act
        final Context updatedContext = tracerProvider.startSpan(SERVICE_BASE_NAME, startingContext, ProcessKind.SEND);

        // Assert
        // Want to ensure that the data added to the parent are available.
        final Optional<Object> parentData = updatedContext.getData(parentKey);
        Assertions.assertTrue(parentData.isPresent());
        Assertions.assertEquals(parentValue, parentData.get());
    }

    @Test
    public void endSpanSuccess() {
        // Act
        tracerProvider.endSpan(new Context("test-span-key", "value"), Signal.complete());

        // Assert
        for (Tracer t : tracers) {
            verify(t, times(1)).end(eq("success"), isNull(), any(Context.class));
        }
    }

    @Test
    public void endSpanNoKey() {
        // Act
        tracerProvider.endSpan(Context.NONE, Signal.complete());

        // Assert
        for (Tracer t : tracers) {
            verify(t, never()).end("", null, Context.NONE);
        }
    }

    @Test
    public void endSpanError() {
        // Arrange
        Throwable testThrow = new Throwable("testError");
        Context sendContext = new Context("test-span-key", "value");

        // Act
        tracerProvider.endSpan(sendContext, Signal.error(testThrow));

        // Assert
        for (Tracer t : tracers) {
            verify(t, times(1)).end("", testThrow, sendContext);
        }
    }

    @Test
    public void endSpanOnSubscribe() {
        // Arrange
        Throwable testThrow = new Throwable("testError");
        Context sendContext = new Context("test-span-key", "value");

        // Act
        tracerProvider.endSpan(sendContext, Signal.error(testThrow));

        // Assert
        for (Tracer t : tracers) {
            verify(t, times(1)).end("", testThrow, sendContext);
        }
    }

    @Test
    public void endSpanAmqpException() {
        // Arrange
        final AmqpErrorCondition errorCondition = AmqpErrorCondition.NOT_FOUND;
        final Exception exception = new AmqpException(true, errorCondition, "", null);
        Context sendContext = new Context("test-span-key", "value");

        // Act
        tracerProvider.endSpan(sendContext, Signal.error(exception));

        // Assert
        for (Tracer t : tracers) {
            verify(t, times(1))
                .end(errorCondition.getErrorCondition(), exception, sendContext);
        }
    }

    @Test
    public void addSpanLinksNoContext() {
        // Act
        assertThrows(NullPointerException.class, () -> tracerProvider.addSpanLinks(null));
    }

    /**
     * Verify that we add spans for all the tracers.
     */
    @Test
    public void addSpanLinks() {
        // Act
        assertThrows(NullPointerException.class, () -> tracerProvider.addSpanLinks(null));
    }

    @Test
    public void getSpanBuilderReturnsUpdatedContext() {
        // Arrange
        final String spanBuilderKey = "spanBuilder-key";
        final String spanBuilderValue = "spanBuilder-value";

        final Context startingContext = Context.NONE;

        when(tracer.getSharedSpanBuilder(anyString(), any())).thenAnswer(
            invocation -> {
                Context passed = invocation.getArgument(1, Context.class);
                return passed.addData(spanBuilderKey, spanBuilderValue);
            }
        );

        // Act
        final Context updatedContext = tracerProvider.getSharedSpanBuilder(SERVICE_BASE_NAME, startingContext);

        // Assert
        final Optional<Object> spanBuilderData = updatedContext.getData(spanBuilderKey);
        Assertions.assertTrue(spanBuilderData.isPresent());
        Assertions.assertEquals(spanBuilderValue, spanBuilderData.get());
    }
}
