/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault;

import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;
import java.util.UUID;

/**
 * An immutable client-side representation of a key vault access policy.
 */
public interface AccessPolicy extends
        ChildResource,
        Wrapper<AccessPolicyEntry> {
    /**
     * @return The Azure Active Directory tenant ID that should be used for
     * authenticating requests to the key vault.
     */
    String tenantId();

    /**
     * @return The object ID of a user or service principal in the Azure Active
     * Directory tenant for the vault.
     */
    String objectId();

    /**
     * @return Application ID of the client making request on behalf of a principal.
     */
    String applicationId();

    /**
     * @return Permissions the identity has for keys and secrets.
     */
    Permissions permissions();

    /**************************************************************
     * Fluent interfaces to attach an access policy
     **************************************************************/

    /**
     * The entirety of an access policy definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
            DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of access policy definition stages applicable as part of a key vault creation.
     */
    interface DefinitionStages {
        /**
         * The first stage of an access policy definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithIdentity<ParentT> {
        }

        /**
         * The access policy definition stage allowing the Active Directory identity to be specified.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithIdentity<ParentT> {
            /**
             * Specifies the object ID of the Active Directory identity this access policy is for.
             *
             * @param objectId the object ID of the AD identity
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forObjectId(UUID objectId);

            /**
             * Specifies the Active Directory user this access policy is for.
             *
             * @param user the AD user object
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forUser(User user);

            /**
             * Specifies the Active Directory user this access policy is for.
             *
             * @param userPrincipalName the user principal name of the AD user
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forUser(String userPrincipalName);

            /**
             * Specifies the Active Directory group this access policy is for.
             *
             * @param group the AD group object
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forGroup(ActiveDirectoryGroup group);

            /**
             * Specifies the Active Directory service principal this access policy is for.
             *
             * @param servicePrincipal the AD service principal object
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forServicePrincipal(ServicePrincipal servicePrincipal);

            /**
             * Specifies the Active Directory service principal this access policy is for.
             *
             * @param servicePrincipalName the service principal name of the AD user
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forServicePrincipal(String servicePrincipalName);
        }

        /**
         * The access policy definition stage allowing permissions to be added.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPermissions<ParentT> {
            /**
             * Allow all permissions for the AD identity to access keys.
             *
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowKeyAllPermissions();

            /**
             * Allow a list of permissions for the AD identity to access keys.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowKeyPermissions(KeyPermissions... permissions);

            /**
             * Allow a list of permissions for the AD identity to access keys.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowKeyPermissions(List<KeyPermissions> permissions);

            /**
             * Allow all permissions for the AD identity to access secrets.
             *
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowSecretAllPermissions();

            /**
             * Allow a list of permissions for the AD identity to access secrets.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowSecretPermissions(SecretPermissions... permissions);

            /**
             * Allow a list of permissions for the AD identity to access secrets.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowSecretPermissions(List<SecretPermissions> permissions);
        }

        /** The final stage of the access policy definition.
         * <p>
         * At this stage, more permissions can be added or application ID can be specified,
         * or the access policy definition can be attached to the parent key vault definition
         * using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InUpdate<ParentT>,
                WithPermissions<ParentT> {
        }
    }

    /**
     * The entirety of an access policy definition as part of a key vault update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of access policy definition stages applicable as part of a key vault update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an access policy definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithIdentity<ParentT> {
        }

        /**
         * The access policy definition stage allowing the Active Directory identity to be specified.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithIdentity<ParentT> {
            /**
             * Specifies the object ID of the Active Directory identity this access policy is for.
             *
             * @param objectId the object ID of the AD identity
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forObjectId(UUID objectId);

            /**
             * Specifies the Active Directory user this access policy is for.
             *
             * @param user the AD user object
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forUser(User user);

            /**
             * Specifies the Active Directory user this access policy is for.
             *
             * @param userPrincipalName the user principal name of the AD user
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forUser(String userPrincipalName);

            /**
             * Specifies the Active Directory group this access policy is for.
             *
             * @param group the AD group object
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forGroup(ActiveDirectoryGroup group);

            /**
             * Specifies the Active Directory service principal this access policy is for.
             *
             * @param servicePrincipal the AD service principal object
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forServicePrincipal(ServicePrincipal servicePrincipal);

            /**
             * Specifies the Active Directory service principal this access policy is for.
             *
             * @param servicePrincipalName the service principal name of the AD user
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> forServicePrincipal(String servicePrincipalName);
        }

        /**
         * The access policy definition stage allowing permissions to be added.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPermissions<ParentT> {
            /**
             * Allow all permissions for the AD identity to access keys.
             *
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowKeyAllPermissions();

            /**
             * Allow a list of permissions for the AD identity to access keys.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowKeyPermissions(KeyPermissions... permissions);

            /**
             * Allow a list of permissions for the AD identity to access keys.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowKeyPermissions(List<KeyPermissions> permissions);

            /**
             * Allow all permissions for the AD identity to access secrets.
             *
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowSecretAllPermissions();

            /**
             * Allow a list of permissions for the AD identity to access secrets.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowSecretPermissions(SecretPermissions... permissions);

            /**
             * Allow a list of permissions for the AD identity to access secrets.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            WithAttach<ParentT> allowSecretPermissions(List<SecretPermissions> permissions);
        }

        /** The final stage of the access policy definition.
         * <p>
         * At this stage, more permissions can be added or application ID can be specified,
         * or the access policy definition can be attached to the parent key vault update
         * using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                WithPermissions<ParentT> {
        }
    }

    /**
     * Grouping of all the key vault update stages.
     */
    interface UpdateStages {
        /**
         * The access policy update stage allowing permissions to be added or removed.
         */
        interface WithPermissions {
            /**
             * Allow all permissions for the AD identity to access keys.
             *
             * @return the next stage of access policy update
             */
            Update allowKeyAllPermissions();

            /**
             * Allow a list of permissions for the AD identity to access keys.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy update
             */
            Update allowKeyPermissions(KeyPermissions... permissions);

            /**
             * Allow a list of permissions for the AD identity to access keys.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy update
             */
            Update allowKeyPermissions(List<KeyPermissions> permissions);

            /**
             * Revoke all permissions for the AD identity to access keys.
             *
             * @return the next stage of access policy update
             */
            Update disallowKeyAllPermissions();

            /**
             * Revoke a list of permissions for the AD identity to access keys.
             *
             * @param permissions the list of permissions to revoke
             * @return the next stage of access policy update
             */
            Update disallowKeyPermissions(KeyPermissions... permissions);

            /**
             * Revoke a list of permissions for the AD identity to access keys.
             *
             * @param permissions the list of permissions to revoke
             * @return the next stage of access policy update
             */
            Update disallowKeyPermissions(List<KeyPermissions> permissions);

            /**
             * Allow all permissions for the AD identity to access secrets.
             *
             * @return the next stage of access policy definition
             */
            Update allowSecretAllPermissions();

            /**
             * Allow a list of permissions for the AD identity to access secrets.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            Update allowSecretPermissions(SecretPermissions... permissions);

            /**
             * Allow a list of permissions for the AD identity to access secrets.
             *
             * @param permissions the list of permissions allowed
             * @return the next stage of access policy definition
             */
            Update allowSecretPermissions(List<SecretPermissions> permissions);

            /**
             * Revoke all permissions for the AD identity to access secrets.
             *
             * @return the next stage of access policy update
             */
            Update disallowSecretAllPermissions();

            /**
             * Revoke a list of permissions for the AD identity to access secrets.
             *
             * @param permissions the list of permissions to revoke
             * @return the next stage of access policy update
             */
            Update disallowSecretPermissions(SecretPermissions... permissions);

            /**
             * Revoke a list of permissions for the AD identity to access secrets.
             *
             * @param permissions the list of permissions to revoke
             * @return the next stage of access policy update
             */
            Update disallowSecretPermissions(List<SecretPermissions> permissions);
        }
    }

    /**
     * The entirety of an access policy update as part of a key vault update.
     */
    interface Update extends
            UpdateStages.WithPermissions,
            Settable<Vault.Update> {
    }
}

