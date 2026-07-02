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
import com.azure.cosmos.implementation.routing.HexConvert;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.PartitionKeyDefinition;
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
import java.util.Map;

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

    public ThinClientStoreModel(
        DiagnosticsClientContext clientContext,
        ISessionContainer sessionContainer,
        ConsistencyLevel defaultConsistencyLevel,
        UserAgentContainer userAgentContainer,
        GlobalEndpointManager globalEndpointManager,
        HttpClient httpClient,
        Map<String, String> additionalHeaders) {
        super(
            clientContext,
            sessionContainer,
            defaultConsistencyLevel,
            QueryCompatibilityMode.Default,
            userAgentContainer,
            globalEndpointManager,
            httpClient,
            ApiType.SQL,
            additionalHeaders);

        String userAgent = userAgentContainer != null
            ? userAgentContainer.getUserAgent()
            : UserAgentContainer.BASE_USER_AGENT_STRING;

        this.defaultHeaders = Collections.singletonMap(
            HttpConstants.HttpHeaders.USER_AGENT, userAgent
        );
    }

    @Override
    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        return super.processMessage(request);
    }

    @Override
    protected void applyGatewayRetryWithHeaders(RxDocumentServiceRequest request) {
        // ThinClient does not use the Gateway V1 server-side 449 retry loop.
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

        // Detect a partial (prefix) hierarchical partition key BEFORE the RNTBD request is serialized.
        // For such a query the prefix's effective-partition-key sub-range
        // [hash(prefix), hash(prefix) + "FF") must reach the thin-client proxy as a doc-level EPK
        // filter. We convey it by setting StartEpk/EndEpk + ReadFeedKeyType=EffectivePartitionKeyRange
        // on the request headers here, so that RntbdRequest.from() ->
        // RntbdRequestHeaders.addStartAndEndKeys serializes them into the RNTBD request with the
        // correct encoding (the hex string taken as UTF-8 bytes, not the decoded hash) - exactly as
        // the Direct/RNTBD path does for the same query (see FeedRangeEpkImpl). The proxy forwards
        // these to the backend as the doc-level EPK filter (see TransportSerialization
        // AddStartAndEndKeysFromHeaders, where ReadFeedKeyType=EffectivePartitionKeyRange selects the
        // hex-string-as-bytes encoding). Without this filter the proxy resolves the request to the
        // owning physical partition and returns every co-located document (an over-span).
        Range<String> prefixEpkRange = null;
        if (request.getOperationType() != OperationType.QueryPlan && partitionKey != null) {
            PartitionKeyDefinition pkDefinition = request.getPartitionKeyDefinition();
            if (pkDefinition != null
                && pkDefinition.getKind() == PartitionKind.MULTI_HASH
                && partitionKey.getComponents().size() < pkDefinition.getPaths().size()) {
                prefixEpkRange = partitionKey.getEPKRangeForPrefixPartitionKey(pkDefinition);
                request.getHeaders().put(
                    HttpConstants.HttpHeaders.READ_FEED_KEY_TYPE,
                    ReadFeedKeyType.EffectivePartitionKeyRange.name());
                request.getHeaders().put(HttpConstants.HttpHeaders.START_EPK, prefixEpkRange.getMin());
                request.getHeaders().put(HttpConstants.HttpHeaders.END_EPK, prefixEpkRange.getMax());
            }
        }

        RntbdRequestArgs rntbdRequestArgs = new RntbdRequestArgs(request);

        HttpHeaders headers = this.getHttpHeaders();
        headers.set(HttpConstants.HttpHeaders.ACTIVITY_ID, request.getActivityId().toString());

        RntbdRequest rntbdRequest = RntbdRequest.from(rntbdRequestArgs);

        if (request.getOperationType() == OperationType.QueryPlan) {
            // QueryPlan is collection-scoped on the thin-client proxy: it carries no
            // EffectivePartitionKey and no StartEpkHash/EndEpkHash headers - the proxy fans out
            // across partitions itself. Keep this explicit so the contract is self-documenting and
            // cannot be violated if a resolved partition key range is ever present on the request.
            //noinspection StatementWithEmptyBody
        } else if (partitionKey != null) {
            if (prefixEpkRange != null) {
                // Partial (prefix) hierarchical partition key. StartEpk/EndEpk + ReadFeedKeyType were
                // already set on the request headers above and serialized into the RNTBD request as the
                // backend's doc-level EPK filter. StartEpkHash/EndEpkHash additionally steer the proxy to
                // the owning physical partition(s), mirroring the Direct/RNTBD path.
                rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.StartEpkHash, HexConvert.hexToBytes(prefixEpkRange.getMin()));
                rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.EndEpkHash, HexConvert.hexToBytes(prefixEpkRange.getMax()));
            } else {
                byte[] epk = partitionKey.getEffectivePartitionKeyBytes(request.getPartitionKeyInternal(), request.getPartitionKeyDefinition());
                rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey, epk);
            }
        } else if (request.requestContext.resolvedPartitionKeyRange != null) {
            PartitionKeyRange pkRange = request.requestContext.resolvedPartitionKeyRange;
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.StartEpkHash, HexConvert.hexToBytes(pkRange.getMinInclusive()));
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.EndEpkHash, HexConvert.hexToBytes(pkRange.getMaxExclusive()));
        } else {
            throw new IllegalStateException(
                "Resolved partition key range should not be null at this point. ResourceType: "
                    + request.getResourceType() + ", OperationType: "
                    + request.getOperationType());
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
