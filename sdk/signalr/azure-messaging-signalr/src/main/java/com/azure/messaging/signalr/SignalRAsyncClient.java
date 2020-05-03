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

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * Client for connecting to a SignalR hub.
 */
@ServiceClient(
    builder = SignalRClientBuilder.class,
    isAsync = true
//    serviceInterfaces = WebSocketConnectionApisService.class // TODO (jgiles) private interface, can't set it
)
public final class SignalRAsyncClient {
    static final String SIGNALR_TRACING_NAMESPACE_VALUE = "Microsoft.SignalRService";

    private final ClientLogger logger = new ClientLogger(SignalRAsyncClient.class);

    private final AzureWebSocketServiceRestAPI innerClient;
    private final WebSocketConnectionApis api;

    // The name of the hub this client is connected to
    private final String hub;

    // The SignalR API version being used by this client
    private final SignalRServiceVersion serviceVersion;

    // package-private (instantiated through builder)
    SignalRAsyncClient(final AzureWebSocketServiceRestAPI innerClient,
                       final String hub,
                       final SignalRServiceVersion serviceVersion) {
        this.innerClient = innerClient;
        this.api = innerClient.getWebSocketConnectionApis();
        this.hub = hub;
        this.serviceVersion = serviceVersion;
    }

    // TODO (jgiles) this is a nicer pattern than what we have in existing client libraries
    static Context configureTracing(final Context context) {
        return (context == null ? Context.NONE : context)
           .addData(AZ_TRACING_NAMESPACE_KEY, SIGNALR_TRACING_NAMESPACE_VALUE);
    }

    public SignalRGroupAsyncClient getGroupClient(final String group) {
        return new SignalRGroupAsyncClient(innerClient, hub, group);
    }

    /**
     * Returns whether the service is considered healthy.
     * @return whether the service is considered healthy.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> isServiceHealthy() {
        return innerClient.getHealthApis()
                   .headIndexWithResponseAsync(Context.NONE)// TODO (jgiles) we should introduce a withResponse overload
                   .map(Response::getStatusCode)
                   .map(code -> code == 200);
    }

    /**
     * Broadcast a text message to all connections on this hub.
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
     * Broadcast a text message to all connections on this hub.
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
        return api.postBroadcastWithResponseAsync(hub, data, excludedUsers, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting data '{}'", data))
           .doOnSuccess(response -> logger.info("Broadcasted data: '{}', response: {}", data, response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast data '{}', response: {}", data, error));
    }

    /**
     * Broadcast a binary message to all connections on this hub.
     *
     * @param data The message to send.
     * @param excludedUsers An optional var-args of user IDs to not broadcast the message to.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> broadcast(final byte[] data, final String... excludedUsers) {
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
    public Mono<Response<Void>> broadcast(final byte[] data, final List<String> excludedUsers) {
        return broadcast(data, excludedUsers, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> broadcast(final byte[] data, final List<String> excludedUsers, final Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return api.postBroadcastWithResponseAsync(hub, byteFlux, data.length, excludedUsers, context)
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting binary data"))
           .doOnSuccess(response -> logger.info("Broadcasted binary data, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast binary data, response: {}", error));
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param data The message to send.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUser(final String userId, final String data) {
        return sendToUser(userId, data, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> sendToUser(final String userId, final String data, final Context context) {
        return api.postSendToUserWithResponseAsync(hub, userId, data, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending to user '{}' data: '{}'", userId, data))
           .doOnSuccess(response -> logger.info("Sent to user '{}' data: '{}', response: {}",
               userId, data, response.getValue()))
           .doOnError(error -> logger.warning("Failed to send data '{}' to user '{}', response: {}",
               data, userId, error));
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param data The binary message to send.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUser(final String userId, final byte[] data) {
        return sendToUser(userId, data, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> sendToUser(final String userId, final byte[] data, final Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return api.postSendToUserWithResponseAsync(hub, userId, byteFlux, data.length, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending binary data to user"))
           .doOnSuccess(response -> logger.info("Sent binary data to user, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to send binary data to user, response: {}", error));
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param data The message to send.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnection(final String connectionId, final String data) {
        return sendToConnection(connectionId, data, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> sendToConnection(final String connectionId, final String data, final Context context) {
        return api.postSendToConnectionWithResponseAsync(hub, connectionId, data, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending to connection '{}' data: '{}'", connectionId, data))
           .doOnSuccess(response -> logger.info("Sent to connection '{}' data: '{}', response: {}",
               connectionId, data, response.getValue()))
           .doOnError(error -> logger.warning("Failed to send data '{}' to connection '{}', response: {}",
               data, connectionId, error));
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param data The binary message to send.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnection(final String connectionId, final byte[] data) {
        return sendToConnectionWithResponse(connectionId, data, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId,
                                                      final byte[] data,
                                                      final Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(data));
        return api.postSendToConnectionWithResponseAsync(
            hub, connectionId, byteFlux, data.length, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending binary data to connection"))
           .doOnSuccess(response -> logger.info("Sent binary data to connection, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to send binary data to connection, response: {}", error));
    }

    /**
     * Remove a specific user from all groups they are joined to.
     *
     * @param userId The user ID to remove from all groups.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserFromAllGroups(final String userId) {
        return removeUserFromAllGroups(userId, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> removeUserFromAllGroups(final String userId, final Context context) {
        return api.deleteRemoveUserFromAllGroupsWithResponseAsync(hub, userId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Removing user '{}' from all groups"))
           .doOnSuccess(response -> logger.info("Removed user '{}' from all groups, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to remove user '{}' from all groups, response: {}", error));
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> doesUserExist(final String userId) {
        return doesUserExistWithResponse(userId).map(SimpleResponse::getValue);
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> doesUserExistWithResponse(final String userId) {
        return doesUserExistWithResponse(userId, Context.NONE);
    }

    // package-private
    Mono<SimpleResponse<Boolean>> doesUserExistWithResponse(final String userId, final Context context) {
        return api.headCheckUserExistenceWithResponseAsync(hub, userId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Checking if user '{}' exists", userId))
           .doOnSuccess(response -> logger.info("Checked if user '{}' exists, response: {}",
               userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if user '{}' exists, response: {}", userId, error));
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> doesGroupExist(final String group) {
        return doesGroupExistWithResponse(group).map(SimpleResponse::getValue);
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> doesGroupExistWithResponse(final String group) {
        return doesGroupExistWithResponse(group, Context.NONE);
    }

    // package-private
    Mono<SimpleResponse<Boolean>> doesGroupExistWithResponse(final String group, final Context context) {
        return api.headCheckUserExistenceWithResponseAsync(hub, group, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Checking if group '{}' exists", group))
           .doOnSuccess(response -> logger.info("Checked if group '{}' exists, response: {}",
               group, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if group '{}' exists, response: {}", group, error));
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnection(final String connectionId) {
        return closeConnection(connectionId, null);
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnection(final String connectionId, final String reason) {
        return closeConnectionWithResponse(connectionId, reason, Context.NONE);
    }

    // package-private
    Mono<Response<Void>> closeConnectionWithResponse(final String connectionId,
                                                     final String reason,
                                                     final Context context) {
        return api.deleteCloseClientConnectionWithResponseAsync(hub, connectionId, reason, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Closing connection {}", connectionId))
           .doOnSuccess(response -> logger.info("Closed connection {}, response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to close connection {}, response: {}", connectionId, error));
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> doesConnectionExist(final String connectionId) {
        return doesConnectionExistWithResponse(connectionId).map(SimpleResponse::getValue);
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<Boolean>> doesConnectionExistWithResponse(final String connectionId) {
        return doesConnectionExistWithResponse(connectionId, Context.NONE);
    }

    // package-private
    Mono<SimpleResponse<Boolean>> doesConnectionExistWithResponse(final String connectionId, final Context context) {
        return api.headCheckConnectionExistenceWithResponseAsync(hub, connectionId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Checking if connection '{}' exists", connectionId))
           .doOnSuccess(response -> logger.info("Checked if connection '{}' exists, response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if connection '{}' exists, response: {}",
               connectionId, error));
    }
}
