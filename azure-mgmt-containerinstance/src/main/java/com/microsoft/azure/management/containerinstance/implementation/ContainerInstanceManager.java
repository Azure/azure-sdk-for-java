/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.containerinstance.ContainerGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure container instance management.
 */
@Beta(SinceVersion.V1_3_0)
public final class ContainerInstanceManager extends Manager<ContainerInstanceManager, ContainerInstanceManagementClientImpl> {

    // The service managers
    private ContainerGroupsImpl containerGroups;
    private StorageManager storageManager;

    /**
     * Get a Configurable instance that can be used to create ContainerInstanceManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new ContainerInstanceManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ContainerInstanceManager that exposes resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the ContainerInstanceManager
     */
    public static ContainerInstanceManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new ContainerInstanceManager(new RestClient.Builder()
            .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
            .withCredentials(credentials)
            .withSerializerAdapter(new AzureJacksonAdapter())
            .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
            .withInterceptor(new ProviderRegistrationInterceptor(credentials))
            .withInterceptor(new ResourceManagerThrottlingInterceptor())
            .build(), subscriptionId);
    }

    /**
     * Creates an instance of ContainerInstanceManager that exposes resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the ContainerInstanceManager
     */
    public static ContainerInstanceManager authenticate(RestClient restClient, String subscriptionId) {
        return new ContainerInstanceManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ContainerInstanceManager that exposes resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the ContainerInstanceManager
         */
        ContainerInstanceManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public ContainerInstanceManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return ContainerInstanceManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ContainerInstanceManager(RestClient restClient, String subscriptionId) {
        super(
            restClient,
            subscriptionId,
            new ContainerInstanceManagementClientImpl(restClient).withSubscriptionId(subscriptionId));

        this.storageManager = StorageManager.authenticate(restClient, subscriptionId);
    }

    /**
     * @return the resource management API entry point
     */
    public ContainerGroups containerGroups() {
        if (containerGroups == null) {
            containerGroups = new ContainerGroupsImpl(this, this.storageManager);
        }

        return containerGroups;
    }
}
