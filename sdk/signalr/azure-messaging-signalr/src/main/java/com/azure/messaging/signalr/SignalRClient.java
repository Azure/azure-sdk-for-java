package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

@ServiceClient(builder = SignalRClientBuilder.class,
    isAsync = false
//    serviceInterfaces = WebSocketConnectionApisService.class // FIXME private interface, can't set it
)
public final class SignalRClient {

    private final SignalRAsyncClient asyncClient;

    SignalRClient(SignalRAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    public SignalRHubClient getHubClient(String hub) {
        return new SignalRHubClient(asyncClient.getHubClient(hub));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignalRStatus getStatus() {
        return asyncClient.getStatus().block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(String message) {
        return sendToAll(message, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(String message, Context context) {
        return asyncClient.sendToAll(message, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(byte[] data) {
        return sendToAll(data, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(byte[] data, Context context) {
        return asyncClient.sendToAll(data, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToUser(String user, String message) {
        return sendToUser(user, message, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToUser(String user, String message, Context context) {
        return asyncClient.sendToUser(user, message, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToUser(String user, byte[] data) {
        return sendToUser(user, data, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToUser(String user, byte[] data, Context context) {
        return asyncClient.sendToUser(user, data, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToConnection(String connectionId, String message) {
        return sendToConnection(connectionId, message, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToConnection(String connectionId, String message, Context context) {
        return asyncClient.sendToConnection(connectionId, message, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToConnection(String connectionId, byte[] data) {
        return sendToConnection(connectionId, data, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToConnection(String connectionId, byte[] data, Context context) {
        return asyncClient.sendToConnection(connectionId, data, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> closeConnection(String connectionId) {
        return closeConnection(connectionId, null);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> closeConnection(String connectionId, String reason) {
        return closeConnection(connectionId, null, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> closeConnection(String connectionId, String reason, Context context) {
        return asyncClient.closeConnection(connectionId, reason, context).block();
    }
}
