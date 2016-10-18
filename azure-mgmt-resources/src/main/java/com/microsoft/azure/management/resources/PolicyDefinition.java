/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.PolicyDefinitionInner;
import com.microsoft.azure.management.resources.implementation.TenantIdDescriptionInner;

/**
 * An immutable client-side representation of an Azure policy.
 */
@Fluent
public interface PolicyDefinition extends
        HasName,
        Refreshable<PolicyDefinition>,
        Updatable<PolicyDefinition.Update>,
        Wrapper<PolicyDefinitionInner> {

    /**
     * Gets or sets policy definition policy type. Possible values include:
     * 'NotSpecified', 'BuiltIn', 'Custom'.
     */
    PolicyType policyType();

    /**
     * Gets or sets the policy definition display name.
     */
    String displayName();

    /**
     * Gets or sets the policy definition description.
     */
    String description();

    /**
     * Gets or sets the policy rule.
     */
    Object policyRule();

    /**
     * Gets or sets the Id of the policy definition.
     */
    String id();

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
         * A policy definition allowing the sku to be set.
         */
        interface WithPolicyRule {
            /**
             * Specifies the rule of the policy
             *
             * @param policyRule the rule object
             * @return the next stage of policy definition
             */
            WithCreate withPolicyRule(Object policyRule);
        }

        /**
         * A policy definition specifying the account kind to be blob.
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
         * A policy definition selecting the general purpose account kind.
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
         * A policy definition selecting the general purpose account kind.
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
         * A policy definition allowing the sku to be set.
         */
        interface WithPolicyRule {
            /**
             * Specifies the rule of the policy
             *
             * @param policyRule the rule object
             * @return the next stage of policy update
             */
            Update withPolicyRule(Object policyRule);
        }

        /**
         * A policy definition specifying the account kind to be blob.
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
         * A policy definition selecting the general purpose account kind.
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
         * A policy definition selecting the general purpose account kind.
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
