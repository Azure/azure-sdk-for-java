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
import reactor.core.scheduler.Schedulers;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class ProactiveOpenConnectionsProcessor implements Closeable {
    private Sinks.Many<OpenConnectionOperation> openConnectionsTaskSink;
    private Sinks.Many<OpenConnectionOperation> openConnectionsTaskSinkBackUp;
    private ConcurrentHashMap<String, Integer> tasksInSink;
    private static final int MaxConnectionsToOpenAcrossAllEndpoints = 50_000;
    private final AtomicInteger totalEstablishedConnections;
    private final AtomicReference<OpenConnectionAggressivenessHint> aggressivenessHint;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private ParallelFlux<OpenConnectionResponse> parallelFlux;

    public ProactiveOpenConnectionsProcessor() {
        this.openConnectionsTaskSink = Sinks.many().multicast().onBackpressureBuffer();
        this.openConnectionsTaskSinkBackUp = Sinks.many().multicast().onBackpressureBuffer();
        this.aggressivenessHint = new AtomicReference<>(OpenConnectionAggressivenessHint.AGGRESSIVE);
        this.totalEstablishedConnections = new AtomicInteger(0);
        this.tasksInSink = new ConcurrentHashMap<>();
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(Configs.getCPUCnt());
    }

    public synchronized void submitOpenConnectionsTask(OpenConnectionOperation openConnectionOperation) {

        String addressUriAsString = openConnectionOperation.getAddressUri().getURIAsString();

        boolean isEndpointTaskInSink = tasksInSink.get(addressUriAsString) != null;

        tasksInSink.putIfAbsent(addressUriAsString, openConnectionOperation.getMinConnectionsRequiredForEndpoint());
        tasksInSink.computeIfPresent(addressUriAsString, (s, min) -> {
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

    }

    public synchronized void toggleOpenConnectionsAggressiveness() {
        System.out.println("Toggle executor aggressiveness");
        if (aggressivenessHint.get() == OpenConnectionAggressivenessHint.AGGRESSIVE) {
            aggressivenessHint.set(OpenConnectionAggressivenessHint.DEFENSIVE);
            scheduledThreadPoolExecutor.setCorePoolSize(1);
        } else {
            aggressivenessHint.set(OpenConnectionAggressivenessHint.AGGRESSIVE);
            scheduledThreadPoolExecutor.setCorePoolSize(Configs.getCPUCnt());
        }
    }

    public ParallelFlux<OpenConnectionResponse> getFlux() {
        return parallelFlux;
    }

    public ParallelFlux<OpenConnectionResponse> getOpenConnectionsPublisherFromOpenConnectionOperation() {
        // steady-state concurrency
        int concurrency = Configs.getCPUCnt();

        // background execution concurrency
        if (aggressivenessHint.get() == OpenConnectionAggressivenessHint.DEFENSIVE) {
            concurrency = 1;
        }

        return Flux.from(openConnectionsTaskSink.asFlux())
                .parallel(concurrency)
                .runOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC).log("Line 90")
                .flatMap(openConnectionOperation -> {
                    if (totalEstablishedConnections.get() < MaxConnectionsToOpenAcrossAllEndpoints) {
                        tasksInSink.remove(openConnectionOperation.getAddressUri().getURIAsString());
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
                })
                .flatMap(openConnectionOpToResponse -> {
                    OpenConnectionOperation openConnectionOperation = openConnectionOpToResponse.getT1();
                    OpenConnectionResponse openConnectionResponse = openConnectionOpToResponse.getT2();

                    if (openConnectionResponse.isConnected()) {
                        totalEstablishedConnections.incrementAndGet();
                        return Mono.empty();
                    }

                    return openConnectionOperation
                            .getRetryPolicy()
                            .shouldRetry((Exception) openConnectionResponse.getException())
                            .flatMap(shouldRetryResult -> {
                                if (shouldRetryResult.shouldRetry) {
                                    return enqueueOpenConnectionOpsForRetry(openConnectionOperation, shouldRetryResult);
                                }
                                return Mono.empty();
                            });
                });
    }

    private Mono<OpenConnectionResponse> enqueueOpenConnectionOpsForRetry(
        OpenConnectionOperation op,
        ShouldRetryResult retryResult
    ) {
        if (retryResult.backOffTime == Duration.ZERO || retryResult.backOffTime == null) {
            this.submitOpenConnectionsTask(op);
            return Mono.empty();
        } else {
            return Mono
                    .delay(retryResult.backOffTime)
                    .flatMap(ignore -> {
                        this.submitOpenConnectionsTask(op);
                        return Mono.empty();
                    });
        }
    }

    // when the flux associated with openConnectionsTaskSink is cancelled
    // this method provides for a way to resume emitting pushed tasks to
    // the sink
    public synchronized void instantiateOpenConnectionsPublisher() {
        openConnectionsTaskSink = openConnectionsTaskSinkBackUp;
        openConnectionsTaskSinkBackUp = Sinks.many().multicast().onBackpressureBuffer();
    }
}
