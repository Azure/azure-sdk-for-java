// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ApiType;
import com.azure.cosmos.implementation.AuthorizationTokenType;
import com.azure.cosmos.implementation.BackoffRetryUtility;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.Exceptions;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.IAuthorizationTokenProvider;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JavaStreamUtils;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext.MetadataDiagnostics;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext.MetadataType;
import com.azure.cosmos.implementation.MetadataRequestRetryPolicy;
import com.azure.cosmos.implementation.OpenConnectionResponse;
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
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.implementation.caches.AsyncCacheNonBlocking;
import com.azure.cosmos.implementation.directconnectivity.rntbd.OpenConnectionTask;
import com.azure.cosmos.implementation.directconnectivity.rntbd.ProactiveOpenConnectionsProcessor;
import com.azure.cosmos.implementation.faultinjection.GatewayServerErrorInjector;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.http.HttpTimeoutPolicy;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GatewayAddressCache implements IAddressCache {
    private static Duration minDurationBeforeEnforcingCollectionRoutingMapRefresh = Duration.ofSeconds(30);

    private final static Logger logger = LoggerFactory.getLogger(GatewayAddressCache.class);
    private final static String protocolFilterFormat = "%s eq %s";
    private final static int DefaultBatchSize = 50;

    private final static int DefaultSuboptimalPartitionForceRefreshIntervalInSeconds = 600;
    private final DiagnosticsClientContext clientContext;

    private final String databaseFeedEntryUrl = PathsHelper.generatePath(ResourceType.Database, "", true);
    private final URI addressEndpoint;
    private final URI serviceEndpoint;

    private final AsyncCacheNonBlocking<PartitionKeyRangeIdentity, AddressInformation[]> serverPartitionAddressCache;
    private final ConcurrentHashMap<PartitionKeyRangeIdentity, Instant> suboptimalServerPartitionTimestamps;
    private final long suboptimalPartitionForceRefreshIntervalInSeconds;

    private final String protocolScheme;
    private final String protocolFilter;
    private final IAuthorizationTokenProvider tokenProvider;
    private final HashMap<String, String> defaultRequestHeaders;
    private final HttpClient httpClient;

    private volatile Pair<PartitionKeyRangeIdentity, AddressInformation[]> masterPartitionAddressCache;
    private volatile Instant suboptimalMasterPartitionTimestamp;

    private final ConcurrentHashMap<String, ForcedRefreshMetadata> lastForcedRefreshMap;
    private final GlobalEndpointManager globalEndpointManager;
    private ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor;
    private final ConnectionPolicy connectionPolicy;
    private final boolean replicaAddressValidationEnabled;
    private final Set<Uri.HealthStatus> replicaValidationScopes;
    private GatewayServerErrorInjector gatewayServerErrorInjector;

    public GatewayAddressCache(
        DiagnosticsClientContext clientContext,
        URI serviceEndpoint,
        Protocol protocol,
        IAuthorizationTokenProvider tokenProvider,
        UserAgentContainer userAgent,
        HttpClient httpClient,
        long suboptimalPartitionForceRefreshIntervalInSeconds,
        ApiType apiType,
        GlobalEndpointManager globalEndpointManager,
        ConnectionPolicy connectionPolicy,
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor,
        GatewayServerErrorInjector gatewayServerErrorInjector) {

        this.clientContext = clientContext;
        try {
            this.addressEndpoint = new URL(serviceEndpoint.toURL(), Paths.ADDRESS_PATH_SEGMENT).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            logger.error("serviceEndpoint {} is invalid", serviceEndpoint, e);
            assert false;
            throw new IllegalStateException(e);
        }
        this.serviceEndpoint = serviceEndpoint;
        this.tokenProvider = tokenProvider;
        this.serverPartitionAddressCache = new AsyncCacheNonBlocking<>();
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

        if(apiType != null) {
            defaultRequestHeaders.put(HttpConstants.HttpHeaders.API_TYPE, apiType.toString());
        }

        // Set requested API version header for version enforcement.
        defaultRequestHeaders.put(HttpConstants.HttpHeaders.VERSION, HttpConstants.Versions.CURRENT_VERSION);
        this.defaultRequestHeaders.put(
            HttpConstants.HttpHeaders.SDK_SUPPORTED_CAPABILITIES,
            HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES);

        this.lastForcedRefreshMap = new ConcurrentHashMap<>();
        this.globalEndpointManager = globalEndpointManager;
        this.proactiveOpenConnectionsProcessor = proactiveOpenConnectionsProcessor;
        this.connectionPolicy = connectionPolicy;
        this.replicaAddressValidationEnabled = Configs.isReplicaAddressValidationEnabled();
        this.replicaValidationScopes = ConcurrentHashMap.newKeySet();
        if (this.replicaAddressValidationEnabled) {
            this.replicaValidationScopes.add(Uri.HealthStatus.UnhealthyPending);
        }
        this.gatewayServerErrorInjector = gatewayServerErrorInjector;
    }

    public GatewayAddressCache(
        DiagnosticsClientContext clientContext,
        URI serviceEndpoint,
        Protocol protocol,
        IAuthorizationTokenProvider tokenProvider,
        UserAgentContainer userAgent,
        HttpClient httpClient,
        ApiType apiType,
        GlobalEndpointManager globalEndpointManager,
        ConnectionPolicy connectionPolicy,
        ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor,
        GatewayServerErrorInjector gatewayServerErrorInjector) {
        this(clientContext,
                serviceEndpoint,
                protocol,
                tokenProvider,
                userAgent,
                httpClient,
                DefaultSuboptimalPartitionForceRefreshIntervalInSeconds,
                apiType,
                globalEndpointManager,
                connectionPolicy,
                proactiveOpenConnectionsProcessor,
                gatewayServerErrorInjector);
    }

    @Override
    public Mono<Utils.ValueHolder<AddressInformation[]>> tryGetAddresses(RxDocumentServiceRequest request,
                                                                         PartitionKeyRangeIdentity partitionKeyRangeIdentity,
                                                                         boolean forceRefreshPartitionAddresses) {

        Utils.checkNotNullOrThrow(request, "request", "");
        Utils.checkNotNullOrThrow(partitionKeyRangeIdentity, "partitionKeyRangeIdentity", "");

        logger.debug("PartitionKeyRangeIdentity {}, forceRefreshPartitionAddresses {}",
            partitionKeyRangeIdentity,
            forceRefreshPartitionAddresses);
        if (StringUtils.equals(partitionKeyRangeIdentity.getPartitionKeyRangeId(),
            PartitionKeyRange.MASTER_PARTITION_KEY_RANGE_ID)) {

            // if that's master partition return master partition address!
            return this.resolveMasterAsync(request, forceRefreshPartitionAddresses, request.properties)
                       .map(partitionKeyRangeIdentityPair -> new Utils.ValueHolder<>(partitionKeyRangeIdentityPair.getRight()));
        }

        evaluateCollectionRoutingMapRefreshForServerPartition(
            request, partitionKeyRangeIdentity, forceRefreshPartitionAddresses);

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
            this.suboptimalServerPartitionTimestamps.remove(partitionKeyRangeIdentity);
        }

        Mono<Utils.ValueHolder<AddressInformation[]>> addressesObs =
                this.serverPartitionAddressCache
                    .getAsync(
                        partitionKeyRangeIdentity,
                        cachedAddresses -> this.getAddressesForRangeId(
                            request,
                            partitionKeyRangeIdentity,
                            forceRefreshPartitionAddressesModified,
                            cachedAddresses),
                        cachedAddresses -> {
                            for (Uri failedEndpoints : request.requestContext.getFailedEndpoints()) {
                                failedEndpoints.setUnhealthy();
                            }
                            return forceRefreshPartitionAddressesModified;
                        })
                    .map(Utils.ValueHolder::new);

        return addressesObs
                .map(addressesValueHolder -> {
                    if (notAllReplicasAvailable(addressesValueHolder.v)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("not all replicas available {}", JavaStreamUtils.info(addressesValueHolder.v));
                        }
                        this.suboptimalServerPartitionTimestamps.putIfAbsent(partitionKeyRangeIdentity, Instant.now());
                    }

                    // Refresh the cache if there was an address has been marked as unhealthy long enough and need to revalidate its status
                    // If you are curious about why we do not depend on 410 to force refresh the addresses, the reason being:
                    // When an address is marked as unhealthy, then the address enumerator will move it to the end of the list
                    // So it could happen that no request will use the unhealthy address for an extended period of time
                    // So the 410 -> forceRefresh workflow may not happen
                    if (Arrays
                            .stream(addressesValueHolder.v)
                            .anyMatch(addressInformation -> addressInformation.getPhysicalUri().shouldRefreshHealthStatus())) {
                        logger.debug("refresh cache due to address uri in unhealthy status for pkRangeId {}", partitionKeyRangeIdentity.getPartitionKeyRangeId());
                        this.serverPartitionAddressCache.refresh(
                                partitionKeyRangeIdentity,
                                cachedAddresses -> this.getAddressesForRangeId(request, partitionKeyRangeIdentity, true, cachedAddresses));
                    }

                    return addressesValueHolder;
                })
                .onErrorResume(ex -> {
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
                        if (Exceptions.isNotFound(dce) ||
                            Exceptions.isGone(dce) ||
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

    @Override
    public void setOpenConnectionsProcessor(ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor) {
        this.proactiveOpenConnectionsProcessor = proactiveOpenConnectionsProcessor;
    }

    public void setGatewayServerErrorInjector(GatewayServerErrorInjector gatewayServerErrorInjector) {
        this.gatewayServerErrorInjector = gatewayServerErrorInjector;
    }

    public Mono<List<Address>> getServerAddressesViaGatewayAsync(
        RxDocumentServiceRequest request,
        String collectionRid,
        List<String> partitionKeyRangeIds,
        boolean forceRefresh) {

        request.setAddressRefresh(true, forceRefresh);
        MetadataRequestRetryPolicy metadataRequestRetryPolicy = new MetadataRequestRetryPolicy(globalEndpointManager);
        metadataRequestRetryPolicy.onBeforeSendRequest(request);

        return BackoffRetryUtility.executeRetry(() -> this.getServerAddressesViaGatewayInternalAsync(
            request, collectionRid, partitionKeyRangeIds, forceRefresh), metadataRequestRetryPolicy);
    }

    private Mono<List<Address>> getServerAddressesViaGatewayInternalAsync(RxDocumentServiceRequest request,
                                                                          String collectionRid,
                                                                          List<String> partitionKeyRangeIds,
                                                                          boolean forceRefresh) {
        if (logger.isDebugEnabled()) {
            logger.debug("getServerAddressesViaGatewayAsync collectionRid {}, partitionKeyRangeIds {}", collectionRid,
                JavaStreamUtils.toString(partitionKeyRangeIds, ","));
        }

        // track address refresh has happened, this is only meant to be used for fault injection validation
        request.faultInjectionRequestContext.recordAddressForceRefreshed(forceRefresh);

        String entryUrl = PathsHelper.generatePath(ResourceType.Document, collectionRid, true);
        HashMap<String, String> addressQuery = new HashMap<>();

        addressQuery.put(HttpConstants.QueryStrings.URL, HttpUtils.urlEncode(entryUrl));

        HashMap<String, String> headers = new HashMap<>(defaultRequestHeaders);
        if (forceRefresh) {
            headers.put(HttpConstants.HttpHeaders.FORCE_REFRESH, "true");
        }

        if (request.forceCollectionRoutingMapRefresh) {
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
        headers.put(HttpConstants.HttpHeaders.ACTIVITY_ID, identifier);

        HttpHeaders httpHeaders = new HttpHeaders(headers);

        Instant addressCallStartTime = Instant.now();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint, targetEndpoint.getPort(), httpHeaders);

        Mono<HttpResponse> httpResponseMono;
        if (tokenProvider.getAuthorizationTokenType() != AuthorizationTokenType.AadToken) {
            httpResponseMono = this.httpClient.send(httpRequest, request.getResponseTimeout());
        } else {
            httpResponseMono = tokenProvider
                .populateAuthorizationHeader(httpHeaders)
                .flatMap(valueHttpHeaders -> this.httpClient.send(httpRequest,request.getResponseTimeout()));
        }

        if (this.gatewayServerErrorInjector != null) {
            httpResponseMono =
                this.gatewayServerErrorInjector.injectGatewayErrors(
                    request.getResponseTimeout(),
                    httpRequest,
                    request,
                    httpResponseMono,
                    partitionKeyRangeIds);
        }

        Mono<RxDocumentServiceResponse> dsrObs = HttpClientUtils.parseResponseAsync(request, clientContext, httpResponseMono);
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
                logAddressResolutionEnd(
                    request,
                    identifier,
                    null,
                    httpRequest.reactorNettyRequestRecord().getTransportRequestId());

                return dsr.getQueryResponse(null, Address.class);
            }).onErrorResume(throwable -> {
            Throwable unwrappedException = reactor.core.Exceptions.unwrap(throwable);
            logAddressResolutionEnd(
                request,
                identifier,
                unwrappedException.toString(),
                httpRequest.reactorNettyRequestRecord().getTransportRequestId());

            if (!(unwrappedException instanceof Exception)) {
                // fatal error
                logger.error("Unexpected failure {}", unwrappedException.getMessage(), unwrappedException);
                return Mono.error(unwrappedException);
            }

            Exception exception = (Exception) unwrappedException;
            CosmosException dce;
            if (!(exception instanceof CosmosException)) {
                // wrap in CosmosException
                logger.warn("Network failure", exception);
                int statusCode = 0;
                if (WebExceptionUtility.isNetworkFailure(exception)) {
                    if (WebExceptionUtility.isReadTimeoutException(exception)) {
                        statusCode = HttpConstants.StatusCodes.REQUEST_TIMEOUT;
                    } else {
                        statusCode = HttpConstants.StatusCodes.SERVICE_UNAVAILABLE;
                    }
                }

                dce = BridgeInternal.createCosmosException(
                    request.requestContext.resourcePhysicalAddress, statusCode, exception);
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
                BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, dce, this.globalEndpointManager);
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

    private void evaluateCollectionRoutingMapRefreshForServerPartition(
        RxDocumentServiceRequest request,
        PartitionKeyRangeIdentity pkRangeIdentity,
        boolean forceRefreshPartitionAddresses) {

        Utils.checkNotNullOrThrow(request, "request", "");
        validatePkRangeIdentity(pkRangeIdentity);

        String collectionRid = pkRangeIdentity.getCollectionRid();
        String partitionKeyRangeId = pkRangeIdentity.getPartitionKeyRangeId();

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
                collectionRid,
                (colRid) -> new ForcedRefreshMetadata());

            if (request.forceCollectionRoutingMapRefresh) {
                forcedRefreshMetadata.signalCollectionRoutingMapRefresh(
                    pkRangeIdentity,
                    true);
            } else if (forcedRefreshMetadata.shouldIncludeCollectionRoutingMapRefresh(pkRangeIdentity)) {
                request.forceCollectionRoutingMapRefresh = true;
                forcedRefreshMetadata.signalCollectionRoutingMapRefresh(
                    pkRangeIdentity,
                    true);
            } else {
                forcedRefreshMetadata.signalPartitionAddressOnlyRefresh(pkRangeIdentity);
            }
        } else if (request.forceCollectionRoutingMapRefresh) {
            ForcedRefreshMetadata forcedRefreshMetadata = this.lastForcedRefreshMap.computeIfAbsent(
                collectionRid,
                (colRid) -> new ForcedRefreshMetadata());
            forcedRefreshMetadata.signalCollectionRoutingMapRefresh(
                pkRangeIdentity,
                false);
        }

        logger.debug("evaluateCollectionRoutingMapRefreshForServerPartition collectionRid {}, partitionKeyRangeId {},"
                + " " +
                "forceRefreshPartitionAddresses {}, forceCollectionRoutingMapRefresh {}",
            collectionRid,
            partitionKeyRangeId,
            forceRefreshPartitionAddresses,
            request.forceCollectionRoutingMapRefresh);
    }

    private void validatePkRangeIdentity(PartitionKeyRangeIdentity pkRangeIdentity) {

        Utils.checkNotNullOrThrow(pkRangeIdentity, "pkRangeId", "");
        Utils.checkNotNullOrThrow(
            pkRangeIdentity.getCollectionRid(),
            "pkRangeId.getCollectionRid()",
            "");
        Utils.checkNotNullOrThrow(
            pkRangeIdentity.getPartitionKeyRangeId(),
            "pkRangeId.getPartitionKeyRangeId()",
            "");
    }

    private Mono<AddressInformation[]> getAddressesForRangeId(
        RxDocumentServiceRequest request,
        PartitionKeyRangeIdentity pkRangeIdentity,
        boolean forceRefresh,
        AddressInformation[] cachedAddresses) {

        Utils.checkNotNullOrThrow(request, "request", "");
        validatePkRangeIdentity(pkRangeIdentity);

        String collectionRid = pkRangeIdentity.getCollectionRid();
        String partitionKeyRangeId = pkRangeIdentity.getPartitionKeyRangeId();

        logger.debug(
                "getAddressesForRangeId collectionRid {}, partitionKeyRangeId {}, forceRefresh {}",
                collectionRid,
                partitionKeyRangeId,
                forceRefresh);

        Mono<List<Address>> addressResponse = this.getServerAddressesViaGatewayAsync(request, collectionRid, Collections.singletonList(partitionKeyRangeId), forceRefresh);

        Mono<List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>>> addressInfos =
            addressResponse.map(
                addresses -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("addresses from getServerAddressesViaGatewayAsync in getAddressesForRangeId {}",
                            JavaStreamUtils.info(addresses));
                    }
                    return addresses
                            .stream()
                            .filter(addressInfo -> this.protocolScheme.equals(addressInfo.getProtocolScheme()))
                            .collect(Collectors.groupingBy(Address::getParitionKeyRangeId))
                            .values()
                            .stream()
                            .map(groupedAddresses -> {
                                Pair<PartitionKeyRangeIdentity, AddressInformation[]> pkrIdToAddressInfos =
                                        toPartitionAddressAndRange(collectionRid, addresses);

                                // refresh / record new addresses in proactiveOpenConnectionsProcessor
                                // when forceRefresh is true
                                if (forceRefresh) {
                                    refreshCollectionRidAndAddressUrisUnderOpenConnectionsAndInitCaches(
                                            collectionRid, pkrIdToAddressInfos.getRight());
                                }

                                return pkrIdToAddressInfos;
                            })
                            .collect(Collectors.toList())
                            ;
                });

        Mono<List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>>> result =
                addressInfos
                    .map(addressInfo -> addressInfo.stream()
                    .filter(a -> StringUtils.equals(a.getLeft().getPartitionKeyRangeId(), partitionKeyRangeId))
                    .collect(Collectors.toList()));

        return result
                .flatMap(
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
                            // merge with the cached addresses
                            // if the address is being returned from gateway again, then keep using the cached addressInformation object
                            // for new addresses, use the new addressInformation object
                            AddressInformation[] mergedAddresses = this.mergeAddresses(list.get(0).getRight(), cachedAddresses);
                            for (AddressInformation address : mergedAddresses) {
                                // The main purpose for this step is to move address health status from unhealthy -> unhealthyPending
                                address.getPhysicalUri().setRefreshed();
                            }

                            if (this.replicaAddressValidationEnabled) {
                                this.validateReplicaAddresses(collectionRid, mergedAddresses);
                            }

                            return Mono.just(mergedAddresses);
                        }
                    })
                .doOnError(e -> logger.debug("getAddressesForRangeId", e));
    }

    public Mono<List<Address>> getMasterAddressesViaGatewayAsync(
        RxDocumentServiceRequest request,
        ResourceType resourceType,
        String resourceAddress,
        String entryUrl,
        boolean forceRefresh,
        boolean useMasterCollectionResolver,
        Map<String, Object> properties) {
        request.setAddressRefresh(true, forceRefresh);
        MetadataRequestRetryPolicy metadataRequestRetryPolicy = new MetadataRequestRetryPolicy(globalEndpointManager);
        metadataRequestRetryPolicy.onBeforeSendRequest(request);

        return BackoffRetryUtility.executeRetry(() -> this.getMasterAddressesViaGatewayAsyncInternal(
            request, resourceType, resourceAddress, entryUrl, forceRefresh, useMasterCollectionResolver, properties), metadataRequestRetryPolicy);
    }

    private Mono<List<Address>> getMasterAddressesViaGatewayAsyncInternal(
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
        headers.put(HttpConstants.HttpHeaders.ACTIVITY_ID, identifier);

        HttpHeaders defaultHttpHeaders = new HttpHeaders(headers);
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, targetEndpoint, targetEndpoint.getPort(), defaultHttpHeaders);
        Instant addressCallStartTime = Instant.now();
        Mono<HttpResponse> httpResponseMono;

        if (tokenProvider.getAuthorizationTokenType() != AuthorizationTokenType.AadToken) {
            httpResponseMono = this.httpClient.send(httpRequest,
                request.getResponseTimeout());
        } else {
            httpResponseMono = tokenProvider
                .populateAuthorizationHeader(defaultHttpHeaders)
                .flatMap(valueHttpHeaders -> this.httpClient.send(httpRequest,
                    request.getResponseTimeout()));
        }

        Mono<RxDocumentServiceResponse> dsrObs = HttpClientUtils.parseResponseAsync(request, this.clientContext, httpResponseMono);

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

                logAddressResolutionEnd(
                    request,
                    identifier,
                    null,
                    httpRequest.reactorNettyRequestRecord().getTransportRequestId());

                return dsr.getQueryResponse(null, Address.class);
            }).onErrorResume(throwable -> {
            Throwable unwrappedException = reactor.core.Exceptions.unwrap(throwable);
            logAddressResolutionEnd(
                request,
                identifier,
                unwrappedException.toString(),
                httpRequest.reactorNettyRequestRecord().getTransportRequestId());

            if (!(unwrappedException instanceof Exception)) {
                // fatal error
                logger.error("Unexpected failure {}", unwrappedException.getMessage(), unwrappedException);
                return Mono.error(unwrappedException);
            }

            Exception exception = (Exception) unwrappedException;
            CosmosException dce;
            if (!(exception instanceof CosmosException)) {
                // wrap in CosmosException
                logger.warn("Network failure", exception);
                int statusCode = 0;
                if (WebExceptionUtility.isNetworkFailure(exception)) {
                    if (WebExceptionUtility.isReadTimeoutException(exception)) {
                        statusCode = HttpConstants.StatusCodes.REQUEST_TIMEOUT;
                    } else {
                        statusCode = HttpConstants.StatusCodes.SERVICE_UNAVAILABLE;
                    }
                }

                dce = BridgeInternal.createCosmosException(
                    request.requestContext.resourcePhysicalAddress, statusCode, exception);
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
                BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, dce, this.globalEndpointManager);
            }

            return Mono.error(dce);
        });
    }

    /***
     *  merge the new addresses get back from gateway with the cached addresses.
     *  If the address is being returned from gateway again, then keep using the cached addressInformation object
     *  If it is a new address being returned, then use the new addressInformation object.
     *
     * @param newAddresses the latest addresses being returned from gateway.
     * @param cachedAddresses the cached addresses.
     *
     * @return the merged addresses.
     */
    private AddressInformation[] mergeAddresses(AddressInformation[] newAddresses, AddressInformation[] cachedAddresses) {
        checkNotNull(newAddresses, "Argument 'newAddresses' should not be null");

        if (cachedAddresses == null) {
            return newAddresses;
        }

        List<AddressInformation> mergedAddresses = new ArrayList<>();
        Map<Uri, List<AddressInformation>> cachedAddressMap =
                Arrays
                    .stream(cachedAddresses)
                    .collect(Collectors.groupingBy(AddressInformation::getPhysicalUri));

        for (AddressInformation newAddress : newAddresses) {
            boolean useCachedAddress = false;
            if (cachedAddressMap.containsKey(newAddress.getPhysicalUri())) {
                for (AddressInformation cachedAddress : cachedAddressMap.get(newAddress.getPhysicalUri())) {
                    if (newAddress.getProtocol() == cachedAddress.getProtocol()
                            && newAddress.isPublic() == cachedAddress.isPublic()
                            && newAddress.isPrimary() == cachedAddress.isPrimary()) {

                        useCachedAddress = true;
                        mergedAddresses.add(cachedAddress);
                        break;
                    }
                }
            }

            if (!useCachedAddress) {
                mergedAddresses.add(newAddress);
            }
        }

        return mergedAddresses.toArray(new AddressInformation[mergedAddresses.size()]);
    }

    private void validateReplicaAddresses(String collectionRid, AddressInformation[] addresses) {
        checkNotNull(addresses, "Argument 'addresses' can not be null");
        checkArgument(StringUtils.isNotEmpty(collectionRid), "Argument 'collectionRid' can not be null");

        // By theory, when we reach here, the status of the address should be in one of the three status: Unknown, Connected, UnhealthyPending
        // using open connection to validate addresses in UnhealthyPending status
        // Could extend to also open connection for unknown in the future

        List<Uri> addressesNeedToValidation = new ArrayList<>();
        for (AddressInformation address : addresses) {
            if (this.replicaValidationScopes.contains(address.getPhysicalUri().getHealthStatus())) {
                switch (address.getPhysicalUri().getHealthStatus()) {
                    case UnhealthyPending:
                        // Generally, an unhealthyPending replica has more chances to fail the request compared to unknown replica
                        // so we want to put it at the head of the validation list
                        addressesNeedToValidation.add(0, address.getPhysicalUri());
                        break;
                    case Unknown:
                        addressesNeedToValidation.add(address.getPhysicalUri());
                        break;
                    default:
                        // the status of the replica can be changed by other flows
                        // ignore the validation if the status is not in the validation scope anymore
                        logger.debug("Validate replica status is not support for status " + address.getPhysicalUri().getHealthStatus());
                        break;
                }
            }
        }

        if (addressesNeedToValidation.size() > 0 && this.proactiveOpenConnectionsProcessor != null) {

            logger.debug("Addresses to validate: [{}]", addressesNeedToValidation);

            int minConnectionsRequiredForEndpoint = 1;

            // 1. replica validation can ensure that for addresses with unknown (only when connection warm up is opted in) /
            // unhealthyPending statuses, open connections flow kicks in
            // 2. replica validation could kick in when there are new partitions (split / upgrade / partition moved scenarios)
            // for a container especially for those endpoint health statuses are can be in unknown / unhealthyPending state
            // 3. this also results in an update of the set of addresses which are under the open connections
            // flow provided these addresses are used by a container in the connection warm up flow
            if (this.proactiveOpenConnectionsProcessor.isCollectionRidUnderOpenConnectionsFlow(collectionRid)) {

                this.proactiveOpenConnectionsProcessor
                        .recordCollectionRidsAndUrisUnderOpenConnectionsAndInitCaches(
                                collectionRid,
                                addressesNeedToValidation
                                        .stream()
                                        .map(Uri::getURIAsString)
                                        .collect(Collectors.toList()));

                minConnectionsRequiredForEndpoint = this.connectionPolicy.getMinConnectionPoolSizePerEndpoint();
            }

            for (Uri addressToBeValidated : addressesNeedToValidation) {

                // replica validation should be triggered for an address with Unknown health status only when
                // the address is used by a container / collection which was part of the warm up flow
                if (addressToBeValidated.getHealthStatus() == Uri.HealthStatus.Unknown
                    && !this.proactiveOpenConnectionsProcessor.isCollectionRidUnderOpenConnectionsFlow(collectionRid)) {
                        continue;
                }

                Mono.fromFuture(this.proactiveOpenConnectionsProcessor
                        .submitOpenConnectionTaskOutsideLoop(
                                collectionRid,
                                this.serviceEndpoint,
                                addressToBeValidated,
                                minConnectionsRequiredForEndpoint))
                    .subscribeOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                    .subscribe();
            }
        }
    }

    private Pair<PartitionKeyRangeIdentity, AddressInformation[]> toPartitionAddressAndRange(String collectionRid, List<Address> addresses) {
        if (logger.isDebugEnabled()) {
            logger.debug("toPartitionAddressAndRange");
        }

        Address address = addresses.get(0);
        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, address.getParitionKeyRangeId());

        AddressInformation[] addressInfos =
                addresses
                    .stream()
                    .map(addr -> GatewayAddressCache.toAddressInformation(addr))
                    .collect(Collectors.toList())
                    .toArray(new AddressInformation[addresses.size()]);

        return Pair.of(partitionKeyRangeIdentity, addressInfos);
    }

    private static AddressInformation toAddressInformation(Address address) {
        return new AddressInformation(true, address.isPrimary(), address.getPhyicalUri(), address.getProtocolScheme());
    }

    public Flux<ImmutablePair<ImmutablePair<String, DocumentCollection> , AddressInformation>> resolveAddressesAndInitCaches(
            String containerLink,
            DocumentCollection collection,
            List<PartitionKeyRangeIdentity> partitionKeyRangeIdentities) {

        checkNotNull(collection, "Argument 'collection' should not be null");
        checkNotNull(partitionKeyRangeIdentities, "Argument 'partitionKeyRangeIdentities' should not be null");

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "openConnectionsAndInitCaches collection: {}, partitionKeyRangeIdentities: {}",
                    collection.getResourceId(),
                    JavaStreamUtils.toString(partitionKeyRangeIdentities, ","));
        }

        if (this.replicaAddressValidationEnabled) {
            this.replicaValidationScopes.add(Uri.HealthStatus.Unknown);
        }

        List<Flux<List<Address>>> tasks = new ArrayList<>();
        int batchSize = GatewayAddressCache.DefaultBatchSize;

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
                this.clientContext,
                OperationType.Read,
                collection.getResourceId(),
                ResourceType.DocumentCollection,
                Collections.emptyMap());

        for (int i = 0; i < partitionKeyRangeIdentities.size(); i += batchSize) {

            int endIndex = i + batchSize;
            endIndex = Math.min(endIndex, partitionKeyRangeIdentities.size());

            tasks.add(
                    this.getServerAddressesViaGatewayWithRetry(
                            request,
                            collection.getResourceId(),
                            partitionKeyRangeIdentities
                                    .subList(i, endIndex)
                                    .stream()
                                    .map(PartitionKeyRangeIdentity::getPartitionKeyRangeId)
                                    .collect(Collectors.toList()),
                            false).flux());
        }

        return Flux.concat(tasks)
                .flatMap(list -> {
                    List<Pair<PartitionKeyRangeIdentity, AddressInformation[]>> pkrIdToAddressInfosList =
                            list.stream()
                                    .filter(addressInfo -> this.protocolScheme.equals(addressInfo.getProtocolScheme()))
                                    .collect(Collectors.groupingBy(Address::getParitionKeyRangeId))
                                    .values()
                                    .stream().map(addresses -> toPartitionAddressAndRange(collection.getResourceId(), addresses))
                                    .collect(Collectors.toList());

                    return Flux.fromIterable(pkrIdToAddressInfosList)
                            .flatMap(pkrIdToAddressInfos -> {
                                PartitionKeyRangeIdentity partitionKeyRangeIdentity = pkrIdToAddressInfos.getLeft();
                                AddressInformation[] addressInfos = pkrIdToAddressInfos.getRight();

                                this.serverPartitionAddressCache.set(partitionKeyRangeIdentity, addressInfos);

                                List<String> addressUrisAsString = Arrays
                                        .stream(addressInfos)
                                        .map(addressInformation -> addressInformation.getPhysicalUri().getURIAsString())
                                        .collect(Collectors.toList());

                                this.proactiveOpenConnectionsProcessor
                                        .recordCollectionRidsAndUrisUnderOpenConnectionsAndInitCaches(collection.getResourceId(), addressUrisAsString);

                                return Flux.fromArray(pkrIdToAddressInfos.getRight());
                            }, Configs.getCPUCnt() * 10, Configs.getCPUCnt() * 3)
                            .flatMap(addressInformation -> Mono.just(new ImmutablePair<>(new ImmutablePair<>(containerLink, collection), addressInformation)));
                });
    }

    public Mono<OpenConnectionResponse> submitOpenConnectionTask(
            AddressInformation address,
            DocumentCollection documentCollection,
            int connectionsPerEndpointCount) {

        // do not fail here, just log
        // this attempts to make the open connections flow
        // best effort
        if (this.proactiveOpenConnectionsProcessor == null) {
            logger.warn("proactiveOpenConnectionsProcessor is null");
            return Mono.empty();
        }

        int connectionsRequiredForEndpoint = Math.max(connectionsPerEndpointCount, this.connectionPolicy.getMinConnectionPoolSizePerEndpoint());

        OpenConnectionTask openConnectionTask = this.proactiveOpenConnectionsProcessor.submitOpenConnectionTaskOutsideLoop(
                documentCollection.getResourceId(),
                this.serviceEndpoint,
                address.getPhysicalUri(),
                connectionsRequiredForEndpoint);

        return Mono.fromFuture(openConnectionTask);
    }

    public Flux<OpenConnectionResponse> submitOpenConnectionTasks(
        PartitionKeyRange partitionKeyRange,
        String collectionRid) {

        if (this.proactiveOpenConnectionsProcessor == null) {
            return Flux.empty();
        }

        checkNotNull(partitionKeyRange, "Argument 'partitionKeyRange' cannot be null!");
        checkNotNull(collectionRid, "Argument 'collectionRid' cannot be null!");

        PartitionKeyRangeIdentity partitionKeyRangeIdentity = new PartitionKeyRangeIdentity(collectionRid, partitionKeyRange.getId());

        return this.serverPartitionAddressCache.getAsync(partitionKeyRangeIdentity, cachedAddresses -> Mono.just(cachedAddresses), cachedAddresses -> true)
            .flatMapMany(cachedAddresses -> Flux.fromArray(cachedAddresses))
            .flatMap(addressInformation -> Mono.fromFuture(
                this.proactiveOpenConnectionsProcessor.submitOpenConnectionTaskOutsideLoop(
                    collectionRid,
                    this.addressEndpoint,
                    addressInformation.getPhysicalUri(),
                    1)));
    }

    private Mono<List<Address>> getServerAddressesViaGatewayWithRetry(
            RxDocumentServiceRequest request,
            String collectionRid,
            List<String> partitionKeyRangeIds,
            boolean forceRefresh) {

        OpenConnectionAndInitCachesRetryPolicy openConnectionAndInitCachesRetryPolicy =
                new OpenConnectionAndInitCachesRetryPolicy(this.connectionPolicy.getThrottlingRetryOptions());

        return BackoffRetryUtility.executeRetry(
                () -> this.getServerAddressesViaGatewayAsync(request, collectionRid, partitionKeyRangeIds, forceRefresh),
                openConnectionAndInitCachesRetryPolicy);

    }

    private boolean notAllReplicasAvailable(AddressInformation[] addressInformations) {
        return addressInformations.length < ServiceConfig.SystemReplicationPolicy.MaxReplicaSetSize;
    }

    private void refreshCollectionRidAndAddressUrisUnderOpenConnectionsAndInitCaches(String collectionRid, AddressInformation[] addressInfos) {
        if (this.proactiveOpenConnectionsProcessor.isCollectionRidUnderOpenConnectionsFlow(collectionRid)) {

            List<String> addressUrisAsString = Arrays
                    .stream(addressInfos)
                    .map(addressInformation -> addressInformation.getPhysicalUri().getURIAsString())
                    .collect(Collectors.toList());

            this.proactiveOpenConnectionsProcessor
                    .recordCollectionRidsAndUrisUnderOpenConnectionsAndInitCaches(collectionRid, addressUrisAsString);
        }
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

    private static void logAddressResolutionEnd(
        RxDocumentServiceRequest request,
        String identifier,
        String errorMessage,
        long transportRequestId) {
        if (request.requestContext.cosmosDiagnostics != null) {
            ImplementationBridgeHelpers
                .CosmosDiagnosticsHelper
                .getCosmosDiagnosticsAccessor()
                .recordAddressResolutionEnd(request, identifier, errorMessage, transportRequestId);
        }
    }

    private static class ForcedRefreshMetadata {
        private final ConcurrentHashMap<PartitionKeyRangeIdentity, Instant> lastPartitionAddressOnlyRefresh;
        private Instant lastCollectionRoutingMapRefresh;

        public ForcedRefreshMetadata() {
            lastPartitionAddressOnlyRefresh = new ConcurrentHashMap<>();
            lastCollectionRoutingMapRefresh = Instant.now();
        }

        public void signalCollectionRoutingMapRefresh(
            PartitionKeyRangeIdentity pk,
            boolean forcePartitionAddressRefresh) {

            Instant nowSnapshot = Instant.now();

            if (forcePartitionAddressRefresh) {
                lastPartitionAddressOnlyRefresh.put(pk, nowSnapshot);
            }

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
