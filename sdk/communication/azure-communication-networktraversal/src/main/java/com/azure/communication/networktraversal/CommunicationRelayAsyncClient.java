// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.implementation.CommunicationNetworkingClientImpl;
import com.azure.communication.networktraversal.implementation.CommunicationNetworkTraversalsImpl;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationRelayConfigurationRequest;
import com.azure.communication.networktraversal.models.CommunicationErrorResponseException;
import com.azure.communication.networktraversal.models.RouteType;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.Context;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous client interface for Azure Communication Network Traversal
 * operations
 */
@ServiceClient(builder = CommunicationRelayClientBuilder.class, isAsync = true)
public final class CommunicationRelayAsyncClient {

    private final CommunicationNetworkTraversalsImpl client;
    private final ClientLogger logger = new ClientLogger(CommunicationRelayAsyncClient.class);

    CommunicationRelayAsyncClient(CommunicationNetworkingClientImpl communicationNetworkingClient) {
        client = communicationNetworkingClient.getCommunicationNetworkTraversals();
    }

    /**
     * Gets a Relay Configuration.
     *
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration() {
        return this.getRelayConfigurationWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(CommunicationUserIdentifier communicationUser) {
        return this.getRelayConfigurationWithResponse(communicationUser, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a Relay Configuration given a RouteType.
     *
     * @param routeType The specified RouteType for the relay request
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(RouteType routeType) {
        return this.getRelayConfigurationWithResponse(null, routeType, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier given a RouteType.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param routeType The specified RouteType for the relay request
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(CommunicationUserIdentifier communicationUser, RouteType routeType) {
        return this.getRelayConfigurationWithResponse(communicationUser, routeType, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a Relay Configuration with response.
     *
     * @return The obtained Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse() {
        return withContext(context -> getRelayConfigurationWithResponse(null, null, context));
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier given a RouteType with response.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param routeType The specified RouteType for the relay request
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration.
     */
    Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(CommunicationUserIdentifier communicationUser, RouteType routeType, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            CommunicationRelayConfigurationRequest body = new CommunicationRelayConfigurationRequest();

            if (communicationUser != null) {
                body.setId(communicationUser.getId());
            }

            if (routeType != null) {
                body.setRouteType(routeType);
            }

            return client.issueRelayConfigurationWithResponseAsync(body, context)
                .onErrorMap(CommunicationErrorResponseException.class, e -> e);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
