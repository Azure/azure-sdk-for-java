// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationRelayConfigurationRequestRouteType;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Synchronous client interface for Communication Service Networktraversal operations
 */
@ServiceClient(builder = CommunicationRelayClientBuilder.class, isAsync = false)
public final class CommunicationRelayClient {

    private final CommunicationRelayAsyncClient client;
    private final ClientLogger logger = new ClientLogger(CommunicationRelayClient.class);

    CommunicationRelayClient(CommunicationRelayAsyncClient communicationNetworkingClient) {
        client = communicationNetworkingClient;
    }

    /**
     * Creates a new CommunicationRelayConfiguration.
     *
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRelayConfiguration getRelayConfiguration() {
        return client.getRelayConfiguration().block();
    }

    /**
     * Creates a new CommunicationRelayConfiguration.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRelayConfiguration getRelayConfiguration(CommunicationUserIdentifier communicationUser) {
        return client.getRelayConfiguration(communicationUser).block();

    }

    /**
     * Creates a new CommunicationRelayConfiguration.
     *
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRelayConfiguration getRelayConfiguration(CommunicationRelayConfigurationRequestRouteType routeType) {
        return client.getRelayConfiguration(routeType).block();

    }

    /**
     * Creates a new CommunicationRelayConfiguration.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRelayConfiguration getRelayConfiguration(CommunicationUserIdentifier communicationUser, CommunicationRelayConfigurationRequestRouteType routeType) {
        return client.getRelayConfiguration(communicationUser, routeType).block();
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRelayConfiguration> getRelayConfigurationWithResponse(CommunicationUserIdentifier communicationUser, Context context) {
        Response<CommunicationRelayConfiguration> response =
            client.getRelayConfigurationWithResponse(communicationUser, null, context).block();

        return response;
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRelayConfiguration> getRelayConfigurationWithResponse(CommunicationRelayConfigurationRequestRouteType routeType, Context context) {
        Response<CommunicationRelayConfiguration> response =
            client.getRelayConfigurationWithResponse(null, routeType, context).block();

        return response;
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @param routeType The specified CommunicationRelayConfigurationRequestRouteType for the relay request
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRelayConfiguration> getRelayConfigurationWithResponse(CommunicationUserIdentifier communicationUser, CommunicationRelayConfigurationRequestRouteType routeType, Context context) {
        Response<CommunicationRelayConfiguration> response =
            client.getRelayConfigurationWithResponse(communicationUser, routeType, context).block();

        return response;
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRelayConfiguration> getRelayConfigurationWithResponse(Context context) {
        Response<CommunicationRelayConfiguration> response =
            client.getRelayConfigurationWithResponse(null, null, context).block();

        return response;
    }
}
