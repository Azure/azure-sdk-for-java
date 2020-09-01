/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.management.servicebus.ServiceBusNamespaces;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure ServiceBus management.
 */
@Beta
public final class ServiceBusManager extends Manager<ServiceBusManager, ServiceBusManagementClientImpl> {
    // Collections
    private ServiceBusNamespacesImpl namespaces;
    /**
     * Get a Configurable instance that can be used to create {@link ServiceBusManager}
     * with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new ServiceBusManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ServiceBusManager that exposes servicebus management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the ServiceBusManager
     */
    public static ServiceBusManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new ServiceBusManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .withInterceptor(new ResourceManagerThrottlingInterceptor())
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of ServiceBusManager that exposes servicebus management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the ServiceBusManager
     */
    public static ServiceBusManager authenticate(RestClient restClient, String subscriptionId) {
        return new ServiceBusManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of TrafficManager that exposes servicebus management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing traffic manager management API entry points that work across subscriptions
         */
        ServiceBusManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl
            extends AzureConfigurableImpl<Configurable>
            implements Configurable {

        public ServiceBusManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return ServiceBusManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private ServiceBusManager(RestClient restClient, String subscriptionId) {
        super(restClient,
                subscriptionId,
                new ServiceBusManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

    /**
     * @return the Service Bus namespace management API entry point
     */
    public ServiceBusNamespaces namespaces() {
        if (namespaces == null) {
            namespaces = new ServiceBusNamespacesImpl(this.inner().namespaces(), this);
        }
        return namespaces;
    }
}