// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.IRetryPolicy;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RetryContext;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.directconnectivity.TimeoutHelper;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class ProactiveOpenConnectionsProcessor implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ProactiveOpenConnectionsProcessor.class);
    private Sinks.Many<OpenConnectionTask> openConnectionsTaskSink;
    private Sinks.Many<OpenConnectionTask> openConnectionsTaskSinkBackUp;
    private final ConcurrentHashMap<String, Integer> endpointToMinConnections;
    private final AtomicReference<AsyncDocumentClient.OpenConnectionAggressivenessHint> aggressivenessHint;
    private static final Map<AsyncDocumentClient.OpenConnectionAggressivenessHint, ConcurrencyConfiguration> concurrencySettings = new HashMap<>();
    private final IOpenConnectionsHandler openConnectionsHandler;

    static {
        concurrencySettings.put(AsyncDocumentClient.OpenConnectionAggressivenessHint.DEFENSIVE, new ConcurrencyConfiguration(Configs.getOpenConnectionsDefensiveConcurrency(), Configs.getOpenConnectionsDefensiveConcurrency()));
        concurrencySettings.put(AsyncDocumentClient.OpenConnectionAggressivenessHint.AGGRESSIVE, new ConcurrencyConfiguration(Configs.getCPUCnt(), Configs.getCPUCnt()));
    }

    public ProactiveOpenConnectionsProcessor(final RntbdEndpoint.Provider endpointProvider) {
        this.openConnectionsTaskSink = Sinks.many().multicast().onBackpressureBuffer();
        this.openConnectionsTaskSinkBackUp = Sinks.many().multicast().onBackpressureBuffer();
        this.aggressivenessHint = new AtomicReference<>(AsyncDocumentClient.OpenConnectionAggressivenessHint.AGGRESSIVE);
        this.endpointToMinConnections = new ConcurrentHashMap<>();
        this.openConnectionsHandler = new RntbdOpenConnectionsHandler(endpointProvider);
    }

    public synchronized void submitOpenConnectionTask(
            String collectionRid, URI serviceEndpoint, Uri addressUri, int minConnectionsRequiredForEndpoint) {
        OpenConnectionTask openConnectionTask = new OpenConnectionTask(collectionRid, serviceEndpoint, addressUri, minConnectionsRequiredForEndpoint);
        this.submitOpenConnectionTask(openConnectionTask);
    }

    private synchronized void submitOpenConnectionTask(OpenConnectionTask openConnectionTask) {

        String addressUriAsString = openConnectionTask.addressUri.getURIAsString();
        boolean isEndpointTaskInSink = endpointToMinConnections.putIfAbsent(addressUriAsString,
                openConnectionTask.minConnectionsRequiredForEndpoint) != null;

        endpointToMinConnections.computeIfPresent(addressUriAsString, (s, min) -> {
            openConnectionTask.minConnectionsRequiredForEndpoint = Math.max(openConnectionTask.minConnectionsRequiredForEndpoint, min);
            return openConnectionTask.minConnectionsRequiredForEndpoint;
        });

        if (!isEndpointTaskInSink) {
            openConnectionsTaskSink.tryEmitNext(openConnectionTask);
            openConnectionsTaskSinkBackUp.tryEmitNext(openConnectionTask);
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
                .flatMap(openConnectionTask -> {
                    endpointToMinConnections.remove(openConnectionTask.addressUri.getURIAsString());

                    Uri addressUri = openConnectionTask.addressUri;
                    URI serviceEndpoint = openConnectionTask.serviceEndpoint;
                    int minConnectionsForEndpoint = openConnectionTask.minConnectionsRequiredForEndpoint;
                    String collectionRid = openConnectionTask.collectionRid;

                    return Flux.zip(Mono.just(openConnectionTask), openConnectionsHandler.openConnections(
                            collectionRid,
                            serviceEndpoint,
                            Arrays.asList(addressUri),
                            this,
                            minConnectionsForEndpoint));
                }, false, concurrencyConfiguration.openConnectionExecutionConcurrency)
                .flatMap(openConnectionTaskToResponse -> {
                    OpenConnectionTask openConnectionTask = openConnectionTaskToResponse.getT1();
                    OpenConnectionResponse openConnectionResponse = openConnectionTaskToResponse.getT2();

                    if (openConnectionResponse.isConnected() && openConnectionResponse.isOpenConnectionAttempted()) {
                        this.submitOpenConnectionTask(openConnectionTask);
                        return Mono.just(openConnectionResponse);
                    }

                    // open connections handler has created the min. required connections for the endpoint
                    if (openConnectionResponse.isConnected() && !openConnectionResponse.isOpenConnectionAttempted()) {
                        return Mono.just(openConnectionResponse);
                    }

                    return openConnectionTask
                            .retryPolicy
                            .shouldRetry((Exception) openConnectionResponse.getException())
                            .flatMap(shouldRetryResult -> {
                                if (shouldRetryResult.shouldRetry) {
                                    return enqueueOpenConnectionOpsForRetry(openConnectionTask, shouldRetryResult);
                                }
                                return Mono.just(openConnectionResponse);
                            });
                });
    }

    public void reinstantiateOpenConnectionsPublisherAndSubscribe() {
        logger.debug("In open connections task sink and concurrency reduction flow");
        this.forceDefensiveOpenConnections();
        this.instantiateOpenConnectionsPublisher();
        this.getOpenConnectionsPublisher().runOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC).subscribe();
    }

    private Mono<OpenConnectionResponse> enqueueOpenConnectionOpsForRetry(
            OpenConnectionTask op,
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

    private void forceDefensiveOpenConnections() {
        if (aggressivenessHint.get() == AsyncDocumentClient.OpenConnectionAggressivenessHint.AGGRESSIVE) {
            aggressivenessHint.set(AsyncDocumentClient.OpenConnectionAggressivenessHint.DEFENSIVE);
        }
    }

    private void completeSink(Sinks.Many<OpenConnectionTask> sink) {
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

    private static class ConcurrencyConfiguration {
        int openConnectionOperationEmissionConcurrency;
        int openConnectionExecutionConcurrency;

        public ConcurrencyConfiguration(int openConnectionOperationEmissionConcurrency, int openConnectionExecutionConcurrency) {
            this.openConnectionOperationEmissionConcurrency = openConnectionOperationEmissionConcurrency;
            this.openConnectionExecutionConcurrency = openConnectionExecutionConcurrency;
        }
    }

    private static final class OpenConnectionTask {
        private final String collectionRid;
        private final URI serviceEndpoint;
        private final Uri addressUri;
        private int minConnectionsRequiredForEndpoint;
        private final IRetryPolicy retryPolicy;

        OpenConnectionTask(
                String collectionRid,
                URI serviceEndpoint,
                Uri addressUri,
                int minConnectionsRequiredForEndpoint) {
            this.collectionRid = collectionRid;
            this.serviceEndpoint = serviceEndpoint;
            this.addressUri = addressUri;
            this.minConnectionsRequiredForEndpoint = minConnectionsRequiredForEndpoint;
            this.retryPolicy = new ProactiveOpenConnectionsRetryPolicy();
        }

        private static class ProactiveOpenConnectionsRetryPolicy implements IRetryPolicy {

            private static final Logger logger = LoggerFactory.getLogger(ProactiveOpenConnectionsRetryPolicy.class);
            private static final int MaxRetryAttempts = 2;
            private static final Duration InitialOpenConnectionReattemptBackOffInMs = Duration.ofMillis(1_000);
            private static final Duration MaxFailedOpenConnectionRetryWindowInMs = Duration.ofMillis(10_000);
            private static final int BackoffMultiplier = 4;
            private Duration currentBackoff;
            private final TimeoutHelper waitTimeTimeoutHelper;
            private final AtomicInteger retryCount;

            private ProactiveOpenConnectionsRetryPolicy() {
                this.waitTimeTimeoutHelper = new TimeoutHelper(MaxFailedOpenConnectionRetryWindowInMs);
                this.retryCount = new AtomicInteger(0);
                this.currentBackoff = InitialOpenConnectionReattemptBackOffInMs;
            }

            @Override
            public Mono<ShouldRetryResult> shouldRetry(Exception e) {

                if (this.retryCount.get() >= MaxRetryAttempts || this.waitTimeTimeoutHelper.isElapsed() || e == null) {
                    return Mono.just(ShouldRetryResult.noRetry());
                }

                logger.warn("In retry policy: ProactiveOpenConnectionsRetryPolicy, retry attempt: {}, exception :{}", this.retryCount.get(), e.getMessage());

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
}
