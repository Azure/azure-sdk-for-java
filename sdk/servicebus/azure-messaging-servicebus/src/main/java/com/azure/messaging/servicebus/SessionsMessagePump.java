// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.implementation.CreditFlowMode;
import com.azure.core.amqp.implementation.MessageFlux;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.handler.DeliveryNotOnLinkException;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.PUMP_ID_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.CONCURRENCY_PER_CORE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.CORES_VS_CONCURRENCY_MESSAGE;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MESSAGE_ID_LOGGING_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * <p>An abstraction to pump messages from multiple sessions.</p>
 * <p>The pump will be connected to at most {@code maxConcurrentSessions} sessions, and streams messages from each of those
 * sessions. Within a session, {@code concurrencyPerSession} messages are pumped in parallel.</p>
 *
 * <p>If a session gets disconnected for some reason, system will acquire link to the next available session, and will roll
 * to that new session to continue streaming messages.</p>
 *
 * <p>The inner abstraction SessionsMessagePump#RollingSessionReceiver is responsible for managing a session, concurrently
 * pumping messages from its current session and rolling to the next session when current session terminates.</p>
 *
 * <p>The {@link SessionsMessagePump} manages {@code maxConcurrentSessions} SessionsMessagePump#RollingSessionReceiver instances.</p>
 *
 * <p>The pumping starts upon subscribing to the Mono that {@link SessionsMessagePump#begin} returns. The pumping can be stopped
 * by cancelling the subscription that {@link SessionsMessagePump#begin} returned.</p>
 *
 * <p>If a SessionsMessagePump#RollingSessionReceiver instance encounters an error when it attempts to acquire a new link,
 * all other SessionsMessagePump#RollingSessionReceiver instance will be canceled, thereby stopping the pumping.
 * The termination signal will be notified through the Mono returned by {@link SessionsMessagePump#begin} that originally
 * started the pumping.</p>
 *
 * <p>Once the Mono from {@link SessionsMessagePump#begin} terminates, to restart the pumping a new {@link SessionsMessagePump}
 * instance should be obtained.</p>
 *
 * <p>The abstraction {@link ServiceBusProcessor} takes care of managing a {@link SessionsMessagePump} and obtaining the next
 * SessionsMessagePump when current one terminates.</p>
 *
 * <p>The SessionsMessagePump termination flow – there are 2 cases leading to SessionsMessagePump termination</p>
 * <ul>
 *     <li>Cancelling Mono returned by SessionsMessagePump.begin().</li>
 *     <li>A RollingSessionReceiver failing to acquire a new session link</li>
 * </ul>
 *
 * <strong>Cancelling Mono returned by SessionsMessagePump.begin()</strong>
 * <p>
 * The when-operator in SessionsMessagePump.begin() synchronously iterates through the list of Mono instances returned by
 * RollingSessionReceiver.receive() calls and cancels each Mono. Cancelling a Mono runs cancel() method on it's backing
 * RollingSessionReceiver.MessageFlux instance. Let’s say the Thread T1 calls cancel(), upon the execution of cancel():</p>
 * <ul>
 * <li>Case1: If the MessageFlux drain-loop is not running, then the Thread T1 running cancel() runs "cancellation operation".</li>
 * <li>Case2: If the MessageFlux drain-loop is currently being executed by a Thread T2, then "cancellation operation"
 * is delegated to T2 and the cancel() call by Thread T1 returns. </li>
 * </ul>
 *
 * <p>Running "cancellation operation" executes currentReceiver.closeAsync().subscribe(), where currenReceiver is the
 * ServiceBusSessionReactorReceiver instance that the RollingSessionReceiver.MessageFlux currently holding.
 * The Thread calling this subscribe() will schedule the amqp-link closure to the Qpid Thread. The subscribe() call here
 * does’t block as the call returns after scheduling.</p>
 *
 * <p>In Case1, the cancel() call by T1 returns after this ServiceBusSessionReactorReceiver.closeAsync().subscribe() call.
 * In Case2, by the time the cancel() call by T1 returns, T2 may or may not have called
 * ServiceBusSessionReactorReceiver.closeAsync().subscribe() or call may in progress.</p>
 *
 * <p>In either way when Thread T1’s cancel() call returns, The onCancel hook of usingWhen operator in
 * RollingSessionReceiver.receive() subscribe to RollingSessionReceiver.terminate(TerminalSignalType.CANCELED, Scheduler)
 * marking RollingSessionReceiver as terminated and dispose the Scheduler it owns.
 *
 * <p>Now that RollingSessionReceiver.terminate(TerminalSignalType.CANCELED, Scheduler) returned control to T1, the When
 * operator moves to the next Mono in the list to perform similar cancellation.</p>
 *
 *<p>Note: In SessionsMessagePump termination by cancellation case, subscription made to the *.closeAsync() is fire-n-forget.</p>
 *
 *  <strong>A RollingSessionReceiver failing to acquire a new session link</strong>
 *
 *  <p> Read the termination by 'Cancelling Mono returned by RollingSessionReceiver.begin()'. When the upstream of
 *  RollingSessionReceiver.MessageFlux fails to acquire a link, it emits {@link MessagePumpTerminatedException} (with inner error
 *  describing the cause). The RollingSessionReceiver.MessageFlux receiving this error propagates the error to downstream usingWhen
 *  operator in RollingSessionReceiver.begin(). The usingWhen operator invokes RollingSessionReceiver.terminate(,) to mark
 *  it as terminated and disposes the Scheduler it owns. The error will further flow to When operator in SessionsMessagePump.begin(),
 *  which cancels rest of the RollingSessionReceiver.MessageFlux, finally the error is emitted by the Mono returned by
 *  SessionsMessagePump.begin().</p>
 */
final class SessionsMessagePump {
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final ArrayList<RollingSessionReceiver> EMPTY = new ArrayList<>(0);
    private static final ArrayList<RollingSessionReceiver> TERMINATED = new ArrayList<>(0);
    private static final Duration CONNECTION_STATE_POLL_INTERVAL = Duration.ofSeconds(20);
    private final long  pumpId;
    private final ClientLogger logger;
    private final String identifier;
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final ServiceBusReceiverInstrumentation instrumentation;
    private final ServiceBusSessionAcquirer sessionAcquirer;
    private final Duration maxSessionLockRenew;
    private final Duration sessionIdleTimeout;
    private final int maxConcurrentSessions;
    private final int concurrencyPerSession;
    private final int prefetch;
    private final boolean enableAutoDisposition;
    private final MessageSerializer serializer;
    private final AmqpRetryPolicy retryPolicy;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final Runnable onTerminate;
    private final AtomicReference<List<RollingSessionReceiver>> rollingReceiversRef = new AtomicReference<>(EMPTY);
    private final SessionReceiversTracker receiversTracker;
    private final Mono<ServiceBusSessionAcquirer.Session> nextSession;

    SessionsMessagePump(String identifier, String fullyQualifiedNamespace, String entityPath, ServiceBusReceiveMode receiveMode,
        ServiceBusReceiverInstrumentation instrumentation, ServiceBusSessionAcquirer sessionAcquirer,
        Duration maxSessionLockRenew, Duration sessionIdleTimeout, int maxConcurrentSessions, int concurrencyPerSession,
        int prefetch, boolean enableAutoDisposition, MessageSerializer serializer, AmqpRetryPolicy retryPolicy,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError, Runnable onTerminate) {
        this.pumpId = COUNTER.incrementAndGet();
        final Map<String, Object> loggingContext = new HashMap<>(3);
        loggingContext.put(PUMP_ID_KEY, pumpId);
        loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, fullyQualifiedNamespace);
        loggingContext.put(ENTITY_PATH_KEY, entityPath);
        this.logger = new ClientLogger(SessionsMessagePump.class, loggingContext);
        this.identifier = identifier;
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        Objects.requireNonNull(receiveMode, "'receiveMode' cannot be null.");
        this.instrumentation = Objects.requireNonNull(instrumentation, "'instrumentation' cannot be null");
        this.sessionAcquirer = Objects.requireNonNull(sessionAcquirer, "'sessionAcquirer' cannot be null");
        this.maxSessionLockRenew = Objects.requireNonNull(maxSessionLockRenew, "'maxSessionLockRenew' cannot be null.");
        this.sessionIdleTimeout = sessionIdleTimeout != null ? sessionIdleTimeout : retryPolicy.getRetryOptions().getTryTimeout();
        this.maxConcurrentSessions = maxConcurrentSessions;
        this.concurrencyPerSession = concurrencyPerSession;
        this.prefetch = prefetch;
        this.enableAutoDisposition = enableAutoDisposition;
        this.serializer = Objects.requireNonNull(serializer, "'serializer' cannot be null.");
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "'retryPolicy' cannot be null.");
        this.processMessage = Objects.requireNonNull(processMessage, "'processMessage' cannot be null.");
        this.processError = Objects.requireNonNull(processError, "'processError' cannot be null.");
        this.onTerminate = Objects.requireNonNull(onTerminate, "'onTerminate' cannot be null.");
        this.receiversTracker = new SessionReceiversTracker(logger, maxConcurrentSessions, fullyQualifiedNamespace, entityPath, receiveMode, instrumentation);
        this.nextSession = new NextSession(pumpId, fullyQualifiedNamespace, entityPath, sessionAcquirer).mono();
    }

    String getIdentifier() {
        return identifier;
    }

    /**
     * Obtain a Mono that when subscribed, start pumping messages from {@code maxConcurrentSessions} sessions.
     * The Mono emits terminal signal once there is a failure in obtaining a new session.
     *
     * <p>The Mono emits {@link UnsupportedOperationException} if it is subscribed more than once. If the Mono is subscribed
     * after the termination of SessionsMessagePump it emits {@link MessagePumpTerminatedException}.</p>
     *
     * @return the Mono to begin and cancel message pumping.
     */
    Mono<Void> begin() {
        logCPUResourcesConcurrencyMismatch();
        final Mono<List<RollingSessionReceiver>> createReceiversMono = Mono.fromSupplier(() -> {
            throwIfTerminatedOrInitialized();
            final List<RollingSessionReceiver> rollingReceivers = createRollingSessionReceivers();
            if (!rollingReceiversRef.compareAndSet(EMPTY, rollingReceivers)) {
                // No AmqpLinks were created or associated wire calls were made at this point for rollingReceivers
                rollingReceivers.clear();
                throwIfTerminatedOrInitialized();
            }
            return rollingReceivers;
        });

        final Function<List<RollingSessionReceiver>, Mono<Void>> pumpFromReceiversMono = rollingReceivers -> {
            final List<Mono<Void>> pumpingList = new ArrayList<>(rollingReceivers.size());
            for (RollingSessionReceiver rollingReceiver : rollingReceivers) {
                pumpingList.add(rollingReceiver.begin());
            }
            final Mono<Void> terminatePumping = pollConnectionState();
            final Mono<Void> pumping = Mono.when(pumpingList);
            return Mono.firstWithSignal(terminatePumping, pumping);
        };

        final Mono<Void> pumpingMessages = Mono.usingWhen(createReceiversMono, pumpFromReceiversMono,
            (__) -> terminate(TerminalSignalType.COMPLETED),
            (__, e) -> terminate(TerminalSignalType.ERRORED),
            (__) -> terminate(TerminalSignalType.CANCELED));

        return pumpingMessages
            .onErrorMap(e -> {
                if (e instanceof MessagePumpTerminatedException) {
                    // Source of 'e': pollConnectionState|NextSession.mono|RollingSessionReceiver.(nextSessionReceiver|NextSessionStream.flux)
                    return e;
                }
                // 'e' here could be reactor.core.CompositeException, e.g, 2 RollingSessionReceiver errors concurrently.
                return new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "pumping#error-map", e);
            })
            .then(Mono.error(() -> MessagePumpTerminatedException.forCompletion(pumpId, fullyQualifiedNamespace, entityPath)));
    }

    private Mono<Void> pollConnectionState() {
        return Flux.interval(CONNECTION_STATE_POLL_INTERVAL)
            .handle((ignored, sink) -> {
                if (sessionAcquirer.isConnectionClosed()) {
                    final RuntimeException e = logger.atInfo()
                        .log(new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "session#connection-state-poll"));
                    sink.error(e);
                }
            }).then();
    }

    private Mono<Void> terminate(TerminalSignalType signalType) {
        final List<RollingSessionReceiver> rollingReceivers = rollingReceiversRef.getAndSet(TERMINATED);
        if (rollingReceivers == TERMINATED) {
            return Mono.empty();
        }
        // By the time one of the terminal handlers in SessionsMessagePump.begin() invokes this terminate (,) API, it is
        // guaranteed that terminate(,) API of all the RollingSessionReceiver instances are invoked. It’s because when
        // the When operator in SessionsMessagePump.begin() detects that it needs to emit terminal signal, as a pre-step,
        // it will go ahead and cancel the Mono returned by each RollingSessionReceiver instance, which will cause it's
        // backing MessageFlux instance to close the ServiceBusSessionReactorReceiver and runs RollingSessionReceiver.terminate(,).
        logger.atInfo().log("Pump terminated. signal:" + signalType);
        receiversTracker.clear();
        onTerminate.run();
        return Mono.empty();
    }

    private List<RollingSessionReceiver> createRollingSessionReceivers() {
        final ArrayList<RollingSessionReceiver> rollingReceivers = new ArrayList<>(maxConcurrentSessions);
        for (int rollerId = 1; rollerId <= maxConcurrentSessions; rollerId++) {
            final RollingSessionReceiver rollingReceiver = new RollingSessionReceiver(pumpId, rollerId, instrumentation,
                fullyQualifiedNamespace, entityPath, nextSession, maxSessionLockRenew, sessionIdleTimeout, concurrencyPerSession,
                prefetch, enableAutoDisposition, serializer, retryPolicy, processMessage, processError,
                receiversTracker);
            rollingReceivers.add(rollingReceiver);
        }
        return rollingReceivers;
    }

    private void throwIfTerminatedOrInitialized() {
        // Any error thrown here is IllegalStateException since SessionsMessagePump#begin() caller is attempting
        // to invoke it in unexpected way (invoke post termination or invoke more than once).
        final List<RollingSessionReceiver> l = rollingReceiversRef.get();
        if (l == TERMINATED) {
            throw logger.atVerbose().log(new IllegalStateException("Cannot invoke begin() once terminated."));
        }
        if (l != EMPTY) {
            throw logger.atVerbose().log(new IllegalStateException("Cannot invoke begin() more than once."));
        }
    }

    private void logCPUResourcesConcurrencyMismatch() {
        final int cores = Runtime.getRuntime().availableProcessors();
        final int poolSize = DEFAULT_BOUNDED_ELASTIC_SIZE;
        final int concurrency = maxConcurrentSessions * concurrencyPerSession;
        if (concurrencyPerSession > poolSize || concurrency > CONCURRENCY_PER_CORE * cores) {
            final String message = concurrency + " (ConcurrentSessions=" + maxConcurrentSessions + ", ConcurrencyPerSession=" + concurrencyPerSession + ")";
            logger.atWarning().log(CORES_VS_CONCURRENCY_MESSAGE, poolSize, cores, message);
        }
    }

    /**
     * The type which provides a Mono {@link NextSession#mono()} that when subscribed acquires a new unnamed session.
     * All the {@link RollingSessionReceiver} in the {@link SessionsMessagePump} shares this Mono to obtain unique sessions.
     *
     * <p>The event the Mono fails to acquire a session, the type marks itself as terminated (i.e., self-terminate) and
     * notifies the subscription about the failure as {@link MessagePumpTerminatedException}. Any later subscriptions will
     * be notified with MessagePumpTerminatedException.</p>
     *
     * <p>If a RollingSessionReceiver encounters acquire session failure, it stops pumping and emit terminal signal.
     * At this point, the SessionMessagePump will cancel all other RollingSessionReceiver instances. The design of
     * the SessionMessagePump is to stop pumping from all sessions once any of the RollingSessionReceiver emits terminal
     * signal. The self-terminating nature of the shared {@link NextSession#mono()} will reduce the chances of one or
     * more of the RollingSessionReceiver attempting session acquire when SessionMessagePump is about to or in progress
     * of canceling those.</p>
     */
    private static final class NextSession
        implements Supplier<Mono<ServiceBusSessionAcquirer.Session>> {
        private final AtomicReference<Boolean> isTerminated = new AtomicReference<>(false);
        private final long pumpId;
        private final String fullyQualifiedNamespace;
        private final String entityPath;
        private final ServiceBusSessionAcquirer sessionAcquirer;

        NextSession(long pumpId, String fullyQualifiedNamespace, String entityPath, ServiceBusSessionAcquirer sessionAcquirer) {
            this.pumpId = pumpId;
            this.fullyQualifiedNamespace = fullyQualifiedNamespace;
            this.entityPath = entityPath;
            this.sessionAcquirer = sessionAcquirer;
        }

        Mono<ServiceBusSessionAcquirer.Session> mono() {
            final Supplier<Mono<ServiceBusSessionAcquirer.Session>> supplier = this;
            return Mono.defer(supplier);
        }

        @Override
        public Mono<ServiceBusSessionAcquirer.Session> get() {
            if (isTerminated.get()) {
                return Mono.error(new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "session#acquire"));
            }
            return sessionAcquirer.acquire()
                .onErrorMap(e -> {
                    isTerminated.set(true);
                    return new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "session#acquire", e);
                });
            // The 'MessagePumpTerminatedException' is not logged here ^, since MessageFlux.onError will log it in WARN level.
        }
    }

    /**
     * A type that is responsible for managing a session, concurrently (with parallelism equal to {@code concurrencyPerSession})
     * pumping messages from the session and rolling to the next session when current session terminates.
     */
    private static final class RollingSessionReceiver
        extends AtomicReference<State<ServiceBusSessionReactorReceiver>> {
        private static final String ROLLER_ID_KEY = "roller-id";
        private static final State<ServiceBusSessionReactorReceiver> INIT = State.init();
        private static final State<ServiceBusSessionReactorReceiver> TERMINATED = State.terminated();
        private final ClientLogger logger;
        private final long pumpId;
        private final int rollerId;
        private final String fullyQualifiedNamespace;
        private final String entityPath;
        private final int concurrency;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final boolean enableAutoDisposition;
        private final Duration maxSessionLockRenew;
        private final Duration sessionIdleTimeout;
        private final MessageSerializer serializer;
        private final ServiceBusReceiverInstrumentation instrumentation;
        private final ServiceBusTracer tracer;
        private final SessionReceiversTracker receiversTracker;
        private final NextSessionStream nextSessionStream;
        private final  MessageFlux messageFlux;

        RollingSessionReceiver(long pumpId, int rollerId, ServiceBusReceiverInstrumentation instrumentation, String fullyQualifiedNamespace,
            String entityPath, Mono<ServiceBusSessionAcquirer.Session> nextSession, Duration maxSessionLockRenew,
            Duration sessionIdleTimeout, int concurrency, int prefetch, boolean enableAutoDisposition,
            MessageSerializer serializer, AmqpRetryPolicy retryPolicy,
            Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
            SessionReceiversTracker receiversTracker) {
            super(INIT);
            this.pumpId = pumpId;
            final Map<String, Object> loggingContext = new HashMap<>(3);
            loggingContext.put(ROLLER_ID_KEY, rollerId);
            loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, fullyQualifiedNamespace);
            loggingContext.put(ENTITY_PATH_KEY, entityPath);
            this.logger = new ClientLogger(RollingSessionReceiver.class, loggingContext);
            this.rollerId = rollerId;
            this.fullyQualifiedNamespace = fullyQualifiedNamespace;
            this.entityPath = entityPath;
            this.concurrency = concurrency;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoDisposition = enableAutoDisposition;
            this.maxSessionLockRenew = maxSessionLockRenew;
            this.sessionIdleTimeout = sessionIdleTimeout;
            this.serializer = serializer;
            this.instrumentation = instrumentation;
            this.tracer = instrumentation.getTracer();
            this.receiversTracker = receiversTracker;
            this.nextSessionStream = new NextSessionStream(pumpId, rollerId, fullyQualifiedNamespace, entityPath, nextSession);
            final Flux<ServiceBusSessionReactorReceiver> nextSessionReceiverStream = nextSessionStream.flux()
                .map(this::nextSessionReceiver);
            this.messageFlux = new MessageFlux(nextSessionReceiverStream, prefetch, CreditFlowMode.RequestDriven, retryPolicy);
        }

        Mono<Void> begin() {
            // Note: The call site i.e., SessionsMessagePump.begin() guarantees the RollingSessionReceiver.begin() is never
            // invoked or subscribed more than once, this ensures we adhere to the requirement that - There must be only one
            // subscription to the backing MessageFlux instance.
            return Mono.usingWhen(
                Mono.fromSupplier(() -> {
                    final Scheduler workerScheduler;
                    if (concurrency > 1) {
                        workerScheduler = Schedulers.newBoundedElastic(
                            DEFAULT_BOUNDED_ELASTIC_SIZE, DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "rolling-session-receiver-" + rollerId);
                    } else {
                        workerScheduler = Schedulers.immediate();
                    }
                    return workerScheduler;
                }),
                workerScheduler -> {
                    final RunOnWorker handleMessageOnWorker = new RunOnWorker(this::handleMessage, workerScheduler);
                    return messageFlux.flatMap(handleMessageOnWorker, concurrency, 1).then();
                },
                (workerScheduler) -> terminate(TerminalSignalType.COMPLETED, workerScheduler),
                (workerScheduler, e) -> terminate(TerminalSignalType.ERRORED, workerScheduler),
                (workerScheduler) -> terminate(TerminalSignalType.CANCELED, workerScheduler)
            );
        }

        private Mono<Void> terminate(TerminalSignalType signalType, Scheduler workerScheduler) {
            final State<ServiceBusSessionReactorReceiver> state = super.getAndSet(TERMINATED);
            if (state == TERMINATED) {
                return Mono.empty();
            }
            // By the time one of the terminal handlers in RollingSessionReceiver.begin() invokes this terminate(,) API,
            // it is guaranteed that the ServiceBusSessionReactorReceiver instance in 'state' (i.e. current session internal
            // receiver) is closed by the backing MessageFlux. Hence, we only need to dispose the resources not managed
            // by the ServiceBusSessionReactorReceiver.
            logger.atInfo().log("Roller terminated. rollerId:" + rollerId + " signal:" + signalType);
            nextSessionStream.close();
            workerScheduler.dispose();
            return Mono.empty();
        }

        private ServiceBusSessionReactorReceiver nextSessionReceiver(ServiceBusSessionAcquirer.Session nextSession) {
            final State<ServiceBusSessionReactorReceiver> lastState = super.get();
            if (lastState == TERMINATED) {
                nextSession.getLink().closeAsync().subscribe();
                throw new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "session#next-receiver roller_" + rollerId);
            }
            final ServiceBusSessionReactorReceiver nextSessionReceiver = new ServiceBusSessionReactorReceiver(logger, tracer,
                nextSession, sessionIdleTimeout, maxSessionLockRenew);
            if (!super.compareAndSet(lastState, new State<>(nextSessionReceiver))) {
                // 1. The 'super.getAndSet(DISPOSED)' in the terminate(,) won the race with the above 'super.compareAndSet'.
                // 2. Multiple 'super.compareAndSet' will never race each other since -
                //      2.1 There will be only one Subscription from 'MessageFlux' throughout RollingSessionReceiver lifetime.
                //      2.2 Spec: nextSessionReceiver runs triggered by the requests made on that Subscription are 'serialized'.
                //
                //      Hence, we WON’T run into the case of – two threads (T1, T2) invoking nextSessionReceiver concurrently,
                //      each creating ServiceBusSessionReactorReceiver and 'compareAndSet' of T1 winning the race with T’s
                //      'compareAndSet' resulting in T1 not closing its ServiceBusSessionReactorReceiver.
                //
                nextSessionReceiver.closeAsync().subscribe();
                throw new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "session#next-receiver roller_" + rollerId);
            }
            if (lastState != INIT) {
                final ServiceBusSessionReactorReceiver lastSessionReceiver = lastState.receiver;
                // The lastReceiver ^ is already closed; otherwise MessageFlux wouldn't have requested for nextReceiver.
                receiversTracker.untrack(lastSessionReceiver);
            }
            receiversTracker.track(nextSessionReceiver);
            return nextSessionReceiver;
        }

        private void handleMessage(Message qpidMessage) {
            final ServiceBusReceivedMessage message = serializer.deserialize(qpidMessage, ServiceBusReceivedMessage.class);

            instrumentation.instrumentProcess(message, ReceiverKind.PROCESSOR, msg -> {
                logger.atVerbose()
                    .addKeyValue(SESSION_ID_KEY, message.getSessionId())
                    .addKeyValue(MESSAGE_ID_LOGGING_KEY, message.getMessageId())
                    .log("Received message.");

                final Throwable error = notifyMessage(msg);
                if (enableAutoDisposition) {
                    if (error == null) {
                        complete(msg);
                    } else {
                        abandon(msg);
                    }
                }
                return error;
            });
        }

        private Throwable notifyMessage(ServiceBusReceivedMessage message) {
            try {
                processMessage.accept(
                    new ServiceBusReceivedMessageContext(receiversTracker, new ServiceBusMessageContext(message)));
            } catch (Exception e) {
                notifyError(new ServiceBusException(e, ServiceBusErrorSource.USER_CALLBACK));
                return e;
            }

            return null;
        }

        private void notifyError(Throwable throwable) {
            try {
                processError.accept(new ServiceBusErrorContext(throwable, fullyQualifiedNamespace, entityPath));
            } catch (Exception e) {
                logger.atVerbose().log("Ignoring error from user processError handler.", e);
            }
        }

        private void complete(ServiceBusReceivedMessage message) {
            try {
                receiversTracker.complete(message).block();
            } catch (Exception e) {
                logger.atVerbose().log("Failed to complete message", e);
            }
        }

        private void abandon(ServiceBusReceivedMessage message) {
            try {
                receiversTracker.abandon(message).block();
            } catch (Exception e) {
                logger.atVerbose().log("Failed to abandon message", e);
            }
        }

        /**
         * A type which provide a Flux {@link NextSessionStream#flux()} that streams next available sessions.
         * Each {@link RollingSessionReceiver} has a {@link NextSessionStream} instance associated,
         * when the {@link RollingSessionReceiver} wants to roll to a new session, it requests next session from the Flux.
         *
         * <p>Underneath, all the {@link NextSessionStream} instances (across all {@link RollingSessionReceiver}) shares
         * the common Mono {@link NextSession#mono()} to acquire unique sessions.</p>
         */
        private static final class NextSessionStream extends AtomicBoolean {
            private final long pumpId;
            private final int rollerId;
            private final String fullyQualifiedNamespace;
            private final String entityPath;
            private final Mono<ServiceBusSessionAcquirer.Session> newSession;

            NextSessionStream(long pumpId, int rollerId, String fullyQualifiedNamespace, String entityPath, Mono<ServiceBusSessionAcquirer.Session> nextSession) {
                super(false);
                this.pumpId = pumpId;
                this.rollerId = rollerId;
                this.fullyQualifiedNamespace = fullyQualifiedNamespace;
                this.entityPath = entityPath;
                this.newSession = Mono.defer(() -> {
                    final boolean isTerminated = super.get();
                    if (isTerminated) {
                        return Mono.error(new MessagePumpTerminatedException(this.pumpId, this.fullyQualifiedNamespace, this.entityPath,
                            "session#next-link roller_" + this.rollerId));
                    } else {
                        return nextSession;
                    }
                }).map(session -> {
                    final boolean isTerminated = super.get();
                    if (isTerminated) {
                        session.getLink().closeAsync().subscribe();
                        throw new MessagePumpTerminatedException(this.pumpId, this.fullyQualifiedNamespace, this.entityPath,
                            "session#next-link roller_" + this.rollerId);
                    }
                    return session;
                });
                // The 'MessagePumpTerminatedException' is not logged here ^, since MessageFlux.onError will log it in WARN level.
            }

            Flux<ServiceBusSessionAcquirer.Session> flux() {
                return nonEagerRepeat(newSession);
            }

            void close() {
                // mark terminated.
                super.set(true);
            }

            private static Flux<ServiceBusSessionAcquirer.Session> nonEagerRepeat(Mono<ServiceBusSessionAcquirer.Session> source) {
                // We want to transform Mono<ServiceBusSessionAcquirer.Session> source to Flux<ServiceBusSessionAcquirer.Session>.
                // Simply using source.repeat() to produce the Flux has a problem due to 'repeat' operator nature.
                //
                // Problem: The 'repeat' operator resubscribes to the source immediately after propagating the last emitted link
                // from the source to downstream MessageFlux. Such a re-subscription can result in the source to start
                // requesting the broker for the next link.
                //
                // Solution: This shortcoming can be alleviated by sandwiching 'repeat' between 'cacheInvalidateIf' and 'filter'.
                //
                return source
                    .cacheInvalidateIf(cachedSession -> cachedSession.getLink().isDisposed())
                    .repeat()
                    .filter(session -> !session.getLink().isDisposed());
                //
                // Solution details: With the sandwiching, the undesired re-subscription from the 'repeat' will be served by
                // the 'cacheInvalidateIf' by "re-emitting" the last emitted link. The 'repeat' operator internally stores this
                // "re-emitted" link.
                // Eventually, when MessageFlux "requests" a new link after the last link's disposal, the 'filter' will
                // propagate that request to 'repeat'. The 'repeat' will emit the stored link (which is the same link that
                // MessageFlux found disposed of). The 'filter' will filter it out, and 'repeat' will resubscribe to
                // 'cacheInvalidateIf', this time cache is invalidated due to the cached link's disposal,
                // and a re-subscription to source (this.newSession) for the new link happens, resulting new link flow
                // through 'repeat' to 'filter' to MessageFlux.
            }
        }

        /**
         * A Function that when called, invokes {@link Message} handler using a Worker thread from a {@link Scheduler}.
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
                return Mono.<Void>fromRunnable(() -> {
                    handleMessage.accept(qpidMessage);
                }).subscribeOn(workerScheduler);
                // The subscribeOn offloads message handling to a Worker from the Scheduler.
            }
        }
    }

    /**
     * Tracks the running {@link ServiceBusSessionReactorReceiver} instances, each backing a RollingSessionReceiver instance.
     * Each time a RollingSessionReceiver rolls to a new ServiceBusSessionReactorReceiver Rn, it will track Rn by invoking
     * tack(Rn) and un-tracks the last (closed) ServiceBusSessionReactorReceiver Rm by invoking untrack(Rm).
     *
     * <p>The type holds sessionId to ServiceBusSessionReactorReceiver mapping. A session message can be disposition only on
     * the ServiceBusSessionReactorReceiver delivered it. The mapping tracked by this type enables looking up
     * the ServiceBusSessionReactorReceiver when a message needs to be disposition.</p>
     *
     * <p>It is possible that, a session say session-1 gets acquired by RollingSessionReceiver Ru,
     * while a RollingSessionReceiver Rv that was previously connected to session-1 rolls to session-2. Measures are
     * taken to ensure the Rv is not removing (un-track) session-1 tracked by Ru from the shared view in such concurrent
     * case, the underlying {@link ConcurrentHashMap#remove(Object, Object)} api avoid such undesired un-track.</p>
     */
    static final class SessionReceiversTracker {
        private final ClientLogger logger;
        private final String fullyQualifiedNamespace;
        private final String entityPath;
        private final ServiceBusReceiveMode receiveMode;
        private final ConcurrentHashMap<String, ServiceBusSessionReactorReceiver> receivers;
        private final ServiceBusReceiverInstrumentation instrumentation;

        private SessionReceiversTracker(ClientLogger logger, int size, String fullyQualifiedNamespace, String entityPath,
            ServiceBusReceiveMode receiveMode, ServiceBusReceiverInstrumentation instrumentation) {
            this.logger = logger;
            this.fullyQualifiedNamespace = fullyQualifiedNamespace;
            this.entityPath = entityPath;
            this.receiveMode = receiveMode;
            this.receivers = new ConcurrentHashMap<>(size);
            this.instrumentation = instrumentation;
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

        String getFullyQualifiedNamespace() {
            return fullyQualifiedNamespace;
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
            final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
                deadLetterDescription, propertiesToModify, transactionContext);

            Mono<Void> updateDispositionMono;
            if (receiver != null) {
                updateDispositionMono = receiver.updateDisposition(message.getLockToken(), deliveryState);
            } else {
                updateDispositionMono = Mono.error(DeliveryNotOnLinkException.noMatchingDelivery(message.getLockToken(), deliveryState));
            }
            return instrumentation.instrumentSettlement(updateDispositionMono, message, message.getContext(), dispositionStatus);
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

        static <T extends AsyncCloseable> State<T> init() {
            return new State<>();
        }

        static <T extends AsyncCloseable> State<T> terminated() {
            return new State<>();
        }

        private State() {
            // Private ctr only used by init(), terminated().
            this.receiver = null;
        }
    }

    /**
     * The signal that triggered {@link SessionsMessagePump} and {@link RollingSessionReceiver} termination.
     */
    private enum TerminalSignalType {
        COMPLETED,
        ERRORED,
        CANCELED,
    }
}
