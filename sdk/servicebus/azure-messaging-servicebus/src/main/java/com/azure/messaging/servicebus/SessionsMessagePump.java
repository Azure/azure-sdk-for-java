// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.CreditFlowMode;
import com.azure.core.amqp.implementation.MessageFlux;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.handler.DeliveryNotOnLinkException;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addSignalTypeAndResult;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MESSAGE_ID_LOGGING_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * An abstraction to pump messages from multiple sessions.
 * <p/>
 * The pump will be connected to at most {@code maxConcurrentSessions} sessions at a time, and streams messages from
 * each of those sessions. Within a session, {@code concurrencyPerSession} messages are pumped in parallel.
 * <p/>
 * If a session gets terminated for some reason, a link to the next available session will be acquired using
 * {@link ServiceBusSessionLinkAcquirer}, and system will roll to the new session to continue streaming messages.
 * The inner abstraction SessionsMessagePump#RollingSessionReceiver is responsible for managing one session
 * (called current session), rolling to the next session when current session terminates and concurrently pumping
 * messages from its current session.
 * <p/>
 * The {@link SessionsMessagePump} manages {@code maxConcurrentSessions} SessionsMessagePump#RollingSessionReceiver
 * instances at a time.
 * <p/>
 * The pumping starts upon the invocation of {@link SessionsMessagePump#begin} and subscribing to the Mono it returns.
 * The pumping can be explicitly terminated by invoking {@link SessionsMessagePump#closeAsync} and subscribing to
 * the Mono it returns.
 * <p/>
 * If a SessionsMessagePump#RollingSessionReceiver instance encounters an error when it attempts to obtain a new link
 * from {@link ServiceBusSessionLinkAcquirer}, all other SessionsMessagePump#RollingSessionReceiver instance will be
 * canceled, and pumping terminates. At this point {@link SessionsMessagePump} itself is considered as terminated.
 * The termination signal will be notified through the Mono returned by {@link SessionsMessagePump#begin} that originally
 * started the pumping.
 * <p/>
 * Once {@link SessionsMessagePump} is terminated (explicitly or implicitly), to restart the pumping a new
 * {@link SessionsMessagePump} instance should be obtained.
 */
final class SessionsMessagePump implements AsyncCloseable {
    private static final ArrayList<RollingSessionReceiver> EMPTY = new ArrayList<>(0);
    private static final ArrayList<RollingSessionReceiver> TERMINATED = new ArrayList<>(0);
    private final ClientLogger logger;
    private final String identifier;
    private final String fqdn;
    private final String entityPath;
    private final ServiceBusReceiverInstrumentation instrumentation;
    private final Duration maxSessionLockRenew;
    private final Duration sessionIdleTimeout;
    private final int maxConcurrentSessions;
    private final int concurrencyPerSession;
    private final int prefetch;
    private final boolean enableAutoDisposition;
    private final Mono<ServiceBusManagementNode> managementNode;
    private final MessageSerializer serializer;
    private final AmqpRetryPolicy retryPolicy;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final AtomicReference<List<RollingSessionReceiver>> rollingReceiversRef = new AtomicReference<>(EMPTY);
    private final SessionReceiversTracker receiversTracker;
    private final Mono<ServiceBusReceiveLink> nextSessionLink;
    private final Sinks.Empty<Void> awaitTerminatingMono = Sinks.empty();

    SessionsMessagePump(String identifier, String fqdn, String entityPath, ServiceBusReceiveMode receiveMode,
        ServiceBusReceiverInstrumentation instrumentation, ServiceBusSessionLinkAcquirer linkAcquirer,
        Duration maxSessionLockRenew, Duration sessionIdleTimeout, int maxConcurrentSessions, int concurrencyPerSession,
        int prefetch, boolean enableAutoDisposition, Mono<ServiceBusManagementNode> managementNode,
        MessageSerializer serializer, AmqpRetryPolicy retryPolicy,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError) {
        final Map<String, Object> loggingContext = new HashMap<>(2);
        loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, fqdn);
        loggingContext.put(ENTITY_PATH_KEY, entityPath);
        this.logger = new ClientLogger(SessionsMessagePump.class, loggingContext);
        this.identifier = identifier;
        this.fqdn = Objects.requireNonNull(fqdn, "'fqdn' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(receiveMode, "'receiveMode' cannot be null.");
        this.instrumentation = Objects.requireNonNull(instrumentation, "'instrumentation' cannot be null");
        Objects.requireNonNull(linkAcquirer, "'linkAcquirer' cannot be null");
        this.maxSessionLockRenew = Objects.requireNonNull(maxSessionLockRenew, "'maxSessionLockRenew' cannot be null.");
        this.sessionIdleTimeout = sessionIdleTimeout;
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.concurrencyPerSession = concurrencyPerSession;
        this.prefetch = prefetch;
        this.enableAutoDisposition = enableAutoDisposition;
        this.managementNode = Objects.requireNonNull(managementNode, "'managementNode' cannot be null.");
        this.serializer = Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null.");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null.");
        this.receiversTracker = new SessionReceiversTracker(logger, maxConcurrentSessions, fqdn, entityPath, receiveMode);
        this.nextSessionLink = new NextSessionLink(linkAcquirer).mono();
    }

    public String getIdentifier() {
        return identifier;
    }

    public Mono<Void> begin() {
        final Mono<List<RollingSessionReceiver>> initReceiversMono = Mono.fromSupplier(() -> {
            throwIfTerminatedOrInitialized(rollingReceiversRef.get());

            final List<RollingSessionReceiver> rollingReceivers = createRollingSessionReceivers();
            if (!rollingReceiversRef.compareAndSet(EMPTY, rollingReceivers)) {
                // A concurrent call already began the pumping, lets cleanup and throw.
                for (RollingSessionReceiver rollingReceiver : rollingReceivers) {
                    // No AmqpLinks were created or associated wire calls were made at this point. So the closeAsync()
                    // calls runs synchronously, disposing the Scheduler associated with each RollingSessionReceiver.
                    rollingReceiver.closeAsync().subscribe();
                }
                throwIfTerminatedOrInitialized(rollingReceiversRef.get());
            }
            return rollingReceivers;
        });

        final Function<List<RollingSessionReceiver>, Mono<Void>> pumpFromReceiversMono = rollingReceivers -> {
            final List<Mono<Void>> pumpingMono = new ArrayList<>(rollingReceivers.size());
            for (RollingSessionReceiver rollingReceiver : rollingReceivers) {
                pumpingMono.add(rollingReceiver.receive());
            }
            return Mono.when(pumpingMono);
        };

        return Mono.usingWhen(initReceiversMono, pumpFromReceiversMono,
            (__) -> closeAsync(TerminationReason.COMPLETED),
            (__, e) -> closeAsync(TerminationReason.ERRORED),
            (__) -> closeAsync(TerminationReason.CANCELED));
    }

    @Override
    public Mono<Void> closeAsync() {
        return closeAsync(TerminationReason.CLOSED);
    }

    private Mono<Void> closeAsync(TerminationReason reason) {
        final List<RollingSessionReceiver> rollingReceivers = this.rollingReceiversRef.getAndSet(TERMINATED);
        if (rollingReceivers == TERMINATED) {
            return awaitTerminatingMono.asMono().publishOn(Schedulers.boundedElastic());
        }
        logger.atInfo().log("Pump terminated. Reason:" + reason);
        receiversTracker.clear();
        final List<Mono<Void>> closingMono = new ArrayList<>(rollingReceivers.size());
        for (RollingSessionReceiver rollingReceiver : rollingReceivers) {
            closingMono.add(rollingReceiver.closeAsync());
        }
        return Mono.whenDelayError(closingMono)
            .doOnNext(s -> {
                awaitTerminatingMono.emitEmpty((signalType, result) -> {
                    addSignalTypeAndResult(logger.atWarning(), signalType, result)
                        .log("Unable to emit terminating signal.");
                    return false;
                });
            });
    }

    private static void throwIfTerminatedOrInitialized(List<RollingSessionReceiver> l) {
        if (l == TERMINATED) {
            throw new TerminatedException("sessions-message-pump");
        }
        if (l != EMPTY) {
            throw new UnsupportedOperationException("Cannot invoke begin() more than once.");
        }
    }

    private List<RollingSessionReceiver> createRollingSessionReceivers() {
        final ArrayList<RollingSessionReceiver> rollingReceivers = new ArrayList<>(maxConcurrentSessions);
        for (int rolllerId = 0; rolllerId < maxConcurrentSessions; rolllerId++) {
            final RollingSessionReceiver rollingReceiver = new RollingSessionReceiver(rolllerId, instrumentation,
                fqdn, entityPath, nextSessionLink, maxSessionLockRenew, sessionIdleTimeout, concurrencyPerSession,
                prefetch, enableAutoDisposition, managementNode, serializer, retryPolicy, processMessage, processError,
                receiversTracker);
            rollingReceivers.add(rollingReceiver);
        }
        return rollingReceivers;
    }

    private static final class NextSessionLink
        implements Supplier<Mono<ServiceBusReceiveLink>> {
        private final AtomicReference<Boolean> isTerminated = new AtomicReference<>(false);
        private final ServiceBusSessionLinkAcquirer linkAcquirer;

        NextSessionLink(ServiceBusSessionLinkAcquirer linkAcquirer) {
            this.linkAcquirer = linkAcquirer;
        }

        Mono<ServiceBusReceiveLink> mono() {
            final Supplier<Mono<ServiceBusReceiveLink>> supplier = this;
            return Mono.defer(supplier);
        }

        @Override
        public Mono<ServiceBusReceiveLink> get() {
            if (isTerminated.get()) {
                return Mono.error(new TerminatedException("acquire-link"));
            }
            return linkAcquirer.acquire()
                .onErrorMap(e -> {
                    isTerminated.set(true);
                    return new TerminatedException("acquire-link", e);
                });
        }
    }

    private static final class RollingSessionReceiver
        extends AtomicReference<State<ServiceBusSessionReactorReceiver>>
        implements AsyncCloseable {
        private static final String ROLLER_ID_KEY = "roller-id";
        private static final State<ServiceBusSessionReactorReceiver> TERMINATED = State.terminated();
        private final ClientLogger logger;
        private final String fqdn;
        private final String entityPath;
        private final int concurrency;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final boolean enableAutoDisposition;
        private final Duration maxSessionLockRenew;
        private final Duration sessionIdleTimeout;
        private final Mono<ServiceBusManagementNode> managementNode;
        private final MessageSerializer serializer;
        private final ServiceBusReceiverInstrumentation instrumentation;
        private final ServiceBusTracer tracer;
        private final SessionReceiversTracker receiversTracker;
        private final SessionLinkStream sessionLinkStream;
        private final  MessageFlux messageFlux;
        private final Scheduler workerScheduler;

        RollingSessionReceiver(int rollerId, ServiceBusReceiverInstrumentation instrumentation, String fqdn, String entityPath,
            Mono<ServiceBusReceiveLink> nextSessionLink, Duration maxSessionLockRenew, Duration sessionIdleTimeout,
            int concurrency, int prefetch, boolean enableAutoDisposition, Mono<ServiceBusManagementNode> managementNode,
            MessageSerializer serializer, AmqpRetryPolicy retryPolicy,
            Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
            SessionReceiversTracker receiversTracker) {
            super(null);
            final Map<String, Object> loggingContext = new HashMap<>(3);
            loggingContext.put(ROLLER_ID_KEY, rollerId);
            loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, fqdn);
            loggingContext.put(ENTITY_PATH_KEY, entityPath);
            this.logger = new ClientLogger(RollingSessionReceiver.class, loggingContext);
            this.fqdn = fqdn;
            this.entityPath = entityPath;
            this.concurrency = concurrency;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoDisposition = enableAutoDisposition;
            this.maxSessionLockRenew = maxSessionLockRenew;
            this.sessionIdleTimeout = sessionIdleTimeout;
            this.managementNode = managementNode;
            this.serializer = serializer;
            this.instrumentation = instrumentation;
            this.tracer = instrumentation.getTracer();
            this.receiversTracker = receiversTracker;
            this.sessionLinkStream = new SessionLinkStream(nextSessionLink);
            final Flux<ServiceBusSessionReactorReceiver> messageFluxUpstream = sessionLinkStream.flux()
                .map(this::nextSessionReceiver);
            this.messageFlux = new MessageFlux(messageFluxUpstream, prefetch, CreditFlowMode.RequestDriven, retryPolicy);
            if (concurrency > 1) {
                this.workerScheduler = Schedulers.newBoundedElastic(DEFAULT_BOUNDED_ELASTIC_SIZE,
                    DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "rolling-session-receiver-" + rollerId);
            } else {
                this.workerScheduler = Schedulers.immediate();
            }
        }

        Mono<Void> receive() {
            return Mono.usingWhen(
                Mono.fromSupplier(() -> this.messageFlux),
                flux -> {
                    return flux.flatMap(new RunOnWorker(this::handleMessage, workerScheduler), concurrency, 1).then();
                },
                (__) -> closeAsync(TerminationReason.COMPLETED),
                (__, e) -> closeAsync(TerminationReason.ERRORED),
                (__) -> closeAsync(TerminationReason.CANCELED)
            );
        }

        @Override
        public Mono<Void> closeAsync() {
            return closeAsync(TerminationReason.CLOSED);
        }

        private Mono<Void> closeAsync(TerminationReason reason) {
            logger.atInfo().log("Roller terminated. Reason:" + reason);
            sessionLinkStream.close();
            final Mono<Void> closesState = Mono.defer(() -> {
                final State<ServiceBusSessionReactorReceiver> state = super.getAndSet(TERMINATED);
                return state.closeAsync();
            });
            return closesState
                .doFinally(__ -> {
                    workerScheduler.dispose();
                }).then();
        }

        private ServiceBusSessionReactorReceiver nextSessionReceiver(SessionLinkStream.Next nextLink) {
            final State<ServiceBusSessionReactorReceiver> lastState = super.get();
            if (lastState == TERMINATED) {
                nextLink.closeAsync().subscribe();
                throw new TerminatedException("rolling-session-receiver");
            }
            final String sessionId = nextLink.sessionId;
            final ServiceBusReceiveLink sessionLink = nextLink.sessionLink;
            final ServiceBusSessionReactorReceiver nextReceiver = new ServiceBusSessionReactorReceiver(
                logger, tracer, managementNode, sessionId, sessionLink, maxSessionLockRenew, sessionIdleTimeout);
            if (!super.compareAndSet(lastState, new State<>(nextReceiver))) {
                // 1. The 'super.getAndSet(DISPOSED)' in the closeAsync won the race with the above 'super.compareAndSet'.
                // 2. Multiple 'super.compareAndSet' will never race each other since -
                //      2.1 There will be only one Subscription from 'MessageFlux' throughout RollingSessionReceiver lifetime.
                //      2.2 Spec: nextSessionReceiver runs triggered by the requests made on that Subscription are 'serialized'.
                //
                //      Hence, we WON’T run into the case of – two threads (T1, T2) invoking nextSessionReceiver concurrently,
                //      each creating ServiceBusSessionReactorReceiver and 'compareAndSet' of T1 winning the race with T’s
                //      'compareAndSet' resulting in T1 not closing its ServiceBusSessionReactorReceiver.
                //
                nextReceiver.closeAsync().subscribe();
                throw new TerminatedException("rolling-session-receiver");
            }
            if (lastState != null) {
                final ServiceBusSessionReactorReceiver lastReceiver = lastState.receiver;
                // The lastReceiver ^ is already closed; otherwise MessageFlux wouldn't have requested for nextReceiver.
                receiversTracker.untrack(lastReceiver);
            }
            receiversTracker.track(nextReceiver);
            return nextReceiver;
        }

        private void handleMessage(Message qpidMessage) {
            final ServiceBusReceivedMessage message = serializer.deserialize(qpidMessage, ServiceBusReceivedMessage.class);
            logger.atVerbose()
                .addKeyValue(SESSION_ID_KEY, message.getSessionId())
                .addKeyValue(MESSAGE_ID_LOGGING_KEY, message.getMessageId())
                .log("Received message.");
            final boolean success = notifyMessage(message);
            if (enableAutoDisposition) {
                if (success) {
                    complete(message);
                } else {
                    abandon(message);
                }
            }
        }

        private boolean notifyMessage(ServiceBusReceivedMessage message) {
            final Context span = instrumentation.instrumentProcess("ServiceBus.process", message, Context.NONE);
            final AutoCloseable scope  = tracer.makeSpanCurrent(span);

            Throwable error = null;
            try {
                processMessage.accept(
                    new ServiceBusReceivedMessageContext(receiversTracker, new ServiceBusMessageContext(message)));
            } catch (Exception e) {
                error = e;
            }

            if (error != null) {
                notifyError(new ServiceBusException(error, ServiceBusErrorSource.USER_CALLBACK));
                tracer.endSpan(error, message.getContext(), scope);
                return false;
            } else {
                tracer.endSpan(null, message.getContext(), scope);
                return true;
            }
        }

        private void notifyError(Throwable throwable) {
            try {
                processError.accept(new ServiceBusErrorContext(throwable, fqdn, entityPath));
            } catch (Exception e) {
                logger.atVerbose().log("Ignoring error from user processError handler.", e);
            }
        }

        private void complete(ServiceBusReceivedMessage message) {
            try {
                this.receiversTracker.complete(message).block();
            } catch (Exception e) {
                logger.atVerbose().log("Failed to complete message", e);
            }
        }

        private void abandon(ServiceBusReceivedMessage message) {
            try {
                this.receiversTracker.abandon(message).block();
            } catch (Exception e) {
                logger.atVerbose().log("Failed to abandon message", e);
            }
        }

        private static final class SessionLinkStream extends AtomicBoolean {
            private final Mono<ServiceBusReceiveLink> nextSessionLink;

            SessionLinkStream(Mono<ServiceBusReceiveLink> nextSessionLink) {
                super(false);
                this.nextSessionLink = Mono.defer(() -> {
                    final boolean isTerminated = super.get();
                    return isTerminated ? Mono.error(new TerminatedException("session-link-stream")) : nextSessionLink;
                }).map(nextLink -> {
                    final boolean isTerminated = super.get();
                    if (isTerminated) {
                        nextLink.closeAsync().subscribe();
                        throw new TerminatedException("session-link-stream");
                    }
                    return nextLink;
                });
            }

            Flux<Next> flux() {
                return nonEagerRepeat(nextSessionLink)
                    .flatMap(nextLink -> {
                        return nextLink.getSessionId().flatMap(sessionId -> {
                            return Mono.just(new Next(sessionId, nextLink));
                        });
                    });
            }

            void close() {
                super.set(true); // mark terminated.
            }

            private static Flux<ServiceBusReceiveLink> nonEagerRepeat(Mono<ServiceBusReceiveLink> source) {
                // We want to produce a Flux<ServiceBusReceiveLink> from a Mono<ServiceBusReceiveLink> source.
                // Simply using source.repeat() to produce the Flux has a problem due to 'repeat' operator nature.
                //
                // The 'repeat' operator resubscribes to the source immediately after propagating the last emitted link
                // from the source to downstream MessageFlux. Such a re-subscription can result in the source to start
                // requesting the broker for the next link. This shortcoming can be alleviated by sandwiching 'repeat'
                // between 'cacheInvalidateIf' and 'filter'.
                //
                return source
                    .cacheInvalidateIf(link -> link.isDisposed())
                    .repeat()
                    .filter(link -> !link.isDisposed());
                //
                // With the sandwiching, the undesired re-subscription from the 'repeat' will be served by the 'cacheInvalidateIf'
                // by "re-emitting" the last emitted link. The 'repeat' operator internally stores this "re-emitted" link.
                // Eventually, when MessageFlux "requests" a new link after the last link's disposal, the 'filter' will
                // propagate that request to 'repeat'. The 'repeat' will emit the stored link (which is the same link that
                // MessageFlux found disposed of). The 'filter' will filter it out, and 'repeat' will resubscribe to
                // 'cacheInvalidateIf', this time cache is invalidated due to the cached link's disposal,
                // and a re-subscription to 'newSessionLink' for the new link happens, resulting new link flow through
                // 'repeat' to 'filter' to MessageFlux.
            }

            private static final class Next {
                final String sessionId;
                final ServiceBusReceiveLink sessionLink;

                Next(String sessionId, ServiceBusReceiveLink sessionLink) {
                    this.sessionId = sessionId;
                    this.sessionLink = sessionLink;
                }

                Mono<Void> closeAsync() {
                    return sessionLink.closeAsync();
                }
            }
        }

        /**
         * A Function that when called, invokes {@link Message} handler using a Worker.
         */
        private static final class RunOnWorker implements Function<Message, Publisher<Void>> {
            private final Consumer<Message> handleMessage;
            private final Scheduler workerScheduler;

            /**
             * Instantiate {@link RunOnWorker} to run the given {@code handleMessage} handler using a Worker
             * from the provided {@code workerScheduler}.
             *
             * @param handleMessage The message handler.
             * @param workerScheduler The Scheduler hosting the Worker to run the message handler.
             */
            RunOnWorker(Consumer<Message> handleMessage, Scheduler workerScheduler) {
                this.handleMessage = handleMessage;
                this.workerScheduler = workerScheduler;
            }

            @Override
            public Mono<Void> apply(Message qpidMessage) {
                return Mono.fromRunnable(() -> {
                    handleMessage.accept(qpidMessage);
                }).subscribeOn(workerScheduler).then();
                // The subscribeOn offloads message handling to a Worker from the Scheduler.
            }
        }
    }

    static final class SessionReceiversTracker {
        private final ClientLogger logger;
        private final String fqdn;
        private final String entityPath;
        private final ServiceBusReceiveMode receiveMode;
        private final ConcurrentHashMap<String, ServiceBusSessionReactorReceiver> receivers;

        private SessionReceiversTracker(ClientLogger logger, int size, String fqdn, String entityPath, ServiceBusReceiveMode receiveMode) {
            this.logger = logger;
            this.fqdn = fqdn;
            this.entityPath = entityPath;
            this.receiveMode = receiveMode;
            this.receivers = new ConcurrentHashMap<>(size);
        }

        private void track(ServiceBusSessionReactorReceiver receiver) {
            receivers.put(receiver.getSessionId(), receiver);
        }

        private void untrack(ServiceBusSessionReactorReceiver receiver) {
            receivers.remove(receiver.getSessionId(), receiver);
        }

        private void clear() {
            receivers.clear();
        }

        String getFqdn() {
            return fqdn;
        }

        String getEntityPath() {
            return entityPath;
        }

        Mono<Void> abandon(ServiceBusReceivedMessage message) {
            return updateDisposition(message, DispositionStatus.ABANDONED, null, null,
                null, null);
        }

        Mono<Void> complete(ServiceBusReceivedMessage message) {
            return updateDisposition(message, DispositionStatus.COMPLETED, null, null,
                null, null);
        }

        Mono<Void> deadLetter(ServiceBusReceivedMessage message) {
            return updateDisposition(message, DispositionStatus.SUSPENDED, null, null,
                null, null);
        }

        Mono<Void> defer(ServiceBusReceivedMessage message) {
            return updateDisposition(message, DispositionStatus.DEFERRED, null, null,
                null, null);
        }

        Mono<Void> abandon(ServiceBusReceivedMessage message, AbandonOptions options) {
            Mono<Void> nullError = checkNull(options, options != null ? options.getTransactionContext() : null);
            if (nullError != null) {
                return nullError;
            }
            return updateDisposition(message, DispositionStatus.ABANDONED, options.getPropertiesToModify(),
                null, null, options.getTransactionContext());
        }

        Mono<Void> complete(ServiceBusReceivedMessage message, CompleteOptions options) {
            Mono<Void> nullError = checkNull(options, options != null ? options.getTransactionContext() : null);
            if (nullError != null) {
                return nullError;
            }
            return updateDisposition(message, DispositionStatus.COMPLETED, null, null,
                null, options.getTransactionContext());
        }

        Mono<Void> deadLetter(ServiceBusReceivedMessage message, DeadLetterOptions options) {
            Mono<Void> nullError = checkNull(options, options != null ? options.getTransactionContext() : null);
            if (nullError != null) {
                return nullError;
            }
            return updateDisposition(message, DispositionStatus.SUSPENDED, options.getPropertiesToModify(),
                options.getDeadLetterReason(), options.getDeadLetterErrorDescription(), options.getTransactionContext());
        }

        Mono<Void> defer(ServiceBusReceivedMessage message, DeferOptions options) {
            Mono<Void> nullError = checkNull(options, options != null ? options.getTransactionContext() : null);
            if (nullError != null) {
                return nullError;
            }
            return updateDisposition(message, DispositionStatus.DEFERRED, options.getPropertiesToModify(),
                null, null, options.getTransactionContext());
        }

        private Mono<Void> updateDisposition(ServiceBusReceivedMessage message, DispositionStatus dispositionStatus,
            Map<String, Object> propertiesToModify, String deadLetterReason, String deadLetterDescription,
            ServiceBusTransactionContext transactionContext) {
            if (receiveMode != ServiceBusReceiveMode.PEEK_LOCK) {
                final String m = String.format("'%s' is not supported on a receiver opened in ReceiveMode.RECEIVE_AND_DELETE.", dispositionStatus);
                return Mono.error(new UnsupportedOperationException(m));
            } else if (message.isSettled()) {
                final String m = "The message has either been deleted or already settled.";
                return Mono.error(new IllegalArgumentException(m));
            } else if (message.getLockToken() == null) {
                final String m = "This operation is not supported for peeked messages. "
                    + "Only messages received using receiveMessages() in PEEK_LOCK mode can be settled.";
                return Mono.error(new UnsupportedOperationException(m));
            }
            final String sessionId = message.getSessionId();
            final ServiceBusSessionReactorReceiver receiver = receivers.get(sessionId);
            if (receiver != null) {
                final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
                    deadLetterDescription, propertiesToModify, transactionContext);
                return receiver.updateDisposition(message.getLockToken(), deliveryState);
            } else {
                return Mono.error(DeliveryNotOnLinkException.noMatchingDelivery(message.getLockToken()));
            }
        }

        private Mono<Void> checkNull(Object options, ServiceBusTransactionContext transactionContext) {
            if (options == null) {
                return monoError(logger, new NullPointerException("'options' cannot be null."));
            }
            if (transactionContext != null && transactionContext.getTransactionId() == null) {
                return monoError(logger, new NullPointerException("'options.transactionContext.transactionId' cannot be null."));
            }
            return null;
        }
    }

    private static final class State<T extends AsyncCloseable> {
        final T receiver;

        State(T receiver) {
            this.receiver = Objects.requireNonNull(receiver);
        }

        static <T extends AsyncCloseable> State<T> terminated() {
            return new State<>();
        }

        Mono<Void> closeAsync() {
            return receiver != null ? receiver.closeAsync() : Mono.empty();
        }

        private State() {
            // Private ctr only used by terminated().
            this.receiver = null;
        }
    }

    private enum TerminationReason {
        COMPLETED,
        ERRORED,
        CANCELED,
        CLOSED,
    }

    private static final class TerminatedException extends RuntimeException {
        TerminatedException(String callSite) {
            super("Cannot pump messages after termination. (Detected at " + callSite + ").");
        }

        TerminatedException(String callSite, Throwable cause) {
            super("Cannot pump messages after terminal error. (Detected at " + callSite + ").", cause);
        }
    }
}
