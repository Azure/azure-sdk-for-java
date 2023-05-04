// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.messaging.servicebus.FluxTrace.PROCESS_ERROR_KEY;

/**
 *  Processor to pump messages from a session unaware entity.
 */
public final class NonSessionProcessor {
    private final ServiceBusClientBuilder builder;
    private final int concurrency;
    private final Consumer<ServiceBusReceivedMessageContext> processMessage;
    private final Consumer<ServiceBusErrorContext> processError;
    private final boolean enableAutoLockRenew;
    private final boolean enableAutoDisposition;
    private final Object lock = new Object();
    private boolean isFirstStart;
    private boolean isClosed;
    private RecoverableMessagePump pump;

    NonSessionProcessor(ServiceBusClientBuilder builder,
        Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
        int concurrency, boolean enableAutoLockRenew, boolean enableAutoDisposition) {
        this.builder = builder;
        this.concurrency = concurrency;
        this.processError = processError;
        this.processMessage = processMessage;
        this.enableAutoLockRenew = enableAutoLockRenew;
        this.enableAutoDisposition = enableAutoDisposition;

        synchronized (lock) {
            this.isFirstStart = true;
            this.isClosed = false;
            // the internal-pump whose lifetime ends when 'close' is called. Calling 'start' post the 'close'
            // will create a new internal-pump.
            this.pump = new RecoverableMessagePump(builder, processMessage,
                processError, concurrency, enableAutoLockRenew, enableAutoDisposition);
        }
    }

    /**
     * Start requesting and pumping the messages.
     */
    public void start() {
        final boolean isResume;
        final RecoverableMessagePump pump;
        synchronized (lock) {
            if (isFirstStart()) {
                isResume = false;
            } else if (wasClosed()) {
                isResume = false;
                // 'restart' ('start' post the 'close') case, create a new internal-pump.
                this.pump = new RecoverableMessagePump(builder, processMessage,
                    processError, concurrency, enableAutoLockRenew, enableAutoDisposition);
            } else {
                isResume = true;
            }
            pump = this.pump;
        }

        if (isResume) {
            // Note: calling 'start' back to back with no interleaving 'close' or 'stop' will do that many
            // resume calls, those additional calls won't have any effect.
            pump.resume().run();
        } else {
            // first time 'start' or a 'restart' ('start' post the 'close').
            pump.begin();
        }
    }

    /**
     * Stops requesting further messages; if the broker happens to return messages requested in the past,
     * those will still be pumped post the stop. Call 'start' to resume the pumping.
     */
    public void stop() {
        synchronized (lock) {
            this.pump.pause();
        }
    }

    /**
     * Terminate pumping, call 'start' to restart the pumping.
     */
    public void close() {
        // Note: 'stop' (i.e. pause) calls after 'close' but before 'start' has no effect.
        final RecoverableMessagePump p;
        synchronized(lock) {
            this.isClosed = true;
            p = this.pump;
        }
        p.dispose();
    }

    private boolean isFirstStart() {
        if (this.isFirstStart) {
            this.isFirstStart = false;
            return true;
        }
        return false;
    }

    private boolean wasClosed() {
        if (this.isClosed) {
            this.isClosed = false;
            return true;
        }
        return false;
    }

    /**
     * The abstraction that wraps a {@link MessagePump} and transparently moves to the next 'MessagePump' when
     * the current one terminates.
     */
    private final static class RecoverableMessagePump {
        static final RuntimeException PUMP_TERMINATED = new RuntimeException("Pump is Terminated (due to Processor close).");
        private final ServiceBusClientBuilder builder;
        private final int concurrency;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final Disposable.Composite disposable = Disposables.composite();
        private final boolean enableAutoLockRenew;
        private final boolean enableAutoDisposition;
        private final Object lock = new Object();
        private MessagePump current;
        private boolean isFirstPump;
        private PumpState pumpState;

        RecoverableMessagePump(ServiceBusClientBuilder builder,
            Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
            int concurrency, boolean enableAutoLockRenew, boolean enableAutoDisposition) {
            this.builder = builder;
            this.concurrency = concurrency;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoLockRenew = enableAutoLockRenew;
            this.enableAutoDisposition = enableAutoDisposition;

            synchronized (lock) {
                this.isFirstPump = true;
                this.pumpState = PumpState.NONE;
                this.current = new MessagePump(builder.receiver().buildAsyncClient(), processMessage,
                    processError, concurrency, enableAutoLockRenew, enableAutoDisposition, false);
            }
        }

        void pause() {
            synchronized (lock) {
                pumpState = PumpState.PAUSED;
                current.pause();
            }
        }

        Runnable resume() {
            final MessagePump c;
            synchronized (lock) {
                pumpState = PumpState.RESUME;
                c = current;
            }
            // Don't hold lock as there is a potential to enter drain-loop.
            return () -> c.resume();
        }

        /**
         * Begin pumping.
         */
        void begin() {
            final Disposable d = Mono.defer(() -> {
                final MessagePump pump = nextPump();
                // Lock free when pumping message.
                return pump.begin();
            })
            .retryWhen(retryWhenSpec())
            .subscribe();

            disposable.add(d);
        }

        void dispose() {
            disposable.dispose();
        }

        private MessagePump nextPump() {
            synchronized (lock) {
                if (isFirstPump) {
                    isFirstPump = false;
                    return current;
                }
                // The next pump inherits its initial pumping state from the old pump.
                final boolean initAsPaused = pumpState == PumpState.PAUSED;
                current = new MessagePump(builder.receiver().buildAsyncClient(),
                    processMessage, processError, concurrency, enableAutoLockRenew, enableAutoDisposition, initAsPaused);
                return current;
            }
        }

        /**
         * Spec to Retry forever with a back-off until this pump is disposed of, which happens when the Processor
         * is closed.
         *
         * @return the retry spec.
         */
        private Retry retryWhenSpec() {
            return Retry.from(retrySignals -> retrySignals
                .concatMap(retrySignal -> {
                    final Retry.RetrySignal signal = retrySignal.copy();
                    final Throwable error = signal.failure();
                    if (error == null) {
                        return Mono.error(new IllegalStateException("RetrySignal::failure() not expected to be null."));
                    }
                    // to_do: log-error
                    if (disposable.isDisposed()) {
                        return Mono.error(PUMP_TERMINATED);
                    }
                    return Mono.delay(Duration.ofSeconds(10), Schedulers.boundedElastic())
                        .handle((v, sink) -> {
                            if (disposable.isDisposed()) {
                                sink.error(PUMP_TERMINATED);
                            } else {
                                sink.next(v);
                            }
                        });
                }));
        }

        private enum PumpState {
            PAUSED,
            RESUME,
            NONE
        }
    }

    /**
     * Abstraction to pump messages as long as the associated 'ServiceBusReceiverAsyncClient' is healthy.
     */
    private final static class MessagePump {
        static final RuntimeException PUMP_COMPLETED = new RuntimeException("Pump Terminated with completion.");
        private static final AtomicLong COUNTER = new AtomicLong();
        private final ClientLogger logger;
        private final ServiceBusReceiverAsyncClient receiverClient;
        private final Consumer<ServiceBusReceivedMessageContext> processMessage;
        private final Consumer<ServiceBusErrorContext> processError;
        private final boolean enableAutoLockRenew;
        private final boolean enableAutoDisposition;
        private final String fqdn;
        private final String entityPath;
        private final PausableSubscription subscription;
        private final ParallelPumping<ServiceBusReceivedMessage> parallelPumping;

        MessagePump(ServiceBusReceiverAsyncClient client,
            Consumer<ServiceBusReceivedMessageContext> processMessage, Consumer<ServiceBusErrorContext> processError,
            int concurrency, boolean enableAutoLockRenew, boolean enableAutoDisposition, boolean initAsPaused) {

            final Map<String, Object> loggingContext = new HashMap<>(1);
            loggingContext.put("id", COUNTER.incrementAndGet());
            this.logger = new ClientLogger(MessagePump.class, loggingContext);

            this.receiverClient = client;
            this.processError = processError;
            this.processMessage = processMessage;
            this.enableAutoLockRenew = enableAutoLockRenew;
            this.enableAutoDisposition = enableAutoDisposition;
            this.fqdn = client.getFullyQualifiedNamespace();
            this.entityPath = client.getEntityPath();
            this.subscription = new PausableSubscription(initAsPaused);
            this.parallelPumping = new ParallelPumping<>(concurrency);
        }

        void pause() {
            subscription.pause();
        }

        void resume() {
            subscription.resume();
        }

        /**
         * Begin pumping ('begin' is called at most once from the serialized reactor chain in 'RecoverableMessagePump').
         *
         * @return A mono that terminates when pumping encounters an error (underlying 'ServiceBusReceiverAsyncClient's
         * retry exhausted or encountered a non-retriable error, or rejection when scheduling parallelly).
         */
        Mono<Void> begin() {
            final Mono<Void> mono = Mono.create(monoSink -> {
                receiverClient.nonSessionProcessorReceiveOnNewStack()
                    .subscribe(new CoreSubscriber<ServiceBusReceivedMessage>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            subscription.setSource(s);
                            final Consumer<ServiceBusReceivedMessage> onMessage = MessagePump.this::onMessage;
                            final Mono<Void> terminatePumping = channelTerminated();
                            final Disposable d = parallelPumping.begin(onMessage, subscription, terminatePumping, monoSink);
                            monoSink.onDispose(d);
                        }

                        @Override
                        public void onNext(ServiceBusReceivedMessage m) {
                            parallelPumping.next(m);
                        }

                        @Override
                        public void onError(Throwable e) {
                            monoSink.error(e);
                            parallelPumping.error(e);
                        }

                        @Override
                        public void onComplete() {
                            monoSink.error(PUMP_COMPLETED);
                            parallelPumping.complete();
                        }
                    });
            });
            return mono;
        }

        private Mono<Void> channelTerminated() {
            return Flux.interval(Duration.ofSeconds(20))
                .handle((ignored, sink) -> {
                    if (receiverClient.isConnectionClosed()) {
                        sink.error(new RuntimeException("Channel is terminated."));
                    } else {
                        sink.next(false);
                    }
                })
                .ignoreElements()
                .then();
        }

        private void onMessage(ServiceBusReceivedMessage message) {
            final ServiceBusMessageContext messageContext = new ServiceBusMessageContext(message);
            final Throwable error = messageContext.getThrowable();
            if (error != null) {
                notifyError(error);
                return;
            }
            final Disposable lockRenewDisposable;
            if (enableAutoLockRenew) {
                lockRenewDisposable = receiverClient.beginLockRenewal(messageContext);
            } else {
                lockRenewDisposable = Disposables.disposed();
            }
            final boolean success = notifyMessage(messageContext);
            if (enableAutoDisposition) {
                if (success) {
                    complete(messageContext);
                } else {
                    abandon(messageContext);
                }
            }
            lockRenewDisposable.dispose();
        }

        private boolean notifyMessage(ServiceBusMessageContext messageContext) {
            Throwable error = null;
            try {
                processMessage.accept(new ServiceBusReceivedMessageContext(receiverClient, messageContext));
            } catch (Exception e) {
                error = e;
            }

            if (error != null) {
                final Context contextWithError = messageContext.getMessage().getContext()
                    .addData(PROCESS_ERROR_KEY, error);
                messageContext.getMessage().setContext(contextWithError);
                notifyError(new ServiceBusException(error, ServiceBusErrorSource.USER_CALLBACK));
                return false;
            }
            return true;
        }

        private void notifyError(Throwable throwable) {
            try {
                processError.accept(new ServiceBusErrorContext(throwable, fqdn, entityPath));
            } catch (Exception e) {
                logger.verbose("Ignoring error from user processError handler.", e);
            }
        }

        private void complete(ServiceBusMessageContext messageContext) {
            try {
                receiverClient.complete(messageContext.getMessage()).block();
            } catch (Exception e) {
                logger.verbose("Failed to complete message", e);
            }
        }

        private void abandon(ServiceBusMessageContext messageContext) {
            try {
                receiverClient.abandon(messageContext.getMessage()).block();
            } catch (Exception e) {
                logger.verbose("Failed to abandon message", e);
            }
        }
    }

    /**
     * A Subscription abstraction that wraps a source Subscription, the abstraction is pausable where all requests,
     * when paused, get locally accumulated. Upon resuming, the accumulated request flows as the back pressure to
     * the source.
     */
    private final static class PausableSubscription extends AtomicReference<Subscription> implements Subscription {
        private final AtomicReference<Subscription> source = new AtomicReference<>();
        private final Paused paused;
        private final Object lock = new Object();
        private boolean initAsPaused;

        PausableSubscription(boolean initAsPaused) {
            this.paused = new Paused();
            this.set(this.paused);
            synchronized (lock) {
                this.initAsPaused = initAsPaused;
            }
        }

        /**
         * Set once the source subscription for this PausableSubscription.
         *
         * @param source the source.
         */
        void setSource(Subscription source) {
            if (this.source.compareAndSet(null, source)) {
                synchronized (lock) {
                    if (initAsPaused) {
                        // If it was initialized with this flag as 'true', don't switch to source until
                        // the first 'resume' call.
                        return;
                    }
                }
                this.set(source);
            }
        }

        @Override
        public void request(long r) {
            // Request either goes to the 'Paused' Subscription (which locally accumulates requests)
            // or flows to the source Subscription.
            this.get().request(r);
        }

        void pause() {
            this.set(paused);
        }

        void resume() {
            synchronized (lock) {
                initAsPaused = false;
            }
            // Switch to 'source' and flows any accumulated requests if was paused previously.
            final Subscription s = source.get();
            if (s == null) {
                return;
            }
            this.set(s);
            final long r = paused.getAndClearAccumulated();
            if (r > 0) {
                this.get().request(r);
            }
        }

        @Override
        public void cancel() {
            final Subscription s = source.get();
            if (s == null) {
                return;
            }
            s.cancel();
        }

        /**
         * Inner Subscription represents the paused state (i.e., pause() called) and to accumulates
         * requests while paused.
         */
        private final class Paused extends AtomicLong implements Subscription {

            @Override
            public void request(long r) {
                this.addAndGet(r);
            }

            @Override
            public void cancel() {
            }

            long getAndClearAccumulated() {
                return this.getAndSet(0);
            }
        }
    }

    /**
     * Abstraction to enqueue events and pump in parallel.
     *
     * @param <T> the event type.
     */
    private final static class ParallelPumping<T> {
        private final Sinks.Many<T> sink = Sinks.many().multicast().onBackpressureBuffer();
        private final int concurrency;

        /**
         * Creates ParallelPumping.
         *
         * @param concurrency the degree of parallelism.
         */
        ParallelPumping(int concurrency) {
            this.concurrency = concurrency;
        }

        // enqueue events to pump.
        void next(T item) {
            sink.emitNext(item, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void error(Throwable e) {
            sink.emitError(e, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        void complete() {
            sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        }

        /**
         * Begin pumping events in parallel.
         *
         * @param itemConsumer the consumer to notify the event with.
         * @param subscription the subscription to request events.
         * @param terminatePumping the mono to listen, and when it signal, terminate pumping.
         * @param terminalSink the sink to notify when pumping terminates
         * @return the disposable that, if disposed of, stops the pumping.
         */
        Disposable begin(Consumer<T> itemConsumer, Subscription subscription, Mono<Void> terminatePumping, MonoSink<Void> terminalSink) {
            final OnItem<T> onItem = new OnItem<>(itemConsumer, Schedulers.boundedElastic(), subscription);
            // Begins with requesting events equal to the degree of parallelism, then upon completion of each event,
            // request the next via 'OnItem' handler.
            subscription.request(concurrency);
            final Flux<Void> consumeItemsInParallel = sink.asFlux().flatMap(onItem, concurrency, 1);
            return Mono.firstWithSignal(consumeItemsInParallel.ignoreElements(), terminatePumping).subscribe(__ -> { },
                terminalSink::error,
                () -> terminalSink.error(MessagePump.PUMP_COMPLETED));
        }

        private static final class OnItem<T> implements Function<T, Publisher<Void>> {
            private final Consumer<T> itemConsumer;
            private final Scheduler workerScheduler;
            private final Subscription subscription;

            OnItem(Consumer<T> itemConsumer, Scheduler workerScheduler, Subscription subscription) {
                this.itemConsumer = itemConsumer;
                this.workerScheduler = workerScheduler;
                this.subscription = subscription;
            }

            @Override
            public Publisher<Void> apply(T item) {
                return Mono.fromRunnable(() -> {
                    itemConsumer.accept(item);
                    subscription.request(1);
                }).subscribeOn(workerScheduler).then();
                // The subscribeOn ^ enabling parallelism.
            }
        }
    }
}
