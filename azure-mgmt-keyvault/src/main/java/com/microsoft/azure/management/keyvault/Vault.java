/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault;

import com.microsoft.azure.management.keyvault.implementation.VaultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;

/**
 * An immutable client-side representation of an Azure Key Vault.
 */
public interface Vault extends
        GroupableResource,
        Refreshable<Vault>,
        Updatable<Vault.Update>,
        Wrapper<VaultInner> {
    /**
     * @return the URI of the vault for performing operations on keys and secrets.
     */
    String vaultUri();

    /**
     * @return the Azure Active Directory tenant ID that should be used for
     * authenticating requests to the key vault.
     */
    String tenantId();

    /**
     * @return SKU details.
     */
    Sku sku();

    /**
     * @return an array of 0 to 16 identities that have access to the key vault. All
     * identities in the array must use the same tenant ID as the key vault's
     * tenant ID.
     */
    List<AccessPolicy> accessPolicies();

    /**
     * @return whether Azure Virtual Machines are permitted to
     * retrieve certificates stored as secrets from the key vault.
     */
    boolean enabledForDeployment();

    /**
     * @return whether Azure Disk Encryption is permitted to
     * retrieve secrets from the vault and unwrap keys.
     */
    boolean enabledForDiskEncryption();

    /**
     * @return whether Azure Resource Manager is permitted to
     * retrieve secrets from the key vault.
     */
    boolean enabledForTemplateDeployment();

    /**************************************************************
     * Fluent interfaces to provision a Vault
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithAccessPolicy,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the key vault definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the key vault definition.
         */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /**
         * A key vault definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithAccessPolicy> {
        }

        /**
         * A key vault definition allowing the sku to be set.
         */
        interface WithSku {
            /**
             * Specifies the sku of the key vault.
             *
             * @param skuName the sku
             * @return the next stage of key vault definition
             */
            WithCreate withSku(SkuName skuName);
        }

        /**
         * A key vault definition allowing access policies to be attached.
         */
        interface WithAccessPolicy {
            /**
             * Attach no access policy.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withEmptyAccessPolicy();

            /**
             * Attach an existing access policy.
             *
             * @param accessPolicy the existing access policy
             * @return the next stage of key vault definition
             */
            WithCreate withAccessPolicy(AccessPolicy accessPolicy);

            /**
             * Begins the definition of a new access policy to be added to this key vault.
             *
             * @return the first stage of the access policy definition
             */
            AccessPolicy.DefinitionStages.Blank<WithCreate> defineAccessPolicy();
        }

        /**
         * A key vault definition allowing various configurations to be set.
         */
        interface WithConfigurations {
            /**
             * Enable Azure Virtual Machines to retrieve certificates stored as secrets from the key vault.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withDeploymentEnabled();

            /**
             * Enable Azure Disk Encryption to retrieve secrets from the vault and unwrap keys.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withDiskEncryptionEnabled();

            /**
             * Enable Azure Resource Manager to retrieve secrets from the key vault.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withTemplateDeploymentEnabled();

            /**
             * Disable Azure Virtual Machines to retrieve certificates stored as secrets from the key vault.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withDeploymentDisabled();

            /**
             * Disable Azure Disk Encryption to retrieve secrets from the vault and unwrap keys.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withDiskEncryptionDisabled();

            /**
             * Disable Azure Resource Manager to retrieve secrets from the key vault.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withTemplateDeploymentDisabled();
        }

        /**
         * A key vault definition with sufficient inputs to create a new
         * storage account in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            Creatable<Vault>,
            GroupableResource.DefinitionWithTags<WithCreate>,
            DefinitionStages.WithSku,
            DefinitionStages.WithConfigurations,
            DefinitionStages.WithAccessPolicy {
        }
    }

    /**
     * Grouping of all the key vault update stages.
     */
    interface UpdateStages {
        /**
         * A key vault update allowing access policies to be modified, attached, or removed.
         */
        interface WithAccessPolicy {
            /**
             * Remove an access policy from the access policy list.
             *
             * @param objectId the object ID of the Active Directory identity the access policy is for
             * @return the key vault update stage
             */
            Update withoutAccessPolicy(String objectId);

            /**
             * Attach an existing access policy.
             *
             * @param accessPolicy the existing access policy
             * @return the key vault update stage
             */
            Update withAccessPolicy(AccessPolicy accessPolicy);

            /**
             * Begins the definition of a new access policy to be added to this key vault.
             *
             * @return the first stage of the access policy definition
             */
            AccessPolicy.UpdateDefinitionStages.Blank<Update> defineAccessPolicy();

            /**
             * Begins the update of an existing access policy attached to this key vault.
             *
             * @param objectId the object ID of the Active Directory identity the access policy is for
             * @return the update stage of the access policy definition
             */
            AccessPolicy.Update updateAccessPolicy(String objectId);
        }

        /**
         * A key vault update allowing various configurations to be set.
         */
        interface WithConfigurations {
            /**
             * Enable Azure Virtual Machines to retrieve certificates stored as secrets from the key vault.
             *
             * @return the key vault update stage
             */
            Update withDeploymentEnabled();

            /**
             * Enable Azure Disk Encryption to retrieve secrets from the vault and unwrap keys.
             *
             * @return the key vault update stage
             */
            Update withDiskEncryptionEnabled();

            /**
             * Enable Azure Resource Manager to retrieve secrets from the key vault.
             *
             * @return the key vault update stage
             */
            Update withTemplateDeploymentEnabled();

            /**
             * Disable Azure Virtual Machines to retrieve certificates stored as secrets from the key vault.
             *
             * @return the key vault update stage
             */
            Update withDeploymentDisabled();

            /**
             * Disable Azure Disk Encryption to retrieve secrets from the vault and unwrap keys.
             *
             * @return the next stage of key vault definition
             */
            Update withDiskEncryptionDisabled();

            /**
             * Disable Azure Resource Manager to retrieve secrets from the key vault.
             *
             * @return the key vault update stage
             */
            Update withTemplateDeploymentDisabled();
        }
    }

    /**
     * The template for a key vault update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<Vault>,
            UpdateStages.WithAccessPolicy,
            UpdateStages.WithConfigurations {
    }
}

