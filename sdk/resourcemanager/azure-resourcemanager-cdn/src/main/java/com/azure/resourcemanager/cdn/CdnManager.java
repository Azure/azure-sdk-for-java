// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.cdn.fluent.CdnManagementClient;
import com.azure.resourcemanager.cdn.implementation.CdnManagementClientBuilder;
import com.azure.resourcemanager.cdn.implementation.CdnProfilesImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.cdn.models.CdnProfiles;

/**
 * Entry point to Azure CDN management.
 */
public final class CdnManager extends Manager<CdnManagementClient> {
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
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the CDN Manager
     */
    public static CdnManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of CDN Manager that exposes CDN manager management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the CDN Manager
     */
    private static CdnManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new CdnManager(httpPipeline, profile);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of CDN Manager that exposes CDN manager management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing CDN manager management API entry points that work across subscriptions
         */
        CdnManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl
            extends AzureConfigurableImpl<Configurable>
            implements Configurable {

        public CdnManager authenticate(TokenCredential credential, AzureProfile profile) {
            return CdnManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private CdnManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new CdnManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
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
