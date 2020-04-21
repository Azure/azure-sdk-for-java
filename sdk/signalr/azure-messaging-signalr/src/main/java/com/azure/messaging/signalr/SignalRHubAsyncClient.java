package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.signalr.implementation.client.AzureWebSocketServiceRestAPI;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

// FIXME add tracing / logging
public final class SignalRHubAsyncClient {
    private final ClientLogger logger = new ClientLogger(SignalRHubAsyncClient.class);

    private final AzureWebSocketServiceRestAPI innerClient;
    private final String hub;

    SignalRHubAsyncClient(AzureWebSocketServiceRestAPI innerClient, String hub) {
        this.innerClient = innerClient;
        this.hub = hub;
    }

    public SignalRGroupAsyncClient getGroupClient(String group) {
        return new SignalRGroupAsyncClient(innerClient, hub, group);
    }

//    /**
//     * Remove a user from all groups within this hub.
//     *
//     * @param user Target user Id.
//     * @throws IllegalArgumentException thrown if parameters fail the validation.
//     * @throws HttpResponseException thrown if the request is rejected by server.
//     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
//     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUser(String user) {
        return removeUser(user, Context.NONE);
    }

    Mono<Response<Void>> removeUser(String user, Context context) {
        return innerClient.webSocketConnectionApis().removeUserFromHubWithResponseAsync(hub, user, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(String message) {
        return sendToAll(message, Context.NONE);
    }

    Mono<Response<Void>> sendToAll(String message, Context context) {
        return innerClient.webSocketConnectionApis().sendToHubConnectionsWithResponseAsync(hub, message, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(byte[] data) {
        return sendToAll(data, Context.NONE);
    }

    Mono<Response<Void>> sendToAll(byte[] data, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return innerClient.webSocketConnectionApis().sendToHubConnectionsWithResponseAsync(hub, byteFlux, data.length, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUser(String user, String message) {
        return sendToUser(user, message, Context.NONE);
    }

    Mono<Response<Void>> sendToUser(String user, String message, Context context) {
        return innerClient.webSocketConnectionApis().sendToHubUserWithResponseAsync(hub, user, message, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUser(String user, byte[] data) {
        return sendToUser(user, data, Context.NONE);
    }

    Mono<Response<Void>> sendToUser(String user, byte[] data, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return innerClient.webSocketConnectionApis().sendToHubUserWithResponseAsync(hub, user, byteFlux, data.length, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnection(String connectionId, String message) {
        return sendToConnection(connectionId, message, Context.NONE);
    }

    Mono<Response<Void>> sendToConnection(String connectionId, String message, Context context) {
        return innerClient.webSocketConnectionApis().sendToHubConnectionWithResponseAsync(hub, connectionId, message, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnection(String connectionId, byte[] data) {
        return sendToConnectionWithResponse(connectionId, data, Context.NONE);
    }

    Mono<Response<Void>> sendToConnectionWithResponse(String connectionId, byte[] data, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return innerClient.webSocketConnectionApis().sendToHubConnectionWithResponseAsync(hub, connectionId, byteFlux, data.length, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> doesUserExist(String user) {
        return doesUserExistWithResponse(user).map(SimpleResponse::getValue);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> doesUserExistWithResponse(String user) {
        return doesUserExistWithResponse(user, Context.NONE);
    }

    Mono<SimpleResponse<Boolean>> doesUserExistWithResponse(String user, Context context) {
        return innerClient.webSocketConnectionApis().headUserWithResponseAsync(hub, user, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnection(String connectionId) {
        return closeConnection(connectionId, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnection(String connectionId, String reason) {
        return closeConnectionWithResponse(connectionId, reason, Context.NONE);
    }

    Mono<Response<Void>> closeConnectionWithResponse(String connectionId, String reason, Context context) {
        return innerClient.webSocketConnectionApis().closeHubConnectionWithResponseAsync(hub, connectionId, context, reason);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> doesConnectionExist(String connectionId) {
        return doesConnectionExistWithResponse(connectionId).map(SimpleResponse::getValue);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> doesConnectionExistWithResponse(String connectionId) {
        return doesConnectionExistWithResponse(connectionId, Context.NONE);
    }

    Mono<SimpleResponse<Boolean>> doesConnectionExistWithResponse(String connectionId, Context context) {
        return innerClient.webSocketConnectionApis().headHubConnectionWithResponseAsync(hub, connectionId, context);
    }
}
