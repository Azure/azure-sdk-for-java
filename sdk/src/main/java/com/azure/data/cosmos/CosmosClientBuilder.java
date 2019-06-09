/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Configs;

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
     * Configs
     * @param configs
     * @return current builder
     */
    public CosmosClientBuilder configs(Configs configs) {
        this.configs = configs;
        return this;
    }

    /**
     * Token Resolver
     * @param tokenResolver
     * @return current builder
     */
    public CosmosClientBuilder tokenResolver(TokenResolver tokenResolver) {
        this.tokenResolver = tokenResolver;
        return this;
    }

    /**
     * The service endpoint url
     * @param serviceEndpoint the service endpoint
     * @return current Builder
     */
    public CosmosClientBuilder endpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
        return this;
    }

    /**
     * This method will take either key or resource token and perform authentication
     * for accessing resource.
     *
     * @param keyOrResourceToken key or resourceToken for authentication .
     * @return current Builder.
     */
    public CosmosClientBuilder key(String keyOrResourceToken) {
        this.keyOrResourceToken = keyOrResourceToken;
        return this;
    }

    /**
     * This method will accept the permission list , which contains the
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
     * This method accepts the (@link ConsistencyLevel) to be used
     * @param desiredConsistencyLevel {@link ConsistencyLevel}
     * @return current Builder
     */
    public CosmosClientBuilder consistencyLevel(ConsistencyLevel desiredConsistencyLevel) {
        this.desiredConsistencyLevel = desiredConsistencyLevel;
        return this;
    }

    /**
     * The (@link ConnectionPolicy) to be used
     * @param connectionPolicy {@link ConnectionPolicy}
     * @return current Builder
     */
    public CosmosClientBuilder connectionPolicy(ConnectionPolicy connectionPolicy) {
        this.connectionPolicy = connectionPolicy;
        return this;
    }

    private void ifThrowIllegalArgException(boolean value, String error) {
        if (value) {
            throw new IllegalArgumentException(error);
        }
    }

    /**
     * Builds a cosmos configuration object with the provided settings
     * @return CosmosClient
     */
    public CosmosClient build() {

        ifThrowIllegalArgException(this.serviceEndpoint == null, "cannot build client without service endpoint");
        ifThrowIllegalArgException(
                this.keyOrResourceToken == null && (permissions == null || permissions.isEmpty()),
                "cannot build client without key or resource token");

        return new CosmosClient(this);
    }

    String getServiceEndpoint() {
        return serviceEndpoint;
    }

    String getKeyOrResourceToken() {
        return keyOrResourceToken;
    }

    public ConnectionPolicy getConnectionPolicy() {
        return connectionPolicy;
    }

    public ConsistencyLevel getDesiredConsistencyLevel() {
        return desiredConsistencyLevel;
    }

    List<Permission> getPermissions() {
        return permissions;
    }

    public Configs getConfigs() {
        return configs;
    }

    public TokenResolver getTokenResolver() {
        return tokenResolver;
    }
}
