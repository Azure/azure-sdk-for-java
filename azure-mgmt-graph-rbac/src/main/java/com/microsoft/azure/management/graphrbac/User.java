/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.graphrbac.implementation.UserInner;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an Azure tenant.
 */
public interface User extends
        Wrapper<UserInner> {

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
     * Fluent interfaces to provision a StorageAccount
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
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the storage account definition.
         */
        interface Blank extends WithDisplayName {
        }

        interface WithDisplayName {
            WithPassword withDisplayName(String displayName);
        }

        interface WithPassword {
            WithMailNickname withPassword(String password);
            WithMailNickname withPassword(String password, boolean forceChangePasswordNextLogin);
        }

        interface WithMailNickname {
            WithCreate withMailNickname(String mailNickname);
        }

        interface WithAccountEnabled {
            WithCreate withAccountEnabled(boolean enabled);
        }

        /**
         * A storage account definition with sufficient inputs to create a new
         * storage account in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<User>,
                WithAccountEnabled {
        }
    }

    /**
     * Grouping of all the storage account update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a storage account update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}
