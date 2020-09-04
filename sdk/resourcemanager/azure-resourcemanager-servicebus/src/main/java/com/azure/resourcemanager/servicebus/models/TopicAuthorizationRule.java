// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.servicebus.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/**
 * Type representing authorization rule defined for topic.
 */
@Fluent
public interface TopicAuthorizationRule extends
    AuthorizationRule<TopicAuthorizationRule>,
    Updatable<TopicAuthorizationRule.Update> {
    /**
     * @return the name of the namespace that the parent topic belongs to
     */
    String namespaceName();

    /**
     * @return the name of the parent topic name
     */
    String topicName();

    /**
     * Grouping of Service Bus topic authorization rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of topic authorization rule definition.
         */
        interface Blank extends AuthorizationRule
                .DefinitionStages
                .WithListenOrSendOrManage<TopicAuthorizationRule.DefinitionStages.WithCreate> {
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
        AuthorizationRule.UpdateStages.WithListenOrSendOrManage<TopicAuthorizationRule.Update> {
    }
}
