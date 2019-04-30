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
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.PartitionKeyRange;
import com.microsoft.azure.cosmosdb.internal.UserAgentContainer;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.AddressInformation;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.AddressResolver;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.GatewayAddressCache;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.GatewayServiceConfigurationReader;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.IAddressResolver;
import com.microsoft.azure.cosmosdb.internal.directconnectivity.Protocol;
import com.microsoft.azure.cosmosdb.internal.routing.CollectionRoutingMap;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyRangeIdentity;
import com.microsoft.azure.cosmosdb.rx.internal.GlobalEndpointManager;
import com.microsoft.azure.cosmosdb.rx.internal.IAuthorizationTokenProvider;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxCollectionCache;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxPartitionKeyRangeCache;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.CompositeHttpClient;
import rx.Completable;
import rx.Single;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GlobalAddressResolver implements IAddressResolver {
    private final static int MaxBackupReadRegions = 3;
    private final GlobalEndpointManager endpointManager;
    private final Protocol protocol;
    private final IAuthorizationTokenProvider tokenProvider;
    private final UserAgentContainer userAgentContainer;
    private final RxCollectionCache collectionCache;
    private final RxPartitionKeyRangeCache routingMapProvider;
    private final int maxEndpoints;
    private final GatewayServiceConfigurationReader serviceConfigReader;
    final Map<URL, EndpointCache> addressCacheByEndpoint;

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

        this.httpClient = httpClient;
        this.endpointManager = endpointManager;
        this.protocol = protocol;
        this.tokenProvider = tokenProvider;
        this.userAgentContainer = userAgentContainer;
        this.collectionCache = collectionCache;
        this.routingMapProvider = routingMapProvider;
        this.serviceConfigReader = serviceConfigReader;

        int maxBackupReadEndpoints = (connectionPolicy.isEnableReadRequestsFallback() == null || connectionPolicy.isEnableReadRequestsFallback()) ? GlobalAddressResolver.MaxBackupReadRegions : 0;
        this.maxEndpoints = maxBackupReadEndpoints + 2; // for write and alternate write endpoint (during failover)
        this.addressCacheByEndpoint = new ConcurrentHashMap<>();

        for (URL endpoint : endpointManager.getWriteEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
        for (URL endpoint : endpointManager.getReadEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
    }

    Completable openAsync(DocumentCollection collection) {
        Single<CollectionRoutingMap> routingMap = this.routingMapProvider.tryLookupAsync(collection.getId(), null, null);
        return routingMap.flatMapCompletable(collectionRoutingMap -> {
            if (collectionRoutingMap == null) {
                return Completable.complete();
            }

            List<PartitionKeyRangeIdentity> ranges = ((List<PartitionKeyRange>)collectionRoutingMap.getOrderedPartitionKeyRanges()).stream().map(range ->
                    new PartitionKeyRangeIdentity(collection.getResourceId(), range.getId())).collect(Collectors.toList());
            List<Completable> tasks = new ArrayList<>();
            for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
                tasks.add(endpointCache.addressCache.openAsync(collection, ranges));
            }
            return Completable.mergeDelayError(tasks);
        });
    }

    @Override
    public Single<AddressInformation[]> resolveAsync(RxDocumentServiceRequest request, boolean forceRefresh) {
        IAddressResolver resolver = this.getAddressResolver(request);
        return resolver.resolveAsync(request, forceRefresh);
    }

    public void dispose() {
        for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
            endpointCache.addressCache.dispose();
        }
    }

    private IAddressResolver getAddressResolver(RxDocumentServiceRequest rxDocumentServiceRequest) {
        URL endpoint = this.endpointManager.resolveServiceEndpoint(rxDocumentServiceRequest);
        return this.getOrAddEndpoint(endpoint).addressResolver;
    }

    private EndpointCache getOrAddEndpoint(URL endpoint) {
        EndpointCache endpointCache = this.addressCacheByEndpoint.computeIfAbsent(endpoint , key -> {
            GatewayAddressCache gatewayAddressCache = new GatewayAddressCache(endpoint, protocol, this.tokenProvider, this.userAgentContainer, this.httpClient);
            AddressResolver addressResolver = new AddressResolver();
            addressResolver.initializeCaches(this.collectionCache, this.routingMapProvider, gatewayAddressCache);
            EndpointCache cache = new EndpointCache();
            cache.addressCache = gatewayAddressCache;
            cache.addressResolver = addressResolver;
            return cache;
        });

        if (this.addressCacheByEndpoint.size() > this.maxEndpoints) {
            List<URL> allEndpoints = new ArrayList(this.endpointManager.getWriteEndpoints());
            allEndpoints.addAll(this.endpointManager.getReadEndpoints());
            Collections.reverse(allEndpoints);
            Queue<URL> endpoints = new LinkedList<>(allEndpoints);
            while (this.addressCacheByEndpoint.size() > this.maxEndpoints) {
                if (endpoints.size() > 0) {
                    URL dequeueEnpoint = ((LinkedList<URL>) endpoints).pop();
                    if (this.addressCacheByEndpoint.get(dequeueEnpoint) != null) {
                        this.addressCacheByEndpoint.remove(dequeueEnpoint);
                    }
                } else {
                    break;
                }
            }
        }
        return endpointCache;
    }

    static class EndpointCache {
        GatewayAddressCache addressCache;
        AddressResolver addressResolver;
    }
}
