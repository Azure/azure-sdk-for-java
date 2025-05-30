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
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Used internally to provide functionality to communicate and process response from THINCLIENT in the Azure Cosmos DB database service.
 */
public class ThinClientStoreModel extends RxGatewayStoreModel {
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
    public StoreResponse unwrapToStoreResponse(RxDocumentServiceRequest request, int statusCode, HttpHeaders headers, ByteBuf content) {
        if (content == null || content.readableBytes() == 0) {
            return super.unwrapToStoreResponse(request, statusCode, headers, Unpooled.EMPTY_BUFFER);
        }
        if (RntbdFramer.canDecodeHead(content)) {

            final RntbdResponse response = RntbdResponse.decode(content);

            if (response != null) {
                return super.unwrapToStoreResponse(
                    request,
                    response.getStatus().code(),
                    new HttpHeaders(response.getHeaders().asMap(request.getActivityId())),
                    response.getContent()
                );
            }

            return super.unwrapToStoreResponse(request, statusCode, headers, null);
        }

        throw new IllegalStateException("Invalid rntbd response");
    }

    @Override
    public HttpRequest wrapInHttpRequest(RxDocumentServiceRequest request, URI requestUri) throws Exception {
        if (this.globalDatabaseAccountName == null) {
            this.globalDatabaseAccountName = this.globalEndpointManager.getLatestDatabaseAccount().getId();
        }
        // todo - neharao1 - validate b/w name() v/s toString()
        request.setThinclientHeaders(
            request.getOperationType().name(),
            request.getResourceType().name(),
            this.globalDatabaseAccountName,
            request.getResourceId());

        byte[] epk = request.getPartitionKeyInternal().getEffectivePartitionKeyBytes(request.getPartitionKeyInternal(), request.getPartitionKeyDefinition());
        RntbdRequestArgs rntbdRequestArgs = new RntbdRequestArgs(request);

        HttpHeaders headers = this.getHttpHeaders();
        headers.set(HttpConstants.HttpHeaders.ACTIVITY_ID, request.getActivityId().toString());

        RntbdRequest rntbdRequest = RntbdRequest.from(rntbdRequestArgs);
        rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey, epk);

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
