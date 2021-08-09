// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.networktraversal.implementation.CommunicationNetworkingClientImpl;
import com.azure.communication.networktraversal.implementation.CommunicationNetworkTraversalsImpl;
import com.azure.communication.networktraversal.models.CommunicationRelayConfiguration;
import com.azure.communication.networktraversal.models.CommunicationErrorResponse;
import com.azure.communication.networktraversal.models.CommunicationRelayConfigurationRequest;
import com.azure.communication.networktraversal.models.CommunicationErrorResponseException;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import reactor.core.publisher.Mono;
import static com.azure.core.util.FluxUtil.monoError;

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
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRelayConfiguration> getRelayConfiguration(CommunicationUserIdentifier communicationUser) {
        CommunicationRelayConfigurationRequest body = new CommunicationRelayConfigurationRequest();
        body.setId(communicationUser.getId());
        
        return Mono.just(client.issueRelayConfiguration(body));
    }

    /**
     * Creates a new CommunicationRelayConfiguration with response.
     *
     * @param communicationUser The CommunicationUserIdentifier for whom to issue a token
     * @return The created Communication Relay Configuration.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRelayConfiguration>> getRelayConfigurationWithResponse(CommunicationUserIdentifier communicationUser) {
        try {
            CommunicationRelayConfigurationRequest body = new CommunicationRelayConfigurationRequest();
            body.setId(communicationUser.getId());
            return client.issueRelayConfigurationWithResponseAsync(body)
                .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
                .flatMap(
                    (Response<CommunicationRelayConfiguration> response) -> {
                        return Mono.just(
                            new SimpleResponse<CommunicationRelayConfiguration>(
                                response,
                                response.getValue()));
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private CommunicationErrorResponseException translateException(CommunicationErrorResponseException exception) {
        CommunicationErrorResponse error = null;
        if (exception.getValue() != null) {
            error = exception.getValue();
        }
        return new CommunicationErrorResponseException(exception.getMessage(), exception.getResponse(), error);
    }
}
