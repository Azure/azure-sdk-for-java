// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of {@link HttpResponse.BodySubscriber} that emits the response body as a {@link Flow} of {@link List}
 * {@link ByteBuffer} while tracking a timeout for each value emitted by the subscription.
 * <p>
 * This differs from {@link HttpResponse.BodySubscribers#ofPublisher()} in that it tracks a timeout for each value
 * emitted by the subscription. This is needed to offer better reliability when reading the response in cases where the
 * server is either slow to respond or the connection has dropped without a signal from the server.
 */
public final class FlowableTimeoutResponseSubscriber
    implements HttpResponse.BodySubscriber<Flow.Publisher<List<ByteBuffer>>> {
    private static final ClientLogger LOGGER = new ClientLogger(FlowableTimeoutResponseSubscriber.class);

    private final CompletableFuture<Flow.Subscription> subscriptionCF = new CompletableFuture<>();
    private final CompletableFuture<SubscriberRef> subscribedCF = new CompletableFuture<>();
    private final AtomicReference<SubscriberRef> subscriberRef = new AtomicReference<>();
    private final CompletionStage<Flow.Publisher<List<ByteBuffer>>> body
        = subscriptionCF.thenCompose(ignored -> CompletableFuture.completedFuture(this::subscribe));

    // We use the completionCF to ensure that only one of
    // onError or onComplete is ever called.
    private final CompletableFuture<Void> completionCF;

    private final long readTimeout;

    /**
     * Creates a response body subscriber that emits the response body as a {@link Flow} of {@link List}
     * {@link ByteBuffer} while tracking a timeout for each value emitted by the subscription.
     *
     * @param readTimeout The timeout for reading each value emitted by the subscription.
     */
    public FlowableTimeoutResponseSubscriber(long readTimeout) {
        completionCF = new CompletableFuture<>();
        completionCF.whenComplete((r, t) -> subscribedCF.thenAccept(s -> complete(s, t)));
        this.readTimeout = readTimeout;
    }

    // An object that holds a reference to a Flow.Subscriber.
    // The reference is cleared when the subscriber is completed - either
    // normally or exceptionally, or when the subscription is cancelled.
    static final class SubscriberRef {
        volatile Flow.Subscriber<? super List<ByteBuffer>> ref;

        SubscriberRef(Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
            ref = subscriber;
        }

        Flow.Subscriber<? super List<ByteBuffer>> get() {
            return ref;
        }

        Flow.Subscriber<? super List<ByteBuffer>> clear() {
            Flow.Subscriber<? super List<ByteBuffer>> res = ref;
            ref = null;
            return res;
        }
    }

    // A subscription that wraps an upstream subscription and
    // holds a reference to a subscriber. The subscriber reference
    // is cleared when the subscription is cancelled
    static final class SubscriptionRef implements Flow.Subscription {
        final Flow.Subscription subscription;
        final SubscriberRef subscriberRef;

        SubscriptionRef(Flow.Subscription subscription, SubscriberRef subscriberRef) {
            this.subscription = subscription;
            this.subscriberRef = subscriberRef;
        }

        @Override
        public void request(long n) {
            if (subscriberRef.get() != null) {
                subscription.request(n);
            }
        }

        @Override
        public void cancel() {
            subscription.cancel();
            subscriberRef.clear();
        }

        void subscribe() {
            Flow.Subscriber<?> subscriber = subscriberRef.get();
            if (subscriber != null) {
                subscriber.onSubscribe(this);
            }
        }

        @Override
        public String toString() {
            return "SubscriptionRef/" + subscription.getClass().getName() + "@" + System.identityHashCode(subscription);
        }
    }

    // This is a callback for the subscribedCF.
    // Do not call directly!
    private void complete(SubscriberRef ref, Throwable t) {
        assert ref != null;
        Flow.Subscriber<?> s = ref.clear();
        // maybe null if subscription was cancelled
        if (s == null)
            return;
        if (t == null) {
            try {
                s.onComplete();
            } catch (Throwable x) {
                s.onError(x);
            }
        } else {
            s.onError(t);
        }
    }

    private void signalError(Throwable err) {
        if (err == null) {
            err = new NullPointerException("null throwable");
        }
        completionCF.completeExceptionally(err);
    }

    private void signalComplete() {
        completionCF.complete(null);
    }

    private void subscribe(Flow.Subscriber<? super List<ByteBuffer>> subscriber) {
        Objects.requireNonNull(subscriber, "subscriber must not be null");
        SubscriberRef ref = new SubscriberRef(subscriber);
        if (subscriberRef.compareAndSet(null, ref)) {
            subscriptionCF.thenAccept((s) -> {
                SubscriptionRef subscription = new SubscriptionRef(s, ref);
                try {
                    subscription.subscribe();
                    subscribedCF.complete(ref);
                } catch (Throwable t) {
                    LOGGER.warning("Failed to call onSubscribe: cancelling subscription: " + t);
                    subscription.cancel();
                }
            });
        } else {
            subscriber.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                }

                @Override
                public void cancel() {
                }
            });
            subscriber.onError(new IllegalStateException("This publisher has already one subscriber"));
        }
    }

    private final AtomicBoolean subscribed = new AtomicBoolean();

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        Objects.requireNonNull(subscription);
        if (!subscribed.compareAndSet(false, true)) {
            subscription.cancel();
        } else {
            subscriptionCF.complete(subscription);
        }
    }

    @Override
    public void onNext(List<ByteBuffer> item) {
        Objects.requireNonNull(item);
        try {
            // cannot be called before onSubscribe()
            assert subscriptionCF.isDone();
            SubscriberRef ref = subscriberRef.get();
            // cannot be called before subscriber calls request(1)
            assert ref != null;
            Flow.Subscriber<? super List<ByteBuffer>> subscriber = ref.get();
            if (subscriber != null) {
                // may be null if subscription was cancelled.
                subscriber.onNext(item);
            }
        } catch (Throwable err) {
            signalError(err);
            subscriptionCF.thenAccept(Flow.Subscription::cancel);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // cannot be called before onSubscribe();
        assert suppress(subscriptionCF.isDone(), "onError called before onSubscribe", throwable);
        // onError can be called before request(1), and therefore can
        // be called before subscriberRef is set.
        signalError(throwable);
        Objects.requireNonNull(throwable);
    }

    @Override
    public void onComplete() {
        // cannot be called before onSubscribe()
        if (!subscriptionCF.isDone()) {
            signalError(new InternalError("onComplete called before onSubscribed"));
        } else {
            // onComplete can be called before request(1),
            // and therefore can be called before subscriberRef
            // is set.
            signalComplete();
        }
    }

    @Override
    public CompletionStage<Flow.Publisher<List<ByteBuffer>>> getBody() {
        return body;
    }

    private boolean suppress(boolean condition, String assertion, Throwable carrier) {
        if (!condition) {
            if (carrier != null) {
                carrier.addSuppressed(new AssertionError(assertion));
            } else {
                LOGGER.logThrowableAsWarning(new AssertionError(assertion));
            }
        }
        return true;
    }
}
