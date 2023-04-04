// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.OpenConnectionAggressivenessHint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.Sinks;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class ProactiveOpenConnectionsProcessor implements Closeable {
    private Sinks.Many<OpenConnectionOperation> openConnectionsTaskSink;
    private Sinks.Many<OpenConnectionOperation> openConnectionsTaskSinkBackUp;
    private final ConcurrentHashMap<String, Integer> endpointToMinConnections;
    private static final int MaxConnectionsToOpenAcrossAllEndpoints = 50_000;
    private final AtomicInteger totalEstablishedConnections;
    private final AtomicReference<OpenConnectionAggressivenessHint> aggressivenessHint;

    public ProactiveOpenConnectionsProcessor() {
        this.openConnectionsTaskSink = Sinks.many().multicast().onBackpressureBuffer();
        this.openConnectionsTaskSinkBackUp = Sinks.many().multicast().onBackpressureBuffer();
        this.aggressivenessHint = new AtomicReference<>(OpenConnectionAggressivenessHint.AGGRESSIVE);
        this.totalEstablishedConnections = new AtomicInteger(0);
        this.endpointToMinConnections = new ConcurrentHashMap<>();
    }

    public synchronized void submitOpenConnectionTask(OpenConnectionOperation openConnectionOperation) {

        String addressUriAsString = openConnectionOperation.getAddressUri().getURIAsString();

        boolean isEndpointTaskInSink = endpointToMinConnections.get(addressUriAsString) != null;

        endpointToMinConnections.putIfAbsent(addressUriAsString, openConnectionOperation.getMinConnectionsRequiredForEndpoint());
        endpointToMinConnections.computeIfPresent(addressUriAsString, (s, min) -> {
            openConnectionOperation.setMinConnectionsRequiredForEndpoint(Math.max(openConnectionOperation.getMinConnectionsRequiredForEndpoint(), min));
            return Math.max(openConnectionOperation.getMinConnectionsRequiredForEndpoint(), min);
        });

        if (!isEndpointTaskInSink) {
            openConnectionsTaskSink.tryEmitNext(openConnectionOperation);
            openConnectionsTaskSinkBackUp.tryEmitNext(openConnectionOperation);
        }
    }

    @Override
    public void close() throws IOException {
        openConnectionsTaskSink.tryEmitComplete();
        openConnectionsTaskSinkBackUp.tryEmitComplete();
    }

    public ParallelFlux<OpenConnectionResponse> getOpenConnectionsPublisher() {
        // aggressive concurrency
        int concurrency = Configs.getCPUCnt();

        // defensive concurrency
        if (aggressivenessHint.get() == OpenConnectionAggressivenessHint.DEFENSIVE) {
            concurrency = 1;
        }

        return openConnectionsTaskSink.asFlux()
                .parallel(concurrency)
                .runOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                .flatMap(openConnectionOperation -> {
                    if (totalEstablishedConnections.get() < MaxConnectionsToOpenAcrossAllEndpoints) {

                        endpointToMinConnections.remove(openConnectionOperation.getAddressUri().getURIAsString());
                        IOpenConnectionsHandler openConnectionsHandler = openConnectionOperation.getOpenConnectionsHandler();
                        Uri addressUri = openConnectionOperation.getAddressUri();
                        URI serviceEndpoint = openConnectionOperation.getServiceEndpoint();
                        int minConnectionsForEndpoint = openConnectionOperation.getMinConnectionsRequiredForEndpoint();
                        OpenConnectionAggressivenessHint hint = aggressivenessHint.get();
                        String collectionRid = openConnectionOperation.getCollectionRid();

                        return Flux.zip(Mono.just(openConnectionOperation), openConnectionsHandler.openConnections(
                                collectionRid,
                                serviceEndpoint,
                                Arrays.asList(addressUri),
                                minConnectionsForEndpoint
                        ));
                    }
                    return Flux.empty();
                }).log("Line 98")
                .flatMap(openConnectionOpToResponse -> {
                    OpenConnectionOperation openConnectionOperation = openConnectionOpToResponse.getT1();
                    OpenConnectionResponse openConnectionResponse = openConnectionOpToResponse.getT2();

                    if (openConnectionResponse.isConnected()) {
                        totalEstablishedConnections.incrementAndGet();
                        return Mono.just(openConnectionResponse);
                    }

                    return openConnectionOperation
                            .getRetryPolicy()
                            .shouldRetry((Exception) openConnectionResponse.getException())
                            .flatMap(shouldRetryResult -> {
                                if (shouldRetryResult.shouldRetry) {
                                    return enqueueOpenConnectionOpsForRetry(openConnectionOperation, shouldRetryResult);
                                }
                                return Mono.just(openConnectionResponse);
                            });
                });
    }

    public void reinstantiateOpenConnectionsPublisherAndSubscribe() {
        this.toggleOpenConnectionsAggressiveness();
        this.instantiateOpenConnectionsPublisher();
        this.getOpenConnectionsPublisher().subscribe();
    }

    private Mono<OpenConnectionResponse> enqueueOpenConnectionOpsForRetry(
            OpenConnectionOperation op,
            ShouldRetryResult retryResult
    ) {
        if (retryResult.backOffTime == Duration.ZERO || retryResult.backOffTime == null) {
            this.submitOpenConnectionTask(op);
            return Mono.empty();
        } else {
            return Mono
                    .delay(retryResult.backOffTime)
                    .flatMap(ignore -> {
                        this.submitOpenConnectionTask(op);
                        return Mono.empty();
                    });
        }
    }

    // when the flux associated with openConnectionsTaskSink is cancelled
    // this method provides for a way to resume emitting pushed tasks to
    // the sink
    private synchronized void instantiateOpenConnectionsPublisher() {
        openConnectionsTaskSink = openConnectionsTaskSinkBackUp;
        openConnectionsTaskSinkBackUp = Sinks.many().multicast().onBackpressureBuffer();
    }

    private synchronized void toggleOpenConnectionsAggressiveness() {
        if (aggressivenessHint.get() == OpenConnectionAggressivenessHint.AGGRESSIVE) {
            aggressivenessHint.set(OpenConnectionAggressivenessHint.DEFENSIVE);
        } else {
            aggressivenessHint.set(OpenConnectionAggressivenessHint.AGGRESSIVE);
        }
    }

    public void decrementTotalConnectionCount() {
        totalEstablishedConnections.decrementAndGet();
    }
}
