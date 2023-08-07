// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;

/**
 * Abstraction to pump messages using a {@link ServiceBusReceiverAsyncClient} as long as the client is healthy.
 */
final class NonSessionMessagePump {
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final String PUMP_ID_KEY = "pump-id";
    private static final Duration CONNECTION_STATE_POLL_INTERVAL = Duration.ofSeconds(20);
    private final long  pumpId;
    private final ServiceBusReceiverAsyncClient client;
    private final String fqdn;
    private final String entityPath;
    private final ClientLogger logger;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final int concurrency;
    private final boolean enableAutoDisposition;
    private final boolean enableAutoLockRenew;
    private final Scheduler workerScheduler;
    private final ServiceBusReceiverInstrumentation instrumentation;
    private final ServiceBusTracer tracer;

    /**
     * Instantiate {@link NonSessionMessagePump} that pumps messages emitted by the given {@code client}. The messages
     * are pumped to the {@code processMessage} concurrently with the parallelism equal to {@code concurrency}.
     *
     * @param client The underlying client to read messages from the broker.
     * @param processMessage The consumer that the pump should invoke for each message.
     * @param processError The consumer that the pump should report the errors.
     * @param concurrency The pumping concurrency, i.e., how many invocations of {@code processMessage} should happen in parallel.
     * @param enableAutoDisposition Indicate if auto-complete or abandon should be enabled.
     */
    NonSessionMessagePump(ServiceBusReceiverAsyncClient client, Consumer<ServiceBusReceivedMessageContext> processMessage,
        Consumer<ServiceBusErrorContext> processError, int concurrency, boolean enableAutoDisposition) {
        this.pumpId = COUNTER.incrementAndGet();
        this.client = client;
        this.fqdn = this.client.getFullyQualifiedNamespace();
        this.entityPath = this.client.getEntityPath();

        final Map<String, Object> loggingContext = new HashMap<>(3);
        loggingContext.put(PUMP_ID_KEY, this.pumpId);
        loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, this.fqdn);
        loggingContext.put(ENTITY_PATH_KEY, this.entityPath);
        this.logger = new ClientLogger(NonSessionMessagePump.class, loggingContext);

        this.processMessage = processMessage;
        this.processError = processError;
        this.concurrency = concurrency;
        this.enableAutoDisposition = enableAutoDisposition;
        this.enableAutoLockRenew = client.isAutoLockRenewRequested();
        if (concurrency > 1) {
            this.workerScheduler = Schedulers.boundedElastic();
        } else {
            // For the max-concurrent-calls == 1 (the default) case, the message handler can be invoked in the same
            // BoundedElastic thread that the ServiceBusReactorReceiver::receive() API (backing the MessageFlux)
            // publishes the message. In this case, we use 'Schedulers.immediate' to avoid the thread switch that
            // is only needed for higher parallelism (max-concurrent-calls > 1) case.
            this.workerScheduler = Schedulers.immediate();
        }
        this.instrumentation = this.client.getInstrumentation();
        this.tracer = this.instrumentation.getTracer();
    }

    /**
     * Begin pumping messages in parallel, with the parallelism equal to the configured {@code concurrency}.
     *
     * @return a mono that emits {@link MessagePumpTerminatedException} when this {@link NonSessionMessagePump} terminates.
     * The pumping terminates when the underlying client encounters a non-retriable error, the retries exhaust,
     * or rejection when scheduling concurrently.
     */
    Mono<Void> begin() {
        final Mono<Void> terminatePumping = pollConnectionState();
        final Mono<Void> pumping = client.nonSessionProcessorReceiveV2()
            .flatMap(new RunOnWorker(this::handleMessage, workerScheduler), concurrency, 1).then();

        final Mono<Void> pumpingMessages = Mono.firstWithSignal(pumping, terminatePumping);

        return pumpingMessages
            .onErrorMap(e -> {
                if (e instanceof MessagePumpTerminatedException) {
                    // 'e' propagated from pollConnectionState().
                    return e;
                }
                // 'e' propagated from client.nonSessionProcessorReceiveV2().
                return new MessagePumpTerminatedException(pumpId, fqdn, entityPath, "pumping#error-map", e);
            })
            .then(Mono.error(() -> MessagePumpTerminatedException.forCompletion(pumpId, fqdn, entityPath)));
    }

    private Mono<Void> pollConnectionState() {
        return Flux.interval(CONNECTION_STATE_POLL_INTERVAL)
            .handle((ignored, sink) -> {
                if (client.isConnectionClosed()) {
                    final RuntimeException e = logger.atInfo().log(new MessagePumpTerminatedException(pumpId, fqdn, entityPath, "non-session#connection-state-poll"));
                    sink.error(e);
                }
            }).then();
    }

    private void handleMessage(ServiceBusReceivedMessage message) {
        final Disposable lockRenewDisposable;
        if (enableAutoLockRenew) {
            lockRenewDisposable = client.beginLockRenewal(message);
        } else {
            lockRenewDisposable = Disposables.disposed();
        }
        final boolean success = notifyMessage(message);
        if (enableAutoDisposition) {
            if (success) {
                complete(message);
            } else {
                abandon(message);
            }
        }
        lockRenewDisposable.dispose();
    }

    private boolean notifyMessage(ServiceBusReceivedMessage message) {
        final Context span = instrumentation.instrumentProcess("ServiceBus.process", message, Context.NONE);
        final AutoCloseable scope  = tracer.makeSpanCurrent(span);

        Throwable error = null;
        try {
            processMessage.accept(new ServiceBusReceivedMessageContext(client, new ServiceBusMessageContext(message)));
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
            return Mono.fromRunnable(() -> {
                handleMessage.accept(message);
            }).subscribeOn(workerScheduler).then();
            // The subscribeOn offloads message handling to a Worker from the Scheduler.
        }
    }
}
