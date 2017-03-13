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
 * Type representing authorization rule defined for topic.
 */
public interface TopicAuthorizationRule extends
        AuthorizationRule<TopicAuthorizationRule>,
        Updatable<TopicAuthorizationRule.Update>,
        HasParent<Topic> {
    /**
     * @return the name of the namespace that the parent topic belongs to
     */
    String namespaceName();

    /**
     * @return the name of the parent topic name
     */
    String topicName();

    /**
     * Grouping of service bus topic authorization rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of topic authorization rule definition.
         */
        interface Blank extends AuthorizationRule.DefinitionStages.WithAccessRight<TopicAuthorizationRule.DefinitionStages.WithCreate> {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created (via {@link WithCreate#create()}), but also allows
         * for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<TopicAuthorizationRule> {
        }
    }

    /**
     * The entirety of the topic authorization rule definition.
     */
    interface Definition extends
            TopicAuthorizationRule.DefinitionStages.Blank,
            TopicAuthorizationRule.DefinitionStages.WithCreate {
    }

    /**
     * The entirety of the topic authorization rule update.
     */
    interface Update extends
            Appliable<TopicAuthorizationRule>,
            AuthorizationRule.UpdateStages.WithAccessRight<TopicAuthorizationRule.Update> {
    }

    /**
     * Grouping of service bus topic rule update stages.
     */
    interface UpdateStages {
    }
}

