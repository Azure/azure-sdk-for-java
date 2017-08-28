/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.search.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.management.search.SearchServices;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure Search service management.
 */
@Beta(Beta.SinceVersion.V1_2_0)
public final class SearchServiceManager extends Manager<SearchServiceManager, SearchManagementClientImpl> {

    // Collections
    private SearchServicesImpl searchServices;

    /**
     * Get a Configurable instance that can be used to create SearchServiceManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new SearchServiceManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of ContainerRegistryManager that exposes Registry resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription
     * @return the SearchServiceManager
     */
    public static SearchServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new SearchServiceManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .withInterceptor(new ResourceManagerThrottlingInterceptor())
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of SearchServiceManager that exposes Registry resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription
     * @return the SearchServiceManager
     */
    public static SearchServiceManager authenticate(RestClient restClient, String subscriptionId) {
        return new SearchServiceManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of SearchServiceManager that exposes Search service resource management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription
         * @return the SearchServiceManager
         */
        SearchServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public SearchServiceManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return SearchServiceManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private SearchServiceManager(RestClient restClient, String subscriptionId) {
        super(restClient,
              subscriptionId,
              new SearchManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }


    /**
     * @return the availability set resource management API entry point
     */
    public SearchServices searchServices() {
        if (searchServices == null) {
            searchServices = new SearchServicesImpl(this);
        }
        return searchServices;
    }
}
