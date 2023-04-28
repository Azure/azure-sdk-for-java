// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.CosmosContainerIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public final class ProactiveOpenConnectionsProcessor implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ProactiveOpenConnectionsProcessor.class);
    private Sinks.Many<OpenConnectionTask> openConnectionsTaskSink;
    private Sinks.Many<OpenConnectionTask> openConnectionsTaskSinkBackUp;
    private final ConcurrentHashMap<String, List<OpenConnectionTask>> endpointsUnderMonitorMap;
    private final Set<String> containersUnderOpenConnectionAndInitCaches;
    private final AtomicReference<ConnectionOpenFlowAggressivenessHint> aggressivenessHint;
    private final AtomicReference<Boolean> isClosed = new AtomicReference<>(false);
    private static final Map<ConnectionOpenFlowAggressivenessHint, ConcurrencyConfiguration> concurrencySettings = new HashMap<>();
    private final IOpenConnectionsHandler openConnectionsHandler;
    private final RntbdEndpoint.Provider endpointProvider;
    private Disposable openConnectionBackgroundTask;
    private final Sinks.EmitFailureHandler serializedEmitFailureHandler;
    private static final int OPEN_CONNECTION_SINK_BUFFER_SIZE = 100_000;

    public ProactiveOpenConnectionsProcessor(final RntbdEndpoint.Provider endpointProvider) {
        this.openConnectionsTaskSink = Sinks.many().multicast().onBackpressureBuffer(OPEN_CONNECTION_SINK_BUFFER_SIZE);
        this.openConnectionsTaskSinkBackUp = Sinks.many().multicast().onBackpressureBuffer(OPEN_CONNECTION_SINK_BUFFER_SIZE);
        this.aggressivenessHint = new AtomicReference<>(ConnectionOpenFlowAggressivenessHint.DEFENSIVE);
        this.endpointsUnderMonitorMap = new ConcurrentHashMap<>();

        this.openConnectionsHandler = new RntbdOpenConnectionsHandler(endpointProvider);
        this.endpointProvider = endpointProvider;
        this.serializedEmitFailureHandler = new SerializedEmitFailureHandler();
        this.containersUnderOpenConnectionAndInitCaches = ConcurrentHashMap.newKeySet();

        concurrencySettings.put(
            ConnectionOpenFlowAggressivenessHint.AGGRESSIVE,
            new ConcurrencyConfiguration(Configs.getAggressiveWarmupConcurrency(), Configs.getAggressiveWarmupConcurrency()));
        concurrencySettings.put(
            ConnectionOpenFlowAggressivenessHint.DEFENSIVE,
            new ConcurrencyConfiguration(Configs.getDefensiveWarmupConcurrency(), Configs.getDefensiveWarmupConcurrency()));
    }

    public OpenConnectionTask submitOpenConnectionTaskOutsideLoop(
            String collectionRid,
            URI serviceEndpoint,
            Uri addressUri,
            int minConnectionsRequiredForEndpoint) {

        OpenConnectionTask openConnectionTask = new OpenConnectionTask(collectionRid, serviceEndpoint, addressUri, minConnectionsRequiredForEndpoint);
        this.submitOpenConnectionTaskOutsideLoopInternal(openConnectionTask);

        return openConnectionTask;
    }

    private void submitOpenConnectionTaskOutsideLoopInternal(OpenConnectionTask openConnectionTask) {
        String addressUriAsString = openConnectionTask.getAddressUri().getURIAsString();

        this.endpointsUnderMonitorMap.compute(addressUriAsString, (key, taskList) -> {
            if (taskList == null) {
                taskList = new ArrayList<>();
            }

            taskList.add(openConnectionTask);

            if (taskList.size() == 1) {
                this.submitOpenConnectionWithinLoopInternal(openConnectionTask);
            }

            return taskList;
        });

        RntbdEndpoint endpoint = this.endpointProvider.createIfAbsent(
                openConnectionTask.getServiceEndpoint(),
                openConnectionTask.getAddressUri().getURI(),
                this,
                openConnectionTask.getMinConnectionsRequiredForEndpoint());

        endpoint.setMinChannelsRequired(Math.max(openConnectionTask.getMinConnectionsRequiredForEndpoint(), endpoint.getMinChannelsRequired()));
    }

    // There are two major kind tasks will be submitted
    // 1. Tasks from up caller -> in this case, before we enqueue a task we would want to check whether the endpoint has already in the map
    // 2. Tasks from existing flow -> for each endpoint, each time we will only 1 connection, if the connection count < the mini connection requirements,then re-queue.
    // for this case, then there is no need to validate whether the endpoint exists
    private synchronized void submitOpenConnectionWithinLoopInternal(OpenConnectionTask openConnectionTask) {
        openConnectionsTaskSink.emitNext(openConnectionTask, serializedEmitFailureHandler);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            logger.info("Shutting down ProactiveOpenConnectionsProcessor...");
            completeSink(openConnectionsTaskSink);
            completeSink(openConnectionsTaskSinkBackUp);
        }
    }

    public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> containerIdentities) {

        synchronized (this) {
            for (CosmosContainerIdentity containerIdentity : containerIdentities) {
                this.containersUnderOpenConnectionAndInitCaches.remove(
                    ImplementationBridgeHelpers
                        .CosmosContainerIdentityHelper
                        .getCosmosContainerIdentityAccessor()
                        .getContainerLink(containerIdentity)
                );
            }

            // only switch to defensive mode if there is no container under openConnectionAndInitCachesFlow
            if (this.containersUnderOpenConnectionAndInitCaches.isEmpty()) {
                this.aggressivenessHint.set(ConnectionOpenFlowAggressivenessHint.DEFENSIVE);
                this.reInstantiateOpenConnectionsPublisherAndSubscribe(true);
            } else {
                logger.debug(
                    "Cannot switch to defensive mode as some of the containers are still under openConnectionAndInitCaches flow: [{}]",
                    this.containersUnderOpenConnectionAndInitCaches);
            }
        }
    }

    public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        boolean shouldReInstantiatePublisher;
        synchronized (this) {
            shouldReInstantiatePublisher = this.containersUnderOpenConnectionAndInitCaches.size() == 0;
            for (CosmosContainerIdentity containerIdentity : cosmosContainerIdentities) {
                this.containersUnderOpenConnectionAndInitCaches.add(
                    ImplementationBridgeHelpers
                        .CosmosContainerIdentityHelper
                        .getCosmosContainerIdentityAccessor()
                        .getContainerLink(containerIdentity)
                );
            }
        }

        if (shouldReInstantiatePublisher) {
            this.aggressivenessHint.set(ConnectionOpenFlowAggressivenessHint.AGGRESSIVE);
            this.reInstantiateOpenConnectionsPublisherAndSubscribe(false);
        }
    }

    public Disposable getBackgroundOpenConnectionsPublisher() {

        ConcurrencyConfiguration concurrencyConfiguration = concurrencySettings.get(aggressivenessHint.get());
        Map<String, List<OpenConnectionTask>> mapSnapshot = new ConcurrentHashMap<>();
        mapSnapshot.putAll(this.endpointsUnderMonitorMap);

        Flux<OpenConnectionTask> initialFlux  = Flux.fromIterable(
            mapSnapshot
                .keySet()
                .stream()
                .map(endpoint -> mapSnapshot.get(endpoint).get(0))
                .collect(Collectors.toList()));

        return Flux.from(openConnectionsTaskSink.asFlux())
                .mergeWith(initialFlux)
                .publishOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                .onErrorResume(throwable -> {
                    logger.warn("An error occurred with proactiveOpenConnectionsProcessor, re-initializing open connections sink", throwable);
                    this.reInstantiateOpenConnectionsPublisherAndSubscribe(false);
                    return Mono.empty();
                })
                .parallel(concurrencyConfiguration.openConnectionOperationEmissionConcurrency)
                .runOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                .flatMap(openConnectionTask -> {
                    Uri addressUri = openConnectionTask.getAddressUri();
                    URI serviceEndpoint = openConnectionTask.getServiceEndpoint();
                    int minConnectionsForEndpoint = openConnectionTask.getMinConnectionsRequiredForEndpoint();
                    String collectionRid = openConnectionTask.getCollectionRid();

                    return Flux.zip(Mono.just(openConnectionTask), openConnectionsHandler.openConnections(
                                    collectionRid,
                                    serviceEndpoint,
                                    Arrays.asList(addressUri),
                                    this,
                                    minConnectionsForEndpoint))
                            .onErrorResume(throwable -> {
                                logger.warn("An error occurred in proactiveOpenConnectionsProcessor", throwable);
                                return Flux.empty();
                            });
                }, true, concurrencyConfiguration.openConnectionExecutionConcurrency)
                .flatMap(openConnectionTaskToResponse -> {
                    OpenConnectionTask openConnectionTask = openConnectionTaskToResponse.getT1();
                    OpenConnectionResponse openConnectionResponse = openConnectionTaskToResponse.getT2();

                    if (openConnectionResponse.isConnected() && openConnectionResponse.isOpenConnectionAttempted()) {
                        this.submitOpenConnectionWithinLoopInternal(openConnectionTask);
                        return Mono.just(openConnectionResponse);
                    }

                    // open connections handler has created the min. required connections for the endpoint
                    if (openConnectionResponse.isConnected() && !openConnectionResponse.isOpenConnectionAttempted()) {
                        // for the specific service endpoint, it has met the mini pool size requirements, so remove from the map
                        // TODO: response should reflect all previous opened connection
                        this.removeEndpointFromMonitor(openConnectionTask.getAddressUri().toString(), openConnectionResponse);
                        return Mono.just(openConnectionResponse);
                    }

                    return openConnectionTask
                            .getRetryPolicy()
                            .shouldRetry((Exception) openConnectionResponse.getException())
                            .flatMap(shouldRetryResult -> {
                                if (shouldRetryResult.shouldRetry) {
                                    return enqueueOpenConnectionOpsForRetry(openConnectionTask, shouldRetryResult)
                                            .onErrorResume(throwable -> {
                                                logger.warn("An error occurred in proactiveOpenConnectionsProcessor", throwable);
                                                return Mono.empty();
                                            });
                                }

                                // TODO: response should reflect all previous opened connection
                                this.removeEndpointFromMonitor(openConnectionTask.getAddressUri().toString(), openConnectionResponse);
                                return Mono.just(openConnectionResponse);
                            });
                }, true)
                .subscribe();
    }

    private void removeEndpointFromMonitor(String addressUriString, OpenConnectionResponse openConnectionResponse) {
        List<OpenConnectionTask> openConnectionTasks = this.endpointsUnderMonitorMap.remove(addressUriString);

        if (openConnectionTasks != null && !openConnectionTasks.isEmpty()) {
            for (OpenConnectionTask connectionTask : openConnectionTasks) {
                connectionTask.complete(openConnectionResponse);
            }
        }
    }

    private synchronized void reInstantiateOpenConnectionsPublisherAndSubscribe(boolean shouldForceDefensiveOpenConnections) {
        if (shouldForceDefensiveOpenConnections) {
            logger.debug("Force defensive opening of connections");
            this.forceDefensiveOpenConnections();
        }
        logger.debug("Re-instantiate openConnectionsTaskSink");
        this.instantiateOpenConnectionsPublisher();

        // If this is not the first time we are initiating the publisher
        // then we are going to dispose the previous one
        if (this.openConnectionBackgroundTask != null) {
            this.openConnectionBackgroundTask.dispose();
        }
        this.openConnectionBackgroundTask = this.getBackgroundOpenConnectionsPublisher();
    }

    private Mono<OpenConnectionResponse> enqueueOpenConnectionOpsForRetry(
            OpenConnectionTask op,
            ShouldRetryResult retryResult) {
        if (retryResult.backOffTime == Duration.ZERO || retryResult.backOffTime == null) {
            this.submitOpenConnectionWithinLoopInternal(op);
            return Mono.empty();
        } else {
            return Mono
                    .delay(retryResult.backOffTime)
                    .flatMap(ignore -> {
                        this.submitOpenConnectionWithinLoopInternal(op);
                        return Mono.empty();
                    });
        }
    }

    // when the flux associated with openConnectionsTaskSink is cancelled
    // this method provides for a way to resume emitting pushed tasks to
    // the sink
    private void instantiateOpenConnectionsPublisher() {
        logger.debug("Re-instantiate open connections task sink");
        openConnectionsTaskSink = Sinks.many().multicast().onBackpressureBuffer(OPEN_CONNECTION_SINK_BUFFER_SIZE);
    }

    private void forceDefensiveOpenConnections() {
        if (aggressivenessHint.get() == ConnectionOpenFlowAggressivenessHint.AGGRESSIVE) {
            aggressivenessHint.set(ConnectionOpenFlowAggressivenessHint.DEFENSIVE);
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
        final int openConnectionOperationEmissionConcurrency;
        final int openConnectionExecutionConcurrency;

        public ConcurrencyConfiguration(int openConnectionOperationEmissionConcurrency, int openConnectionExecutionConcurrency) {
            this.openConnectionOperationEmissionConcurrency = openConnectionOperationEmissionConcurrency;
            this.openConnectionExecutionConcurrency = openConnectionExecutionConcurrency;
        }
    }

    private static class SerializedEmitFailureHandler implements Sinks.EmitFailureHandler {

        @Override
        public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
            if (emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED) || emitResult.equals(Sinks.EmitResult.FAIL_OVERFLOW)) {
                logger.debug("SerializedEmitFailureHandler.onEmitFailure - Signal:{}, Result: {}", signalType, emitResult);

                return true;
            }

            logger.debug("SerializedEmitFailureHandler.onEmitFailure - Signal:{}, Result: {}", signalType, emitResult);
            return false;
        }
    }

    private enum ConnectionOpenFlowAggressivenessHint {
        AGGRESSIVE, DEFENSIVE
    }
}
