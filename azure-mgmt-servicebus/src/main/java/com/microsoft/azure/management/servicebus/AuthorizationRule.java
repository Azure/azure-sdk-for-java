/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.servicebus;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.servicebus.implementation.ServiceBusManager;
import com.microsoft.azure.management.servicebus.implementation.SharedAccessAuthorizationRuleInner;

import java.util.List;

/**
 */
@Fluent
public interface AuthorizationRule extends
        IndependentChildResource<ServiceBusManager, SharedAccessAuthorizationRuleInner>,
    Refreshable<AuthorizationRule>,
    Updatable<AuthorizationRule.Update> {

    /**
     * The rights associated with the rule.
     */
    List<AccessRights> rights();

    /**
     * Gets the primary and secondary connection strings for the namespace.
     */
    void listKeys();

    /**
     * Regenerates primary or secondary connection strings for the topic.
     */
    void regenerateKeys();


    interface Definition extends
        AuthorizationRule.DefinitionStages.Blank,
        AuthorizationRule.DefinitionStages.WithGroup,
        AuthorizationRule.DefinitionStages.WithCreate{
    }

    interface DefinitionStages {

        interface WithResourceGroupNameParameter {
            /**
             * Name of the Resource group within the Azure subscription.
             *
             * @param resourceGroupNameParameter
             * @return the next stage
             */
            Definition withResourceGroupNameParameter(String resourceGroupNameParameter);
        }

        interface WithNamespaceNameParameter {
            /**
             * The namespace name
             *
             * @param namespaceNameParameter
             * @return the next stage
             */
            Definition withNamespaceNameParameter(String namespaceNameParameter);
        }

        interface WithTopicNameParameter {
            /**
             * The topic name.
             *
             * @param topicNameParameter
             * @return the next stage
             */
            Definition withTopicNameParameter(String topicNameParameter);
        }

        interface WithAuthorizationRuleNameParameter {
            /**
             * The authorizationrule name.
             *
             * @param authorizationRuleNameParameter
             * @return the next stage
             */
            Definition withAuthorizationRuleNameParameter(String authorizationRuleNameParameter);
        }

        interface WithRights {
            Definition withRights(AccessRights rights);
        }

        interface WithCreate extends
            Creatable<AuthorizationRule>,
            AuthorizationRule.DefinitionStages.WithResourceGroupNameParameter,
            AuthorizationRule.DefinitionStages.WithNamespaceNameParameter,
            AuthorizationRule.DefinitionStages.WithTopicNameParameter,
            AuthorizationRule.DefinitionStages.WithAuthorizationRuleNameParameter,
            AuthorizationRule.DefinitionStages.WithRights{
        }

        interface Blank extends
            GroupableResource.DefinitionWithRegion<WithGroup>{
        }

        interface WithGroup extends
            GroupableResource.DefinitionStages.WithGroup<WithCreate>{
        }
    }

    interface Update extends
        AuthorizationRule.UpdateStages.WithResourceGroupNameParameter,
        AuthorizationRule.UpdateStages.WithNamespaceNameParameter,
        AuthorizationRule.UpdateStages.WithTopicNameParameter,
        AuthorizationRule.UpdateStages.WithAuthorizationRuleNameParameter,
        AuthorizationRule.UpdateStages.WithRights{
    }

    interface UpdateStages {

        interface WithResourceGroupNameParameter {
            /**
             * Name of the Resource group within the Azure subscription.
             *
             * @param resourceGroupNameParameter
             * @return the next stage
             */
            Update withResourceGroupNameParameter(String resourceGroupNameParameter);
        }

        interface WithNamespaceNameParameter {
            /**
             * The namespace name
             *
             * @param namespaceNameParameter
             * @return the next stage
             */
            Update withNamespaceNameParameter(String namespaceNameParameter);
        }

        interface WithTopicNameParameter {
            /**
             * The topic name.
             *
             * @param topicNameParameter
             * @return the next stage
             */
            Update withTopicNameParameter(String topicNameParameter);
        }

        interface WithAuthorizationRuleNameParameter {
            /**
             * The authorizationrule name.
             *
             * @param authorizationRuleNameParameter
             * @return the next stage
             */
            Update withAuthorizationRuleNameParameter(String authorizationRuleNameParameter);
        }

        interface WithRights {
            Update withRights(AccessRights rights);
        }
    }

}
