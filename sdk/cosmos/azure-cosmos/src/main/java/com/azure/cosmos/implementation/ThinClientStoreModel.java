// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

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
import com.azure.cosmos.implementation.routing.HexConvert;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ResourceLeakDetector;
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
    private static final boolean leakDetectionDebuggingEnabled = ResourceLeakDetector.getLevel().ordinal() >=
        ResourceLeakDetector.Level.ADVANCED.ordinal();

    private String globalDatabaseAccountName = null;
    private final Map<String, String> defaultHeaders;

    public ThinClientStoreModel(
        DiagnosticsClientContext clientContext,
        ISessionContainer sessionContainer,
        ConsistencyLevel defaultConsistencyLevel,
        UserAgentContainer userAgentContainer,
        GlobalEndpointManager globalEndpointManager,
        HttpClient httpClient) {
        super(
            clientContext,
            sessionContainer,
            defaultConsistencyLevel,
            QueryCompatibilityMode.Default,
            userAgentContainer,
            globalEndpointManager,
            httpClient,
            ApiType.SQL);

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
        ByteBuf content) throws Exception {

        if (content == null) {
            return super.unwrapToStoreResponse(endpoint, request, statusCode, headers, Unpooled.EMPTY_BUFFER);
        }

        if (content.readableBytes() == 0) {

            content.release();
            return super.unwrapToStoreResponse(endpoint, request, statusCode, headers, Unpooled.EMPTY_BUFFER);
        }

        if (leakDetectionDebuggingEnabled) {
            content.touch(this);
        }
        if (RntbdFramer.canDecodeHead(content)) {

            final RntbdResponse response = RntbdResponse.decode(content);

            if (response != null) {
                ByteBuf payloadBuf = response.getContent();

                if (payloadBuf != Unpooled.EMPTY_BUFFER && leakDetectionDebuggingEnabled) {
                    content.touch(this);
                }

                StoreResponse storeResponse = super.unwrapToStoreResponse(
                    endpoint,
                    request,
                    response.getStatus().code(),
                    new HttpHeaders(response.getHeaders().asMap(request.getActivityId())),
                    payloadBuf
                );

                if (payloadBuf == Unpooled.EMPTY_BUFFER) {
                    // means the original RNTBD payload did not have any payload - so, we can release it
                    content.release();
                }

                return storeResponse;
            }

            content.release();
            return super.unwrapToStoreResponse(endpoint, request, statusCode, headers, null);
        }

        content.release();
        throw new IllegalStateException("Invalid rntbd response");
    }

    @Override
    protected boolean partitionKeyRangeResolutionNeeded(RxDocumentServiceRequest request) {
        return request.getPartitionKeyInternal() == null
            && request.requestContext.resolvedPartitionKeyRange == null
            && request.getPartitionKeyRangeIdentity() != null;
    }
    @Override
    public HttpRequest wrapInHttpRequest(RxDocumentServiceRequest request, URI requestUri) throws Exception {
        if (this.globalDatabaseAccountName == null) {
            this.globalDatabaseAccountName = this.globalEndpointManager.getLatestDatabaseAccount().getId();
        }
        // todo - neharao1 - validate b/w name() v/s toString()
        request.setThinclientHeaders(
            request.getOperationType(),
            request.getResourceType(),
            this.globalDatabaseAccountName,
            request.getResourceId());

        if (request.properties == null) {
            request.properties = new HashMap<>();
        }

        RntbdRequestArgs rntbdRequestArgs = new RntbdRequestArgs(request);

        HttpHeaders headers = this.getHttpHeaders();
        headers.set(HttpConstants.HttpHeaders.ACTIVITY_ID, request.getActivityId().toString());

        RntbdRequest rntbdRequest = RntbdRequest.from(rntbdRequestArgs);

        PartitionKeyInternal partitionKey = request.getPartitionKeyInternal();

        if (partitionKey != null) {
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

        rntbdRequest.encode(byteBuf, true);

        byte[] contentAsByteArray = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, contentAsByteArray, 0, byteBuf.writerIndex());

        return new HttpRequest(
            HttpMethod.POST,
            requestUri,
            requestUri.getPort(),
            headers,
            Flux.just(contentAsByteArray));
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
