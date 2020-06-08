// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerservice;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.containerservice.implementation.KubernetesClustersImpl;
import com.azure.resourcemanager.containerservice.models.KubernetesClusters;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.Manager;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;

/** Entry point to Azure Container Service management. */
public final class ContainerServiceManager
    extends Manager<ContainerServiceManager, ContainerServiceManagementClient> {
    // The service managers
    private KubernetesClustersImpl kubernetesClusters;

    /**
     * Get a Configurable instance that can be used to create ContainerServiceManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new ContainerServiceManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ContainerServiceManager that exposes Azure Container Service resource management API entry
     * points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the ContainerServiceManager
     */
    public static ContainerServiceManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of ContainerServiceManager that exposes Service resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the ContainerServiceManager
     */
    public static ContainerServiceManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return authenticate(httpPipeline, profile, new SdkContext());
    }

    /**
     * Creates an instance of ContainerServiceManager that exposes Service resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the ContainerServiceManager
     */
    public static ContainerServiceManager authenticate(
        HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new ContainerServiceManager(httpPipeline, profile, sdkContext);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ContainerServiceManager that exposes Service resource management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the ContainerServiceManager
         */
        ContainerServiceManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public ContainerServiceManager authenticate(TokenCredential credential, AzureProfile profile) {
            return ContainerServiceManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private ContainerServiceManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(
            httpPipeline,
            profile,
            new ContainerServiceManagementClientBuilder()
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .pipeline(httpPipeline)
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
            sdkContext);
    }

    /** @return the Azure Kubernetes cluster resource management API entry point */
    public KubernetesClusters kubernetesClusters() {
        if (this.kubernetesClusters == null) {
            this.kubernetesClusters = new KubernetesClustersImpl(this);
        }
        return this.kubernetesClusters;
    }
}
