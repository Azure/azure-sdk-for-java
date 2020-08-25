/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * Type representing authorization rule of an event hub.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubAuthorizationRule
        extends
        AuthorizationRule<EventHubAuthorizationRule> {
    /**
     * @return the resource group of the namespace where parent event hub resides
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String namespaceResourceGroupName();
    /**
     * @return the namespace name of parent event hub
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String namespaceName();
    /**
     * @return the name of the parent event hub
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String eventHubName();

    /**
     * Grouping of event hub authorization rule definition stages.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface DefinitionStages {
        /**
         * The first stage of event hub authorization rule definition.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface Blank extends WithEventHub {
        }

        /**
         * Stage of the authorization rule definition allowing to specify the event for which rule needs to be created.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithEventHub {
            /**
             * Specifies that authorization rule needs to be created for the given event hub.
             *
             * @param eventHubResourceId the resource id of the event Hub
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithAccessPolicy withExistingEventHubId(String eventHubResourceId);
            /**
             * Specifies that authorization rule needs to be created for the given event hub.
             *
             * @param resourceGroupName event hub namespace resource group name
             * @param namespaceName event hub parent namespace name
             * @param eventHubName event hub name
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithAccessPolicy withExistingEventHub(String resourceGroupName, String namespaceName, String eventHubName);
            /**
             * Specifies that authorization rule needs to be created for the given event hub.
             *
             * @param eventHub the event hub
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithAccessPolicy withExistingEventHub(EventHub eventHub);
        }

        /**
         * Stage of the authorization rule definition allowing to specify access policy.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithAccessPolicy extends AuthorizationRule
                .DefinitionStages
                .WithListenOrSendOrManage<WithCreate> {

        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithCreate extends Creatable<EventHubAuthorizationRule> {
        }
    }

    /**
     * The entirety of the event hub namespace authorization rule definition.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithEventHub,
            DefinitionStages.WithAccessPolicy,
            DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the event hub authorization rule update.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Update extends
            Appliable<EventHubAuthorizationRule>,
            UpdateStages.WithListenOrSendOrManage<EventHubAuthorizationRule.Update> {
    }
}
