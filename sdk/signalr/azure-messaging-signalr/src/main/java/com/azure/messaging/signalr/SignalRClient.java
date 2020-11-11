// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.signalr.implementation.WebSocketConnectionApisImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.core.annotation.ReturnType.SINGLE;

/**
 * The synchronous client for connecting to a SignalR hub (for an asynchronous API, refer to the
 * {@link SignalRAsyncClient} class documentation). To create an instance of this class, refer to the code snippet
 * below, and for more information about configuration options, refer to the JavaDoc for {@link SignalRClientBuilder}.
 *
 * <p>The SignalR client instance will either be connected to a {@link SignalRClientBuilder#hub(String) specific hub},
 * or else it will be using the default hub provided by the Azure SignalR service. Within the SignalR client, users
 * may perform operations including:
 *
 * <ul>
 *     <li>Sending messages to {@link #sendToAll(String, String...) everyone in the hub},</li>
 *     <li>Sending messages to a {@link #sendToUser(String, String) specific user} or
 *     {@link #sendToConnection(String, String) connection},</li>
 *     <li>{@link #removeUserFromAllGroups(String) Removing a user} from all groups,</li>
 *     <li>{@link #closeConnection(String) Closing a connection} of a specific user</li>
 *     <li>To check the existence of a {@link #userExists(String) user}, a {@link #connectionExists(String) connection},
 *     or a {@link #groupExists(String) group},</li>
 *     <li>Check on the {@link #getStatus() health status} of the SignalR service.</li>
 * </ul>
 *
 * <p>It is possible to connect to a specific group within a hub by calling
 * {@link #getGroupClient(String)}, which will return a {@link SignalRGroupClient}. All operations performed on
 * this SignalR group client will take into account the hub and group in which it exists.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.messaging.signalr.secretclientbuilder.connectionstring.sync}
 *
 * @see SignalRClientBuilder
 * @see SignalRAsyncClient
 * @see SignalRGroupClient
 */
@ServiceClient(
    builder = SignalRClientBuilder.class,
    serviceInterfaces = WebSocketConnectionApisImpl.WebSocketConnectionApisService.class
)
public final class SignalRClient {
    private final SignalRAsyncClient asyncClient;

    SignalRClient(final SignalRAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Creates a new {@link SignalRGroupClient synchronous group client} for connecting to a specified SignalR
     * group, which will also remain associated with the hub that was previously specified for the calling client
     * instance in its builder (set via {@link SignalRClientBuilder#hub(String)}.
     *
     * @param group The name of the group.
     * @return A new client for connecting to a specified SignalR group.
     */
    public SignalRGroupClient getGroupClient(final String group) {
        return new SignalRGroupClient(asyncClient.getGroupAsyncClient(group));
    }

    /**
     * Returns status information related to the SignalR service, in particular whether it is considered
     * {@link SignalRHubStatus#isAvailable() available}.
     *
     * @return status information related to the SignalR service.
     */
    @ServiceMethod(returns = SINGLE)
    public SignalRHubStatus getStatus() {
        return asyncClient.getStatus().block();
    }

    /**
     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAll.String.String}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAll.String.String.2}
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
     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAll.String.List}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the second argument:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAll.String.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final String message, final List<String> excludedConnectionIds) {
        sendToAllWithResponse(message, excludedConnectionIds, Context.NONE);
    }

    /**
     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAll.String.List}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the second argument:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAll.String.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToAllWithResponse(final String message,
                                                final List<String> excludedConnectionIds,
                                                final Context context) {
        return asyncClient.sendToAll(message, excludedConnectionIds, context).block();
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.String}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.String.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional var-args of connection IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final byte[] message, final String... excludedConnectionIds) {
        sendToAll(message, excludedConnectionIds == null
                               ? Collections.emptyList() : Arrays.asList(excludedConnectionIds));
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.List}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final byte[] message, final List<String> excludedConnectionIds) {
        sendToAllWithResponse(message, excludedConnectionIds, Context.NONE);
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.List}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrclient.sendToAllBytes.byte.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToAllWithResponse(final byte[] message,
                                                final List<String> excludedConnectionIds,
                                                final Context context) {
        return asyncClient.sendToAll(message, excludedConnectionIds, context).block();
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToUser(final String userId, final String message) {
        sendToUserWithResponse(userId, message, Context.NONE);
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToUserWithResponse(final String userId, final String message, final Context context) {
        return asyncClient.sendToUser(userId, message, context).block();
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The binary message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToUser(final String userId, final byte[] message) {
        sendToUserWithResponse(userId, message, Context.NONE);
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The binary message to send.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToUserWithResponse(final String userId, final byte[] message, final Context context) {
        return asyncClient.sendToUser(userId, message, context).block();
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToConnection(final String connectionId, final String message) {
        sendToConnectionWithResponse(connectionId, message, Context.NONE);
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToConnectionWithResponse(final String connectionId,
                                                       final String message,
                                                       final Context context) {
        return asyncClient.sendToConnection(connectionId, message, context).block();
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The binary message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToConnection(final String connectionId, final byte[] message) {
        sendToConnectionWithResponse(connectionId, message, Context.NONE);
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The binary message to send.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToConnectionWithResponse(final String connectionId,
                                                       final byte[] message,
                                                       final Context context) {
        return asyncClient.sendToConnectionWithResponse(connectionId, message, context).block();
    }

    /**
     * Remove a specific user from all groups they are joined to.
     *
     * @param userId The user ID to remove from all groups.
     */
    @ServiceMethod(returns = SINGLE)
    public void removeUserFromAllGroups(final String userId) {
        removeUserFromAllGroupsWithResponse(userId, Context.NONE);
    }

    /**
     * Remove a specific user from all groups they are joined to.
     *
     * @param userId The user ID to remove from all groups.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> removeUserFromAllGroupsWithResponse(final String userId, final Context context) {
        return asyncClient.removeUserFromAllGroups(userId, context).block();
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     * @return A Boolean value representing whether the user exists in this hub.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean userExists(final String userId) {
        return userExistsWithResponse(userId, Context.NONE).getValue();
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a Boolean value representing whether the user exists in this hub, as well as
     *      status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Boolean> userExistsWithResponse(final String userId, final Context context) {
        return asyncClient.userExistsWithResponse(userId, context).block();
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     * @return A Boolean value representing whether the group exists.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean groupExists(final String group) {
        return groupExistsWithResponse(group, Context.NONE).getValue();
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a Boolean value representing whether the group exists, as well as
     *     status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Boolean> groupExistsWithResponse(final String group, final Context context) {
        return asyncClient.groupExistsWithResponse(group, context).block();
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     */
    @ServiceMethod(returns = SINGLE)
    public void closeConnection(final String connectionId) {
        closeConnectionWithResponse(connectionId, null, Context.NONE);
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     * @param reason The reason why the connection was closed.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> closeConnectionWithResponse(final String connectionId,
                                                      final String reason,
                                                      final Context context) {
        return asyncClient.closeConnectionWithResponse(connectionId, reason, context).block();
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     * @return A Boolean value representing whether the connection exists in this hub.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean connectionExists(final String connectionId) {
        return connectionExistsWithResponse(connectionId, Context.NONE).getValue();
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a Boolean value representing whether the connection exists in this hub, as well
     *     as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Boolean> connectionExistsWithResponse(final String connectionId, final Context context) {
        return asyncClient.connectionExistsWithResponse(connectionId, context).block();
    }
}
