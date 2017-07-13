/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
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
        Updatable<ActiveDirectoryGroup.Update>{
    /**
     * @return security enabled field.
     */
    boolean securityEnabled();

    /**
     * @return mail field.
     */
    String mail();

    Set<ActiveDirectoryObject> listMembers();

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
            WithCreate withMember(String objectId);
        }

        /**
         * An AD group definition with sufficient inputs to create a new
         * group in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<ActiveDirectoryGroup>,
                WithMember {
        }
    }

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
            Update withMember(String objectId);

            /**
             * Removes a member based on its object id.
             * @param objectId the Active Directory object's id
             * @return the next AD Group update stage
             */
            Update withoutMember(String objectId);
        }
    }

    interface Update extends
            Appliable<ActiveDirectoryGroup>,
            UpdateStages.WithMember {
    }
}
