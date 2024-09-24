// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.PUMP_ID_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.CONCURRENCY_PER_CORE;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.CORES_VS_CONCURRENCY_MESSAGE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE;
import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;

/**
 * Abstraction to pump messages using a {@link ServiceBusReceiverAsyncClient} (associated with a session unaware entity)
 * as long as the client is healthy.
 *
 * <p>The pumping starts upon subscribing to the Mono that {@link MessagePump#begin} returns. The pumping can
 * be stopped by cancelling the subscription that {@link MessagePump#begin} returned.</p>
 *
 * <p>Once the Mono from {@link MessagePump#begin} terminates, to restart the pumping a new {@link MessagePump}
 * instance should be obtained.</p>
 *
 * <p>The abstraction {@link ServiceBusProcessor} takes care of managing a {@link MessagePump} and obtaining
 * the next MessagePump when current one terminates.</p>
 */
final class MessagePump {
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final Duration CONNECTION_STATE_POLL_INTERVAL = Duration.ofSeconds(20);
    private final long  pumpId;
    private final ServiceBusReceiverAsyncClient client;
    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final ClientLogger logger;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final int concurrency;
    private final boolean useDedicatedThreadPool;
    private final boolean enableAutoDisposition;
    private final boolean enableAutoLockRenew;
    private final ServiceBusReceiverInstrumentation instrumentation;

    /**
     * Instantiate {@link MessagePump} that pumps messages emitted by the given {@code client}. The messages
     * are pumped to the {@code processMessage} concurrently with the parallelism equal to {@code concurrency}.
     *
     * @param client The underlying client to read messages from the broker.
     * @param processMessage The consumer that the pump should invoke for each message.
     * @param processError The consumer that the pump should report the errors.
     * @param concurrency The pumping concurrency, i.e., how many invocations of {@code processMessage} should happen in parallel.
     * @param useDedicatedThreadPool Indicates if a dedicated thread pool with lifetime same as this MessagePump should be used to pump messages.
     * @param enableAutoDisposition Indicate if auto-complete or abandon should be enabled.
     */
    MessagePump(ServiceBusReceiverAsyncClient client, Consumer<ServiceBusReceivedMessageContext> processMessage,
        Consumer<ServiceBusErrorContext> processError, int concurrency, boolean useDedicatedThreadPool, boolean enableAutoDisposition) {
        this.pumpId = COUNTER.incrementAndGet();
        this.client = client;
        this.fullyQualifiedNamespace = this.client.getFullyQualifiedNamespace();
        this.entityPath = this.client.getEntityPath();

        final Map<String, Object> loggingContext = new HashMap<>(3);
        loggingContext.put(PUMP_ID_KEY, this.pumpId);
        loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, this.fullyQualifiedNamespace);
        loggingContext.put(ENTITY_PATH_KEY, this.entityPath);
        this.logger = new ClientLogger(MessagePump.class, loggingContext);

        this.processMessage = processMessage;
        this.processError = processError;
        this.concurrency = concurrency;
        this.useDedicatedThreadPool = useDedicatedThreadPool;
        this.enableAutoDisposition = enableAutoDisposition;
        this.enableAutoLockRenew = client.isAutoLockRenewRequested();
        this.instrumentation = this.client.getInstrumentation();
    }

    /**
     * Begin pumping messages in parallel, with the parallelism equal to the configured {@code concurrency}.
     *
     * @return a mono that emits {@link MessagePumpTerminatedException} when this {@link MessagePump} terminates.
     * The pumping terminates when the underlying client encounters a non-retriable error, the retries exhaust,
     * or rejection when scheduling concurrently.
     */
    Mono<Void> begin() {
        logCPUResourcesConcurrencyMismatch();
        final Mono<Void> terminatePumping = pollConnectionState();
        final Mono<Void> pumping = Mono.usingWhen(getWorkerScheduler(),
            workerScheduler -> {
                final RunOnWorker handleMessageOnWorker = new RunOnWorker(this::handleMessage, workerScheduler);
                return client.nonSessionProcessorReceiveV2()
                    .flatMap(handleMessageOnWorker, concurrency, 1).then();
            },
            this::disposeWorkerScheduler);

        final Mono<Void> pumpingMessages = Mono.firstWithSignal(pumping, terminatePumping);

        return pumpingMessages
            .onErrorMap(e -> {
                if (e instanceof MessagePumpTerminatedException) {
                    // Source of 'e': pollConnectionState.
                    return e;
                }
                // 'e' propagated from client.nonSessionProcessorReceiveV2.
                return new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "pumping#error-map", e);
            })
            .then(Mono.error(() -> MessagePumpTerminatedException.forCompletion(pumpId, fullyQualifiedNamespace, entityPath)));
    }

    private Mono<Void> pollConnectionState() {
        return Flux.interval(CONNECTION_STATE_POLL_INTERVAL)
            .handle((ignored, sink) -> {
                if (client.isConnectionClosed()) {
                    final RuntimeException e = logger.atInfo()
                        .log(new MessagePumpTerminatedException(pumpId, fullyQualifiedNamespace, entityPath, "non-session#connection-state-poll"));
                    sink.error(e);
                }
            }).then();
    }

    private void handleMessage(ServiceBusReceivedMessage message) {
        instrumentation.instrumentProcess(message, ReceiverKind.PROCESSOR, msg -> {
            final Disposable lockRenewDisposable;
            if (enableAutoLockRenew) {
                lockRenewDisposable = client.beginLockRenewal(message);
            } else {
                lockRenewDisposable = Disposables.disposed();
            }
            final Throwable error = notifyMessage(message);
            if (enableAutoDisposition) {
                if (error == null) {
                    complete(message);
                } else {
                    abandon(message);
                }
            }
            lockRenewDisposable.dispose();
            return error;
        });
    }

    private Throwable notifyMessage(ServiceBusReceivedMessage message) {
        try {
            processMessage.accept(new ServiceBusReceivedMessageContext(client, new ServiceBusMessageContext(message)));
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
            client.complete(message).block();
        } catch (Exception e) {
            logger.atVerbose().log("Failed to complete message", e);
        }
    }

    private void abandon(ServiceBusReceivedMessage message) {
        try {
            client.abandon(message).block();
        } catch (Exception e) {
            logger.atVerbose().log("Failed to abandon message", e);
        }
    }

    /**
     * Obtain the worker scheduler facilitating the threads to pump messages to the {@link #processMessage} handler.
     *
     * @return the worker scheduler for message pumping.
     */
    private Publisher<Scheduler> getWorkerScheduler() {
        return Mono.fromSupplier(() -> {
            if (useDedicatedThreadPool) {
                // We set the upper limit ("threadCap") that pool can grow dynamically to twice the "concurrency". However,
                // due to the operator setup in the 'begin()' method, the max threads that gets created in the pool will be
                // limited to "concurrency", despite the higher cap.
                //
                // Since this is an exclusive pool per message pump instance, the scheduler will be disposed of upon this
                // pump termination.
                return Schedulers.newBoundedElastic(concurrency * 2, DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "message-pump-" + pumpId);
            } else {
                if (concurrency > 1) {
                    // Uses the global thread pool with lifetime scoped to the JVM. Since this is a shared global pool,
                    // any attempt to dispose this scheduler at later point will have no effect.
                    return Schedulers.boundedElastic();
                } else {
                    // For the max-concurrent-calls == 1 (the default) case, the message handler can be invoked in the same
                    // BoundedElastic thread that the ServiceBusReactorReceiver::receive() API (backing the MessageFlux)
                    // publishes the message. In this case, we use 'Schedulers.immediate' to avoid the thread switch that
                    // is only needed for higher parallelism (max-concurrent-calls > 1) case.
                    return Schedulers.immediate();
                }
            }
        });
    }

    /**
     * Disposes the worker scheduler obtained from {@link #getWorkerScheduler()}.
     * <p>
     * If the {@code workerScheduler} is the shared global {@link Schedulers#boundedElastic()} pool, then this method call
     * will not have any effect given this scheduler lifetime is scoped to the JVM.
     * </p>
     *
     * @param workerScheduler the worker scheduler to dispose.
     * @return mono that terminates after the disposal.
     */
    private Mono<Void> disposeWorkerScheduler(Scheduler workerScheduler) {
        if (!useDedicatedThreadPool) {
            // No op, the MessagePump used shared global pool.
            return Mono.empty();
        }

        // The "disposeGracefully()" will trigger shutdown() in underlying executor services allowing running or queued
        // for running tasks to finish. The method won't internally call "awaitForTermination()", the Reactor API
        // documentation recommends applying timeout then call "dispose()" to interrupt any tasks hanging around.
        return workerScheduler.disposeGracefully()
            .timeout(Duration.ofSeconds(5))         // TODO (anuchan): make 'timeout' configurable?
            .onErrorResume(TimeoutException.class, e -> {
                workerScheduler.dispose();
                // logger.log(..)                  // TODO (anuchan): WARN log about timeout and forceful interruption of user code.
                return Mono.empty();
            });
    }

    private void logCPUResourcesConcurrencyMismatch() {
        final int cores = Runtime.getRuntime().availableProcessors();
        final int poolSize = useDedicatedThreadPool ? concurrency : DEFAULT_BOUNDED_ELASTIC_SIZE;
        if (concurrency > poolSize || concurrency > CONCURRENCY_PER_CORE * cores) {
            logger.atWarning().log(CORES_VS_CONCURRENCY_MESSAGE, poolSize, cores, concurrency);
        }
    }

    /**
     * A Function that when called, invokes {@link ServiceBusReceivedMessage} handler using a Worker.
     */
    private static final class RunOnWorker implements Function<ServiceBusReceivedMessage, Publisher<Void>> {
        private final Consumer<ServiceBusReceivedMessage> handleMessage;
        private final Scheduler workerScheduler;

        /**
         * Instantiate {@link RunOnWorker} to run the given {@code handleMessage} handler using a Worker
         * from the provided {@code workerScheduler}.
         *
         * @param handleMessage The message handler.
         * @param workerScheduler The Scheduler hosting the Worker to run the message handler.
         */
        RunOnWorker(Consumer<ServiceBusReceivedMessage> handleMessage, Scheduler workerScheduler) {
            this.handleMessage = handleMessage;
            this.workerScheduler = workerScheduler;
        }

        @Override
        public Mono<Void> apply(ServiceBusReceivedMessage message) {
            return Mono.<Void>fromRunnable(() -> {
                handleMessage.accept(message);
            }).subscribeOn(workerScheduler);
            // The subscribeOn offloads message handling to a Worker from the Scheduler.
        }
    }
}
