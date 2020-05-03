// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.core.annotation.ReturnType.SINGLE;

/**
 * Client for connecting to a SignalR hub.
 */
@ServiceClient(
    builder = SignalRClientBuilder.class
//    serviceInterfaces = WebSocketConnectionApisService.class // TODO (jgiles) private interface, can't set it
)
public final class SignalRClient {
    private final SignalRAsyncClient asyncClient;

    SignalRClient(final SignalRAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Creates a new client for connecting to a specified SignalR group.
     *
     * @param group The name of the group.
     * @return A new client for connecting to a specified SignalR group.
     */
    public SignalRGroupClient getGroupClient(final String group) {
        return new SignalRGroupClient(asyncClient.getGroupClient(group));
    }

    /**
     * Returns whether the service is considered healthy.
     * @return whether the service is considered healthy.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean isServiceHealthy() {
        return asyncClient.isServiceHealthy().block();
    }

    /**
     * Broadcast a text message to all connections on this hub.
     *
     * @param data The message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> broadcast(final String data, final String... excludedUsers) {
        return broadcast(data,
            excludedUsers == null ? Collections.emptyList() : Arrays.asList(excludedUsers),
            Context.NONE);
    }

    /**
     * Broadcast a text message to all connections on this hub.
     *
     * @param data The message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> broadcast(final String data, final List<String> excludedUsers) {
        return broadcast(data, excludedUsers, Context.NONE);
    }

    /**
     * Broadcast a text message to all connections on this hub.
     *
     * @param data The message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> broadcast(final String data, final List<String> excludedUsers, final Context context) {
        return asyncClient.broadcast(data, excludedUsers, context).block();
    }

    /**
     * Broadcast a binary message to all connections on this hub.
     *
     * @param data The message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> broadcast(final byte[] data, final String... excludedUsers) {
        return broadcast(data,
            excludedUsers == null ? Collections.emptyList() : Arrays.asList(excludedUsers),
            Context.NONE);
    }

    /**
     * Broadcast a binary message to all connections on this hub.
     *
     * @param data The message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> broadcast(final byte[] data, final List<String> excludedUsers) {
        return broadcast(data, excludedUsers, Context.NONE);
    }

    /**
     * Broadcast a binary message to all connections on this hub.
     *
     * @param data The message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> broadcast(final byte[] data, final List<String> excludedUsers, final Context context) {
        return asyncClient.broadcast(data, excludedUsers, context).block();
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param data The message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToUser(final String userId, final String data) {
        return sendToUser(userId, data, Context.NONE);
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param data The message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToUser(final String userId, final String data, final Context context) {
        return asyncClient.sendToUser(userId, data, context).block();
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param data The binary message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToUser(final String userId, final byte[] data) {
        return sendToUser(userId, data, Context.NONE);
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param data The binary message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToUser(final String userId, final byte[] data, final Context context) {
        return asyncClient.sendToUser(userId, data, context).block();
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param data The message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToConnection(final String connectionId, final String data) {
        return sendToConnection(connectionId, data, Context.NONE);
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param data The message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToConnection(final String connectionId, final String data, final Context context) {
        return asyncClient.sendToConnection(connectionId, data, context).block();
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param data The binary message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToConnection(final String connectionId, final byte[] data) {
        return sendToConnection(connectionId, data, Context.NONE);
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param data The binary message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToConnection(final String connectionId, final byte[] data, final Context context) {
        return asyncClient.sendToConnectionWithResponse(connectionId, data, context).block();
    }

    /**
     * Remove a specific user from all groups they are joined to.
     *
     * @param userId The user ID to remove from all groups.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeUserFromAllGroups(final String userId) {
        return removeUserFromAllGroups(userId, Context.NONE);
    }

    /**
     * Remove a specific user from all groups they are joined to.
     *
     * @param userId The user ID to remove from all groups.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeUserFromAllGroups(final String userId, final Context context) {
        return asyncClient.removeUserFromAllGroups(userId, context).block();
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean doesUserExist(final String userId) {
        return doesUserExistWithResponse(userId, Context.NONE).getValue();
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     */
    @ServiceMethod(returns = SINGLE)
    public SimpleResponse<Boolean> doesUserExistWithResponse(final String userId, final Context context) {
        return asyncClient.doesUserExistWithResponse(userId, context).block();
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean doesGroupExist(final String group) {
        return doesGroupExistWithResponse(group, Context.NONE).getValue();
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     */
    @ServiceMethod(returns = SINGLE)
    public SimpleResponse<Boolean> doesGroupExistWithResponse(final String group, final Context context) {
        return asyncClient.doesGroupExistWithResponse(group, context).block();
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> closeConnection(final String connectionId) {
        return closeConnection(connectionId, null, Context.NONE);
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> closeConnection(final String connectionId, final String reason) {
        return closeConnection(connectionId, reason, Context.NONE);
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> closeConnection(final String connectionId, final String reason, final Context context) {
        return asyncClient.closeConnectionWithResponse(connectionId, reason, context).block();
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean doesConnectionExist(final String connectionId) {
        return doesConnectionExistWithResponse(connectionId, Context.NONE).getValue();
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     */
    @ServiceMethod(returns = SINGLE)
    public SimpleResponse<Boolean> doesConnectionExistWithResponse(final String connectionId, final Context context) {
        return asyncClient.doesConnectionExistWithResponse(connectionId, context).block();
    }
}
