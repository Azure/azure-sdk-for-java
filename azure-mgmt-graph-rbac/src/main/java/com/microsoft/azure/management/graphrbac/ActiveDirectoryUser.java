/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.graphrbac.implementation.UserInner;
import com.microsoft.azure.management.resources.fluentcore.arm.CountryIsoCode;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An immutable client-side representation of an Azure AD user.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
@Beta
public interface ActiveDirectoryUser extends
        ActiveDirectoryObject,
        HasInner<UserInner>,
        Updatable<ActiveDirectoryUser.Update> {
    /**
     * @return user principal name
     */
    String userPrincipalName();

    /**
     * @return user signIn name
     */
    String signInName();

    /**
     * @return user mail
     */
    String mail();

    /**
     * @return the mail alias for the user
     */
    String mailNickname();

    /**
     * @return the usage location of the user
     */
    @Beta(SinceVersion.V1_2_0)
    CountryIsoCode usageLocation();

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
            /**
             * Specifies the user principal name of the user. It must contain one of
             * the verified domains for the tenant.
             * @param userPrincipalName the user principal name
             * @return the next stage of user definition
             */
            @Beta(SinceVersion.V1_2_0)
            WithPassword withUserPrincipalName(String userPrincipalName);

            /**
             * Specifies the email alias of the new user.
             * @param emailAlias the email alias of the new user
             * @return the next stage of user definition
             */
            @Beta(SinceVersion.V1_2_0)
            WithPassword withEmailAlias(String emailAlias);
        }

        /**
         * A user definition allowing password to be specified.
         */
        interface WithPassword {
            /**
             * Specifies the password of the user.
             * @param password the password of the user
             * @return the next stage of user definition
             */
            @Beta(SinceVersion.V1_2_0)
            WithCreate withPassword(String password);
        }

        /**
         * A user definition allowing setting whether the user should change password on the next login.
         */
        interface WithPromptToChangePasswordOnLogin {
            /**
             * Specifies whether the user should change password on the next login.
             * @param promptToChangePasswordOnLogin true if the user should change password on next login.
             * @return the next stage of user definition
             */
            @Beta(SinceVersion.V1_2_0)
            WithCreate withPromptToChangePasswordOnLogin(boolean promptToChangePasswordOnLogin);
        }

        /**
         * A user definition allowing specifying whether the account is enabled.
         */
        interface WithAccontEnabled {
            /**
             * Specifies whether the user account is enabled.
             * @param accountEnabled true if account is enabled, false otherwise
             * @return the next stage of user definition
             */
            @Beta(SinceVersion.V1_2_0)
            WithCreate withAccountEnabled(boolean accountEnabled);
        }

        /**
         * A user definition allowing usage location to be specified.
         */
        interface WithUsageLocation {
            /**
             * Specifies the usage location for the user. Required for users that
             * will be assigned licenses due to legal requirement to check for
             * availability of services in countries.
             * @param usageLocation A two letter country code (ISO standard 3166).
             * @return The next stage of user definition
             */
            @Beta(SinceVersion.V1_2_0)
            WithCreate withUsageLocation(CountryIsoCode usageLocation);
        }

        /**
         * An AD user definition with sufficient inputs to create a new
         * user in the cloud, but exposing additional optional inputs to
         * specify.
         */
        @Beta(SinceVersion.V1_2_0)
        interface WithCreate extends
                Creatable<ActiveDirectoryUser>,
                DefinitionStages.WithAccontEnabled,
                DefinitionStages.WithPromptToChangePasswordOnLogin,
                DefinitionStages.WithUsageLocation {
        }
    }

    /**
     * Group of all the user update stages.
     */
    interface UpdateStages {
        /**
         * A user update allowing password to be specified.
         */
        interface WithPassword {
            /**
             * Specifies the password of the user.
             * @param password the password of the user
             * @return the next stage of user update
             */
            @Beta(SinceVersion.V1_2_0)
            Update withPassword(String password);
        }

        /**
         * A user update allowing setting whether the user should change password on the next login.
         */
        interface WithPromptToChangePasswordOnLogin {
            /**
             * Specifies whether the user should change password on the next login.
             * @param promptToChangePasswordOnLogin true if the user should change password on next login.
             * @return the next stage of user update
             */
            @Beta(SinceVersion.V1_2_0)
            Update withPromptToChangePasswordOnLogin(boolean promptToChangePasswordOnLogin);
        }

        /**
         * A user update allowing specifying whether the account is enabled.
         */
        interface WithAccontEnabled {
            /**
             * Specifies whether the user account is enabled.
             * @param accountEnabled true if account is enabled, false otherwise
             * @return the next stage of user update
             */
            @Beta(SinceVersion.V1_2_0)
            Update withAccountEnabled(boolean accountEnabled);
        }

        /**
         * A user update allowing usage location to be specified.
         */
        interface WithUsageLocation {
            /**
             * Specifies the usage location for the user. Required for users that
             * will be assigned licenses due to legal requirement to check for
             * availability of services in countries.
             * @param usageLocation A two letter country code (ISO standard 3166).
             * @return The next stage of user update
             */
            @Beta(SinceVersion.V1_2_0)
            Update withUsageLocation(CountryIsoCode usageLocation);
        }
    }

    /**
     * The template for a user update operation, containing all the settings that can be modified.
     */
    @Beta(SinceVersion.V1_2_0)
    interface Update extends
            Appliable<ActiveDirectoryUser>,
            UpdateStages.WithAccontEnabled,
            UpdateStages.WithPassword,
            UpdateStages.WithPromptToChangePasswordOnLogin,
            UpdateStages.WithUsageLocation {
    }
}
