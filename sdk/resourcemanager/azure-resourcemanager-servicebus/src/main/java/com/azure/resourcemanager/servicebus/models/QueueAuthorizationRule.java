// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/**
 * Type representing authorization rule defined for queue.
 */
@Fluent
public interface QueueAuthorizationRule extends
    AuthorizationRule<QueueAuthorizationRule>,
    Updatable<QueueAuthorizationRule.Update> {
    /**
     * @return the name of the namespace that the parent queue belongs to
     */
    String namespaceName();

    /**
     * @return the name of the parent queue name
     */
    String queueName();

    /**
     * Grouping of Service Bus queue authorization rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of queue authorization rule definition.
         */
        interface Blank extends AuthorizationRule.DefinitionStages.WithListenOrSendOrManage<
            QueueAuthorizationRule.DefinitionStages.WithCreate> {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<QueueAuthorizationRule> {
        }
    }

    /**
     * The entirety of the queue authorization rule definition.
     */
    interface Definition extends
            QueueAuthorizationRule.DefinitionStages.Blank,
            QueueAuthorizationRule.DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the queue authorization rule update.
     */
    interface Update extends
        Appliable<QueueAuthorizationRule>,
        AuthorizationRule.UpdateStages.WithListenOrSendOrManage<QueueAuthorizationRule.Update> {
    }
}
