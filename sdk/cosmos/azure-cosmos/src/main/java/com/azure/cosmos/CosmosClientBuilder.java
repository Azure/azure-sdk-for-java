// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.cosmos.internal.Configs;
import com.azure.cosmos.internal.Permission;
import com.azure.cosmos.internal.Configs;
import com.azure.cosmos.internal.Permission;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Helper class to buildAsyncClient {@link CosmosAsyncClient} instances
 * as logical representation of the Azure Cosmos database service.
 *
 * <pre>
 * {@code
 * ConnectionPolicy getConnectionPolicy = new ConnectionPolicy();
 * getConnectionPolicy.getConnectionMode(ConnectionMode.DIRECT);
 * CosmonsClient client = new CosmosAsyncClient.builder()
 *         .getEndpoint(serviceEndpoint)
 *         .getKey(getKey)
 *         .getConnectionPolicy(getConnectionPolicy)
 *         .getConsistencyLevel(ConsistencyLevel.SESSION)
 *         .buildAsyncClient();
 * }
 * </pre>
 */
@ServiceClientBuilder(serviceClients = {CosmosClient.class, CosmosAsyncClient.class})
public class CosmosClientBuilder {

    private Configs configs = new Configs();
    private String serviceEndpoint;
    private String keyOrResourceToken;
    private ConnectionPolicy connectionPolicy;
    private ConsistencyLevel desiredConsistencyLevel;
    private List<Permission> permissions;
    private TokenResolver tokenResolver;
    private CosmosKeyCredential cosmosKeyCredential;

    public CosmosClientBuilder() {
    }

    /**
     * Gets the token resolver
     * @return the token resolver
     */
    public TokenResolver getTokenResolver() {
        return tokenResolver;
    }

    /**
     * Sets the token resolver
     * @param tokenResolver
     * @return current builder
     */
    public CosmosClientBuilder setTokenResolver(TokenResolver tokenResolver) {
        this.tokenResolver = tokenResolver;
        return this;
    }

    /**
     * Gets the Azure Cosmos DB endpoint the SDK will connect to
     * @return the endpoint
     */
    public String getEndpoint() {
        return serviceEndpoint;
    }

    /**
     * Sets the Azure Cosmos DB endpoint the SDK will connect to
     * @param endpoint the service endpoint
     * @return current Builder
     */
    public CosmosClientBuilder setEndpoint(String endpoint) {
        this.serviceEndpoint = endpoint;
        return this;
    }

    /**
     * Gets either a master or readonly key used to perform authentication
     * for accessing resource.
     * @return the key
     */
    public String getKey() {
        return keyOrResourceToken;
    }

    /**
     * Sets either a master or readonly key used to perform authentication
     * for accessing resource.
     *
     * @param key master or readonly key
     * @return current Builder.
     */
    public CosmosClientBuilder setKey(String key) {
        this.keyOrResourceToken = key;
        return this;
    }

    /**
     * Sets a resource token used to perform authentication
     * for accessing resource.
     * @return the resourceToken
     */
    public String getResourceToken() {
        return keyOrResourceToken;
    }

    /**
     * Sets a resource token used to perform authentication
     * for accessing resource.
     *
     * @param resourceToken resourceToken for authentication
     * @return current Builder.
     */
    public CosmosClientBuilder setResourceToken(String resourceToken) {
        this.keyOrResourceToken = resourceToken;
        return this;
    }

    /**
     * Gets the permission list, which contains the
     * resource tokens needed to access resources.
     * @return the permission list
     */
    public List<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Sets the permission list, which contains the
     * resource tokens needed to access resources.
     *
     * @param permissions Permission list for authentication.
     * @return current Builder.
     */
    public CosmosClientBuilder setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * Gets the {@link ConsistencyLevel} to be used
     * @return the consistency level
     */
    public ConsistencyLevel getConsistencyLevel() {
        return this.desiredConsistencyLevel;
    }

    /**
     * Sets the {@link ConsistencyLevel} to be used
     * @param desiredConsistencyLevel {@link ConsistencyLevel}
     * @return current Builder
     */
    public CosmosClientBuilder setConsistencyLevel(ConsistencyLevel desiredConsistencyLevel) {
        this.desiredConsistencyLevel = desiredConsistencyLevel;
        return this;
    }

    /**
     * Gets the (@link ConnectionPolicy) to be used
     * @return the connection policy
     */
    public ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    /**
     * Sets the {@link ConnectionPolicy} to be used
     * @param connectionPolicy {@link ConnectionPolicy}
     * @return current Builder
     */
    public CosmosClientBuilder setConnectionPolicy(ConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
        return this;
    }

    /**
     * Gets the {@link CosmosKeyCredential} to be used
     * @return cosmosKeyCredential
     */
    public CosmosKeyCredential getCosmosKeyCredential() {
        return cosmosKeyCredential;
    }

    /**
     * Sets the {@link CosmosKeyCredential} to be used
     * @param cosmosKeyCredential {@link CosmosKeyCredential}
     * @return current builder
     */
    public CosmosClientBuilder setCosmosKeyCredential(CosmosKeyCredential cosmosKeyCredential) {
        this.cosmosKeyCredential = cosmosKeyCredential;
        return this;
    }

    /**
     * Builds a cosmos configuration object with the provided properties
     * @return CosmosAsyncClient
     */
    public CosmosAsyncClient buildAsyncClient() {

        validateConfig();
        return new CosmosAsyncClient(this);
    }

    private void validateConfig() {
        ifThrowIllegalArgException(this.serviceEndpoint == null, "cannot buildAsyncClient client without service endpoint");
        ifThrowIllegalArgException(
            this.keyOrResourceToken == null && (permissions == null || permissions.isEmpty())
                && this.tokenResolver == null && this.cosmosKeyCredential == null,
            "cannot buildAsyncClient client without any one of key, resource token, permissions, token resolver, and cosmos key credential");
        ifThrowIllegalArgException(cosmosKeyCredential != null && StringUtils.isEmpty(cosmosKeyCredential.getKey()),
            "cannot buildAsyncClient client without key credential");
    }

    /**
     * Builds a cosmos sync client object with the provided properties
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
