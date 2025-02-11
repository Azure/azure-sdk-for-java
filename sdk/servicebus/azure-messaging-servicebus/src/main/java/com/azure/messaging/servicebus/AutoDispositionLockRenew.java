// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.util.context.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SEQUENCE_NUMBER_KEY;

/**
 * @deprecated The AutoDisposition (Complete|Abandon) and AutoLock renewal features in the Low-Level Receiver Client
 * {@link ServiceBusReceiverAsyncClient} was an oversight. These two features are slated to remove from the Low-Level
 * Receiver client; this aligns with the current expectation of the Low-Level client in other languages (e.g., NET).
 * See https://github.com/Azure/azure-sdk-for-java/issues/26084. In the V1 stack, these (to be deprecated) features
 * are implemented by {@link FluxAutoComplete} and {@link FluxAutoLockRenew} as FluxOperator. In V2, the exact behavior
 * is implemented in this type.
 * See the Notes in AutoDispositionLockRenew#Subscriber for detailed explanation why auto-XX features are flawed and
 * slated for deprecation and removal.
 */
@Deprecated
final class AutoDispositionLockRenew extends FluxOperator<ServiceBusReceivedMessage, ServiceBusReceivedMessage> {
    private final ClientLogger logger;
    private final ServiceBusReceiverAsyncClient client;
    private final boolean enableAutoDisposition;
    private final boolean enableAutoLockRenew;
    private final Semaphore dispositionLock;

    AutoDispositionLockRenew(Flux<? extends ServiceBusReceivedMessage> upstream, ServiceBusReceiverAsyncClient client,
        boolean enableAutoDisposition, boolean enableAutoLockRenew, Semaphore dispositionLock) {
        super(upstream);

        final Map<String, Object> loggingContext = new HashMap<>(2);
        loggingContext.put(FULLY_QUALIFIED_NAMESPACE_KEY, client.getFullyQualifiedNamespace());
        loggingContext.put(ENTITY_PATH_KEY, client.getEntityPath());
        this.logger = new ClientLogger(AutoDispositionLockRenew.class, loggingContext);

        this.client = client;
        this.enableAutoDisposition = enableAutoDisposition;
        this.enableAutoLockRenew = enableAutoLockRenew;
        this.dispositionLock = dispositionLock;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> actual) {
        Objects.requireNonNull(actual, "'actual' cannot be null.");
        source.subscribe(
            new Subscriber(logger, client, enableAutoDisposition, enableAutoLockRenew, dispositionLock, actual));
    }

    private static final class Subscriber extends BaseSubscriber<ServiceBusReceivedMessage> {
        private final ClientLogger logger;
        private final ServiceBusReceiverAsyncClient client;
        private final boolean enableAutoDisposition;
        private final boolean enableAutoLockRenew;
        private final Semaphore dispositionLock;
        private final CoreSubscriber<? super ServiceBusReceivedMessage> downstream;

        Subscriber(ClientLogger logger, ServiceBusReceiverAsyncClient client, boolean enableAutoDisposition,
            boolean enableAutoLockRenew, Semaphore dispositionLock,
            CoreSubscriber<? super ServiceBusReceivedMessage> downstream) {
            this.logger = logger;
            this.client = client;
            this.enableAutoDisposition = enableAutoDisposition;
            this.enableAutoLockRenew = enableAutoLockRenew;
            this.dispositionLock = dispositionLock;
            this.downstream = downstream;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            logger.atInfo().log("Subscription received. Subscribing downstream. {}", subscription);
            downstream.onSubscribe(this);
        }

        @Override
        protected void hookOnNext(ServiceBusReceivedMessage message) {
            final Disposable lockRenewDisposable
                = enableAutoLockRenew ? client.beginLockRenewal(message) : Disposables.disposed();

            final String seqNumber = message != null ? String.valueOf(message.getSequenceNumber()) : "n/a";

            logger.atVerbose().addKeyValue(SEQUENCE_NUMBER_KEY, seqNumber).log("onNext: Passing message downstream.");

            if (enableAutoDisposition) {
                try {
                    dispositionLock.acquire();
                } catch (InterruptedException e) {
                    logger.atInfo()
                        .addKeyValue(SEQUENCE_NUMBER_KEY, seqNumber)
                        .log("Unable to acquire dispositionLock.", e);
                }
            }

            try {
                downstream.onNext(message);
                // Note_1: The onNext returning doesn't mean downstream completed its message processing. Per Reactive
                // patterns, the onNext may buffer the message to a queue for a delayed processing or switch thread and
                // return immediately. It means this type performs message disposition before the actual business logic
                // finish processing the message. This makes the auto-disposition feature flawed and application must
                // opt out auto-disposition via 'ServiceBusClientBuilder#disableAutoComplete()' when building
                // 'ServiceBusReceiverAsyncClient'.
                disposition(message, seqNumber, true);
            } catch (Exception e) {
                logger.atError()
                    .addKeyValue(SEQUENCE_NUMBER_KEY, seqNumber)
                    .log("Error occurred when downstream processing message.", e);
                // Note_2: There is no guarantee that the above onNext is the last subscriber in the pipeline running
                // the application logic, so try-catch on the above onNext to inspect if any error is flawed.
                disposition(message, seqNumber, false);
            } finally {
                if (enableAutoDisposition) {
                    // Note_3: Assuming downstream.onNext is the last one in the pipeline (unfortunate assumption to
                    // support deprecated flawed auto-disposition), this semaphore is the best effort to make the final
                    // disposition attempt in the async chain work. E.g., the application applied take(2); after
                    // the seeing second onNext (on thread_1), the take-operator emits completion, and the app may close
                    // the client on the app's main thread, but then onNext returns to the call-site in this operator
                    // (i.e. above downstream.onNext(message) returns) on thread_1 and attempt to disposition which fails
                    // due to closed client.
                    //
                    dispositionLock.release();
                    //
                    // Note_4: While auto-disposition is flawed, with the feature out there, the best we can assume is -
                    // above onNext is the final in the pipeline, so stop recurring renewal (i.e., stop the auto-renew).
                    // On the other hand, if auto-disposition is not enabled, so user invokes the disposition manually,
                    // then eventually, the recurring renewal terminates when the broker finds an attempt to renew
                    // the disposition-ed message and returns an error.
                    lockRenewDisposable.dispose();
                }
                logger.atVerbose().addKeyValue(SEQUENCE_NUMBER_KEY, seqNumber).log("onNext: Finished.");
                //
                // Finally, Note_1, Note_2, Note_3 and Note_4 explains why the entire auto-XX features are flawed and
                // that we want to deprecate and simplify it by aligning with other language's (e.g. .NET) low level
                // client.
            }
        }

        protected void hookOnError(Throwable throwable) {
            logger.atInfo().log("Propagating upstream error signal to downstream.", throwable);
            downstream.onError(throwable);
        }

        @Override
        protected void hookOnComplete() {
            logger.atInfo().log("Propagating upstream completion signal to downstream.");
            downstream.onComplete();
        }

        @Override
        public Context currentContext() {
            return downstream.currentContext();
        }

        private void disposition(ServiceBusReceivedMessage message, String seqNumber, boolean isComplete) {
            if (!enableAutoDisposition) {
                return;
            }
            if (message != null && message.isSettled()) {
                return;
            }
            try {
                if (isComplete) {
                    client.complete(message).block();
                } else {
                    client.abandon(message).block();
                }
            } catch (Exception e) {
                logger.atWarning()
                    .addKeyValue(SEQUENCE_NUMBER_KEY, seqNumber)
                    .log("Unable to '{}' message, cancelling the message streaming.",
                        isComplete ? "Complete" : "Abandon", e);
                upstream().cancel();
                onError(e);
            }
        }
    }
}
