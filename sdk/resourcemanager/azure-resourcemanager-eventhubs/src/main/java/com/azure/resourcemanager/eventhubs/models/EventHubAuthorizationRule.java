// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

/**
 * Type representing authorization rule of an event hub.
 */
@Fluent
public interface EventHubAuthorizationRule
        extends
        AuthorizationRule<EventHubAuthorizationRule> {
    /**
     * @return the resource group of the namespace where parent event hub resides
     */
    String namespaceResourceGroupName();
    /**
     * @return the namespace name of parent event hub
     */
    String namespaceName();
    /**
     * @return the name of the parent event hub
     */
    String eventHubName();

    /**
     * Grouping of event hub authorization rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of event hub authorization rule definition.
         */
        interface Blank extends WithEventHub {
        }

        /**
         * Stage of the authorization rule definition allowing to specify the event for which rule needs to be created.
         */
        interface WithEventHub {
            /**
             * Specifies that authorization rule needs to be created for the given event hub.
             *
             * @param eventHubResourceId the resource id of the event Hub
             * @return the next stage of the definition
             */
            WithAccessPolicy withExistingEventHubId(String eventHubResourceId);
            /**
             * Specifies that authorization rule needs to be created for the given event hub.
             *
             * @param resourceGroupName event hub namespace resource group name
             * @param namespaceName event hub parent namespace name
             * @param eventHubName event hub name
             * @return the next stage of the definition
             */
            WithAccessPolicy withExistingEventHub(String resourceGroupName, String namespaceName, String eventHubName);
            /**
             * Specifies that authorization rule needs to be created for the given event hub.
             *
             * @param eventHub the event hub
             * @return the next stage of the definition
             */
            WithAccessPolicy withExistingEventHub(EventHub eventHub);
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
        interface WithCreate extends Creatable<EventHubAuthorizationRule> {
        }
    }

    /**
     * The entirety of the event hub namespace authorization rule definition.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithEventHub,
            DefinitionStages.WithAccessPolicy,
            DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the event hub authorization rule update.
     */
    interface Update extends
        Appliable<EventHubAuthorizationRule>,
        UpdateStages.WithListenOrSendOrManage<EventHubAuthorizationRule.Update> {
    }
}
