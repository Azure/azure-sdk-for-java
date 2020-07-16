// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.implementation.annotation.Beta;
import com.azure.resourcemanager.containerregistry.implementation.RegistriesImpl;
import com.azure.resourcemanager.containerregistry.implementation.RegistryTaskRunsImpl;
import com.azure.resourcemanager.containerregistry.implementation.RegistryTasksImpl;
import com.azure.resourcemanager.containerregistry.models.Registries;
import com.azure.resourcemanager.containerregistry.models.RegistryTaskRuns;
import com.azure.resourcemanager.containerregistry.models.RegistryTasks;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.Manager;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.storage.StorageManager;

/** Entry point to Azure container registry management. */
public final class ContainerRegistryManager
    extends Manager<ContainerRegistryManager, ContainerRegistryManagementClient> {
    // The service managers
    private RegistriesImpl registries;
    private final StorageManager storageManager;
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
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the ContainerRegistryManager
     */
    public static ContainerRegistryManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of ContainerRegistryManager that exposes Registry resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the ContainerRegistryManager
     */
    public static ContainerRegistryManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return authenticate(httpPipeline, profile, new SdkContext());
    }

    /**
     * Creates an instance of ContainerRegistryManager that exposes Registry resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the ContainerRegistryManager
     */
    public static ContainerRegistryManager authenticate(
        HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new ContainerRegistryManager(httpPipeline, profile, sdkContext);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ContainerRegistryManager that exposes Registry resource management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the ContainerRegistryManager
         */
        ContainerRegistryManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public ContainerRegistryManager authenticate(TokenCredential credential, AzureProfile profile) {
            return ContainerRegistryManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    /**
     * Creates a ContainerRegistryManager.
     *
     * @param httpPipeline the HttpPipeline used to authenticate through ContainerRegistryManager.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     */
    private ContainerRegistryManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new ContainerRegistryManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
            sdkContext);
        this.storageManager = StorageManager.authenticate(httpPipeline, profile, sdkContext);
    }

    /** @return the availability set resource management API entry point */
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
