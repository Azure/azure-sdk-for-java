// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.models.DiskEncryptionSetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/** An immutable client-side representation of an Azure disk encryption set. */
@Fluent
public interface DiskEncryptionSet
    extends GroupableResource<ComputeManager, DiskEncryptionSetInner>,
        Updatable<DiskEncryptionSet.Update>,
        Refreshable<DiskEncryptionSet> {
    /** @return resource id of the Azure key vault containing the key or secret */
    String keyVaultId();

    /** @return id representing the encryption key in KeyVault */
    String encryptionKeyId();

    /** @return the System Assigned (Local) Managed Service Identity specific Active Directory service principal ID
     *          assigned to the disk encryption set.
     */
    String systemAssignedManagedServiceIdentityPrincipalId();

    /**
     * If automatic key rotation is enabled, the system will automatically update all managed disks, snapshots,
     * and images referencing the disk encryption set to use the new version of the key within one hour.
     * @return whether automatic key rotation is enabled
     */
    Boolean isAutomaticKeyRotationEnabled();

    /** @return the type of key used to encrypt the data of the disk */
    DiskEncryptionSetType encryptionType();

    /** The entirety of the disk encryption set definition. */
    interface Definition
        extends DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithEncryptionType,
        DefinitionStages.WithKeyVault,
        DefinitionStages.WithKeyVaultKey,
        DefinitionStages.WithSystemAssignedManagedServiceIdentity,
        DefinitionStages.WithSystemAssignedIdentityBasedAccessOrCreate,
        DefinitionStages.WithCreate { }

    /** Grouping of disk encryption set definition stages */
    interface DefinitionStages {
        /** The first stage of a disk encryption set definition. */
        interface Blank extends DefinitionWithRegion<WithGroup> { }

        /** The stage of a disk encryption set definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithEncryptionType> { }

        /**
         * The stage of a disk encryption set definition allowing to set the disk encryption set type.
         */
        interface WithEncryptionType {
            /**
             * Set the disk encryption set type.
             * @param type the type of key used to encrypt the data of the disk
             * @return the next stage of the definition
             */
            WithKeyVault withEncryptionType(DiskEncryptionSetType type);
        }

        /**
         * The stage of a disk encryption set definition allowing to associate with an Azure key vault.
         */
        interface WithKeyVault {
            /**
             * Associates with the disk encryption set an Azure key vault by its resource ID.
             * @param keyVaultId resource ID of the Azure key vault
             * @return the next stage of the definition
             */
            WithKeyVaultKey withExistingKeyVault(String keyVaultId);
        }

        /**
         * The stage of a disk encryption set definition allowing to associate with an Azure key vault key.
         */
        interface WithKeyVaultKey {
            /**
             * Associate with the disk encryption set an Azure key vault key by its ID.
             * @param keyId ID of the Azure key vault key
             * @return the next stage of the definition
             */
            WithSystemAssignedManagedServiceIdentity withExistingKey(String keyId);
        }

        /**
         * The stage of a disk encryption set definition allowing to enable System Assigned Managed Service
         * Identity.
         */
        interface WithSystemAssignedManagedServiceIdentity {
            /**
             * Specifies that System Assigned Managed Service Identity needs to be enabled in the disk
             * encryption set.
             *
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedManagedServiceIdentity();
        }

        /**
         * The stage of the System Assigned Managed Service Identity enabled disk encryption set allowing to set
         * access role for the key vault.
         */
        interface WithSystemAssignedIdentityBasedAccessOrCreate extends WithCreate {
            /**
             * Specifies that disk encryption set's system assigned identity should have the given RBAC based access
             * (described by the role) on the current Azure key vault that's associated with it.
             * Only works for key vaults that use the 'Azure role-based access control' permission model.
             * If you prefer Access Policy based access for Azure Key Vault (like the examples from Portal or CLI),
             * instead of calling this method, you may want to call Vault-related methods after creating the
             * {@link DiskEncryptionSet} instance.
             * @param builtInRole access role to assigned to the disk encryption set's local identity
             * @return the next stage of the definition
             */
            WithCreate withRBACBasedAccessToCurrentKeyVault(BuiltInRole builtInRole);

            /**
             * Specifies that disk encryption set's system assigned identity should have the RBAC based access
             * with default {@link BuiltInRole#KEY_VAULT_CRYPTO_SERVICE_ENCRYPTION_USER} on the current Azure key vault
             * that's associated with it.
             * Only works for key vaults that use the 'Azure role-based access control' permission model.
             * If you prefer Access Policy based access for Azure Key Vault (like the examples from Portal or CLI),
             * instead of calling this method, you may want to call Vault-related methods after creating the
             * {@link DiskEncryptionSet} instance.
             * @return the next stage of the definition
             */
            WithCreate withRBACBasedAccessToCurrentKeyVault();
        }

        /**
         * The stage of a disk encryption set definition allowing to enable automatic key rotation.
         */
        interface WithAutomaticKeyRotation {
            /**
             * Enable automatic key rotation.
             * If enabled, the system will automatically update all managed disks, snapshots, and images
             * referencing the disk encryption set to use the new version of the key within one hour.
             * @return the next stage of the definition
             */
            WithCreate withAutomaticKeyRotation();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<DiskEncryptionSet>,
                DefinitionWithTags<WithCreate>,
                WithAutomaticKeyRotation { }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<DiskEncryptionSet>,
        UpdateWithTags<Update>,
        UpdateStages.WithSystemAssignedManagedServiceIdentity,
        UpdateStages.WithAutomaticKeyRotation {
    }

    /** Grouping of disk encryption set update stages. */
    interface UpdateStages {
        /**
         * The stage of a disk encryption set update allowing to enable System Assigned Managed Service
         * Identity.
         */
        interface WithSystemAssignedManagedServiceIdentity {
            /**
             * Specifies that System Assigned Managed Service Identity needs to be enabled in the disk
             * encryption set.
             * @return the next stage of the update
             */
            Update withSystemAssignedManagedServiceIdentity();

            /**
             * Specifies that System Assigned (Local) Managed Service Identity needs to be disabled in the disk
             * encryption set.
             * @return the next stage of the update
             */
            Update withoutSystemAssignedManagedServiceIdentity();
        }

        /**
         * The stage of a disk encryption set update allowing to enable automatic key rotation.
         */
        interface WithAutomaticKeyRotation {
            /**
             * Enable automatic key rotation.
             * If enabled, the system will automatically update all managed disks, snapshots, and images
             * referencing the disk encryption set to use the new version of the key within one hour.
             * @return the next stage of the update
             */
            Update withAutomaticKeyRotation();

            /**
             * Disable automatic key rotation.
             * @return the next stage of the update
             */
            Update withoutAutomaticKeyRotation();
        }
    }
}
