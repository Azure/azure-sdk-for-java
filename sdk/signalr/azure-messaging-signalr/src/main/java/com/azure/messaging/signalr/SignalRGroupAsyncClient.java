// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.signalr.implementation.client.AzureWebSocketServiceRestAPI;
import com.azure.messaging.signalr.implementation.client.WebSocketConnectionApis;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.messaging.signalr.SignalRAsyncClient.configureTracing;

/**
 * Async client for connecting to a SignalR group. Created from calling
 * {@link SignalRAsyncClient#getGroupClient(String)} on a {@link SignalRAsyncClient} instance.
 *
 * @see SignalRAsyncClient
 */
@ServiceClient(
    builder = SignalRClientBuilder.class,
    isAsync = true
//    serviceInterfaces = WebSocketConnectionApisService.class // TODO (jgiles) private interface, can't set it
)
public final class SignalRGroupAsyncClient {
    private final ClientLogger logger = new ClientLogger(SignalRGroupAsyncClient.class);

    private final WebSocketConnectionApis api;

    // The name of the hub this group belongs to.
    private final String hub;

    // The name of this group
    private final String group;

    // Package-private (instantiated through SignalRAsyncClient)
    SignalRGroupAsyncClient(final AzureWebSocketServiceRestAPI innerClient, final String hub, final String group) {
        this.api = innerClient.getWebSocketConnectionApis();
        this.hub = hub;
        this.group = group;
    }

    /**
     * Send a text message to every connection in this group.
     *
     * @param data The message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> broadcast(final String data, final String... excludedUsers) {
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
    public Mono<Response<Void>> broadcast(final String data, final List<String> excludedUsers) {
        return broadcast(data, excludedUsers, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> broadcast(final String data, final List<String> excludedUsers, final Context context) {
        return api.postGroupBroadcastWithResponseAsync(hub, group, data, excludedUsers, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting data '{}'", data))
           .doOnSuccess(response -> logger.info("Broadcasted data: '{}', response: {}", data, response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast data '{}', response: {}", data, error));
    }

    /**
     * Send a binary message to every connection in this group.
     *
     * @param data The binary message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> broadcast(final byte[] data, final String... excludedUsers) {
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
    public Mono<Response<Void>> broadcast(final byte[] data, final List<String> excludedUsers) {
        return broadcast(data, excludedUsers, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> broadcast(final byte[] data, final List<String> excludedUsers, final Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return api.postGroupBroadcastWithResponseAsync(hub, group, byteFlux, data.length, excludedUsers, context)
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting binary data"))
           .doOnSuccess(response -> logger.info("Broadcasted binary data, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast binary data, response: {}", error));
    }

    /**
     * Add a user to this group
     *
     * @param userId The user name to add
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addUser(final String userId) {
        return addUserWithResponse(userId, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> addUserWithResponse(final String userId, final Context context) {
        // TODO (jgiles) null TTL
        return api.putAddUserToGroupWithResponseAsync(hub, group, userId, null, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Adding user '{}'", userId))
           .doOnSuccess(response -> logger.info("Added user '{}', response: {}", userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to add user '{}', response: {}", userId, error));
    }

    /**
     * Remove a user from this group.
     *
     * @param userId The user name to remove
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUser(final String userId) {
        return removeUserWithResponse(userId, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> removeUserWithResponse(final String userId, final Context context) {
        return api.deleteRemoveUserFromGroupWithResponseAsync(hub, group, userId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Removing user '{}'", userId))
           .doOnSuccess(response -> logger.info("Removed user '{}', response: {}", userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to remove user '{}', response: {}", userId, error));
    }

    /**
     * Check if a user is in this group
     *
     * @param userId The user name to check for
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> doesUserExist(final String userId) {
        return doesUserExistWithResponse(userId).map(SimpleResponse::getValue);
    }

    /**
     * Check if a user is in this group
     *
     * @param userId The user name to check for
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> doesUserExistWithResponse(final String userId) {
        return doesUserExistWithResponse(userId, Context.NONE);
    }

    // package-private
    Mono<SimpleResponse<Boolean>> doesUserExistWithResponse(final String userId, final Context context) {
        return api.headCheckUserExistenceInGroupWithResponseAsync(hub, group, userId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Checking if user '{}' exists", userId))
           .doOnSuccess(response -> logger.info("Checked if user '{}' exists, response: {}",
               userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if user '{}' exists, response: {}", userId, error));
    }

    /**
     * Add a specific connection to this group.
     *
     * @param connectionId The connection id to add to this group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addConnection(final String connectionId) {
        return addConnectionWithResponse(connectionId, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> addConnectionWithResponse(final String connectionId, final Context context) {
        return api.putAddConnectionToGroupWithResponseAsync(hub, group, connectionId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Adding connection '{}'", connectionId))
           .doOnSuccess(response -> logger.info("Added connection '{}', response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to add connection '{}', response: {}", connectionId, error));
    }

    /**
     * Remove a specific connection from this group.
     *
     * @param connectionId The connection id to remove from this group.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeConnection(final String connectionId) {
        return removeConnectionWithResponse(connectionId, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> removeConnectionWithResponse(final String connectionId, final Context context) {
        return api.deleteRemoveConnectionFromGroupWithResponseAsync(hub, group, connectionId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Removing connection '{}'", connectionId))
           .doOnSuccess(response -> logger.info("Removed connection '{}', response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to remove connection '{}', response: {}", connectionId, error));
    }
}
