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

package com.microsoft.azure.cosmosdb.rx.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.AddressInformation;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.AddressResolver;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.GatewayAddressCache;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.IAddressResolver;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.GatewayServiceConfigurationReader;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import com.microsoft.azure.cosmosdb.rx.internal.GlobalEndpointManager;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxCollectionCache;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxPartitionKeyRangeCache;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import rx.Single;

// TODO: this needs to move to direct-impl project
// requires refactoring due to circular dependency
// This is just a mock so simple write works
// TODO: Implement GlobalAddressResolver
// https://msdata.visualstudio.com/CosmosDB/_workitems/edit/308406
public class GlobalAddressResolver implements IAddressResolver {
    private final static int MaxBackupReadRegions = 3;

    private final GlobalEndpointManager endpointManager;
    private final Protocol protocol;
    private final IAuthorizationTokenProvider tokenProvider;
    private final UserAgentContainer userAgentContainer;
    private final RxCollectionCache collectionCache;
    private final RxPartitionKeyRangeCache routingMapProvider;
    private final GatewayServiceConfigurationReader serviceConfigReader;

    private GatewayAddressCache gatewayAddressCache;
    private AddressResolver addressResolver;
    private CompositeHttpClient<ByteBuf, ByteBuf> httpClient;

    public GlobalAddressResolver(
        CompositeHttpClient<ByteBuf, ByteBuf> httpClient,
        GlobalEndpointManager endpointManager,
        Protocol protocol,
        IAuthorizationTokenProvider tokenProvider,
        RxCollectionCache collectionCache,
        RxPartitionKeyRangeCache routingMapProvider,
        UserAgentContainer userAgentContainer,
        GatewayServiceConfigurationReader serviceConfigReader,
        ConnectionPolicy connectionPolicy) {

        this.httpClient =  httpClient;
        this.endpointManager = endpointManager;
        this.protocol = protocol;
        this.tokenProvider = tokenProvider;
        this.userAgentContainer = userAgentContainer;
        this.collectionCache = collectionCache;
        this.routingMapProvider = routingMapProvider;
        this.serviceConfigReader = serviceConfigReader;

        initializeAddressCache();
    }

    private void initializeAddressCache() {
        this.gatewayAddressCache = new GatewayAddressCache(
            endpointManager.getReadEndpoints().get(0),
            this.tokenProvider,
            this.userAgentContainer,
            httpClient);

        this.addressResolver = new AddressResolver();
        this.addressResolver.initializeCaches(this.collectionCache, this.routingMapProvider, this.gatewayAddressCache);
    }

    @Override
    public Single<AddressInformation[]> resolveAsync(RxDocumentServiceRequest request, boolean forceRefreshPartitionAddresses) {
        return addressResolver.resolveAsync(request, false);
    }
}
