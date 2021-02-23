// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.time.Duration;
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
    public void broadcast(final String data, final String... excludedUsers) {
        broadcast(data, excludedUsers == null ? Collections.emptyList() : Arrays.asList(excludedUsers));
    }

    /**
     * Send a text message to every connection in this group.
     *
     * @param data The message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void broadcast(final String data, final List<String> excludedUsers) {
        broadcastWithResponse(data, excludedUsers, Context.NONE);
    }

    /**
     * Send a text message to every connection in this group.
     *
     * @param data The message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> broadcastWithResponse(final String data,
                                                final List<String> excludedUsers,
                                                final Context context) {
        return asyncGroupClient.broadcast(data, excludedUsers, context).block();
    }

    /**
     * Send a binary message to every connection in this group.
     *
     * @param data The binary message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void broadcast(final byte[] data, final String... excludedUsers) {
        broadcast(data, excludedUsers == null ? Collections.emptyList() : Arrays.asList(excludedUsers));
    }

    /**
     * Send a binary message to every connection in this group.
     *
     * @param data The binary message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void broadcast(final byte[] data, final List<String> excludedUsers) {
        broadcastWithResponse(data, excludedUsers, Context.NONE);
    }

    /**
     * Send a binary message to every connection in this group.
     *
     * @param data The binary message to send.
     * @param excludedUsers An optional list of user IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> broadcastWithResponse(final byte[] data,
                                                final List<String> excludedUsers,
                                                final Context context) {
        return asyncGroupClient.broadcast(data, excludedUsers, context).block();
    }

    /**
     * Add a user to this group that will remain in the group until they are manually removed.
     *
     * @param userId The user name to add
     */
    @ServiceMethod(returns = SINGLE)
    public void addUser(final String userId) {
        addUserWithResponse(userId, null, Context.NONE);
    }

    /**
     * Add a user to this group with a specified time-to-live before that user will be removed. A null time to live will
     * mean that the user remains in the group indefinitely.
     *
     * @param userId The user name to add
     * @param timeToLive Specifies the duration that the user exists in the group. If not set, the user lives in the
     * group forever.
     */
    @ServiceMethod(returns = SINGLE)
    public void addUser(final String userId, final Duration timeToLive) {
        addUserWithResponse(userId, timeToLive, Context.NONE);
    }

    /**
     * Add a user to this group.
     *
     * @param userId The user name to add.
     * @param timeToLive Specifies the duration that the user exists in the group. If not set, the user lives in the
     *     group forever.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
      *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> addUserWithResponse(final String userId, final Duration timeToLive, final Context context) {
        return asyncGroupClient.addUserWithResponse(userId, timeToLive, context).block();
    }

    /**
     * Remove a user from this group.
     *
     * @param userId The user name to remove
     */
    @ServiceMethod(returns = SINGLE)
    public void removeUser(final String userId) {
        removeUserWithResponse(userId, Context.NONE);
    }

    /**
     * Remove a user from this group.
     *
     * @param userId The user name to remove
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeUserWithResponse(final String userId, final Context context) {
        return asyncGroupClient.removeUserWithResponse(userId, context).block();
    }

    /**
     * Check if a user is in this group.
     *
     * @param userId The user name to check for.
     * @return Boolean true value if the user does exist in this group, and false if not.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean doesUserExist(final String userId) {
        return doesUserExistWithResponse(userId, Context.NONE).getValue();
    }

    /**
     * Check if a user is in this group.
     *
     * @param userId The user name to check for.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a Boolean value representing whether the user exists in this group, as well as
     *     status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Boolean> doesUserExistWithResponse(final String userId, final Context context) {
        return asyncGroupClient.doesUserExistWithResponse(userId, context).block();
    }

    /**
     * Add a specific connection to this group.
     *
     * @param connectionId The connection id to add to this group.
     */
    @ServiceMethod(returns = SINGLE)
    public void addConnection(final String connectionId) {
        addConnectionWithResponse(connectionId, Context.NONE);
    }

    /**
     * Add a specific connection to this group.
     *
     * @param connectionId The connection id to add to this group.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> addConnectionWithResponse(final String connectionId, final Context context) {
        return asyncGroupClient.addConnectionWithResponse(connectionId, context).block();
    }

    /**
     * Remove a specific connection from this group.
     *
     * @param connectionId The connection id to remove from this group.
     */
    @ServiceMethod(returns = SINGLE)
    public void removeConnection(final String connectionId) {
        removeConnectionWithResponse(connectionId, Context.NONE);
    }

    /**
     * Remove a specific connection from this group.
     *
     * @param connectionId The connection id to remove from this group.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeConnectionWithResponse(final String connectionId, final Context context) {
        return asyncGroupClient.removeConnectionWithResponse(connectionId, context).block();
    }
}
