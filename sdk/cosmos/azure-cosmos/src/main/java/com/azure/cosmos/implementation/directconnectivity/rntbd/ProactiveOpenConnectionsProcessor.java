// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.Sinks;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class ProactiveOpenConnectionsProcessor implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ProactiveOpenConnectionsProcessor.class);
    private static final int MaxConnectionsToOpenAcrossAllEndpoints = 50_000;
    private Sinks.Many<OpenConnectionOperation> openConnectionsTaskSink;
    private Sinks.Many<OpenConnectionOperation> openConnectionsTaskSinkBackUp;
    private final ConcurrentHashMap<String, Integer> endpointToMinConnections;
    private final AtomicInteger totalEstablishedConnections;
    private final AtomicReference<AsyncDocumentClient.OpenConnectionAggressivenessHint> aggressivenessHint;
    private static final Map<AsyncDocumentClient.OpenConnectionAggressivenessHint, ConcurrencyConfiguration> concurrencySettings = new HashMap<>();

    static {
        concurrencySettings.put(AsyncDocumentClient.OpenConnectionAggressivenessHint.DEFENSIVE, new ConcurrencyConfiguration(Configs.getCPUCnt(), 1));
        concurrencySettings.put(AsyncDocumentClient.OpenConnectionAggressivenessHint.AGGRESSIVE, new ConcurrencyConfiguration(Configs.getCPUCnt(), Configs.getCPUCnt()));
    }

    public ProactiveOpenConnectionsProcessor() {
        this.openConnectionsTaskSink = Sinks.many().multicast().onBackpressureBuffer();
        this.openConnectionsTaskSinkBackUp = Sinks.many().multicast().onBackpressureBuffer();
        this.aggressivenessHint = new AtomicReference<>(AsyncDocumentClient.OpenConnectionAggressivenessHint.AGGRESSIVE);
        this.totalEstablishedConnections = new AtomicInteger(0);
        this.endpointToMinConnections = new ConcurrentHashMap<>();
    }

    public synchronized void submitOpenConnectionTask(OpenConnectionOperation openConnectionOperation) {

        String addressUriAsString = openConnectionOperation.getAddressUri().getURIAsString();
        boolean isEndpointTaskInSink = endpointToMinConnections.putIfAbsent(addressUriAsString, openConnectionOperation.getMinConnectionsRequiredForEndpoint()) != null;

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
        completeSink(openConnectionsTaskSink);
        completeSink(openConnectionsTaskSinkBackUp);
    }

    public synchronized ParallelFlux<OpenConnectionResponse> getOpenConnectionsPublisher() {

        ConcurrencyConfiguration concurrencyConfiguration = concurrencySettings.get(aggressivenessHint.get());

        return Flux.from(openConnectionsTaskSink.asFlux())
                .publishOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                .parallel(concurrencyConfiguration.openConnectionOperationEmissionConcurrency)
                .runOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                .flatMap(openConnectionOperation -> {
                    if (totalEstablishedConnections.get() < MaxConnectionsToOpenAcrossAllEndpoints) {

                        endpointToMinConnections.remove(openConnectionOperation.getAddressUri().getURIAsString());

                        IOpenConnectionsHandler openConnectionsHandler = openConnectionOperation.getOpenConnectionsHandler();
                        Uri addressUri = openConnectionOperation.getAddressUri();
                        URI serviceEndpoint = openConnectionOperation.getServiceEndpoint();
                        int minConnectionsForEndpoint = openConnectionOperation.getMinConnectionsRequiredForEndpoint();
                        AsyncDocumentClient.OpenConnectionAggressivenessHint hint = aggressivenessHint.get();
                        String collectionRid = openConnectionOperation.getCollectionRid();

                        return Flux.zip(Mono.just(openConnectionOperation), openConnectionsHandler.openConnections(
                                collectionRid,
                                serviceEndpoint,
                                Arrays.asList(addressUri),
                                minConnectionsForEndpoint
                        ));
                    }
                    return Flux.empty();
                }, false, concurrencyConfiguration.openConnectionExecutionConcurrency)
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
        logger.debug("In open connections task sink and concurrency reduction flow");
        this.toggleOpenConnectionsAggressiveness();
        this.instantiateOpenConnectionsPublisher();
        this.getOpenConnectionsPublisher().runOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC).subscribe();
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
        logger.debug("Reinstantiate open connections task sink");
        openConnectionsTaskSink = openConnectionsTaskSinkBackUp;
        openConnectionsTaskSinkBackUp = Sinks.many().multicast().onBackpressureBuffer();
    }

    private void toggleOpenConnectionsAggressiveness() {
        if (aggressivenessHint.get() == AsyncDocumentClient.OpenConnectionAggressivenessHint.AGGRESSIVE) {
            aggressivenessHint.set(AsyncDocumentClient.OpenConnectionAggressivenessHint.DEFENSIVE);
        } else {
            aggressivenessHint.set(AsyncDocumentClient.OpenConnectionAggressivenessHint.AGGRESSIVE);
        }
    }

    private void completeSink(Sinks.Many<OpenConnectionOperation> sink) {
        Sinks.EmitResult completeEmitResult = sink.tryEmitComplete();

        if (completeEmitResult == Sinks.EmitResult.OK) {
            logger.debug("Sink completed.");
        } else if (completeEmitResult == Sinks.EmitResult.FAIL_CANCELLED ||
                completeEmitResult == Sinks.EmitResult.FAIL_TERMINATED) {
            logger.debug("Sink already completed, EmitResult: {}", completeEmitResult);
        } else {
            logger.warn("Sink completion failed, EmitResult: {}", completeEmitResult);
        }
    }

    public void decrementTotalConnectionCount() {
        totalEstablishedConnections.decrementAndGet();
    }

    private static class ConcurrencyConfiguration {
        int openConnectionOperationEmissionConcurrency;
        int openConnectionExecutionConcurrency;

        public ConcurrencyConfiguration(int openConnectionOperationEmissionConcurrency, int openConnectionExecutionConcurrency) {
            this.openConnectionOperationEmissionConcurrency = openConnectionOperationEmissionConcurrency;
            this.openConnectionExecutionConcurrency = openConnectionExecutionConcurrency;
        }
    }

}
