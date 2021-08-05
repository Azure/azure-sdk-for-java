// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.containerinstance.fluent.ContainerInstanceManagementClient;
import com.azure.resourcemanager.containerinstance.implementation.ContainerInstanceManagementClientBuilder;
import com.azure.resourcemanager.containerinstance.implementation.ContainerGroupsImpl;
import com.azure.resourcemanager.containerinstance.models.ContainerGroups;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.storage.StorageManager;

/** Entry point to Azure container instance management. */
public final class ContainerInstanceManager
    extends Manager<ContainerInstanceManagementClient> {

    // The service managers
    private ContainerGroupsImpl containerGroups;
    private final StorageManager storageManager;
    private final AuthorizationManager authorizationManager;
    private final NetworkManager networkManager;

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
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the ContainerInstanceManager
     */
    public static ContainerInstanceManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of ContainerInstanceManager that exposes resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the ContainerInstanceManager
     */
    private static ContainerInstanceManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new ContainerInstanceManager(httpPipeline, profile);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ContainerInstanceManager that exposes resource management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the ContainerInstanceManager
         */
        ContainerInstanceManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public ContainerInstanceManager authenticate(TokenCredential credential, AzureProfile profile) {
            return ContainerInstanceManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private ContainerInstanceManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new ContainerInstanceManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());

        this.storageManager = AzureConfigurableImpl.configureHttpPipeline(httpPipeline, StorageManager.configure())
            .authenticate(null, profile);
        this.authorizationManager = AzureConfigurableImpl
            .configureHttpPipeline(httpPipeline, AuthorizationManager.configure())
            .authenticate(null, profile);
        this.networkManager = AzureConfigurableImpl.configureHttpPipeline(httpPipeline, NetworkManager.configure())
            .authenticate(null, profile);
    }

    /** @return the storage manager in container instance manager */
    public StorageManager storageManager() {
        return storageManager;
    }

    /** @return the authorization manager in container instance manager */
    public AuthorizationManager authorizationManager() {
        return authorizationManager;
    }

    /** @return the network manager in container instance manager */
    public NetworkManager networkManager() {
        return networkManager;
    }

    /** @return the resource management API entry point */
    public ContainerGroups containerGroups() {
        if (this.containerGroups == null) {
            this.containerGroups = new ContainerGroupsImpl(this);
        }

        return this.containerGroups;
    }
}
