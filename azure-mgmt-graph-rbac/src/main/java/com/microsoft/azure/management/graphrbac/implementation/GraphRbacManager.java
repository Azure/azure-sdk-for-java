/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Entry point to Azure resource management.
 */
public final class GraphRbacManager {
    // The sdk clients
    private final GraphRbacManagementClientImpl graphRbacManagementClient;
    // The collections

    /**
     * Creates an instance of GraphRbacManager that exposes resource management API entry points.
     *
     * @param credentials the credentials to use
     * @return the GraphRbacManager instance
     */
    public static GraphRbacManager authenticate(ServiceClientCredentials credentials) {
        return new GraphRbacManager(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build());
    }

    /**
     * Creates an instance of GraphRbacManager that exposes resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls
     * @return the interface exposing resource management API entry points that work across subscriptions
     */
    public static GraphRbacManager authenticate(RestClient restClient) {
        return new GraphRbacManager(restClient);
    }

    /**
     * Get a Configurable instance that can be used to create GraphRbacManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new GraphRbacManager.ConfigurableImpl();
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of GraphRbacManager that exposes resource management API entry points.
         *
         * @param credentials the credentials to use
         * @return the interface exposing resource management API entry points that work across subscriptions
         */
        GraphRbacManager authenticate(ServiceClientCredentials credentials);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public GraphRbacManager authenticate(ServiceClientCredentials credentials) {
            return GraphRbacManager.authenticate(buildRestClient(credentials));
        }
    }

    private GraphRbacManager(RestClient restClient) {
        this.graphRbacManagementClient = new GraphRbacManagementClientImpl(restClient);
    }
}
