// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.eventhubs.fluent.models.ArmDisasterRecoveryInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import reactor.core.publisher.Mono;

/**
 * Type representing disaster recovery pairing for event hub namespaces.
 */
@Fluent
public interface EventHubDisasterRecoveryPairing extends
    NestedResource,
    HasManager<EventHubsManager>,
    Refreshable<EventHubDisasterRecoveryPairing>,
    Updatable<EventHubDisasterRecoveryPairing.Update>,
        HasInnerModel<ArmDisasterRecoveryInner> {
    /**
     * @return primary event hub namespace resource group
     */
    String primaryNamespaceResourceGroupName();

    /**
     * @return primary event hub namespace in the pairing
     */
    String primaryNamespaceName();

    /**
     * @return secondary event hub namespace in the pairing
     */
    String secondaryNamespaceId();

    /**
     * @return the namespace role
     */
    RoleDisasterRecovery namespaceRole();

    /**
     * @return provisioning state of the pairing
     */
    ProvisioningStateDR provisioningState();

    /**
     * Break the pairing between a primary and secondary namespace.
     *
     * @return completable representing the pairing break action
     */
    Mono<Void> breakPairingAsync();

    /**
     * Break the pairing between a primary and secondary namespace.
     */
    void breakPairing();

    /**
     * Perform fail over so that the secondary namespace becomes the primary.
     *
     * @return completable representing the fail-over action
     */
    Mono<Void> failOverAsync();

    /**
     * Perform fail over so that the secondary namespace becomes the primary.
     */
    void failOver();

    /**
     * @return the authorization rules for the event hub disaster recovery pairing
     */
    PagedFlux<DisasterRecoveryPairingAuthorizationRule> listAuthorizationRulesAsync();

    /**
     * @return the authorization rules for the event hub disaster recovery pairing
     */
    PagedIterable<DisasterRecoveryPairingAuthorizationRule> listAuthorizationRules();

    /**
     * The entirety of the event hub disaster recovery pairing definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithPrimaryNamespace,
            DefinitionStages.WithSecondaryNamespace,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of disaster recovery pairing definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a disaster recovery pairing definition.
         */
        interface Blank extends WithPrimaryNamespace {
        }

        /**
         * The stage of the disaster recovery pairing definition allowing to specify primary event hub namespace.
         */
        interface WithPrimaryNamespace {
            /**
             * Specifies that the given namespace should be used as primary namespace in disaster recovery pairing.
             *
             * @param namespaceCreatable creatable definition for the primary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            WithSecondaryNamespace withNewPrimaryNamespace(Creatable<EventHubNamespace> namespaceCreatable);

            /**
             * Specifies that the given namespace should be used as primary namespace in disaster recovery pairing.
             *
             * @param namespace the primary event hub namespace
             * @return next stage of the disaster recovery pairing definition
             */
            WithSecondaryNamespace withExistingPrimaryNamespace(EventHubNamespace namespace);

            /**
             * Specifies that the given namespace should be used as primary namespace in disaster recovery pairing.
             *
             * @param resourceGroupName resource group name of primary namespace
             * @param  namespaceName the primary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            WithSecondaryNamespace withExistingPrimaryNamespace(String resourceGroupName, String namespaceName);

            /**
             * Specifies that the given namespace should be used as primary namespace in disaster recovery pairing.
             *
             * @param namespaceId the primary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            WithSecondaryNamespace withExistingPrimaryNamespaceId(String namespaceId);
        }

        /**
         * The stage of the disaster recovery pairing definition allowing to specify the secondary event hub namespace.
         */
        interface WithSecondaryNamespace {
            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespaceCreatable creatable definition for the primary namespace
             * @return next stage of the event hub definition
             */
            WithCreate withNewSecondaryNamespace(Creatable<EventHubNamespace> namespaceCreatable);

            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespace the secondary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            WithCreate withExistingSecondaryNamespace(EventHubNamespace namespace);

            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespaceId the secondary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            WithCreate withExistingSecondaryNamespaceId(String namespaceId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends
            Creatable<EventHubDisasterRecoveryPairing> {
        }
    }

    /**
     * Grouping of disaster recovery pairing update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the disaster recovery pairing definition allowing to specify primary event hub namespace.
         */
        interface WithSecondaryNamespace {
            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespaceCreatable creatable definition for the secondary namespace
             * @return next stage of the disaster recovery pairing update
             */
            Update withNewSecondaryNamespace(Creatable<EventHubNamespace> namespaceCreatable);

            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespace the secondary event hub namespace
             * @return next stage of the disaster recovery pairing update
             */
            Update withExistingSecondaryNamespace(EventHubNamespace namespace);

            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespaceId the secondary namespace
             * @return next stage of the disaster recovery pairing update
             */
            Update withExistingSecondaryNamespaceId(String namespaceId);
        }
    }

    /**
     * The template for a disaster recovery pairing update operation, containing all the settings
     * that can be modified.
     */
    interface Update extends
        UpdateStages.WithSecondaryNamespace,
        Appliable<EventHubDisasterRecoveryPairing> {
    }
}
