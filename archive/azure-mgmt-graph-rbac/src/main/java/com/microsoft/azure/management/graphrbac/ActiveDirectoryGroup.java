/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.ADGroupInner;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import rx.Observable;

import java.util.Set;

/**
 * An immutable client-side representation of an Azure AD group.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta
public interface ActiveDirectoryGroup extends
        ActiveDirectoryObject,
        HasInner<ADGroupInner>,
        Updatable<ActiveDirectoryGroup.Update> {
    /**
     * @return security enabled field.
     */
    boolean securityEnabled();

    /**
     * @return mail field.
     */
    String mail();

    /**
     * Lists the members in the group.
     * @return an unmodifiable set of the members
     */
    @Beta(SinceVersion.V1_2_0)
    Set<ActiveDirectoryObject> listMembers();

    /**
     * Lists the members in the group.
     * @return an unmodifiable set of the members
     */
    @Beta(SinceVersion.V1_2_0)
    Observable<ActiveDirectoryObject> listMembersAsync();

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithEmailAlias,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the AD group definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the AD group definition.
         */
        interface Blank extends WithEmailAlias {
        }

        /**
         * An AD Group definition allowing mail nickname to be specified.
         */
        interface WithEmailAlias {
            @Beta(SinceVersion.V1_2_0)
            WithCreate withEmailAlias(String mailNickname);
        }

        /**
         * An AD Group definition allowing members to be added.
         */
        interface WithMember {
            /**
             * Add a member based on its object id. The member can be a user, a group, a service principal, or an application.
             * @param objectId the Active Directory object's id
             * @return the next AD Group definition stage
             */
            @Beta(SinceVersion.V1_2_0)
            WithCreate withMember(String objectId);

            /**
             * Adds a user as a member in the group.
             * @param user the Active Directory user to add
             * @return the next AD group definition stage
             */
            @Beta(SinceVersion.V1_2_0)
            WithCreate withMember(ActiveDirectoryUser user);

            /**
             * Adds a group as a member in the group.
             * @param group the Active Directory group to add
             * @return the next AD group definition stage
             */
            @Beta(SinceVersion.V1_2_0)
            WithCreate withMember(ActiveDirectoryGroup group);

            /**
             * Adds a service principal as a member in the group.
             * @param servicePrincipal the service principal to add
             * @return the next AD group definition stage
             */
            @Beta(SinceVersion.V1_2_0)
            WithCreate withMember(ServicePrincipal servicePrincipal);
        }

        /**
         * An AD group definition with sufficient inputs to create a new
         * group in the cloud, but exposing additional optional inputs to
         * specify.
         */
        @Beta(SinceVersion.V1_2_0)
        interface WithCreate extends
                Creatable<ActiveDirectoryGroup>,
                WithMember {
        }
    }

    /**
     * Grouping of all the AD group update stages.
     */
    interface UpdateStages {
        /**
         * An AD Group definition allowing members to be added or removed.
         */
        interface WithMember {
            /**
             * Adds a member based on its object id. The member can be a user, a group, a service principal, or an application.
             * @param objectId the Active Directory object's id
             * @return the next AD Group update stage
             */
            @Beta(SinceVersion.V1_2_0)
            Update withMember(String objectId);

            /**
             * Adds a user as a member in the group.
             * @param user the Active Directory user to add
             * @return the next AD group update stage
             */
            @Beta(SinceVersion.V1_2_0)
            Update withMember(ActiveDirectoryUser user);

            /**
             * Adds a group as a member in the group.
             * @param group the Active Directory group to add
             * @return the next AD group update stage
             */
            @Beta(SinceVersion.V1_2_0)
            Update withMember(ActiveDirectoryGroup group);

            /**
             * Adds a service principal as a member in the group.
             * @param servicePrincipal the service principal to add
             * @return the next AD group update stage
             */
            @Beta(SinceVersion.V1_2_0)
            Update withMember(ServicePrincipal servicePrincipal);

            /**
             * Removes a member based on its object id.
             * @param objectId the Active Directory object's id
             * @return the next AD Group update stage
             */
            @Beta(SinceVersion.V1_2_0)
            Update withoutMember(String objectId);

            /**
             * Removes a user as a member in the group.
             * @param user the Active Directory user to remove
             * @return the next AD group update stage
             */
            @Beta(SinceVersion.V1_2_0)
            Update withoutMember(ActiveDirectoryUser user);

            /**
             * Removes a group as a member in the group.
             * @param group the Active Directory group to remove
             * @return the next AD group update stage
             */
            @Beta(SinceVersion.V1_2_0)
            Update withoutMember(ActiveDirectoryGroup group);

            /**
             * Removes a service principal as a member in the group.
             * @param servicePrincipal the service principal to remove
             * @return the next AD group update stage
             */
            @Beta(SinceVersion.V1_2_0)
            Update withoutMember(ServicePrincipal servicePrincipal);
        }
    }

    /**
     * The template for a group update operation, containing all the settings that can be modified.
     */
    @Beta(SinceVersion.V1_2_0)
    interface Update extends
            Appliable<ActiveDirectoryGroup>,
            UpdateStages.WithMember {
    }
}
