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
 * Client for connecting to a SignalR group. Created from calling {@link SignalRClient#getGroupClient(String)} on a
 * {@link SignalRClient} instance.
 *
 * @see SignalRClient
 */
@ServiceClient(
    builder = SignalRClientBuilder.class
//    serviceInterfaces = WebSocketConnectionApisService.class // TODO (jgiles) private interface, can't set it
)
public final class SignalRGroupClient {
    private final SignalRGroupAsyncClient asyncGroupClient;

    // Package-private (instantiated through SignalRClient)
    SignalRGroupClient(final SignalRGroupAsyncClient asyncGroupClient) {
        this.asyncGroupClient = asyncGroupClient;
    }

    /**
     * Send a text message to every connection in this group.
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
     * Send a text message to every connection in this group.
     *
     * @param data The message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> broadcast(final String data, final List<String> excludedUsers) {
        return broadcast(data, excludedUsers, Context.NONE);
    }

    /**
     * Send a text message to every connection in this group.
     *
     * @param data The message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> broadcast(final String data, final List<String> excludedUsers, final Context context) {
        return asyncGroupClient.broadcast(data, excludedUsers, context).block();
    }

    /**
     * Send a binary message to every connection in this group.
     *
     * @param data The binary message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> broadcast(final byte[] data, final String... excludedUsers) {
        return broadcast(data,
            excludedUsers == null ? Collections.emptyList() : Arrays.asList(excludedUsers),
            Context.NONE);
    }

    /**
     * Send a binary message to every connection in this group.
     *
     * @param data The binary message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> broadcast(final byte[] data, final List<String> excludedUsers) {
        return broadcast(data, excludedUsers, Context.NONE);
    }

    /**
     * Send a binary message to every connection in this group.
     *
     * @param data The binary message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> broadcast(final byte[] data, final List<String> excludedUsers, final Context context) {
        return asyncGroupClient.broadcast(data, excludedUsers, context).block();
    }

    /**
     * Add a user to this group
     *
     * @param userId The user name to add
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> addUser(final String userId) {
        return addUser(userId, Context.NONE);
    }

    /**
     * Add a user to this group
     *
     * @param userId The user name to add
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> addUser(final String userId, final Context context) {
        return asyncGroupClient.addUserWithResponse(userId, context).block();
    }

    /**
     * Remove a user from this group.
     *
     * @param userId The user name to remove
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeUser(final String userId) {
        return removeUser(userId, Context.NONE);
    }

    /**
     * Remove a user from this group.
     *
     * @param userId The user name to remove
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeUser(final String userId, final Context context) {
        return asyncGroupClient.removeUserWithResponse(userId, context).block();
    }

    /**
     * Check if a user is in this group
     *
     * @param userId The user name to check for
     */
    @ServiceMethod(returns = SINGLE)
    public boolean doesUserExist(final String userId) {
        return doesUserExistWithResponse(userId, Context.NONE).getValue();
    }

    /**
     * Check if a user is in this group
     *
     * @param userId The user name to check for
     */
    @ServiceMethod(returns = SINGLE)
    public SimpleResponse<Boolean> doesUserExistWithResponse(final String userId, final Context context) {
        return asyncGroupClient.doesUserExistWithResponse(userId, context).block();
    }

    /**
     * Add a specific connection to this group.
     *
     * @param connectionId The connection id to add to this group.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> addConnection(final String connectionId) {
        return addConnection(connectionId, Context.NONE);
    }

    /**
     * Add a specific connection to this group.
     *
     * @param connectionId The connection id to add to this group.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> addConnection(final String connectionId, final Context context) {
        return asyncGroupClient.addConnectionWithResponse(connectionId, context).block();
    }

    /**
     * Remove a specific connection from this group.
     *
     * @param connectionId The connection id to remove from this group.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeConnection(final String connectionId) {
        return removeConnection(connectionId, Context.NONE);
    }

    /**
     * Remove a specific connection from this group.
     *
     * @param connectionId The connection id to remove from this group.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeConnection(final String connectionId, final Context context) {
        return asyncGroupClient.removeConnectionWithResponse(connectionId, context).block();
    }
}
