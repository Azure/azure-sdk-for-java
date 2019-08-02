// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.Permission;

import java.util.List;

/**
 * Helper class to build {@link CosmosClient} instances
 * as logical representation of the Azure Cosmos database service.
 *
 * <pre>
 * {@code
 * ConnectionPolicy connectionPolicy = new ConnectionPolicy();
 * connectionPolicy.connectionMode(ConnectionMode.DIRECT);
 * CosmonsClient client = new CosmosClient.builder()
 *         .endpoint(serviceEndpoint)
 *         .key(key)
 *         .connectionPolicy(connectionPolicy)
 *         .consistencyLevel(ConsistencyLevel.SESSION)
 *         .build();
 * }
 * </pre>
 */
public class CosmosClientBuilder {

    private Configs configs = new Configs();
    private String serviceEndpoint;
    private String keyOrResourceToken;
    private ConnectionPolicy connectionPolicy;
    private ConsistencyLevel desiredConsistencyLevel;
    private List<Permission> permissions;
    private TokenResolver tokenResolver;

    CosmosClientBuilder() {
    }

    /**
     * Gets the token resolver
     * @return the token resolver
     */
    public TokenResolver tokenResolver() {
        return tokenResolver;
    }

    /**
     * Sets the token resolver
     * @param tokenResolver
     * @return current builder
     */
    public CosmosClientBuilder tokenResolver(TokenResolver tokenResolver) {
        this.tokenResolver = tokenResolver;
        return this;
    }

    /**
     * Gets the Azure Cosmos DB endpoint the SDK will connect to
     * @return the endpoint
     */
    public String endpoint() {
        return serviceEndpoint;
    }

    /**
     * Sets the Azure Cosmos DB endpoint the SDK will connect to
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
     * @return the key
     */
    public String key() {
        return keyOrResourceToken;
    }

    /**
     * Sets either a master or readonly key used to perform authentication
     * for accessing resource.
     *
     * @param key master or readonly key
     * @return current Builder.
     */
    public CosmosClientBuilder key(String key) {
        this.keyOrResourceToken = key;
        return this;
    }

    /**
     * Sets a resource token used to perform authentication
     * for accessing resource.
     * @return the resourceToken
     */
    public String resourceToken() {
        return keyOrResourceToken;
    }

    /**
     * Sets a resource token used to perform authentication
     * for accessing resource.
     *
     * @param resourceToken resourceToken for authentication
     * @return current Builder.
     */
    public CosmosClientBuilder resourceToken(String resourceToken) {
        this.keyOrResourceToken = resourceToken;
        return this;
    }

    /**
     * Gets the permission list, which contains the
     * resource tokens needed to access resources.
     * @return the permission list
     */
    public List<Permission> permissions() {
        return permissions;
    }

    /**
     * Sets the permission list, which contains the
     * resource tokens needed to access resources.
     *
     * @param permissions Permission list for authentication.
     * @return current Builder.
     */
    public CosmosClientBuilder permissions(List<Permission> permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Gets the (@link ConsistencyLevel) to be used
     * @return the consistency level
     */
    public ConsistencyLevel consistencyLevel() {
        return this.desiredConsistencyLevel;
    }

    /**
     * Sets the (@link ConsistencyLevel) to be used
     * @param desiredConsistencyLevel {@link ConsistencyLevel}
     * @return current Builder
     */
    public CosmosClientBuilder consistencyLevel(ConsistencyLevel desiredConsistencyLevel) {
        this.desiredConsistencyLevel = desiredConsistencyLevel;
        return this;
    }

    /**
     * Gets the (@link ConnectionPolicy) to be used
     * @return the connection policy
     */
    public ConnectionPolicy connectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Sets the (@link ConnectionPolicy) to be used
     * @param connectionPolicy {@link ConnectionPolicy}
     * @return current Builder
     */
    public CosmosClientBuilder connectionPolicy(ConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
        return this;
    }

    /**
     * Builds a cosmos configuration object with the provided properties
     * @return CosmosClient
     */
    public CosmosClient build() {

        ifThrowIllegalArgException(this.serviceEndpoint == null, "cannot build client without service endpoint");
        ifThrowIllegalArgException(
            this.keyOrResourceToken == null && (permissions == null || permissions.isEmpty()) && this.tokenResolver == null,
            "cannot build client without any one of key, resource token, permissions, and token resolver");

        return new CosmosClient(this);
    }

    Configs configs() {
        return configs;
    }

    /**
     * Configs
     * @param configs
     * @return current builder
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
