/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.dns.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.dns.models.DnsManagementClientBuilder;
import com.azure.management.dns.models.DnsManagementClientImpl;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.dns.DnsZones;

/**
 * Entry point to Azure DNS zone management.
 */
public final class DnsZoneManager extends Manager<DnsZoneManager, DnsManagementClientImpl> {
    // Collections
    private DnsZones zones;

    /**
     * Get a Configurable instance that can be used to create {@link DnsZoneManager}
     * with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new DnsZoneManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of DnsZoneManager that exposes DNS zone management API entry points.
     *
     * @param credential the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the DnsZoneManager
     */
    public static DnsZoneManager authenticate(AzureTokenCredential credential, String subscriptionId) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .buildClient(), subscriptionId);
    }

    /**
     * Creates an instance of DnsZoneManager that exposes DNS zone management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the DnsZoneManager
     */
    public static DnsZoneManager authenticate(RestClient restClient, String subscriptionId) {
        return new DnsZoneManager(restClient, subscriptionId, new SdkContext());
    }

    /**
     * Creates an instance of DnsZoneManager that exposes DNS zone management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @param sdkContext the sdk context
     * @return the DnsZoneManager
     */
    public static DnsZoneManager authenticate(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        return new DnsZoneManager(restClient, subscriptionId, sdkContext);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of DnsZoneManager that exposes DNS zone API entry points.
         *
         * @param credential the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing DNS zone management API entry points that work across subscriptions
         */
        DnsZoneManager authenticate(AzureTokenCredential credential, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl
            extends AzureConfigurableImpl<Configurable>
            implements Configurable {

        public DnsZoneManager authenticate(AzureTokenCredential credential, String subscriptionId) {
            return DnsZoneManager.authenticate(buildRestClient(credential), subscriptionId);
        }
    }

    private DnsZoneManager(RestClient restClient, String subscriptionId, SdkContext sdkContext) {
        super(restClient,
                subscriptionId,
                new DnsManagementClientBuilder()
                        .pipeline(restClient.getHttpPipeline())
                        .subscriptionId(subscriptionId)
                        .build(),
                sdkContext
        );
    }

    /**
     * @return entry point to DNS zone manager zone management
     */
    public DnsZones zones() {
        if (this.zones == null) {
            this.zones = new DnsZonesImpl(this);
        }
        return this.zones;
    }
}
