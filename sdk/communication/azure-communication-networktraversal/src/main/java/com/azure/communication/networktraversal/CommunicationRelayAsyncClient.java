// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.implementation.CommunicationNetworkTraversalClientImpl;
import com.azure.communication.networktraversal.implementation.CommunicationNetworkTraversalsImpl;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationRelayConfigurationRequest;
import com.azure.communication.networktraversal.implementation.models.CommunicationErrorResponseException;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Context;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Asynchronous client interface for Azure Communication Network Traversal
 * operations
 */
@ServiceClient(builder = CommunicationRelayClientBuilder.class, isAsync = true)
public final class CommunicationRelayAsyncClient {

    private final CommunicationNetworkTraversalsImpl client;
    private final ClientLogger logger = new ClientLogger(CommunicationRelayAsyncClient.class);

    CommunicationRelayAsyncClient(CommunicationNetworkTraversalClientImpl communicationNetworkingClient) {
        client = communicationNetworkingClient.getCommunicationNetworkTraversals();
    }

    /**
     * Gets a Relay Configuration.
     *
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration() {
        GetRelayConfigurationOptions options = new GetRelayConfigurationOptions();
        return this.getRelayConfigurationWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier.
     *
     * @param options of the GetRelayConfigurationOptions request
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(GetRelayConfigurationOptions options) {
        return this.getRelayConfigurationWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier given a RouteType with response.
     *
     * @param options of the GetRelayConfigurationOptions request
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration.
     */
    Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(GetRelayConfigurationOptions options, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            CommunicationRelayConfigurationRequest body = new CommunicationRelayConfigurationRequest();
            if (options != null) {
                if (options.getCommunicationUserIdentifier() != null) {
                    body.setId(options.getCommunicationUserIdentifier().getId());
                }

                if (options.getRouteType() != null) {
                    body.setRouteType(options.getRouteType());
                }
            }
            return client.issueRelayConfigurationWithResponseAsync(body, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> e);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
