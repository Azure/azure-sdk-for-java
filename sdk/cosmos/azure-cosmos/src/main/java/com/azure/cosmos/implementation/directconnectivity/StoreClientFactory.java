// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.SessionContainer;
import com.azure.cosmos.implementation.UserAgentContainer;

// TODO: DANOBLE: no support for ICommunicationEventSource ask Ji
//  Links:
//  https://msdata.visualstudio.com/CosmosDB/SDK/_workitems/edit/262496

// We suppress the "try" warning here because the close() method's signature
// allows it to throw InterruptedException which is strongly advised against
// by AutoCloseable (see: http://docs.oracle.com/javase/7/docs/api/java/lang/AutoCloseable.html#close()).
// close() will never throw an InterruptedException but the exception remains in the
// signature for backwards compatibility purposes.
@SuppressWarnings("try")
public class StoreClientFactory implements AutoCloseable {

    private final Configs configs;
    private final Protocol protocol;
    private final TransportClient transportClient;
    private volatile boolean isClosed;

    public StoreClientFactory(
        Configs configs,
        ConnectionPolicy connectionPolicy,
        UserAgentContainer userAgent,
        boolean enableTransportClientSharing) {

        this.configs = configs;
        this.protocol = configs.getProtocol();
        if (enableTransportClientSharing) {
            this.transportClient = SharedTransportClient.getOrCreateInstance(protocol, configs, connectionPolicy, userAgent);
        } else {
            if (protocol == Protocol.HTTPS) {
                this.transportClient = new HttpTransportClient(configs, connectionPolicy, userAgent);
            } else if (protocol == Protocol.TCP) {
                this.transportClient = new RntbdTransportClient(configs, connectionPolicy, userAgent);
            } else {
                throw new IllegalArgumentException(String.format("protocol: %s", this.protocol));
            }
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
