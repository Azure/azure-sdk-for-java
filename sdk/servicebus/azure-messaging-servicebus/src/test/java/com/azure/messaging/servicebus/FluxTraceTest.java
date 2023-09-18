// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FluxTraceTest {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private final ServiceBusReceivedMessage receivedMessage = new ServiceBusReceivedMessage(BinaryData.fromString("Some Data"));
    private final ServiceBusMessageContext message = new ServiceBusMessageContext(receivedMessage);
    private final TestPublisher<ServiceBusMessageContext> messagesPublisher = TestPublisher.create();

    @ParameterizedTest
    @EnumSource(ReceiverKind.class)
    public void testProcessSpans(ReceiverKind receiverKind) {
        TestTracer tracer = new TestTracer();
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(tracer, null, "fqdn", "entityPath", null, receiverKind);
        FluxTrace fluxTrace = new FluxTrace(messagesPublisher.flux(), instrumentation);

        StepVerifier.create(fluxTrace)
            .then(() -> messagesPublisher.next(message))
            .assertNext(m -> {
                switch (receiverKind) {
                    case SYNC_RECEIVER:
                        assertEquals(0, tracer.getStartedSpans().size());
                        break;
                    default:
                        assertEquals(1, tracer.getStartedSpans().size());
                        assertEquals("ServiceBus.process", tracer.getStartedSpans().get(0));
                        break;
                }
            })
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    @ParameterizedTest
    @EnumSource(ReceiverKind.class)
    public void nullMessage(ReceiverKind receiverKind) {
        TestTracer tracer = new TestTracer();
        ServiceBusReceiverInstrumentation instrumentation = new ServiceBusReceiverInstrumentation(tracer, null, "fqdn", "entityPath", null, receiverKind);
        FluxTrace fluxTrace = new FluxTrace(messagesPublisher.flux(), instrumentation);

        StepVerifier.create(fluxTrace)
            .then(() -> messagesPublisher.next(new ServiceBusMessageContext("sessionId", new RuntimeException("foo"))))
            .assertNext(m -> assertEquals(0, tracer.getStartedSpans().size()))
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);
    }

    private static class TestTracer implements Tracer {
        private final List<String> startedSpans = new ArrayList<>();
        @Override
        public Context start(String methodName, StartSpanOptions options, Context context) {
            startedSpans.add(methodName);
            return context;
        }

        @Override
        public Context start(String methodName, Context context) {
            return start(methodName, new StartSpanOptions(SpanKind.INTERNAL), context);
        }

        @Override
        public void end(String errorMessage, Throwable throwable, Context context) {
        }

        @Override
        public void setAttribute(String key, String value, Context context) {
        }

        public List<String> getStartedSpans() {
            return startedSpans;
        }
    }
}
