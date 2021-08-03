// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.DirectBridgeInternal;
import com.azure.cosmos.implementation.directconnectivity.HttpUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.http.ReactorNettyRequestRecord;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlStore;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Used internally to provide functionality to communicate and process response from GATEWAY in the Azure Cosmos DB database service.
 */
class RxGatewayStoreModel implements RxStoreModel {
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

    public RxGatewayStoreModel(
            DiagnosticsClientContext clientContext,
            ISessionContainer sessionContainer,
            ConsistencyLevel defaultConsistencyLevel,
            QueryCompatibilityMode queryCompatibilityMode,
            UserAgentContainer userAgentContainer,
            GlobalEndpointManager globalEndpointManager,
            HttpClient httpClient) {
        this.clientContext = clientContext;
        this.defaultHeaders = new HashMap<>();
        this.defaultHeaders.put(HttpConstants.HttpHeaders.CACHE_CONTROL,
                "no-cache");
        this.defaultHeaders.put(HttpConstants.HttpHeaders.VERSION,
                HttpConstants.Versions.CURRENT_VERSION);

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

    private Mono<RxDocumentServiceResponse> execute(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    private Mono<RxDocumentServiceResponse> readFeed(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.GET);
    }

    private Mono<RxDocumentServiceResponse> query(RxDocumentServiceRequest request) {
        if(request.getOperationType() != OperationType.QueryPlan) {
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
        if(request.getResourceType().equals(ResourceType.DatabaseAccount)) {
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

        return httpResponseMono.flatMap(httpResponse ->  {

            // header key/value pairs
            HttpHeaders httpResponseHeaders = httpResponse.headers();
            int httpResponseStatus = httpResponse.statusCode();

            Mono<byte[]> contentObservable = httpResponse
                .bodyAsByteArray()
                .switchIfEmpty(Mono.just(EMPTY_BYTE_ARRAY));

            return contentObservable
                       .map(content -> {
                               //Adding transport client request timeline to diagnostics
                               ReactorNettyRequestRecord reactorNettyRequestRecord = httpResponse.request().reactorNettyRequestRecord();
                               if (reactorNettyRequestRecord != null) {
                                   reactorNettyRequestRecord.setTimeCompleted(Instant.now());
                                   BridgeInternal.setGatewayRequestTimelineOnDiagnostics(request.requestContext.cosmosDiagnostics,
                                       reactorNettyRequestRecord.takeTimelineSnapshot());
                               }

                               // If there is any error in the header response this throws exception
                               // TODO: potential performance improvement: return Observable.error(exception) on failure instead of throwing Exception
                               validateOrThrow(request, HttpResponseStatus.valueOf(httpResponseStatus), httpResponseHeaders, content);

                               // transforms to Observable<StoreResponse>
                               StoreResponse rsp = new StoreResponse(httpResponseStatus,
                                   HttpUtils.unescape(httpResponseHeaders.toMap().entrySet()),
                                   content);
                               DirectBridgeInternal.setRequestTimeline(rsp, reactorNettyRequestRecord.takeTimelineSnapshot());
                               if (request.requestContext.cosmosDiagnostics != null) {
                                   BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, rsp, null);
                                   DirectBridgeInternal.setCosmosDiagnostics(rsp, request.requestContext.cosmosDiagnostics);
                               }
                               return rsp;
                       })
                       .single();

        }).map(rsp -> {
            if (httpRequest.reactorNettyRequestRecord() != null) {
                return new RxDocumentServiceResponse(this.clientContext, rsp,
                    httpRequest.reactorNettyRequestRecord().takeTimelineSnapshot());

            } else {
                return new RxDocumentServiceResponse(this.clientContext, rsp);
            }
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
                           if (BridgeInternal.getClientSideRequestStatics(request.requestContext.cosmosDiagnostics).getGatewayRequestTimeline() == null && httpRequest.reactorNettyRequestRecord() != null) {
                               BridgeInternal.setGatewayRequestTimelineOnDiagnostics(request.requestContext.cosmosDiagnostics,
                                   httpRequest.reactorNettyRequestRecord().takeTimelineSnapshot());
                           }

                           BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, null, dce);
                           BridgeInternal.setCosmosDiagnostics(dce, request.requestContext.cosmosDiagnostics);
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

            String body = bodyAsBytes != null ? new String(bodyAsBytes) : null;
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

    private Mono<RxDocumentServiceResponse> invokeAsyncInternal(RxDocumentServiceRequest request)  {
        switch (request.getOperationType()) {
            case Create:
            case Batch:
                return this.create(request);
            case Patch:
                return this.patch(request);
            case Upsert:
                return this.upsert(request);
            case Delete:
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
        return BackoffRetryUtility.executeRetry(funcDelegate, new WebExceptionRetryPolicy(BridgeInternal.getRetryContext(request.requestContext.cosmosDiagnostics)));
    }

    @Override
    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        this.applySessionToken(request);

        Mono<RxDocumentServiceResponse> responseObs = invokeAsync(request);

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
                        BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, null, dce);
                        BridgeInternal.setCosmosDiagnostics(dce, request.requestContext.cosmosDiagnostics);
                    }

                    return Mono.error(dce);
                }
        ).map(response ->
                {
                    this.captureSessionToken(request, response.getResponseHeaders());
                    return response;
                }
        );
    }

    @Override
    public void enableThroughputControl(ThroughputControlStore throughputControlStore) {
        // no-op
        // Disable throughput control for gateway mode
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

    private void applySessionToken(RxDocumentServiceRequest request) {
        Map<String, String> headers = request.getHeaders();
        Objects.requireNonNull(headers, "RxDocumentServiceRequest::headers is required and cannot be null");

        String requestConsistencyLevel = headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        boolean sessionTokenApplicable =
            Strings.areEqual(requestConsistencyLevel, ConsistencyLevel.SESSION.toString()) ||
                (this.defaultConsistencyLevel == ConsistencyLevel.SESSION &&
                    // skip applying the session token when Eventual Consistency is explicitly requested
                    // on request-level for data plane operations.
                    // The session token is ignored on teh backend/gateway in this case anyway
                    // and the session token can be rather large (even run in the 16 KB header length problem
                    // on the gateway - so not worth sending when not needed
                    (!request.isReadOnlyRequest() ||
                        request.getResourceType() != ResourceType.Document ||
                        !Strings.areEqual(requestConsistencyLevel, ConsistencyLevel.EVENTUAL.toString())));

        if (!Strings.isNullOrEmpty(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))) {
            if (!sessionTokenApplicable || isMasterOperation(request.getResourceType(), request.getOperationType())) {
                request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
            }
            return; //User is explicitly controlling the session.
        }

        if (!sessionTokenApplicable || isMasterOperation(request.getResourceType(), request.getOperationType())) {
            return; // Only apply the session token in case of session consistency and when resource is not a master resource
        }

        //Apply the ambient session.
        String sessionToken = this.sessionContainer.resolveGlobalSessionToken(request);

        if (!Strings.isNullOrEmpty(sessionToken)) {
            headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
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
