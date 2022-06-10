// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

/**
 * Type representing authorization rule of an event hub namespace.
 */
@Fluent
public interface EventHubNamespaceAuthorizationRule
        extends AuthorizationRule<EventHubNamespaceAuthorizationRule> {
    /**
     * @return the resource group of the namespace where parent event hub resides.
     */
    String namespaceResourceGroupName();

    /**
     * @return the name of the event hub namespace.
     */
    String namespaceName();

    /**
     * Grouping of Event Hub namespace authorization rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of event hub namespace authorization rule definition.
         */
        interface Blank extends WithNamespace {
        }

        /**
         * Stage of the authorization rule definition allowing to specify
         * the event namespace for which rule needs to be created.
         */
        interface WithNamespace {
            /**
             * Specifies that authorization rule needs to be created for the given event hub namespace.
             *
             * @param namespaceResourceId the resource id of the event Hub namespace
             * @return the next stage of the definition
             */
            WithAccessPolicy withExistingNamespaceId(String namespaceResourceId);

            /**
             * Specifies that authorization rule needs to be created for the given event hub namespace.
             *
             * @param resourceGroupName namespace resource group name
             * @param namespaceName namespace name
             * @return the next stage of the definition
             */
            WithAccessPolicy withExistingNamespace(String resourceGroupName, String namespaceName);

            /**
             * Specifies that authorization rule needs to be created for the given event hub namespace.
             *
             * @param namespace the namespace
             * @return the next stage of the definition
             */
            WithAccessPolicy withExistingNamespace(EventHubNamespace namespace);
        }

        /**
         * Stage of the authorization rule definition allowing to specify access policy.
         */
        interface WithAccessPolicy extends AuthorizationRule
                .DefinitionStages
                .WithListenOrSendOrManage<WithCreate> {

        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<EventHubNamespaceAuthorizationRule> {
        }
    }

    /**
     * The entirety of the event hub namespace authorization rule definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithNamespace,
            DefinitionStages.WithAccessPolicy,
            DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the event hub namespace authorization rule update.
     */
    interface Update extends
        Appliable<EventHubNamespaceAuthorizationRule>,
        AuthorizationRule.UpdateStages.WithListenOrSendOrManage<EventHubNamespaceAuthorizationRule.Update> {
    }
}
