/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerservice.implementation;


import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.containerservice.ContainerServices;
import com.azure.management.containerservice.KubernetesClusters;
import com.azure.management.containerservice.models.ContainerServiceManagementClientBuilder;
import com.azure.management.containerservice.models.ContainerServiceManagementClientImpl;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;

/**
 * Entry point to Azure Container Service management.
 */
public final class ContainerServiceManager extends Manager<ContainerServiceManager, ContainerServiceManagementClientImpl> {
    // The service managers
    private ContainerServicesImpl containerServices;
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
     * Creates an instance of ContainerServiceManager that exposes Azure Container Service resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the ContainerServiceManager
     */
    public static ContainerServiceManager authenticate(AzureTokenCredential credentials, String subscriptionId) {
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
     * Creates an instance of ContainerServiceManager that exposes Service resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the ContainerServiceManager
     */
    public static ContainerServiceManager authenticate(RestClient restClient, String subscriptionId) {
        return authenticate(restClient, subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of ContainerServiceManager that exposes Service resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @param sdkContext the sdk context
     * @return the ContainerServiceManager
     */
    public static ContainerServiceManager authenticate(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        return new ContainerServiceManager(restClient, subscriptionId, sdkContext);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ContainerServiceManager that exposes Service resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the ContainerServiceManager
         */
        ContainerServiceManager authenticate(AzureTokenCredential credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements  Configurable {
        @Override
        public ContainerServiceManager authenticate(AzureTokenCredential credentials, String subscriptionId) {
            return ContainerServiceManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ContainerServiceManager(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        super(
                restClient,
                subscriptionId,
                new ContainerServiceManagementClientBuilder()
                    .host(restClient.getBaseUrl().toString())
                    .pipeline(restClient.getHttpPipeline())
                    .subscriptionId(subscriptionId)
                    .build(),
                sdkContext
        );
    }

    /**
     * @return the Azure Container services resource management API entry point
     */
    public ContainerServices containerServices() {
        if (this.containerServices == null) {
            this.containerServices = new ContainerServicesImpl(this);
        }
        return this.containerServices;
    }

    /**
     * @return the Azure Kubernetes cluster resource management API entry point
     */
    public KubernetesClusters kubernetesClusters() {
        if (this.kubernetesClusters == null) {
            this.kubernetesClusters = new KubernetesClustersImpl(this);
        }
        return this.kubernetesClusters;
    }
}