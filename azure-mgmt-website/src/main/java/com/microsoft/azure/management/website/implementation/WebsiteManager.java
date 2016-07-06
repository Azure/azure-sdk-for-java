/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.website.Sites;
import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Entry point to Azure storage resource management.
 */
public final class WebsiteManager extends Manager<WebsiteManager, WebSiteManagementClientImpl> {
    // Collections
    private Sites sites;

    /**
     * Get a Configurable instance that can be used to create StorageManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new WebsiteManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static WebsiteManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
        return new WebsiteManager(AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of StorageManager that exposes storage resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the StorageManager
     */
    public static WebsiteManager authenticate(RestClient restClient, String subscriptionId) {
        return new WebsiteManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of StorageManager that exposes storage management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing storage management API entry points that work across subscriptions
         */
        WebsiteManager authenticate(ServiceClientCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public WebsiteManager authenticate(ServiceClientCredentials credentials, String subscriptionId) {
            return WebsiteManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private WebsiteManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new WebSiteManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

    /**
     * @return the web app management API entry point
     */
    public Sites sites() {
        if (sites == null) {
            sites = new SitesImpl(innerManagementClient.sites(), this);
        }
        return sites;
    }
}
