// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdFramer;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdResponse;
import com.azure.cosmos.implementation.http.HttpClient;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.routing.HexConvert;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 *
 * Used internally to provide functionality to communicate and process response from THINCLIENT in the Azure Cosmos DB database service.
 */
public class ThinClientStoreModel extends RxGatewayStoreModel {

    private static final Logger logger = LoggerFactory.getLogger(ThinClientStoreModel.class);

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
    public URI getRootUri(RxDocumentServiceRequest request) {
        // need to have thin client endpoint here
        var uri = this.globalEndpointManager.resolveServiceEndpoint(request).getGatewayRegionalEndpoint();
        // TODO: @nehrao1 remove before check-in, leaving here for now for context
        // return URI.create("https://57.155.105.105:10650/"); // https://chukangzhongstagesignoff-eastus2.documents-staging.windows-ppe.net:10650/
    }

    @Override
    public StoreResponse unwrapToStoreResponse(RxDocumentServiceRequest request, int statusCode, HttpHeaders headers, ByteBuf content) {
        if (content == null || content.readableBytes() == 0) {
            return super.unwrapToStoreResponse(request, statusCode, headers, Unpooled.EMPTY_BUFFER);
        }

        Instant decodeStartTime = Instant.now();

        if (RntbdFramer.canDecodeHead(content)) {

            final RntbdResponse response = RntbdResponse.decode(content);

            if (response != null) {
                response.setDecodeEndTime(Instant.now());
                response.setDecodeStartTime(decodeStartTime);

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

        // todo - neharao1 - validate b/w name() v/s toString()
        request.setThinclientHeaders(request.getOperationType().name(), request.getResourceType().name());

        byte[] epk = request.getPartitionKeyInternal().getEffectivePartitionKeyBytes(request.getPartitionKeyInternal(), request.getPartitionKeyDefinition());
        if (request.properties == null) {
            request.properties = new HashMap<>();
        }
        //request.properties.put(EFFECTIVE_PARTITION_KEY, epk);
        //request.properties.put(HttpConstants.HttpHeaders.GLOBAL_DATABASE_ACCOUNT_NAME, "chukangzhongstagesignoff");
        request.getHeaders().put(HttpConstants.HttpHeaders.GLOBAL_DATABASE_ACCOUNT_NAME, "tiagonapoli-cdb-test"); // "chukangzhongstagesignoff"
        request.getHeaders().put(WFConstants.BackendHeaders.COLLECTION_RID, "cLklAJU8SN0=");
        // todo - neharao1: no concept of a replica / service endpoint that can be passed
        RntbdRequestArgs rntbdRequestArgs = new RntbdRequestArgs(request);

        // todo - neharao1: validate what HTTP headers are needed - for now have put default ThinClient HTTP headers
        // todo - based on fabianm comment - thinClient also takes op type and resource type headers as HTTP headers
        HttpHeaders headers = this.getHttpHeaders();

        RntbdRequest rntbdRequest = RntbdRequest.from(rntbdRequestArgs);
        boolean success = rntbdRequest.setHeaderValue(
            RntbdConstants.RntbdRequestHeader.EffectivePartitionKey,
            epk);
        if (!success) {
            logger.error("Failed to update EPK to value {}", HexConvert.bytesToHex(epk));
        } else {
            logger.error("Updated EPK to value {}", HexConvert.bytesToHex(epk));
        }
        // todo: neharao1 - validate whether Java heap buffer is okay v/s Direct buffer
        // todo: eventually need to use pooled buffer
        ByteBuf byteBuf = Unpooled.buffer();

        logger.error("HEADERS: {}", rntbdRequest.getHeaders().dumpTokens());

        // todo: lifting the logic from there to encode the RntbdRequest instance into a ByteBuf (ByteBuf is a network compatible format)
        // todo: double-check with fabianm to see if RntbdRequest across RNTBD over TCP (Direct connectivity mode) is same as that when using ThinClient proxy
        // todo: need to conditionally add some headers (userAgent, replicaId/endpoint, etc)
        rntbdRequest.encode(byteBuf, true);

        byte[] contentAsByteArray = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, contentAsByteArray, 0, byteBuf.writerIndex());

        try {
            Files.write(java.nio.file.Paths.get("E:\\Temp\\java" + UUID.randomUUID() + ".bin"), contentAsByteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HttpRequest(
            HttpMethod.POST,
            //requestUri,
            //https://thinclient-performancetests-eastus2.documents-staging.windows-ppe.net:10650
            //https://cdb-ms-stage-eastus2-fe2-sql.eastus2.cloudapp.azure.com:10650
            //https://57.155.105.105:10650/
            // https://tiagonapoli-cdb-test-westus3.documents.azure.com:10650
            URI.create("https://57.155.105.105:10650/"), // https://127.0.0.1:10650/ //https://chukangzhongstagesignoff-eastus2.documents-staging.windows-ppe.net:10650/ // thinclient-performancetests-eastus2.documents-staging.windows-ppe.net  cdb-ms-stage-eastus2-fe2-sql.eastus2.cloudapp.azure.com
            //requestUri.getPort(),
            10650,
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
