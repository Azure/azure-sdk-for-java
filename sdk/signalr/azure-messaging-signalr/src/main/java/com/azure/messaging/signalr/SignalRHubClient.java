package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;

public final class SignalRHubClient {
    private final SignalRHubAsyncClient asyncHubClient;

    SignalRHubClient(SignalRHubAsyncClient asyncHubClient) {
        this.asyncHubClient = asyncHubClient;
    }

    public SignalRGroupClient getGroupClient(String group) {
        return new SignalRGroupClient(asyncHubClient.getGroupClient(group));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeUser(String user) {
        return removeUser(user, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeUser(String user, Context context) {
        return asyncHubClient.removeUser(user, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(String message) {
        return sendToAll(message, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(String message, Context context) {
        return asyncHubClient.sendToAll(message, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(byte[] data) {
        return sendToAll(data, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToAll(byte[] data, Context context) {
        return asyncHubClient.sendToAll(data, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToUser(String user, String message) {
        return sendToUser(user, message, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToUser(String user, String message, Context context) {
        return asyncHubClient.sendToUser(user, message, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToUser(String user, byte[] data) {
        return sendToUser(user, data, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToUser(String user, byte[] data, Context context) {
        return asyncHubClient.sendToUser(user, data, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToConnection(String connectionId, String message) {
        return sendToConnection(connectionId, message, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToConnection(String connectionId, String message, Context context) {
        return asyncHubClient.sendToConnection(connectionId, message, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToConnection(String connectionId, byte[] data) {
        return sendToConnection(connectionId, data, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendToConnection(String connectionId, byte[] data, Context context) {
        return asyncHubClient.sendToConnectionWithResponse(connectionId, data, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean doesUserExist(String user) {
        return doesUserExistWithResponse(user, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SimpleResponse<Boolean> doesUserExistWithResponse(String user, Context context) {
        return asyncHubClient.doesUserExistWithResponse(user, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> closeConnection(String connectionId) {
        return closeConnection(connectionId, null, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> closeConnection(String connectionId, String reason) {
        return closeConnection(connectionId, reason, Context.NONE);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> closeConnection(String connectionId, String reason, Context context) {
        return asyncHubClient.closeConnectionWithResponse(connectionId, reason, context).block();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean doesConnectionExist(String connectionId) {
        return doesConnectionExistWithResponse(connectionId, Context.NONE).getValue();
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public SimpleResponse<Boolean> doesConnectionExistWithResponse(String connectionId, Context context) {
        return asyncHubClient.doesConnectionExistWithResponse(connectionId, context).block();
    }
}
