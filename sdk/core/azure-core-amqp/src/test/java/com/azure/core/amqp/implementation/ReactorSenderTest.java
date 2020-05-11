// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.ExponentialAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.reactor.Reactor;
import org.apache.qpid.proton.reactor.Selectable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link ReactorSender}
 */
public class ReactorSenderTest {

    private String entityPath = "entity-path";

    @Mock
    private Sender sender;
    @Mock
    private SendLinkHandler handler;
    @Mock
    private ReactorProvider reactorProvider;
    @Mock
    private TokenManager tokenManager;
    @Mock
    private Reactor reactor;
    @Mock
    private Selectable selectable;
    @Mock
    private MessageSerializer messageSerializer;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        Delivery delivery = mock(Delivery.class);
        when(delivery.getRemoteState()).thenReturn(Accepted.getInstance());
        when(delivery.getTag()).thenReturn("tag".getBytes());
        when(handler.getDeliveredMessages()).thenReturn(Flux.just(delivery));
        when(reactor.selectable()).thenReturn(selectable);

        when(handler.getLinkCredits()).thenReturn(Flux.just(100));
        when(handler.getEndpointStates()).thenReturn(Flux.just(EndpointState.ACTIVE));
        when(handler.getErrors()).thenReturn(Flux.empty());
        when(tokenManager.getAuthorizationResults()).thenReturn(Flux.just(AmqpResponseCode.ACCEPTED));
        when(sender.getCredit()).thenReturn(0);
        doNothing().when(selectable).setChannel(any());
        doNothing().when(selectable).onReadable(any());
        doNothing().when(selectable).onFree(any());
        doNothing().when(selectable).setReading(true);
        doNothing().when(reactor).update(selectable);
        ReactorDispatcher reactorDispatcher = new ReactorDispatcher(reactor);
        when(reactor.attachments()).thenReturn(new Record() {
            @Override
            public <T> T get(Object o, Class<T> aClass) {
                return null;
            }

            @Override
            public <T> void set(Object o, Class<T> aClass, T t) {

            }

            @Override
            public void clear() {

            }
        });
        when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);
        when(sender.getRemoteMaxMessageSize()).thenReturn(UnsignedLong.valueOf(1000));
    }

    @Test
    public void testLinkSize() throws IOException {
        ReactorSender reactorSender = new ReactorSender(entityPath, sender, handler, reactorProvider, tokenManager,
            messageSerializer, Duration.ofSeconds(1), new ExponentialAmqpRetryPolicy(new AmqpRetryOptions()));
        StepVerifier.create(reactorSender.getLinkSize())
            .expectNext(1000)
            .verifyComplete();
        StepVerifier.create(reactorSender.getLinkSize())
            .expectNext(1000)
            .verifyComplete();
        verify(sender, times(1)).getRemoteMaxMessageSize();
    }

    @Test
    public void testSend() {
        Message message = Proton.message();
        message.setMessageId("id");
        message.setBody(new AmqpValue("hello"));
        ReactorSender reactorSender = new ReactorSender(entityPath, sender, handler, reactorProvider, tokenManager,
            messageSerializer, Duration.ofSeconds(1), new ExponentialAmqpRetryPolicy(new AmqpRetryOptions()));
        ReactorSender spyReactorSender = spy(reactorSender);

        doReturn(Mono.empty()).when(spyReactorSender).send(any(byte[].class), anyInt(), anyInt());
        StepVerifier.create(spyReactorSender.send(message))
            .verifyComplete();
        StepVerifier.create(spyReactorSender.send(message))
            .verifyComplete();
        verify(sender, times(1)).getRemoteMaxMessageSize();
        verify(spyReactorSender, times(2)).send(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void testSendBatch() {
        Message message = Proton.message();
        message.setMessageId("id1");
        message.setBody(new AmqpValue("hello"));

        Message message2 = Proton.message();
        message2.setMessageId("id2");
        message2.setBody(new AmqpValue("world"));

        ReactorSender reactorSender = new ReactorSender(entityPath, sender, handler, reactorProvider, tokenManager,
            messageSerializer, Duration.ofSeconds(1), new ExponentialAmqpRetryPolicy(new AmqpRetryOptions()));
        ReactorSender spyReactorSender = spy(reactorSender);

        doReturn(Mono.empty()).when(spyReactorSender).send(any(byte[].class), anyInt(), anyInt());
        StepVerifier.create(spyReactorSender.send(Arrays.asList(message, message2)))
            .verifyComplete();
        StepVerifier.create(spyReactorSender.send(Arrays.asList(message, message2)))
            .verifyComplete();
        verify(sender, times(1)).getRemoteMaxMessageSize();
        verify(spyReactorSender, times(2)).send(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void testLinkSizeSmallerThanMessageSize() {
        when(sender.getRemoteMaxMessageSize()).thenReturn(UnsignedLong.valueOf(10));
        Message message = Proton.message();
        message.setMessageId("id");
        message.setBody(new AmqpValue("hello"));
        ReactorSender reactorSender = new ReactorSender(entityPath, sender, handler, reactorProvider, tokenManager,
            messageSerializer, Duration.ofSeconds(1), new ExponentialAmqpRetryPolicy(new AmqpRetryOptions()));
        ReactorSender spyReactorSender = spy(reactorSender);

        doReturn(Mono.empty()).when(spyReactorSender).send(any(byte[].class), anyInt(), anyInt());
        StepVerifier.create(spyReactorSender.send(message))
            .verifyErrorSatisfies(throwable -> {
                Assertions.assertTrue(throwable instanceof AmqpException);
                Assertions.assertTrue(throwable.getMessage().startsWith("Error sending. Size of the payload exceeded "
                    + "maximum message size"));
            });
        verify(sender, times(1)).getRemoteMaxMessageSize();
        verify(spyReactorSender, times(0)).send(any(byte[].class), anyInt(), anyInt());
    }

}
