/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.UserInner;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of an Azure AD user.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
@Beta
public interface User extends
        Indexable,
        HasInner<UserInner> {

    /**
     * @return Gets or sets object Id.
     */
    String objectId();

    /**
     * @return Gets or sets object type.
     */
    String objectType();

    /**
     * @return Gets or sets user principal name.
     */
    String userPrincipalName();

    /**
     * @return Gets or sets user display name.
     */
    String displayName();

    /**
     * @return Gets or sets user signIn name.
     */
    String signInName();

    /**
     * @return Gets or sets user mail.
     */
    String mail();

    /**
     * @return The mail alias for the user.
     */
    String mailNickname();

    /**************************************************************
     * Fluent interfaces to provision a User
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithDisplayName,
            DefinitionStages.WithPassword,
            DefinitionStages.WithMailNickname,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the user definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the user definition.
         */
        interface Blank extends WithDisplayName {
        }

        /**
         * The stage of a user definition allowing display name to be set.
         */
        interface WithDisplayName {
            /**
             * Specifies the display name of the user.
             *
             * @param displayName the human-readable display name
             * @return the next stage of a user definition
             */
            WithPassword withDisplayName(String displayName);
        }

        /**
         * The stage of a user definition allowing password to be set.
         */
        interface WithPassword {
            /**
             * Specifies the password for the user.
             *
             * @param password the password
             * @return the next stage for a user definition
             */
            WithMailNickname withPassword(String password);

            /**
             * Specifies the temporary password for the user.
             *
             * @param password the temporary password
             * @param forceChangePasswordNextLogin if set to true, the user will have to change the password next time
             * @return the next stage for a user definition
             */
            WithMailNickname withPassword(String password, boolean forceChangePasswordNextLogin);
        }

        /**
         * The stage of a user definition allowing mail nickname to be specified.
         */
        interface WithMailNickname {
            /**
             * Specifies the mail nickname for the user.
             *
             * @param mailNickname the mail nickname
             * @return the next stage for a user definition
             */
            WithCreate withMailNickname(String mailNickname);
        }

        /**
         * The stage of a user definition allowing specifying if the account is enabled.
         */
        interface WithAccountEnabled {
            /**
             * Specifies if the user account is enabled upon creation.
             *
             * @param enabled if set to true, the user account is enabled
             * @return the next stage for a user definition
             */
            WithCreate withAccountEnabled(boolean enabled);
        }

        /**
         * An AD user definition with sufficient inputs to create a new
         * user in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<User>,
                WithAccountEnabled {
        }
    }

    /**
     * Grouping of all the user update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a user update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}
