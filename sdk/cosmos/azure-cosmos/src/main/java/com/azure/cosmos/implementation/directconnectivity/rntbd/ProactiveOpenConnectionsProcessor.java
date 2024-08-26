// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.ShouldRetryResult;
import com.azure.cosmos.implementation.directconnectivity.AddressSelector;
import com.azure.cosmos.implementation.directconnectivity.TransportException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

public final class ProactiveOpenConnectionsProcessor implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ProactiveOpenConnectionsProcessor.class);
    private Sinks.Many<OpenConnectionTask> openConnectionsTaskSink;
    private final ConcurrentHashMap<String, List<OpenConnectionTask>> endpointsUnderMonitorMap;
    private final ReentrantReadWriteLock.WriteLock endpointsUnderMonitorMapWriteLock;
    private final ReentrantReadWriteLock.ReadLock endpointsUnderMonitorMapReadLock;
    private final Set<String> containersUnderOpenConnectionAndInitCaches;
    private final Map<String, Set<String>> collectionRidsAndUrisUnderOpenConnectionAndInitCaches;
    private final Set<String> addressUrisUnderOpenConnectionsAndInitCaches;
    private final Object containersUnderOpenConnectionAndInitCachesLock;
    private final AtomicReference<ConnectionOpenFlowAggressivenessHint> aggressivenessHint;
    private final AtomicReference<Boolean> isClosed = new AtomicReference<>(false);
    private static final Map<ConnectionOpenFlowAggressivenessHint, ConcurrencyConfiguration> concurrencySettings = new HashMap<>();
    private final IOpenConnectionsHandler openConnectionsHandler;
    private final RntbdEndpoint.Provider endpointProvider;
    private final AddressSelector addressSelector;
    private Disposable openConnectionBackgroundTask;
    private final Sinks.EmitFailureHandler serializedEmitFailureHandler;
    private static final int OPEN_CONNECTION_SINK_BUFFER_SIZE = 100_000;

    public ProactiveOpenConnectionsProcessor(final RntbdEndpoint.Provider endpointProvider, final AddressSelector addressSelector) {
        this.aggressivenessHint = new AtomicReference<>(ConnectionOpenFlowAggressivenessHint.DEFENSIVE);
        this.endpointsUnderMonitorMap = new ConcurrentHashMap<>();
        ReentrantReadWriteLock throughputReadWriteLock = new ReentrantReadWriteLock();
        this.endpointsUnderMonitorMapWriteLock = throughputReadWriteLock.writeLock();
        this.endpointsUnderMonitorMapReadLock = throughputReadWriteLock.readLock();

        this.openConnectionsHandler = new RntbdOpenConnectionsHandler(endpointProvider);
        this.endpointProvider = endpointProvider;
        this.addressSelector = addressSelector;
        this.serializedEmitFailureHandler = new SerializedEmitFailureHandler();
        this.containersUnderOpenConnectionAndInitCaches = ConcurrentHashMap.newKeySet();
        this.collectionRidsAndUrisUnderOpenConnectionAndInitCaches = new ConcurrentHashMap<>();
        this.addressUrisUnderOpenConnectionsAndInitCaches = ConcurrentHashMap.newKeySet();
        this.containersUnderOpenConnectionAndInitCachesLock = new Object();

        concurrencySettings.put(
            ConnectionOpenFlowAggressivenessHint.AGGRESSIVE,
            new ConcurrencyConfiguration(Configs.getAggressiveWarmupConcurrency(), Configs.getAggressiveWarmupConcurrency()));
        concurrencySettings.put(
            ConnectionOpenFlowAggressivenessHint.DEFENSIVE,
            new ConcurrencyConfiguration(Configs.getOpenConnectionsConcurrency(), Configs.getOpenConnectionsConcurrency()));
    }

    public void init() {
        this.openConnectionBackgroundTask = this.getBackgroundOpenConnectionsPublisher();
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

        // endpointProvider is closed when RntbdTransportClient
        // is closed
        // the if check below prevents connectionStateListener
        // to open connection / channels when channels are being closed
        // and RntbdTransportClient / RntbdEndpointProvider are also closed
        // this prevents netty executor classes from entering into IllegalStateException
        if (this.endpointProvider.isClosed() || this.isClosed.get()) {
            openConnectionTask.completeExceptionally(new ClosedClientTransportException(lenientFormat("%s is closed", this), null));
            return;
        }

        this.endpointsUnderMonitorMapReadLock.lock();
        try {
            this.endpointsUnderMonitorMap.compute(addressUriAsString, (key, taskList) -> {
                if (taskList == null) {
                    taskList = new ArrayList<>();
                }

                taskList.add(openConnectionTask);

                // Only submit task to the sink if this is the first openConnectionTask to the endpoint
                if (taskList.size() == 1) {
                    this.submitOpenConnectionWithinLoopInternal(openConnectionTask);
                }

                return taskList;
            });
        } finally {
            this.endpointsUnderMonitorMapReadLock.unlock();
        }

        // it is necessary to invoke getOrCreateEndpoint
        // multiple times to ensure a global max of min connections required
        // is taken when an endpoint is used by different containers
        // and each container has a different min connection required setting for the endpoint
        getOrCreateEndpoint(openConnectionTask);
    }

    // There are two major kind tasks will be submitted
    // 1. Tasks from up caller -> in this case, before we enqueue a task we would want to check whether the endpoint has already in the map
    // 2. Tasks from existing flow -> for each endpoint, each time we will only 1 connection, if the connection count < the mini connection requirements,then re-queue.
    // for this case, then there is no need to validate whether the endpoint exists
    //
    // Using synchronized here to reduce the Sinks.EmitResult.FAIL_NON_SERIALIZED errors.
    private synchronized void submitOpenConnectionWithinLoopInternal(OpenConnectionTask openConnectionTask) {
        openConnectionsTaskSink.emitNext(openConnectionTask, serializedEmitFailureHandler);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            logger.info("Shutting down ProactiveOpenConnectionsProcessor...");
            completeSink(openConnectionsTaskSink);

            // Fail all pending tasks
            this.endpointsUnderMonitorMap.forEach((addresses, taskList) -> {
                for (OpenConnectionTask openConnectionTask : taskList) {
                    openConnectionTask.completeExceptionally(new ClosedClientTransportException(lenientFormat("%s is closed", this), null));
                }
            });
        }
    }

    public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> containerIdentities) {

        synchronized (this.containersUnderOpenConnectionAndInitCachesLock) {
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
        synchronized (this.containersUnderOpenConnectionAndInitCachesLock) {
            shouldReInstantiatePublisher = this.containersUnderOpenConnectionAndInitCaches.size() == 0;
            for (CosmosContainerIdentity containerIdentity : cosmosContainerIdentities) {
                this.containersUnderOpenConnectionAndInitCaches.add(
                    ImplementationBridgeHelpers
                        .CosmosContainerIdentityHelper
                        .getCosmosContainerIdentityAccessor()
                        .getContainerLink(containerIdentity)
                );
            }

            if (shouldReInstantiatePublisher) {
                this.aggressivenessHint.set(ConnectionOpenFlowAggressivenessHint.AGGRESSIVE);
                this.reInstantiateOpenConnectionsPublisherAndSubscribe(false);
            }
        }
    }

    public void recordCollectionRidsAndUrisUnderOpenConnectionsAndInitCaches(String collectionRid, List<String> addressUrisAsString) {
        this.collectionRidsAndUrisUnderOpenConnectionAndInitCaches.compute(collectionRid, (ignore, urisAsString) -> {

            if (urisAsString == null) {
                urisAsString = new HashSet<>(addressUrisAsString);
            } else {
                urisAsString.addAll(addressUrisAsString);
            }

            this.addressUrisUnderOpenConnectionsAndInitCaches.addAll(addressUrisAsString);

            return urisAsString;
        });
    }

    public boolean isAddressUriUnderOpenConnectionsFlow(String addressUriAsString) {
        return this.addressUrisUnderOpenConnectionsAndInitCaches.contains(addressUriAsString);
    }

    public boolean isCollectionRidUnderOpenConnectionsFlow(String collectionRid) {
        return this.collectionRidsAndUrisUnderOpenConnectionAndInitCaches.containsKey(collectionRid);
    }

    private Disposable getBackgroundOpenConnectionsPublisher() {

        ConcurrencyConfiguration concurrencyConfiguration = concurrencySettings.get(aggressivenessHint.get());

        Map<String, List<OpenConnectionTask>> mapSnapshot = new ConcurrentHashMap<>();

        this.endpointsUnderMonitorMapWriteLock.lock();
        try {
            this.instantiateOpenConnectionsPublisher();
            mapSnapshot.putAll(this.endpointsUnderMonitorMap);
        } finally {
            this.endpointsUnderMonitorMapWriteLock.unlock();
        }

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
                .parallel(concurrencyConfiguration.openConnectionTaskEmissionConcurrency)
                .runOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                .flatMap(openConnectionTask -> {

                    RntbdEndpoint endpoint = getOrCreateEndpoint(openConnectionTask);

                    return Flux.zip(Mono.just(openConnectionTask), openConnectionsHandler.openConnections(
                                    openConnectionTask.getCollectionRid(),
                                    Arrays.asList(endpoint),
                                    openConnectionTask.getMinConnectionsRequiredForEndpoint()))
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
                        this.removeEndpointFromMonitor(openConnectionTask.getAddressUri().toString(), openConnectionResponse);
                        return Mono.just(openConnectionResponse);
                    }

                    return openConnectionTask
                            .getRetryPolicy()
                            .shouldRetry((Exception) openConnectionResponse.getException())
                            .flatMap(shouldRetryResult -> {
                                if (shouldRetryResult.shouldRetry) {
                                    return enqueueOpenConnectionTaskForRetry(openConnectionTask, shouldRetryResult)
                                            .onErrorResume(throwable -> {
                                                logger.warn("An error occurred in proactiveOpenConnectionsProcessor", throwable);
                                                return Mono.empty();
                                            });
                                }

                                this.removeEndpointFromMonitor(openConnectionTask.getAddressUri().toString(), openConnectionResponse);
                                return Mono.just(openConnectionResponse);
                            });
                }, true)
                .subscribe();
    }

    private RntbdEndpoint getOrCreateEndpoint(OpenConnectionTask openConnectionTask) {

        RntbdEndpoint endpoint = this.endpointProvider.createIfAbsent(
                openConnectionTask.getServiceEndpoint(),
                openConnectionTask.getAddressUri(),
                this,
                openConnectionTask.getMinConnectionsRequiredForEndpoint(),
                this.addressSelector);

        endpoint.setMinChannelsRequired(Math.max(openConnectionTask.getMinConnectionsRequiredForEndpoint(),
                endpoint.getMinChannelsRequired()));

        return endpoint;
    }

    private void removeEndpointFromMonitor(String addressUriString, OpenConnectionResponse openConnectionResponse) {
        List<OpenConnectionTask> openConnectionTasks = this.endpointsUnderMonitorMap.remove(addressUriString);

        logger.debug("Open connections completed for endpoint : {}, no. of connections opened : {}", addressUriString, openConnectionResponse.getOpenConnectionCountToEndpoint());

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

        // If this is not the first time we are initiating the publisher
        // then we are going to dispose the previous one
        if (this.openConnectionBackgroundTask != null) {
            this.openConnectionBackgroundTask.dispose();
        }
        this.openConnectionBackgroundTask = this.getBackgroundOpenConnectionsPublisher();
    }

    private Mono<OpenConnectionResponse> enqueueOpenConnectionTaskForRetry(
            OpenConnectionTask openConnectionTask,
            ShouldRetryResult retryResult) {
        if (retryResult.backOffTime == Duration.ZERO || retryResult.backOffTime == null) {
            this.submitOpenConnectionWithinLoopInternal(openConnectionTask);
            return Mono.empty();
        } else {
            return Mono
                    .delay(retryResult.backOffTime)
                    .flatMap(ignore -> {
                        this.submitOpenConnectionWithinLoopInternal(openConnectionTask);
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
        final int openConnectionTaskEmissionConcurrency;
        final int openConnectionExecutionConcurrency;

        public ConcurrencyConfiguration(int openConnectionTaskEmissionConcurrency, int openConnectionExecutionConcurrency) {
            this.openConnectionTaskEmissionConcurrency = openConnectionTaskEmissionConcurrency;
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
