// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluent.inner.PolicyAssignmentInner;

/**
 * An immutable client-side representation of an Azure policy assignment.
 */
@Fluent
public interface PolicyAssignment extends
        HasName,
        HasId,
        Indexable,
        Refreshable<PolicyAssignment>,
        HasInner<PolicyAssignmentInner> {

    /**
     * @return the policy assignment display name
     */
    String displayName();

    /**
     * @return the policy definition Id
     */
    String policyDefinitionId();

    /**
     * @return the scope at which the policy assignment exists
     */
    String scope();

    /**
     * @return the type of the policy assignment
     */
    String type();

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            PolicyAssignment.DefinitionStages.Blank,
            PolicyAssignment.DefinitionStages.WithPolicyDefinition,
            PolicyAssignment.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the policy assignment definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the policy assignment.
         */
        interface Blank extends PolicyAssignment.DefinitionStages.WithScope {
        }

        /**
         * A policy assignment specifying the scope of the policy.
         */
        interface WithScope {
            /**
             * Specifies the scope of the policy assignment.
             *
             * @param scope the scope of the policy assignment
             * @return the next stage of policy assignment
             */
            WithPolicyDefinition forScope(String scope);

            /**
             * Specifies the scope of the policy assignment to be a resource group.
             *
             * @param resourceGroup the resource group to assign the policy
             * @return the next stage of policy assignment
             */
            WithPolicyDefinition forResourceGroup(ResourceGroup resourceGroup);

            /**
             * Specifies the scope of the policy assignment to be a resource.
             *
             * @param genericResource the resource to assign the policy
             * @return the next stage of policy assignment
             */
            WithPolicyDefinition forResource(GenericResource genericResource);
        }

        /**
         * A policy assignment allowing the policy definition to be set.
         */
        interface WithPolicyDefinition {
            /**
             * Specifies the policy assignment.
             *
             * @param policyDefinitionId the ID of the policy assignment
             * @return the next stage of policy assignment
             */
            WithCreate withPolicyDefinitionId(String policyDefinitionId);

            /**
             * Specifies the policy definition.
             *
             * @param policyDefinition the policy definition
             * @return the next stage of policy assignment
             */
            WithCreate withPolicyDefinition(PolicyDefinition policyDefinition);
        }

        /**
         * A policy assignment allowing the display name to be set.
         */
        interface WithDisplayName {
            /**
             * Specifies the display name of the policy assignment.
             *
             * @param displayName the display name of the policy assignment
             * @return the next stage of policy assignment
             */
            WithCreate withDisplayName(String displayName);
        }

        /**
         * A policy assignment with sufficient inputs to create a new policy
         * assignment in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<PolicyAssignment>,
                DefinitionStages.WithDisplayName {
        }
    }
}
