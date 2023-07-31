// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.GatewayServiceConfigurationReader;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.directconnectivity.RequestHelper;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.faultinjection.GatewayServerErrorInjector;
import com.azure.cosmos.implementation.faultinjection.IFaultInjectorProvider;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.http.ReactorNettyRequestRecord;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import com.azure.cosmos.models.CosmosContainerIdentity;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders.INTENDED_COLLECTION_RID_HEADER;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Used internally to provide functionality to communicate and process response from GATEWAY in the Azure Cosmos DB database service.
 */
public class RxGatewayStoreModel implements RxStoreModel {
    private final static byte[] EMPTY_BYTE_ARRAY = {};
    private final DiagnosticsClientContext clientContext;
    private final Logger logger = LoggerFactory.getLogger(RxGatewayStoreModel.class);
    private final Map<String, String> defaultHeaders;
    private final HttpClient httpClient;
    private final QueryCompatibilityMode queryCompatibilityMode;
    private final GlobalEndpointManager globalEndpointManager;
    private ConsistencyLevel defaultConsistencyLevel;
    private ISessionContainer sessionContainer;
    private ThroughputControlStore throughputControlStore;
    private boolean useMultipleWriteLocations;
    private RxPartitionKeyRangeCache partitionKeyRangeCache;
    private GatewayServiceConfigurationReader gatewayServiceConfigurationReader;
    private RxClientCollectionCache collectionCache;
    private GatewayServerErrorInjector gatewayServerErrorInjector;

    public RxGatewayStoreModel(
        DiagnosticsClientContext clientContext,
        ISessionContainer sessionContainer,
        ConsistencyLevel defaultConsistencyLevel,
        QueryCompatibilityMode queryCompatibilityMode,
        UserAgentContainer userAgentContainer,
        GlobalEndpointManager globalEndpointManager,
        HttpClient httpClient,
        ApiType apiType) {
        this.clientContext = clientContext;
        this.defaultHeaders = new HashMap<>();
        this.defaultHeaders.put(HttpConstants.HttpHeaders.CACHE_CONTROL,
            "no-cache");
        this.defaultHeaders.put(HttpConstants.HttpHeaders.VERSION,
            HttpConstants.Versions.CURRENT_VERSION);
        this.defaultHeaders.put(
            HttpConstants.HttpHeaders.SDK_SUPPORTED_CAPABILITIES,
            HttpConstants.SDKSupportedCapabilities.SUPPORTED_CAPABILITIES);

        if (apiType != null) {
            this.defaultHeaders.put(HttpConstants.HttpHeaders.API_TYPE, apiType.toString());
        }

        if (userAgentContainer == null) {
            userAgentContainer = new UserAgentContainer();
        }

        this.defaultHeaders.put(HttpConstants.HttpHeaders.USER_AGENT, userAgentContainer.getUserAgent());

        if (defaultConsistencyLevel != null) {
            this.defaultHeaders.put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                defaultConsistencyLevel.toString());
        }

        this.defaultConsistencyLevel = defaultConsistencyLevel;
        this.globalEndpointManager = globalEndpointManager;
        this.queryCompatibilityMode = queryCompatibilityMode;

        this.httpClient = httpClient;
        this.sessionContainer = sessionContainer;
    }

    void setGatewayServiceConfigurationReader(GatewayServiceConfigurationReader gatewayServiceConfigurationReader) {
        this.gatewayServiceConfigurationReader = gatewayServiceConfigurationReader;
    }

    public void setPartitionKeyRangeCache(RxPartitionKeyRangeCache partitionKeyRangeCache) {
        this.partitionKeyRangeCache = partitionKeyRangeCache;
    }

    public void setUseMultipleWriteLocations(boolean useMultipleWriteLocations) {
        this.useMultipleWriteLocations = useMultipleWriteLocations;
    }

    boolean isUseMultipleWriteLocations() {
        return useMultipleWriteLocations;
    }

    RxPartitionKeyRangeCache getPartitionKeyRangeCache() {
        return partitionKeyRangeCache;
    }

    GatewayServiceConfigurationReader getGatewayServiceConfigurationReader() {
        return gatewayServiceConfigurationReader;
    }

    RxClientCollectionCache getCollectionCache() {
        return collectionCache;
    }

    public void setCollectionCache(RxClientCollectionCache collectionCache) {
        this.collectionCache = collectionCache;
    }

    private Mono<RxDocumentServiceResponse> create(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    private Mono<RxDocumentServiceResponse> patch(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.PATCH);
    }

    private Mono<RxDocumentServiceResponse> upsert(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    private Mono<RxDocumentServiceResponse> read(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.GET);
    }

    private Mono<RxDocumentServiceResponse> replace(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.PUT);
    }

    private Mono<RxDocumentServiceResponse> delete(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.DELETE);
    }

    private Mono<RxDocumentServiceResponse> deleteByPartitionKey(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    private Mono<RxDocumentServiceResponse> execute(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    private Mono<RxDocumentServiceResponse> readFeed(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.GET);
    }

    private Mono<RxDocumentServiceResponse> query(RxDocumentServiceRequest request) {
        if (request.getOperationType() != OperationType.QueryPlan) {
            request.getHeaders().put(HttpConstants.HttpHeaders.IS_QUERY, "true");
        }

        switch (this.queryCompatibilityMode) {
            case SqlQuery:
                request.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE,
                    RuntimeConstants.MediaTypes.SQL);
                break;
            case Default:
            case Query:
            default:
                request.getHeaders().put(HttpConstants.HttpHeaders.CONTENT_TYPE,
                    RuntimeConstants.MediaTypes.QUERY_JSON);
                break;
        }
        return this.performRequest(request, HttpMethod.POST);
    }

    public Mono<RxDocumentServiceResponse> performRequest(RxDocumentServiceRequest request, HttpMethod method) {
        try {
            if (request.requestContext.cosmosDiagnostics == null) {
                request.requestContext.cosmosDiagnostics = clientContext.createDiagnostics();
            }

            URI uri = getUri(request);
            request.requestContext.resourcePhysicalAddress = uri.toString();

            if (this.throughputControlStore != null) {
                return this.throughputControlStore.processRequest(request, performRequestInternal(request, method, uri));
            }

            return this.performRequestInternal(request, method, uri);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    /**
     * Given the request it creates an flux which upon subscription issues HTTP call and emits one RxDocumentServiceResponse.
     *
     * @param request
     * @param method
     * @param requestUri
     * @return Flux<RxDocumentServiceResponse>
     */
    public Mono<RxDocumentServiceResponse> performRequestInternal(RxDocumentServiceRequest request, HttpMethod method, URI requestUri) {

        try {

            HttpHeaders httpHeaders = this.getHttpRequestHeaders(request.getHeaders());

            Flux<byte[]> contentAsByteArray = request.getContentAsByteArrayFlux();

            HttpRequest httpRequest = new HttpRequest(method,
                requestUri,
                requestUri.getPort(),
                httpHeaders,
                contentAsByteArray);

            Duration responseTimeout = Duration.ofSeconds(Configs.getHttpResponseTimeoutInSeconds());
            if (OperationType.QueryPlan.equals(request.getOperationType())) {
                responseTimeout = Duration.ofSeconds(Configs.getQueryPlanResponseTimeoutInSeconds());
            } else if (request.isAddressRefresh()) {
                responseTimeout = Duration.ofSeconds(Configs.getAddressRefreshResponseTimeoutInSeconds());
            }

            Mono<HttpResponse> httpResponseMono = this.httpClient.send(httpRequest, responseTimeout);

            if (this.gatewayServerErrorInjector != null) {
                httpResponseMono = this.gatewayServerErrorInjector.injectGatewayErrors(responseTimeout, httpRequest, request, httpResponseMono);
                return toDocumentServiceResponse(httpResponseMono, request, httpRequest);
            }

            return toDocumentServiceResponse(httpResponseMono, request, httpRequest);

        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private HttpHeaders getHttpRequestHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders(this.defaultHeaders.size());
        // Add default headers.
        for (Entry<String, String> entry : this.defaultHeaders.entrySet()) {
            if (!headers.containsKey(entry.getKey())) {
                // populate default header only if there is no overwrite by the request header
                httpHeaders.set(entry.getKey(), entry.getValue());
            }
        }

        // Add override headers.
        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                if (entry.getValue() == null) {
                    // netty doesn't allow setting null value in header
                    httpHeaders.set(entry.getKey(), "");
                } else {
                    httpHeaders.set(entry.getKey(), entry.getValue());
                }
            }
        }
        return httpHeaders;
    }

    private URI getUri(RxDocumentServiceRequest request) throws URISyntaxException {
        URI rootUri = request.getEndpointOverride();
        if (rootUri == null) {
            if (request.getIsMedia()) {
                // For media read request, always use the write endpoint.
                rootUri = this.globalEndpointManager.getWriteEndpoints().get(0);
            } else {
                rootUri = this.globalEndpointManager.resolveServiceEndpoint(request);
            }
        }

        String path = PathsHelper.generatePath(request.getResourceType(), request, request.isFeed);
        if (request.getResourceType().equals(ResourceType.DatabaseAccount)) {
            path = StringUtils.EMPTY;
        }

        return new URI("https",
            null,
            rootUri.getHost(),
            rootUri.getPort(),
            ensureSlashPrefixed(path),
            null,  // Query string not used.
            null);
    }

    private String ensureSlashPrefixed(String path) {
        if (path == null) {
            return null;
        }

        if (path.startsWith("/")) {
            return path;
        }

        return "/" + path;
    }

    /**
     * Transforms the reactor netty's client response Observable to RxDocumentServiceResponse Observable.
     *
     *
     * Once the customer code subscribes to the observable returned by the CRUD APIs,
     * the subscription goes up till it reaches the source reactor netty's observable, and at that point the HTTP invocation will be made.
     *
     * @param httpResponseMono
     * @param request
     * @return {@link Mono}
     */
    private Mono<RxDocumentServiceResponse> toDocumentServiceResponse(Mono<HttpResponse> httpResponseMono,
                                                                      RxDocumentServiceRequest request,
                                                                      HttpRequest httpRequest) {

        return httpResponseMono.flatMap(httpResponse -> {

            // header key/value pairs
            HttpHeaders httpResponseHeaders = httpResponse.headers();
            int httpResponseStatus = httpResponse.statusCode();

            Mono<byte[]> contentObservable = httpResponse
                .bodyAsByteArray()
                .switchIfEmpty(Mono.just(EMPTY_BYTE_ARRAY));

            return contentObservable
                .map(content -> {
                    // Capture transport client request timeline
                    ReactorNettyRequestRecord reactorNettyRequestRecord = httpResponse.request().reactorNettyRequestRecord();
                    if (reactorNettyRequestRecord != null) {
                        reactorNettyRequestRecord.setTimeCompleted(Instant.now());
                    }

                    // If there is any error in the header response this throws exception
                    validateOrThrow(request, HttpResponseStatus.valueOf(httpResponseStatus), httpResponseHeaders, content);

                    StoreResponse rsp = new StoreResponse(httpResponseStatus,
                        HttpUtils.unescape(httpResponseHeaders.toMap()),
                        content);

                    if (reactorNettyRequestRecord != null) {
                        rsp.setRequestTimeline(reactorNettyRequestRecord.takeTimelineSnapshot());

                        if (this.gatewayServerErrorInjector != null) {
                            // only configure when fault injection is used
                            rsp.setFaultInjectionRuleId(
                                request
                                    .faultInjectionRequestContext
                                    .getFaultInjectionRuleId(reactorNettyRequestRecord.getTransportRequestId()));

                            rsp.setFaultInjectionRuleEvaluationResults(
                                request
                                    .faultInjectionRequestContext
                                    .getFaultInjectionRuleEvaluationResults(reactorNettyRequestRecord.getTransportRequestId()));
                        }
                    }

                    if (request.requestContext.cosmosDiagnostics != null) {
                        BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, rsp, globalEndpointManager);
                    }
                    return rsp;
                })
                .single();

        }).map(rsp -> {
            RxDocumentServiceResponse rxDocumentServiceResponse;
            if (httpRequest.reactorNettyRequestRecord() != null) {
                rxDocumentServiceResponse =
                    new RxDocumentServiceResponse(this.clientContext, rsp,
                        httpRequest.reactorNettyRequestRecord().takeTimelineSnapshot());

            } else {
                rxDocumentServiceResponse =
                    new RxDocumentServiceResponse(this.clientContext, rsp);
            }
            rxDocumentServiceResponse.setCosmosDiagnostics(request.requestContext.cosmosDiagnostics);
            return rxDocumentServiceResponse;
        }).onErrorResume(throwable -> {
            Throwable unwrappedException = reactor.core.Exceptions.unwrap(throwable);
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

                int statusCode = 0;
                if (WebExceptionUtility.isNetworkFailure(exception)) {
                    if (WebExceptionUtility.isReadTimeoutException(exception)) {
                        statusCode = HttpConstants.StatusCodes.REQUEST_TIMEOUT;
                    } else {
                        statusCode = HttpConstants.StatusCodes.SERVICE_UNAVAILABLE;
                    }
                }

                dce = BridgeInternal.createCosmosException(request.requestContext.resourcePhysicalAddress, statusCode, exception);
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
                if (httpRequest.reactorNettyRequestRecord() != null) {
                    ReactorNettyRequestRecord reactorNettyRequestRecord = httpRequest.reactorNettyRequestRecord();
                    BridgeInternal.setRequestTimeline(dce, reactorNettyRequestRecord.takeTimelineSnapshot());

                    ImplementationBridgeHelpers
                        .CosmosExceptionHelper
                        .getCosmosExceptionAccessor()
                        .setFaultInjectionRuleId(
                            dce,
                            request.faultInjectionRequestContext
                                .getFaultInjectionRuleId(reactorNettyRequestRecord.getTransportRequestId()));

                    ImplementationBridgeHelpers
                        .CosmosExceptionHelper
                        .getCosmosExceptionAccessor()
                        .setFaultInjectionEvaluationResults(
                            dce,
                            request.faultInjectionRequestContext
                                .getFaultInjectionRuleEvaluationResults(reactorNettyRequestRecord.getTransportRequestId()));
                }

                BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, dce, globalEndpointManager);
            }

            return Mono.error(dce);
        });
    }

    private void validateOrThrow(RxDocumentServiceRequest request,
                                 HttpResponseStatus status,
                                 HttpHeaders headers,
                                 byte[] bodyAsBytes) {

        int statusCode = status.code();

        if (statusCode >= HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY) {
            String statusCodeString = status.reasonPhrase() != null
                ? status.reasonPhrase().replace(" ", "")
                : "";

            String body = bodyAsBytes != null ? new String(bodyAsBytes, StandardCharsets.UTF_8) : null;
            CosmosError cosmosError;
            cosmosError = (StringUtils.isNotEmpty(body)) ? new CosmosError(body) : new CosmosError();
            cosmosError = new CosmosError(statusCodeString,
                String.format("%s, StatusCode: %s", cosmosError.getMessage(), statusCodeString),
                cosmosError.getPartitionedQueryExecutionInfo());

            CosmosException dce = BridgeInternal.createCosmosException(request.requestContext.resourcePhysicalAddress, statusCode, cosmosError, headers.toMap());
            BridgeInternal.setRequestHeaders(dce, request.getHeaders());
            throw dce;
        }
    }

    private Mono<RxDocumentServiceResponse> invokeAsyncInternal(RxDocumentServiceRequest request) {
        switch (request.getOperationType()) {
            case Create:
            case Batch:
                return this.create(request);
            case Patch:
                return this.patch(request);
            case Upsert:
                return this.upsert(request);
            case Delete:
                if (request.getResourceType() == ResourceType.PartitionKey) {
                    return this.deleteByPartitionKey(request);
                }
                return this.delete(request);
            case ExecuteJavaScript:
                return this.execute(request);
            case Read:
                return this.read(request);
            case ReadFeed:
                return this.readFeed(request);
            case Replace:
                return this.replace(request);
            case SqlQuery:
            case Query:
            case QueryPlan:
                return this.query(request);
            default:
                throw new IllegalStateException("Unknown operation setType " + request.getOperationType());
        }
    }

    private Mono<RxDocumentServiceResponse> invokeAsync(RxDocumentServiceRequest request) {
        Callable<Mono<RxDocumentServiceResponse>> funcDelegate = () -> invokeAsyncInternal(request).single();

        MetadataRequestRetryPolicy metadataRequestRetryPolicy = new MetadataRequestRetryPolicy(this.globalEndpointManager);
        metadataRequestRetryPolicy.onBeforeSendRequest(request);

        return BackoffRetryUtility.executeRetry(funcDelegate, metadataRequestRetryPolicy);
    }

    @Override
    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        Mono<RxDocumentServiceResponse> responseObs = this.addIntendedCollectionRidAndSessionToken(request).then(invokeAsync(request));

        return responseObs.onErrorResume(
            e -> {
                CosmosException dce = Utils.as(e, CosmosException.class);

                if (dce == null) {
                    logger.error("unexpected failure {}", e.getMessage(), e);
                    return Mono.error(e);
                }

                if ((!ReplicatedResourceClientUtils.isMasterResource(request.getResourceType())) &&
                    (dce.getStatusCode() == HttpConstants.StatusCodes.PRECONDITION_FAILED ||
                        dce.getStatusCode() == HttpConstants.StatusCodes.CONFLICT ||
                        (
                            dce.getStatusCode() == HttpConstants.StatusCodes.NOTFOUND &&
                                !Exceptions.isSubStatusCode(dce,
                                    HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)))) {
                    this.captureSessionToken(request, dce.getResponseHeaders());
                }

                if (Exceptions.isThroughputControlRequestRateTooLargeException(dce)) {
                    if (request.requestContext.cosmosDiagnostics != null) {
                        BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, dce, globalEndpointManager);
                    }
                }

                return Mono.error(dce);
            }
        ).flatMap(response ->
            this.captureSessionTokenAndHandlePartitionSplit(request, response.getResponseHeaders()).then(Mono.just(response))
        );
    }

    @Override
    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
        // no-op
        // Disable throughput control for gateway mode
    }

    @Override
    public Flux<Void> submitOpenConnectionTasksAndInitCaches(CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {
        return Flux.empty();
    }

    @Override
    public void configureFaultInjectorProvider(IFaultInjectorProvider injectorProvider, Configs configs) {
        if (this.gatewayServerErrorInjector == null) {
            this.gatewayServerErrorInjector = new GatewayServerErrorInjector(configs);
        }

        this.gatewayServerErrorInjector.registerServerErrorInjector(injectorProvider.getServerErrorInjector());
    }

    @Override
    public void recordOpenConnectionsAndInitCachesCompleted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        //no-op
    }

    @Override
    public void recordOpenConnectionsAndInitCachesStarted(List<CosmosContainerIdentity> cosmosContainerIdentities) {
        //no-op
    }

    private void captureSessionToken(RxDocumentServiceRequest request, Map<String, String> responseHeaders) {
        if (request.getResourceType() == ResourceType.DocumentCollection &&
            request.getOperationType() == OperationType.Delete) {

            String resourceId;
            if (request.getIsNameBased()) {
                resourceId = responseHeaders.get(HttpConstants.HttpHeaders.OWNER_ID);
            } else {
                resourceId = request.getResourceId();
            }
            this.sessionContainer.clearTokenByResourceId(resourceId);
        } else {
            this.sessionContainer.setSessionToken(request, responseHeaders);
        }
    }

    private Mono<Void> captureSessionTokenAndHandlePartitionSplit(RxDocumentServiceRequest request,
                                                                  Map<String, String> responseHeaders) {
        this.captureSessionToken(request, responseHeaders);
        if (request.requestContext.resolvedPartitionKeyRange != null &&
            StringUtils.isNotEmpty(request.requestContext.resolvedCollectionRid) &&
            StringUtils.isNotEmpty(responseHeaders.get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID)) &&
            !responseHeaders.get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID).equals(request.requestContext.resolvedPartitionKeyRange.getId())) {
            return this.partitionKeyRangeCache.refreshAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request.requestContext.resolvedCollectionRid)
                .flatMap(collectionRoutingMapValueHolder -> Mono.empty());
        }
        return Mono.empty();
    }

    private Mono<Void> addIntendedCollectionRidAndSessionToken(RxDocumentServiceRequest request) {
        return applySessionToken(request).then(addIntendedCollectionRid(request));
    }

    private Mono<Void> addIntendedCollectionRid(RxDocumentServiceRequest request) {
        if (this.collectionCache != null && request.getResourceType().equals(ResourceType.Document)) {
            return this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request).flatMap(documentCollectionValueHolder -> {
                if (StringUtils.isEmpty(request.getHeaders().get(INTENDED_COLLECTION_RID_HEADER))) {
                    request.getHeaders().put(INTENDED_COLLECTION_RID_HEADER,
                        request.requestContext.resolvedCollectionRid);
                } else {
                    request.intendedCollectionRidPassedIntoSDK = true;
                }
                return Mono.empty();
            });
        }
        return Mono.empty();
    }

    private Mono<Void> applySessionToken(RxDocumentServiceRequest request) {
        Map<String, String> headers = request.getHeaders();
        Objects.requireNonNull(headers, "RxDocumentServiceRequest::headers is required and cannot be null");

        // Master resource operations don't require session token.
        if (isMasterOperation(request.getResourceType(), request.getOperationType())) {
            if (!Strings.isNullOrEmpty(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))) {
                request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
            }
            return Mono.empty();
        }

        boolean sessionConsistency = RequestHelper.getConsistencyLevelToUse(this.gatewayServiceConfigurationReader,
            request) == ConsistencyLevel.SESSION;

        if (!Strings.isNullOrEmpty(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))) {
            if (!sessionConsistency ||
                (!request.isReadOnlyRequest() && request.getOperationType() != OperationType.Batch && !this.useMultipleWriteLocations)) {
                request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
            }
            return Mono.empty(); //User is explicitly controlling the session.
        }

        if (!sessionConsistency ||
            (!request.isReadOnlyRequest() && request.getOperationType() != OperationType.Batch && !this.useMultipleWriteLocations)) {
            return Mono.empty();
            // Only apply the session token in case of session consistency and if request is read only,
            // apply token for write request only if batch operation or multi master
        }

        if (this.collectionCache != null && this.partitionKeyRangeCache != null) {
            return this.collectionCache.resolveCollectionAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics), request).
                flatMap(collectionValueHolder -> {

                    if (collectionValueHolder == null || collectionValueHolder.v == null) {
                        //Apply the ambient session.
                        String sessionToken = this.sessionContainer.resolveGlobalSessionToken(request);

                        if (!Strings.isNullOrEmpty(sessionToken)) {
                            headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
                        }
                        return Mono.empty();
                    }
                    return partitionKeyRangeCache.tryLookupAsync(BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                        collectionValueHolder.v.getResourceId(),
                        null,
                        null).flatMap(collectionRoutingMapValueHolder -> {
                        if (collectionRoutingMapValueHolder == null || collectionRoutingMapValueHolder.v == null) {
                            //Apply the ambient session.
                            String sessionToken = this.sessionContainer.resolveGlobalSessionToken(request);

                            if (!Strings.isNullOrEmpty(sessionToken)) {
                                headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
                            }
                            return Mono.empty();
                        }
                        String partitionKeyRangeId =
                            request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
                        PartitionKeyInternal partitionKeyInternal = request.getPartitionKeyInternal();

                        if (StringUtils.isNotEmpty(partitionKeyRangeId)) {
                            PartitionKeyRange range =
                                collectionRoutingMapValueHolder.v.getRangeByPartitionKeyRangeId(partitionKeyRangeId);
                            request.requestContext.resolvedPartitionKeyRange = range;
                            if (request.requestContext.resolvedPartitionKeyRange == null) {
                                SessionTokenHelper.setPartitionLocalSessionToken(request, partitionKeyRangeId,
                                    sessionContainer);
                            } else {
                                SessionTokenHelper.setPartitionLocalSessionToken(request, sessionContainer);
                            }
                        } else if (partitionKeyInternal != null) {
                            String effectivePartitionKeyString = PartitionKeyInternalHelper
                                .getEffectivePartitionKeyString(
                                    partitionKeyInternal,
                                    collectionValueHolder.v.getPartitionKey());
                            PartitionKeyRange range =
                                collectionRoutingMapValueHolder.v.getRangeByEffectivePartitionKey(effectivePartitionKeyString);
                            request.requestContext.resolvedPartitionKeyRange = range;
                            SessionTokenHelper.setPartitionLocalSessionToken(request, sessionContainer);
                        } else {
                            //Apply the ambient session.
                            String sessionToken = this.sessionContainer.resolveGlobalSessionToken(request);

                            if (!Strings.isNullOrEmpty(sessionToken)) {
                                headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
                            }
                        }

                        return Mono.empty();
                    });
                });
        } else {
            //Apply the ambient session.
            String sessionToken = this.sessionContainer.resolveGlobalSessionToken(request);

            if (!Strings.isNullOrEmpty(sessionToken)) {
                headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
            }
            return Mono.empty();
        }
    }

    private static boolean isMasterOperation(ResourceType resourceType, OperationType operationType) {
        // Stored procedures, trigger, and user defined functions CRUD operations are done on
        // master so they do not require the session token.
        // Stored procedures execute is not a master operation
        return ReplicatedResourceClientUtils.isMasterResource(resourceType) ||
            isStoredProcedureMasterOperation(resourceType, operationType) ||
            operationType == OperationType.QueryPlan;
    }

    private static boolean isStoredProcedureMasterOperation(ResourceType resourceType, OperationType operationType) {
        return resourceType == ResourceType.StoredProcedure && operationType != OperationType.ExecuteJavaScript;
    }
}
