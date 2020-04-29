// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosPermissionProperties;

import java.util.List;
import java.util.Objects;

/**
 * Helper class to buildAsyncClient {@link CosmosAsyncClient} instances
 * as logical representation of the Azure Cosmos database service.
 *
 * <pre>
 * {@code
 * ConnectionPolicy connectionPolicy = new ConnectionPolicy();
 * getConnectionPolicy.getConnectionMode(ConnectionMode.DIRECT);
 * CosmosAsyncClient client = new CosmosClientBuilder()
 *         .endpoint(serviceEndpoint)
 *         .key(key)
 *         .connectionPolicy(connectionPolicy)
 *         .consistencyLevel(ConsistencyLevel.SESSION)
 *         .buildAsyncClient();
 * }
 * </pre>
 */
@ServiceClientBuilder(serviceClients = {CosmosClient.class, CosmosAsyncClient.class})
public class CosmosClientBuilder {
    private Configs configs = new Configs();
    private String serviceEndpoint;
    private String keyOrResourceToken;
    private TokenCredential tokenCredential;
    private ConnectionPolicy connectionPolicy;
    private ConsistencyLevel desiredConsistencyLevel;
    private List<CosmosPermissionProperties> permissions;
    private CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver;
    private CosmosKeyCredential cosmosKeyCredential;
    private boolean sessionCapturingOverrideEnabled;
    private boolean connectionReuseAcrossClientsEnabled;

    /**
     * Instantiates a new Cosmos client builder.
     */
    public CosmosClientBuilder() {
    }

    /**
     * Session capturing is enabled by default for {@link ConsistencyLevel#SESSION}.
     * For other consistency levels, it is not needed, unless if you need occasionally send requests with Session
     * Consistency while the client is not configured in session.
     * <p>
     * enabling Session capturing for Session mode has no effect.
     * @param sessionCapturingOverrideEnabled session capturing override
     * @return current cosmosClientBuilder
     */
    public CosmosClientBuilder sessionCapturingOverrideEnabled(boolean sessionCapturingOverrideEnabled) {
        this.sessionCapturingOverrideEnabled = sessionCapturingOverrideEnabled;
        return this;
    }

    /**
     * Indicates if Session capturing is enabled for non Session modes.
     * The default is false.
     *
     * @return the session capturing override
     */
    boolean isSessionCapturingOverrideEnabled() {
        return this.sessionCapturingOverrideEnabled;
    }

    /**
     * Enables connections sharing across multiple Cosmos Clients. The default is false.
     *
     *
     * <pre>
     * {@code
     * ConnectionPolicy connectionPolicy = new ConnectionPolicy();
     * getConnectionPolicy.getConnectionMode(ConnectionMode.DIRECT);
     * CosmosAsyncClient client1 = new CosmosClientBuilder()
     *         .endpoint(serviceEndpoint1)
     *         .key(key1)
     *         .connectionPolicy(connectionPolicy)
     *         .consistencyLevel(ConsistencyLevel.SESSION)
     *         .connectionReuseAcrossClientsEnabled(true)
     *         .buildAsyncClient();
     *
     * CosmosAsyncClient client2 = new CosmosClientBuilder()
     *         .endpoint(serviceEndpoint2)
     *         .key(key2)
     *         .connectionPolicy(connectionPolicy)
     *         .consistencyLevel(ConsistencyLevel.SESSION)
     *         .connectionReuseAcrossClientsEnabled(true)
     *         .buildAsyncClient();
     *
     * // when configured this way client1 and client2 will share connections when possible.
     * }
     * </pre>
     *
     * When you have multiple instances of Cosmos Client in the same JVM interacting to multiple Cosmos accounts,
     * enabling this allows connection sharing in Direct mode if possible between instances of Cosmos Client.
     *
     * Please note, when setting this option, the connection configuration (e.g., socket timeout config, idle timeout
     * config) of the first instantiated client will be used for all other client instances.
     *
     * @param connectionReuseAcrossClientsEnabled connection sharing
     * @return current cosmosClientBuilder
     */
    public CosmosClientBuilder connectionReuseAcrossClientsEnabled(boolean connectionReuseAcrossClientsEnabled) {
        this.connectionReuseAcrossClientsEnabled = true;
        return this;
    }

    /**
     * Indicates whether connection sharing is enabled. The default is false.
     *
     * When you have multiple instances of Cosmos Client in the same JVM interacting to multiple Cosmos accounts,
     * enabling this allows connection sharing in Direct mode if possible between instances of Cosmos Client.
     *
     * @return the connection sharing across multiple clients
     */
    boolean isConnectionReuseAcrossClientsEnabled() {
        return this.connectionReuseAcrossClientsEnabled;
    }

    /**
     * Gets the token resolver
     *
     * @return the token resolver
     */
    CosmosAuthorizationTokenResolver getAuthorizationTokenResolver() {
        return cosmosAuthorizationTokenResolver;
    }

    /**
     * Sets the token resolver
     *
     * @param cosmosAuthorizationTokenResolver the token resolver
     * @return current cosmosClientBuilder
     */
    public CosmosClientBuilder authorizationTokenResolver(
        CosmosAuthorizationTokenResolver cosmosAuthorizationTokenResolver) {
        this.cosmosAuthorizationTokenResolver = Objects.requireNonNull(cosmosAuthorizationTokenResolver,
            "'cosmosAuthorizationTokenResolver' cannot be null.");
        this.keyOrResourceToken = null;
        this.cosmosKeyCredential= null;
        this.permissions = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets the Azure Cosmos DB endpoint the SDK will connect to
     *
     * @return the endpoint
     */
    String getEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Sets the Azure Cosmos DB endpoint the SDK will connect to
     *
     * @param endpoint the service endpoint
     * @return current Builder
     */
    public CosmosClientBuilder endpoint(String endpoint) {
        this.serviceEndpoint = endpoint;
        return this;
    }

    /**
     * Gets either a master or readonly key used to perform authentication
     * for accessing resource.
     *
     * @return the key
     */
    String getKey() {
        return keyOrResourceToken;
    }

    /**
     * Sets either a master or readonly key used to perform authentication
     * for accessing resource.
     *
     * @param key master or readonly key
     * @return current Builder.
     * @throws NullPointerException If {@code key} is {@code null}.
     */
    public CosmosClientBuilder key(String key) {
        this.keyOrResourceToken = Objects.requireNonNull(key, "'key' cannot be null.");
        this.cosmosAuthorizationTokenResolver = null;
        this.cosmosKeyCredential= null;
        this.permissions = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets a resource token used to perform authentication
     * for accessing resource.
     *
     * @return the resourceToken
     */
    String getResourceToken() {
        return keyOrResourceToken;
    }

    /**
     * Sets a resource token used to perform authentication
     * for accessing resource.
     *
     * @param resourceToken resourceToken for authentication
     * @return current Builder.
     * @throws NullPointerException If {@code resourceToken} is {@code null}.
     */
    public CosmosClientBuilder resourceToken(String resourceToken) {
        this.keyOrResourceToken = resourceToken;
        this.cosmosAuthorizationTokenResolver = null;
        this.cosmosKeyCredential= null;
        this.permissions = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets a token credential instance used to perform authentication
     * for accessing resource.
     *
     * @return the resourceToken
     */
    TokenCredential getCredential() {
        return tokenCredential;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TokenCredential}.
     * @return the updated CosmosClientBuilder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public CosmosClientBuilder credential(TokenCredential credential) {
        this.tokenCredential = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.keyOrResourceToken = null;
        this.cosmosAuthorizationTokenResolver = null;
        this.cosmosKeyCredential= null;
        this.permissions = null;
        return this;
    }

    /**
     * Gets the permission list, which contains the
     * resource tokens needed to access resources.
     *
     * @return the permission list
     */
    List<CosmosPermissionProperties> getPermissions() {
        return permissions;
    }

    /**
     * Sets the permission list, which contains the
     * resource tokens needed to access resources.
     *
     * @param permissions Permission list for authentication.
     * @return current Builder.
     * @throws NullPointerException If {@code permissions} is {@code null}.
     */
    public CosmosClientBuilder permissions(List<CosmosPermissionProperties> permissions) {
        this.permissions = Objects.requireNonNull(permissions, "'permissions' cannot be null.");
        this.keyOrResourceToken = null;
        this.cosmosAuthorizationTokenResolver = null;
        this.cosmosKeyCredential= null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Gets the {@link ConsistencyLevel} to be used
     *
     * @return the consistency level
     */
    ConsistencyLevel getConsistencyLevel() {
        return this.desiredConsistencyLevel;
    }

    /**
     * Sets the {@link ConsistencyLevel} to be used
     *
     * @param desiredConsistencyLevel {@link ConsistencyLevel}
     * @return current Builder
     */
    public CosmosClientBuilder consistencyLevel(ConsistencyLevel desiredConsistencyLevel) {
        this.desiredConsistencyLevel = desiredConsistencyLevel;
        return this;
    }

    /**
     * Gets the (@link ConnectionPolicy) to be used
     *
     * @return the connection policy
     */
    ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Sets the {@link ConnectionPolicy} to be used
     *
     * @param connectionPolicy {@link ConnectionPolicy}
     * @return current Builder
     */
    public CosmosClientBuilder connectionPolicy(ConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
        return this;
    }

    /**
     * Gets the {@link CosmosKeyCredential} to be used
     *
     * @return cosmosKeyCredential
     */
    CosmosKeyCredential getKeyCredential() {
        return cosmosKeyCredential;
    }

    /**
     * Sets the {@link CosmosKeyCredential} to be used
     *
     * @param cosmosKeyCredential {@link CosmosKeyCredential}
     * @return current cosmosClientBuilder
     * @throws NullPointerException If {@code cosmosKeyCredential} is {@code null}.
     */
    public CosmosClientBuilder keyCredential(CosmosKeyCredential cosmosKeyCredential) {
        this.cosmosKeyCredential = Objects.requireNonNull(cosmosKeyCredential, "'cosmosKeyCredential' cannot be null.");
        this.keyOrResourceToken = null;
        this.cosmosAuthorizationTokenResolver = null;
        this.permissions = null;
        this.tokenCredential = null;
        return this;
    }

    /**
     * Builds a cosmos configuration object with the provided properties
     *
     * @return CosmosAsyncClient
     */
    public CosmosAsyncClient buildAsyncClient() {

        validateConfig();
        return new CosmosAsyncClient(this);
    }

    private void validateConfig() {
        ifThrowIllegalArgException(this.serviceEndpoint == null,
            "cannot buildAsyncClient client without service endpoint");
        ifThrowIllegalArgException(
            this.keyOrResourceToken == null && (permissions == null || permissions.isEmpty())
                && this.cosmosAuthorizationTokenResolver == null && this.cosmosKeyCredential == null
                && this.tokenCredential == null,
            "cannot buildAsyncClient client without any one of key, resource token, permissions, token resolver, and "
                + "cosmos key credential");
        ifThrowIllegalArgException(cosmosKeyCredential != null && StringUtils.isEmpty(cosmosKeyCredential.getKey()),
            "cannot buildAsyncClient client without key credential");
    }

    /**
     * Builds a cosmos sync client object with the provided properties
     *
     * @return CosmosClient
     */
    public CosmosClient buildClient() {

        validateConfig();
        return new CosmosClient(this);
    }

    Configs configs() {
        return configs;
    }

    /**
     * Configs
     *
     * @return current cosmosClientBuilder
     */
    CosmosClientBuilder configs(Configs configs) {
        this.configs = configs;
        return this;
    }

    private void ifThrowIllegalArgException(boolean value, String error) {
        if (value) {
            throw new IllegalArgumentException(error);
        }
    }
}
