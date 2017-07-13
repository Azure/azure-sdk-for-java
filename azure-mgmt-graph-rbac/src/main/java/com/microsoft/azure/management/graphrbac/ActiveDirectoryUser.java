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
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of an Azure AD user.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta
public interface ActiveDirectoryUser extends
        ActiveDirectoryObject,
        HasInner<UserInner> {
    /**
     * @return Gets or sets user principal name.
     */
    String userPrincipalName();

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
     * Fluent interfaces to provision a user
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithUserPrincipalName,
            DefinitionStages.WithPassword,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the user definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the user definition.
         */
        interface Blank extends WithUserPrincipalName {
        }

        /**
         * A user definition allowing user principal name to be specified.
         */
        interface WithUserPrincipalName {
            WithPassword withUserPrincipalName(String userPrincipalName);

            WithPassword withEmailAlias(String emailAlias);
        }

        /**
         * A user definition allowing password to be specified.
         */
        interface WithPassword {
            WithCreate withPassword(String password);
        }

        interface WithMailNickname {
            WithCreate withMailNickname(String mailNickname);
        }

        interface WithPromptToChangePasswordOnLogin {
            WithCreate withPromptToChangePasswordOnLogin(boolean promptToChangePasswordOnLogin);
        }

        /**
         * An AD user definition with sufficient inputs to create a new
         * user in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<ActiveDirectoryUser>,
                DefinitionStages.WithMailNickname,
                DefinitionStages.WithPromptToChangePasswordOnLogin {
        }
    }
}
