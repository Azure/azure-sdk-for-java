// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.SessionContainer;
import com.azure.data.cosmos.internal.UserAgentContainer;

// TODO: DANOBLE: no support for ICommunicationEventSource ask Ji
//  Links:
//  https://msdata.visualstudio.com/CosmosDB/SDK/_workitems/edit/262496

public class StoreClientFactory implements AutoCloseable {
    private final Configs configs;
    private final int maxConcurrentConnectionOpenRequests;
    private final int requestTimeoutInSeconds;
    private final Protocol protocol;
    private final TransportClient transportClient;
    private volatile boolean isClosed;

    public StoreClientFactory(
        Configs configs,
        int requestTimeoutInSeconds,
        int maxConcurrentConnectionOpenRequests,
        UserAgentContainer userAgent) {

        this.configs = configs;
        this.protocol = configs.getProtocol();
        this.requestTimeoutInSeconds = requestTimeoutInSeconds;
        this.maxConcurrentConnectionOpenRequests = maxConcurrentConnectionOpenRequests;

        if (protocol == Protocol.HTTPS) {
            this.transportClient = new HttpTransportClient(configs, requestTimeoutInSeconds, userAgent);
        } else if (protocol == Protocol.TCP){
            this.transportClient = new RntbdTransportClient(configs, requestTimeoutInSeconds, userAgent);
        } else {
            throw new IllegalArgumentException(String.format("protocol: %s", this.protocol));
        }
    }

    public void close() throws Exception {
        this.transportClient.close();
        this.isClosed = true;
    }

    // TODO wew don't have support for the following yet
    // TODO enableReadRequestsFallback ask Ji
    // TODO useFallbackClient ask Ji
    public StoreClient createStoreClient(
        IAddressResolver addressResolver,
        SessionContainer sessionContainer,
        GatewayServiceConfigurationReader serviceConfigurationReader,
        IAuthorizationTokenProvider authorizationTokenProvider,
        boolean useMultipleWriteLocations) {
        this.throwIfClosed();

        return new StoreClient(configs,
            addressResolver,
            sessionContainer,
            serviceConfigurationReader,
            authorizationTokenProvider,
            this.transportClient,
            useMultipleWriteLocations);
    }

    private void throwIfClosed() {
        if (isClosed) {
            throw new IllegalStateException("storeClient already closed!");
        }
    }
}
