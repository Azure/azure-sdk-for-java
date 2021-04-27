// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.implementation.WebPubSubsImpl;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.messaging.webpubsub.WebPubSubAsyncServiceClient.configureTracing;
import static com.azure.messaging.webpubsub.models.WebPubSubContentType.APPLICATION_JSON;
import static com.azure.messaging.webpubsub.models.WebPubSubContentType.APPLICATION_OCTET_STREAM;

/**
 * <p>Async client for interacting with a specific Azure Web Pub Sub group, contained within a Azure Web Pub Sub hub.
 * An instance of this group client is able to be created by calling
 * {@link WebPubSubAsyncServiceClient#getAsyncGroup(String)} on a {@link WebPubSubAsyncServiceClient} instance. All operations
 * performed on an instance of a group client takes into account the specified group name, as well as the name of the
 * hub specified when instantiating the {@link WebPubSubAsyncServiceClient} via the {@link WebPubSubClientBuilder}. This is
 * demonstrated in the code below:</p>
 *
 * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.instance}
 *
 * @see WebPubSubAsyncServiceClient
 * @see WebPubSubClientBuilder
 */
public final class WebPubSubAsyncGroup {
    private final ClientLogger logger = new ClientLogger(WebPubSubAsyncGroup.class);

    private final WebPubSubsImpl webPubSubApis;

    // The name of the hub this group belongs to.
    private final String hub;

    // The name of this group
    private final String group;

    // Package-private (instantiated through WebPubSubAsyncServiceClient)
    WebPubSubAsyncGroup(final WebPubSubsImpl webPubSubApis, final String hub, final String group) {
        this.webPubSubApis = webPubSubApis;
        this.hub = hub;
        this.group = group;
    }

    /**
     * Broadcast a text message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same group, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#String}
     *
     *  @param message The message to send.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final String message) {
        return sendToAll(message, null);
    }

    /**
     * Broadcast a text message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same group, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#String-WebPubSubContentType}
     *
     *  @param message The message to send.
     * @param contentType The content type of the message.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final String message, final WebPubSubContentType contentType) {
        return sendToAllWithResponse(message, contentType, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Broadcast a text message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the third argument:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.withexclusions}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAllWithResponse(final String message,
                                                      final WebPubSubContentType contentType,
                                                      final Iterable<String> excludedConnectionIds) {
        return withContext(context -> sendToAllWithResponse(message, contentType, excludedConnectionIds, context));
    }

    // package-private
    Mono<Response<Void>> sendToAllWithResponse(final String message,
                                               WebPubSubContentType contentType,
                                               final Iterable<String> excludedConnectionIds,
                                               final Context context) {
        contentType = contentType == null ? APPLICATION_JSON : contentType;
        switch (contentType) {
            case TEXT_PLAIN:
                return webPubSubApis.sendToGroupWithResponseAsync(hub, group, message, excludedConnectionIds, configureTracing(context))
                   .doOnSubscribe(ignoredValue -> logger.verbose("Broadcasting message"))
                   .doOnSuccess(response -> logger.verbose("Broadcasted message, response: {}", response.getValue()))
                   .doOnError(error -> logger.warning("Failed to broadcast message, response: {}", error));
            default:
            case APPLICATION_JSON:
            case APPLICATION_OCTET_STREAM:
                return sendToAllWithResponse(message.getBytes(StandardCharsets.UTF_8), contentType,
                    excludedConnectionIds, context);
        }
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#byte}
     *
     * @param message The binary message to send.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final byte[] message) {
        return sendToAll(message, null);
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAll#byte-WebPubSubContentType}
     *
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final byte[] message, final WebPubSubContentType contentType) {
        return sendToAllWithResponse(message, contentType, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.byte}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to a list and as third parameter to this method.</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncgroup.sendToAllWithResponse.byte.withexclusion}
     *
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAllWithResponse(final byte[] message,
                                                      final WebPubSubContentType contentType,
                                                      final Iterable<String> excludedConnectionIds) {
        return withContext(context -> sendToAllWithResponse(message, contentType, excludedConnectionIds, context));
    }

    // package-private
    Mono<Response<Void>> sendToAllWithResponse(final byte[] message,
                                               WebPubSubContentType contentType,
                                               final Iterable<String> excludedConnectionIds,
                                               final Context context) {
        contentType = contentType == null ? APPLICATION_OCTET_STREAM : contentType;
        switch (contentType) {
            case TEXT_PLAIN:
                return sendToAllWithResponse(new String(message, StandardCharsets.UTF_8), contentType,
                    excludedConnectionIds, context);
            default:
            case APPLICATION_OCTET_STREAM:
            case APPLICATION_JSON:
                final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
                return webPubSubApis.sendToGroupWithResponseAsync(
                    hub, group, contentType, byteFlux, message.length, excludedConnectionIds, configureTracing(context))
                           .doOnSubscribe(ignoredValue -> logger.verbose("Broadcasting binary message"))
                           .doOnSuccess(response -> logger.verbose("Broadcasted binary message, response: {}", response.getValue()))
                           .doOnError(error -> logger.warning("Failed to broadcast binary message, response: {}", error));
        }
    }

    /**
     * Add a user to this group that will remain in the group until they are manually removed.
     *
     * @param userId The user name to add.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addUser(final String userId) {
        return addUserWithResponse(userId, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Add a user to this group.
     *
     * @param userId The user name to add
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addUserWithResponse(final String userId) {
        return withContext(context -> addUserWithResponse(userId, context));
    }

    // package-private
    Mono<Response<Void>> addUserWithResponse(final String userId, final Context context) {
        return webPubSubApis.addUserToGroupWithResponseAsync(hub, group, userId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Adding user '{}'", userId))
           .doOnSuccess(response -> logger.verbose("Added user '{}', response: {}", userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to add user '{}', response: {}", userId, error));
    }

    /**
     * Remove a user from this group.
     *
     * @param userId The user name to remove.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeUser(final String userId) {
        return removeUserWithResponse(userId).flatMap(FluxUtil::toMono);
    }

    /**
     * Remove a user from this group.
     *
     * @param userId The user name to remove.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserWithResponse(final String userId) {
        return withContext(context -> removeUserWithResponse(userId, context));
    }

    // package-private
    Mono<Response<Void>> removeUserWithResponse(final String userId, final Context context) {
        return webPubSubApis.removeUserFromGroupWithResponseAsync(hub, group, userId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Removing user '{}'", userId))
           .doOnSuccess(response -> logger.verbose("Removed user '{}', response: {}", userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to remove user '{}', response: {}", userId, error));
    }

    /**
     * Add a specific connection to this group.
     *
     * @param connectionId The connection id to add to this group.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addConnection(final String connectionId) {
        return addConnectionWithResponse(connectionId).flatMap(FluxUtil::toMono);
    }

    /**
     * Add a specific connection to this group.
     *
     * @param connectionId The connection id to add to this group.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addConnectionWithResponse(final String connectionId) {
        return withContext(context -> addConnectionWithResponse(connectionId, context));
    }

    // package-private
    Mono<Response<Void>> addConnectionWithResponse(final String connectionId, final Context context) {
        return webPubSubApis.addConnectionToGroupWithResponseAsync(hub, group, connectionId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Adding connection '{}'", connectionId))
           .doOnSuccess(response -> logger.verbose("Added connection '{}', response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to add connection '{}', response: {}", connectionId, error));
    }

    /**
     * Remove a specific connection from this group.
     *
     * @param connectionId The connection id to remove from this group.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeConnection(final String connectionId) {
        return removeConnectionWithResponse(connectionId).flatMap(FluxUtil::toMono);
    }

    /**
     * Remove a specific connection from this group.
     *
     * @param connectionId The connection id to remove from this group.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeConnectionWithResponse(final String connectionId) {
        return withContext(context -> removeConnectionWithResponse(connectionId, context));
    }

    // package-private
    Mono<Response<Void>> removeConnectionWithResponse(final String connectionId, final Context context) {
        return webPubSubApis.removeConnectionFromGroupWithResponseAsync(hub, group, connectionId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Removing connection '{}'", connectionId))
           .doOnSuccess(response -> logger.verbose("Removed connection '{}', response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to remove connection '{}', response: {}", connectionId, error));
    }
}
