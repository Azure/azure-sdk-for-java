/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.cdn.CdnProfiles;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure CDN management.
 */
public final class CdnManager extends Manager<CdnManager, CdnManagementClientImpl> {
    // Collections
    private CdnProfiles profiles;

    /**
     * Get a Configurable instance that can be used to create {@link CdnManager}
     * with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new CdnManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of CDN Manager that exposes CDN manager management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the CDN Manager
     */
    public static CdnManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new CdnManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .withInterceptor(new ResourceManagerThrottlingInterceptor())
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of CDN Manager that exposes CDN manager management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the CDN Manager
     */
    public static CdnManager authenticate(RestClient restClient, String subscriptionId) {
        return new CdnManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of CDN Manager that exposes CDN manager management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing CDN manager management API entry points that work across subscriptions
         */
        CdnManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl
            extends AzureConfigurableImpl<Configurable>
            implements Configurable {

        public CdnManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return CdnManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private CdnManager(RestClient restClient, String subscriptionId) {
        super(restClient,
                subscriptionId,
                new CdnManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

    /**
     * @return entry point to CDN manager profile management
     */
    public CdnProfiles profiles() {
        if (this.profiles == null) {
            this.profiles = new CdnProfilesImpl(this);
        }
        return this.profiles;
    }
}