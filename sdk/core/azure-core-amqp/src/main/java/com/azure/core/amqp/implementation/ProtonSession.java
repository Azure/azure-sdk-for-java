// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.amqp.exception.AmqpErrorCondition.TIMEOUT_ERROR;
import static com.azure.core.amqp.implementation.ClientConstants.SESSION_ID_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.SESSION_NAME_KEY;
import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

/**
 * A type for managing and abstracting operations on a QPid Proton-j low-level {@link Session} instance.
 */
final class ProtonSession {
    private static final String SESSION_NOT_OPENED = "session has not been opened.";
    private static final String NOT_OPENING_DISPOSED_SESSION = "session is already disposed, not opening.";
    private static final String DISPOSED_MESSAGE_FORMAT = "Cannot create %s from a closed session.";
    private static final String REACTOR_CLOSED_MESSAGE_FORMAT
        = "connectionId:[%s] sessionName:[%s] connection-reactor is disposed.";
    private static final String OBTAIN_CHANNEL_TIMEOUT_MESSAGE_FORMAT
        = "connectionId:[%s] sessionName:[%s] obtaining channel (%s) timed out.";
    private final AtomicReference<Resource> resource = new AtomicReference<>(Resource.EMPTY);
    private final AtomicBoolean opened = new AtomicBoolean(false);
    private final Sinks.Empty<Void> openAwaiter = Sinks.empty();
    private final Connection connection;
    private final ReactorProvider reactorProvider;
    private final SessionHandler handler;
    private final String id;
    private final ClientLogger logger;

    /**
     * Creates a ProtonSession.
     *
     * @param connectionId the id of the QPid Proton-j Connection hosting the session.
     * @param hostname the host name of the broker that the QPid Proton-j Connection hosting
     *  the session is connected to.
     * @param connection the QPid Proton-j Connection hosting the session.
     * @param handlerProvider the handler provider for various type of endpoints (session, link).
     * @param reactorProvider the provider for reactor dispatcher.
     * @param sessionName the session name.
     * @param openTimeout the session open timeout.
     * @param logger the client logger.
     */
    ProtonSession(String connectionId, String hostname, Connection connection, ReactorHandlerProvider handlerProvider,
        ReactorProvider reactorProvider, String sessionName, Duration openTimeout, ClientLogger logger) {
        this.connection = Objects.requireNonNull(connection, "'connection' cannot be null.");
        this.reactorProvider = Objects.requireNonNull(reactorProvider, "'reactorProvider' cannot be null.");
        Objects.requireNonNull(handlerProvider, "'handlerProvider' cannot be null.");
        this.handler = handlerProvider.createSessionHandler(connectionId, hostname, sessionName, openTimeout);
        this.id = handler.getId();
        this.logger = Objects.requireNonNull(logger, "'logger' cannot be null.");
    }

    /**
     * Gets the id, useful for the logging purposes.
     *
     * @return the id.
     */
    String getId() {
        return id;
    }

    /**
     * Gets the session name.
     *
     * @return the session name.
     */
    String getName() {
        return handler.getSessionName();
    }

    /**
     * Gets the identifier of the QPid Proton-j Connection hosting the session.
     *
     * @return the connection identifier.
     */
    String getConnectionId() {
        return handler.getConnectionId();
    }

    /**
     * Gets the host name of the broker that the QPid Proton-j Connection facilitating the session
     * is connected to.
     *
     * @return the hostname.
     */
    String getHostname() {
        return handler.getHostname();
    }

    /**
     * Gets the error context of the session.
     *
     * @return the error context.
     */
    AmqpErrorContext getErrorContext() {
        return handler.getErrorContext();
    }

    /**
     * Gets the connectivity states of the session.
     *
     * @return the session's connectivity states.
     */
    Flux<EndpointState> getEndpointStates() {
        return handler.getEndpointStates();
    }

    /**
     * Gets the reactor dispatcher provider associated with the session.
     * <p>
     * Any operation (e.g., obtaining sender, receiver) on the session must be invoked on the reactor dispatcher.
     * </p>
     *
     * @return the reactor dispatcher provider.
     */
    ReactorProvider getReactorProvider() {
        return reactorProvider;
    }

    /**
     * Opens the session in the QPid Proton-j Connection.
     * <p>
     * The session open attempt is made upon the first subscription, once opened later subscriptions will complete
     * immediately, i.e. there is an open-only-once semantics.
     * </p>
     * <p>
     * If the session (or parent Qpid Proton-j connection) is disposed after opening, any later operation attempts
     * (e.g., creating sender, receiver, channel) will fail.
     * </p>
     * <p>
     * By design, no re-open attempt will be made within this type. Lifetime of a {@link ProtonSession} instance is
     * scoped to life time of one low level Qpid Proton-j session instance it manages, which is scoped within the
     * life time of qpid Proton-j Connection hosting it. Re-establishing session requires querying the connection-cache
     * to obtain the latest connection (may not be same as the connection facilitated this session) then hosting and
     * opening a new {@link ProtonSession} on it. It means, upon a retriable {@link AmqpException} from any APIs (to
     * create sender, receiver, channel) in this type, the call sites needs to obtain a new {@link ProtonSession}
     * by polling the connection-cache.
     * </p>
     *
     * @return a mono that completes once the session is opened.
     * <p>
     * <ul>the mono can terminates with retriable {@link AmqpException} if
     *      <li>the session disposal happened while opening,</li>
     *      <li>or the connection reactor thread got shutdown while opening.</li>
     * </ul>
     * </p>
     */
    Mono<Void> open() {
        if (opened.getAndSet(true)) {
            return openAwaiter.asMono();
        }
        try {
            getReactorProvider().getReactorDispatcher().invoke(() -> {
                final Session session = connection.session();
                BaseHandler.setHandler(session, handler);
                session.open();
                logger.atInfo()
                    .addKeyValue(SESSION_NAME_KEY, handler.getSessionName())
                    .addKeyValue(SESSION_ID_KEY, id)
                    .log("session local open scheduled.");

                if (resource.compareAndSet(Resource.EMPTY, new Resource(session))) {
                    openAwaiter.emitEmpty(FAIL_FAST);
                } else {
                    session.close();
                    if (resource.get() == Resource.DISPOSED) {
                        openAwaiter.emitError(new ProtonSessionClosedException(NOT_OPENING_DISPOSED_SESSION),
                            FAIL_FAST);
                    } else {
                        openAwaiter.emitError(new IllegalStateException("session is already opened."), FAIL_FAST);
                    }
                }
            });
        } catch (Exception e) {
            if (e instanceof IOException | e instanceof RejectedExecutionException) {
                final String message = String.format(REACTOR_CLOSED_MESSAGE_FORMAT, getConnectionId(), getName());
                openAwaiter.emitError(retriableAmqpError(null, message, e), FAIL_FAST);
            } else {
                openAwaiter.emitError(e, FAIL_FAST);
            }
        }
        return openAwaiter.asMono();
    }

    /**
     * Gets a bidirectional channel on the session.
     * <p>
     * A channel consists of a sender link and a receiver link, and is used for management operations where a sender
     * link is used to send a request to the broker and a receiver link gets the associated response from the broker.
     * </p>
     *
     * @param name the channel name, which is used as the name prefix for sender link and receiver link in the channel.
     * @param timeout the timeout for obtaining the channel.
     *
     * @return a mono that completes with a {@link ProtonChannel} once the channel is created in the session.
     * <p>
     *  <ul>
     *      <li>the mono terminates with {@link IllegalStateException} if the session is not opened yet via {@link #open()}.</li>
     *      <li>the mono terminates with
     *          <ul>
     *              <li>a retriable {@link ProtonSessionClosedException} if the session is disposed,</li>
     *              <li>a retriable {@link AmqpException} if obtaining channel timeout,</li>
     *              <li>or a retriable {@link AmqpException} if the connection reactor thread is shutdown.</li>
     *          </ul>
     *      </li>
     *  </ul>
     * </p>
     */
    Mono<ProtonChannel> channel(final String name, Duration timeout) {
        final Mono<ProtonChannel> channel = Mono.create(sink -> {
            if (name == null) {
                sink.error(new NullPointerException("'name' cannot be null."));
                return;
            }
            try {
                // Check to see if call can "fail fast in the current thread" before scheduling the work to QPid
                // Reactor thread. Call can still encounter error by the time QPid Reactor thread picks the work,
                // but a potential unnecessary thread-hopping-cost can be saved with this simple entry time check.
                resource.get().validate(logger, "channel");
            } catch (ProtonSessionClosedException | IllegalStateException e) {
                sink.error(e);
                return;
            }
            try {
                getReactorProvider().getReactorDispatcher().invoke(() -> {
                    final Session session;
                    try {
                        session = getSession("channel");
                    } catch (ProtonSessionClosedException | IllegalStateException e) {
                        sink.error(e);
                        return;
                    }
                    final String senderName = name + ":sender";
                    final String receiverName = name + ":receiver";
                    sink.success(new ProtonChannel(name, session.sender(senderName), session.receiver(receiverName)));
                });
            } catch (Exception e) {
                if (e instanceof IOException | e instanceof RejectedExecutionException) {
                    final String message = String.format(REACTOR_CLOSED_MESSAGE_FORMAT, getConnectionId(), getName());
                    sink.error(retriableAmqpError(null, message, e));
                } else {
                    sink.error(e);
                }
            }
        });
        // TODO (anu): when removing v1 support, move the timeout to the call site, ReactorSession::channel(), so it
        //  aligns with the placement of timeout for ReactorSession::open().
        return channel.timeout(timeout, Mono.error(() -> {
            final String message
                = String.format(OBTAIN_CHANNEL_TIMEOUT_MESSAGE_FORMAT, getConnectionId(), getName(), name);
            return retriableAmqpError(TIMEOUT_ERROR, message, null);
        }));
    }

    /**
     * Gets a QPid Proton-j sender on the session.
     * <p>
     * The call site required to invoke this method on Reactor dispatcher thread using {@link #getReactorProvider()}.
     * It is possible to run into race conditions with QPid Proton-j if invoked from any other threads.
     * </p>
     *
     * @param name the sender name.
     *
     * @return the sender.
     * @throws IllegalStateException if the attempt to obtain the sender was made before opening the session.
     * @throws ProtonSessionClosedException (a retriable {@link AmqpException}) if the session was disposed.
     */
    Sender senderUnsafe(String name) {
        final Session session = getSession("sender link");
        return session.sender(name);
    }

    /**
     * Gets a QPid Proton-j receiver on the session.
     * <p>
     * The call site required to invoke this method on Reactor dispatcher thread using {@link #getReactorProvider()}.
     * It is possible to run into race conditions with QPid Proton-j if invoked from any other threads.
     * </p>
     *
     * @param name the sender name.
     *
     * @return the sender.
     * @throws IllegalStateException if the attempt to obtain the receiver was made before opening the session.
     * @throws ProtonSessionClosedException a retriable {@link AmqpException}) if the session was disposed.
     */
    Receiver receiverUnsafe(String name) {
        final Session session = getSession("receive link");
        return session.receiver(name);
    }

    /**
     * Begin the disposal by locally closing the underlying QPid Proton-j session.
     *
     * @param errorCondition the error condition to close the session with.
     */
    void beginClose(ErrorCondition errorCondition) {
        final Resource s = resource.getAndSet(Resource.DISPOSED);
        if (s == Resource.EMPTY || s == Resource.DISPOSED) {
            return;
        }
        final Session session = s.value();
        if (session.getLocalState() != EndpointState.CLOSED) {
            session.close();
            if (errorCondition != null && session.getCondition() == null) {
                session.setCondition(errorCondition);
            }
        }
    }

    /**
     * Completes the disposal by closing the {@link SessionHandler}.
     */
    void endClose() {
        handler.close();
    }

    /**
     * Obtain the underlying QPid Proton-j {@link Session} atomically.
     *
     * @param endpointType the type of the endpoint (e.g., sender, receiver, channel) that the call site want to host
     *  on the obtained session. The provided string value is used only to form error message for any exception.
     *
     * @return the QPid Proton-j session.
     * @throws IllegalStateException if the attempt to obtain the session was made before opening via {@link #open()}.
     * @throws ProtonSessionClosedException a retriable {@link AmqpException}) if the session was disposed.
     */
    private Session getSession(String endpointType) {
        final Resource r = resource.get();
        r.validate(logger, endpointType);
        return r.value();
    }

    /**
     * Creates a retriable AMQP exception.
     * <p>
     * The call sites uses this method to translate a session unavailability "event" (session disposed, session operation
     * timed-out or connection being closed) to a retriable error. While the "event" is transient, recovering from it
     * by creating a new session on current connection or on a new connection needs to be done not by this class or
     * the parent 'ReactorConnection' owning this ProtonSession but by the downstream. E.g., the downstream Consumer,
     * Producer Client that has access to the chain to propagate retry request to top level V2 ReactorConnectionCache.
     * </p>
     * @param condition the error condition.
     * @param message the error message.
     * @param cause the actual cause of error.
     * @return the retriable AMQP exception.
     */
    private AmqpException retriableAmqpError(AmqpErrorCondition condition, String message, Throwable cause) {
        return new AmqpException(true, condition, message, cause, handler.getErrorContext());
    }

    /**
     * Type representing a bidirectional channel hosted on the QPid Proton-j session.
     * <p>
     * The {@link RequestResponseChannel} underneath uses an instance of {@link ProtonChannel} for the bi-directional
     * communication with the broker.
     * </p>
     */
    static final class ProtonChannel {
        private final String name;
        private final Sender sender;
        private final Receiver receiver;

        /**
         * Creates a ProtonChannel.
         *
         * @param name the channel name.
         * @param sender the sender endpoint of the channel.
         * @param receiver the receiver endpoint of the channel.
         */
        ProtonChannel(String name, Sender sender, Receiver receiver) {
            this.name = name;
            this.sender = sender;
            this.receiver = receiver;
        }

        /**
         * Gets the channel name.
         *
         * @return the channel name.
         */
        String getName() {
            return name;
        }

        /**
         * Gets the sender endpoint of the channel.
         *
         * @return the channel's sender endpoint.
         */
        Sender getSender() {
            return sender;
        }

        /**
         * Gets the receiver endpoint of the channel.
         *
         * @return the channel's receiver endpoint.
         */
        Receiver getReceiver() {
            return receiver;
        }
    }

    /**
     * A retriable {@link AmqpException} indicating session is disposed.
     */
    static final class ProtonSessionClosedException extends AmqpException {
        private ProtonSessionClosedException(String message) {
            super(true, message, null);
        }
    }

    /**
     * A type to store the underlying QPid Proton-j {@link Session} that the {@link ProtonSession} manages.
     * The {@link ProtonSession} access this resource atomically.
     */
    private static final class Resource {
        private static final Resource EMPTY = new Resource();
        private static final Resource DISPOSED = new Resource();
        private final Session session;

        /**
         * Creates a Resource initialized with a QPid Proton-j {@link Session}.
         *
         * @param session the session.
         */
        Resource(Session session) {
            this.session = Objects.requireNonNull(session, "'session' cannot be null.");
        }

        /**
         * Gets the underlying QPid Proton-j {@link Session}.
         *
         * @return the session.
         */
        Session value() {
            assert this != EMPTY;
            return this.session;
        }

        /**
         * Check that if resource is in a valid state i.e., if it holds a session. An error is thrown if the resource
         * is not initialized with a session or disposed.
         *
         * @param logger the logger to log the error in case of invalid state.
         * @param endpointType the type of the endpoint (e.g., sender, receiver, channel) that the call site want to
         * host on the underlying session. The provided string value is used only to form error message.
         * @throws IllegalStateException if the resource is not initialized with a session.
         * @throws ProtonSessionClosedException if the resource is disposed.
         */
        void validate(ClientLogger logger, String endpointType) {
            if (this == Resource.EMPTY) {
                throw logger.logExceptionAsError(new IllegalStateException(SESSION_NOT_OPENED));
            }
            if (this == Resource.DISPOSED) {
                throw logger.logExceptionAsWarning(
                    new ProtonSessionClosedException(String.format(DISPOSED_MESSAGE_FORMAT, endpointType)));
            }
        }

        /**
         * Constructor for the static EMPTY and DISPOSED state.
         */
        private Resource() {
            this.session = null;
        }
    }
}
