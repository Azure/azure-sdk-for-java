package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.messaging.signalr.implementation.client.AzureWebSocketServiceRestAPI;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

// FIXME add tracing / logging
public final class SignalRGroupAsyncClient {
    private final AzureWebSocketServiceRestAPI innerClient;
    private final String hub;
    private final String group;

    SignalRGroupAsyncClient(AzureWebSocketServiceRestAPI innerClient, String hub, String group) {
        this.innerClient = innerClient;
        this.hub = hub;
        this.group = group;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addUser(String user) {
        return addUserWithResponse(user, Context.NONE);
    }

    Mono<Response<Void>> addUserWithResponse(String user, Context context) {
        return innerClient.webSocketConnectionApis().addHubUserWithResponseAsync(hub, group, user, context, null); // FIXME null TTL
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUser(String user) {
        return removeUserWithResponse(user, Context.NONE);
    }

    Mono<Response<Void>> removeUserWithResponse(String user, Context context) {
        return innerClient.webSocketConnectionApis().removeHubUserWithResponseAsync(hub, group, user, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addConnection(String connectionId) {
        return addConnectionWithResponse(connectionId, Context.NONE);
    }

    Mono<Response<Void>> addConnectionWithResponse(String connectionId, Context context) {
        return innerClient.webSocketConnectionApis().addConnectionWithResponseAsync(group, connectionId, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeConnection(String connectionId) {
        return removeConnectionWithResponse(connectionId, Context.NONE);
    }

    Mono<Response<Void>> removeConnectionWithResponse(String connectionId, Context context) {
        return innerClient.webSocketConnectionApis().removeConnectionWithResponseAsync(group, connectionId, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(String message) {
        return sendToAllWithResponse(message, Context.NONE);
    }

    Mono<Response<Void>> sendToAllWithResponse(String message, Context context) {
        return innerClient.webSocketConnectionApis().sendToHubGroupWithResponseAsync(hub, group, message, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(byte[] data) {
        return sendToAllWithResponse(data, Context.NONE);
    }

    Mono<Response<Void>> sendToAllWithResponse(byte[] data, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return innerClient.webSocketConnectionApis().sendToHubGroupWithResponseAsync(hub, group, byteFlux, data.length, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> doesUserExist(String user) {
        return doesUserExistWithResponse(user).map(SimpleResponse::getValue);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> doesUserExistWithResponse(String user) {
        return doesUserExistWithResponse(user, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<SimpleResponse<Boolean>> doesUserExistWithResponse(String user, Context context) {
//        return innerClient.webSocketConnectionApis().headUserWithResponseAsync(group, user, context);
        return innerClient.webSocketConnectionApis().headHubUserWithResponseAsync(hub, group, user, context);
    }
}
