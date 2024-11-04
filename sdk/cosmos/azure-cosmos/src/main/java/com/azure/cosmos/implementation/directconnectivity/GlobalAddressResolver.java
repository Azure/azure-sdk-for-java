// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.faultinjection.GatewayServerErrorInjector;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.CosmosContainerIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
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
    private ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor;
    private ConnectionPolicy connectionPolicy;
    private GatewayServerErrorInjector gatewayServerErrorInjector;

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

    public Flux<Void> submitOpenConnectionTasksAndInitCaches(CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {

        // Strip the leading "/", which follows the same format for document requests
        // TODO: currently, the cache key used for collectionCache is inconsistent: some are using path with "/",
        // some use path with stripped leading "/",
        // TODO: ideally it should have been consistent across
        return Flux.fromIterable(proactiveContainerInitConfig.getCosmosContainerIdentities())
            .publishOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
            .flatMap(cosmosContainerIdentity -> {
                return this
                    .collectionCache
                    .resolveByNameAsync(
                        null,
                        ImplementationBridgeHelpers
                            .CosmosContainerIdentityHelper
                            .getCosmosContainerIdentityAccessor()
                            .getContainerLink(cosmosContainerIdentity),
                        null)
                    .flatMapMany(collection -> {
                        if (collection == null) {
                            logger.warn("Can not find the collection, no connections will be opened");
                            return Flux.empty();
                        }

                        return this.routingMapProvider.tryGetOverlappingRangesAsync(
                                null,
                                collection.getResourceId(),
                                PartitionKeyInternalHelper.FullRange,
                                true,
                                null)
                            .flatMap(valueHolder -> {

                                String containerLink = ImplementationBridgeHelpers
                                    .CosmosContainerIdentityHelper
                                    .getCosmosContainerIdentityAccessor()
                                    .getContainerLink(cosmosContainerIdentity);

                                if (valueHolder == null || valueHolder.v == null || valueHolder.v.size() == 0) {
                                    logger.warn(
                                        "There is no pkRanges found for collection {}, no connections will be opened",
                                        collection.getResourceId());
                                    return Mono.just(new ImmutablePair<>(containerLink, new ArrayList<PartitionKeyRangeIdentity>()));
                                }

                                List<PartitionKeyRangeIdentity> pkrs = valueHolder.v
                                    .stream()
                                    .map(pkRange -> new PartitionKeyRangeIdentity(collection.getResourceId(), pkRange.getId()))
                                    .collect(Collectors.toList());

                                return Mono.just(new ImmutablePair<String, List<PartitionKeyRangeIdentity>>(containerLink, pkrs));
                            })
                            .flatMapMany(containerLinkToPkrs -> {
                                if (proactiveContainerInitConfig.getProactiveConnectionRegionsCount() > 0) {
                                    return Flux.fromIterable(this.endpointManager.getReadEndpoints().subList(0, proactiveContainerInitConfig.getProactiveConnectionRegionsCount()))
                                        .flatMap(readEndpoint -> {
                                            if (this.addressCacheByEndpoint.containsKey(readEndpoint)) {
                                                EndpointCache endpointCache = this.addressCacheByEndpoint.get(readEndpoint);
                                                return this.resolveAddressesPerCollection(
                                                        endpointCache,
                                                        containerLinkToPkrs.left,
                                                        collection,
                                                        containerLinkToPkrs.right)
                                                    .flatMap(collectionToAddresses -> {
                                                        ImmutablePair<String, DocumentCollection> containerLinkToCollection
                                                            = collectionToAddresses.left;
                                                        AddressInformation addressInformation =
                                                            collectionToAddresses.right;

                                                        Map<CosmosContainerIdentity, ContainerDirectConnectionMetadata> containerPropertiesMap = ImplementationBridgeHelpers
                                                            .CosmosContainerProactiveInitConfigHelper
                                                            .getCosmosContainerProactiveInitConfigAccessor()
                                                            .getContainerPropertiesMap(proactiveContainerInitConfig);

                                                        ContainerDirectConnectionMetadata containerDirectConnectionMetadata = containerPropertiesMap
                                                                .get(cosmosContainerIdentity);

                                                        // check against the COSMOS.MIN_CONNECTION_POOL_SIZE_PER_ENDPOINT system property
                                                        // during client initialization
                                                        int connectionsPerEndpointCountForContainer = Math.max(containerDirectConnectionMetadata
                                                            .getMinConnectionPoolSizePerEndpointForContainer(), Configs.getMinConnectionPoolSizePerEndpoint());

                                                        return this.submitOpenConnectionInternal(
                                                                endpointCache,
                                                                addressInformation,
                                                                containerLinkToCollection.getRight(),
                                                                connectionsPerEndpointCountForContainer).then();
                                                    })
                                                    // onErrorResume helps to fallback in case of gateway issues when doing address resolution
                                                    // requests for a specific region
                                                    // this ensures connection warm up can move onto subsequent regions if configured
                                                    .onErrorResume(throwable -> {
                                                        // no particular reason to have specific handling for a CosmosException type
                                                        // since any error thrown in the connection warmup flow is eventually swallowed
                                                        // downstream
                                                        Throwable unwrappedThrowable = Exceptions.unwrap(throwable);
                                                        logger.warn("An exception occurred when resolving addresses for region : {}",
                                                                readEndpoint, unwrappedThrowable);
                                                        return Flux.empty();
                                                    });
                                            }

                                            return Flux.empty();
                                            // Resolve metadata GET address requests 1 region at a time
                                        }, 1);
                                }

                                return Flux.empty();
                            });
                    });
            }, Configs.getCPUCnt(), Configs.getCPUCnt());
    }

    private Flux<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>> resolveAddressesPerCollection(
            EndpointCache endpointCache,
            String containerLink,
            DocumentCollection collection,
            List<PartitionKeyRangeIdentity> partitionKeyRangeIdentities) {
        return endpointCache
            .addressCache
            .resolveAddressesAndInitCaches(
                containerLink,
                collection,
                partitionKeyRangeIdentities
            );
    }

    private Mono<OpenConnectionResponse> submitOpenConnectionInternal(
            EndpointCache endpointCache,
            AddressInformation address,
            DocumentCollection documentCollection,
            int connectionPerEndpointCount) {

        return endpointCache.addressCache.submitOpenConnectionTask(address, documentCollection, connectionPerEndpointCount);
    }

    @Override
    public void setOpenConnectionsProcessor(ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor) {
        this.proactiveOpenConnectionsProcessor = proactiveOpenConnectionsProcessor;

        // setup proactiveOpenConnectionsProcessor for existing address cache
        // For the new ones added later, the proactiveOpenConnectionsProcessor will pass through constructor
        for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
            endpointCache.addressCache.setOpenConnectionsProcessor(this.proactiveOpenConnectionsProcessor);
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

    public void configureFaultInjectorProvider(IFaultInjectorProvider faultInjectorProvider, Configs configs) {
        if (this.gatewayServerErrorInjector == null) {
            this.gatewayServerErrorInjector = new GatewayServerErrorInjector(configs);

            // setup gatewayServerErrorInjector for existing address cache
            // For the new ones added later, the gatewayServerErrorInjector will pass through constructor
            for (EndpointCache endpointCache : this.addressCacheByEndpoint.values()) {
                endpointCache.addressCache.setGatewayServerErrorInjector(this.gatewayServerErrorInjector);
            }
        }

        this.gatewayServerErrorInjector.registerServerErrorInjector(faultInjectorProvider.getServerErrorInjector());
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
                this.proactiveOpenConnectionsProcessor,
                this.gatewayServerErrorInjector);
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

    public GatewayAddressCache getGatewayAddressCache(URI endpoint) {
        EndpointCache endpointCache = this.addressCacheByEndpoint.get(endpoint);

        if (endpointCache != null) {
            return endpointCache.addressCache;
        }

        return null;
    }

    static class EndpointCache {
        GatewayAddressCache addressCache;
        AddressResolver addressResolver;
    }
}
