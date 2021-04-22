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
import com.azure.messaging.webpubsub.implementation.WebPubSubsImpl;
import com.azure.messaging.webpubsub.models.GetAuthenticationTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubAuthenticationToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.azure.messaging.webpubsub.models.WebPubSubPermission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;
import static com.azure.messaging.webpubsub.models.WebPubSubContentType.APPLICATION_JSON;
import static com.azure.messaging.webpubsub.models.WebPubSubContentType.APPLICATION_OCTET_STREAM;

/**
 * The asynchronous client for connecting to an Azure Web Pub Sub hub (for a synchronous API, refer to the
 * {@link WebPubSubServiceClient} class documentation). To create an instance of this class, refer to the code snippet
 * below, and for more information about configuration options, refer to the JavaDoc for {@link WebPubSubClientBuilder}.
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
 * {@link #getAsyncGroup(String)}, which will return a {@link WebPubSubAsyncGroup}. All operations performed
 * on this Azure Web Pub Sub group client will take into account the hub and group in which it exists.</p>
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.messaging.webpubsub.webpubsubclientbuilder.connectionstring.async}
 *
 * @see WebPubSubClientBuilder
 * @see WebPubSubServiceClient
 * @see WebPubSubAsyncGroup
 */
@ServiceClient(
    builder = WebPubSubClientBuilder.class,
    isAsync = true,
    serviceInterfaces = WebPubSubsImpl.WebPubSubsService.class
)
public final class WebPubSubAsyncServiceClient {
    static final String TRACING_NAMESPACE_VALUE = "Microsoft.SignalRService";

    private final ClientLogger logger = new ClientLogger(WebPubSubAsyncServiceClient.class);

    private final WebPubSubsImpl webPubSubApis;

    // The name of the hub this client is connected to
    private final String hub;

    private final String endpoint;

    private final WebPubSubAuthenticationPolicy webPubSubAuthPolicy;

    // The Azure Web Pub Sub API version being used by this client
    private final WebPubSubServiceVersion serviceVersion;

    // package-private (instantiated through builder)
    WebPubSubAsyncServiceClient(final WebPubSubsImpl webPubSubApis,
                                final String hub,
                                final String endpoint,
                                final WebPubSubAuthenticationPolicy webPubSubAuthPolicy,
                                final WebPubSubServiceVersion serviceVersion) {
        this.webPubSubApis = webPubSubApis;
        this.hub = hub;
        this.endpoint = endpoint;
        this.webPubSubAuthPolicy = webPubSubAuthPolicy;
        this.serviceVersion = serviceVersion;
    }

    // TODO (jgiles) this is a nicer pattern than what we have in existing client libraries
    static Context configureTracing(final Context context) {
        return (context == null ? Context.NONE : context)
           .addData(AZ_TRACING_NAMESPACE_KEY, TRACING_NAMESPACE_VALUE);
    }

    /**
     * Creates a new asynchronous {@link WebPubSubGroup} for connecting to a specified
     * Azure Web Pub Sub group, which will also remain associated with the hub that was previously specified for the
     * calling client instance in its builder (set via {@link WebPubSubClientBuilder#hub(String)}.
     *
     * @param group The name of the group.
     * @return A new client for connecting to a specified Azure Web Pub Sub group.
     */
    public WebPubSubAsyncGroup getAsyncGroup(final String group) {
        return new WebPubSubAsyncGroup(webPubSubApis, hub, group);
    }

    /**
     * Creates an authentication token.
     *
     * @param options Options to apply when creating the authentication token.
     * @return A new authentication token instance.
     */
    public WebPubSubAuthenticationToken getAuthenticationToken(GetAuthenticationTokenOptions options) {
        final String endpoint = this.endpoint.endsWith("/") ? this.endpoint : this.endpoint + "/";
        final String audience = endpoint + "client/hubs/" + hub;

        final String authToken = WebPubSubAuthenticationPolicy.getAuthenticationToken(
            audience, options, webPubSubAuthPolicy.getCredential());

        // The endpoint should always be http or https and client endpoint should be ws or wss respectively.
        final String clientEndpoint = endpoint.replaceFirst("http", "ws");
        final String clientUrl = clientEndpoint + "client/hubs/" + hub;

        final String url = clientUrl + "?access_token=" + authToken;

        return new WebPubSubAuthenticationToken(authToken, url);
    }

    /**
     * Broadcast a text message to all connections on this hub.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.WebPubSubAsyncServiceClient.sendToAll#String}
     *
     * @param message The message to send.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final String message) {
        return sendToAllWithResponse(message, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Broadcast a text message to all connections on this hub.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#String-WebPubSubContentType}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final String message, final WebPubSubContentType contentType) {
        return sendToAllWithResponse(message, contentType, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Broadcast a text message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse}
     *
     * <p>To send a message to all users within the same hub, with one or more connection IDs excluded, simply add the
     * excluded connection IDs to a List and pass that in as the third argument:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.withexclusions}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @param excludedConnectionIds An optional iterable of connection IDs to not broadcast the message to.
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
                return webPubSubApis.sendToAllWithResponseAsync(
                        hub, message, excludedConnectionIds, configureTracing(context))
                   .doOnSubscribe(ignoredValue -> logger.verbose("Broadcasting message"))
                   .doOnSuccess(response -> logger.verbose("Broadcasted message, response: {}", response.getValue()))
                   .doOnError(error -> logger.warning("Failed to broadcast message, response: {}", error));
            default:
            case APPLICATION_OCTET_STREAM:
            case APPLICATION_JSON:
                return sendToAllWithResponse(
                    message.getBytes(StandardCharsets.UTF_8), contentType, excludedConnectionIds, context);

        }
    }

    /**
     * Broadcast a binary message to all connections on this hub.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#byte}
     *
     * @param message The message to send.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final byte[] message) {
        return sendToAllWithResponse(message, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Broadcast a binary message to all connections on this hub.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAll#byte-WebPubSubContentType}
     *
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToAll(final byte[] message, final WebPubSubContentType contentType) {
        return sendToAllWithResponse(message, contentType, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Broadcast a binary message to all connections on this hub, excluding any connection IDs provided in the
     * {@code excludedConnectionIds} list.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>To send a binary message to all users within the same hub, with no exclusions, do the following:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.byte}
     *
     * <p>To send a binary message to all users within the same hub, with one or more connection IDs excluded, simply
     * add the excluded connection IDs to the end of the method call as var-args:</p>
     *
     * {@codesnippet com.azure.messaging.webpubsub.webpubsubasyncserviceclient.sendToAllWithResponse.byte.withexclusion}
     *
     * @param message The message to send.
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

                return webPubSubApis.sendToAllWithResponseAsync(
                    hub, contentType, byteFlux, message.length, excludedConnectionIds, configureTracing(context))
                       .doOnSubscribe(ignoredValue -> logger.verbose("Broadcasting binary data"))
                       .doOnSuccess(response ->
                            logger.verbose("Broadcasted binary data, response: {}", response.getValue()))
                       .doOnError(error -> logger.warning("Failed to broadcast binary data, response: {}", error));
        }
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
        return sendToUserWithResponse(userId, message, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToUser(final String userId, final String message, final WebPubSubContentType contentType) {
        return sendToUserWithResponse(userId, message, contentType).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a text message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUserWithResponse(final String userId,
                                                       final String message,
                                                       final WebPubSubContentType contentType) {
        return withContext(context -> sendToUserWithResponse(userId, message, contentType, context));
    }

    // package-private
    Mono<Response<Void>> sendToUserWithResponse(final String userId,
                                                final String message,
                                                WebPubSubContentType contentType,
                                                Context context) {

        contentType = contentType == null ? APPLICATION_JSON : contentType;
        switch (contentType) {
            case TEXT_PLAIN:
                return webPubSubApis.sendToUserWithResponseAsync(hub, userId, message, configureTracing(context))
                   .doOnSubscribe(ignoredValue ->
                       logger.verbose("Sending to user '{}'", userId, message))
                   .doOnSuccess(response ->
                       logger.verbose("Sent to user '{}', response: {}", userId, response.getValue()))
                   .doOnError(error ->
                       logger.warning("Failed to send message to user '{}', response: {}", message, userId, error));
            default:
            case APPLICATION_JSON:
            case APPLICATION_OCTET_STREAM:
                return sendToUserWithResponse(userId, message.getBytes(StandardCharsets.UTF_8), contentType, context);

        }
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
        return sendToUserWithResponse(userId, message, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToUser(final String userId, final byte[] message, final WebPubSubContentType contentType) {
        return sendToUserWithResponse(userId, message, contentType).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a binary message to a specific user.
     *
     * @param userId User name to send to.
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToUserWithResponse(final String userId,
                                                       final byte[] message,
                                                       final WebPubSubContentType contentType) {
        return withContext(context -> sendToUserWithResponse(userId, message, contentType, context));
    }

    // package-private
    Mono<Response<Void>> sendToUserWithResponse(final String userId,
                                                final byte[] message,
                                                WebPubSubContentType contentType,
                                                final Context context) {
        contentType = contentType == null ? APPLICATION_OCTET_STREAM : contentType;

        switch (contentType) {
            case TEXT_PLAIN:
                return sendToUserWithResponse(userId, new String(message, StandardCharsets.UTF_8),
                    contentType, context);
            default:
            case APPLICATION_OCTET_STREAM:
            case APPLICATION_JSON:
                final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
                return webPubSubApis.sendToUserWithResponseAsync(
                        hub, userId, contentType, byteFlux, message.length, configureTracing(context))
                   .doOnSubscribe(ignoredValue -> logger.verbose("Sending binary data to user"))
                   .doOnSuccess(response ->
                        logger.verbose("Sent binary data to user, response: {}", response.getValue()))
                   .doOnError(error -> logger.warning("Failed to send binary data to user, response: {}", error));
        }
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
        return sendToConnectionWithResponse(connectionId, message, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToConnection(final String connectionId,
                                       final String message,
                                       final WebPubSubContentType contentType) {
        return sendToConnectionWithResponse(connectionId, message, contentType).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The message to send.
     * @param contentType The content type of the message.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId,
                                                             final String message,
                                                             final WebPubSubContentType contentType) {
        return withContext(context -> sendToConnectionWithResponse(connectionId, message, contentType, context));
    }

    // package-private
    Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId,
                                                      final String message,
                                                      WebPubSubContentType contentType,
                                                      final Context context) {
        contentType = contentType == null ? APPLICATION_JSON : contentType;
        switch (contentType) {
            case TEXT_PLAIN:
                return webPubSubApis.sendToConnectionWithResponseAsync(
                        hub, connectionId, message, configureTracing(context))
                    .doOnSubscribe(ignoredValue -> logger.verbose("Sending to connection '{}'", connectionId))
                    .doOnSuccess(response ->
                        logger.verbose("Sent to connection '{}', response: {}", connectionId, response.getValue()))
                    .doOnError(error ->
                        logger.warning("Failed to send message to connection '{}', response: {}", connectionId, error));
            default:
            case APPLICATION_JSON:
            case APPLICATION_OCTET_STREAM:
                return sendToConnectionWithResponse(connectionId, message.getBytes(StandardCharsets.UTF_8),
                    contentType, context);
        }
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
        return sendToConnectionWithResponse(connectionId, message, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @return An empty {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> sendToConnection(final String connectionId,
                                       final byte[] message,
                                       final WebPubSubContentType contentType) {
        return sendToConnectionWithResponse(connectionId, message, contentType).flatMap(FluxUtil::toMono);
    }

    /**
     * Send a binary message to a specific connection
     *
     * @param connectionId Connection ID to send to.
     * @param message The binary message to send.
     * @param contentType The content type of the message.
     * @return A {@link Mono} containing a {@link Response} with a null value, but status code and response headers
     *      representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId,
                                                             final byte[] message,
                                                             final WebPubSubContentType contentType) {
        return withContext(context -> sendToConnectionWithResponse(connectionId, message, contentType, context));
    }

    // package-private
    Mono<Response<Void>> sendToConnectionWithResponse(final String connectionId,
                                                      final byte[] message,
                                                      WebPubSubContentType contentType,
                                                      final Context context) {

        contentType = contentType == null ? APPLICATION_OCTET_STREAM : contentType;

        switch (contentType) {
            case TEXT_PLAIN:
                return sendToConnectionWithResponse(connectionId, new String(message, StandardCharsets.UTF_8),
                    contentType, context);
            default:
            case APPLICATION_OCTET_STREAM:
            case APPLICATION_JSON:
                final Flux<ByteBuffer> byteFlux = Flux.just(ByteBuffer.wrap(message));
                return webPubSubApis.sendToConnectionWithResponseAsync(
                       hub, connectionId, contentType, byteFlux, message.length, configureTracing(context))
                    .doOnSubscribe(ignoredValue -> logger.verbose("Sending binary message to connection"))
                    .doOnSuccess(response ->
                         logger.verbose("Sent binary message to connection, response: {}", response.getValue()))
                    .doOnError(error -> logger.warning("Failed to send binary message to connection, response: {}", error));
        }
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
    Mono<Response<Void>> removeUserFromAllGroupsWithResponse(final String userId, final Context context) {
        return webPubSubApis.removeUserFromAllGroupsWithResponseAsync(hub, userId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Removing user '{}' from all groups"))
           .doOnSuccess(response ->
                logger.verbose("Removed user '{}' from all groups, response: {}", response.getValue()))
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
    public Mono<Boolean> checkUserExists(final String userId) {
        return checkUserExistsWithResponse(userId).map(Response::getValue);
    }

    /**
     * Check if a particular user is connected to this hub.
     *
     * @param userId The user name to check for.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the user exists in
     *      this hub, as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> checkUserExistsWithResponse(final String userId) {
        return withContext(context -> checkUserExistsWithResponse(userId, context));
    }

    // package-private
    Mono<Response<Boolean>> checkUserExistsWithResponse(final String userId, final Context context) {
        return webPubSubApis.userExistsWithResponseAsync(hub, userId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Checking if user '{}' exists", userId))
           .doOnSuccess(response -> logger.verbose("Checked if user '{}' exists, response: {}",
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
    public Mono<Boolean> checkGroupExists(final String group) {
        return checkGroupExistsWithResponse(group).map(Response::getValue);
    }

    /**
     * Check if a particular group exists (i.e. has active connections).
     *
     * @param group The group name to check for.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the group exists,
     *      as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> checkGroupExistsWithResponse(final String group) {
        return withContext(context -> checkGroupExistsWithResponse(group, context));
    }

    // package-private
    Mono<Response<Boolean>> checkGroupExistsWithResponse(final String group, final Context context) {
        return webPubSubApis.groupExistsWithResponseAsync(hub, group, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Checking if group '{}' exists", group))
           .doOnSuccess(response -> logger.verbose("Checked if group '{}' exists, response: {}",
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
                                                     final Context context) {
        return webPubSubApis.closeClientConnectionWithResponseAsync(hub, connectionId, reason, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Closing connection {}", connectionId))
           .doOnSuccess(response -> logger.verbose("Closed connection {}, response: {}",
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
    public Mono<Boolean> checkConnectionExists(final String connectionId) {
        return checkConnectionExistsWithResponse(connectionId).map(Response::getValue);
    }

    /**
     * Check if a specific connection is connected to this hub.
     *
     * @param connectionId Connection ID to check.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the connection
     *     exists in this hub, as well as status code and response headers representing the response from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> checkConnectionExistsWithResponse(final String connectionId) {
        return withContext(context -> checkConnectionExistsWithResponse(connectionId, context));
    }

    // package-private
    Mono<Response<Boolean>> checkConnectionExistsWithResponse(final String connectionId, final Context context) {
        return webPubSubApis.connectionExistsWithResponseAsync(hub, connectionId, configureTracing(context))
           .doOnSubscribe(ignoredValue -> logger.verbose("Checking if connection '{}' exists", connectionId))
           .doOnSuccess(response -> logger.verbose("Checked if connection '{}' exists, response: {}",
               connectionId, response.getValue()))
           .doOnError(error -> logger.warning("Failed to check if connection '{}' exists, response: {}",
               connectionId, error));
    }

    /**
     * Check if a connection has permission to the specified action.
     *
     * @param permission The permission to be checked against the given connection ID.
     * @param connectionId Target connection Id.
     * @param targetName Get the permission for the specific target. The meaning of the target depends on the
     * specific permission.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the connection
     *     has the specified permission.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boolean> checkPermissionExists(final WebPubSubPermission permission, final String connectionId,
                                               final String targetName) {
        return checkPermissionExistsWithResponse(permission, connectionId, targetName).map(Response::getValue);
    }

    /**
     * Check if a connection has permission to the specified action.
     *
     * @param permission The permission to be checked against the given connection ID.
     * @param connectionId Target connection Id.
     * @param targetName Get the permission for the specific target. The meaning of the target depends on the
     * specific permission.
     * @return A {@link Mono} containing a {@link Response} with a Boolean value representing whether the connection
      *     has the specified permission, as well as status code and response headers representing the response from
     *      the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Boolean>> checkPermissionExistsWithResponse(final WebPubSubPermission permission,
                                                                     final String connectionId,
                                                                     final String targetName) {
        return withContext(context -> checkPermissionExistsWithResponse(permission, connectionId, targetName, context));
    }

    // package-private
    Mono<Response<Boolean>> checkPermissionExistsWithResponse(final WebPubSubPermission permission,
                                                              final String connectionId,
                                                              final String targetName,
                                                              final Context context) {
        return webPubSubApis.checkPermissionWithResponseAsync(hub, permission, connectionId, targetName, configureTracing(context))
                   .doOnSubscribe(ignoredValue -> logger.verbose("Checking if permission '{}' exists for connection '{}'",
                       permission, connectionId))
                   .doOnSuccess(response -> logger.verbose("Checked if permission '{}' exists, response: {}",
                       connectionId, response.getValue()))
                   .doOnError(error -> logger.warning("Failed to check if permission '{}' exists, response: {}",
                       connectionId, error));
    }

    /**
     * Grant permission to the specified connection.
     *
     * @param permission The permission to be granted to the given connection ID.
     * @param connectionId Target connection ID.
     * @return A {@link Mono} containing a void {@link Response}, enabling retrieval of the status code and response
     *      headers from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> grantPermission(final WebPubSubPermission permission, final String connectionId) {
        return grantPermissionWithResponse(permission, connectionId, null).map(Response::getValue);
    }

    /**
     * Grant permission to the specified connection.
     *
     * @param permission The permission to be granted to the given connection ID.
     * @param connectionId Target connection ID.
     * @param targetName The specific target that will have the permission granted.
     * @return A {@link Mono} containing a void {@link Response}, enabling retrieval of the status code and response
     *      headers from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> grantPermissionWithResponse(final WebPubSubPermission permission,
                                                            final String connectionId,
                                                            final String targetName) {
        return withContext(context -> grantPermissionWithResponse(permission, connectionId, targetName, context));
    }

    // package-private
    Mono<Response<Void>> grantPermissionWithResponse(final WebPubSubPermission permission,
                                                     final String connectionId,
                                                     final String targetName,
                                                     final Context context) {
        return webPubSubApis.grantPermissionWithResponseAsync(hub, permission, connectionId, targetName, configureTracing(context))
                   .doOnSubscribe(ignoredValue -> logger.verbose("Granting permission '{}' for connection '{}'",
                       permission, connectionId))
                   .doOnSuccess(response -> logger.verbose("Granted permission '{}', response: {}",
                       connectionId, response.getValue()))
                   .doOnError(error -> logger.warning("Failed to grant permission '{}', response: {}",
                       connectionId, error));
    }

    /**
     * Revoke permission for the specified connection.
     *
     * @param permission The permission to be revoked.
     * @param connectionId Target connection ID.
     * @return A {@link Mono} containing a void {@link Response}, enabling retrieval of the status code and response
     *      headers from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> revokePermission(final WebPubSubPermission permission, final String connectionId) {
        return revokePermissionWithResponse(permission, connectionId, null).map(Response::getValue);
    }

    /**
     * Revoke permission for the specified connection.
     *
     * @param permission The permission to be revoked.
     * @param connectionId Target connection ID.
     * @param targetName Revoke the permission for the specific target.
     * @return A {@link Mono} containing a void {@link Response}, enabling retrieval of the status code and response
     *      headers from the service.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> revokePermissionWithResponse(final WebPubSubPermission permission,
                                                            final String connectionId,
                                                            final String targetName) {
        return withContext(context -> revokePermissionWithResponse(permission, connectionId, targetName, context));
    }

    // package-private
    Mono<Response<Void>> revokePermissionWithResponse(final WebPubSubPermission permission,
                                                     final String connectionId,
                                                     final String targetName,
                                                     final Context context) {
        return webPubSubApis.revokePermissionWithResponseAsync(hub, permission, connectionId, targetName, configureTracing(context))
                   .doOnSubscribe(ignoredValue -> logger.verbose("Revoking permission '{}' for connection '{}'",
                       permission, connectionId))
                   .doOnSuccess(response -> logger.verbose("Revoked permission '{}', response: {}",
                       connectionId, response.getValue()))
                   .doOnError(error -> logger.warning("Failed to revoke permission '{}', response: {}",
                       connectionId, error));
    }
}
