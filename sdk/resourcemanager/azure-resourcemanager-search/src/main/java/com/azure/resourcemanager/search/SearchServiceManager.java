// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.search.fluent.SearchManagementClient;
import com.azure.resourcemanager.search.implementation.SearchManagementClientBuilder;
import com.azure.resourcemanager.search.implementation.SearchServicesImpl;
import com.azure.resourcemanager.search.models.SearchServices;

/**
 * Entry point to Azure Cognitive Search service management.
 */
public final class SearchServiceManager extends Manager<SearchManagementClient> {
    // Collections
    private SearchServices searchServices;

    /**
     * Get a Configurable instance that can be used to create SearchServiceManager with optional configuration.
     *
     * @return Configurable
     */
    public static Configurable configure() {
        return new SearchServiceManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of SearchServiceManager that exposes Cognitive Search resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the SearchServiceManager
     */
    public static SearchServiceManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of SearchServiceManager that exposes Cognitive Search resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the SearchServiceManager
     */
    private static SearchServiceManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new SearchServiceManager(httpPipeline, profile);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of SearchServiceManager that exposes Cognitive Search service
         * resource management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the SearchServiceManager
         */
        SearchServiceManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        @Override
        public SearchServiceManager authenticate(TokenCredential credential, AzureProfile profile) {
            return SearchServiceManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private SearchServiceManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new SearchManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient()
        );
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
