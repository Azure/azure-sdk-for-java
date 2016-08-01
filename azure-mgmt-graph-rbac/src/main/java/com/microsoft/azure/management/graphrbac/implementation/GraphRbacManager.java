/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.RequestIdHeaderInterceptor;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.management.graphrbac.ServicePrincipals;
import com.microsoft.azure.management.graphrbac.Users;
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
    private Users users;
    private ServicePrincipals servicePrincipals;

    /**
     * Creates an instance of GraphRbacManager that exposes resource management API entry points.
     *
     * @param credentials the credentials to use
     * @return the GraphRbacManager instance
     */
    public static GraphRbacManager authenticate(ServiceClientCredentials credentials, String tenantId) {
        return new GraphRbacManager(new RestClient.Builder()
                .withBaseUrl("https://graph.windows.net")
                .withInterceptor(new RequestIdHeaderInterceptor())
                .withCredentials(credentials)
                .build(), tenantId);
    }

    /**
     * Creates an instance of GraphRbacManager that exposes resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls
     * @return the interface exposing resource management API entry points that work across subscriptions
     */
    public static GraphRbacManager authenticate(RestClient restClient, String tenantId) {
        return new GraphRbacManager(restClient, tenantId);
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
        GraphRbacManager authenticate(ServiceClientCredentials credentials, String tenantId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        protected ConfigurableImpl() {
            super.restClientBuilder = new RestClient.Builder()
                    .withBaseUrl("https://graph.windows.net")
                    .withInterceptor(new RequestIdHeaderInterceptor());
        }

        public GraphRbacManager authenticate(ServiceClientCredentials credentials, String tenantId) {
            return GraphRbacManager.authenticate(buildRestClient(credentials), tenantId);
        }
    }

    private GraphRbacManager(RestClient restClient, String tenantId) {
        this.graphRbacManagementClient = new GraphRbacManagementClientImpl(restClient).withTenantID(tenantId);
    }

    /**
     * @return the storage account management API entry point
     */
    public Users storageAccounts() {
        if (users == null) {
            users = new UsersImpl(graphRbacManagementClient.users(), this);
        }
        return users;
    }

    /**
     * @return the storage account management API entry point
     */
    public ServicePrincipals servicePrincipals() {
        if (servicePrincipals == null) {
            servicePrincipals = new ServicePrincipalsImpl(graphRbacManagementClient.servicePrincipals(), this);
        }
        return servicePrincipals;
    }
}
