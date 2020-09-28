// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.privatedns.fluent.PrivateDnsManagementClient;
import com.azure.resourcemanager.privatedns.implementation.PrivateDnsManagementClientBuilder;
import com.azure.resourcemanager.privatedns.implementation.PrivateDnsZonesImpl;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZones;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

/** Entry point to Azure private DNS zone management. */
public final class PrivateDnsZoneManager extends Manager<PrivateDnsManagementClient> {

    private PrivateDnsZones privateZones;

    /**
     * Get a Configurable instance that can be used to create {@link PrivateDnsZoneManager} with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new PrivateDnsZoneManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of PrivateDnsZoneManager that exposes private DNS zone management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the PrivateDnsZoneManager
     */
    public static PrivateDnsZoneManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of PrivateDnsZoneManager that exposes private DNS zone management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the PrivateDnsZoneManager
     */
    public static PrivateDnsZoneManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new PrivateDnsZoneManager(httpPipeline, profile);
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of PrivateDnsZoneManager that exposes private DNS zone API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing private DNS zone management API entry points that work across subscriptions
         */
        PrivateDnsZoneManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public PrivateDnsZoneManager authenticate(TokenCredential credential, AzureProfile profile) {
            return PrivateDnsZoneManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private PrivateDnsZoneManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new PrivateDnsManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
    }

    /**
     * @return the entry point to private DNS zone management.
     */
    public PrivateDnsZones privateZones() {
        if (privateZones == null) {
            privateZones = new PrivateDnsZonesImpl(this);
        }
        return privateZones;
    }
}
