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
import java.util.UUID;

/**
 * An immutable client-side representation of an Azure Key Vault.
 */
public interface Vault extends
        GroupableResource,
        Refreshable<Vault>,
        Updatable<Vault.Update>,
        Wrapper<VaultInner> {
    /**
     * @return The URI of the vault for performing operations on keys and secrets.
     */
    String vaultUri();

    /**
     * @return The Azure Active Directory tenant ID that should be used for
     * authenticating requests to the key vault.
     */
    UUID tenantId();

    /**
     * @return SKU details.
     */
    Sku sku();

    /**
     * @return An array of 0 to 16 identities that have access to the key vault. All
     * identities in the array must use the same tenant ID as the key vault's
     * tenant ID.
     */
    List<AccessPolicyEntry> accessPolicies();

    /**
     * @return Property to specify whether Azure Virtual Machines are permitted to
     * retrieve certificates stored as secrets from the key vault.
     */
    Boolean enabledForDeployment();

    /**
     * @return Property to specify whether Azure Disk Encryption is permitted to
     * retrieve secrets from the vault and unwrap keys.
     */
    Boolean enabledForDiskEncryption();

    /**
     * @return Property to specify whether Azure Resource Manager is permitted to
     * retrieve secrets from the key vault.
     */
    Boolean enabledForTemplateDeployment();

    AccessPolicy.AppAuthorizationStages.WithPermissions defineAppAuthorization(String applicationId);

    /**************************************************************
     * Fluent interfaces to provision a Vault
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithTenantIdOrAccessPolicy,
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
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithTenantIdOrAccessPolicy> {
        }

        interface WithTenantIdOrAccessPolicy extends WithTenantId, WithAccessPolicy {
        }

        interface WithTenantId {
            WithAccessPolicy withTenantId(UUID tenantId);
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

        interface WithAccessPolicy {
            WithCreate withEmptyAccessPolicy();
            WithCreate withAccessPolicy(AccessPolicy accessPolicy);
            AccessPolicy.DefinitionStages.Blank<WithCreate> defineAccessPolicy();
        }

        interface WithConfigurations {
            WithCreate enabledForDeployment(boolean enabled);
            WithCreate enabledForDiskEncryption(boolean enabled);
            WithCreate enabledForTemplateDeployment(boolean enabled);
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
            DefinitionStages.WithConfigurations {
        }
    }

    /**
     * Grouping of all the key vault update stages.
     */
    interface UpdateStages {
        interface WithAccessPolicy {
            Update withoutAccessPolicy(String objectId);
            Update withAccessPolicy(AccessPolicy accessPolicy);
            AccessPolicy.UpdateDefinitionStages.Blank<Update> defineAccessPolicy();
            AccessPolicy.Update updateAccessPolicy(String objectId);
        }

        interface WithConfigurations {
            Update enabledForDeployment(boolean enabled);
            Update enabledForDiskEncryption(boolean enabled);
            Update enabledForTemplateDeployment(boolean enabled);
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

