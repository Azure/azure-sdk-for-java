// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.ProtonSession.ProtonChannel;
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
    private final ProtonSession session;

    /**
     * Creates session wrapper.
     *
     * @param session session to wrap.
     */
    public ProtonSessionWrapper(ProtonSession session) {
        this.session = Objects.requireNonNull(session, "'session' cannot be null.");
    }

    String getId() {
        return session.getId();
    }

    String getName() {
        return session.getName();
    }

    String getConnectionId() {
        return session.getConnectionId();
    }

    String getHostname() {
        return session.getHostname();
    }

    Flux<EndpointState> getEndpointStates() {
        return session.getEndpointStates();
    }

    ReactorProvider getReactorProvider() {
        return session.getReactorProvider();
    }

    AmqpErrorContext getErrorContext() {
        return session.getErrorContext();
    }

    Mono<Void> open() {
        return session.open();
    }

    Mono<ProtonChannelWrapper> channel(String name, Duration timeout) {
        return session.channel(name, timeout).map(ProtonChannelWrapper::new);
    }

    Sender senderUnsafe(String name) {
        return session.senderUnsafe(name);
    }

    Receiver receiverUnsafe(String name) {
        return session.receiverUnsafe(name);
    }

    void beginClose(ErrorCondition condition) {
        session.beginClose(condition);
    }

    void endClose() {
        session.endClose();
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
         * Creates channel wrapper.
         *
         * @param channel the channel to wrap.
         */
        ProtonChannelWrapper(ProtonChannel channel) {
            Objects.requireNonNull(channel, "'channel' cannot be null.");
            this.name = channel.getName();
            this.sender = channel.getSender();
            this.receiver = channel.getReceiver();
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
