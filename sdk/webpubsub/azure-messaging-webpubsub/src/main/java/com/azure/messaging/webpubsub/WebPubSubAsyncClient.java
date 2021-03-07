// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.webpubsub.implementation.HealthApisImpl;
import com.azure.messaging.webpubsub.implementation.WebPubSubApisImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * The asynchronous client for connecting to an Azure Web Pub Sub hub (for a synchronous API, refer to the
 * {@link WebPubSubClient} class documentation). To create an instance of this class, refer to the code snippet below,
 * and for more information about configuration options, refer to the JavaDoc for {@link WebPubSubClientBuilder}.
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
 *     <li>To check the existence of a {@link #userExists(String) user}, a {@link #connectionExists(String) connection},
 *     or a {@link #groupExists(String) group},</li>
 * </ul>
 *
 * <p>It is possible to connect to a specific group within a hub by calling
 * {@link #getGroupAsyncClient(String)}, which will return a {@link WebPubSubGroupAsyncClient}. All operations performed
 * on this Azure Web Pub Sub group client will take into account the hub and group in which it exists.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.async}
 *
 * @see WebPubSubClientBuilder
 * @see WebPubSubClient
 * @see WebPubSubGroupAsyncClient
 */
@ServiceClient(
    builder = WebPubSubClientBuilder.class,
    isAsync = true,
    serviceInterfaces = WebPubSubApisImpl.WebPubSubApisService.class
)
public final class WebPubSubAsyncClient {
    // TODO (jogiles) find the appropriate name
    static final String TRACING_NAMESPACE_VALUE = "Microsoft.WebSubPubService";

    private final ClientLogger logger = new ClientLogger(WebPubSubAsyncClient.class);

    private final WebPubSubApisImpl webSubPubApis;

    // The name of the hub this client is connected to
    private final String hub;

    // The Azure Web Pub Sub API version being used by this client
    private final WebPubSubServiceVersion serviceVersion;

    // package-private (instantiated through builder)
    WebPubSubAsyncClient(final WebPubSubApisImpl webSubPubApis,
                         final String hub,
                         final WebPubSubServiceVersion serviceVersion) {
        this.webSubPubApis = webSubPubApis;
        this.hub = hub;
        this.serviceVersion = serviceVersion;
    }

    // TODO (jgiles) this is a nicer pattern than what we have in existing client libraries
    static Context configureTracing(final Context context) {
        return (context == null ? Context.NONE : context)
           .addData(AZ_TRACING_NAMESPACE_KEY, TRACING_NAMESPACE_VALUE);
    }

    /**
     * Creates a new {@link WebPubSubGroupAsyncClient asynchronous group client} for connecting to a specified
     * Azure Web Pub Sub group, which will also remain associated with the hub that was previously specified for the
     * calling client instance in its builder (set via {@link WebPubSubClientBuilder#hub(String)}.
     *
     * @param group The name of the group.
     * @return A new client for connecting to a specified Azure Web Pub Sub group.
     */
    public WebPubSubGroupAsyncClient getGroupAsyncClient(final String group) {
        return new WebPubSubGroupAsyncClient(webSubPubApis, hub, group);
    }

//    /**
//     * Returns status information related to the Azure Web Pub Sub service, in particular whether it is considered
//     * {@link WebPubSubHubStatus#isAvailable() available}.
//     *
//     * @return status information related to the Azure Web Pub Sub service.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<WebPubSubHubStatus> getStatus() {
//        return withContext(context -> healthApis
//                   .getHealthStatusWithResponseAsync(context)// TODO (jgiles) we should introduce a withResponse overload
//                   .map(WebPubSubHubStatus::new));
//    }

    /**
     * Broadcast a text message to all connections on this hub.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.String}
     *
     * @param message The message to send.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final String message) {
        return sendToAllWithResponse(message, null).flatMap(FluxUtil::toMono);
    }

//    /**
//     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
//     * {@code excludedConnectionIds} list.
//     *
//     * <p><strong>Code Samples</strong></p>
//     *
//     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
//     *
//     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List}
//     *
//     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
//     * excluded connection IDs to a List and pass that in as the second argument:</p>
//     *
//     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List.2}
//     *
//     * @param message The message to send.
//     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
//     * @return An empty {@link Mono}.
//     */
//    @ServiceMethod(returns = ReturnType.SINGLE)
//    public Mono<Void> sendToAll(final String message, final List<String> excludedConnectionIds) {
//        return sendToAllWithResponse(message, excludedConnectionIds).flatMap(FluxUtil::toMono);
//    }

    /**
     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the second argument:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAll.String.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional iterable of connection IDs to not broadcast the message to.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAllWithResponse(final String message, final List<String> excludedConnectionIds) {
        return withContext(context -> sendToAllWithResponse(message, excludedConnectionIds, context));
    }

    // package-private
    Mono<Response<Void>> sendToAllWithResponse(final String message,
                                               final List<String> excludedUsers,
                                               final Context context) {
        return webSubPubApis.broadcastWithResponseAsync(hub, message, excludedUsers, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting message '{}'", message))
           .doOnSuccess(response -> logger.info("Broadcasted message: '{}', response: {}",
               message, response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast message '{}', response: {}", message, error));
    }

    /**
     * Broadcast a binary message to all connections on this hub.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.String}
     *
     * @param message The message to send.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final byte[] message) {
        return sendToAllWithResponse(message, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncclient.sendToAllBytes.byte.List.2}
     *
     * @param message The message to send.
     * @param excludedConnectionIds An optional list of connection IDs to not broadcast the message to.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToAllWithResponse(final byte[] message, final List<String> excludedConnectionIds) {
        return withContext(context -> sendToAllWithResponse(message, excludedConnectionIds, context));
    }

    // package-private
    Mono<Response<Void>> sendToAllWithResponse(final byte[] message,
                                   final List<String> excludedConnectionIds,
                                   Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
        context = configureTracing(context);

        return webSubPubApis.broadcastWithResponseAsync(byteFlux, message.length, hub, excludedConnectionIds, context)
           .doOnSubscribe(ignoredValue -> logger.info("Broadcasting binary data"))
           .doOnSuccess(response -> logger.info("Broadcasted binary data, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to broadcast binary data, response: {}", error));
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToUser(final String userId, final String message) {
        return sendToUserWithResponse(userId, message).flatMap(FluxUtil::toMono);
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
    public Mono<Response<Void>> sendToUserWithResponse(final String userId, final String message) {
        return withContext(context -> sendToUserWithResponse(userId, message, context));
    }

    // package-private
    Mono<Response<Void>> sendToUserWithResponse(final String userId, final String message, Context context) {
        return webSubPubApis.sendToUserWithResponseAsync(hub, userId, message, configureTracing(context))
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
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToUser(final String userId, final byte[] message) {
        return sendToUserWithResponse(userId, message).flatMap(FluxUtil::toMono);
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
    public Mono<Response<Void>> sendToUserWithResponse(final String userId, final byte[] message) {
        return withContext(context -> sendToUserWithResponse(userId, message, context));
    }

    // package-private
    Mono<Response<Void>> sendToUserWithResponse(final String userId, final byte[] message, Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
        context = configureTracing(context);
        return webSubPubApis.sendToUserWithResponseAsync(userId, byteFlux, message.length, hub, context)
           .doOnSubscribe(ignoredValue -> logger.info("Sending binary data to user"))
           .doOnSuccess(response -> logger.info("Sent binary data to user, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to send binary data to user, response: {}", error));
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToConnection(final String connectionId, final String message) {
        return sendToConnectionWithResponse(connectionId, message).flatMap(FluxUtil::toMono);
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
    public Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId, final String message) {
        return withContext(context -> sendToConnectionWithResponse(connectionId, message, context));
    }

    // package-private
    Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId, final String message, Context context) {
        return webSubPubApis.sendToConnectionWithResponseAsync(hub, connectionId, message, configureTracing(context))
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
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToConnection(final String connectionId, final byte[] message) {
        return sendToConnectionWithResponse(connectionId, message).flatMap(FluxUtil::toMono);
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
    public Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId, final byte[] message) {
        return withContext(context -> sendToConnectionWithResponse(connectionId, message, context));
    }

    // package-private
    Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId,
                                                      final byte[] message,
                                                      Context context) {
        final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
        context = configureTracing(context);
        return webSubPubApis.sendToConnectionWithResponseAsync(connectionId, byteFlux, message.length, hub, context)
           .doOnSubscribe(ignoredValue -> logger.info("Sending binary message to connection"))
           .doOnSuccess(response -> logger.info("Sent binary message to connection, response: {}", response.getValue()))
           .doOnError(error -> logger.warning("Failed to send binary message to connection, response: {}", error));
    }

    /**
     * Remove a specific user from all groups they are joined to.
     *
     * @param userId The user ID to remove from all groups.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeUserFromAllGroups(final String userId) {
        return removeUserFromAllGroupsWithResponse(userId).flatMap(FluxUtil::toMono);
    }

    /**
     * Remove a specific user from all groups they are joined to.
     *
     * @param userId The user ID to remove from all groups.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeUserFromAllGroupsWithResponse(final String userId) {
        return withContext(context -> removeUserFromAllGroupsWithResponse(userId, context));
    }

    // package-private
    Mono<Response<Void>> removeUserFromAllGroupsWithResponse(final String userId, Context context) {
        return webSubPubApis.removeUserFromAllGroupsWithResponseAsync(hub, userId, configureTracing(context))
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
        return webSubPubApis.checkUserExistenceWithResponseAsync(hub, userId, context)
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
        return webSubPubApis.checkUserExistenceWithResponseAsync(hub, group, context)
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
    public Mono<Void> closeConnection(final String connectionId) {
        return closeConnectionWithResponse(connectionId, null).flatMap(FluxUtil::toMono);
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
    public Mono<Response<Void>> closeConnectionWithResponse(final String connectionId, final String reason) {
        return withContext(context -> closeConnectionWithResponse(connectionId, reason, context));
    }

    // package-private
    Mono<Response<Void>> closeConnectionWithResponse(final String connectionId,
                                                     final String reason,
                                                     Context context) {
        context = configureTracing(context);
        return webSubPubApis.closeClientConnectionWithResponseAsync(hub, connectionId, reason, context)
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
        return webSubPubApis.checkConnectionExistenceWithResponseAsync(hub, connectionId, context)
           .doOnSubscribe(ignoredValue -> logger.info("Checking if connection '{}' exists", connectionId))
           .doOnSuccess(response -> logger.info("Checked if connection '{}' exists, response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if connection '{}' exists, response: {}",
               connectionId, error));
    }
}
