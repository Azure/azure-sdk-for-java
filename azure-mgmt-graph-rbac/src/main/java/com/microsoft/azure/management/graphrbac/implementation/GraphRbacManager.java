/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.interceptors.RequestIdHeaderInterceptor;
import com.microsoft.rest.RestClient;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.graphrbac.ServicePrincipals;
import com.microsoft.azure.management.graphrbac.Users;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;

/**
 * Entry point to Azure Graph RBAC management.
 */
@Beta
public final class GraphRbacManager {
    private String tenantId;
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
    public static GraphRbacManager authenticate(AzureTokenCredentials credentials) {
        return new GraphRbacManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment().graphEndpoint())
                .withInterceptor(new RequestIdHeaderInterceptor())
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .build(), credentials.domain());
    }

    /**
     * Creates an instance of GraphRbacManager that exposes resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls
     * @param tenantId the tenantId in Active Directory
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
        GraphRbacManager authenticate(AzureTokenCredentials credentials);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public GraphRbacManager authenticate(AzureTokenCredentials credentials) {
            return GraphRbacManager.authenticate(
                    buildRestClient(credentials, AzureEnvironment.Endpoint.GRAPH),
                    credentials.domain());
        }
    }

    private GraphRbacManager(RestClient restClient, String tenantId) {
        this.graphRbacManagementClient = new GraphRbacManagementClientImpl(restClient).withTenantID(tenantId);
        this.tenantId = tenantId;
    }

    /**
     * @return the tenant ID the graph client is associated with
     */
    public String tenantId() {
        return tenantId;
    }

    /**
     * @return the storage account management API entry point
     */
    public Users users() {
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
