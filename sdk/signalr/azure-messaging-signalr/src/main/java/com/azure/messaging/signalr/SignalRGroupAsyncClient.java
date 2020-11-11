// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.signalr.implementation.WebSocketConnectionApisImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.messaging.signalr.SignalRAsyncClient.configureTracing;

/**
 * <p>Async client for interacting with a specific SignalR group, contained within a SignalR hub. An instance of this
 * group client is able to be created by calling {@link SignalRAsyncClient#getGroupAsyncClient(String)} on a
 * {@link SignalRAsyncClient} instance. All operations performed on an instance of a group client takes into account
 * the specified group name, as well as the name of the hub specified when instantiating the
 * {@link SignalRAsyncClient} via the {@link SignalRClientBuilder}. This is demonstrated in the code below:</p>
 *
 * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.instance}
 *
 * @see SignalRAsyncClient
 * @see SignalRClientBuilder
 */
@ServiceClient(
    builder = SignalRClientBuilder.class,
    isAsync = true,
    serviceInterfaces = WebSocketConnectionApisImpl.WebSocketConnectionApisService.class
)
public final class SignalRGroupAsyncClient {
    private final ClientLogger logger = new ClientLogger(SignalRGroupAsyncClient.class);

    private final WebSocketConnectionApisImpl webSocketApis;

    // The name of the hub this group belongs to.
    private final String hub;

    // The name of this group
    private final String group;

    // Package-private (instantiated through SignalRAsyncClient)
    SignalRGroupAsyncClient(final WebSocketConnectionApisImpl webSocketApis, final String hub, final String group) {
        this.webSocketApis = webSocketApis;
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
     * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.String}
     *
     * <p>To send a message to all users within the same group, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.String.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional var-args of connection IDs to not broadcast the message to.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(final String message, final String... excludedConnectionIds) {
        return withContext(context -> sendToAll(message,
            excludedConnectionIds == null ? Collections.emptyList() : Arrays.asList(excludedConnectionIds),
            context));
    }

    /**
     * Broadcast a text message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.List}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the second argument:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.sendToAll.String.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(final String message, final List<String> excludedConnectionIds) {
        return withContext(context -> sendToAll(message, excludedConnectionIds, context));
    }

    // package-private
    Mono<Response<Void>> sendToAll(final String message,
                                   final List<String> excludedConnectionIds,
                                   Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.defaultHubGroupBroadcastWithResponseAsync(
                    group, message, excludedConnectionIds, context)
                : webSocketApis.groupBroadcastWithResponseAsync(hub, group, message, excludedConnectionIds, context))
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting message '{}'", message))
           .doOnSuccess(response ->
                            logger.info("Broadcasted message: '{}', response: {}", message, response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast message '{}', response: {}", message, error));
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.String}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.String.2}
     *
     * @param message The binary message to send.
     * @param excludedConnectionIds An optional var-args of connection IDs to not broadcast the message to.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(final byte[] message, final String... excludedConnectionIds) {
        return withContext(context -> sendToAll(message,
            excludedConnectionIds == null ? Collections.emptyList() : Arrays.asList(excludedConnectionIds),
            context));
    }

    /**
     * Broadcast a binary message to all connections in this group, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.List}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrgroupasyncclient.sendToAllBytes.byte.List.2}
     *
     * @param message The binary message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAll(final byte[] message, final List<String> excludedConnectionIds) {
        return withContext(context -> sendToAll(message, excludedConnectionIds, context));
    }

    // package-private
    Mono<Response<Void>> sendToAll(final byte[] message,
                                   final List<String> excludedConnectionIds,
                                   Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.defaultHubGroupBroadcastWithResponseAsync(
                    group, byteFlux, message.length, excludedConnectionIds, context)
                : webSocketApis.groupBroadcastWithResponseAsync(
                    hub, group, byteFlux, message.length, excludedConnectionIds, context))
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting binary message"))
           .doOnSuccess(response -> logger.info("Broadcasted binary message, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast binary message, response: {}", error));
    }

    /**
     * Add a user to this group that will remain in the group until they are manually removed.
     *
     * @param userId The user name to add.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addUser(final String userId) {
        return withContext(context -> addUserWithResponse(userId, null, context));
    }

    /**
     * Add a user to this group with a specified time-to-live before that user will be removed. A null time to live will
     * mean that the user remains in the group indefinitely.
     *
     * @param userId The user name to add
     * @param timeToLive Specifies the duration that the user exists in the group. If not set, the user lives in the
     * group forever.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addUser(final String userId, final Duration timeToLive) {
        return withContext(context -> addUserWithResponse(userId, timeToLive, context));
    }

    // package-private
    Mono<Response<Void>> addUserWithResponse(final String userId, final Duration timeToLive, Context context) {
        // The user can set a TTL for how long the added user may last in the group. We must null check it (as that
        // means 'indefinitely', but we also must be careful with the Duration object, as its 'getSeconds()' methods
        // returns a long value. If the long value exceeds Integer.MAX_VALUE, then we will throw an exception
        Integer ttl = null;
        if (timeToLive != null) {
            final long ttlLong = timeToLive.getSeconds();
            if (ttlLong > Integer.MAX_VALUE) {
                logger.logThrowableAsError(new IllegalArgumentException(
                    "timeToLive represents how long the user is added to the group in seconds. Null is valid and"
                    + "represents an indefinite existence in the group, otherwise the duration must be between 0 and "
                    + "Integer.MAX_VALUE seconds. The provided value is " + ttlLong + " seconds."));
            } else {
                ttl = (int) Math.min(Integer.MAX_VALUE, ttlLong);
            }
        }

        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.addUserToDefaultHubGroupWithResponseAsync(group, userId, ttl, context)
                : webSocketApis.addUserToGroupWithResponseAsync(hub, group, userId, ttl, context))
           .doOnSubscribe(ignoredValue -> logger.info("Adding user '{}'", userId))
           .doOnSuccess(response -> logger.info("Added user '{}', response: {}", userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to add user '{}', response: {}", userId, error));
    }

    /**
     * Remove a user from this group.
     *
     * @param userId The user name to remove.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUser(final String userId) {
        return withContext(context -> removeUserWithResponse(userId, context));
    }

    // package-private
    Mono<Response<Void>> removeUserWithResponse(final String userId, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.removeUserFromDefaultHubGroupWithResponseAsync(group, userId, context)
                : webSocketApis.removeUserFromGroupWithResponseAsync(hub, group, userId, context))
           .doOnSubscribe(ignoredValue -> logger.info("Removing user '{}'", userId))
           .doOnSuccess(response -> logger.info("Removed user '{}', response: {}", userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to remove user '{}', response: {}", userId, error));
    }

    /**
     * Check if a user is in this group.
     *
     * @param userId The user name to check for.
     * @return A {@link Mono} containing a Boolean value representing whether the user exists in this group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> userExists(final String userId) {
        return userExistsWithResponse(userId).map(Response::getValue);
    }

    /**
     * Check if a user is in this group.
     *
     * @param userId The user name to check for.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the user exists in
     *     this group, as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> userExistsWithResponse(final String userId) {
        return withContext(context -> userExistsWithResponse(userId, context));
    }

    // package-private
    Mono<Response<Boolean>> userExistsWithResponse(final String userId, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.checkUserExistenceInDefaultHubGroupWithResponseAsync(group, userId, context)
                : webSocketApis.checkUserExistenceInGroupWithResponseAsync(hub, group, userId, context))
           .doOnSubscribe(ignoredValue -> logger.info("Checking if user '{}' exists", userId))
           .doOnSuccess(response -> logger.info("Checked if user '{}' exists, response: {}",
               userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if user '{}' exists, response: {}", userId, error));
    }

    /**
     * Add a specific connection to this group.
     *
     * @param connectionId The connection id to add to this group.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addConnection(final String connectionId) {
        return withContext(context -> addConnectionWithResponse(connectionId, context));
    }

    // package-private
    Mono<Response<Void>> addConnectionWithResponse(final String connectionId, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.addConnectionToDefaultHubGroupWithResponseAsync(group, connectionId, context)
                : webSocketApis.addConnectionToGroupWithResponseAsync(hub, group, connectionId, context))
           .doOnSubscribe(ignoredValue -> logger.info("Adding connection '{}'", connectionId))
           .doOnSuccess(response -> logger.info("Added connection '{}', response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to add connection '{}', response: {}", connectionId, error));
    }

    /**
     * Remove a specific connection from this group.
     *
     * @param connectionId The connection id to remove from this group.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeConnection(final String connectionId) {
        return withContext(context -> removeConnectionWithResponse(connectionId, context));
    }

    // package-private
    Mono<Response<Void>> removeConnectionWithResponse(final String connectionId, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.removeConnectionFromDefaultHubGroupWithResponseAsync(group, connectionId, context)
                : webSocketApis.removeConnectionFromGroupWithResponseAsync(hub, group, connectionId, context))
           .doOnSubscribe(ignoredValue -> logger.info("Removing connection '{}'", connectionId))
           .doOnSuccess(response -> logger.info("Removed connection '{}', response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to remove connection '{}', response: {}", connectionId, error));
    }
}
