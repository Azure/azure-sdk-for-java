// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;


import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.NotImplementedException;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdAddressCacheToken;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.concurrent.Queues;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GlobalAddressResolver implements AddressResolverExtension {

    private final static int MaxBackupReadRegions = 3;
    final Map<URI, EndpointCache> addressCacheByEndpoint;
    private final RxCollectionCache collectionCache;
    private final GlobalEndpointManager endpointManager;
    private final int maxEndpoints;
    private final Protocol protocol;
    private final RxPartitionKeyRangeCache routingMapProvider;
    private final GatewayServiceConfigurationReader serviceConfigReader;
    private final IAuthorizationTokenProvider tokenProvider;
    private final UserAgentContainer userAgentContainer;
    private final HttpClient httpClient;

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

        int maxBackupReadEndpoints = connectionPolicy.isReadRequestsFallbackEnabled()
            ? GlobalAddressResolver.MaxBackupReadRegions
            : 0;

        this.maxEndpoints = maxBackupReadEndpoints + 2; // for write and alternate write getEndpoint (during failover)
        this.addressCacheByEndpoint = new ConcurrentHashMap<>();

        for (URI endpoint : endpointManager.getWriteEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
        for (URI endpoint : endpointManager.getReadEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
    }

    public void dispose() {
        for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
            endpointCache.addressCache.dispose();
        }
    }

    @Override
    public URI getAddressResolverURI(RxDocumentServiceRequest rxDocumentServiceRequest) {
        return this.endpointManager.resolveServiceEndpoint(rxDocumentServiceRequest);
    }

    @Override
    public Mono<AddressInformation[]> resolveAsync(RxDocumentServiceRequest request, boolean forceRefresh) {
        IAddressResolver resolver = this.getAddressResolver(request);
        return resolver.resolveAsync(request, forceRefresh);
    }

    @Override
    public Mono<Void> updateAsync(final RxDocumentServiceRequest request, final List<RntbdAddressCacheToken> tokens) {

        final ArrayList<CompletableFuture<?>> updates = new ArrayList<>(tokens.size());

        for (RntbdAddressCacheToken token : tokens) {

            final PartitionKeyRangeIdentity partitionKeyRangeIdentity = token.getPartitionKeyRangeIdentity();

            if (partitionKeyRangeIdentity != null) {
                final EndpointCache endpointCache = this.addressCacheByEndpoint.get(token.getAddressResolverURI());
                if (endpointCache != null) {
                    updates.add(endpointCache.addressCache.updateAsync(request, partitionKeyRangeIdentity).toFuture());
                }
            }
        }

        return updates.size() > 0
            ? Mono.fromFuture(CompletableFuture.allOf(updates.toArray(new CompletableFuture<?>[0])))
            : Mono.empty();
    }

    Mono<Void> openAsync(DocumentCollection collection) {
        Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMap = this.routingMapProvider.tryLookupAsync(null,
            collection.getId(), null, null);
        return routingMap.flatMap(collectionRoutingMap -> {

            if (collectionRoutingMap.v == null) {
                return Mono.empty();
            }

            List<PartitionKeyRangeIdentity> ranges =
                collectionRoutingMap.v.getOrderedPartitionKeyRanges().stream().map(range ->
                new PartitionKeyRangeIdentity(collection.getResourceId(), range.getId())).collect(Collectors.toList());
            List<Mono<Void>> tasks = new ArrayList<>();
            for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
                tasks.add(endpointCache.addressCache.openAsync(collection, ranges));
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Mono<Void>[] array = new Mono[this.addressCacheByEndpoint.values().size()];
            return Flux.mergeDelayError(Queues.SMALL_BUFFER_SIZE, tasks.toArray(array)).then();
        });
    }

    private IAddressResolver getAddressResolver(RxDocumentServiceRequest rxDocumentServiceRequest) {
        URI endpoint = this.endpointManager.resolveServiceEndpoint(rxDocumentServiceRequest);
        return this.getOrAddEndpoint(endpoint).addressResolver;
    }

    private EndpointCache getOrAddEndpoint(URI endpoint) {
        EndpointCache endpointCache = this.addressCacheByEndpoint.computeIfAbsent(endpoint, key -> {
            GatewayAddressCache gatewayAddressCache = new GatewayAddressCache(endpoint, protocol, this.tokenProvider,
                this.userAgentContainer, this.httpClient);
            AddressResolver addressResolver = new AddressResolver();
            addressResolver.initializeCaches(this.collectionCache, this.routingMapProvider, gatewayAddressCache);
            EndpointCache cache = new EndpointCache();
            cache.addressCache = gatewayAddressCache;
            cache.addressResolver = addressResolver;
            return cache;
        });

        if (this.addressCacheByEndpoint.size() > this.maxEndpoints) {
            List<URI> allEndpoints = new ArrayList<>(this.endpointManager.getWriteEndpoints());
            allEndpoints.addAll(this.endpointManager.getReadEndpoints());
            Collections.reverse(allEndpoints);
            LinkedList<URI> endpoints = new LinkedList<>(allEndpoints);
            while (this.addressCacheByEndpoint.size() > this.maxEndpoints) {
                if (endpoints.size() > 0) {
                    URI dequeueEnpoint = endpoints.pop();
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
