// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

import static com.azure.core.annotation.ReturnType.SINGLE;

/**
 * <p>Sync client for interacting with a specific Azure Web Pub Sub group, contained within an Azure Web Pub Sub hub.
 * An instance of this group client is able to be created by calling
 * {@link WebPubSubServiceClient#getGroup(String)} on a {@link WebPubSubServiceClient} instance. All
 * operations performed on an instance of a group client takes into account the specified group name, as well as the
 * name of the hub specified when instantiating the {@link WebPubSubServiceClient} via the
 * {@link WebPubSubClientBuilder}. This is demonstrated in the code below:</p>
 *
 * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.instance}
 *
 * @see WebPubSubAsyncServiceClient
 * @see WebPubSubClientBuilder
 */
public final class WebPubSubGroup {
    private final WebPubSubAsyncGroup asyncGroupClient;

    // Package-private (instantiated through WebPubSubServiceClient)
    WebPubSubGroup(final WebPubSubAsyncGroup asyncGroupClient) {
        this.asyncGroupClient = asyncGroupClient;
    }

    /**
     * Broadcast a text message to all connections in this group.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same group, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#String}
     *
     * @param message The message to send.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final String message) {
        sendToAll(message, null);
    }

    /**
     * Broadcast a text message to all connections in this group.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same group, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#String-WebPubSubContentType}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final String message, final WebPubSubContentType contentType) {
        sendToAllWithResponse(message, contentType, null, Context.NONE);
    }

    /**
     * Broadcast a text message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the third argument:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.withexclusions}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @param excludedConnectionIds An optional var-args of connection IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToAllWithResponse(final String message,
                                                final WebPubSubContentType contentType,
                                                final Iterable<String> excludedConnectionIds,
                                                final Context context) {
        return asyncGroupClient.sendToAllWithResponse(message, contentType, excludedConnectionIds, context).block();
    }

    /**
     * Broadcast a binary message to all connections in this group.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#byte}
     *
     * @param message The binary message to send.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final byte[] message) {
        sendToAll(message, null);
    }

    /**
     * Broadcast a binary message to all connections in this group.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.sendToAll#byte-WebPubSubContentType}
     *
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendToAll(final byte[] message, final WebPubSubContentType contentType) {
        sendToAllWithResponse(message, contentType, null, Context.NONE);
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.byte}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubgroup.sendToAllWithResponse.byte.withexclusion}
     *
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> sendToAllWithResponse(final byte[] message,
                                                final WebPubSubContentType contentType,
                                                final Iterable<String> excludedConnectionIds,
                                                final Context context) {
        return asyncGroupClient.sendToAllWithResponse(message, contentType, excludedConnectionIds, context).block();
    }

    /**
     * Add a user to this group.
     *
     * @param userId The user name to add
     */
    @ServiceMethod(returns = SINGLE)
    public void addUser(final String userId) {
        addUserWithResponse(userId, Context.NONE);
    }

    /**
     * Add a user to this group.
     *
     * @param userId The user name to add.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} with a null value, but status code and response headers representing the response from
      *      the service.
     */
    @ServiceMethod(returns = SINGLE)
    public Response<Void> addUserWithResponse(final String userId, final Context context) {
        return asyncGroupClient.addUserWithResponse(userId, context).block();
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
