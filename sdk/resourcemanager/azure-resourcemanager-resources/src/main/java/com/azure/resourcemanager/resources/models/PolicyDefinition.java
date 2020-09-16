// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluent.inner.PolicyDefinitionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of an Azure policy.
 */
@Fluent
public interface PolicyDefinition extends
        HasName,
        HasId,
        Indexable,
        Refreshable<PolicyDefinition>,
        Updatable<PolicyDefinition.Update>,
        HasInner<PolicyDefinitionInner> {

    /**
     * @return the type of the policy definition
     */
    PolicyType policyType();

    /**
     * @return the policy definition display name
     */
    String displayName();

    /**
     * @return the policy definition description
     */
    String description();

    /**
     * @return the policy rule
     */
    Object policyRule();

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the policy definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the policy definition.
         */
        interface Blank extends WithPolicyRule {
        }

        /**
         * A policy definition allowing the policy rule to be set.
         */
        interface WithPolicyRule {
            /**
             * Specifies the rule of the policy.
             *
             * @param policyRule the rule object
             * @return the next stage of policy definition
             */
            WithCreate withPolicyRule(Object policyRule);

            /**
             * Specifies the rule of the policy in JSON format.
             *
             * @param policyRuleJson the rule object
             * @return the next stage of policy definition
             */
            WithCreate withPolicyRuleJson(String policyRuleJson);
        }

        /**
         * A policy definition specifying the policy type to be blob.
         */
        interface WithPolicyType {
            /**
             * Specifies the type of the policy. The default value is 'NotSpecified'.
             *
             * @param policyType the policy type enum
             * @return the next stage of policy definition
             */
            WithCreate withPolicyType(PolicyType policyType);
        }

        /**
         * A policy definition allowing the display name to be set.
         */
        interface WithDisplayName {
            /**
             * Specifies the display name of the policy.
             *
             * @param displayName the display name of the policy
             * @return the next stage of policy definition
             */
            WithCreate withDisplayName(String displayName);
        }

        /**
         * A policy definition allowing policy description to be set.
         */
        interface WithDescription {
            /**
             * Specifies the description of the policy.
             *
             * @param description the description of the policy
             * @return the next stage of policy definition
             */
            WithCreate withDescription(String description);
        }

        /**
         * A policy definition with sufficient inputs to create a new
         * policy in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<PolicyDefinition>,
                DefinitionStages.WithDescription,
                DefinitionStages.WithDisplayName,
                DefinitionStages.WithPolicyType {
        }
    }

    /**
     * Grouping of all the policy update stages.
     */
    interface UpdateStages {
        /**
         * A policy definition allowing the policy rule to be set.
         */
        interface WithPolicyRule {
            /**
             * Specifies the rule of the policy.
             *
             * @param policyRule the rule object
             * @return the next stage of policy update
             */
            Update withPolicyRule(Object policyRule);
        }

        /**
         * A policy definition allowing the policy type to be set.
         */
        interface WithPolicyType {
            /**
             * Specifies the type of the policy. The default value is 'NotSpecified'.
             *
             * @param policyType the policy type enum
             * @return the next stage of policy update
             */
            Update withPolicyType(PolicyType policyType);
        }

        /**
         * A policy definition allowing the display name to be set.
         */
        interface WithDisplayName {
            /**
             * Specifies the display name of the policy.
             *
             * @param displayName the display name of the policy
             * @return the next stage of policy update
             */
            Update withDisplayName(String displayName);
        }

        /**
         * A policy definition allowing the policy description to be set.
         */
        interface WithDescription {
            /**
             * Specifies the description of the policy.
             *
             * @param description the description of the policy
             * @return the next stage of policy update
             */
            Update withDescription(String description);
        }
    }

    /**
     * The template for a policy update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<PolicyDefinition>,
            UpdateStages.WithDescription,
            UpdateStages.WithDisplayName,
            UpdateStages.WithPolicyRule,
            UpdateStages.WithPolicyType {
    }
}
