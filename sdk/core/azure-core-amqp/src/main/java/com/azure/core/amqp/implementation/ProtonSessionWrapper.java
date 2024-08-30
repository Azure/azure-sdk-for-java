// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.ProtonSession.ProtonChannel;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

/**
 * A temporary type to support {@link ProtonSession} in v2 or to support direct use of low-level {@link Session}
 * in v1 or v2.
 * <ul>
 *     <li>In v2 mode without "com.azure.core.amqp.cache" opt-in or in v1 mode,
 *     operations are directly performed on Qpid Proton-j low-level {@link Session} instance.</li>
 *     <li>In v2 mode with "com.azure.core.amqp.cache" explicitly opted-in, operations are
 *     performed on ({@link ProtonSession}) instance, that internally delegates to Qpid Proton-j low-level
 *     {@link Session}, but with safety measures.</li>
 * </ul>
 * <p>
 * TODO (anu): remove this temporary type when removing v1 and 'ProtonSession' is no longer opt-in for v2.
 * </p>
 */
public final class ProtonSessionWrapper {
    // Const defined only for log and error message purposes, actual configuration used in ClientBuilder.
    private static final String SESSION_CHANNEL_CACHE_KEY = "com.azure.core.amqp.cache";
    private final Session sessionUnsafe;
    private final ProtonSession session;
    private final SessionHandler handler;
    private final ReactorProvider provider;

    /**
     * Creates session wrapper for v2 client with {@link #SESSION_CHANNEL_CACHE_KEY} opted-in.
     *
     * @param session session to wrap.
     */
    ProtonSessionWrapper(ProtonSession session) {
        this.session = Objects.requireNonNull(session, "'session' cannot be null.");
        this.handler = null;
        this.provider = null;
        this.sessionUnsafe = null;
    }

    /**
     * Creates session wrapper for v2 client without {@link #SESSION_CHANNEL_CACHE_KEY} opted-in or v1 client.
     *
     * @param sessionUnsafe session to wrap.
     * @param handler handler for the session.
     * @param provider the reactor dispatcher provider.
     */
    public ProtonSessionWrapper(Session sessionUnsafe, SessionHandler handler, ReactorProvider provider) {
        this.sessionUnsafe = Objects.requireNonNull(sessionUnsafe, "'sessionUnsafe' cannot be null.");
        this.handler = Objects.requireNonNull(handler, "'handler' cannot be null.");
        this.provider = Objects.requireNonNull(provider, "'provider' cannot be null.");
        this.session = null;
    }

    /**
     * Check if the client is in v2 with {@link #SESSION_CHANNEL_CACHE_KEY} opted-in (hence uses {@link ProtonSession}).
     *
     * @return true if the client is in v2 mode and opted-in for {@link #SESSION_CHANNEL_CACHE_KEY}.
     */
    boolean isV2ClientOnSessionCache() {
        return session != null;
    }

    String getId() {
        if (isV2ClientOnSessionCache()) {
            return session.getId();
        } else {
            return handler.getId();
        }
    }

    String getName() {
        if (isV2ClientOnSessionCache()) {
            return session.getName();
        } else {
            return handler.getSessionName();
        }
    }

    String getConnectionId() {
        if (isV2ClientOnSessionCache()) {
            return session.getConnectionId();
        } else {
            return handler.getConnectionId();
        }
    }

    String getHostname() {
        if (isV2ClientOnSessionCache()) {
            return session.getHostname();
        } else {
            return handler.getHostname();
        }
    }

    Flux<EndpointState> getEndpointStates() {
        if (isV2ClientOnSessionCache()) {
            return session.getEndpointStates();
        } else {
            return handler.getEndpointStates();
        }
    }

    ReactorProvider getReactorProvider() {
        if (isV2ClientOnSessionCache()) {
            return session.getReactorProvider();
        } else {
            return provider;
        }
    }

    AmqpErrorContext getErrorContext() {
        if (isV2ClientOnSessionCache()) {
            return session.getErrorContext();
        } else {
            return handler.getErrorContext();
        }
    }

    void openUnsafe(ClientLogger logger) {
        if (isV2ClientOnSessionCache()) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                "Requires v2 client without " + SESSION_CHANNEL_CACHE_KEY + " or v1 client."));
        }
        sessionUnsafe.open();
    }

    Mono<Void> open() {
        if (!isV2ClientOnSessionCache()) {
            return Mono.error(
                new UnsupportedOperationException("open() requires v2 client with " + SESSION_CHANNEL_CACHE_KEY));
        }
        return session.open();
    }

    Mono<ProtonChannelWrapper> channel(String name, Duration timeout) {
        if (isV2ClientOnSessionCache()) {
            return session.channel(name, timeout).map(ProtonChannelWrapper::new);
        } else {
            return Mono.just(new ProtonChannelWrapper(name, sessionUnsafe));
        }
    }

    Sender senderUnsafe(String name) {
        if (isV2ClientOnSessionCache()) {
            return session.senderUnsafe(name);
        } else {
            return sessionUnsafe.sender(name);
        }
    }

    Receiver receiverUnsafe(String name) {
        if (isV2ClientOnSessionCache()) {
            return session.receiverUnsafe(name);
        } else {
            return sessionUnsafe.receiver(name);
        }
    }

    void beginClose(ErrorCondition condition) {
        if (isV2ClientOnSessionCache()) {
            session.beginClose(condition);
        } else {
            if (sessionUnsafe.getLocalState() != EndpointState.CLOSED) {
                sessionUnsafe.close();
                if (condition != null && sessionUnsafe.getCondition() == null) {
                    sessionUnsafe.setCondition(condition);
                }
            }
        }
    }

    void endClose() {
        if (isV2ClientOnSessionCache()) {
            session.endClose();
        } else {
            handler.close();
        }
    }

    /**
     * A temporary type to represent a channel obtained from the {@link ProtonSessionWrapper}.
     * <p>
     * TODO (anu): remove this temporary type when removing parent ProtonSessionWrapper type.
     * </p>
     */
    static final class ProtonChannelWrapper {
        private final String name;
        private final Sender sender;
        private final Receiver receiver;

        /**
         * Creates channel wrapper for v2 client with {@link #SESSION_CHANNEL_CACHE_KEY} opted-in
         * (hence uses {@link ProtonChannel} in {@link ProtonSession}).
         *
         * @param channel the channel to wrap.
         */
        ProtonChannelWrapper(ProtonChannel channel) {
            Objects.requireNonNull(channel, "'channel' cannot be null.");
            this.name = channel.getName();
            this.sender = channel.getSender();
            this.receiver = channel.getReceiver();
        }

        /**
         * Creates channel wrapper for v2 client without {@link #SESSION_CHANNEL_CACHE_KEY} opted-in or V1 client.
         *
         * @param name the name of the channel.
         * @param sessionUnsafe the session to host the sender and receiver in the channel
         */
        ProtonChannelWrapper(String name, Session sessionUnsafe) {
            this.name = Objects.requireNonNull(name, "'name' cannot be null.");
            Objects.requireNonNull(sessionUnsafe, "'sessionUnsafe' cannot be null.");
            // In current V2 (that doesn't have ProtonSession) or V1, the RequestResponseChannel's sender and receiver
            // gets created in the calling thread. Continue to do the same here in wrapper (i.e, no behavioral change).
            this.sender = sessionUnsafe.sender(name + ":sender");
            this.receiver = sessionUnsafe.receiver(name + ":receiver");
        }

        String getName() {
            return name;
        }

        Sender sender() {
            return sender;
        }

        Receiver receiver() {
            return receiver;
        }
    }
}
