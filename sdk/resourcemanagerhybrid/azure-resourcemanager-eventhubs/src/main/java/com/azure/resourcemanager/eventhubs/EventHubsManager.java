// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.eventhubs.fluent.EventHubManagementClient;
import com.azure.resourcemanager.eventhubs.implementation.EventHubManagementClientBuilder;
import com.azure.resourcemanager.eventhubs.implementation.DisasterRecoveryPairingAuthorizationRulesImpl;
import com.azure.resourcemanager.eventhubs.implementation.EventHubAuthorizationRulesImpl;
import com.azure.resourcemanager.eventhubs.implementation.EventHubConsumerGroupsImpl;
import com.azure.resourcemanager.eventhubs.implementation.EventHubDisasterRecoveryPairingsImpl;
import com.azure.resourcemanager.eventhubs.implementation.EventHubNamespaceAuthorizationRulesImpl;
import com.azure.resourcemanager.eventhubs.implementation.EventHubNamespacesImpl;
import com.azure.resourcemanager.eventhubs.implementation.EventHubsImpl;
import com.azure.resourcemanager.eventhubs.models.DisasterRecoveryPairingAuthorizationRules;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationRules;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroups;
import com.azure.resourcemanager.eventhubs.models.EventHubDisasterRecoveryPairings;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRules;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaces;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.storage.StorageManager;

/**
 * Entry point to Azure EventHub resource management.
 */
public final class EventHubsManager extends Manager<EventHubManagementClient> {
    private EventHubNamespaces namespaces;
    private EventHubs eventHubs;
    private EventHubConsumerGroups consumerGroups;
    private EventHubAuthorizationRules eventHubAuthorizationRules;
    private EventHubNamespaceAuthorizationRules namespaceAuthorizationRules;
    private EventHubDisasterRecoveryPairings eventHubDisasterRecoveryPairings;
    private DisasterRecoveryPairingAuthorizationRules disasterRecoveryPairingAuthorizationRules;

    private final StorageManager storageManager;

    /**
     * Get a Configurable instance that can be used to create EventHubsManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new EventHubsManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of EventHubsManager that exposes EventHub resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the EventHubsManager
     */
    public static EventHubsManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of EventHubsManager that exposes EventHub resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the EventHubsManager
     */
    private static EventHubsManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new EventHubsManager(httpPipeline, profile);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of EventHubsManager that exposes EventHub management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing EventHub management API entry points that work across subscriptions
         */
        EventHubsManager authenticate(TokenCredential credential, AzureProfile profile);
    }
    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public EventHubsManager authenticate(TokenCredential credential, AzureProfile profile) {
            return EventHubsManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }
    private EventHubsManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(
            httpPipeline,
            profile,
            new EventHubManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
        storageManager = AzureConfigurableImpl.configureHttpPipeline(httpPipeline, StorageManager.configure())
            .authenticate(null, profile);
    }

    /**
     * @return entry point to manage EventHub namespaces
     */
    public EventHubNamespaces namespaces() {
        if (this.namespaces == null) {
            this.namespaces = new EventHubNamespacesImpl(this);
        }
        return this.namespaces;
    }

    /**
     * @return entry point to manage event hubs
     */
    public EventHubs eventHubs() {
        if (this.eventHubs == null) {
            this.eventHubs = new EventHubsImpl(this, this.storageManager);
        }
        return this.eventHubs;
    }

    /**
     * @return entry point to manage event hub consumer groups
     */
    public EventHubConsumerGroups consumerGroups() {
        if (this.consumerGroups == null) {
            this.consumerGroups = new EventHubConsumerGroupsImpl(this);
        }
        return this.consumerGroups;
    }

    /**
     * @return entry point to manage disaster recovery pairing of event hub namespaces.
     */
    public EventHubDisasterRecoveryPairings eventHubDisasterRecoveryPairings() {
        if (this.eventHubDisasterRecoveryPairings == null) {
            this.eventHubDisasterRecoveryPairings = new EventHubDisasterRecoveryPairingsImpl(this);
        }
        return this.eventHubDisasterRecoveryPairings;
    }

    /**
     * @return entry point to manage event hub authorization rules.
     */
    public EventHubAuthorizationRules eventHubAuthorizationRules() {
        if (this.eventHubAuthorizationRules == null) {
            this.eventHubAuthorizationRules = new EventHubAuthorizationRulesImpl(this);
        }
        return this.eventHubAuthorizationRules;
    }

    /**
     * @return entry point to manage event hub namespace authorization rules.
     */
    public EventHubNamespaceAuthorizationRules namespaceAuthorizationRules() {
        if (this.namespaceAuthorizationRules == null) {
            this.namespaceAuthorizationRules = new EventHubNamespaceAuthorizationRulesImpl(this);
        }
        return this.namespaceAuthorizationRules;
    }

    /**
     * @return entry point to manage disaster recovery pairing authorization rules.
     */
    public DisasterRecoveryPairingAuthorizationRules disasterRecoveryPairingAuthorizationRules() {
        if (this.disasterRecoveryPairingAuthorizationRules == null) {
            this.disasterRecoveryPairingAuthorizationRules = new DisasterRecoveryPairingAuthorizationRulesImpl(this);
        }
        return this.disasterRecoveryPairingAuthorizationRules;
    }
}
