// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.webpubsub.implementation.WebPubSubsImpl;
import com.azure.messaging.webpubsub.models.GetAuthenticationTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubAuthenticationToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.azure.messaging.webpubsub.models.WebPubSubPermission;

import static com.azure.core.annotation.ReturnType.SINGLE;

/**
 * The synchronous client for connecting to a Azure Web Pub Sub hub (for an asynchronous API, refer to the
 * {@link WebPubSubAsyncServiceClient} class documentation). To create an instance of this class, refer to the code
 * snippet below, and for more information about configuration options, refer to the JavaDoc for
 * {@link WebPubSubClientBuilder}.
 *
 * <p>The Azure Web Pub Sub client instance must be connected to a
 * {@link WebPubSubClientBuilder#hub(String) specific hub} that is represented by a non-null and non-empty String.
 * Within the Azure Web Pub Sub client, users may perform operations including:
 *
 * <ul>
 *     <li>Sending messages to {@link #sendToAll(String) everyone in the hub},</li>
 *     <li>Sending messages to a {@link #sendToUser(String, String) specific user} or
 *     {@link #sendToConnection(String, String) connection},</li>
 *     <li>{@link #removeUserFromAllGroups(String) Removing a user} from all groups,</li>
 *     <li>{@link #closeConnection(String) Closing a connection} of a specific user</li>
 *     <li>To check the existence of a {@link #checkUserExists(String) user}, a
 *     {@link #checkConnectionExists(String) connection}, or a {@link #checkGroupExists(String) group},</li>
 * </ul>
 *
 * <p>It is possible to connect to a specific group within a hub by calling
 * {@link #getGroup(String)}, which will return a {@link WebPubSubGroup}. All operations performed on
 * this Azure Web Pub Sub group client will take into account the hub and group in which it exists.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.sync}
 *
 * @see WebPubSubClientBuilder
 * @see WebPubSubAsyncServiceClient
 * @see WebPubSubGroup
 */
@ServiceClient(
    builder = WebPubSubClientBuilder.class,
    serviceInterfaces = WebPubSubsImpl.WebPubSubsService.class
)
public final class WebPubSubServiceClient {
    private final WebPubSubAsyncServiceClient asyncClient;

    WebPubSubServiceClient(final WebPubSubAsyncServiceClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Creates a new synchronous {@link WebPubSubGroup} for connecting to a specified
     * Azure Web Pub Sub group, which will also remain associated with the hub that was previously specified for the
     * calling client instance in its builder (set via {@link WebPubSubClientBuilder#hub(String)}.
     *
     * @param group The name of the group.
     * @return A new client for connecting to a specified Azure Web Pub Sub group.
     */
    public WebPubSubGroup getGroup(final String group) {
        return new WebPubSubGroup(asyncClient.getAsyncGroup(group));
    }

    /**
     * Creates an authentication token.
     *
     * @param options Options to apply when creating the authentication token.
     * @return A new authentication token instance.
     */
    public WebPubSubAuthenticationToken getAuthenticationToken(GetAuthenticationTokenOptions options) {
        return asyncClient.getAuthenticationToken(options);
    }

    /**
     * Broadcast a text message to all connections on this hub.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#String}
     *
     * @param message The message to send.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final String message) {
        sendToAll(message, null);
    }

    /**
     * Broadcast a text message to all connections on this hub.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#String-WebPubSubContentType}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final String message, final WebPubSubContentType contentType) {
        sendToAllWithResponse(message, contentType, null, Context.NONE);
    }

    /**
     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the third argument:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.withexclusions}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToAllWithResponse(final String message,
                                                final WebPubSubContentType contentType,
                                                final Iterable<String> excludedConnectionIds,
                                                final Context context) {
        return asyncClient.sendToAllWithResponse(message, contentType, excludedConnectionIds, context).block();
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#byte}
     *
     * @param message The message to send.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final byte[] message) {
        sendToAll(message, null);
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAll#byte-WebPubSubContentType}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final byte[] message, final WebPubSubContentType contentType) {
        sendToAllWithResponse(message, contentType, null, Context.NONE);
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.byte}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubserviceclient.sendToAllWithResponse.byte.withexclusion}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToAllWithResponse(final byte[] message,
                                                final WebPubSubContentType contentType,
                                                final Iterable<String> excludedConnectionIds,
                                                final Context context) {
        return asyncClient.sendToAllWithResponse(message, contentType, excludedConnectionIds, context).block();
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToUser(final String userId, final String message) {
        sendToUser(userId, message, null);
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     * @param contentType The content type of the message.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToUser(final String userId, final String message, final WebPubSubContentType contentType) {
        sendToUserWithResponse(userId, message, contentType, Context.NONE);
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToUserWithResponse(final String userId,
                                                 final String message,
                                                 final WebPubSubContentType contentType,
                                                 final Context context) {
        return asyncClient.sendToUserWithResponse(userId, message, contentType, context).block();
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The binary message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToUser(final String userId, final byte[] message) {
        sendToUser(userId, message, null);
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToUser(final String userId, final byte[] message, final WebPubSubContentType contentType) {
        sendToUserWithResponse(userId, message, contentType, Context.NONE);
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToUserWithResponse(final String userId,
                                                 final byte[] message,
                                                 final WebPubSubContentType contentType,
                                                 final Context context) {
        return asyncClient.sendToUserWithResponse(userId, message, contentType, context).block();
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToConnection(final String connectionId, final String message) {
        sendToConnection(connectionId, message, null);
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     * @param contentType The content type of the message.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToConnection(final String connectionId,
                                 final String message,
                                 final WebPubSubContentType contentType) {
        sendToConnectionWithResponse(connectionId, message, contentType, Context.NONE);
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToConnectionWithResponse(final String connectionId,
                                                       final String message,
                                                       final WebPubSubContentType contentType,
                                                       final Context context) {
        return asyncClient.sendToConnectionWithResponse(connectionId, message, contentType, context).block();
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The binary message to send.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToConnection(final String connectionId, final byte[] message) {
        sendToConnection(connectionId, message, null);
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     */
    @ServiceMethod(returns = SINGLE)
    public void sendToConnection(final String connectionId,
                                 final byte[] message,
                                 final WebPubSubContentType contentType) {
        sendToConnectionWithResponse(connectionId, message, contentType, Context.NONE);
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *     the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToConnectionWithResponse(final String connectionId,
                                                       final byte[] message,
                                                       final WebPubSubContentType contentType,
                                                       final Context context) {
        return asyncClient.sendToConnectionWithResponse(connectionId, message, contentType, context).block();
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
        return asyncClient.removeUserFromAllGroupsWithResponse(userId, context).block();
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     * @return A Boolean value representing whether the user exists in this hub.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean checkUserExists(final String userId) {
        return checkUserExistsWithResponse(userId, Context.NONE).getValue();
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
    public Response<Boolean> checkUserExistsWithResponse(final String userId, final Context context) {
        return asyncClient.checkUserExistsWithResponse(userId, context).block();
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     * @return A Boolean value representing whether the group exists.
     */
    @ServiceMethod(returns = SINGLE)
    public boolean checkGroupExists(final String group) {
        return checkGroupExistsWithResponse(group, Context.NONE).getValue();
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
    public Response<Boolean> checkGroupExistsWithResponse(final String group, final Context context) {
        return asyncClient.checkGroupExistsWithResponse(group, context).block();
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
    public boolean checkConnectionExists(final String connectionId) {
        return checkConnectionExistsWithResponse(connectionId, Context.NONE).getValue();
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
    public Response<Boolean> checkConnectionExistsWithResponse(final String connectionId, final Context context) {
        return asyncClient.checkConnectionExistsWithResponse(connectionId, context).block();
    }

    /**
     * Check if a connection has permission to the specified action.
     *
     * @param permission The permission to be checked against the given connection ID.
     * @param connectionId Target connection Id.
     * @param targetName Get the permission for the specific target. The meaning of the target depends on the specific
     * permission.
     * @return A Boolean value representing whether the connection has the specified permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public boolean checkPermissionExists(final WebPubSubPermission permission, final String connectionId,
                                         final String targetName) {
        return checkPermissionExistsWithResponse(permission, connectionId, targetName, Context.NONE).getValue();
    }

    /**
     * Check if a connection has permission to the specified action.
     *
     * @param permission The permission to be checked against the given connection ID.
     * @param connectionId Target connection Id.
     * @param targetName Get the permission for the specific target. The meaning of the target depends on the specific
     * permission.
     * @param context The context to associate with this operation.
     * @return A {@link Response} with a Boolean value representing whether the connection
     * has the specified permission, as well as status code and response headers representing the response from
     * the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Boolean> checkPermissionExistsWithResponse(final WebPubSubPermission permission,
                                                               final String connectionId,
                                                               final String targetName,
                                                               final Context context) {
        return asyncClient.checkPermissionExistsWithResponse(permission, connectionId, targetName, context).block();
    }

    /**
     * Grant permission to the specified connection.
     *
     * @param permission The permission to be granted to the given connection ID.
     * @param connectionId Target connection ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void grantPermission(final WebPubSubPermission permission, final String connectionId) {
        this.grantPermissionWithResponse(permission, connectionId, null, Context.NONE).getValue();
    }

    /**
     * Grant permission to the specified connection.
     *
     * @param permission The permission to be granted to the given connection ID.
     * @param connectionId Target connection ID.
     * @param targetName The specific target that will have the permission granted.
     * @param context The context to associate with the operation.
     * @return A void {@link Response}, enabling retrieval of the status code and response
     *      headers from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> grantPermissionWithResponse(final WebPubSubPermission permission,
                                                            final String connectionId,
                                                            final String targetName,
                                                            final Context context) {
        return asyncClient.grantPermissionWithResponse(permission, connectionId, targetName, context).block();
    }

    /**
     * Revoke permission for the specified connection.
     *
     * @param permission The permission to be revoked.
     * @param connectionId Target connection ID.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void revokePermission(final WebPubSubPermission permission, final String connectionId) {
        revokePermissionWithResponse(permission, connectionId, null, Context.NONE);
    }

    /**
     * Revoke permission for the specified connection.
     *
     * @param permission The permission to be revoked.
     * @param connectionId Target connection ID.
     * @param targetName Revoke the permission for the specific target.
     * @param context The context to associate with this operation.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> revokePermissionWithResponse(final WebPubSubPermission permission,
                                                       final String connectionId,
                                                       final String targetName,
                                                       Context context) {
        return asyncClient.revokePermissionWithResponse(permission, connectionId, targetName, context).block();
    }
}
