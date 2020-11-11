// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.signalr.implementation.WebSocketConnectionApisImpl;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.core.annotation.ReturnType.SINGLE;

/**
 * <p>Sync client for interacting with a specific SignalR group, contained within a SignalR hub. An instance of this
 * group client is able to be created by calling {@link SignalRClient#getGroupClient(String)} on a
 * {@link SignalRClient} instance. All operations performed on an instance of a group client takes into account
 * the specified group name, as well as the name of the hub specified when instantiating the
 * {@link SignalRClient} via the {@link SignalRClientBuilder}. This is demonstrated in the code below:</p>
 *
 * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.instance}
 *
 * @see SignalRAsyncClient
 * @see SignalRClientBuilder
 */
@ServiceClient(
    builder = SignalRClientBuilder.class,
    serviceInterfaces = WebSocketConnectionApisImpl.WebSocketConnectionApisService.class
)
public final class SignalRGroupClient {
    private final SignalRGroupAsyncClient asyncGroupClient;

    // Package-private (instantiated through SignalRClient)
    SignalRGroupClient(final SignalRGroupAsyncClient asyncGroupClient) {
        this.asyncGroupClient = asyncGroupClient;
    }

    /**
     * Broadcast a text message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same group, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.String}
     *
     * <p>To send a message to all users within the same group, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.String.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional var-args of connection IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final String message, final String... excludedConnectionIds) {
        sendToAll(message, excludedConnectionIds == null
                               ? Collections.emptyList() : Arrays.asList(excludedConnectionIds));
    }

    /**
     * Broadcast a text message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.List}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the second argument:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final String message, final List<String> excludedConnectionIds) {
        sendToAllWithResponse(message, excludedConnectionIds, Context.NONE);
    }

    /**
     * Broadcast a text message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.List}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the second argument:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAll.String.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional var-args of connection IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToAllWithResponse(final String message,
                                                final List<String> excludedConnectionIds,
                                                final Context context) {
        return asyncGroupClient.sendToAll(message, excludedConnectionIds, context).block();
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.String}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.String.2}
     *
     * @param message The binary message to send.
     * @param excludedConnectionIds An optional var-args of connection IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final byte[] message, final String... excludedConnectionIds) {
        sendToAll(message, excludedConnectionIds == null
                               ? Collections.emptyList() : Arrays.asList(excludedConnectionIds));
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.List}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.List.2}
     *
     * @param message The binary message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final byte[] message, final List<String> excludedConnectionIds) {
        sendToAllWithResponse(message, excludedConnectionIds, Context.NONE);
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.List}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupclient.sendToAllBytes.byte.List.2}
     *
     * @param message The binary message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToAllWithResponse(final byte[] message,
                                                final List<String> excludedConnectionIds,
                                                final Context context) {
        return asyncGroupClient.sendToAll(message, excludedConnectionIds, context).block();
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
    public boolean userExists(final String userId) {
        return userExistsWithResponse(userId, Context.NONE).getValue();
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
    public Response<Boolean> userExistsWithResponse(final String userId, final Context context) {
        return asyncGroupClient.userExistsWithResponse(userId, context).block();
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
