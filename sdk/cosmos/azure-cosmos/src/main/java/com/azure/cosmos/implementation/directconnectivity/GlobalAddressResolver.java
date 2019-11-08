// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;


import com.azure.cosmos.ConnectionPolicy;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

;

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
    private HttpClient httpClient;

    public GlobalAddressResolver(
            HttpClient httpClient,
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

        int maxBackupReadEndpoints = (connectionPolicy.getEnableReadRequestsFallback() == null || connectionPolicy.getEnableReadRequestsFallback()) ? GlobalAddressResolver.MaxBackupReadRegions : 0;
        this.maxEndpoints = maxBackupReadEndpoints + 2; // for write and alternate write getEndpoint (during failover)
        this.addressCacheByEndpoint = new ConcurrentHashMap<>();

        for (URL endpoint : endpointManager.getWriteEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
        for (URL endpoint : endpointManager.getReadEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
    }

    Mono<Void> openAsync(DocumentCollection collection) {
        Mono<CollectionRoutingMap> routingMap = this.routingMapProvider.tryLookupAsync(collection.getId(), null, null);
        return routingMap.flatMap(collectionRoutingMap -> {

            List<PartitionKeyRangeIdentity> ranges = ((List<PartitionKeyRange>)collectionRoutingMap.getOrderedPartitionKeyRanges()).stream().map(range ->
                    new PartitionKeyRangeIdentity(collection.getResourceId(), range.getId())).collect(Collectors.toList());
            List<Mono<Void>> tasks = new ArrayList<>();
            for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
                tasks.add(endpointCache.addressCache.openAsync(collection, ranges));
            }
            //  TODO: Not sure if this will work.
            return Mono.whenDelayError(tasks);
        }).switchIfEmpty(Mono.defer(Mono::empty));
    }

    @Override
    public Mono<AddressInformation[]> resolveAsync(RxDocumentServiceRequest request, boolean forceRefresh) {
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
            LinkedList<URL> endpoints = new LinkedList<>(allEndpoints);
            while (this.addressCacheByEndpoint.size() > this.maxEndpoints) {
                if (endpoints.size() > 0) {
                    URL dequeueEnpoint = endpoints.pop();
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
