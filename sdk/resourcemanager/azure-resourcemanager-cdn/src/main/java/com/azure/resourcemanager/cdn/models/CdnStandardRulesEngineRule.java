package com.azure.resourcemanager.cdn.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/**
 * An immutable client-side representation of an Azure CDN Standard rules engine rule.
 */
@Fluent
public interface CdnStandardRulesEngineRule extends ChildResource<CdnEndpoint> {
    /**
     * Grouping of CDN delivery rule definition stages as a part of parent CDN endpoint definition.
     */
    interface DefinitionStage {
        /**
         * The first stage of a CDN delivery rule definition.
         *
         * @param <T> the stage of the parent CDN endpoint definition to return to after attaching this definition
         */
        interface Blank<T> extends WithOrder<T> {}

        /**
         * The stage of a CDN delivery rule definition allowing to specify the order of the rule in the Rules Engine.
         *
         * @param <T> the stage of the parent CDN endpoint definition to return to after attaching this definition
         */
        interface WithOrder<T> {
            /**
             * Specify the order in which the rule are applied for the endpoint. Possible values {0,1,2,3,………}.
             * A rule with a lesser order will be applied before a rule with a greater order.
             * Rule with order 0 is a special rule. It does not require any condition and actions listed in it will
             * always be applied.
             *
             * @param order the order of the delivery rule
             * @return the next stage of the delivery rule definition
             */
            WithMatchConditions<T> withOrder(int order);
        }

        /**
         * The stage of a CDN delivery rule definition allowing to specify match conditions.
         *
         * @param <T> the stage of the parent CDN endpoint definition to return to after attaching this definition
         */
        interface WithMatchConditions<T> {
            /**
             * Specify a list of conditions that must be matched for the actions to be executed.
             *
             * @param matchConditions the conditions that must be matched for the actions to be executed
             * @return the next stage of the delivery rule definition
             */
            WithActions<T> withMatchConditions(DeliveryRuleCondition... matchConditions);
        }

        /**
         * The stage of a CDN delivery rule definition allowing to specify actions.
         *
         * @param <T> the stage of the parent CDN endpoint definition to return to after attaching this definition
         */
        interface WithActions<T> {
            /**
             * Specify a list of actions that are executed when all the conditions of a rule are satisfied.
             *
             * @param actions the actions that are executed when all the conditions of a rule are satisfied
             * @return the next stage of the delivery rule definition
             */
            Attachable<T> withActions(DeliveryRuleAction... actions);
        }
    }

    /**
     * Grouping of CDN delivery rule update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a CDN delivery rule update allowing to update the order of the rule in the Rules Engine.
         *
         * @param <T> the stage of the parent CDN endpoint update to return to after updating this definition
         */
        interface WithOrder<T> {
            /**
             * Specify the order in which the rule are applied for the endpoint. Possible values {0,1,2,3,………}.
             * A rule with a lesser order will be applied before a rule with a greater order.
             * Rule with order 0 is a special rule. It does not require any condition and actions listed in it will
             * always be applied.
             *
             * @param order the order of the delivery rule
             * @return the next stage of the delivery rule update
             */
            Update<T> withOrder(int order);
        }

        /**
         * The stage of a CDN delivery rule update allowing to update match conditions.
         *
         * @param <T> the stage of the parent CDN endpoint update to return to after updating this definition
         */
        interface WithMatchConditions<T> {
            /**
             * Specify a list of conditions that must be matched for the actions to be executed.
             *
             * @param matchConditions the conditions that must be matched for the actions to be executed
             * @return the next stage of the delivery rule update
             */
            Update<T> withMatchConditions(DeliveryRuleCondition... matchConditions);
        }

        /**
         * The stage of a CDN delivery rule update allowing to update actions.
         *
         * @param <T> the stage of the parent CDN endpoint update to return to after updating this definition
         */
        interface WithActions<T> {
            /**
             * Specify a list of actions that are executed when all the conditions of a rule are satisfied.
             *
             * @param actions the actions that are executed when all the conditions of a rule are satisfied
             * @return the next stage of the delivery rule update
             */
            Update<T> withActions(DeliveryRuleAction... actions);
        }
    }

    /**
     * The entirety of a CDN delivery rule definition.
     *
     * @param <ParentT> the stage of the parent CDN endpoint definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStage.Blank<ParentT>,
        DefinitionStage.WithMatchConditions<ParentT>,
        DefinitionStage.WithActions<ParentT>,
        Attachable<ParentT> {
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     *
     * @param <ParentT> the stage of the parent CDN endpoint update to return to after updating this definition
     */
    interface Update<ParentT>
        extends Settable<ParentT>,
        UpdateStages.WithOrder<ParentT>,
        UpdateStages.WithMatchConditions<ParentT>,
        UpdateStages.WithActions<ParentT> {
    }
}
