/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * Type representing authorization rule defined for subscription.
 */
public interface SubscriptionAuthorizationRule extends
        AuthorizationRule<SubscriptionAuthorizationRule>,
        Updatable<SubscriptionAuthorizationRule.Update>,
        HasParent<Subscription> {
    /**
     * @return the name of the namespace that the grand-parent topic belongs to
     */
    String namespaceName();

    /**
     * @return the name of the topic the the parent subscription belongs to
     */
    String topicName();

    /**
     * @return the name of the parent subscription name
     */
    String subscriptionName();

    /**
     * Grouping of service bus subscription authorization rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of subscription authorization rule definition.
         */
        interface Blank extends AuthorizationRule.DefinitionStages.WithAccessRight<SubscriptionAuthorizationRule.DefinitionStages.WithCreate> {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<SubscriptionAuthorizationRule> {
        }
    }

    /**
     * The entirety of the subscription authorization rule definition.
     */
    interface Definition extends
            SubscriptionAuthorizationRule.DefinitionStages.Blank,
            SubscriptionAuthorizationRule.DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the subscription authorization rule update.
     */
    interface Update extends
            Appliable<SubscriptionAuthorizationRule>,
            AuthorizationRule.UpdateStages.WithAccessRight<SubscriptionAuthorizationRule.Update> {
    }

    /**
     * Grouping of service bus subscription rule update stages.
     */
    interface UpdateStages {
    }
}
