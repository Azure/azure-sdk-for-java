// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.directconnectivity.TransportException;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.models.CosmosContainerIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

public final class ProactiveOpenConnectionsProcessor implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(ProactiveOpenConnectionsProcessor.class);
    private final ConcurrentHashMap<String, List<OpenConnectionTask>> endpointsUnderMonitorMap;
    private final ReentrantReadWriteLock.ReadLock endpointsUnderMonitorMapReadLock;
    private final Set<String> containersUnderOpenConnectionAndInitCaches;
    private final Map<String, Set<String>> collectionRidsAndUrisUnderOpenConnectionAndInitCaches;
    private final Set<String> addressUrisUnderOpenConnectionsAndInitCaches;
    private final Object containersUnderOpenConnectionAndInitCachesLock;
    private final AtomicReference<ConnectionOpenFlowAggressivenessHint> aggressivenessHint;
    private final AtomicReference<Boolean> isClosed = new AtomicReference<>(false);
    private final RntbdEndpoint.Provider endpointProvider;
    private Disposable openConnectionBackgroundTask;

    public ProactiveOpenConnectionsProcessor(final RntbdEndpoint.Provider endpointProvider) {
        this.aggressivenessHint = new AtomicReference<>(ConnectionOpenFlowAggressivenessHint.DEFENSIVE);
        this.endpointsUnderMonitorMap = new ConcurrentHashMap<>();
        ReentrantReadWriteLock throughputReadWriteLock = new ReentrantReadWriteLock();
        this.endpointsUnderMonitorMapReadLock = throughputReadWriteLock.readLock();

        this.endpointProvider = endpointProvider;
        this.containersUnderOpenConnectionAndInitCaches = ConcurrentHashMap.newKeySet();
        this.collectionRidsAndUrisUnderOpenConnectionAndInitCaches = new ConcurrentHashMap<>();
        this.addressUrisUnderOpenConnectionsAndInitCaches = ConcurrentHashMap.newKeySet();
        this.containersUnderOpenConnectionAndInitCachesLock = new Object();
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
            openConnectionTask.completeExceptionally(new TransportException(lenientFormat("%s is closed", this), null));
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

    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            logger.info("Shutting down ProactiveOpenConnectionsProcessor...");
            // Fail all pending tasks
            this.endpointsUnderMonitorMap.forEach((addresses, taskList) -> {
                for (OpenConnectionTask openConnectionTask : taskList) {
                    openConnectionTask.completeExceptionally(new TransportException(lenientFormat("%s is closed", this), null));
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
        return false;
    }

    public boolean isCollectionRidUnderOpenConnectionsFlow(String collectionRid) {
        return false;
    }

    private Disposable getBackgroundOpenConnectionsPublisher() {

        return null;
    }

    private RntbdEndpoint getOrCreateEndpoint(OpenConnectionTask openConnectionTask) {

        RntbdEndpoint endpoint = this.endpointProvider.createIfAbsent(
                openConnectionTask.getServiceEndpoint(),
                openConnectionTask.getAddressUri(),
                this,
                openConnectionTask.getMinConnectionsRequiredForEndpoint());

        endpoint.setMinChannelsRequired(Math.max(openConnectionTask.getMinConnectionsRequiredForEndpoint(),
                endpoint.getMinChannelsRequired()));

        return endpoint;
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

    private void forceDefensiveOpenConnections() {
        if (aggressivenessHint.get() == ConnectionOpenFlowAggressivenessHint.AGGRESSIVE) {
            aggressivenessHint.set(ConnectionOpenFlowAggressivenessHint.DEFENSIVE);
        }
    }

    private enum ConnectionOpenFlowAggressivenessHint {
        AGGRESSIVE, DEFENSIVE
    }
}
