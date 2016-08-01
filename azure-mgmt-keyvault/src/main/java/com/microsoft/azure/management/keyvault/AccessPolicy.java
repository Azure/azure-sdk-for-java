/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault;

import com.microsoft.azure.management.graphrbac.ServicePrincipal;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
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
    UUID tenantId();

    /**
     * @return The object ID of a user or service principal in the Azure Active
     * Directory tenant for the vault.
     */
    UUID objectId();

    /**
     * @return Application ID of the client making request on behalf of a principal.
     */
    UUID applicationId();

    /**
     * @return Permissions the identity has for keys and secrets.
     */
    Permissions permissions();

    /**************************************************************
     * Fluent interfaces to attach an access policy
     **************************************************************/

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

        interface WithIdentity<ParentT> {
            WithAttach<ParentT> forObjectId(UUID objectId);
            WithAttach<ParentT> forUser(User user);
            WithAttach<ParentT> forServicePrincipal(ServicePrincipal servicePrincipal);
        }

        /**
         * The access policy definition stage allowing permissions to be added.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPermissions<ParentT> {
            WithAttach<ParentT> allowKeyDecrypting();
            WithAttach<ParentT> allowKeyEncrypting();
            WithAttach<ParentT> allowKeyUnwrapping();
            WithAttach<ParentT> allowKeyWrapping();
            WithAttach<ParentT> allowKeyVerifying();
            WithAttach<ParentT> allowKeySigning();
            WithAttach<ParentT> allowKeyGetting();
            WithAttach<ParentT> allowKeyListing();
            WithAttach<ParentT> allowKeyUpdating();
            WithAttach<ParentT> allowKeyCreating();
            WithAttach<ParentT> allowKeyImporting();
            WithAttach<ParentT> allowKeyDeleting();
            WithAttach<ParentT> allowKeyBackingUp();
            WithAttach<ParentT> allowKeyRestoring();
            WithAttach<ParentT> allowKeyAllPermissions();
            WithAttach<ParentT> allowKeyPermission(String permission);
            WithAttach<ParentT> allowKeyPermissions(List<String> permissions);
            WithAttach<ParentT> disallowKeyDecrypting();
            WithAttach<ParentT> disallowKeyEncrypting();
            WithAttach<ParentT> disallowKeyUnwrapping();
            WithAttach<ParentT> disallowKeyWrapping();
            WithAttach<ParentT> disallowKeyVerifying();
            WithAttach<ParentT> disallowKeySigning();
            WithAttach<ParentT> disallowKeyGetting();
            WithAttach<ParentT> disallowKeyListing();
            WithAttach<ParentT> disallowKeyUpdating();
            WithAttach<ParentT> disallowKeyCreating();
            WithAttach<ParentT> disallowKeyImporting();
            WithAttach<ParentT> disallowKeyDeleting();
            WithAttach<ParentT> disallowKeyBackingUp();
            WithAttach<ParentT> disallowKeyRestoring();
            WithAttach<ParentT> disallowKeyAllPermissions();
            WithAttach<ParentT> disallowKeyPermission(String permission);
            WithAttach<ParentT> disallowKeyPermissions(List<String> permissions);
            WithAttach<ParentT> allowSecretGetting();
            WithAttach<ParentT> allowSecretListing();
            WithAttach<ParentT> allowSecretSetting();
            WithAttach<ParentT> allowSecretDeleting();
            WithAttach<ParentT> allowSecretAllPermissions();
            WithAttach<ParentT> allowSecretPermission(String permission);
            WithAttach<ParentT> allowSecretPermissions(List<String> permissions);
            WithAttach<ParentT> disallowSecretGetting();
            WithAttach<ParentT> disallowSecretListing();
            WithAttach<ParentT> disallowSecretSetting();
            WithAttach<ParentT> disallowSecretDeleting();
            WithAttach<ParentT> disallowSecretAllPermissions();
            WithAttach<ParentT> disallowSecretPermission(String permission);
            WithAttach<ParentT> disallowSecretPermissions(List<String> permissions);
        }

        /** The final stage of the access policy definition.
         * <p>
         * At this stage, more permissions can be added or application ID can be specified,
         * or the access policy definition can be attached to the parent key vault definition
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
    }

    /**
     * The template for a key vault update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}

