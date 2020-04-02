/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerregistry.implementation;

import com.azure.core.implementation.annotation.Beta;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.containerregistry.Registries;
import com.azure.management.containerregistry.RegistryTaskRuns;
import com.azure.management.containerregistry.RegistryTasks;
import com.azure.management.containerregistry.models.ContainerRegistryManagementClientBuilder;
import com.azure.management.containerregistry.models.ContainerRegistryManagementClientImpl;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.implementation.StorageManager;

/**
 * Entry point to Azure container registry management.
 */
public final class ContainerRegistryManager extends Manager<ContainerRegistryManager, ContainerRegistryManagementClientImpl> {
    // The service managers
    private RegistriesImpl registries;
    private StorageManager storageManager;
    private RegistryTasksImpl tasks;
    private RegistryTaskRunsImpl registryTaskRuns;

    /**
     * Get a Configurable instance that can be used to create ContainerRegistryManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new ContainerRegistryManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ContainerRegistryManager that exposes Registry resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the ContainerRegistryManager
     */
    public static ContainerRegistryManager authenticate(AzureTokenCredential credentials, String subscriptionId) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credentials.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
//                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withPolicy(new ProviderRegistrationPolicy(credentials))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), subscriptionId);
    }

    /**
     * Creates an instance of ContainerRegistryManager that exposes Registry resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the ContainerRegistryManager
     */
    public static ContainerRegistryManager authenticate(RestClient restClient, String subscriptionId) {
        return authenticate(restClient, subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of ContainerRegistryManager that exposes Registry resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @param sdkContext the sdk context
     * @return the ContainerRegistryManager
     */
    public static ContainerRegistryManager authenticate(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        return new ContainerRegistryManager(restClient, subscriptionId, sdkContext);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ContainerRegistryManager that exposes Registry resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the ContainerRegistryManager
         */
        ContainerRegistryManager authenticate(AzureTokenCredential credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public ContainerRegistryManager authenticate(AzureTokenCredential credentials, String subscriptionId) {
            return ContainerRegistryManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    /**
     * Creates a ContainerRegistryManager.
     *
     * @param restClient the RestClient used to authenticate through StorageManager.
     * @param subscriptionId the subscription id used in authentication through StorageManager.
     * @param sdkContext the sdk context
     */
    private ContainerRegistryManager(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        super(
                restClient,
                subscriptionId,
                new ContainerRegistryManagementClientBuilder()
                    .pipeline(restClient.getHttpPipeline())
                    .host(restClient.getBaseUrl().toString())
                    .subscriptionId(subscriptionId)
                    .build(),
                sdkContext);
        this.storageManager = StorageManager.authenticate(restClient, subscriptionId, sdkContext);
    }


    /**
     * @return the availability set resource management API entry point
     */
    public Registries containerRegistries() {
        if (this.registries == null) {
            this.registries = new RegistriesImpl(this, this.storageManager);
        }
        return this.registries;
    }

    /**
     * Gets the current instance of ContainerRegistryManager's tasks.
     *
     * @return the tasks of the current instance of ContainerRegistryManager.
     */
    @Beta
    public RegistryTasks containerRegistryTasks() {
        if (this.tasks == null) {
            this.tasks = new RegistryTasksImpl(this);
        }
        return this.tasks;
    }

    /**
     * Gets the current instance of ContainerRegistryManager's registry task runs.
     *
     * @return the registry task runs of the current instance of ContainerRegistryManager.
     */
    @Beta
    public RegistryTaskRuns registryTaskRuns() {
        if (this.registryTaskRuns == null) {
            this.registryTaskRuns = new RegistryTaskRunsImpl(this);
        }
        return this.registryTaskRuns;
    }
}