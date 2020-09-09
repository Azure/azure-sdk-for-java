// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.inner.VaultInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import java.util.List;

/** An immutable client-side representation of an Azure Key Vault. */
@Fluent
public interface Vault
    extends GroupableResource<KeyVaultManager, VaultInner>, Refreshable<Vault>, Updatable<Vault.Update> {
    /** @return an authenticated Key Vault secret client */
    SecretAsyncClient secretClient();

    /** @return an authenticated Key Vault key client */
    KeyAsyncClient keyClient();

    /** @return an authenticated Key Vault rest client */
    HttpPipeline vaultHttpPipeline();

    /** @return the Key Vault key API entry point */
    Keys keys();

    /** @return the Key Vault secret API entry point */
    Secrets secrets();

    /** @return the URI of the vault for performing operations on keys and secrets. */
    String vaultUri();

    /**
     * @return the Azure Active Directory tenant ID that should be used for authenticating requests to the key vault.
     */
    String tenantId();

    /** @return SKU details. */
    Sku sku();

    /**
     * @return an array of 0 to 16 identities that have access to the key vault. All identities in the array must use
     *     the same tenant ID as the key vault's tenant ID.
     */
    List<AccessPolicy> accessPolicies();

    /**
     * @return whether Azure Virtual Machines are permitted to retrieve certificates stored as secrets from the key
     *     vault.
     */
    boolean enabledForDeployment();

    /** @return whether Azure Disk Encryption is permitted to retrieve secrets from the vault and unwrap keys. */
    boolean enabledForDiskEncryption();

    /** @return whether Azure Resource Manager is permitted to retrieve secrets from the key vault. */
    boolean enabledForTemplateDeployment();

    /** @return whether soft delete is enabled for this key vault. */
    boolean softDeleteEnabled();

    /**
     * @return whether purge protection is enabled for this key vault. Purge protection can only be enabled if soft
     *     delete is enabled.
     */
    boolean purgeProtectionEnabled();

    /**
     * Get the createMode value.
     *
     * @return the createMode value
     */
    CreateMode createMode();

    /**
     * Get the networkAcls value.
     *
     * @return the networkAcls value
     */
    NetworkRuleSet networkRuleSet();

    /**************************************************************
     * Fluent interfaces to provision a Vault
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithAccessPolicy,
            DefinitionStages.WithCreate {
    }

    /** Grouping of all the key vault definition stages. */
    interface DefinitionStages {
        /** The first stage of the key vault definition. */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /** A key vault definition allowing resource group to be set. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithAccessPolicy> {
        }

        /** A key vault definition allowing the sku to be set. */
        interface WithSku {
            /**
             * Specifies the sku of the key vault.
             *
             * @param skuName the sku
             * @return the next stage of key vault definition
             */
            WithCreate withSku(SkuName skuName);
        }

        /** A key vault definition allowing access policies to be attached. */
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

        /** A key vault definition allowing the networkAcl to be set. */
        interface WithNetworkRuleSet {

            /**
             * Specifies that by default access to key vault should be allowed from all networks.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withAccessFromAllNetworks();

            /**
             * Specifies that by default access to key vault should be denied from all networks. except from those
             * networks specified via withVirtualNetworkRules, withAccessFromIpAddressRange, withAccessFromIpAddress
             *
             * @return the next stage of key vault definition
             */
            WithCreate withAccessFromSelectedNetworks();

            /**
             * Specifies that access to the key vault from the specific ip address should be allowed.
             *
             * @param ipAddress the ip address
             * @return the next stage of key vault definition
             */
            WithCreate withAccessFromIpAddress(String ipAddress);

            /**
             * Specifies that access to the key vault from the specific ip range should be allowed.
             *
             * @param ipAddressCidr the ip address CIDR
             * @return the next stage of key vault definition
             */
            WithCreate withAccessFromIpAddressRange(String ipAddressCidr);

            /**
             * Specifies that access to the key vault should be allowed from applications running on Microsoft azure
             * services.
             *
             * @return the next stage of key vault definition.
             */
            WithCreate withAccessFromAzureServices();

            /**
             * Set the bypass value.
             *
             * @param bypass the bypass value to set
             * @return the next stage of key vault definition.
             */
            WithCreate withBypass(NetworkRuleBypassOptions bypass);

            /**
             * Set the defaultAction value.
             *
             * @param defaultAction the defaultAction value to set
             * @return the next stage of key vault definition.
             */
            WithCreate withDefaultAction(NetworkRuleAction defaultAction);

            /**
             * Get the virtualNetworkRules value.
             *
             * @param virtualNetworkRules the virtual network rules
             * @return the next stage of key vault definition.
             */
            WithCreate withVirtualNetworkRules(List<VirtualNetworkRule> virtualNetworkRules);
        }

        /** A key vault definition allowing various configurations to be set. */
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
             * Enable soft delete for the key vault.
             *
             * @return the next stage of key vault definition
             */
            WithCreate withSoftDeleteEnabled();

            /**
             * Enable purge protection for the key vault; valid only if soft delete is also enabled.
             *
             * @return the next stage of key vault definition.
             */
            WithCreate withPurgeProtectionEnabled();

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
         * A key vault definition with sufficient inputs to create a new storage account in the cloud, but exposing
         * additional optional inputs to specify.
         */
        interface WithCreate
            extends Creatable<Vault>,
                GroupableResource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithSku,
                DefinitionStages.WithNetworkRuleSet,
                DefinitionStages.WithConfigurations,
                DefinitionStages.WithAccessPolicy {
        }
    }

    /** Grouping of all the key vault update stages. */
    interface UpdateStages {
        /** A key vault update allowing access policies to be modified, attached, or removed. */
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

        /** A key vault update allowing the NetworkRuleSet to be set. */
        interface WithNetworkRuleSet {

            /**
             * Specifies that by default access to key vault should be allowed from all networks.
             *
             * @return the next stage of key vault definition
             */
            Update withAccessFromAllNetworks();

            /**
             * Specifies that by default access to key vault should be denied from all networks. except from those
             * networks specified via withVirtualNetworkRules, withAccessFromIpAddressRange withAccesFromIpAddress
             *
             * @return the update stage of key vault definition
             */
            Update withAccessFromSelectedNetworks();

            /**
             * Specifies that access to the key vault from the specific ip address should be allowed.
             *
             * @param ipAddress the ip address
             * @return the update stage of key vault definition
             */
            Update withAccessFromIpAddress(String ipAddress);

            /**
             * Specifies that access to the key vault from the specific ip range should be allowed.
             *
             * @param ipAddressCidr the idAddress range in Cidr format
             * @return the update stage of key vault definition
             */
            Update withAccessFromIpAddressRange(String ipAddressCidr);

            /**
             * Specifies that access to the key vault should be allowed from applications running on Microsoft azure
             * services.
             *
             * @return the update stage of key vault definition.
             */
            Update withAccessFromAzureServices();

            /**
             * Set the bypass value.
             *
             * @param bypass the bypass value to set
             * @return the update stage of key vault definition.
             */
            Update withBypass(NetworkRuleBypassOptions bypass);

            /**
             * Set the defaultAction value.
             *
             * @param defaultAction the defaultAction value to set
             * @return the update stage of key vault definition.
             */
            Update withDefaultAction(NetworkRuleAction defaultAction);

            /**
             * Get the virtualNetworkRules value.
             *
             * @param virtualNetworkRules virtual network rules
             * @return the update stage of key vault definition.
             */
            Update withVirtualNetworkRules(List<VirtualNetworkRule> virtualNetworkRules);
        }

        /** A key vault update allowing various configurations to be set. */
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
             * Enable soft delete for the key vault.
             *
             * @return the next stage of key vault definition
             */
            Update withSoftDeleteEnabled();

            /**
             * Enable purge protection for the key vault; valid only if soft delete is also enabled.
             *
             * @return the next stage of key vault definition.
             */
            Update withPurgeProtectionEnabled();

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

    /** The template for a key vault update operation, containing all the settings that can be modified. */
    interface Update
        extends GroupableResource.UpdateWithTags<Update>,
            Appliable<Vault>,
            UpdateStages.WithAccessPolicy,
            UpdateStages.WithNetworkRuleSet,
            UpdateStages.WithConfigurations {
    }
}
