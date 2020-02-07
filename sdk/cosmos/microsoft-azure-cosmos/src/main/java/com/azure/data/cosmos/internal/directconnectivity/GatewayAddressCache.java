// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.PartitionKeyRangeGoneException;
import com.azure.data.cosmos.internal.AuthorizationTokenType;
import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.Exceptions;
import com.azure.data.cosmos.internal.HttpConstants;
import com.azure.data.cosmos.internal.IAuthorizationTokenProvider;
import com.azure.data.cosmos.internal.JavaStreamUtils;
import com.azure.data.cosmos.internal.OperationType;
import com.azure.data.cosmos.internal.PartitionKeyRange;
import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.PathsHelper;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.ResourceType;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.caches.AsyncCache;
import com.azure.data.cosmos.internal.http.HttpClient;
import com.azure.data.cosmos.internal.http.HttpHeaders;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GatewayAddressCache implements IAddressCache {
    private final static Logger logger = LoggerFactory.getLogger(GatewayAddressCache.class);
    private final static String protocolFilterFormat = "%s eq %s";
    private final static int DefaultBatchSize = 50;

    private final static int DefaultSuboptimalPartitionForceRefreshIntervalInSeconds = 600;
    private final ServiceConfig serviceConfig = ServiceConfig.getInstance();

    private final String databaseFeedEntryUrl = PathsHelper.generatePath(ResourceType.Database, "", true);
    private final URL serviceEndpoint;
    private final URL addressEndpoint;

    private final AsyncCache<PartitionKeyRangeIdentity, AddressInformation[]> serverPartitionAddressCache;
    private final ConcurrentHashMap<PartitionKeyRangeIdentity, Instant> suboptimalServerPartitionTimestamps;
    private final long suboptimalPartitionForceRefreshIntervalInSeconds;

    private final String protocolScheme;
    private final String protocolFilter;
    private final IAuthorizationTokenProvider tokenProvider;
    private final HashMap<String, String> defaultRequestHeaders;
    private final HttpClient httpClient;

    private volatile Pair<PartitionKeyRangeIdentity, AddressInformation[]> masterPartitionAddressCache;
    private volatile Instant suboptimalMasterPartitionTimestamp;

    public GatewayAddressCache(
            URL serviceEndpoint,
            Protocol protocol,
            IAuthorizationTokenProvider tokenProvider,
            UserAgentContainer userAgent,
            HttpClient httpClient,
            long suboptimalPartitionForceRefreshIntervalInSeconds) {
        try {
            this.addressEndpoint = new URL(serviceEndpoint, Paths.ADDRESS_PATH_SEGMENT);
        } catch (MalformedURLException e) {
            logger.error("serviceEndpoint {} is invalid", serviceEndpoint, e);
            assert false;
            throw new IllegalStateException(e);
        }
        this.tokenProvider = tokenProvider;
        this.serviceEndpoint = serviceEndpoint;
        this.serverPartitionAddressCache = new AsyncCache<>();
        this.suboptimalServerPartitionTimestamps = new ConcurrentHashMap<>();
        this.suboptimalMasterPartitionTimestamp = Instant.MAX;

        this.suboptimalPartitionForceRefreshIntervalInSeconds = suboptimalPartitionForceRefreshIntervalInSeconds;

        this.protocolScheme = protocol.scheme();
        this.protocolFilter = String.format(GatewayAddressCache.protocolFilterFormat,
                Constants.Properties.PROTOCOL,
                this.protocolScheme);

        this.httpClient = httpClient;

        if (userAgent == null) {
            userAgent = new UserAgentContainer();
        }

        defaultRequestHeaders = new HashMap<>();
        defaultRequestHeaders.put(HttpConstants.HttpHeaders.USER_AGENT, userAgent.getUserAgent());

        // Set requested API version header for version enforcement.
        defaultRequestHeaders.put(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);
    }

    public GatewayAddressCache(
            URL serviceEndpoint,
            Protocol protocol,
            IAuthorizationTokenProvider tokenProvider,
            UserAgentContainer userAgent,
            HttpClient httpClient) {
        this(serviceEndpoint,
             protocol,
             tokenProvider,
             userAgent,
             httpClient,
             DefaultSuboptimalPartitionForceRefreshIntervalInSeconds);
    }

    private URL getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    @Override
    public Mono<Utils.ValueHolder<AddressInformation[]>> tryGetAddresses(RxDocumentServiceRequest request,
                                                        PartitionKeyRangeIdentity partitionKeyRangeIdentity,
                                                        boolean forceRefreshPartitionAddresses) {

        com.azure.data.cosmos.internal.Utils.checkNotNullOrThrow(request, "request", "");
        com.azure.data.cosmos.internal.Utils.checkNotNullOrThrow(partitionKeyRangeIdentity, "partitionKeyRangeIdentity", "");

        logger.debug("PartitionKeyRangeIdentity {}, forceRefreshPartitionAddresses {}",
            partitionKeyRangeIdentity,
            forceRefreshPartitionAddresses);

        if (StringUtils.equals(partitionKeyRangeIdentity.getPartitionKeyRangeId(),
                PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)) {

            // if that's master partition return master partition address!
            return this.resolveMasterAsync(request, forceRefreshPartitionAddresses, request.properties)
                       .map(partitionKeyRangeIdentityPair -> new Utils.ValueHolder<>(partitionKeyRangeIdentityPair.getRight()));
        }

        Instant suboptimalServerPartitionTimestamp = this.suboptimalServerPartitionTimestamps.get(partitionKeyRangeIdentity);

        if (suboptimalServerPartitionTimestamp != null) {
            logger.debug("suboptimalServerPartitionTimestamp is {}", suboptimalServerPartitionTimestamp);

            boolean forceRefreshDueToSuboptimalPartitionReplicaSet = Duration.between(suboptimalServerPartitionTimestamp, Instant.now()).getSeconds()
                    > this.suboptimalPartitionForceRefreshIntervalInSeconds;

            if (forceRefreshDueToSuboptimalPartitionReplicaSet) {
                // Compares the existing value for the specified key with a specified value,
                // and if they are equal, updates the key with a third value.
                Instant newValue = this.suboptimalServerPartitionTimestamps.computeIfPresent(partitionKeyRangeIdentity,
                        (key, oldVal) -> {
                            logger.debug("key = {}, oldValue = {}", key, oldVal);

                            if (suboptimalServerPartitionTimestamp.equals(oldVal)) {
                                return Instant.MAX;
                            } else {
                                return oldVal;
                            }
                        });

                logger.debug("newValue is {}", newValue);
                if (!newValue.equals(suboptimalServerPartitionTimestamp)) {
                    logger.debug("setting forceRefreshPartitionAddresses to true");
                    // the value was replaced;
                    forceRefreshPartitionAddresses = true;
                }
            }
        }

        final boolean forceRefreshPartitionAddressesModified = forceRefreshPartitionAddresses;

        if (forceRefreshPartitionAddressesModified) {
            logger.debug("refresh serverPartitionAddressCache for {}", partitionKeyRangeIdentity);

            this.serverPartitionAddressCache.refresh(
                    partitionKeyRangeIdentity,
                    () -> this.getAddressesForRangeId(
                            request,
                            partitionKeyRangeIdentity.getCollectionRid(),
                            partitionKeyRangeIdentity.getPartitionKeyRangeId(),
                            true));

            this.suboptimalServerPartitionTimestamps.remove(partitionKeyRangeIdentity);
        }

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs = this.serverPartitionAddressCache.getAsync(
                partitionKeyRangeIdentity,
                null,
                () -> this.getAddressesForRangeId(
                        request,
                        partitionKeyRangeIdentity.getCollectionRid(),
                        partitionKeyRangeIdentity.getPartitionKeyRangeId(),
                    false)).map(Utils.ValueHolder::new);

        return addressesObs.map(
            addressesValueHolder -> {
                if (notAllReplicasAvailable(addressesValueHolder.v)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("not all replicas available {}", JavaStreamUtils.info(addressesValueHolder.v));
                        }

                        this.suboptimalServerPartitionTimestamps.putIfAbsent(partitionKeyRangeIdentity, Instant.now());
                    }

                    return addressesValueHolder;
                }).onErrorResume(throwable -> {
                    Throwable unwrappedException = reactor.core.Exceptions.unwrap(throwable);
                    CosmosClientException dce = com.azure.data.cosmos.internal.Utils.as(unwrappedException, CosmosClientException.class);
                    if (dce == null) {
                        logger.error("unexpected failure", throwable);
                        if (forceRefreshPartitionAddressesModified) {
                            this.suboptimalServerPartitionTimestamps.remove(partitionKeyRangeIdentity);
                        }
                        return Mono.error(unwrappedException);
                    } else {
                        logger.debug("tryGetAddresses dce", dce);
                        if (Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.NOTFOUND) ||
                                Exceptions.isStatusCode(dce, HttpConstants.StatusCodes.GONE) ||
                                Exceptions.isSubStatusCode(dce, HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE)) {
                            //remove from suboptimal cache in case the collection+pKeyRangeId combo is gone.
                            this.suboptimalServerPartitionTimestamps.remove(partitionKeyRangeIdentity);

                            logger.debug("tryGetAddresses: inner onErrorResumeNext return empty", dce);
                            return Mono.just(new Utils.ValueHolder<>(null));
                        }
                        return Mono.error(unwrappedException);
                    }
        });
    }

    public Mono<List<Address>> getServerAddressesViaGatewayAsync(
            RxDocumentServiceRequest request,
            String collectionRid,
            List<String> partitionKeyRangeIds,
            boolean forceRefresh) {
        if (logger.isDebugEnabled()) {
            logger.debug("getServerAddressesViaGatewayAsync collectionRid {}, partitionKeyRangeIds {}", collectionRid,
                JavaStreamUtils.toString(partitionKeyRangeIds, ","));
        }
        String entryUrl = PathsHelper.generatePath(ResourceType.Document, collectionRid, true);
        HashMap<String, String> addressQuery = new HashMap<>();

        addressQuery.put(HttpConstants.QueryStrings.URL, HttpUtils.urlEncode(entryUrl));

        HashMap<String, String> headers = new HashMap<>(defaultRequestHeaders);
        if (forceRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_REFRESH, Boolean.TRUE.toString());
        }

        addressQuery.put(HttpConstants.QueryStrings.FILTER, HttpUtils.urlEncode(this.protocolFilter));

        addressQuery.put(HttpConstants.QueryStrings.PARTITION_KEY_RANGE_IDS, String.join(",", partitionKeyRangeIds));
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        String token;

        token = this.tokenProvider.getUserAuthorizationToken(
                collectionRid,
                ResourceType.Document,
                HttpConstants.HttpMethods.GET,
                headers,
                AuthorizationTokenType.PrimaryMasterKey,
                request.properties);

        if (token == null && request.getIsNameBased()) {
            // User doesn't have rid based resource token. Maybe user has name based.
            String collectionAltLink = PathsHelper.getCollectionPath(request.getResourceAddress());
            token = this.tokenProvider.getUserAuthorizationToken(
                    collectionAltLink,
                    ResourceType.Document,
                    HttpConstants.HttpMethods.GET,
                    headers,
                    AuthorizationTokenType.PrimaryMasterKey,
                    request.properties);
        }

        token = HttpUtils.urlEncode(token);
        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, token);
        URL targetEndpoint = Utils.setQuery(this.addressEndpoint.toString(), Utils.createQuery(addressQuery));
        String identifier = logAddressResolutionStart(request, targetEndpoint);

        HttpHeaders httpHeaders = new HttpHeaders(headers.size());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpHeaders.set(entry.getKey(), entry.getValue());
        }

        HttpRequest httpRequest;
        try {
            httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint.toURI(), targetEndpoint.getPort(), httpHeaders);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(targetEndpoint.toString(), e);
        }
        Mono<HttpResponse> httpResponseMono = this.httpClient.send(httpRequest);

        Mono<RxDocumentServiceResponse> dsrObs = HttpClientUtils.parseResponseAsync(httpResponseMono, httpRequest);
        return dsrObs.map(
                dsr -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getServerAddressesViaGatewayAsync deserializes result");
                    }
                    logAddressResolutionEnd(request, identifier);
                    return dsr.getQueryResponse(Address.class);
                });
    }

    public void dispose() {
        // TODO We will implement this in future once we will move to httpClient to CompositeHttpClient
        //https://msdata.visualstudio.com/CosmosDB/_workitems/edit/340842
    }

    private Mono<Pair<PartitionKeyRangeIdentity, AddressInformation[]>> resolveMasterAsync(RxDocumentServiceRequest request, boolean forceRefresh, Map<String, Object> properties) {
        logger.debug("resolveMasterAsync forceRefresh: {}", forceRefresh);
        Pair<PartitionKeyRangeIdentity, AddressInformation[]> masterAddressAndRangeInitial = this.masterPartitionAddressCache;

        forceRefresh = forceRefresh ||
                (masterAddressAndRangeInitial != null &&
                        notAllReplicasAvailable(masterAddressAndRangeInitial.getRight()) &&
                        Duration.between(this.suboptimalMasterPartitionTimestamp, Instant.now()).getSeconds() > this.suboptimalPartitionForceRefreshIntervalInSeconds);

        if (forceRefresh || this.masterPartitionAddressCache == null) {
            Mono<List<Address>> masterReplicaAddressesObs = this.getMasterAddressesViaGatewayAsync(
                    request,
                    ResourceType.Database,
                    null,
                    databaseFeedEntryUrl,
                    forceRefresh,
                    false,
                    properties);

            return masterReplicaAddressesObs.map(
                    masterAddresses -> {
                        Pair<PartitionKeyRangeIdentity, AddressInformation[]> masterAddressAndRangeRes =
                                this.toPartitionAddressAndRange("", masterAddresses);
                        this.masterPartitionAddressCache = masterAddressAndRangeRes;

                        if (notAllReplicasAvailable(masterAddressAndRangeRes.getRight())
                                && this.suboptimalMasterPartitionTimestamp.equals(Instant.MAX)) {
                            this.suboptimalMasterPartitionTimestamp = Instant.now();
                        } else {
                            this.suboptimalMasterPartitionTimestamp = Instant.MAX;
                        }

                        return masterPartitionAddressCache;
                    })
                    .doOnError(
                            e -> {
                                this.suboptimalMasterPartitionTimestamp = Instant.MAX;
                            });
        } else {
            if (notAllReplicasAvailable(masterAddressAndRangeInitial.getRight())
                    && this.suboptimalMasterPartitionTimestamp.equals(Instant.MAX)) {
                this.suboptimalMasterPartitionTimestamp = Instant.now();
            }

            return Mono.just(masterAddressAndRangeInitial);
        }
    }

    private Mono<AddressInformation[]> getAddressesForRangeId(
            RxDocumentServiceRequest request,
            String collectionRid,
            String partitionKeyRangeId,
            boolean forceRefresh) {
        logger.debug("getAddressesForRangeId collectionRid {}, partitionKeyRangeId {}, forceRefresh {}",
            collectionRid, partitionKeyRangeId, forceRefresh);
        Mono<List<Address>> addressResponse = this.getServerAddressesViaGatewayAsync(request, collectionRid, Collections.singletonList(partitionKeyRangeId), forceRefresh);

        Mono<List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>>> addressInfos =
                addressResponse.map(
                        addresses -> {
                            if (logger.isDebugEnabled()) {
                                logger.debug("addresses from getServerAddressesViaGatewayAsync in getAddressesForRangeId {}",
                                    JavaStreamUtils.info(addresses));
                            }
                            return addresses.stream().filter(addressInfo ->
                                    this.protocolScheme.equals(addressInfo.getProtocolScheme()))
                                    .collect(Collectors.groupingBy(
                                            Address::getParitionKeyRangeId))
                                    .values().stream()
                                    .map(groupedAddresses -> toPartitionAddressAndRange(collectionRid, addresses))
                                    .collect(Collectors.toList());
                        });

        Mono<List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>>> result = addressInfos.map(addressInfo -> addressInfo.stream()
                .filter(a ->
                        StringUtils.equals(a.getLeft().getPartitionKeyRangeId(), partitionKeyRangeId))
                .collect(Collectors.toList()));

        return result.flatMap(
                list -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAddressesForRangeId flatMap got result {}", JavaStreamUtils.info(list));
                    }

                    if (list.isEmpty()) {

                        String errorMessage = String.format(
                                RMResources.PartitionKeyRangeNotFound,
                                partitionKeyRangeId,
                                collectionRid);

                        PartitionKeyRangeGoneException e = new PartitionKeyRangeGoneException(errorMessage);
                        BridgeInternal.setResourceAddress(e, collectionRid);

                        return Mono.error(e);
                    } else {
                        return Mono.just(list.get(0).getRight());
                    }
                }).doOnError(e -> {
            logger.debug("getAddressesForRangeId", e);
        });
    }

    public Mono<List<Address>> getMasterAddressesViaGatewayAsync(
            RxDocumentServiceRequest request,
            ResourceType resourceType,
            String resourceAddress,
            String entryUrl,
            boolean forceRefresh,
            boolean useMasterCollectionResolver,
            Map<String, Object> properties) {

        logger.debug("getMasterAddressesViaGatewayAsync " +
                "resourceType {}, " +
                "resourceAddress {}, " +
                "entryUrl {}, " +
                "forceRefresh {}, " +
                "useMasterCollectionResolver {}",
            resourceType,
            resourceAddress,
            entryUrl,
            forceRefresh,
            useMasterCollectionResolver
        );

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(HttpConstants.QueryStrings.URL, HttpUtils.urlEncode(entryUrl));
        HashMap<String, String> headers = new HashMap<>(defaultRequestHeaders);

        if (forceRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_REFRESH, Boolean.TRUE.toString());
        }

        if (useMasterCollectionResolver) {
            headers.put(HttpConstants.HttpHeaders.USE_MASTER_COLLECTION_RESOLVER, Boolean.TRUE.toString());
        }

        queryParameters.put(HttpConstants.QueryStrings.FILTER, HttpUtils.urlEncode(this.protocolFilter));
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        String token = this.tokenProvider.getUserAuthorizationToken(
                resourceAddress,
                resourceType,
                HttpConstants.HttpMethods.GET,
                headers,
                AuthorizationTokenType.PrimaryMasterKey,
                properties);

        headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, HttpUtils.urlEncode(token));
        URL targetEndpoint = Utils.setQuery(this.addressEndpoint.toString(), Utils.createQuery(queryParameters));
        String identifier = logAddressResolutionStart(request, targetEndpoint);

        HttpHeaders defaultHttpHeaders = new HttpHeaders(headers.size());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            defaultHttpHeaders.set(entry.getKey(), entry.getValue());
        }

        HttpRequest httpRequest;
        try {
            httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint.toURI(), targetEndpoint.getPort(), defaultHttpHeaders);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(targetEndpoint.toString(), e);
        }

        Mono<HttpResponse> httpResponseMono = this.httpClient.send(httpRequest);
        Mono<RxDocumentServiceResponse> dsrObs = HttpClientUtils.parseResponseAsync(httpResponseMono, httpRequest);

        return dsrObs.map(
                dsr -> {
                    logAddressResolutionEnd(request, identifier);
                    return dsr.getQueryResponse(Address.class);
                });
    }

    private Pair<PartitionKeyRangeIdentity, AddressInformation[]> toPartitionAddressAndRange(String collectionRid, List<Address> addresses) {
        logger.debug("toPartitionAddressAndRange");
        Address address = addresses.get(0);

        AddressInformation[] addressInfos =
                addresses.stream().map(addr ->
                                GatewayAddressCache.toAddressInformation(addr)
                                      ).collect(Collectors.toList()).toArray(new AddressInformation[addresses.size()]);
        return Pair.of(new PartitionKeyRangeIdentity(collectionRid, address.getParitionKeyRangeId()), addressInfos);
    }

    private static AddressInformation toAddressInformation(Address address) {
        return new AddressInformation(true, address.IsPrimary(), address.getPhyicalUri(), address.getProtocolScheme());
    }

    public Mono<Void> openAsync(
            DocumentCollection collection,
            List<PartitionKeyRangeIdentity> partitionKeyRangeIdentities) {

        if (logger.isDebugEnabled()) {
            logger.debug("openAsync collection: {}, partitionKeyRangeIdentities: {}", collection, JavaStreamUtils.toString(partitionKeyRangeIdentities, ","));
        }

        List<Flux<List<Address>>> tasks = new ArrayList<>();
        int batchSize = GatewayAddressCache.DefaultBatchSize;

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                OperationType.Read,
                //    collection.AltLink,
                collection.resourceId(),
                ResourceType.DocumentCollection,
                //       AuthorizationTokenType.PrimaryMasterKey
                Collections.emptyMap());
        for (int i = 0; i < partitionKeyRangeIdentities.size(); i += batchSize) {

            int endIndex = i + batchSize;
            endIndex = endIndex < partitionKeyRangeIdentities.size()
                    ? endIndex : partitionKeyRangeIdentities.size();

            tasks.add(this.getServerAddressesViaGatewayAsync(
                    request,
                    collection.resourceId(),

                    partitionKeyRangeIdentities.subList(i, endIndex).
                            stream().map(PartitionKeyRangeIdentity::getPartitionKeyRangeId).collect(Collectors.toList()),
                    false).flux());
        }

        return Flux.concat(tasks)
                .doOnNext(list -> {
                    List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>> addressInfos = list.stream()
                            .filter(addressInfo -> this.protocolScheme.equals(addressInfo.getProtocolScheme()))
                            .collect(Collectors.groupingBy(Address::getParitionKeyRangeId))
                            .values().stream().map(addresses -> toPartitionAddressAndRange(collection.resourceId(), addresses))
                            .collect(Collectors.toList());

                    for (Pair<PartitionKeyRangeIdentity, AddressInformation[]> addressInfo : addressInfos) {
                        this.serverPartitionAddressCache.set(
                                new PartitionKeyRangeIdentity(collection.resourceId(), addressInfo.getLeft().getPartitionKeyRangeId()),
                                addressInfo.getRight());
                    }
                }).then();
    }

    private boolean notAllReplicasAvailable(AddressInformation[] addressInformations) {
        return addressInformations.length < ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize;
    }

    private static String logAddressResolutionStart(RxDocumentServiceRequest request, URL targetEndpointUrl) {
        try {
            if (request.requestContext.cosmosResponseDiagnostics != null) {
                return BridgeInternal.recordAddressResolutionStart(request.requestContext.cosmosResponseDiagnostics, targetEndpointUrl.toURI());
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    private static void logAddressResolutionEnd(RxDocumentServiceRequest request, String identifier) {
        if (request.requestContext.cosmosResponseDiagnostics != null) {
            BridgeInternal.recordAddressResolutionEnd(request.requestContext.cosmosResponseDiagnostics, identifier);
        }
    }
}
