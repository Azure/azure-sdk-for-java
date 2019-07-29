// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;


import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosError;
import com.azure.data.cosmos.internal.directconnectivity.HttpUtils;
import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import com.azure.data.cosmos.internal.http.HttpClient;
import com.azure.data.cosmos.internal.http.HttpHeaders;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Used internally to provide functionality to communicate and process response from GATEWAY in the Azure Cosmos DB database service.
 */
class RxGatewayStoreModel implements RxStoreModel {

    private final static int INITIAL_RESPONSE_BUFFER_SIZE = 1024;
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

    private Flux<RxDocumentServiceResponse> doCreate(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    private Flux<RxDocumentServiceResponse> upsert(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    private Flux<RxDocumentServiceResponse> read(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.GET);
    }

    private Flux<RxDocumentServiceResponse> replace(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.PUT);
    }

    private Flux<RxDocumentServiceResponse> delete(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.DELETE);
    }

    private Flux<RxDocumentServiceResponse> execute(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.POST);
    }

    private Flux<RxDocumentServiceResponse> readFeed(RxDocumentServiceRequest request) {
        return this.performRequest(request, HttpMethod.GET);
    }

    private Flux<RxDocumentServiceResponse> query(RxDocumentServiceRequest request) {
        request.getHeaders().put(HttpConstants.HttpHeaders.IS_QUERY, "true");

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
    public Flux<RxDocumentServiceResponse> performRequest(RxDocumentServiceRequest request, HttpMethod method) {

        try {
            URI uri = getUri(request);

            HttpHeaders httpHeaders = this.getHttpRequestHeaders(request.getHeaders());

            Flux<ByteBuf> byteBufObservable = Flux.empty();

            if (request.getContentObservable() != null) {
                byteBufObservable = request.getContentObservable().map(Unpooled::wrappedBuffer);
            } else if (request.getContent() != null){
                byteBufObservable = Flux.just(Unpooled.wrappedBuffer(request.getContent()));
            }


            HttpRequest httpRequest = new HttpRequest(method,
                    uri,
                    uri.getPort(),
                    httpHeaders,
                    byteBufObservable);

            Mono<HttpResponse> httpResponseMono = this.httpClient.send(httpRequest);

            return toDocumentServiceResponse(httpResponseMono, request);

        } catch (Exception e) {
            return Flux.error(e);
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
                rootUri = this.globalEndpointManager.getWriteEndpoints().get(0).toURI();
            } else {
                rootUri = this.globalEndpointManager.resolveServiceEndpoint(request).toURI();
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
            return path;
        }

        if (path.startsWith("/")) {
            return path;
        }

        return "/" + path;
    }

    private Mono<String> toString(Flux<ByteBuf> contentObservable) {
        return contentObservable
            .reduce(
                new ByteArrayOutputStream(INITIAL_RESPONSE_BUFFER_SIZE),
                (out, bb) -> {
                    try {
                        bb.readBytes(out, bb.readableBytes());
                        return out;
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
            .map(out -> new String(out.toByteArray(), StandardCharsets.UTF_8));
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
     * @return {@link Flux}
     */
    private Flux<RxDocumentServiceResponse> toDocumentServiceResponse(Mono<HttpResponse> httpResponseMono,
                                                                            RxDocumentServiceRequest request) {

        if (request.getIsMedia()) {
            return httpResponseMono.flatMap(httpResponse -> {

                // header key/value pairs
                HttpHeaders httpResponseHeaders = httpResponse.headers();
                int httpResponseStatus = httpResponse.statusCode();

                Flux<InputStream> inputStreamObservable;

                if (request.getOperationType() == OperationType.Delete) {
                    // for delete we don't expect any body
                    inputStreamObservable = Flux.just(IOUtils.toInputStream("", StandardCharsets.UTF_8));
                } else {
                    // transforms the ByteBufFlux to Flux<InputStream>
                    inputStreamObservable = httpResponse
                            .body()
                            .flatMap(byteBuf ->
                                    Flux.just(IOUtils.toInputStream(byteBuf.toString(StandardCharsets.UTF_8), StandardCharsets.UTF_8)));
                }

                return inputStreamObservable
                        .flatMap(contentInputStream -> {
                            try {
                                // If there is any error in the header response this throws exception
                                // TODO: potential performance improvement: return Observable.error(exception) on failure instead of throwing Exception
                                validateOrThrow(request,
                                        HttpResponseStatus.valueOf(httpResponseStatus),
                                        httpResponseHeaders,
                                        null,
                                        contentInputStream);

                                // transforms to Observable<StoreResponse>
                                StoreResponse rsp = new StoreResponse(httpResponseStatus, HttpUtils
                                        .unescape(httpResponseHeaders.toMap().entrySet()), contentInputStream);
                                return Flux.just(rsp);
                            } catch (Exception e) {
                                return Flux.error(e);
                            }
                        }).single();

            }).map(RxDocumentServiceResponse::new).flux();

        } else {
            return httpResponseMono.flatMap(httpResponse ->  {

                // header key/value pairs
                HttpHeaders httpResponseHeaders = httpResponse.headers();
                int httpResponseStatus = httpResponse.statusCode();

                Flux<String> contentObservable;

                if (request.getOperationType() == OperationType.Delete) {
                    // for delete we don't expect any body
                    contentObservable = Flux.just(StringUtils.EMPTY);
                } else {
                    // transforms the ByteBufFlux to Flux<String>
                    contentObservable = toString(httpResponse.body()).flux();
                }

                return contentObservable
                        .flatMap(content -> {
                            try {
                                // If there is any error in the header response this throws exception
                                // TODO: potential performance improvement: return Observable.error(exception) on failure instead of throwing Exception
                                validateOrThrow(request, HttpResponseStatus.valueOf(httpResponseStatus), httpResponseHeaders, content, null);

                                // transforms to Observable<StoreResponse>
                                StoreResponse rsp = new StoreResponse(httpResponseStatus,
                                        HttpUtils.unescape(httpResponseHeaders.toMap().entrySet()),
                                        content);
                                return Flux.just(rsp);
                            } catch (Exception e) {
                                return Flux.error(e);
                            }
                        }).single();

            }).map(RxDocumentServiceResponse::new)
                    .onErrorResume(throwable -> {
                        if (!(throwable instanceof Exception)) {
                            // fatal error
                            logger.error("Unexpected failure {}", throwable.getMessage(), throwable);
                            return Mono.error(throwable);
                        }

                        Exception exception = (Exception) throwable;
                        if (!(exception instanceof CosmosClientException)) {
                            // wrap in CosmosClientException
                            logger.error("Network failure", exception);
                            CosmosClientException dce = BridgeInternal.createCosmosClientException(0, exception);
                            BridgeInternal.setRequestHeaders(dce, request.getHeaders());
                            return Mono.error(dce);
                        }

                        return Mono.error(exception);
            }).flux();
        }
    }

    private void validateOrThrow(RxDocumentServiceRequest request, HttpResponseStatus status, HttpHeaders headers, String body,
                                 InputStream inputStream) throws CosmosClientException {

        int statusCode = status.code();

        if (statusCode >= HttpConstants.StatusCodes.MINIMUM_STATUSCODE_AS_ERROR_GATEWAY) {
            if (body == null && inputStream != null) {
                try {
                    body = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    logger.error("Failed to get content from the http response", e);
                    CosmosClientException dce = BridgeInternal.createCosmosClientException(0, e);
                    BridgeInternal.setRequestHeaders(dce, request.getHeaders());
                    throw dce;
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }

            String statusCodeString = status.reasonPhrase() != null
                    ? status.reasonPhrase().replace(" ", "")
                    : "";
            CosmosError cosmosError;
            cosmosError = (StringUtils.isNotEmpty(body)) ? BridgeInternal.createCosmosError(body) : new CosmosError();
            cosmosError = new CosmosError(statusCodeString,
                    String.format("%s, StatusCode: %s", cosmosError.getMessage(), statusCodeString),
                    cosmosError.getPartitionedQueryExecutionInfo());

            CosmosClientException dce = BridgeInternal.createCosmosClientException(statusCode, cosmosError, headers.toMap());
            BridgeInternal.setRequestHeaders(dce, request.getHeaders());
            throw dce;
        }
    }

    private Flux<RxDocumentServiceResponse> invokeAsyncInternal(RxDocumentServiceRequest request)  {
        switch (request.getOperationType()) {
            case Create:
                return this.doCreate(request);
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
                return this.query(request);
            default:
                throw new IllegalStateException("Unknown operation type " + request.getOperationType());
        }
    }

    private Flux<RxDocumentServiceResponse> invokeAsync(RxDocumentServiceRequest request) {
        Callable<Mono<RxDocumentServiceResponse>> funcDelegate = () -> invokeAsyncInternal(request).single();
        return BackoffRetryUtility.executeRetry(funcDelegate, new WebExceptionRetryPolicy()).flux();
    }

    @Override
    public Flux<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        this.applySessionToken(request);

        Flux<RxDocumentServiceResponse> responseObs = invokeAsync(request);

        return responseObs.onErrorResume(
                e -> {
                    CosmosClientException dce = Utils.as(e, CosmosClientException.class);

                    if (dce == null) {
                        logger.error("unexpected failure {}", e.getMessage(), e);
                        return Flux.error(e);
                    }

                    if ((!ReplicatedResourceClientUtils.isMasterResource(request.getResourceType())) &&
                            (dce.statusCode() == HttpConstants.StatusCodes.PRECONDITION_FAILED ||
                                    dce.statusCode() == HttpConstants.StatusCodes.CONFLICT ||
                                    (
                                            dce.statusCode() == HttpConstants.StatusCodes.NOTFOUND &&
                                                    !Exceptions.isSubStatusCode(dce,
                                                            HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)))) {
                        this.captureSessionToken(request, dce.responseHeaders());
                    }

                    return Flux.error(dce);
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

        if (headers != null &&
                !Strings.isNullOrEmpty(request.getHeaders().get(HttpConstants.HttpHeaders.SESSION_TOKEN))) {
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