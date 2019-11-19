/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerservice.package_2017_09;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.arm.resources.AzureConfigurable;
import com.microsoft.azure.arm.resources.implementation.AzureConfigurableCoreImpl;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.containerservice.v2017_08_31.ManagedClusters;
import com.microsoft.azure.management.containerservice.v2017_07_01.ContainerServices;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure ContainerService resource management.
 */
public final class ContainerServiceManager {
    private com.microsoft.azure.management.containerservice.v2017_07_01.implementation.ContainerServiceManager manager20170701;
    private com.microsoft.azure.management.containerservice.v2017_08_31.implementation.ContainerServiceManager manager20170831;
    private com.microsoft.azure.management.containerservice.v2017_09_30.implementation.ContainerServiceManager manager20170930;
    /**
     * Get a Configurable instance that can be used to create ContainerServiceManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new ContainerServiceManager.ConfigurableImpl();
    }
    /**
     * Creates an instance of ContainerServiceManager that exposes ContainerService resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the ContainerServiceManager
     */
    public static ContainerServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new ContainerServiceManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .build(), subscriptionId);
    }
    /**
     * Creates an instance of ContainerServiceManager that exposes ContainerService resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the ContainerServiceManager
     */
    public static ContainerServiceManager authenticate(RestClient restClient, String subscriptionId) {
        return new ContainerServiceManager(restClient, subscriptionId);
    }
    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of ContainerServiceManager that exposes ContainerService management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing ContainerService management API entry points that work across subscriptions
         */
        ContainerServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * @return Entry point to manage ContainerServices.
     */
    public ContainerServices containerServices() {
        return manager20170701.containerServices();
    }

    /**
     * @return Entry point to manage orchestrators.
     */
    public com.microsoft.azure.management.containerservice.v2017_09_30.ContainerServices orchestrators() {
        return manager20170930.containerServices();
    }

    /**
     * @return Entry point to managed clusters.
     */
    public ManagedClusters managedClusters() {
        return manager20170831.managedClusters();
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableCoreImpl<Configurable> implements Configurable {
        public ContainerServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return ContainerServiceManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ContainerServiceManager(RestClient restClient, String subscriptionId) {
        this.manager20170701 = com.microsoft.azure.management.containerservice.v2017_07_01.implementation.ContainerServiceManager.authenticate(restClient, subscriptionId);
        this.manager20170831 = com.microsoft.azure.management.containerservice.v2017_08_31.implementation.ContainerServiceManager.authenticate(restClient, subscriptionId);
        this.manager20170930 = com.microsoft.azure.management.containerservice.v2017_09_30.implementation.ContainerServiceManager.authenticate(restClient, subscriptionId);
    }
}
