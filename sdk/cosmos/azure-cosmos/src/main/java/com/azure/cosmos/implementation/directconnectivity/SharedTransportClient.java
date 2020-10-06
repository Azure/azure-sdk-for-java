// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
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
// We suppress the "try" warning here because the close() method's signature
// allows it to throw InterruptedException which is strongly advised against
// by AutoCloseable (see: http://docs.oracle.com/javase/7/docs/api/java/lang/AutoCloseable.html#close()).
// close() will never throw an InterruptedException but the exception remains in the
// signature for backwards compatibility purposes.
@SuppressWarnings("try")
public class SharedTransportClient extends TransportClient {
    private static final Logger logger = LoggerFactory.getLogger(SharedTransportClient.class);
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static SharedTransportClient sharedTransportClient;
    private final RntbdTransportClient.Options rntbdOptions;

    public static TransportClient getOrCreateInstance(
        Protocol protocol,
        Configs configs,
        ConnectionPolicy connectionPolicy,
        UserAgentContainer userAgent,
        DiagnosticsClientContext.DiagnosticsClientConfig diagnosticsClientConfig,
        IAddressResolver addressResolver) {

        synchronized (SharedTransportClient.class) {
            if (sharedTransportClient == null) {
                assert counter.get() == 0;
                logger.info("creating a new shared RntbdTransportClient");
                sharedTransportClient = new SharedTransportClient(protocol, configs, connectionPolicy, userAgent, addressResolver);
            } else {
                logger.info("Reusing an instance of RntbdTransportClient");
            }

            counter.incrementAndGet();

            diagnosticsClientConfig.withRntbdOptions(sharedTransportClient.rntbdOptions);
            return sharedTransportClient;
        }
    }

    private final TransportClient transportClient;

    private SharedTransportClient(
        Protocol protocol,
        Configs configs,
        ConnectionPolicy connectionPolicy,
        UserAgentContainer userAgent,
        IAddressResolver addressResolver) {
        if (protocol == Protocol.TCP) {
            this.rntbdOptions =
                new RntbdTransportClient.Options.Builder(connectionPolicy).userAgent(userAgent).build();
            this.transportClient = new RntbdTransportClient(rntbdOptions, configs.getSslContext(), addressResolver);

        } else if (protocol == Protocol.HTTPS){
            this.rntbdOptions = null;
            this.transportClient = new HttpTransportClient(configs, connectionPolicy, userAgent);
        } else {
            throw new IllegalArgumentException(String.format("protocol: %s", protocol));
        }
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
        synchronized (SharedTransportClient.class) {
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
