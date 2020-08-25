/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;
import com.microsoft.azure.management.eventhub.implementation.ArmDisasterRecoveryInner;
import com.microsoft.azure.management.eventhub.implementation.EventHubManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import rx.Completable;
import rx.Observable;

/**
 * Type representing disaster recovery pairing for event hub namespaces.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubDisasterRecoveryPairing extends
        NestedResource,
        HasManager<EventHubManager>,
        Refreshable<EventHubDisasterRecoveryPairing>,
        Updatable<EventHubDisasterRecoveryPairing.Update>,
        HasInner<ArmDisasterRecoveryInner> {
    /**
     * @return primary event hub namespace resource group
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String primaryNamespaceResourceGroupName();

    /**
     * @return primary event hub namespace in the pairing
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String primaryNamespaceName();
    /**
     * @return secondary event hub namespace in the pairing
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String secondaryNamespaceId();
    /**
     * @return the namespace role
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    RoleDisasterRecovery namespaceRole();
    /**
     * @return provisioning state of the pairing
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    ProvisioningStateDR provisioningState();

    /**
     * Break the pairing between a primary and secondary namespace.
     *
     * @return completable representing the pairing break action
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Completable breakPairingAsync();
    /**
     * Break the pairing between a primary and secondary namespace.
     */
    @Method
    @Beta(Beta.SinceVersion.V1_7_0)
    void breakPairing();
    /**
     * Perform fail over so that the secondary namespace becomes the primary.
     *
     * @return completable representing the fail-over action
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Completable failOverAsync();
    /**
     * Perform fail over so that the secondary namespace becomes the primary.
     */
    @Method
    @Beta(Beta.SinceVersion.V1_7_0)
    void failOver();
    /**
     * @return the authorization rules for the event hub disaster recovery pairing
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    Observable<DisasterRecoveryPairingAuthorizationRule> listAuthorizationRulesAsync();
    /**
     * @return the authorization rules for the event hub disaster recovery pairing
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    PagedList<DisasterRecoveryPairingAuthorizationRule> listAuthorizationRules();

    /**
     * The entirety of the event hub disaster recovery pairing definition.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithPrimaryNamespace,
            DefinitionStages.WithSecondaryNamespace,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of disaster recovery pairing definition stages.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface DefinitionStages {
        /**
         * The first stage of a disaster recovery pairing definition.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface Blank extends WithPrimaryNamespace {
        }

        /**
         * The stage of the disaster recovery pairing definition allowing to specify primary event hub namespace.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithPrimaryNamespace {
            /**
             * Specifies that the given namespace should be used as primary namespace in disaster recovery pairing.
             *
             * @param namespaceCreatable creatable definition for the primary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithSecondaryNamespace withNewPrimaryNamespace(Creatable<EventHubNamespace> namespaceCreatable);
            /**
             * Specifies that the given namespace should be used as primary namespace in disaster recovery pairing.
             *
             * @param namespace the primary event hub namespace
             * @return next stage of the disaster recovery pairing definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithSecondaryNamespace withExistingPrimaryNamespace(EventHubNamespace namespace);
            /**
             * Specifies that the given namespace should be used as primary namespace in disaster recovery pairing.
             *
             * @param resourceGroupName resource group name of primary namespace
             * @param  namespaceName the primary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithSecondaryNamespace withExistingPrimaryNamespace(String resourceGroupName, String namespaceName);
            /**
             * Specifies that the given namespace should be used as primary namespace in disaster recovery pairing.
             *
             * @param namespaceId the primary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithSecondaryNamespace withExistingPrimaryNamespaceId(String namespaceId);
        }

        /**
         * The stage of the disaster recovery pairing definition allowing to specify the secondary event hub namespace.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithSecondaryNamespace {
            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespaceCreatable creatable definition for the primary namespace
             * @return next stage of the event hub definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withNewSecondaryNamespace(Creatable<EventHubNamespace> namespaceCreatable);
            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespace the secondary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withExistingSecondaryNamespace(EventHubNamespace namespace);
            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespaceId the secondary namespace
             * @return next stage of the disaster recovery pairing definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithCreate withExistingSecondaryNamespaceId(String namespaceId);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithCreate extends
                Creatable<EventHubDisasterRecoveryPairing> {
        }
    }

    /**
     * Grouping of disaster recovery pairing update stages.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface UpdateStages {
        /**
         * The stage of the disaster recovery pairing definition allowing to specify primary event hub namespace.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithSecondaryNamespace {
            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespaceCreatable creatable definition for the secondary namespace
             * @return next stage of the disaster recovery pairing update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withNewSecondaryNamespace(Creatable<EventHubNamespace> namespaceCreatable);
            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespace the secondary event hub namespace
             * @return next stage of the disaster recovery pairing update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withExistingSecondaryNamespace(EventHubNamespace namespace);
            /**
             * Specifies that the given namespace should be used as secondary namespace in disaster recovery pairing.
             *
             * @param namespaceId the secondary namespace
             * @return next stage of the disaster recovery pairing update
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            Update withExistingSecondaryNamespaceId(String namespaceId);
        }
    }

    /**
     * The template for a disaster recovery pairing update operation, containing all the settings
     * that can be modified.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Update extends
            UpdateStages.WithSecondaryNamespace,
            Appliable<EventHubDisasterRecoveryPairing> {
    }
}
