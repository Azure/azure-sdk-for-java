/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.eventhub.DisasterRecoveryPairingAuthorizationRules;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationRules;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroups;
import com.microsoft.azure.management.eventhub.EventHubDisasterRecoveryPairings;
import com.microsoft.azure.management.eventhub.EventHubNamespaceAuthorizationRules;
import com.microsoft.azure.management.eventhub.EventHubNamespaces;
import com.microsoft.azure.management.eventhub.EventHubs;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;

/**
 * Entry point to Azure EventHub resource management.
 */
@Beta(SinceVersion.V1_2_0)
public final class EventHubManager extends Manager<EventHubManager, EventHubManagementClientImpl> {
    private EventHubNamespaces namespaces;
    private EventHubs eventHubs;
    private EventHubConsumerGroups consumerGroups;
    private EventHubAuthorizationRules eventHubAuthorizationRules;
    private EventHubNamespaceAuthorizationRules namespaceAuthorizationRules;
    private EventHubDisasterRecoveryPairings eventHubDisasterRecoveryPairings;
    private DisasterRecoveryPairingAuthorizationRules disasterRecoveryPairingAuthorizationRules;

    private StorageManager storageManager;
    /**
     * Get a Configurable instance that can be used to create EventHubManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new EventHubManager.ConfigurableImpl();
    }
    /**
     * Creates an instance of EventHubManager that exposes EventHub resource management API entry points.
     *
     * @param credentials the credentials to use
     * @param subscriptionId the subscription UUID
     * @return the EventHubManager
     */
    public static EventHubManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
        return new EventHubManager(new RestClient.Builder()
                .withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withSerializerAdapter(new AzureJacksonAdapter())
                .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                .withInterceptor(new ResourceManagerThrottlingInterceptor())
                .build(), subscriptionId);
    }
    /**
     * Creates an instance of EventHubManager that exposes EventHub resource management API entry points.
     *
     * @param restClient the RestClient to be used for API calls.
     * @param subscriptionId the subscription UUID
     * @return the EventHubManager
     */
    public static EventHubManager authenticate(RestClient restClient, String subscriptionId) {
        return new EventHubManager(restClient, subscriptionId);
    }
    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of EventHubManager that exposes EventHub management API entry points.
         *
         * @param credentials the credentials to use
         * @param subscriptionId the subscription UUID
         * @return the interface exposing EventHub management API entry points that work across subscriptions
         */
        EventHubManager authenticate(AzureTokenCredentials credentials, String subscriptionId);
    }
    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public EventHubManager authenticate(AzureTokenCredentials credentials, String subscriptionId) {
            return EventHubManager.authenticate(buildRestClient(credentials), subscriptionId);
        }
    }
    private EventHubManager(RestClient restClient, String subscriptionId) {
        super(
                restClient,
                subscriptionId,
                new EventHubManagementClientImpl(restClient).withSubscriptionId(subscriptionId));
        storageManager = StorageManager.authenticate(restClient, subscriptionId);

    }

    /**
     * @return entry point to manage EventHub namespaces
     */
    @Beta(SinceVersion.V2_0_0)
    public EventHubNamespaces namespaces() {
        if (this.namespaces == null) {
            this.namespaces = new EventHubNamespacesImpl(this);
        }
        return this.namespaces;
    }

    /**
     * @return entry point to manage event hubs
     */
    @Beta(SinceVersion.V2_0_0)
    public EventHubs eventHubs() {
        if (this.eventHubs == null) {
            this.eventHubs = new EventHubsImpl(this, this.storageManager);
        }
        return this.eventHubs;
    }

    /**
     * @return entry point to manage event hub consumer groups
     */
    @Beta(SinceVersion.V2_0_0)
    public EventHubConsumerGroups consumerGroups() {
        if (this.consumerGroups == null) {
            this.consumerGroups = new EventHubConsumerGroupsImpl(this);
        }
        return this.consumerGroups;
    }

    /**
     * @return entry point to manage disaster recovery pairing of event hub namespaces.
     */
    @Beta(SinceVersion.V2_0_0)
    public EventHubDisasterRecoveryPairings eventHubDisasterRecoveryPairings() {
        if (this.eventHubDisasterRecoveryPairings == null) {
            this.eventHubDisasterRecoveryPairings = new EventHubDisasterRecoveryPairingsImpl(this);
        }
        return this.eventHubDisasterRecoveryPairings;
    }

    /**
     * @return entry point to manage event hub authorization rules.
     */
    @Beta(SinceVersion.V2_0_0)
    public EventHubAuthorizationRules eventHubAuthorizationRules() {
        if (this.eventHubAuthorizationRules == null) {
            this.eventHubAuthorizationRules = new EventHubAuthorizationRulesImpl(this);
        }
        return this.eventHubAuthorizationRules;
    }

    /**
     * @return entry point to manage event hub namespace authorization rules.
     */
    @Beta(SinceVersion.V2_0_0)
    public EventHubNamespaceAuthorizationRules namespaceAuthorizationRules() {
        if (this.namespaceAuthorizationRules == null) {
            this.namespaceAuthorizationRules = new EventHubNamespaceAuthorizationRulesImpl(this);
        }
        return this.namespaceAuthorizationRules;
    }

    /**
     * @return entry point to manage disaster recovery pairing authorization rules.
     */
    @Beta(SinceVersion.V2_0_0)
    public DisasterRecoveryPairingAuthorizationRules disasterRecoveryPairingAuthorizationRules() {
        if (this.disasterRecoveryPairingAuthorizationRules == null) {
            this.disasterRecoveryPairingAuthorizationRules = new DisasterRecoveryPairingAuthorizationRulesImpl(this);
        }
        return this.disasterRecoveryPairingAuthorizationRules;
    }
}