// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;


import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.concurrent.Queues;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GlobalAddressResolver implements IAddressResolver {
    private static final Logger logger = LoggerFactory.getLogger(GlobalAddressResolver.class);

    private final static int MaxBackupReadRegions = 3;
    private final DiagnosticsClientContext diagnosticsClientContext;
    private final GlobalEndpointManager endpointManager;
    private final Protocol protocol;
    private final IAuthorizationTokenProvider tokenProvider;
    private final UserAgentContainer userAgentContainer;
    private final RxCollectionCache collectionCache;
    private final RxPartitionKeyRangeCache routingMapProvider;
    private final int maxEndpoints;
    private final GatewayServiceConfigurationReader serviceConfigReader;
    final Map<URI, EndpointCache> addressCacheByEndpoint;
    private final boolean tcpConnectionEndpointRediscoveryEnabled;

    private HttpClient httpClient;

    public GlobalAddressResolver(
            DiagnosticsClientContext diagnosticsClientContext,
            HttpClient httpClient,
            GlobalEndpointManager endpointManager,
            Protocol protocol,
            IAuthorizationTokenProvider tokenProvider,
            RxCollectionCache collectionCache,
            RxPartitionKeyRangeCache routingMapProvider,
            UserAgentContainer userAgentContainer,
            GatewayServiceConfigurationReader serviceConfigReader,
            ConnectionPolicy connectionPolicy) {
        this.diagnosticsClientContext = diagnosticsClientContext;
        this.httpClient = httpClient;
        this.endpointManager = endpointManager;
        this.protocol = protocol;
        this.tokenProvider = tokenProvider;
        this.userAgentContainer = userAgentContainer;
        this.collectionCache = collectionCache;
        this.routingMapProvider = routingMapProvider;
        this.serviceConfigReader = serviceConfigReader;
        this.tcpConnectionEndpointRediscoveryEnabled = connectionPolicy.isTcpConnectionEndpointRediscoveryEnabled();

        int maxBackupReadEndpoints = (connectionPolicy.isReadRequestsFallbackEnabled()) ? GlobalAddressResolver.MaxBackupReadRegions : 0;
        this.maxEndpoints = maxBackupReadEndpoints + 2; // for write and alternate write getEndpoint (during failover)
        this.addressCacheByEndpoint = new ConcurrentHashMap<>();

        for (URI endpoint : endpointManager.getWriteEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
        for (URI endpoint : endpointManager.getReadEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
    }

    Mono<Void> openAsync(DocumentCollection collection) {
        Mono<Utils.ValueHolder<CollectionRoutingMap>> routingMap = this.routingMapProvider.tryLookupAsync(null, collection.getId(), null, null);
        return routingMap.flatMap(collectionRoutingMap -> {

            if ( collectionRoutingMap.v == null) {
                return Mono.empty();
            }

            List<PartitionKeyRangeIdentity> ranges = collectionRoutingMap.v.getOrderedPartitionKeyRanges().stream().map(range ->
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

    @Override
    public void updateAddresses(final RxDocumentServiceRequest request, final URI serverKey) {

        Objects.requireNonNull(request, "expected non-null request");
        Objects.requireNonNull(serverKey, "expected non-null serverKey");

        if (this.tcpConnectionEndpointRediscoveryEnabled) {
            URI serviceEndpoint = this.endpointManager.resolveServiceEndpoint(request);
            this.addressCacheByEndpoint.computeIfPresent(serviceEndpoint, (ignored, endpointCache) -> {

                final GatewayAddressCache addressCache = endpointCache.addressCache;
                addressCache.updateAddresses(serverKey);

                return endpointCache;
            });
        } else {
            logger.warn("tcpConnectionEndpointRediscovery is not enabled, should not reach here.");
        }
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
        URI endpoint = this.endpointManager.resolveServiceEndpoint(rxDocumentServiceRequest);
        return this.getOrAddEndpoint(endpoint).addressResolver;
    }

    private EndpointCache getOrAddEndpoint(URI endpoint) {
        EndpointCache endpointCache = this.addressCacheByEndpoint.computeIfAbsent(endpoint , key -> {
            GatewayAddressCache gatewayAddressCache = new GatewayAddressCache(
                this.diagnosticsClientContext,
                endpoint,
                protocol,
                this.tokenProvider,
                this.userAgentContainer,
                this.httpClient,
                this.tcpConnectionEndpointRediscoveryEnabled);
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
                    URI dequeueEndpoint = endpoints.pop();
                    if (this.addressCacheByEndpoint.get(dequeueEndpoint) != null) {
                        this.addressCacheByEndpoint.remove(dequeueEndpoint);
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
