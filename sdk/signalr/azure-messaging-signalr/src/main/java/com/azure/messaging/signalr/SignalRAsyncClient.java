// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.signalr;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.signalr.implementation.HealthApisImpl;
import com.azure.messaging.signalr.implementation.WebSocketConnectionApisImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * The asynchronous client for connecting to a SignalR hub (for a synchronous API, refer to the {@link SignalRClient}
 * class documentation). To create an instance of this class, refer to the code snippet below, and for more information
 * about configuration options, refer to the JavaDoc for {@link SignalRClientBuilder}.
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
 * {@link #getGroupAsyncClient(String)}, which will return a {@link SignalRGroupAsyncClient}. All operations performed
 * on this SignalR group client will take into account the hub and group in which it exists.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.messaging.signalr.secretclientbuilder.connectionstring.async}
 *
 * @see SignalRClientBuilder
 * @see SignalRClient
 * @see SignalRGroupAsyncClient
 */
@ServiceClient(
    builder = SignalRClientBuilder.class,
    isAsync = true,
    serviceInterfaces = WebSocketConnectionApisImpl.WebSocketConnectionApisService.class
)
public final class SignalRAsyncClient {
    static final String SIGNALR_TRACING_NAMESPACE_VALUE = "Microsoft.SignalRService";

    private final ClientLogger logger = new ClientLogger(SignalRAsyncClient.class);

    private final WebSocketConnectionApisImpl webSocketApis;
    private final HealthApisImpl healthApis;

    // The name of the hub this client is connected to
    private final String hub;

    // The SignalR API version being used by this client
    private final SignalRServiceVersion serviceVersion;

    // package-private (instantiated through builder)
    SignalRAsyncClient(final WebSocketConnectionApisImpl webSocketApis,
                       final HealthApisImpl healthApis,
                       final String hub,
                       final SignalRServiceVersion serviceVersion) {
        this.webSocketApis = webSocketApis;
        this.healthApis = healthApis;
        this.hub = hub;
        this.serviceVersion = serviceVersion;
    }

    // TODO (jgiles) this is a nicer pattern than what we have in existing client libraries
    static Context configureTracing(final Context context) {
        return (context == null ? Context.NONE : context)
           .addData(AZ_TRACING_NAMESPACE_KEY, SIGNALR_TRACING_NAMESPACE_VALUE);
    }

    /**
     * Creates a new {@link SignalRGroupAsyncClient asynchronous group client} for connecting to a specified SignalR
     * group, which will also remain associated with the hub that was previously specified for the calling client
     * instance in its builder (set via {@link SignalRClientBuilder#hub(String)}.
     *
     * @param group The name of the group.
     * @return A new client for connecting to a specified SignalR group.
     */
    public SignalRGroupAsyncClient getGroupAsyncClient(final String group) {
        return new SignalRGroupAsyncClient(webSocketApis, hub, group);
    }

    /**
     * Returns status information related to the SignalR service, in particular whether it is considered
     * {@link SignalRHubStatus#isAvailable() available}.
     *
     * @return status information related to the SignalR service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SignalRHubStatus> getStatus() {
        return withContext(context -> healthApis
                   .getHealthStatusWithResponseAsync(context)// TODO (jgiles) we should introduce a withResponse overload
                   .map(SignalRHubStatus::new));
    }

    /**
     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.String}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.String.2}
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
     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.List}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the second argument:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrasyncclient.sendToAll.String.List.2}
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
    Mono<Response<Void>> sendToAll(final String message, final List<String> excludedUsers, Context context) {
        context = configureTracing(context);
        return (hub == null
                    ? webSocketApis.defaultHubBroadcastWithResponseAsync(message, excludedUsers, context)
                    : webSocketApis.broadcastWithResponseAsync(hub, message, excludedUsers, context))
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting message '{}'", message))
           .doOnSuccess(response -> logger.info("Broadcasted message: '{}', response: {}",
               message, response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast message '{}', response: {}", message, error));
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} var-args (keeping in mind that it is valid to provide no excluded connection IDs).
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.String}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.String.2}
     *
     * @param message The message to send.
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
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.List}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.signalr.signalrasyncclient.sendToAllBytes.byte.List.2}
     *
     * @param message The message to send.
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
                ? webSocketApis.defaultHubBroadcastWithResponseAsync(byteFlux, message.length, excludedConnectionIds, context)
                : webSocketApis.broadcastWithResponseAsync(hub, byteFlux, message.length, excludedConnectionIds, context))
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting binary data"))
           .doOnSuccess(response -> logger.info("Broadcasted binary data, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast binary data, response: {}", error));
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUser(final String userId, final String message) {
        return withContext(context -> sendToUser(userId, message, context));
    }

    // package-private
    Mono<Response<Void>> sendToUser(final String userId, final String message, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.sendToDefaultHubUserWithResponseAsync(userId, message, context)
                : webSocketApis.sendToUserWithResponseAsync(hub, userId, message, context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending to user '{}' message: '{}'", userId, message))
           .doOnSuccess(response -> logger.info("Sent to user '{}' message: '{}', response: {}",
               userId, message, response.getValue()))
           .doOnError(error -> logger.warning("Failed to send message '{}' to user '{}', response: {}",
               message, userId, error));
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The binary message to send.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUser(final String userId, final byte[] message) {
        return withContext(context -> sendToUser(userId, message, context));
    }

    // package-private
    Mono<Response<Void>> sendToUser(final String userId, final byte[] message, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.sendToDefaultHubUserWithResponseAsync(userId, byteFlux, message.length, context)
                : webSocketApis.sendToUserWithResponseAsync(hub, userId, byteFlux, message.length, context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending binary data to user"))
           .doOnSuccess(response -> logger.info("Sent binary data to user, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to send binary data to user, response: {}", error));
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnection(final String connectionId, final String message) {
        return withContext(context -> sendToConnection(connectionId, message, context));
    }

    // package-private
    Mono<Response<Void>> sendToConnection(final String connectionId, final String message, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.sendToDefaultHubConnectionWithResponseAsync(connectionId, message, context)
                : webSocketApis.sendToConnectionWithResponseAsync(hub, connectionId, message, context))
           .doOnSubscribe(ignoredValue ->
               logger.info("Sending to connection '{}' message: '{}'", connectionId, message))
           .doOnSuccess(response ->
               logger.info("Sent to connection '{}' message: '{}', response: {}",
                   connectionId, message, response.getValue()))
           .doOnError(error ->
               logger.warning("Failed to send message '{}' to connection '{}', response: {}",
                   message, connectionId, error));
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The binary message to send.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnection(final String connectionId, final byte[] message) {
        return withContext(context -> sendToConnectionWithResponse(connectionId, message, context));
    }

    // package-private
    Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId,
                                                      final byte[] message,
                                                      Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.sendToDefaultHubConnectionWithResponseAsync(connectionId, byteFlux, message.length, context)
                : webSocketApis.sendToConnectionWithResponseAsync(hub, connectionId, byteFlux, message.length, context))
           .doOnSubscribe(ignoredValue -> logger.info("Sending binary message to connection"))
           .doOnSuccess(response -> logger.info("Sent binary message to connection, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to send binary message to connection, response: {}", error));
    }

    /**
     * Remove a specific user from all groups they are joined to.
     *
     * @param userId The user ID to remove from all groups.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserFromAllGroups(final String userId) {
        return withContext(context -> removeUserFromAllGroups(userId, context));
    }

    // package-private
    Mono<Response<Void>> removeUserFromAllGroups(final String userId, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.removeUserFromAllDefaultHubGroupsWithResponseAsync(userId, context)
                : webSocketApis.removeUserFromAllGroupsWithResponseAsync(hub, userId, context))
           .doOnSubscribe(ignoredValue -> logger.info("Removing user '{}' from all groups"))
           .doOnSuccess(response -> logger.info("Removed user '{}' from all groups, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to remove user '{}' from all groups, response: {}", error));
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the user exists in
     *      this hub, as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> userExists(final String userId) {
        return userExistsWithResponse(userId).map(Response::getValue);
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the user exists in
     *      this hub, as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> userExistsWithResponse(final String userId) {
        return withContext(context -> userExistsWithResponse(userId, context));
    }

    // package-private
    Mono<Response<Boolean>> userExistsWithResponse(final String userId, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.checkDefaultHubUserExistenceWithResponseAsync(userId, context)
                : webSocketApis.checkUserExistenceWithResponseAsync(hub, userId, context))
           .doOnSubscribe(ignoredValue -> logger.info("Checking if user '{}' exists", userId))
           .doOnSuccess(response -> logger.info("Checked if user '{}' exists, response: {}",
               userId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if user '{}' exists, response: {}", userId, error));
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the group exists,
     *      as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> groupExists(final String group) {
        return groupExistsWithResponse(group).map(Response::getValue);
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the group exists,
     *      as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> groupExistsWithResponse(final String group) {
        return withContext(context -> groupExistsWithResponse(group, context));
    }

    // package-private
    Mono<Response<Boolean>> groupExistsWithResponse(final String group, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.checkDefaultHubUserExistenceWithResponseAsync(group, context)
                : webSocketApis.checkUserExistenceWithResponseAsync(hub, group, context))
           .doOnSubscribe(ignoredValue -> logger.info("Checking if group '{}' exists", group))
           .doOnSuccess(response -> logger.info("Checked if group '{}' exists, response: {}",
               group, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if group '{}' exists, response: {}", group, error));
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnection(final String connectionId) {
        return closeConnection(connectionId, null);
    }

    /**
     * Close a specific connection to this hub.
     *
     * @param connectionId Connection ID to close.
     * @param reason The reason why the connection was closed.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> closeConnection(final String connectionId, final String reason) {
        return withContext(context -> closeConnectionWithResponse(connectionId, reason, context));
    }

    // package-private
    Mono<Response<Void>> closeConnectionWithResponse(final String connectionId,
                                                     final String reason,
                                                     Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.closeDefaultHubClientConnectionWithResponseAsync(connectionId, reason, context)
                : webSocketApis.closeClientConnectionWithResponseAsync(hub, connectionId, reason, context))
           .doOnSubscribe(ignoredValue -> logger.info("Closing connection {}", connectionId))
           .doOnSuccess(response -> logger.info("Closed connection {}, response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to close connection {}, response: {}", connectionId, error));
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the connection
     *     exists in this hub, as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> connectionExists(final String connectionId) {
        return connectionExistsWithResponse(connectionId).map(Response::getValue);
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the connection
     *     exists in this hub, as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> connectionExistsWithResponse(final String connectionId) {
        return withContext(context -> connectionExistsWithResponse(connectionId, context));
    }

    // package-private
    Mono<Response<Boolean>> connectionExistsWithResponse(final String connectionId, Context context) {
        context = configureTracing(context);
        return (hub == null
                ? webSocketApis.checkDefaultHubConnectionExistenceWithResponseAsync(connectionId, context)
                : webSocketApis.checkConnectionExistenceWithResponseAsync(hub, connectionId, context))
           .doOnSubscribe(ignoredValue -> logger.info("Checking if connection '{}' exists", connectionId))
           .doOnSuccess(response -> logger.info("Checked if connection '{}' exists, response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if connection '{}' exists, response: {}",
               connectionId, error));
    }
}
