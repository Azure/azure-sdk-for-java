// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
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
    }

    public ThinClientStoreModel(ThinClientStoreModel inner) {
        super(inner);
    }

    @Override
    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        return super.processMessage(request);
    }

    @Override
    protected Map<String, String> getDefaultHeaders(
        ApiType apiType,
        UserAgentContainer userAgentContainer,
        ConsistencyLevel clientDefaultConsistencyLevel) {

        checkNotNull(userAgentContainer, "Argument 'userAGentContainer' must not be null.");

        Map<String, String> defaultHeaders = new HashMap<>();
        // For ThinClient http/2 used for framing only
        // All operation-level headers are only added to the rntbd-encoded message
        // the thin client proxy will parse the rntbd headers (not the content!) and substitute any
        // missing headers for routing (like partitionId or replicaId)
        // Since the Thin client proxy also needs to set the user-agent header to a different value
        // it is not added to the rntbd headers - just http-headers in the SDK
        defaultHeaders.put(HttpConstants.HttpHeaders.USER_AGENT, userAgentContainer.getUserAgent());

        return defaultHeaders;
    }

    @Override
    public HttpRequest wrapInHttpRequest(RxDocumentServiceRequest request, URI requestUri) throws Exception {

        // todo - neharao1 - validate b/w name() v/s toString()
        request.setThinclientHeaders(request.getOperationType().name(), request.getResourceType().name());

        // todo - neharao1: no concept of a replica / service endpoint that can be passed
        RntbdRequestArgs rntbdRequestArgs = new RntbdRequestArgs(request);

        // todo - neharao1: validate what HTTP headers are needed - for now have put default ThinClient HTTP headers
        // todo - based on fabianm comment - thinClient also takes op type and resource type headers as HTTP headers
        HttpHeaders headers = this.getHttpHeaders();

        RntbdRequest rntbdRequest = RntbdRequest.from(rntbdRequestArgs);

        // todo: neharao1 - validate whether Java heap buffer is okay v/s Direct buffer
        // todo: eventually need to use pooled buffer
        ByteBuf byteBuf = Unpooled.buffer();

        // todo: comment can be removed - RntbdRequestEncoder does the same - a type of ChannelHandler in ChannelPipeline (a Netty concept)
        // todo: lifting the logic from there to encode the RntbdRequest instance into a ByteBuf (ByteBuf is a network compatible format)
        // todo: double-check with fabianm to see if RntbdRequest across RNTBD over TCP (Direct connectivity mode) is same as that when using ThinClient proxy
        // todo: need to conditionally add some headers (userAgent, replicaId/endpoint, etc)
        rntbdRequest.encode(byteBuf);

        return new HttpRequest(
            HttpMethod.POST,
            requestUri,
            requestUri.getPort(),
            headers,
            Flux.just(byteBuf.array()));
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        // todo: select only required headers from defaults
        Map<String, String> defaultHeaders = this.getDefaultHeaders();

        for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
            httpHeaders.set(header.getKey(), header.getValue());
        }

        // todo: add thin client resourcetype/operationtype headers
        return httpHeaders;
    }
}
