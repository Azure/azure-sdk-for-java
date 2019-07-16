/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
