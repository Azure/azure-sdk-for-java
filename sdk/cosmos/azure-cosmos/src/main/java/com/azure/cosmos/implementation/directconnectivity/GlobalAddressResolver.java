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
import com.azure.cosmos.implementation.IOpenConnectionsHandler;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.models.CosmosContainerIdentity;
import com.azure.cosmos.models.OpenConnectionAggressivenessHint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class GlobalAddressResolver implements IAddressResolver {
    private static final Logger logger = LoggerFactory.getLogger(GlobalAddressResolver.class);

    private final static int MaxBackupReadRegions = 3;
    private final static int MaxContainerCountToBatch = 5;
    private final static int MaxAddressesToBuffer = 100;
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
    private ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor;
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
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig,
            OpenConnectionAggressivenessHint hint,
            boolean isBackgroundFlow
    ) {

        // Strip the leading "/", which follows the same format for document requests
        // TODO: currently, the cache key used for collectionCache is inconsistent: some are using path with "/",
        //  some use path with stripped leading "/",
        // TODO: ideally it should have been consistent across
        List<Flux<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>>> addressResolutionFluxList
                = new ArrayList<>();

        for (CosmosContainerIdentity containerIdentity : proactiveContainerInitConfig.getCosmosContainerIdentities()) {
            Flux<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>> addressResolutionFlux = Flux.just(containerIdentity)
                    .flatMap(cosmosContainerIdentity ->
                            this
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
                                                .flatMapMany(pkRangeIdentities -> {

                                                    String containerLink = ImplementationBridgeHelpers
                                                            .CosmosContainerIdentityHelper
                                                            .getCosmosContainerIdentityAccessor()
                                                            .getContainerLink(containerIdentity);

                                                    return this.resolveAddressesPerCollection(
                                                            containerLink,
                                                            collection,
                                                            pkRangeIdentities,
                                                            proactiveContainerInitConfig,
                                                            hint
                                                    );
                                                        }
                                                );
                                    }), 1, 1);

            addressResolutionFluxList.add(addressResolutionFlux);
        }

        Comparator<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>> comparator = (o1, o2) -> {
            if (o1 == null || o2 == null) return -1;

            ImmutablePair<String, DocumentCollection> containerLinkToColl1 = o1.getLeft();
            ImmutablePair<String, DocumentCollection> containerLinkToColl2 = o2.getLeft();

            return containerLinkToColl1.getLeft().compareTo(containerLinkToColl2.getLeft());
        };

        List<Flux<OpenConnectionResponse>> openConnectionTasks = new ArrayList<>();

        for (int i = 0; i < addressResolutionFluxList.size(); i++) {

            int startIdx = i;
            int endIdx = Math.min(i + MaxContainerCountToBatch, addressResolutionFluxList.size());

            Flux<OpenConnectionResponse> openConnectionTask = getMergeCompareFluxOfVariousPublisherCount(startIdx, endIdx, comparator, addressResolutionFluxList)
                    .publishOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                    .buffer(MaxAddressesToBuffer)
                    .flatMap(immutablePairs -> {
                        Collections.shuffle(immutablePairs);
                        return Mono.just(immutablePairs);
                    })
                    .flatMapIterable(immutablePairs -> immutablePairs)
                    .flatMap(collectionToAddressPair -> {

                                ImmutablePair<String, DocumentCollection> containerLinkToCollection = collectionToAddressPair.left;

                                String containerLink = containerLinkToCollection.left;
                                DocumentCollection collection = containerLinkToCollection.right;
                                AddressInformation address = collectionToAddressPair.right;

                                Map<String, Integer> containerLinkToMinConnectionsMap = ImplementationBridgeHelpers
                                        .CosmosContainerProactiveInitConfigHelper
                                        .getCosmosContainerIdentityAccessor()
                                        .getContainerLinkToMinConnectionsMap(proactiveContainerInitConfig);

                                int connectionsPerReplicaCountForContainer = containerLinkToMinConnectionsMap
                                        .getOrDefault(containerLink, Configs.getMinConnectionPoolSizePerEndpoint());

                                return this.openConnectionInternal(
                                        address,
                                        collection,
                                        proactiveContainerInitConfig,
                                        connectionsPerReplicaCountForContainer,
                                        hint,
                                        isBackgroundFlow
                                );
                    }
                    )
                    .subscribeOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC);

            openConnectionTasks.add(openConnectionTask);
        }

        return Flux.mergeSequential(openConnectionTasks);
    }

    @SuppressWarnings("unchecked")
    public static Flux<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>> getMergeCompareFluxOfVariousPublisherCount(
            int startIdx,
            int endIdx,
            Comparator<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>> comparator,
            List<Flux<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>>> addressResolutionFluxList) {
        return Flux.mergeComparing(1, comparator, addressResolutionFluxList.subList(startIdx, endIdx).toArray(new Flux[0]));
    }

    private Flux<ImmutablePair<ImmutablePair<String, DocumentCollection>, AddressInformation>> resolveAddressesPerCollection(
            String containerLink,
            DocumentCollection collection,
            List<PartitionKeyRangeIdentity> partitionKeyRangeIdentities,
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig,
            OpenConnectionAggressivenessHint hint
    ) {
        if (proactiveContainerInitConfig.getProactiveConnectionRegionsCount() > 0) {
            return Flux.fromStream(this.endpointManager.getReadEndpoints().stream())
                    .take(proactiveContainerInitConfig.getProactiveConnectionRegionsCount())
                    .flatMap(readEndpoint -> {
                        if (this.addressCacheByEndpoint.containsKey(readEndpoint)) {
                            return this.addressCacheByEndpoint.get(readEndpoint)
                                    .addressCache
                                    .resolveAddressesAndInitCaches(
                                            containerLink,
                                            collection,
                                            partitionKeyRangeIdentities,
                                            hint
                                    );
                        }
                        return Flux.empty();
                    }, 1);
        }

        return Flux.empty();
    }

    private Flux<OpenConnectionResponse> openConnectionInternal(
            AddressInformation address,
            DocumentCollection documentCollection,
            CosmosContainerProactiveInitConfig proactiveContainerInitConfig,
            int connectionPerEndpointCount,
            OpenConnectionAggressivenessHint hint,
            boolean isBackgroundFlow
    ) {
        if (proactiveContainerInitConfig.getProactiveConnectionRegionsCount() > 0) {
            return Flux.fromStream(this.endpointManager.getReadEndpoints().stream())
                    .take(proactiveContainerInitConfig.getProactiveConnectionRegionsCount())
                    .flatMap(readEndpoint -> {
                        if (this.addressCacheByEndpoint.containsKey(readEndpoint)) {
                            return this.addressCacheByEndpoint.get(readEndpoint)
                                    .addressCache
                                    .openConnections(
                                            address,
                                            documentCollection,
                                            hint,
                                            connectionPerEndpointCount,
                                            isBackgroundFlow
                                    );
                        }
                        return Flux.empty();
                    }, 1);
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
    public void setOpenConnectionsProcessor(ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor) {
        this.proactiveOpenConnectionsProcessor = proactiveOpenConnectionsProcessor;

        // setup proactiveOpenConnectionsProcessor for existing address cache
        // For the new ones added later, the openConnectionHandler will pass through constructor
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
                this.openConnectionsHandler,
                this.proactiveOpenConnectionsProcessor
            );
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
