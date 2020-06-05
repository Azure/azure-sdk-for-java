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
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import com.azure.cosmos.implementation.http.ReactorNettyRequestRecord;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
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
    private final Logger logger = LoggerFactory.getLogger(RxGatewayStoreModel.class);
    private final Map<String, String> defaultHeaders;
    private final HttpClient httpClient;
    private final QueryCompatibilityMode queryCompatibilityMode;
    private final GlobalEndpointManager globalEndpointManager;
    private ConsistencyLevel defaultConsistencyLevel;
    private ISessionContainer sessionContainer;

    public RxGatewayStoreModel(
            ISessionContainer sessionContainer,
            ConsistencyLevel defaultConsistencyLevel,
            QueryCompatibilityMode queryCompatibilityMode,
            UserAgentContainer userAgentContainer,
            GlobalEndpointManager globalEndpointManager,
            HttpClient httpClient) {
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

    /**
     * Given the request it creates an flux which upon subscription issues HTTP call and emits one RxDocumentServiceResponse.
     *
     * @param request
     * @param method
     * @return Flux<RxDocumentServiceResponse>
     */
    public Mono<RxDocumentServiceResponse> performRequest(RxDocumentServiceRequest request, HttpMethod method) {

        try {

            if (request.requestContext.cosmosDiagnostics == null) {
                request.requestContext.cosmosDiagnostics = BridgeInternal.createCosmosDiagnostics();
            }

            URI uri = getUri(request);

            HttpHeaders httpHeaders = this.getHttpRequestHeaders(request.getHeaders());

            // The RxDocumentServiceRequest::getContentAsByteBufFlux guaranteed to return
            // a valid flux (including Flux.empty) hence null check is not required here.
            Flux<ByteBuf> byteBufObservable = request.getContentAsByteBufFlux();

            HttpRequest httpRequest = new HttpRequest(method,
                    uri,
                    uri.getPort(),
                    httpHeaders,
                    byteBufObservable);

            Mono<HttpResponse> httpResponseMono = this.httpClient.send(httpRequest);

            return toDocumentServiceResponse(httpResponseMono, request);

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
                                                                      RxDocumentServiceRequest request) {

        return httpResponseMono.flatMap(httpResponse ->  {

            // header key/value pairs
            HttpHeaders httpResponseHeaders = httpResponse.headers();
            int httpResponseStatus = httpResponse.statusCode();

            Mono<byte[]> contentObservable;

            if (request.getOperationType() == OperationType.Delete) {
                // for delete we don't expect any body
                contentObservable = Mono.just(EMPTY_BYTE_ARRAY);
            } else {
                // transforms the ByteBufFlux to Flux<String>
                contentObservable = httpResponse
                                        .bodyAsByteArray()
                                        .switchIfEmpty(Mono.just(EMPTY_BYTE_ARRAY));
            }

            return contentObservable
                       .map(content -> {
                               //Adding transport client request timeline to diagnostics
                               ReactorNettyRequestRecord reactorNettyRequestRecord = httpResponse.request().getReactorNettyRequestRecord();
                               if (reactorNettyRequestRecord != null) {
                                   reactorNettyRequestRecord.setTimeCompleted(Instant.now());
                                   BridgeInternal.setTransportClientRequestTimelineOnDiagnostics(request.requestContext.cosmosDiagnostics,
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

        }).map(RxDocumentServiceResponse::new)
                   .onErrorResume(throwable -> {
                       Throwable unwrappedException = reactor.core.Exceptions.unwrap(throwable);
                       if (!(unwrappedException instanceof Exception)) {
                           // fatal error
                           logger.error("Unexpected failure {}", unwrappedException.getMessage(), unwrappedException);
                           return Mono.error(unwrappedException);
                       }

                       Exception exception = (Exception) unwrappedException;
                       if (!(exception instanceof CosmosException)) {
                           // wrap in CosmosException
                           logger.error("Network failure", exception);
                           CosmosException dce = BridgeInternal.createCosmosException(0, exception);
                           BridgeInternal.setRequestHeaders(dce, request.getHeaders());
                           return Mono.error(dce);
                       }

                       if (request.requestContext.cosmosDiagnostics != null) {
                           BridgeInternal.recordGatewayResponse(request.requestContext.cosmosDiagnostics, request, null, (CosmosException)exception);
                           BridgeInternal.setCosmosDiagnostics((CosmosException)exception, request.requestContext.cosmosDiagnostics);
                       }

                       return Mono.error(exception);
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

            CosmosException dce = BridgeInternal.createCosmosException(statusCode, cosmosError, headers.toMap());
            BridgeInternal.setRequestHeaders(dce, request.getHeaders());
            throw dce;
        }
    }

    private Mono<RxDocumentServiceResponse> invokeAsyncInternal(RxDocumentServiceRequest request)  {
        switch (request.getOperationType()) {
            case Create:
                return this.create(request);
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
        return BackoffRetryUtility.executeRetry(funcDelegate, new WebExceptionRetryPolicy());
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

                    return Mono.error(dce);
                }
        ).map(response ->
                {
                    this.captureSessionToken(request, response.getResponseHeaders());
                    return response;
                }
        );
    }

    private void captureSessionToken(RxDocumentServiceRequest request, Map<String, String> responseHeaders) {
        if (request.getResourceType() == ResourceType.DocumentCollection && request.getOperationType() == OperationType.Delete) {
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

        if (!Strings.isNullOrEmpty(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))) {
            if (ReplicatedResourceClientUtils.isMasterResource(request.getResourceType())) {
                request.getHeaders().remove(HttpConstants.HttpHeaders.SESSION_TOKEN);
            }
            return; //User is explicitly controlling the session.
        }

        String requestConsistencyLevel = headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        boolean sessionConsistency =
                this.defaultConsistencyLevel == ConsistencyLevel.SESSION ||
                        (!Strings.isNullOrEmpty(requestConsistencyLevel)
                                && Strings.areEqual(requestConsistencyLevel, ConsistencyLevel.SESSION.toString()));

        if (!sessionConsistency || ReplicatedResourceClientUtils.isMasterResource(request.getResourceType())) {
            return; // Only apply the session token in case of session consistency and when resource is not a master resource
        }

        //Apply the ambient session.
        String sessionToken = this.sessionContainer.resolveGlobalSessionToken(request);

        if (!Strings.isNullOrEmpty(sessionToken)) {
            headers.put(HttpConstants.HttpHeaders.SESSION_TOKEN, sessionToken);
        }
    }
}
