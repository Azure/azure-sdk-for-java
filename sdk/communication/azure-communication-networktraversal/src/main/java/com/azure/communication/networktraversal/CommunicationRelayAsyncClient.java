// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.implementation.CommunicationNetworkingClientImpl;
import com.azure.communication.networktraversal.implementation.CommunicationNetworkTraversalsImpl;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationRelayConfigurationRequest;
import com.azure.communication.networktraversal.models.CommunicationErrorResponseException;
import com.azure.communication.networktraversal.models.CommunicationRelayConfigurationRequestRouteType;
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
     * Creates a new CommunicationRelayConfiguration.
     *
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration() {
        return this.getRelayConfigurationWithResponse().flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new CommunicationRelayConfiguration.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(CommunicationUserIdentifier communicationUser) {
        return this.getRelayConfigurationWithResponse(communicationUser).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new CommunicationRelayConfiguration.
     *
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(CommunicationRelayConfigurationRequestRouteType routeType) {
        return this.getRelayConfigurationWithResponse(routeType).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new CommunicationRelayConfiguration.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(CommunicationUserIdentifier communicationUser, CommunicationRelayConfigurationRequestRouteType routeType) {
        return this.getRelayConfigurationWithResponse(communicationUser, routeType).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse() {
        return withContext(context -> getRelayConfigurationWithResponse(null, null, context));
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(CommunicationUserIdentifier communicationUser) {
        return withContext(context -> getRelayConfigurationWithResponse(communicationUser, null, context));
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(CommunicationRelayConfigurationRequestRouteType routeType) {
        return withContext(context -> getRelayConfigurationWithResponse(null, routeType, context));
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(CommunicationUserIdentifier communicationUser, CommunicationRelayConfigurationRequestRouteType routeType) {
        return withContext(context -> getRelayConfigurationWithResponse(communicationUser, routeType, context));
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @param context A {@link Context} representing the request context.
     * @return The created Communication Relay Configuration.
     */
    Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(CommunicationUserIdentifier communicationUser, CommunicationRelayConfigurationRequestRouteType routeType, Context context) {
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
