// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
     * Grouping of CDN Standard rules engine rule definition stages as a part of parent CDN endpoint definition.
     */
    interface DefinitionStage {
        /**
         * The first stage of a CDN Standard rules engine rule definition.
         *
         * @param <T> the stage of the parent CDN endpoint definition to return to after attaching this definition
         */
        interface Blank<T> extends WithOrder<T> {
        }

        /**
         * The stage of a CDN Standard rules engine rule definition allowing to specify the order of the rule.
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
             * @param order the order of the Standard rules engine rule
             * @return the next stage of the Standard rules engine rule definition
             */
            WithMatchConditionsOrActions<T> withOrder(int order);
        }

        /**
         * The stage of a CDN Standard rules engine rule definition allowing to specify match conditions or actions.
         * For Global rule(order=0), only actions can be specified.
         * For the rest(order>0), both match conditions and actions must both be specified.
         *
         * @param <T> the stage of the parent CDN endpoint definition to return to after attaching this definition
         */
        interface WithMatchConditionsOrActions<T> extends WithMatchConditions<T>, WithActions<T> {
        }

        /**
         * The stage of a CDN Standard rules engine rule definition allowing to specify match conditions.
         *
         * @param <T> the stage of the parent CDN endpoint definition to return to after attaching this definition
         */
        interface WithMatchConditions<T> {
            /**
             * Specify a list of conditions that must be matched for the actions to be executed.
             * Must be left blank for Global rule(order=0). Required for the rest(order>0).
             *
             * @param matchConditions the conditions that must be matched for the actions to be executed
             * @return the next stage of the Standard rules engine rule definition
             */
            WithActions<T> withMatchConditions(DeliveryRuleCondition... matchConditions);
        }

        /**
         * The stage of a CDN Standard rules engine rule definition allowing to specify actions.
         *
         * @param <T> the stage of the parent CDN endpoint definition to return to after attaching this definition
         */
        interface WithActions<T> {
            /**
             * Specify a list of actions that are executed when all the conditions of a rule are satisfied.
             *
             * @param actions the actions that are executed when all the conditions of a rule are satisfied
             * @return the next stage of the Standard rules engine rule definition
             */
            Attachable<T> withActions(DeliveryRuleAction... actions);
        }
    }

    /**
     * Grouping of CDN Standard rules engine rule update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a CDN Standard rules engine rule update allowing to update the order of the rule in the Rules Engine.
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
             * @param order the order of the Standard rules engine rule
             * @return the next stage of the Standard rules engine rule update
             */
            Update<T> withOrder(int order);
        }

        /**
         * The stage of a CDN Standard rules engine rule update allowing to update match conditions.
         *
         * @param <T> the stage of the parent CDN endpoint update to return to after updating this definition
         */
        interface WithMatchConditions<T> {
            /**
             * Specify a list of conditions that must be matched for the actions to be executed.
             * Must be left blank for Global rule(order=0). Required for the rest(order>0).
             *
             * @param matchConditions the conditions that must be matched for the actions to be executed
             * @return the next stage of the Standard rules engine rule update
             */
            Update<T> withMatchConditions(DeliveryRuleCondition... matchConditions);
        }

        /**
         * The stage of a CDN Standard rules engine rule update allowing to update actions.
         *
         * @param <T> the stage of the parent CDN endpoint update to return to after updating this definition
         */
        interface WithActions<T> {
            /**
             * Specify a list of actions that are executed when all the conditions of a rule are satisfied.
             *
             * @param actions the actions that are executed when all the conditions of a rule are satisfied
             * @return the next stage of the Standard rules engine rule update
             */
            Update<T> withActions(DeliveryRuleAction... actions);
        }
    }

    /**
     * The entirety of a CDN Standard rules engine rule definition.
     *
     * @param <ParentT> the stage of the parent CDN endpoint definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends DefinitionStage.Blank<ParentT>,
        DefinitionStage.WithMatchConditionsOrActions<ParentT>, Attachable<ParentT> {
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     *
     * @param <ParentT> the stage of the parent CDN endpoint update to return to after updating this definition
     */
    interface Update<ParentT> extends Settable<ParentT>, UpdateStages.WithOrder<ParentT>,
        UpdateStages.WithMatchConditions<ParentT>, UpdateStages.WithActions<ParentT> {
    }
}
