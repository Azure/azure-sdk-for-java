// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.implementation.handler.DeliverySettleMode;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.ProtonSessionWrapper.ProtonChannelWrapper;
import com.azure.core.amqp.implementation.ProtonSession.ProtonChannel;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Record;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestResponseChannelCacheTest {
    private static final String CON_ID = "MF_0f4c2e_1680070221023";
    private static final String CH_ENTITY_PATH = "orders";
    private static final String CH_SESSION_NAME = "cbs-session";
    private static final String CH_LINK_NAME = "cbs";
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(30);
    private AmqpRetryPolicy retryPolicy = new FixedAmqpRetryPolicy(new AmqpRetryOptions());
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        Mockito.framework().clearInlineMock(this);
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @Test
    public void shouldGetChannel() {
        final int maxChannels = 1;
        try (MockEndpoint endpoint = createEndpoint(CON_ID, maxChannels)) {
            endpoint.arrange();
            final RequestResponseChannelCache channelCache = createCache(endpoint);
            try {
                final Mono<RequestResponseChannel> channelMono = channelCache.get();

                StepVerifier.create(channelMono, 0)
                    .thenRequest(1)
                    .then(() -> endpoint.activateCurrentChannel())
                    .expectNextMatches(ch -> {
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        return true;
                    })
                    .expectComplete()
                    .verify(VERIFY_TIMEOUT);
            } finally {
                channelCache.dispose();
            }
            // should close the channel upon the cache disposal in above finally {..}.
            endpoint.assertCurrentChannelClosed();
        }
    }

    @Test
    public void shouldCacheChannel() {
        final int maxChannels = 1;
        try (MockEndpoint endpoint = createEndpoint(CON_ID, maxChannels)) {
            endpoint.arrange();
            final RequestResponseChannelCache channelCache = createCache(endpoint);
            try {
                final RequestResponseChannel[] c = new RequestResponseChannel[1];
                final Mono<RequestResponseChannel> channelMono = channelCache.get();

                // The first request (subscription) for channel populates the cache.
                StepVerifier.create(channelMono, 0)
                    .thenRequest(1)
                    .then(() -> endpoint.activateCurrentChannel())
                    .expectNextMatches(ch -> {
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        // Store the channel to assert that the same is returned for later channel request.
                        c[0] = ch;
                        return true;
                    })
                    .expectComplete()
                    .verify(VERIFY_TIMEOUT);

                // Later a second channel request (Subscription) must be served from the cache.
                StepVerifier.create(channelMono, 0).thenRequest(1).expectNextMatches(ch -> {
                    Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                    // Assert the second subscription got the same channel (cached) as first subscription.
                    Assertions.assertEquals(c[0], ch);
                    return true;
                }).expectComplete().verify(VERIFY_TIMEOUT);

            } finally {
                channelCache.dispose();
            }
            // should close the channel upon the cache disposal in above finally {..}.
            endpoint.assertCurrentChannelClosed();
        }
    }

    @Test
    public void shouldRefreshCacheOnChannelCompletion() {
        final int maxChannels = 2;
        try (MockEndpoint endpoint = createEndpoint(CON_ID, maxChannels)) {
            endpoint.arrange();
            final RequestResponseChannelCache channelCache = createCache(endpoint);
            try {
                final RequestResponseChannel[] c = new RequestResponseChannel[1];
                final Mono<RequestResponseChannel> channelMono = channelCache.get();

                // The first request (subscription) for channel populates the cache.
                StepVerifier.create(channelMono, 0)
                    .thenRequest(1)
                    .then(() -> endpoint.activateCurrentChannel())
                    .expectNextMatches(ch -> {
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        // Store the channel to assert that once it's closed later channel request
                        // gets a (new) different channel.
                        c[0] = ch;
                        return true;
                    })
                    .expectComplete()
                    .verify(VERIFY_TIMEOUT);

                // Close the cached channel by completing channel endpoint.
                endpoint.completeCurrentChannel();

                // A new request (subscription) for connection should refresh cache.
                StepVerifier.create(channelMono, 0)
                    .thenRequest(1)
                    .then(() -> endpoint.activateCurrentChannel())
                    .expectNextMatches(ch -> {
                        // Assert the second subscription got a new channel as a result of cache refresh.
                        Assertions.assertNotEquals(c[0], ch);
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        return true;
                    })
                    .expectComplete()
                    .verify(VERIFY_TIMEOUT);

            } finally {
                channelCache.dispose();
            }
            // should close the channel upon the cache disposal in above finally {..}.
            endpoint.assertCurrentChannelClosed();
        }
    }

    @Test
    public void shouldRefreshCacheOnChannelError() {
        final int maxChannels = 2;
        try (MockEndpoint endpoint = createEndpoint(CON_ID, maxChannels)) {
            endpoint.arrange();
            final RequestResponseChannelCache channelCache = createCache(endpoint);
            try {
                final RequestResponseChannel[] c = new RequestResponseChannel[1];
                final Mono<RequestResponseChannel> channelMono = channelCache.get();

                // The first request (subscription) for channel populates the cache.
                StepVerifier.create(channelMono, 0)
                    .thenRequest(1)
                    .then(() -> endpoint.activateCurrentChannel())
                    .expectNextMatches(ch -> {
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        // Store the channel to assert that once it's closed later channel request
                        // gets a (new) different channel.
                        c[0] = ch;
                        return true;
                    })
                    .expectComplete()
                    .verify(VERIFY_TIMEOUT);

                // Close the cached channel by error-ing channel endpoint.
                endpoint.errorCurrentChannel(new RuntimeException("channel dropped"));

                // A new request (subscription) for connection should refresh cache.
                StepVerifier.create(channelMono, 0)
                    .thenRequest(1)
                    .then(() -> endpoint.activateCurrentChannel())
                    .expectNextMatches(ch -> {
                        // Assert the second subscription got a new channel as a result of cache refresh.
                        Assertions.assertNotEquals(c[0], ch);
                        Assertions.assertTrue(endpoint.isCurrentChannel(ch));
                        return true;
                    })
                    .expectComplete()
                    .verify(VERIFY_TIMEOUT);

            } finally {
                channelCache.dispose();
            }
            // should close the channel upon the cache disposal in above finally {..}.
            endpoint.assertCurrentChannelClosed();
        }
    }

    @Test
    public void shouldEmitChannelClosedErrorOnConnectionTermination() {
        final ReactorConnection connection = mock(ReactorConnection.class);
        when(connection.isDisposed()).thenReturn(true);
        final RequestResponseChannelCache channelCache
            = new RequestResponseChannelCache(connection, CH_ENTITY_PATH, CH_SESSION_NAME, CH_LINK_NAME, retryPolicy);
        try {
            final Mono<RequestResponseChannel> channelMono = channelCache.get();
            StepVerifier.create(channelMono, 0)
                .thenRequest(1)
                .expectError(RequestResponseChannelClosedException.class)
                .verify(VERIFY_TIMEOUT);
        } finally {
            channelCache.dispose();
        }
    }

    @Test
    public void shouldEmitChannelClosedErrorOnCacheTermination() {
        final RequestResponseChannelCache channelCache = new RequestResponseChannelCache(mock(ReactorConnection.class),
            CH_ENTITY_PATH, CH_SESSION_NAME, CH_LINK_NAME, retryPolicy);
        final Mono<RequestResponseChannel> channelMono = channelCache.get();
        channelCache.dispose();
        StepVerifier.create(channelMono, 0)
            .thenRequest(1)
            .expectError(RequestResponseChannelClosedException.class)
            .verify(VERIFY_TIMEOUT);
    }

    private MockEndpoint createEndpoint(String connectionId, int maxChannels) {
        return new MockEndpoint(connectionId, CH_ENTITY_PATH, CH_SESSION_NAME, CH_LINK_NAME, maxChannels, retryPolicy);
    }

    private RequestResponseChannelCache createCache(MockEndpoint ep) {
        return new RequestResponseChannelCache(ep.connection(), CH_ENTITY_PATH, CH_SESSION_NAME, CH_LINK_NAME,
            retryPolicy);
    }

    static final class MockEndpoint implements AutoCloseable {
        private static final String FQDN = "contoso-shopping.servicebus.windows.net";
        private final String connectionId;
        private final String chEntityPath;
        private final String chSessionName;
        private final String chLinkName;
        // Describes the endpoint state (EndpointState.*, error, completion) of each channel mock endpoint supplies.
        // If the queue is null, then each 'ReactorConnection::newRequestResponseChannel' returns a new channel with
        // no endpoint state set yet.
        private final Deque<ChannelState> channelStateQueue;
        private final int maxChannels;
        private final AmqpRetryOptions retryOptions;
        private ReactorConnection connection;
        private final MockRequestResponseChannel[] currentChannel = new MockRequestResponseChannel[1];
        private final int[] createChannelCount = new int[1];

        MockEndpoint(String connectionId, String chEntityPath, String chSessionName, String chLinkName, int maxChannels,
            AmqpRetryPolicy retryPolicy) {
            this.connectionId = connectionId;
            this.chEntityPath = chEntityPath;
            this.chSessionName = chSessionName;
            this.chLinkName = chLinkName;
            this.channelStateQueue = null;
            this.maxChannels = maxChannels;
            this.retryOptions = retryPolicy.getRetryOptions();
        }

        MockEndpoint(String connectionId, String chEntityPath, String chSessionName, String chLinkName,
            Deque<ChannelState> channelStateQueue, AmqpRetryPolicy retryPolicy) {
            this.connectionId = connectionId;
            this.chEntityPath = chEntityPath;
            this.chSessionName = chSessionName;
            this.chLinkName = chLinkName;
            this.channelStateQueue = Objects.requireNonNull(channelStateQueue);
            this.maxChannels = channelStateQueue.size();
            this.retryOptions = retryPolicy.getRetryOptions();
        }

        void arrange() {
            connection = mock(ReactorConnection.class);
            when(connection.getId()).thenReturn(connectionId);
            when(connection.getShutdownSignals()).thenReturn(Flux.never());
            when(connection.isDisposed()).thenReturn(false);
            when(connection.closeAsync()).thenReturn(Mono.empty());

            final Deque<MockRequestResponseChannel> channels = new ArrayDeque<>(maxChannels);
            for (int i = 0; i < maxChannels; i++) {
                final MockRequestResponseChannel channel = new MockRequestResponseChannel();
                channel.arrange(connection, FQDN, chLinkName, chEntityPath, retryOptions);
                channels.add(channel);
            }

            when(connection.newRequestResponseChannel(eq(chSessionName), eq(chLinkName), eq(chEntityPath)))
                .then(invocation -> {
                    final MockRequestResponseChannel channel = channels.remove();
                    if (channel == null) {
                        throw new RuntimeException("Attempted to obtain more than max channels:" + maxChannels);
                    }
                    if (currentChannel[0] != null && !currentChannel[0].inner().isDisposed()) {
                        throw new RuntimeException(
                            "Unexpected request for new channel when current one is not disposed.");
                    }
                    if (channelStateQueue != null) {
                        final ChannelState state = Objects.requireNonNull(channelStateQueue.remove());
                        state.apply(channel);
                    }
                    createChannelCount[0]++;
                    currentChannel[0] = channel;
                    return Mono.just(channel.inner());
                });
        }

        ReactorConnection connection() {
            return connection;
        }

        void activateCurrentChannel() {
            Objects.requireNonNull(currentChannel[0]);
            currentChannel[0].emitEndpointState(EndpointState.ACTIVE);
        }

        void completeCurrentChannel() {
            Objects.requireNonNull(currentChannel[0]);
            currentChannel[0].completeEndpointState();
        }

        void errorCurrentChannel(Throwable error) {
            Objects.requireNonNull(currentChannel[0]);
            currentChannel[0].errorEndpointState(error);
        }

        boolean isCurrentChannel(RequestResponseChannel expected) {
            Objects.requireNonNull(currentChannel[0]);
            return currentChannel[0].inner() == expected;
        }

        void assertCurrentChannelClosed() {
            Objects.requireNonNull(currentChannel[0]);
            Assertions.assertTrue(currentChannel[0].inner().isDisposed());
        }

        void assertChannelCreateCount(int expected) {
            Assertions.assertEquals(expected, createChannelCount[0]);
        }

        @Override
        public void close() {
            connection.closeAsync().block();
        }

        // Describes the state of a channel that 'MockEndpoint' utility class provides.
        static final class ChannelState {
            private final boolean never;
            private final boolean complete;
            private final Throwable error;
            private final EndpointState state;

            private ChannelState(boolean never, boolean complete, Throwable error, EndpointState state) {
                this.never = never;
                this.complete = complete;
                this.error = error;
                this.state = state;
            }

            static ChannelState never() {
                return new ChannelState(true, false, null, null);
            }

            static ChannelState complete() {
                return new ChannelState(false, true, null, null);
            }

            static ChannelState error(Throwable error) {
                Objects.requireNonNull(error);
                return new ChannelState(false, false, error, null);
            }

            static ChannelState as(EndpointState state) {
                Objects.requireNonNull(state);
                return new ChannelState(false, false, null, state);
            }

            // Apply the state to the given channel.
            void apply(MockRequestResponseChannel channel) {
                if (complete) {
                    channel.completeEndpointState();
                } else if (error != null) {
                    channel.errorEndpointState(error);
                } else if (state != null) {
                    channel.emitEndpointState(state);
                }
                // else if (never) {
                // NOP (the channel endpoint never emits).
                // }
            }
        }

        private static final class MockRequestResponseChannel {
            private final Sinks.Many<EndpointState> sendLinkStates;
            private final Sinks.Many<EndpointState> receiveLinkStates;
            private RequestResponseChannel channel;

            MockRequestResponseChannel() {
                sendLinkStates = Sinks.many().replay().latestOrDefault(EndpointState.UNINITIALIZED);
                receiveLinkStates = Sinks.many().replay().latestOrDefault(EndpointState.UNINITIALIZED);
            }

            void arrange(ReactorConnection connection, String fqdn, String chLinkName, String chEntityPath,
                AmqpRetryOptions retryOptions) {
                final String connectionId = connection.getId();
                final ReactorDispatcher reactorDispatcher = mock(ReactorDispatcher.class);
                try {
                    doAnswer(invocation -> {
                        final Runnable work = invocation.getArgument(0);
                        work.run();
                        return null;
                    }).when(reactorDispatcher).invoke(any(Runnable.class));
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
                final ReactorProvider reactorProvider = mock(ReactorProvider.class);
                when(reactorProvider.getReactorDispatcher()).thenReturn(reactorDispatcher);

                final Sender sender = mock(Sender.class);
                final Receiver receiver = mock(Receiver.class);
                final Record record = mock(Record.class);
                final Session session = mock(Session.class);
                when(sender.attachments()).thenReturn(record);
                when(receiver.attachments()).thenReturn(record);
                when(session.sender(eq(chLinkName + ":sender"))).thenReturn(sender);
                when(session.receiver(eq(chLinkName + ":receiver"))).thenReturn(receiver);

                final ReceiveLinkHandler2 receiveLinkHandler = mock(ReceiveLinkHandler2.class);
                final SendLinkHandler sendLinkHandler = mock(SendLinkHandler.class);
                when(receiveLinkHandler.getEndpointStates()).thenReturn(receiveLinkStates.asFlux());
                when(sendLinkHandler.getEndpointStates()).thenReturn(sendLinkStates.asFlux());
                final ReactorHandlerProvider handlerProvider = mock(ReactorHandlerProvider.class);
                when(handlerProvider.createReceiveLinkHandler(eq(connectionId), eq(fqdn), eq(chLinkName),
                    eq(chEntityPath), eq(DeliverySettleMode.ACCEPT_AND_SETTLE_ON_DELIVERY), eq(false),
                    eq(reactorDispatcher), eq(retryOptions))).thenReturn(receiveLinkHandler);
                when(receiveLinkHandler.getMessages()).thenReturn(Flux.never());
                when(
                    handlerProvider.createSendLinkHandler(eq(connectionId), eq(fqdn), eq(chLinkName), eq(chEntityPath)))
                        .thenReturn(sendLinkHandler);

                final MessageSerializer serializer = mock(MessageSerializer.class);
                final SenderSettleMode settleMode = SenderSettleMode.SETTLED;
                final ReceiverSettleMode receiverSettleMode = ReceiverSettleMode.SECOND;

                final ProtonChannelWrapper protonChannel
                    = new ProtonChannelWrapper(new ProtonChannel(chLinkName, sender, receiver));
                channel = new RequestResponseChannel(connection, connectionId, fqdn, chEntityPath, protonChannel,
                    retryOptions, handlerProvider, reactorProvider, serializer, settleMode, receiverSettleMode,
                    AmqpMetricsProvider.noop(), true);
            }

            RequestResponseChannel inner() {
                return channel;
            }

            void emitEndpointState(EndpointState endpointState) {
                sendLinkStates.emitNext(endpointState, Sinks.EmitFailureHandler.FAIL_FAST);
                receiveLinkStates.emitNext(endpointState, Sinks.EmitFailureHandler.FAIL_FAST);
            }

            void completeEndpointState() {
                sendLinkStates.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                receiveLinkStates.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            }

            void errorEndpointState(Throwable error) {
                sendLinkStates.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
                receiveLinkStates.emitError(error, Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }
    }
}
