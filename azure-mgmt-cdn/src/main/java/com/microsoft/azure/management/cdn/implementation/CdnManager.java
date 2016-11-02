package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.cdn.CdnProfiles;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;

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
     * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the TrafficManager
     */
    public static CdnManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new CdnManager(credentials.getEnvironment().newRestClientBuilder()
                .withCredentials(credentials)
                .build(), subscriptionId);
    }

    /**
     * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the TrafficManager
     */
    public static CdnManager authenticate(RestClient restClient, String subscriptionId) {
        return new CdnManager(restClient, subscriptionId);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of TrafficManager that exposes traffic manager management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing traffic manager management API entry points that work across subscriptions
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
     * @return entry point to traffic manager profile management
     */
    public CdnProfiles profiles() {
        if (this.profiles == null) {
            this.profiles = new CdnProfilesImpl(
                    super.innerManagementClient,
                    this);
        }
        return this.profiles;
    }
}