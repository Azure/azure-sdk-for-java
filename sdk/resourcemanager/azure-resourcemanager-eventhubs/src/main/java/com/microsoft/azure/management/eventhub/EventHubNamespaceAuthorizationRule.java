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
 * Type representing authorization rule of an event hub namespace.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface EventHubNamespaceAuthorizationRule
        extends AuthorizationRule<EventHubNamespaceAuthorizationRule> {
    /**
     * @return the resource group of the namespace where parent event hub resides.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String namespaceResourceGroupName();
    /**
     * @return the name of the event hub namespace.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    String namespaceName();

    /**
     * Grouping of Event Hub namespace authorization rule definition stages.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface DefinitionStages {
        /**
         * The first stage of event hub namespace authorization rule definition.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface Blank extends WithNamespace {
        }

        /**
         * Stage of the authorization rule definition allowing to specify the event namespace for which rule needs to be created.
         */
        @Beta(Beta.SinceVersion.V1_7_0)
        interface WithNamespace {
            /**
             * Specifies that authorization rule needs to be created for the given event hub namespace.
             *
             * @param namespaceResourceId the resource id of the event Hub namespace
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithAccessPolicy withExistingNamespaceId(String namespaceResourceId);
            /**
             * Specifies that authorization rule needs to be created for the given event hub namespace.
             *
             * @param resourceGroupName namespace resource group name
             * @param namespaceName namespace name
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithAccessPolicy withExistingNamespace(String resourceGroupName, String namespaceName);
            /**
             * Specifies that authorization rule needs to be created for the given event hub namespace.
             *
             * @param namespace the namespace
             * @return the next stage of the definition
             */
            @Beta(Beta.SinceVersion.V1_7_0)
            WithAccessPolicy withExistingNamespace(EventHubNamespace namespace);
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
        interface WithCreate extends Creatable<EventHubNamespaceAuthorizationRule> {
        }
    }

    /**
     * The entirety of the event hub namespace authorization rule definition.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithNamespace,
            DefinitionStages.WithAccessPolicy,
            DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the event hub namespace authorization rule update.
     */
    @Beta(Beta.SinceVersion.V1_7_0)
    interface Update extends
            Appliable<EventHubNamespaceAuthorizationRule>,
            AuthorizationRule.UpdateStages.WithListenOrSendOrManage<EventHubNamespaceAuthorizationRule.Update> {
    }
}
