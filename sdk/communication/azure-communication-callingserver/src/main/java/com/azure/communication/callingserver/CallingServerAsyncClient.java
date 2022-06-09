// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.ServerCallingsImpl;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.implementation.models.CreateCallRequest;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;


import static com.azure.core.util.FluxUtil.monoError;

/**
 * Asynchronous client that supports calling server operations.
 *
 * <p><strong>Instantiating a asynchronous CallingServer client</strong></p>
 *
 * <p>View {@link CallingServerClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CallingServerClientBuilder
 */
@ServiceClient(builder = CallingServerClientBuilder.class, isAsync = true)
public final class CallingServerAsyncClient {
    private final CallConnectionsImpl callConnectionInternal;
    private final ServerCallingsImpl serverCallingInternal;
    private final ClientLogger logger;
    private final ContentDownloader contentDownloader;
    private final HttpPipeline httpPipelineInternal;
    private final String resourceEndpoint;


    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callConnectionInternal = callServiceClient.getCallConnections();
        serverCallingInternal = callServiceClient.getServerCallings();
        httpPipelineInternal = callServiceClient.getHttpPipeline();
        resourceEndpoint = callServiceClient.getEndpoint();
        contentDownloader = new ContentDownloader(resourceEndpoint, httpPipelineInternal);
        logger = new ClientLogger(CallingServerAsyncClient.class);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source identity.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> createCall(
        CommunicationIdentifier source,
        CommunicationIdentifier target,
        String callbackUri,
        CreateCallOptions createCallOptions) {
        try {
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return callConnectionInternal.createCallAsync(request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
