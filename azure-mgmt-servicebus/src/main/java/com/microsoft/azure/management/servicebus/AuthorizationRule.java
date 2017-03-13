/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.azure.management.servicebus.implementation.SharedAccessAuthorizationRuleInner;

import java.util.List;

/**
 * Type representing authorization rule.
 *
 * @param <RuleT> the specific rule type
 */
@Fluent
public interface AuthorizationRule<RuleT extends AuthorizationRule> extends
        IndependentChildResource<ServiceBusManager, SharedAccessAuthorizationRuleInner>,
        Refreshable<RuleT> {

    /**
     * The rights associated with the rule.
     */
    List<AccessRights> rights();

    /**
     * @return the primary and secondary keys
     */
    void listKeys();

    /**
     * Regenerates primary or secondary keys.
     */
    void regenerateKeys();

    /**
     * Grouping of commons authorization rule definition stages shared between different service bus
     * entities (namespace, queue, topic, subscription) access rules.
     */
    interface DefinitionStages {
        /**
         * The stage of the rule definition allowing to specify the access rights.
         *
         * @param <T> the next stage
         */
        interface WithAccessRight<T> {
            /**
             * Specifies the access rights.
             *
             * @param rights the access rights
             * @return the next stage
             */
            T withAccessRight(AccessRights rights);
        }
    }

    /**
     * Grouping of commons authorization rule update stages shared between different service bus
     * entities (namespace, queue, topic, subscription) access rules.
     */
    interface UpdateStages {
        /**
         * The stage of the rule definition allowing to add or remove access rights.
         *
         * @param <T> the next stage
         */
        interface WithAccessRight<T> {
            /**
             * Specifies the access rights.
             *
             * @param rights the access rights
             * @return the next stage
             */
            T withAccessRight(AccessRights rights);
            /**
             * Removes the access rights.
             *
             * @param rights the access rights
             * @return the next stage
             */
            T withoutAccessRight(AccessRights rights);
        }
    }
}
