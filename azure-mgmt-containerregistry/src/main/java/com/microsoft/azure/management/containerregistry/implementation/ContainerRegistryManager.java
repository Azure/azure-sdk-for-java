/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.containerregistry.Registries;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure compute resource management.
 */
public final class ContainerRegistryManager extends Manager<ContainerRegistryManager, ContainerRegistryManagementClientImpl> {
    // The service managers
    private RegistriesImpl registries;

    /**
     * Get a Configurable instance that can be used to create ComputeManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new ContainerRegistryManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the ComputeManager
     */
    public static ContainerRegistryManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new ContainerRegistryManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the ComputeManager
     */
    public static ContainerRegistryManager authenticate(RestClient restClient, String subscriptionId) {
        return new ContainerRegistryManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ComputeManager that exposes Compute resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the ComputeManager
         */
        ContainerRegistryManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public ContainerRegistryManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return ContainerRegistryManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ContainerRegistryManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new ContainerRegistryManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }


    /**
     * @return the availability set resource management API entry point
     */
    public Registries containerRegistries() {
        if (registries == null) {
            registries = new RegistriesImpl(this);
        }
        return registries;
    }
}