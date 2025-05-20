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
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
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
    private final static Logger logger = LoggerFactory.getLogger(ThinClientStoreModel.class);

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

    @Override
    public Mono<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        logger.info("entered ThinClientStoreModel.processMessage");
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
        logger.info("entered ThinClientStoreModel.wrapInHttpRequest");
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
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.StartEpkHash, pkRange.getMinInclusive().getBytes(StandardCharsets.UTF_8));
            rntbdRequest.setHeaderValue(RntbdConstants.RntbdRequestHeader.EndEpkHash, pkRange.getMaxExclusive().getBytes(StandardCharsets.UTF_8));
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
