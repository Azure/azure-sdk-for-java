// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    public int updateAddresses(final URI serverKey) {

        Objects.requireNonNull(serverKey, "expected non-null serverKey");

        AtomicInteger updatedCount = new AtomicInteger(0);

        if (this.tcpConnectionEndpointRediscoveryEnabled) {
            for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
                final GatewayAddressCache addressCache = endpointCache.addressCache;

                updatedCount.accumulateAndGet(addressCache.updateAddresses(serverKey), (oldValue, newValue) -> oldValue + newValue);
            }
        } else {
            logger.warn("tcpConnectionEndpointRediscovery is not enabled, should not reach here.");
        }

        return updatedCount.get();
    }

    @Override
    public Flux<OpenConnectionResponse> openConnectionsAndInitCaches(String containerLink) {
        checkArgument(StringUtils.isNotEmpty(containerLink), "Argument 'containerLink' should not be null nor empty");

        // Strip the leading "/", which follows the same format for document requests
        // TODO: currently, the cache key used for collectionCache is inconsistent: some are using path with "/", some use path with stripped leading "/",
        // TODO: ideally it should have been consistent across
        String cacheKey = StringUtils.strip(containerLink, Constants.Properties.PATH_SEPARATOR);
        return this.collectionCache.resolveByNameAsync(null, cacheKey, null)
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

                                if(valueHolder == null || valueHolder.v == null || valueHolder.v.size() == 0) {
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
                            .flatMapMany(pkRangeIdentities -> this.openConnectionsAndInitCachesInternal(collection, pkRangeIdentities));
                });
    }

    private Flux<OpenConnectionResponse> openConnectionsAndInitCachesInternal(
            DocumentCollection collection,
            List<PartitionKeyRangeIdentity> partitionKeyRangeIdentities) {

        // Currently, we will only open connections to current read region
        return Flux.just(this.endpointManager.getReadEndpoints().stream().findFirst())
                .flatMap(readEndpointOptional -> {
                    if (readEndpointOptional.isPresent()) {
                        if (this.addressCacheByEndpoint.containsKey(readEndpointOptional.get())) {
                            return this.addressCacheByEndpoint.get(readEndpointOptional.get())
                                        .addressCache
                                        .openConnectionsAndInitCaches(collection, partitionKeyRangeIdentities);
                        }
                    }

                    return Flux.empty();
                });
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
    public Mono<PartitionAddressInformation> resolveAsync(RxDocumentServiceRequest request, boolean forceRefresh) {
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
                this.tcpConnectionEndpointRediscoveryEnabled,
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
