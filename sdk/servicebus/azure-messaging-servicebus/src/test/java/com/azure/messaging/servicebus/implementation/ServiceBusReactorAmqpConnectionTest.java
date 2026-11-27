// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusReactorAmqpConnectionTest {
    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMock(this);
    }

    @ParameterizedTest
    @CsvSource({
        "false,false,LINK_DETACH_FORCED",
        "false,true,LINK_DETACH_FORCED",
        "true,false,LINK_DETACH_FORCED",
        "true,true,LINK_DETACH_FORCED",
        "false,false,CONNECTION_FORCED",
        "false,true,CONNECTION_FORCED",
        "true,false,CONNECTION_FORCED",
        "true,true,CONNECTION_FORCED",
        "false,false,RESOURCE_LIMIT_EXCEEDED",
        "false,true,RESOURCE_LIMIT_EXCEEDED",
        "true,false,RESOURCE_LIMIT_EXCEEDED",
        "true,true,RESOURCE_LIMIT_EXCEEDED",
        "false,false,NOT_ALLOWED",
        "false,true,NOT_ALLOWED",
        "true,false,NOT_ALLOWED",
        "true,true,NOT_ALLOWED" })
    void receiveLinkFailureRemovesOnlyItsSession(boolean sessionReceiver, boolean replaceBeforeFailure,
        AmqpErrorCondition condition) {
        final ServiceBusSession failed = mock(ServiceBusSession.class);
        final ServiceBusSession replacement = mock(ServiceBusSession.class);
        when(failed.getSessionName()).thenReturn("cached-session");
        final Sinks.One<ServiceBusReceiveLink> pending = Sinks.one();
        when(failed.createConsumer(anyString(), anyString(), any(), any(), any(), any(), anyString()))
            .thenReturn(pending.asMono());
        when(failed.createConsumer(anyString(), anyString(), any(), any(), any(), any(), anyString(), anyString()))
            .thenReturn(pending.asMono());
        final AtomicReference<AmqpSession> cached = new AtomicReference<>(failed);
        final ConnectionOptions options = mock(ConnectionOptions.class);
        when(options.getRetry()).thenReturn(new AmqpRetryOptions());
        final ReactorHandlerProvider handlers = mock(ReactorHandlerProvider.class);
        final ConnectionHandler handler = mock(ConnectionHandler.class);
        when(handlers.createConnectionHandler(anyString(), any())).thenReturn(handler);
        when(handler.getEndpointStates()).thenReturn(Flux.never());
        final ServiceBusReactorAmqpConnection connection
            = new ServiceBusReactorAmqpConnection("connection", options, mock(ReactorProvider.class), handlers,
                mock(ServiceBusAmqpLinkProvider.class), mock(TokenManagerProvider.class), null, false, true, false) {
                @Override
                public Mono<AmqpSession> createSession(String name) {
                    return Mono.just(cached.get());
                }

                @Override
                protected boolean removeSession(String name, AmqpSession expected) {
                    Assertions.assertEquals("cached-session", name);
                    Assertions.assertSame(failed, expected);
                    return cached.compareAndSet(expected, null);
                }
            };
        final AmqpException error = new AmqpException(true, condition, "receive failure", null);
        final Mono<ServiceBusReceiveLink> link = sessionReceiver
            ? connection.createReceiveLink("link", "entity", ServiceBusReceiveMode.PEEK_LOCK, null,
                MessagingEntityType.QUEUE, "client", "session-id")
            : connection.createReceiveLink("link", "entity", ServiceBusReceiveMode.PEEK_LOCK, null,
                MessagingEntityType.QUEUE, "client");
        StepVerifier.create(link).then(() -> {
            if (replaceBeforeFailure) {
                cached.set(replacement);
            }
            pending.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
        }).expectErrorMatches(actual -> actual == error).verify(Duration.ofSeconds(5));
        if (replaceBeforeFailure) {
            Assertions.assertSame(replacement, cached.get());
        } else if (condition == AmqpErrorCondition.LINK_DETACH_FORCED
            || condition == AmqpErrorCondition.CONNECTION_FORCED) {
            Assertions.assertNull(cached.get());
        } else {
            Assertions.assertSame(failed, cached.get());
        }
    }
}
