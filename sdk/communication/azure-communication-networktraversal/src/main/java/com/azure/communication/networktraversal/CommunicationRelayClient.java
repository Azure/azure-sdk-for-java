// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
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
     * Gets a Relay Configuration.
     *
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRelayConfiguration getRelayConfiguration() {
        return client.getRelayConfiguration().block();
    }

    /**
     * Gets a Relay Configuration for a CommunicationUserIdentifier.
     *
     * @param options of the GetRelayConfigurationOptions request
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRelayConfiguration getRelayConfiguration(GetRelayConfigurationOptions options) {
        return client.getRelayConfiguration(options).block();

    }

    /**
     * Gets a Relay Configuration with response.
     *
     * @param options of the GetRelayConfigurationOptions request
     * @param context A {@link Context} representing the request context.
     * @return The obtained Communication Relay Configuration
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRelayConfiguration> getRelayConfigurationWithResponse(GetRelayConfigurationOptions options, Context context) {
        Response<CommunicationRelayConfiguration> response =
            client.getRelayConfigurationWithResponse(options, context).block();

        return response;
    }
}
