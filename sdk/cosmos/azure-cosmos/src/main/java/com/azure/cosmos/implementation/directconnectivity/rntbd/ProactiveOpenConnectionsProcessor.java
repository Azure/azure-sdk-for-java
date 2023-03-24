// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.query.TriFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public final class ProactiveOpenConnectionsProcessor implements Closeable {

    private final Sinks.Many<Mono<OpenConnectionResponse>> openConnectionsTaskSink;
    private final List<Sinks.Many<OpenConnectionOperation>> openConnectionsOperationSinks;
    private static final int MaxOpenConnectionOpsToBufferAndShuffle = 100;
    private final Random random;

    public ProactiveOpenConnectionsProcessor(int distributionFactor) {
        this.openConnectionsTaskSink = Sinks.unsafe().many().multicast().onBackpressureBuffer();
        this.openConnectionsOperationSinks = new ArrayList<>();
        this.random = new Random();

        for (int i = 0; i < distributionFactor; i++) {
            this.openConnectionsOperationSinks.add(Sinks.unsafe().many().unicast().onBackpressureBuffer());
        }
    }

    public void submitOpenConnectionsTask(OpenConnectionOperation openConnectionOperation) {
        int sinkId = random.nextInt(this.openConnectionsOperationSinks.size());
        Sinks.Many<OpenConnectionOperation> sink = this.openConnectionsOperationSinks.get(sinkId);
        sink.tryEmitNext(openConnectionOperation);
    }

    @Override
    public void close() throws IOException {

    }

    public Flux<OpenConnectionResponse> getOpenConnectionsPublisher() {
        return openConnectionsTaskSink
                .asFlux()
                .flatMap(listMono -> listMono);
    }

    public Flux<OpenConnectionResponse> getOpenConnectionsPublisherFromOpenConnectionOperation() {
        return Flux.range(0, openConnectionsOperationSinks.size())
                .publishOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                .flatMap(integer -> openConnectionsOperationSinks.get(integer).asFlux().subscribeOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC))
                .buffer(MaxOpenConnectionOpsToBufferAndShuffle)
                .flatMap(openConnectionOperations -> {
                    Collections.shuffle(openConnectionOperations);
                    return Mono.just(openConnectionOperations);
                })
                .flatMapIterable(openConnectionOperations -> openConnectionOperations)
                .flatMap(openConnectionOperation -> BackoffRetryUtility.fluxExecuteRetry(openConnectionOperation.getOpenConnectionCallable()
                        , new ProactiveOpenConnectionsRetryPolicy()), 1, 1)
                .subscribeOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC);
    }

    private static class ProactiveOpenConnectionsRetryPolicy implements IRetryPolicy {

        private static final int MaxRetryAttempts = 4;
        private static final Duration InitialOpenConnectionReattemptBackOffInMs = Duration.ofMillis(10);
        private static final Duration MaxFailedOpenConnectionRetryWindowInMs = Duration.ofMillis(500);
        private static final int BackoffMultiplier = 2;
        private Duration currentBackoff;
        private final TimeoutHelper waitTimeTimeoutHelper;
        private final AtomicInteger retryCount;

        private ProactiveOpenConnectionsRetryPolicy() {
            this.waitTimeTimeoutHelper = new TimeoutHelper(InitialOpenConnectionReattemptBackOffInMs);
            this.retryCount = new AtomicInteger(0);
        }

        @Override
        public Mono<ShouldRetryResult> shouldRetry(Exception e) {

            if (this.waitTimeTimeoutHelper.isElapsed() || this.retryCount.get() >= MaxRetryAttempts) {
                return Mono.just(ShouldRetryResult.noRetry());
            }

            this.retryCount.incrementAndGet();

            Duration effectiveBackoff = getEffectiveBackoff(this.currentBackoff, this.waitTimeTimeoutHelper.getRemainingTime());
            this.currentBackoff = getEffectiveBackoff(Duration.ofMillis(this.currentBackoff.toMillis() * BackoffMultiplier), MaxFailedOpenConnectionRetryWindowInMs);

            return Mono.just(ShouldRetryResult.retryAfter(effectiveBackoff));
        }

        @Override
        public RetryContext getRetryContext() {
            return null;
        }

        private static Duration getEffectiveBackoff(Duration backoff, Duration remainingTime) {
            if (backoff.compareTo(remainingTime) > 0) {
                return remainingTime;
            }

            return backoff;
        }
    }
}
