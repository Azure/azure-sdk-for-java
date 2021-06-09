// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.ServerCallsImpl;
import com.azure.communication.callingserver.implementation.converters.CallConnectionRequestConverter;
import com.azure.communication.callingserver.implementation.converters.JoinCallConverter;
import com.azure.communication.callingserver.implementation.converters.ServerCallingErrorConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;


/**
 * Async client that supports server call operations.
 */
@ServiceClient(builder = CallingServerClientBuilder.class, isAsync = true)
public final class CallingServerAsyncClient {
    private final CallConnectionsImpl callConnectionInternal;
    private final ServerCallsImpl serverCallInternal;
    private final ClientLogger logger = new ClientLogger(CallingServerAsyncClient.class);

    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callConnectionInternal = callServiceClient.getCallConnections();
        serverCallInternal = callServiceClient.getServerCalls();
    }

    /**
     * Create a Call Connection Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param createCallOptions The call Options.
     * @return response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> createCallConnection(CommunicationIdentifier source,
                                               CommunicationIdentifier[] targets,
                                               CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'createCallOptions' cannot be null.");
            CreateCallRequestInternal request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return this.callConnectionInternal.createCallAsync(request)
                .onErrorMap(CommunicationErrorException.class, e -> ServerCallingErrorConverter.translateException(e))
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a Call Connection Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param createCallOptions The call Options.
     * @return response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> createCallConnectionWithResponse(CommunicationIdentifier source,
                                                                      CommunicationIdentifier[] targets,
                                                                      CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");
            CreateCallRequestInternal request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return this.callConnectionInternal.createCallWithResponseAsync(request)
                .onErrorMap(CommunicationErrorException.class, e -> ServerCallingErrorConverter.translateException(e))
                .map(response -> new SimpleResponse<>(response,
                    new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<CallConnection> createCallConnectionInternal(CommunicationIdentifier source,
                                                          CommunicationIdentifier[] targets,
                                                          CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'createCallOptions' cannot be null.");
            CreateCallRequestInternal request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return this.callConnectionInternal.createCallAsync(request)
                .onErrorMap(CommunicationErrorException.class, e -> ServerCallingErrorConverter.translateException(e))
                .flatMap(response -> Mono.just(new CallConnection(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal))));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CallConnection>> createCallConnectionWithResponseInternal(CommunicationIdentifier source,
                                                                      CommunicationIdentifier[] targets,
                                                                      CreateCallOptions createCallOptions,
                                                                     Context context) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");
            CreateCallRequestInternal request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callConnectionInternal.createCallWithResponseAsync(request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> ServerCallingErrorConverter.translateException(e))
                    .map(response -> new SimpleResponse<>(response,
                        new CallConnection(new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal))));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a Call
     *
     * @param serverCallId The server call id.
     * @param source to Join Call.
     * @param joinCallOptions join call options.
     * @return response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> join(String serverCallId,
                                           CommunicationIdentifier source,
                                           JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return this.serverCallInternal
                .joinCallAsync(serverCallId, JoinCallConverter.convert(source, joinCallOptions))
                .onErrorMap(CommunicationErrorException.class, e -> ServerCallingErrorConverter.translateException(e))
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a Call
     *
     * @param serverCallId The server call id.
     * @param source to Join Call.
     * @param joinCallOptions join call options.
     * @return response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>>joinWithResponse(String serverCallId,
                                                                CommunicationIdentifier source,
                                                                JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return this.serverCallInternal.
                joinCallWithResponseAsync(serverCallId, JoinCallConverter.convert(source, joinCallOptions))
                .onErrorMap(CommunicationErrorException.class, e -> ServerCallingErrorConverter.translateException(e))
                .map(response -> new SimpleResponse<>(response,
                    new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<CallConnection> joinInternal(String serverCallId,
                                                    CommunicationIdentifier source,
                                                    JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return this.serverCallInternal
                .joinCallAsync(serverCallId, JoinCallConverter.convert(source, joinCallOptions))
                .onErrorMap(CommunicationErrorException.class, e -> ServerCallingErrorConverter.translateException(e))
                .flatMap(response -> Mono.just(new CallConnection(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal))));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


    Mono<Response<CallConnection>>joinWithResponseInternal(String serverCallId,
                                                                             CommunicationIdentifier source,
                                                                             JoinCallOptions joinCallOptions,
                                                                      Context context) {
        try {
            Objects.requireNonNull(serverCallId, "'serverCallId' cannot be null.");
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.serverCallInternal.
                    joinCallWithResponseAsync(serverCallId,
                        JoinCallConverter.convert(source, joinCallOptions), contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> ServerCallingErrorConverter.translateException(e))
                    .map(response -> new SimpleResponse<>(response,
                        new CallConnection(new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal))));
            });

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get CallConnection object
     *
     * @param callConnectionId The call connection id.
     * @return CallConnection
     */
    public CallConnectionAsync getCallConnection(String callConnectionId) {
        return new CallConnectionAsync(callConnectionId, callConnectionInternal);
    }

    CallConnection getCallConnectionInternal(String callConnectionId) {
        return new CallConnection(new CallConnectionAsync(callConnectionId, callConnectionInternal));
    }

    /**
     * Get ServerCall object
     *
     * @param serverCallId The server call id.
     * @return ServerCall
     */
    public ServerCallAsync initializeServerCall(String serverCallId) {
        return new ServerCallAsync(serverCallId, serverCallInternal);
    }

    ServerCall initializeServerCallInternal(String serverCallId) {
        return new ServerCall(serverCallId, new ServerCallAsync(serverCallId, serverCallInternal));
    }
}
