package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;

public final class SignalRGroupClient {
    private final SignalRGroupAsyncClient asyncGroupClient;

    public SignalRGroupClient(SignalRGroupAsyncClient asyncGroupClient) {
        this.asyncGroupClient = asyncGroupClient;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addUser(String user) {
        return addUser(user, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addUser(String user, Context context) {
        return asyncGroupClient.addUserWithResponse(user, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeUser(String user) {
        return removeUser(user, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeUser(String user, Context context) {
        return asyncGroupClient.removeUserWithResponse(user, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addConnection(String connectionId) {
        return addConnection(connectionId, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> addConnection(String connectionId, Context context) {
        return asyncGroupClient.addConnectionWithResponse(connectionId, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeConnection(String connectionId) {
        return removeConnection(connectionId, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeConnection(String connectionId, Context context) {
        return asyncGroupClient.removeConnectionWithResponse(connectionId, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(String message) {
        return sendToAll(message, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(String message, Context context) {
        return asyncGroupClient.sendToAllWithResponse(message, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(byte[] data) {
        return sendToAll(data, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(byte[] data, Context context) {
        return asyncGroupClient.sendToAllWithResponse(data, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean doesUserExist(String user) {
        return doesUserExistWithResponse(user, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SimpleResponse<Boolean> doesUserExistWithResponse(String user, Context context) {
        return asyncGroupClient.doesUserExistWithResponse(user, context).block();
    }
}
