// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.authorization.fluent.inner.RoleAssignmentInner;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/** An immutable client-side representation of an Azure AD role assignment. */
@Fluent
public interface RoleAssignment
    extends Indexable, HasInner<RoleAssignmentInner>, HasId, HasName, HasManager<AuthorizationManager> {
    /** @return the role assignment scope */
    String scope();

    /** @return the role definition ID */
    String roleDefinitionId();

    /** @return the principal ID */
    String principalId();

    /**************************************************************
     * Fluent interfaces to provision an role assignment
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithRole,
            DefinitionStages.WithScope,
            DefinitionStages.WithCreate {
    }

    /** Grouping of all the role assignment definition stages. */
    interface DefinitionStages {
        /** The first stage of the role assignment definition. */
        interface Blank extends WithAssignee {
        }

        /** The stage of role assignment definition allowing specifying the assignee information. */
        interface WithAssignee {
            /**
             * Specifies the assignee of the role assignment.
             *
             * @param objectId the object ID of an Active Directory identity
             * @return the next stage in role assignment definition
             */
            WithRole forObjectId(String objectId);

            /**
             * Specifies the assignee of the role assignment to be a user.
             *
             * @param user the user object
             * @return the next stage in role assignment definition
             */
            WithRole forUser(ActiveDirectoryUser user);

            /**
             * Specifies the assignee of the role assignment to be a user.
             *
             * @param name the user's user principal name, full display name, or email address
             * @return the next stage in role assignment definition
             */
            WithRole forUser(String name);

            /**
             * Specifies the assignee of the role assignment to be a group.
             *
             * @param activeDirectoryGroup the user group
             * @return the next stage in role assignment definition
             */
            WithRole forGroup(ActiveDirectoryGroup activeDirectoryGroup);

            /**
             * Specifies the assignee of the role assignment to be a service principal.
             *
             * @param servicePrincipal the service principal object
             * @return the next stage in role assignment definition
             */
            WithRole forServicePrincipal(ServicePrincipal servicePrincipal);

            /**
             * Specifies the assignee of the role assignment to be a service principal.
             *
             * @param servicePrincipalName the service principal name
             * @return the next stage in role assignment definition
             */
            WithRole forServicePrincipal(String servicePrincipalName);
        }

        /** The stage of role assignment definition allowing specifying the role. */
        interface WithRole {
            /**
             * Specifies the name of a built in role for this assignment.
             *
             * @param role the name of the role
             * @return the next stage in role assignment definition
             */
            WithScope withBuiltInRole(BuiltInRole role);
            /**
             * Specifies the ID of the custom role for this assignment.
             *
             * @param roleDefinitionId ID of the custom role definition
             * @return the next stage in role assignment definition
             */
            WithScope withRoleDefinition(String roleDefinitionId);
        }

        /** The stage of role assignment definition allowing specifying the scope of the assignment. */
        interface WithScope {
            /**
             * Specifies the scope of the role assignment. The scope is usually the ID of a subscription, a resource
             * group, a resource, etc.
             *
             * @param scope the scope of the assignment
             * @return the next stage in role assignment definition
             */
            WithCreate withScope(String scope);

            /**
             * Specifies the scope of the role assignment to be a resource group.
             *
             * @param resourceGroup the resource group the assignee is assigned to access
             * @return the next stage in role assignment definition
             */
            WithCreate withResourceGroupScope(ResourceGroup resourceGroup);

            /**
             * Specifies the scope of the role assignment to be a specific resource.
             *
             * @param resource the resource the assignee is assigned to access
             * @return the next stage in role assignment definition
             */
            WithCreate withResourceScope(Resource resource);

            /**
             * Specifies the scope of the role assignment to be an entire subscription.
             *
             * @param subscriptionId the subscription the assignee is assigned to access
             * @return the next stage in role assignment definition
             */
            WithCreate withSubscriptionScope(String subscriptionId);
        }

        /**
         * An role assignment definition with sufficient inputs to create a new role assignment in the cloud, but
         * exposing additional optional inputs to specify.
         */
        interface WithCreate extends Creatable<RoleAssignment> {
        }
    }
}
