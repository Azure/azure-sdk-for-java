// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processor that listens to upstream messages, pushes them downstream then completes it if necessary.
 */
class ServiceBusMessageProcessor extends FluxProcessor<ServiceBusReceivedMessage, ServiceBusReceivedMessage>
    implements Subscription {
    private final ClientLogger logger = new ClientLogger(ServiceBusMessageProcessor.class);
    private final boolean isAutoComplete;
    private final AmqpRetryOptions retryOptions;
    private final Function<ServiceBusReceivedMessage, Mono<Void>> completeFunction;
    private final Function<ServiceBusReceivedMessage, Mono<Void>> onAbandon;
    private final Function<ServiceBusReceivedMessage, Mono<Instant>> renewLockFunction;
    private final Deque<ServiceBusReceivedMessage> messageQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, PendingComplete> pendingCompletes = new HashMap<>();
    private final boolean autoLockRenewal;
    private final Duration maxAutoLockRenewalDuration;
    private final Mono<ServiceBusManagementNode> managementNodeMono;

    //private final Map<UUID, Disposable> autoLockRenewMap = new HashMap<>();

    ServiceBusMessageProcessor(boolean isAutoComplete, AmqpRetryOptions retryOptions,
        Function<ServiceBusReceivedMessage, Mono<Void>> completeFunction,
        Function<ServiceBusReceivedMessage, Mono<Void>> onAbandon,
        boolean autoLockRenewal, Duration maxAutoLockRenewalDuration,
        Mono<ServiceBusManagementNode> managementNodeMono,
        Function<ServiceBusReceivedMessage, Mono<Instant>> renewLockFunction) {
        super();
        this.isAutoComplete = isAutoComplete;
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.completeFunction = Objects.requireNonNull(completeFunction, "'completeFunction' cannot be null.");
        this.onAbandon = Objects.requireNonNull(onAbandon, "'onAbandon' cannot be null.");
        this.autoLockRenewal = autoLockRenewal;
        this.maxAutoLockRenewalDuration = maxAutoLockRenewalDuration;
        this.managementNodeMono = managementNodeMono;
        this.renewLockFunction = renewLockFunction;
    }

    private volatile boolean isDone;
    private volatile CoreSubscriber<? super ServiceBusReceivedMessage> downstream;
    private volatile boolean isCancelled;

    volatile Subscription upstream;
    private static final AtomicReferenceFieldUpdater<ServiceBusMessageProcessor, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, Subscription.class,
            "upstream");

    volatile int once;
    static final AtomicIntegerFieldUpdater<ServiceBusMessageProcessor> ONCE =
        AtomicIntegerFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, "once");

    volatile int wip;
    static final AtomicIntegerFieldUpdater<ServiceBusMessageProcessor> WIP =
        AtomicIntegerFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, "wip");

    volatile long requested;
    static final AtomicLongFieldUpdater<ServiceBusMessageProcessor> REQUESTED =
        AtomicLongFieldUpdater.newUpdater(ServiceBusMessageProcessor.class, "requested");

    private volatile Throwable error;

    /**
     * Invoked when this subscribes to an upstream publisher.
     *
     * @param subscription Subscription for the upstream publisher.
     */
    @Override
    public void onSubscribe(Subscription subscription) {
        Objects.requireNonNull(subscription, "'subscription' cannot be null.");

        if (Operators.setOnce(UPSTREAM, this, subscription)) {
            subscription.request(1);
        } else {
            logger.warning("This processor cannot be subscribed to with multiple upstreams.");
        }
    }

    @Override
    public boolean isTerminated() {
        return isDone || isCancelled;
    }

    @Override
    public void onNext(ServiceBusReceivedMessage message) {
        if (isTerminated()) {
            final Context context = downstream == null ? currentContext() : downstream.currentContext();
            Operators.onNextDropped(message, context);

            return;
        }

        messageQueue.add(message);
        print(this.getClass(), "onNext", "Going to drain the queue .. ");
        drain();
    }

    /**
     * Invoked when an error occurs upstream.
     *
     * @param throwable Error that occurred upstream.
     */
    @Override
    public void onError(Throwable throwable) {
        if (isDone || isCancelled) {
            logger.error("Exception occurred from upstream when this is already terminated.", throwable);
            Operators.onErrorDropped(throwable, currentContext());
            return;
        }

        error = throwable;
        isDone = true;

        drain();
    }

    /**
     * Invoked when upstream has finished emitting items.
     */
    @Override
    public void onComplete() {
        if (isDone) {
            return;
        }

        isDone = true;
        print(this.getClass(), "onComplete", "Going to drain the queue .. ");
        drain();
    }

    @Override
    public void request(long request) {
        print(this.getClass(), "request", "Entry .. ");
        logger.info("Back-pressure request: {}", request);
        if (Operators.validate(request)) {
            Operators.addCap(REQUESTED, this, request);

            if (upstream != null) {
                upstream.request(request);
            }
            print(this.getClass(), "request", "Going to drain the queue .. ");
            drain();
        }
    }

    @Override
    public void cancel() {
        if (isCancelled) {
            return;
        }

        logger.info("Cancelling subscription.");
        isCancelled = true;
        print(this.getClass(), "cancel", "Going to drain the queue .. ");
        drain();
    }

    @Override
    public void dispose() {
        if (isDone) {
            return;
        }

        logger.info("Disposing subscription.");
        isDone = true;
        print(this.getClass(), "dispose", "Going to drain the queue .. ");
        drain();
    }

    public Mono<Void> disposeAsync() {
        dispose();

        if (pendingCompletes.isEmpty()) {
            return Mono.empty();
        }

        final List<Mono<Void>> pending = pendingCompletes.keySet()
            .stream()
            .map(key -> {
                final PendingComplete entry = pendingCompletes.get(key);
                return entry != null ? entry.getOnComplete() : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return Mono.when(pending).then(Mono.fromRunnable(() -> pendingCompletes.clear()));
    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> downstream) {
        Objects.requireNonNull(downstream, "'downstream' cannot be null.");
        if (once == 0 && ONCE.compareAndSet(this, 0, 1)) {
            downstream.onSubscribe(this);
            this.downstream = downstream;
            if (isCancelled) {
                this.downstream = null;
            } else {
                print(this.getClass(), "subscribe", "Going to drain the queue .. ");
                drain();
            }
        } else {
            Operators.error(downstream,
                new IllegalStateException("ServiceBusMessageSubscriber can only have one subscriber."));
        }
    }

    private void drain() {
        // If someone is already in this loop, then we are already clearing the queue.
        if (!WIP.compareAndSet(this, 0, 1)) {
            return;
        }

        try {
            print(this.getClass(), "drain", "Going to call drainQueue() .. ");
            drainQueue();
        } finally {
            if (WIP.decrementAndGet(this) != 0) {
                logger.warning("There is another worker in drainLoop. But there should only be 1 worker.");
            }
        }
    }

    private void drainQueue() {
        if (downstream == null) {
            return;
        }

        final ServiceBusReceivedMessage lastMessage = messageQueue.peekLast();
        if (lastMessage == null) {
            if (isDone) {
                downstream.onComplete();
            }
            return;
        }

        ServiceBusReceivedMessage message = messageQueue.poll();
        while (message != lastMessage) {
            if (message == null) {
                logger.warning("The last message is not null, but the head node is null. lastMessage: {}", lastMessage);
                message = messageQueue.poll();
                continue;
            }

            if (isCancelled) {
                Operators.onDiscard(message, downstream.currentContext());
                Operators.onDiscardQueueWithClear(messageQueue, downstream.currentContext(), null);
                return;
            }

            print(this.getClass(), "drainQueue" , message.getLockToken() + "( "+new String(message.getBody())+") calling next() with this message" );
            next(message);

            message = messageQueue.poll();
        }

        // Emit the message which is equal to lastMessage.
        print(this.getClass(), "drainQueue" , message.getLockToken() + " ( "+new String(message.getBody())+") calling next() with this last message" );

        next(message);
        print(this.getClass(), "drainQueue" , message.getLockToken() + " ( "+new String(message.getBody())+") After calling next() isDone = "+ isDone );

        if (isDone) {
            if (error != null) {
                downstream.onError(error);
            } else if (messageQueue.peekLast() == null) {
                print(this.getClass(), "drainQueue", "Going to call downstream.onComplete .. ");
                downstream.onComplete();
            }
        }
    }

    // TODO : Here  look for Disposable.. to renewLock autometically
    private void next(ServiceBusReceivedMessage message) {
        AtomicReference<Disposable> disposeRenewLockTimer = new  AtomicReference<Disposable>();
        final int renewLockAfterSeconds = 5;

        print(this.getClass(), "next" , "Entry " + message.getLockToken() + ", " + new String(message.getBody()));
        final UUID lockToken = message.getLockToken();
        final boolean isCompleteMessage = isAutoComplete && lockToken != null
            && !MessageUtils.ZERO_LOCK_TOKEN.equals(lockToken);

        logger.info("sequenceNumber[{}]. lock[{}]. Auto-complete message? [{}]",
            message.getSequenceNumber(), lockToken, isCompleteMessage);

        final PendingComplete pendingComplete = new PendingComplete(message);
        if (isCompleteMessage) {
            pendingCompletes.put(lockToken, pendingComplete);
        }
        if (autoLockRenewal) {
            Disposable renewLockDisposable = renewLockFunction.apply(message)
                .repeat()
                .delayElements(Duration.ofSeconds(renewLockAfterSeconds))
                .doOnNext(instant -> {
                    // This will ensure that we are getting valid refresh time
                    if (instant != null) {
                        logger.info(" Received new refresh time " + instant);
                        print(this.getClass(), "next" , "!!!!! Received new refresh time " + instant);

                    }
                })
                .subscribe();

            disposeRenewLockTimer.set(renewLockDisposable);

            // autoLockRenewMap.put(message.getLockToken(), disposeRenewLockTimer);
        }

        try {
            print(this.getClass(), "next" , "calling downstream.onNext  " + message.getLockToken() + ", Calling downstream onNext");
            downstream.onNext(message);
        } catch (Exception e) {
            logger.error("Exception occurred while handling downstream onNext operation.", e);

            final PendingComplete pending = pendingCompletes.get(lockToken);
            if (isCompleteMessage && pending != null) {
                logger.info("Abandoning message lock: {}", lockToken);
                onAbandon.apply(message)
                    .onErrorStop()
                    .doOnError(error -> {
                        logger.warning("Could not abandon message with lock: {}", lockToken, error);
                        pending.error(error);
                    })
                    .doOnSuccess(unused -> pending.complete())
                    .doFinally(signal -> {
                        logger.info("lock[{}]. Abandon status: [{}]", lockToken, signal);
                        pendingCompletes.remove(lockToken);
                    })
                    .block();
            }

            downstream.onError(Operators.onOperatorError(upstream, e, message, downstream.currentContext()));
        }

        try {
            // check that the pending operation is in the queue and not running yet.
            final PendingComplete pending = pendingCompletes.get(lockToken);
            if (isCompleteMessage && pending != null && !pending.isRunningGetAndSet()) {
                final ServiceBusReceivedMessage completedMessage = pending.getMessage();

                logger.info("sequenceNumber[{}]. lock[{}]. Completing message.",
                    completedMessage.getSequenceNumber(), completedMessage.getLockToken());

                completeFunction.apply(completedMessage)
                    .onErrorStop()
                    .doOnError(error -> {
                        logger.warning("Could not complete message with lock: {}",
                            completedMessage.getLockToken(), error);

                        pending.error(error);
                    })
                    .doOnSuccess(ignored -> pending.complete())
                    .doFinally(signal -> {
                        logger.info("lock[{}]. Complete status: [{}]", completedMessage.getLockToken(), signal);

                        pendingCompletes.remove(lockToken);
                        print(this.getClass(), "next" , " pendingCompletes.remove" + message.getLockToken() + ", After removing from pendingCompletes");

                    })
                    .block(retryOptions.getTryTimeout());
            } else {
                print(this.getClass(), "next" , " Token :" + message.getLockToken() + ", isCompleteMessage = "+ isCompleteMessage  + ", pending isrunning: "+ pending);

            }

            //TODO(hemanttanwar) : dispose AutoLockRenew subscriber
            if (autoLockRenewal
                && disposeRenewLockTimer.get() != null
                && !disposeRenewLockTimer.get().isDisposed()){
                // dispose the renewLockTimer : if it was started in first place
                print(this.getClass(), "next" , " Token :" + message.getLockToken() + ", Stopping auto lock Renew subscriber.");
                disposeRenewLockTimer.get().dispose();
            }
        } catch (Exception e) {
            logger.error("Exception occurred while auto-completing message. Sequence: {}. Lock token: {}",
                message.getSequenceNumber(), message.getLockToken(), e);
            e.printStackTrace();
            downstream.onError(Operators.onOperatorError(upstream, e, message, downstream.currentContext()));
        }
        print(this.getClass(), "next" , "  " + message.getLockToken() + ", Exit ..");

    }

    private Instant renewLock(ServiceBusReceivedMessage message){
        print(getClass(), "renewLock", "  Renew Lock here  by making call to ManagementChannel ... lock token =" + message.getLockToken());
        return Instant.now().plusSeconds(30);
    }

    private static final class PendingComplete {
        private final ServiceBusReceivedMessage message;
        private final AtomicBoolean isRunning = new AtomicBoolean();
        private final Mono<Void> onComplete;
        private MonoSink<Void> sink;

        private PendingComplete(ServiceBusReceivedMessage message) {
            this.message = message;
            this.onComplete = Mono.create(sink -> {
                this.sink = sink;
            });
            onComplete.subscribe();
        }

        private Mono<Void> getOnComplete() {
            return onComplete;
        }

        private boolean isRunningGetAndSet() {
            return isRunning.getAndSet(true);
        }

        private void error(Throwable throwable) {
            sink.error(throwable);
        }

        private void complete() {
            sink.success();
        }

        private ServiceBusReceivedMessage getMessage() {
            return message;
        }
    }
    private void print(Class clazz, String method, String message) {
        System.out.println(clazz.getName() + "." +  method + " " + message);
    }
}
