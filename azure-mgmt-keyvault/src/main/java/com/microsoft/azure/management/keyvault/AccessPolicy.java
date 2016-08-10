/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault;

import com.microsoft.azure.management.graphrbac.Group;
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
            WithAttach<ParentT> forUser(String userPrincipalName);
            WithAttach<ParentT> forGroup(Group group);
            WithAttach<ParentT> forServicePrincipal(ServicePrincipal servicePrincipal);
            WithAttach<ParentT> forServicePrincipal(String servicePrincipalName);
        }

        /**
         * The access policy definition stage allowing permissions to be added.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPermissions<ParentT> {
            WithAttach<ParentT> allowKeyAllPermissions();
            WithAttach<ParentT> allowKeyPermissions(KeyPermissions... permissions);
            WithAttach<ParentT> allowKeyPermissions(List<KeyPermissions> permissions);
            WithAttach<ParentT> allowSecretAllPermissions();
            WithAttach<ParentT> allowSecretPermissions(SecretPermissions... permissions);
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

    interface UpdateDefinition<ParentT> extends
            UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of access policy definition stages applicable as part of a key vault creation.
     */
    interface UpdateDefinitionStages {
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
            WithAttach<ParentT> forUser(String userPrincipalName);
            WithAttach<ParentT> forGroup(Group group);
            WithAttach<ParentT> forServicePrincipal(ServicePrincipal servicePrincipal);
            WithAttach<ParentT> forServicePrincipal(String servicePrincipalName);
        }

        /**
         * The access policy definition stage allowing permissions to be added.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPermissions<ParentT> {
            WithAttach<ParentT> allowKeyAllPermissions();
            WithAttach<ParentT> allowKeyPermissions(KeyPermissions... permissions);
            WithAttach<ParentT> allowKeyPermissions(List<KeyPermissions> permissions);
            WithAttach<ParentT> allowSecretAllPermissions();
            WithAttach<ParentT> allowSecretPermissions(SecretPermissions... permissions);
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
                Attachable.InDefinition<ParentT>,
                WithPermissions<ParentT> {
        }
    }

    /**
     * Grouping of all the key vault update stages.
     */
    interface UpdateStages {
        /**
         * The access policy definition stage allowing permissions to be added.
         */
        interface WithPermissions {
            Update allowKeyAllPermissions();
            Update allowKeyPermissions(KeyPermissions... permissions);
            Update allowKeyPermissions(List<KeyPermissions> permissions);
            Update disallowKeyAllPermissions();
            Update disallowKeyPermissions(KeyPermissions... permissions);
            Update disallowKeyPermissions(List<KeyPermissions> permissions);
            Update allowSecretAllPermissions();
            Update allowSecretPermissions(SecretPermissions... permissions);
            Update allowSecretPermissions(List<SecretPermissions> permissions);
            Update disallowSecretAllPermissions();
            Update disallowSecretPermissions(SecretPermissions... permissions);
            Update disallowSecretPermissions(List<SecretPermissions> permissions);

        }
    }

    /**
     * The template for a key vault update operation, containing all the settings that can be modified.
     */
    interface Update extends
            UpdateStages.WithPermissions,
            Settable<Vault.Update>{
    }
}

