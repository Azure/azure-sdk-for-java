// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cdn.fluent.models.RuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.List;

/**
 * An immutable client-side representation of an Azure Front Door (AFD) rule that lives under a
 * {@link RuleSet}.
 */
@Fluent
public interface Rule extends ExternalChildResource<Rule, RuleSet>, HasInnerModel<RuleInner> {

    /**
     * Gets the name of the rule set containing this rule.
     *
     * @return the rule set name
     */
    String ruleSetName();

    /**
     * Gets the order in which the rule is applied for the endpoint.
     *
     * @return the order
     */
    Integer order();

    /**
     * Gets the list of conditions that must be matched for the actions to be executed.
     *
     * @return the conditions
     */
    List<DeliveryRuleCondition> conditions();

    /**
     * Gets the list of actions that are executed when all the conditions of a rule are satisfied.
     *
     * @return the actions
     */
    List<DeliveryRuleAction> actions();

    /**
     * Gets whether the rules engine should continue running the remaining rules or stop
     * after this rule is matched.
     *
     * @return the match processing behavior
     */
    MatchProcessingBehavior matchProcessingBehavior();

    /**
     * Gets the provisioning state reported by the service.
     *
     * @return the provisioning state
     */
    AfdProvisioningState provisioningState();

    /**
     * Gets the deployment status for the rule.
     *
     * @return the deployment status
     */
    DeploymentStatus deploymentStatus();

    /**
     * Grouping of rule definition stages as part of a parent {@link RuleSet} definition.
     */
    interface DefinitionStages {
        /**
         * The first stage of a rule definition.
         *
         * @param <ParentT> the stage of the parent rule set definition to return to after attaching
         */
        interface Blank<ParentT> extends WithOrder<ParentT> {
        }

        /**
         * The stage of a rule definition requiring the order to be specified.
         *
         * @param <ParentT> the stage of the parent rule set definition to return to after attaching
         */
        interface WithOrder<ParentT> {
            /**
             * Specifies the order in which the rule is applied for the endpoint.
             * A rule with a lesser order will be applied before a rule with a greater order.
             * Rule with order 0 is a special rule that does not require any condition and actions
             * listed in it will always be applied.
             *
             * @param order the order value
             * @return the next stage of the definition
             */
            WithActions<ParentT> withOrder(int order);
        }

        /**
         * The stage of a rule definition requiring the actions to be specified.
         *
         * @param <ParentT> the stage of the parent rule set definition to return to after attaching
         */
        interface WithActions<ParentT> {
            /**
             * Specifies the list of actions that are executed when all the conditions of a rule are satisfied.
             *
             * @param actions the list of actions
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withActions(List<DeliveryRuleAction> actions);
        }

        /**
         * The stage of the definition containing optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent rule set definition to return to after attaching
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Specifies the list of conditions that must be matched for the actions to be executed.
             *
             * @param conditions the list of conditions
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withConditions(List<DeliveryRuleCondition> conditions);

            /**
             * Specifies whether the rules engine should continue running the remaining rules
             * or stop after this rule is matched.
             *
             * @param matchProcessingBehavior the match processing behavior
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMatchProcessingBehavior(MatchProcessingBehavior matchProcessingBehavior);
        }

        /**
         * The final stage of a rule definition.
         *
         * @param <ParentT> the stage of the parent rule set definition to return to after attaching
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined rule to the parent rule set.
             *
             * @return the next stage of the parent definition
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of a rule definition.
     *
     * @param <ParentT> the stage of the parent rule set definition to return to after attaching
     */
    interface Definition<ParentT> extends DefinitionStages.Blank<ParentT>, DefinitionStages.WithOrder<ParentT>,
        DefinitionStages.WithActions<ParentT>, DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of rule definition stages that run as part of a {@link RuleSet.Update} flow.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a rule definition inside a rule set update.
         *
         * @param <ParentT> the stage of the parent rule set update to return to after attaching
         */
        interface Blank<ParentT> extends WithOrder<ParentT> {
        }

        /**
         * The stage of a rule update-definition requiring the order to be specified.
         *
         * @param <ParentT> the stage of the parent rule set update to return to after attaching
         */
        interface WithOrder<ParentT> {
            /**
             * Specifies the order in which the rule is applied for the endpoint.
             *
             * @param order the order value
             * @return the next stage of the definition
             */
            WithActions<ParentT> withOrder(int order);
        }

        /**
         * The stage of a rule update-definition requiring the actions to be specified.
         *
         * @param <ParentT> the stage of the parent rule set update to return to after attaching
         */
        interface WithActions<ParentT> {
            /**
             * Specifies the list of actions that are executed when all the conditions of a rule are satisfied.
             *
             * @param actions the list of actions
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withActions(List<DeliveryRuleAction> actions);
        }

        /**
         * The stage of the definition containing optional settings prior to attachment.
         *
         * @param <ParentT> the stage of the parent rule set update to return to after attaching
         */
        interface WithAttach<ParentT> extends Attachable<ParentT> {
            /**
             * Specifies the list of conditions that must be matched for the actions to be executed.
             *
             * @param conditions the list of conditions
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withConditions(List<DeliveryRuleCondition> conditions);

            /**
             * Specifies whether the rules engine should continue running the remaining rules
             * or stop after this rule is matched.
             *
             * @param matchProcessingBehavior the match processing behavior
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withMatchProcessingBehavior(MatchProcessingBehavior matchProcessingBehavior);
        }

        /**
         * The final stage of a rule definition inside a rule set update.
         *
         * @param <ParentT> the stage of the parent rule set update to return to after attaching
         */
        interface Attachable<ParentT> {
            /**
             * Attaches the defined rule to the parent rule set update.
             *
             * @return the next stage of the parent update
             */
            ParentT attach();
        }
    }

    /**
     * The entirety of a rule update inside a {@link RuleSet.Update} flow.
     */
    interface Update extends Settable<RuleSet.Update> {
        /**
         * Specifies the order in which the rule is applied for the endpoint.
         *
         * @param order the order value
         * @return the next stage of the update
         */
        Update withOrder(int order);

        /**
         * Specifies the list of actions that are executed when all the conditions of a rule are satisfied.
         *
         * @param actions the list of actions
         * @return the next stage of the update
         */
        Update withActions(List<DeliveryRuleAction> actions);

        /**
         * Specifies the list of conditions that must be matched for the actions to be executed.
         *
         * @param conditions the list of conditions
         * @return the next stage of the update
         */
        Update withConditions(List<DeliveryRuleCondition> conditions);

        /**
         * Specifies whether the rules engine should continue running the remaining rules
         * or stop after this rule is matched.
         *
         * @param matchProcessingBehavior the match processing behavior
         * @return the next stage of the update
         */
        Update withMatchProcessingBehavior(MatchProcessingBehavior matchProcessingBehavior);
    }
}
