// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.servicebus.fluent.ServiceBusManagementClient;
import com.azure.resourcemanager.servicebus.implementation.ServiceBusManagementClientBuilder;
import com.azure.resourcemanager.servicebus.implementation.ServiceBusNamespacesImpl;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespaces;

/**
 * Entry point to Azure ServiceBus management.
 */
public final class ServiceBusManager extends Manager<ServiceBusManagementClient> {
    // Collections
    private ServiceBusNamespaces namespaces;
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
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the ServiceBusManager
     */
    public static ServiceBusManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of ServiceBusManager that exposes servicebus management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the ServiceBusManager
     */
    private static ServiceBusManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new ServiceBusManager(httpPipeline, profile);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of TrafficManager that exposes servicebus management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing ServiceBus manager management API entry points that work across subscriptions
         */
        ServiceBusManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl
            extends AzureConfigurableImpl<Configurable>
            implements Configurable {

        public ServiceBusManager authenticate(TokenCredential credential, AzureProfile profile) {
            return ServiceBusManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private ServiceBusManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new ServiceBusManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
    }

    /**
     * @return the Service Bus namespace management API entry point
     */
    public ServiceBusNamespaces namespaces() {
        if (namespaces == null) {
            namespaces = new ServiceBusNamespacesImpl(this.serviceClient().getNamespaces(), this);
        }
        return namespaces;
    }
}
