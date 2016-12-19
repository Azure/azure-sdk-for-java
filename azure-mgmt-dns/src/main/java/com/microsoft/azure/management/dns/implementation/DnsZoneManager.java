package com.microsoft.azure.management.dns.implementation;

import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.dns.DnsZones;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;

/**
 * Entry point to Azure Dns zone manager management.
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
     * Creates an instance of DnsZoneManager that exposes Dns zone manager management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the DnsZoneManager
     */
    public static DnsZoneManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new DnsZoneManager(credentials.getEnvironment().newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of DnsZoneManager that exposes Dns zone manager management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the DnsZoneManager
     */
    public static DnsZoneManager authenticate(RestClient restClient, String subscriptionId) {
        return new DnsZoneManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of DnsZoneManager that exposes Dns zone API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing Dns zone manager management API entry points that work across subscriptions
         */
        DnsZoneManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl
            extends AzureConfigurableImpl<Configurable>
            implements Configurable {

        public DnsZoneManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return DnsZoneManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }

    private DnsZoneManager(RestClient restClient, String subscriptionId) {
        super(restClient,
                subscriptionId,
                new DnsManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
    }

    /**
     * @return entry point to Dns zone manager zone management
     */
    public DnsZones zones() {
        if (this.zones == null) {
            this.zones = new DnsZonesImpl(
                    super.innerManagementClient,
                    this);
        }
        return this.zones;
    }
}
