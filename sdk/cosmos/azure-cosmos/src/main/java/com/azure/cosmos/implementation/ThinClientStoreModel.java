// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdFramer;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdResponse;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.caches.RxClientCollectionCache;
import com.azure.cosmos.implementation.routing.CollectionRoutingMap;
import com.azure.cosmos.implementation.routing.HexConvert;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyRangeIdentity;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.PartitionKind;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ResourceLeakDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Used internally to provide functionality to communicate and process response from THINCLIENT in the Azure Cosmos DB database service.
 */
public class ThinClientStoreModel extends RxGatewayStoreModel {
    private static final Logger logger = LoggerFactory.getLogger(ThinClientStoreModel.class);
    private static final boolean leakDetectionDebuggingEnabled = ResourceLeakDetector.getLevel().ordinal() >=
        ResourceLeakDetector.Level.ADVANCED.ordinal();

    private volatile String globalDatabaseAccountName = null;
    private final Map<String, String> defaultHeaders;
    private RxGatewayStoreModel gatewayStoreModel;

    public ThinClientStoreModel(
        DiagnosticsClientContext clientContext,
        ISessionContainer sessionContainer,
        ConsistencyLevel defaultConsistencyLevel,
        UserAgentContainer userAgentContainer,
        GlobalEndpointManager globalEndpointManager,
        HttpClient httpClient,
        Map<String, String> additionalHeaders) {
        this(clientContext, sessionContainer, defaultConsistencyLevel, userAgentContainer,
            globalEndpointManager, httpClient, additionalHeaders, null);
    }

    public ThinClientStoreModel(
        DiagnosticsClientContext clientContext,
        ISessionContainer sessionContainer,
        ConsistencyLevel defaultConsistencyLevel,
        UserAgentContainer userAgentContainer,
        GlobalEndpointManager globalEndpointManager,
        HttpClient httpClient,
        Map<String, String> additionalHeaders,
        CosmosClientTelemetryConfig clientTelemetryConfig) {
        super(
            clientContext,
            sessionContainer,
            defaultConsistencyLevel,
            QueryCompatibilityMode.Default,
            userAgentContainer,
            globalEndpointManager,
            httpClient,
            ApiType.SQL,
            additionalHeaders,
            clientTelemetryConfig);

        String userAgent = userAgentContainer != null
            ? userAgentContainer.getUserAgent()
            : UserAgentContainer.BASE_USER_AGENT_STRING;

        this.defaultHeaders = Collections.singletonMap(
            HttpConstants.HttpHeaders.USER_AGENT, userAgent
        );
    }

    @Override
    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        return Mono.defer(() -> {
            request.captureOriginalSessionToken();
            String originalToken = request.getOriginalSessionToken();
            request.useThinClientMode = true;
            if (originalToken == null || !originalToken.contains(",") || !shouldApplySessionToken(request)) {
                return super.processMessage(request);
            }

            request.requestContext.sessionToken = null;
            if (getCollectionCache() == null || getPartitionKeyRangeCache() == null) {
                return processWithGateway(request);
            }

            MetadataDiagnosticsContext diagnostics =
                BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics);
            return getCollectionCache().resolveCollectionAsync(diagnostics, request).flatMap(collectionHolder -> {
                if (collectionHolder.v == null) {
                    return processWithGateway(request);
                }
                DocumentCollection collection = collectionHolder.v;
                return getPartitionKeyRangeCache()
                    .tryLookupAsync(diagnostics, collection.getResourceId(), null, request.properties)
                    .flatMap(mapHolder -> {
                        PartitionKeyRange target = resolveSessionTokenRange(request, collection, mapHolder.v);
                        ISessionToken scoped =
                            SessionTokenHelper.tryScopeExplicitSessionToken(originalToken, target, mapHolder.v);
                        if (scoped == null) {
                            return processWithGateway(request);
                        }
                        request.requestContext.resolvedPartitionKeyRange = target;
                        request.requestContext.sessionToken = scoped;
                        return super.processMessage(request);
                    });
            });
        });
    }

    void setGatewayStoreModel(RxGatewayStoreModel gatewayStoreModel) {
        this.gatewayStoreModel = gatewayStoreModel;
    }

    private Mono<RxDocumentServiceResponse> processWithGateway(RxDocumentServiceRequest request) {
        logger.debug("Using classic Gateway because explicit compound session progress cannot be safely scoped");
        request.useThinClientMode = false;
        return Objects.requireNonNull(this.gatewayStoreModel, "Classic Gateway store is required for session fallback")
            .processMessage(request);
    }

    private static PartitionKeyRange resolveSessionTokenRange(
        RxDocumentServiceRequest request, DocumentCollection collection, CollectionRoutingMap routingMap) {
        if (routingMap == null || !collection.getResourceId().equals(routingMap.getCollectionUniqueId())
            || request.getOperationType() == OperationType.QueryPlan) {
            return null;
        }

        PartitionKeyRangeIdentity identity = request.getPartitionKeyRangeIdentity();
        if (identity != null && identity.getCollectionRid() != null
            && !identity.getCollectionRid().equals(collection.getResourceId())) {
            return null;
        }
        String rangeId = identity == null ? null : identity.getPartitionKeyRangeId();
        String rangeHeader = request.getHeaders().get(HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID);
        if (!Strings.isNullOrEmpty(rangeHeader)) {
            String[] parts = rangeHeader.split(",", -1);
            if (parts.length > 2 || parts[parts.length - 1].isEmpty()
                || (parts.length == 2 && !parts[0].equals(collection.getResourceId()))
                || (rangeId != null && !rangeId.equals(parts[parts.length - 1]))) {
                return null;
            }
            rangeId = parts[parts.length - 1];
        }

        PartitionKeyRange target = rangeId == null ? null : routingMap.tryGetRangeByPartitionKeyRangeId(rangeId);
        if (rangeId != null && target == null) {
            return null;
        }

        String start = request.getHeaders().get(HttpConstants.HttpHeaders.START_EPK);
        String end = request.getHeaders().get(HttpConstants.HttpHeaders.END_EPK);
        PartitionKeyInternal key = request.getPartitionKeyInternal();
        PartitionKeyRange scopeTarget = null;
        if (start != null || end != null) {
            if (start == null || end == null || start.compareTo(end) >= 0
                || !ReadFeedKeyType.EffectivePartitionKeyRange.name().equalsIgnoreCase(
                    request.getHeaders().get(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE))) {
                return null;
            }
            List<PartitionKeyRange> overlapping =
                routingMap.getOverlappingRanges(new Range<>(start, end, true, false));
            if (overlapping.size() != 1) {
                return null;
            }
            scopeTarget = overlapping.get(0);
        } else if (key != null) {
            if (collection.getPartitionKey().getKind() == PartitionKind.MULTI_HASH
                && key.getComponents().size() < collection.getPartitionKey().getPaths().size()) {
                return null;
            }
            String epk = key.getEffectivePartitionKeyString(key, collection.getPartitionKey());
            scopeTarget = routingMap.getRangeByEffectivePartitionKey(epk);
        } else if (target == null && request.requestContext.resolvedPartitionKeyRange != null) {
            scopeTarget = routingMap.tryGetRangeByPartitionKeyRangeId(
                request.requestContext.resolvedPartitionKeyRange.getId());
        }
        if (target != null && scopeTarget != null && !target.getId().equals(scopeTarget.getId())) {
            return null;
        }
        return scopeTarget == null ? target : scopeTarget;
    }

    @Override
    protected void applyGatewayRetryWithHeaders(RxDocumentServiceRequest request) {
        // ThinClient does not use the Gateway V1 server-side 449 retry loop.
        request.getHeaders().remove(HttpConstants.HttpHeaders.NO_RETRY_449);
    }

    @Override
    protected Map<String, String> getDefaultHeaders(
        ApiType apiType,
        UserAgentContainer userAgentContainer) {

        // For ThinClient http/2 used for framing only
        // All operation-level headers are only added to the rntbd-encoded message
        // the thin client proxy will parse the rntbd headers (not the content!) and substitute any
        // missing headers for routing (like partitionId or replicaId)
        // Since the Thin client proxy also needs to set the user-agent header to a different value
        // it is not added to the rntbd headers - just http-headers in the SDK
        return this.defaultHeaders;
    }

    @Override
    public URI getRootUri(RxDocumentServiceRequest request) {
        // need to have thin client endpoint here
        return this.globalEndpointManager.resolveServiceEndpoint(request).getThinclientRegionalEndpoint();
    }

    @Override
    public StoreResponse unwrapToStoreResponse(
        String endpoint,
        RxDocumentServiceRequest request,
        int statusCode,
        HttpHeaders headers,
        ByteBuf content) {

        if (content == null) {
            return super.unwrapToStoreResponse(endpoint, request, statusCode, headers, Unpooled.EMPTY_BUFFER);
        }

        if (content.refCnt() == 0) {
            // ByteBuf was already released (e.g., stream RST due to responseTimeout on HTTP/2).
            // Treat as empty response to avoid IllegalReferenceCountException during decoding.
            logger.debug("Content ByteBuf already released (refCnt=0) in unwrapToStoreResponse, treating as empty");
            return super.unwrapToStoreResponse(endpoint, request, statusCode, headers, Unpooled.EMPTY_BUFFER);
        }

        if (content.readableBytes() == 0) {
            if (content.refCnt() > 0) {
                safeSilentRelease(content);
            }
            return super.unwrapToStoreResponse(endpoint, request, statusCode, headers, Unpooled.EMPTY_BUFFER);
        }

        if (leakDetectionDebuggingEnabled) {
            content.touch("ThinClientStoreModel.unwrapToStoreResponse - refCnt: " + content.refCnt());
        }

        try {
            if (RntbdFramer.canDecodeHead(content)) {

                final RntbdResponse response = RntbdResponse.decode(content);

                if (response != null) {
                    ByteBuf payloadBuf = response.getContent();

                    if (payloadBuf != Unpooled.EMPTY_BUFFER && leakDetectionDebuggingEnabled) {
                        payloadBuf.touch("ThinClientStoreModel.after RNTBD decoding - refCnt: " + payloadBuf.refCnt());
                    }

                    try {
                        StoreResponse storeResponse = super.unwrapToStoreResponse(
                            endpoint,
                            request,
                            response.getStatus().code(),
                            new HttpHeaders(response.getHeaders().asMap(request.getActivityId())),
                            payloadBuf
                        );

                        if (payloadBuf == Unpooled.EMPTY_BUFFER && content.refCnt() > 0) {
                            safeSilentRelease(content);
                        }

                        return storeResponse;
                    } catch (Throwable t) {
                        if (payloadBuf == Unpooled.EMPTY_BUFFER && content.refCnt() > 0) {
                            safeSilentRelease(content);
                        }

                        throw t;
                    }
                }

                if (content.refCnt() > 0) {
                    safeSilentRelease(content);
                }
                return super.unwrapToStoreResponse(endpoint, request, statusCode, headers, Unpooled.EMPTY_BUFFER);
            }

            if (content.refCnt() > 0) {
                safeSilentRelease(content);
            }
            throw new IllegalStateException("Invalid rntbd response");
        } catch (Throwable t) {
            // Ensure container is not leaked on any unexpected path
            if (content.refCnt() > 0) {
                safeSilentRelease(content);
            }
            throw t;
        }
    }

    @Override
    protected boolean partitionKeyRangeResolutionNeeded(RxDocumentServiceRequest request) {
        return request.getPartitionKeyInternal() == null
            && request.requestContext.resolvedPartitionKeyRange == null
            && request.getPartitionKeyRangeIdentity() != null;
    }

    @Override
    public Mono<RxDocumentServiceResponse> performRequestInternal(RxDocumentServiceRequest request, URI requestUri) {
        // Ensure partitionKeyDefinition is resolved from the collection cache before
        // reaching wrapInHttpRequest, which needs it for client-side EPK computation.
        // This handles cases where clone() or other code paths didn't propagate partitionKeyDefinition.
        if (request.getPartitionKeyInternal() != null && request.getPartitionKeyDefinition() == null) {
            RxClientCollectionCache cache = this.getCollectionCache();
            if (cache != null) {
                return cache
                    .resolveCollectionAsync(
                        BridgeInternal.getMetaDataDiagnosticContext(request.requestContext.cosmosDiagnostics),
                        request)
                    .flatMap(collectionHolder -> {
                        if (collectionHolder.v != null) {
                            request.setPartitionKeyDefinition(collectionHolder.v.getPartitionKey());
                        } else {
                            throw new NullPointerException(
                                "Collection cache returned null for request to "
                                    + request.getResourceAddress()
                                    + ". Cannot resolve partitionKeyDefinition for client-side EPK computation.");
                        }
                        return super.performRequestInternal(request, requestUri);
                    });
            }
        }

        return super.performRequestInternal(request, requestUri);
    }

    @Override
    public HttpRequest wrapInHttpRequest(RxDocumentServiceRequest request, URI requestUri) throws Exception {
        if (this.globalDatabaseAccountName == null) {
            this.globalDatabaseAccountName = this.globalEndpointManager.getLatestDatabaseAccount().getId();
        }

        request.setThinclientHeaders(
            request.getOperationType(),
            request.getResourceType(),
            this.globalDatabaseAccountName,
            request.getResourceId());

        if (request.properties == null) {
            request.properties = new HashMap<>();
        }

        PartitionKeyInternal partitionKey = request.getPartitionKeyInternal();

        RntbdRequestArgs rntbdRequestArgs = new RntbdRequestArgs(request);

        HttpHeaders headers = this.getHttpHeaders();
        headers.set(HttpConstants.HttpHeaders.ACTIVITY_ID, request.getActivityId().toString());

        RntbdRequest rntbdRequest = RntbdRequest.from(rntbdRequestArgs);

        String originalToken = request.getOriginalSessionToken();
        if (originalToken != null && originalToken.contains(",") && request.requestContext.sessionToken != null
            && shouldApplySessionToken(request)) {
            // Keep the caller's header intact so retries and hedges never inherit narrowed progress.
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.SessionToken,
                SessionTokenHelper.concatPartitionKeyRangeIdWithSessionToken(
                    request.requestContext.resolvedPartitionKeyRange.getId(),
                    request.requestContext.sessionToken.convertToString()));
        }

        String startEpk = request.getHeaders().get(HttpConstants.HttpHeaders.START_EPK);
        String endEpk = request.getHeaders().get(HttpConstants.HttpHeaders.END_EPK);
        String readFeedKeyType = request.getHeaders().get(HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE);
        if (request.getOperationType() == OperationType.QueryPlan) {
            // QueryPlan is collection-scoped on the thin-client proxy: it carries no
            // EffectivePartitionKey and no StartEpkHash/EndEpkHash headers - the proxy fans out
            // across partitions itself. Keep this explicit so the contract is self-documenting and
            // cannot be violated if a resolved partition key range is ever present on the request.
            //noinspection StatementWithEmptyBody
        } else if (startEpk != null && endEpk != null
            && ReadFeedKeyType.EffectivePartitionKeyRange.name().equalsIgnoreCase(readFeedKeyType)) {
            // The request already carries the effective-partition-key range as HTTP headers (set together by
            // FeedRangeEpkImpl#populateFeedRangeFilteringHeaders). This covers a partial (prefix)
            // hierarchical partition key query as well as any explicit EPK-range read. All three headers
            // (ReadFeedKeyType=EffectivePartitionKeyRange, StartEpk, EndEpk) are required for the backend to
            // interpret the range: RntbdRequestHeaders#addStartAndEndKeys reads each independently and only
            // emits the ReadFeedKeyType RNTBD token when READ_FEED_KEY_TYPE is present, so we guard on it here
            // as well to keep the StartEpk/EndEpk string tokens and the StartEpkHash/EndEpkHash binary tokens
            // coherent. The ReadFeedKeyType/StartEpk/EndEpk RNTBD string tokens are copied verbatim from those
            // HTTP headers by RntbdRequestHeaders#addStartAndEndKeys during RntbdRequest.from() above, so only
            // the StartEpkHash/EndEpkHash tokens (the decoded hash bytes) are additive here. They steer the
            // thin-client proxy to the owning physical partition(s); without them the proxy resolves the
            // request to the whole physical partition and returns every co-located document (an over-span).
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.StartEpkHash,
                HexConvert.hexToBytes(startEpk));
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.EndEpkHash,
                HexConvert.hexToBytes(endEpk));
        } else if (partitionKey != null) {
            byte[] epk = partitionKey.getEffectivePartitionKeyBytes(request.getPartitionKeyInternal(), request.getPartitionKeyDefinition());
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey, epk);
        } else if (request.requestContext.resolvedPartitionKeyRange == null) {
            throw new IllegalStateException(
                "Resolved partition key range should not be null at this point. ResourceType: "
                + request.getResourceType() + ", OperationType: "
                + request.getOperationType());
        } else {
            PartitionKeyRange pkRange = request.requestContext.resolvedPartitionKeyRange;
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.StartEpkHash, HexConvert.hexToBytes(pkRange.getMinInclusive()));
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.EndEpkHash, HexConvert.hexToBytes(pkRange.getMaxExclusive()));
        }

        // todo: eventually need to use pooled buffer
        ByteBuf byteBuf = Unpooled.buffer();
        try {
            rntbdRequest.encode(byteBuf, true);

            byte[] contentAsByteArray = ByteBufUtil.getBytes(byteBuf, 0, byteBuf.writerIndex(), false);

            return new HttpRequest(
                HttpMethod.POST,
                requestUri,
                requestUri.getPort(),
                headers,
                Flux.just(contentAsByteArray))
                .withThinClientRequest(true);
        } finally {
            if (byteBuf.refCnt() > 0) {
                safeSilentRelease(byteBuf);
            }
        }
    }

    @Override
    public Map<String, String> getDefaultHeaders() {
        return this.defaultHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        // todo: select only required headers from defaults
        Map<String, String> defaultHeaders = this.getDefaultHeaders();

        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            httpHeaders.set(header.getKey(), header.getValue());
        }

        return httpHeaders;
    }
}
