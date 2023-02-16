// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

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
    private ApiType apiType;

    private HttpClient httpClient;
    private IOpenConnectionsHandler openConnectionsHandler;
    private ConnectionPolicy connectionPolicy;

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
        ConnectionPolicy connectionPolicy,
        ApiType apiType) {
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
        this.connectionPolicy = connectionPolicy;

        int maxBackupReadEndpoints = (connectionPolicy.isReadRequestsFallbackEnabled()) ? GlobalAddressResolver.MaxBackupReadRegions : 0;
        this.maxEndpoints = maxBackupReadEndpoints + 2; // for write and alternate write getEndpoint (during failover)
        this.addressCacheByEndpoint = new ConcurrentHashMap<>();
        this.apiType = apiType;

        for (URI endpoint : endpointManager.getWriteEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
        for (URI endpoint : endpointManager.getReadEndpoints()) {
            this.getOrAddEndpoint(endpoint);
        }
    }

    @Override
    public Flux<OpenConnectionResponse> openConnectionsAndInitCaches(
        CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {

        // Strip the leading "/", which follows the same format for document requests
        // TODO: currently, the cache key used for collectionCache is inconsistent: some are using path with "/",
        //  some use path with stripped leading "/",
        // TODO: ideally it should have been consistent across
        return Flux.fromIterable(proactiveContainerInitConfig.getCosmosContainerIdentities())
                .flatMap(containerIdentity ->
                    this
                        .collectionCache
                        .resolveByNameAsync(
                            null,
                            ImplementationBridgeHelpers
                                .CosmosContainerIdentityHelper
                                .getCosmosContainerIdentityAccessor()
                                .getContainerLink(containerIdentity),
                            null)
                        .flatMapMany(collection -> {
                            if (collection == null) {
                                logger.warn("Can not find the collection, no connections will be opened");
                                return Mono.empty();
                            }

                            return this.routingMapProvider.tryGetOverlappingRangesAsync(
                                            null,
                                            collection.getResourceId(),
                                            PartitionKeyInternalHelper.FullRange,
                                            true,
                                            null)
                                    .map(valueHolder -> {

                                        if (valueHolder == null || valueHolder.v == null || valueHolder.v.size() == 0) {
                                            logger.warn(
                                                    "There is no pkRanges found for collection {}, no connections will be opened",
                                                    collection.getResourceId());
                                            return new ArrayList<PartitionKeyRangeIdentity>();
                                        }

                                        return valueHolder.v
                                                .stream()
                                                .map(pkRange -> new PartitionKeyRangeIdentity(collection.getResourceId(), pkRange.getId()))
                                                .collect(Collectors.toList());
                                    })
                                    .flatMapMany(pkRangeIdentities -> this.openConnectionsAndInitCachesInternal(collection, pkRangeIdentities, proactiveContainerInitConfig));
                        }));
    }

    private Flux<OpenConnectionResponse> openConnectionsAndInitCachesInternal(
            DocumentCollection collection,
            List<PartitionKeyRangeIdentity> partitionKeyRangeIdentities,
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig
    ) {

        if (proactiveContainerInitConfig.getNumProactiveConnectionRegions() > 0) {
            return Flux.fromStream(this.endpointManager.getReadEndpoints().stream())
                    .take(proactiveContainerInitConfig.getNumProactiveConnectionRegions())
                    .flatMap(readEndpoint -> {
                        if (this.addressCacheByEndpoint.containsKey(readEndpoint)) {
                            return this.addressCacheByEndpoint.get(readEndpoint)
                                    .addressCache
                                    .openConnectionsAndInitCaches(collection, partitionKeyRangeIdentities);
                        }
                        return Flux.empty();
                    });
        }

        return Flux.empty();
    }

    @Override
    public void setOpenConnectionsHandler(IOpenConnectionsHandler openConnectionHandler) {
        this.openConnectionsHandler = openConnectionHandler;

        // setup openConnectionHandler for existing address cache
        // For the new ones added later, the openConnectionHandler will pass through constructor
        for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
            endpointCache.addressCache.setOpenConnectionsHandler(openConnectionsHandler);
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
                this.apiType,
                this.endpointManager,
                this.connectionPolicy,
                this.openConnectionsHandler);
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
