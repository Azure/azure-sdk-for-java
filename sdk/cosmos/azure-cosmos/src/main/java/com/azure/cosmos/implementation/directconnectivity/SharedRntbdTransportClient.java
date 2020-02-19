// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.LifeCycleUtils;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class uses a shared RntbdTransportClient for multiple Cosmos Clients.
 * The benefit is the underlying connections can be shared if possible across multiple Cosmos client instances.
 */
public class SharedRntbdTransportClient extends TransportClient {
    private static final Logger logger = LoggerFactory.getLogger(SharedRntbdTransportClient.class);
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static SharedRntbdTransportClient sharedTransportClient;

    public static TransportClient getOrCreateInstance(Configs configs, int requestTimeout, UserAgentContainer userAgent) {
        synchronized (TransportClient.class) {
            if (sharedTransportClient == null) {
                assert counter.get() == 0;
                logger.info("creating a new shared RntbdTransportClient");
                sharedTransportClient = new SharedRntbdTransportClient(configs, requestTimeout, userAgent);
            } else {
                logger.info("Reusing an instance of RntbdTransportClient");
            }

            counter.incrementAndGet();
            return sharedTransportClient;
        }
    }

    private final TransportClient transportClient;

    private SharedRntbdTransportClient(Configs configs, int requestTimeout, UserAgentContainer userAgent) {
        this.transportClient = new RntbdTransportClient(configs, requestTimeout, userAgent);
    }

    @Override
    protected Mono<StoreResponse> invokeStoreAsync(Uri physicalAddress, RxDocumentServiceRequest request) {
        return transportClient.invokeStoreAsync(physicalAddress, request);
    }

    public int getReferenceCounter() {
        return counter.get();
    }

    @Override
    public void close() throws Exception {
        synchronized (TransportClient.class) {
            final int numberOfActiveTransportClients = counter.decrementAndGet();
            logger.info("closing one reference to the shared RntbdTransportClient, the number of remaining references is {}", numberOfActiveTransportClients);
            if (numberOfActiveTransportClients == 0) {
                logger.info("All references to shared RntbdTransportClient are closed. Closing the underlying RntbdTransportClient");
                LifeCycleUtils.closeQuietly(sharedTransportClient.transportClient);
                sharedTransportClient = null;
            }
        }
    }
}
