// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AuthorizationTokenType;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.JavaStreamUtils;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext.MetadataDiagnostics;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext.MetadataType;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.PathsHelper;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.UnauthorizedException;
import com.azure.cosmos.implementation.UserAgentContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.caches.AsyncCache;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GatewayAddressCache implements IAddressCache {
    private final static Duration minDurationBeforeEnforcingCollectionRoutingMapRefresh = Duration.ofSeconds(30);

    private final static Logger logger = LoggerFactory.getLogger(GatewayAddressCache.class);
    private final static String protocolFilterFormat = "%s eq %s";
    private final static int DefaultBatchSize = 50;

    private final static int DefaultSuboptimalPartitionForceRefreshIntervalInSeconds = 600;
    private final DiagnosticsClientContext clientContext;
    private final ServiceConfig serviceConfig = ServiceConfig.getInstance();

    private final String databaseFeedEntryUrl = PathsHelper.generatePath(ResourceType.Database, "", true);
    private final URI serviceEndpoint;
    private final URI addressEndpoint;

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

    private final ConcurrentHashMap<URI, Set<PartitionKeyRangeIdentity>> serverPartitionAddressToPkRangeIdMap;
    private final boolean tcpConnectionEndpointRediscoveryEnabled;

    private final ConcurrentHashMap<String, ForcedRefreshMetadata> lastForcedRefreshMap;

    public GatewayAddressCache(
            DiagnosticsClientContext clientContext,
            URI serviceEndpoint,
            Protocol protocol,
            IAuthorizationTokenProvider tokenProvider,
            UserAgentContainer userAgent,
            HttpClient httpClient,
            long suboptimalPartitionForceRefreshIntervalInSeconds,
            boolean tcpConnectionEndpointRediscoveryEnabled) {
        this.clientContext = clientContext;
        try {
            this.addressEndpoint = new URL(serviceEndpoint.toURL(), Paths.ADDRESS_PATH_SEGMENT).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
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

        this.serverPartitionAddressToPkRangeIdMap = new ConcurrentHashMap<>();
        this.tcpConnectionEndpointRediscoveryEnabled = tcpConnectionEndpointRediscoveryEnabled;
        this.lastForcedRefreshMap = new ConcurrentHashMap<>();
    }

    public GatewayAddressCache(
            DiagnosticsClientContext clientContext,
            URI serviceEndpoint,
            Protocol protocol,
            IAuthorizationTokenProvider tokenProvider,
            UserAgentContainer userAgent,
            HttpClient httpClient,
            boolean tcpConnectionEndpointRediscoveryEnabled) {
        this(clientContext,
             serviceEndpoint,
             protocol,
             tokenProvider,
             userAgent,
             httpClient,
             DefaultSuboptimalPartitionForceRefreshIntervalInSeconds,
             tcpConnectionEndpointRediscoveryEnabled);
    }

    @Override
    public void updateAddresses(final URI serverKey) {

        Objects.requireNonNull(serverKey, "expected non-null serverKey");

        if (this.tcpConnectionEndpointRediscoveryEnabled) {
            this.serverPartitionAddressToPkRangeIdMap.computeIfPresent(serverKey, (uri, partitionKeyRangeIdentitySet) -> {

                for (PartitionKeyRangeIdentity partitionKeyRangeIdentity : partitionKeyRangeIdentitySet) {
                    if (partitionKeyRangeIdentity.getPartitionKeyRangeId().equals(PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)) {
                        this.masterPartitionAddressCache = null;
                    } else {
                        this.serverPartitionAddressCache.remove(partitionKeyRangeIdentity);
                    }
                }

                return null;
            });
        } else {
            logger.warn("tcpConnectionEndpointRediscovery is not enabled, should not reach here.");
        }

    }

    @Override
    public Mono<Utils.ValueHolder<AddressInformation[]>> tryGetAddresses(RxDocumentServiceRequest request,
                                                                        PartitionKeyRangeIdentity partitionKeyRangeIdentity,
                                                                        boolean forceRefreshPartitionAddresses) {

        Utils.checkNotNullOrThrow(request, "request", "");
        Utils.checkNotNullOrThrow(partitionKeyRangeIdentity, "partitionKeyRangeIdentity", "");

        if (forceRefreshPartitionAddresses) {
            // forceRefreshPartitionAddresses==true indicates we are requesting the latest
            // Replica addresses from the Gateway
            // There are a couple of cases (for example when getting 410/0 after a split happened for the parent
            // partition when just refreshing addresses isn't sufficient (because the Gateway in its cache)
            // might also not know about the partition split that happened
            // to recover from this condition the client would need to either trigger a PKRange cache refresh
            // on the client or force the Gateway CollectionRoutingMap to be refreshed (so that the Gateway gets
            // aware of the split and latest EPK map.
            // Due to the fact that forcing the CollectionRoutingMap to be refreshed in Gateway there is additional
            // load on the ServiceFabric naming service we want to throttle how often we would force the collection
            // routing map refresh
            // These are the throttle conditions: We will only enforce collection routing map to be refreshed
            // - if there has been at least 1 attempt to just refresh replica addresses without forcing collection
            //   routing map refresh on the physical partition before
            // - only one request per Container to force collection routing map refresh is allowed every 30 seconds
            //
            // The throttling logic is implemented in  `ForcedRefreshMetadata.shouldIncludeCollectionRoutingMapRefresh`
            ForcedRefreshMetadata forcedRefreshMetadata = this.lastForcedRefreshMap.computeIfAbsent(
                partitionKeyRangeIdentity.getCollectionRid(),
                (colRid) -> new ForcedRefreshMetadata());

            if (request.forceCollectionRoutingMapRefresh) {
                forcedRefreshMetadata.signalCollectionRoutingMapRefresh(partitionKeyRangeIdentity);
            } else if (forcedRefreshMetadata.shouldIncludeCollectionRoutingMapRefresh(partitionKeyRangeIdentity)) {
                request.forceCollectionRoutingMapRefresh = true;
                forcedRefreshMetadata.signalCollectionRoutingMapRefresh(partitionKeyRangeIdentity);
            } else {
                forcedRefreshMetadata.signalPartitionAddressOnlyRefresh(partitionKeyRangeIdentity);
            }
        } else if (request.forceCollectionRoutingMapRefresh) {
            ForcedRefreshMetadata forcedRefreshMetadata = this.lastForcedRefreshMap.computeIfAbsent(
                partitionKeyRangeIdentity.getCollectionRid(),
                (colRid) -> new ForcedRefreshMetadata());
            forcedRefreshMetadata.signalCollectionRoutingMapRefresh(partitionKeyRangeIdentity);
        }

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
                if (!suboptimalServerPartitionTimestamp.equals(newValue)) {
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
            }).onErrorResume(ex -> {
            Throwable unwrappedException = reactor.core.Exceptions.unwrap(ex);
            CosmosException dce = Utils.as(unwrappedException, CosmosException.class);
            if (dce == null) {
                logger.error("unexpected failure", ex);
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
                    logger.debug("tryGetAddresses: inner onErrorResumeNext return null", dce);
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
        request.setAddressRefresh(true);
        String entryUrl = PathsHelper.generatePath(ResourceType.Document, collectionRid, true);
        HashMap<String, String> addressQuery = new HashMap<>();

        addressQuery.put(HttpConstants.QueryStrings.URL, HttpUtils.urlEncode(entryUrl));

        HashMap<String, String> headers = new HashMap<>(defaultRequestHeaders);
        if (forceRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_REFRESH, "true");
        }

        if(request.forceCollectionRoutingMapRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_COLLECTION_ROUTING_MAP_REFRESH, "true");
        }

        addressQuery.put(HttpConstants.QueryStrings.FILTER, HttpUtils.urlEncode(this.protocolFilter));

        addressQuery.put(HttpConstants.QueryStrings.PARTITION_KEY_RANGE_IDS, String.join(",", partitionKeyRangeIds));
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());

        if (tokenProvider.getAuthorizationTokenType() != AuthorizationTokenType.AadToken) {
            String token = null;
            try {
                token = this.tokenProvider.getUserAuthorizationToken(
                    collectionRid,
                    ResourceType.Document,
                    RequestVerb.GET,
                    headers,
                    AuthorizationTokenType.PrimaryMasterKey,
                    request.properties);
            } catch (UnauthorizedException e) {
                // User doesn't have rid based resource token. Maybe user has name based.

                if (logger.isDebugEnabled()) {
                    logger.debug("User doesn't have resource token for collection rid {}", collectionRid);
                }
            }

            if (token == null && request.getIsNameBased()) {
                // User doesn't have rid based resource token. Maybe user has name based.
                String collectionAltLink = PathsHelper.getCollectionPath(request.getResourceAddress());
                token = this.tokenProvider.getUserAuthorizationToken(
                        collectionAltLink,
                        ResourceType.Document,
                        RequestVerb.GET,
                        headers,
                        AuthorizationTokenType.PrimaryMasterKey,
                        request.properties);
            }

            token = HttpUtils.urlEncode(token);
            headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, token);
        }

        URI targetEndpoint = Utils.setQuery(this.addressEndpoint.toString(), Utils.createQuery(addressQuery));
        String identifier = logAddressResolutionStart(
            request, targetEndpoint, forceRefresh, request.forceCollectionRoutingMapRefresh);

        HttpHeaders httpHeaders = new HttpHeaders(headers);

        Instant addressCallStartTime = Instant.now();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint, targetEndpoint.getPort(), httpHeaders);

        Mono<HttpResponse> httpResponseMono;
        if (tokenProvider.getAuthorizationTokenType() != AuthorizationTokenType.AadToken) {
            httpResponseMono = this.httpClient.send(httpRequest,
                Duration.ofSeconds(Configs.getAddressRefreshResponseTimeoutInSeconds()));
        } else {
            httpResponseMono = tokenProvider
                .populateAuthorizationHeader(httpHeaders)
                .flatMap(valueHttpHeaders -> this.httpClient.send(httpRequest,
                    Duration.ofSeconds(Configs.getAddressRefreshResponseTimeoutInSeconds())));
        }

        Mono<RxDocumentServiceResponse> dsrObs = HttpClientUtils.parseResponseAsync(request, clientContext, httpResponseMono, httpRequest);
        return dsrObs.map(
            dsr -> {
                MetadataDiagnosticsContext metadataDiagnosticsContext =
                    BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);
                if (metadataDiagnosticsContext != null) {
                    Instant addressCallEndTime = Instant.now();
                    MetadataDiagnostics metaDataDiagnostic = new MetadataDiagnostics(addressCallStartTime,
                        addressCallEndTime,
                        MetadataType.SERVER_ADDRESS_LOOKUP);
                    metadataDiagnosticsContext.addMetaDataDiagnostic(metaDataDiagnostic);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("getServerAddressesViaGatewayAsync deserializes result");
                }
                logAddressResolutionEnd(request, identifier, null);
                return dsr.getQueryResponse(Address.class);
            }).onErrorResume(throwable -> {
            Throwable unwrappedException = reactor.core.Exceptions.unwrap(throwable);
            logAddressResolutionEnd(request, identifier, unwrappedException.toString());
            if (!(unwrappedException instanceof Exception)) {
                // fatal error
                logger.error("Unexpected failure {}", unwrappedException.getMessage(), unwrappedException);
                return Mono.error(unwrappedException);
            }

            Exception exception = (Exception) unwrappedException;
            CosmosException dce;
            if (!(exception instanceof CosmosException)) {
                // wrap in CosmosException
                logger.error("Network failure", exception);
                dce = BridgeInternal.createCosmosException(request.requestContext.resourcePhysicalAddress, 0, exception);
                BridgeInternal.setRequestHeaders(dce, request.getHeaders());
            } else {
                dce = (CosmosException) exception;
            }

            if (WebExceptionUtility.isNetworkFailure(dce)) {
                if (WebExceptionUtility.isReadTimeoutException(dce)) {
                    BridgeInternal.setSubStatusCode(dce, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
                } else {
                    BridgeInternal.setSubStatusCode(dce, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);
                }
            }

            if (request.requestContext.cosmosDiagnostics != null) {
                BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, null,
                    dce);
                BridgeInternal.setCosmosDiagnostics(dce,
                    request.requestContext.cosmosDiagnostics);
            }

            return Mono.error(dce);
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
        request.setAddressRefresh(true);
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(HttpConstants.QueryStrings.URL, HttpUtils.urlEncode(entryUrl));
        HashMap<String, String> headers = new HashMap<>(defaultRequestHeaders);

        if (forceRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_REFRESH, "true");
        }

        if (useMasterCollectionResolver) {
            headers.put(HttpConstants.HttpHeaders.USE_MASTER_COLLECTION_RESOLVER, "true");
        }

        if(request.forceCollectionRoutingMapRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_COLLECTION_ROUTING_MAP_REFRESH, "true");
        }

        queryParameters.put(HttpConstants.QueryStrings.FILTER, HttpUtils.urlEncode(this.protocolFilter));
        headers.put(HttpConstants.HttpHeaders.X_DATE, Utils.nowAsRFC1123());

        if (tokenProvider.getAuthorizationTokenType() != AuthorizationTokenType.AadToken) {
            String token = this.tokenProvider.getUserAuthorizationToken(
                    resourceAddress,
                    resourceType,
                    RequestVerb.GET,
                    headers,
                    AuthorizationTokenType.PrimaryMasterKey,
                    properties);

            headers.put(HttpConstants.HttpHeaders.AUTHORIZATION, HttpUtils.urlEncode(token));
        }

        URI targetEndpoint = Utils.setQuery(this.addressEndpoint.toString(), Utils.createQuery(queryParameters));
        String identifier = logAddressResolutionStart(
            request, targetEndpoint, true, true);

        HttpHeaders defaultHttpHeaders = new HttpHeaders(headers);
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint, targetEndpoint.getPort(), defaultHttpHeaders);
        Instant addressCallStartTime = Instant.now();
        Mono<HttpResponse> httpResponseMono;

        if (tokenProvider.getAuthorizationTokenType() != AuthorizationTokenType.AadToken) {
            httpResponseMono = this.httpClient.send(httpRequest,
                Duration.ofSeconds(Configs.getAddressRefreshResponseTimeoutInSeconds()));
        } else {
            httpResponseMono = tokenProvider
                .populateAuthorizationHeader(defaultHttpHeaders)
                .flatMap(valueHttpHeaders -> this.httpClient.send(httpRequest,
                    Duration.ofSeconds(Configs.getAddressRefreshResponseTimeoutInSeconds())));
        }

        Mono<RxDocumentServiceResponse> dsrObs = HttpClientUtils.parseResponseAsync(request, this.clientContext, httpResponseMono, httpRequest);

        return dsrObs.map(
            dsr -> {
                MetadataDiagnosticsContext metadataDiagnosticsContext =
                    BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);
                if (metadataDiagnosticsContext != null) {
                    Instant addressCallEndTime = Instant.now();
                    MetadataDiagnostics metaDataDiagnostic = new MetadataDiagnostics(addressCallStartTime,
                        addressCallEndTime,
                        MetadataType.MASTER_ADDRESS_LOOK_UP);
                    metadataDiagnosticsContext.addMetaDataDiagnostic(metaDataDiagnostic);
                }

                logAddressResolutionEnd(request, identifier, null);
                return dsr.getQueryResponse(Address.class);
            }).onErrorResume(throwable -> {
            Throwable unwrappedException = reactor.core.Exceptions.unwrap(throwable);
            logAddressResolutionEnd(request, identifier, unwrappedException.toString());
            if (!(unwrappedException instanceof Exception)) {
                // fatal error
                logger.error("Unexpected failure {}", unwrappedException.getMessage(), unwrappedException);
                return Mono.error(unwrappedException);
            }

            Exception exception = (Exception) unwrappedException;
            CosmosException dce;
            if (!(exception instanceof CosmosException)) {
                // wrap in CosmosException
                logger.error("Network failure", exception);
                dce = BridgeInternal.createCosmosException(request.requestContext.resourcePhysicalAddress, 0, exception);
                BridgeInternal.setRequestHeaders(dce, request.getHeaders());
            } else {
                dce = (CosmosException) exception;
            }

            if (WebExceptionUtility.isNetworkFailure(dce)) {
                if (WebExceptionUtility.isReadTimeoutException(dce)) {
                    BridgeInternal.setSubStatusCode(dce, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_READ_TIMEOUT);
                } else {
                    BridgeInternal.setSubStatusCode(dce, HttpConstants.SubStatusCodes.GATEWAY_ENDPOINT_UNAVAILABLE);
                }
            }

            if (request.requestContext.cosmosDiagnostics != null) {
                BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, null,
                    dce);
                BridgeInternal.setCosmosDiagnostics(dce,
                    request.requestContext.cosmosDiagnostics);
            }

            return Mono.error(dce);
        });
    }

    private Pair<PartitionKeyRangeIdentity, AddressInformation[]> toPartitionAddressAndRange(String collectionRid, List<Address> addresses) {
        if (logger.isDebugEnabled()) {
            logger.debug("toPartitionAddressAndRange");
        }

        Address address = addresses.get(0);
        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, address.getParitionKeyRangeId());

        AddressInformation[] addressInfos =
                addresses.stream().map(addr ->
                                GatewayAddressCache.toAddressInformation(addr)
                                      ).collect(Collectors.toList()).toArray(new AddressInformation[addresses.size()]);

        if (this.tcpConnectionEndpointRediscoveryEnabled) {
            for (AddressInformation addressInfo : addressInfos) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Added address to serverPartitionAddressToPkRangeIdMap: ({\"partitionKeyRangeIdentity\":{},\"address\":{}})",
                        partitionKeyRangeIdentity,
                        addressInfo);
                }

                this.serverPartitionAddressToPkRangeIdMap.compute(addressInfo.getServerKey(), (serverKey, partitionKeyRangeIdentitySet) -> {
                    if (partitionKeyRangeIdentitySet == null) {
                        partitionKeyRangeIdentitySet = ConcurrentHashMap.newKeySet();
                    }

                    partitionKeyRangeIdentitySet.add(partitionKeyRangeIdentity);
                    return partitionKeyRangeIdentitySet;
                });
            }
        }

        return Pair.of(partitionKeyRangeIdentity, addressInfos);
    }

    private static AddressInformation toAddressInformation(Address address) {
        return new AddressInformation(true, address.isPrimary(), address.getPhyicalUri(), address.getProtocolScheme());
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
                this.clientContext,
                OperationType.Read,
                //    collection.AltLink,
                collection.getResourceId(),
                ResourceType.DocumentCollection,
                //       AuthorizationTokenType.PrimaryMasterKey
                Collections.emptyMap());
        for (int i = 0; i < partitionKeyRangeIdentities.size(); i += batchSize) {

            int endIndex = i + batchSize;
            endIndex = endIndex < partitionKeyRangeIdentities.size()
                    ? endIndex : partitionKeyRangeIdentities.size();

            tasks.add(this.getServerAddressesViaGatewayAsync(
                    request,
                    collection.getResourceId(),

                    partitionKeyRangeIdentities.subList(i, endIndex).
                            stream().map(PartitionKeyRangeIdentity::getPartitionKeyRangeId).collect(Collectors.toList()),
                    false).flux());
        }

        return Flux.concat(tasks)
                .doOnNext(list -> {
                    List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>> addressInfos = list.stream()
                            .filter(addressInfo -> this.protocolScheme.equals(addressInfo.getProtocolScheme()))
                            .collect(Collectors.groupingBy(Address::getParitionKeyRangeId))
                            .values().stream().map(addresses -> toPartitionAddressAndRange(collection.getResourceId(), addresses))
                            .collect(Collectors.toList());

                    for (Pair<PartitionKeyRangeIdentity, AddressInformation[]> addressInfo : addressInfos) {
                        this.serverPartitionAddressCache.set(
                                new PartitionKeyRangeIdentity(collection.getResourceId(), addressInfo.getLeft().getPartitionKeyRangeId()),
                                addressInfo.getRight());
                    }
                }).then();
    }

    private boolean notAllReplicasAvailable(AddressInformation[] addressInformations) {
        return addressInformations.length < ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize;
    }

    private static String logAddressResolutionStart(
        RxDocumentServiceRequest request,
        URI targetEndpointUrl,
        boolean forceRefresh,
        boolean forceCollectionRoutingMapRefresh) {
        if (request.requestContext.cosmosDiagnostics != null) {
            return BridgeInternal.recordAddressResolutionStart(
                request.requestContext.cosmosDiagnostics,
                targetEndpointUrl,
                forceRefresh,
                forceCollectionRoutingMapRefresh);
        }

        return null;
    }

    private static void logAddressResolutionEnd(RxDocumentServiceRequest request, String identifier, String errorMessage) {
        if (request.requestContext.cosmosDiagnostics != null) {
            BridgeInternal.recordAddressResolutionEnd(request.requestContext.cosmosDiagnostics, identifier, errorMessage);
        }
    }

    private static class ForcedRefreshMetadata {
        private final ConcurrentHashMap<PartitionKeyRangeIdentity, Instant> lastPartitionAddressOnlyRefresh;
        private Instant lastCollectionRoutingMapRefresh;

        public ForcedRefreshMetadata() {
            lastPartitionAddressOnlyRefresh = new ConcurrentHashMap<>();
            lastCollectionRoutingMapRefresh = Instant.now();
        }

        public void signalCollectionRoutingMapRefresh(PartitionKeyRangeIdentity pk) {
            Instant nowSnapshot = Instant.now();
            lastPartitionAddressOnlyRefresh.put(pk, nowSnapshot);
            lastCollectionRoutingMapRefresh = nowSnapshot;
        }

        public void signalPartitionAddressOnlyRefresh(PartitionKeyRangeIdentity pk) {
            lastPartitionAddressOnlyRefresh.put(pk, Instant.now());
        }

        public boolean shouldIncludeCollectionRoutingMapRefresh(PartitionKeyRangeIdentity pk) {
            Instant lastPartitionAddressRefreshSnapshot = lastPartitionAddressOnlyRefresh.get(pk);
            Instant lastCollectionRoutingMapRefreshSnapshot = lastCollectionRoutingMapRefresh;

            if (lastPartitionAddressRefreshSnapshot == null ||
                !lastPartitionAddressRefreshSnapshot.isAfter(lastCollectionRoutingMapRefreshSnapshot)) {
                // Enforce that at least one refresh attempt is made without
                // forcing collection routing map refresh as well
                return false;
            }

            Duration durationSinceLastForcedCollectionRoutingMapRefresh =
                Duration.between(lastCollectionRoutingMapRefreshSnapshot, Instant.now());
            boolean returnValue = durationSinceLastForcedCollectionRoutingMapRefresh
                .compareTo(minDurationBeforeEnforcingCollectionRoutingMapRefresh) >= 0;

            return returnValue;
        }
    }
}
