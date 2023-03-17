// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.models.ManagedHsmInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsUpdatingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;

import java.util.List;

/** An immutable client-side representation of an Azure Managed HSM. */
@Fluent
public interface ManagedHsm
    extends GroupableResource<KeyVaultManager, ManagedHsmInner>, Refreshable<ManagedHsm>,
    SupportsListingPrivateLinkResource,
    SupportsUpdatingPrivateEndpointConnection {

    /** @return the AAD tenant ID that should be used for authenticating requests to the managed HSM */
    String tenantId();

    /** @return Managed HSM SKU */
    ManagedHsmSku sku();

    /** @return initial administrators object ids for this managed hsm pool */
    List<String> initialAdminObjectIds();

    /** @return the URI of the managed hsm pool for performing operations on keys */
    String hsmUri();

    /** @return whether the 'soft delete' functionality is enabled for this managed HSM */
    boolean isSoftDeleteEnabled();

    /** @return softDelete data retention days. It accepts >=7 and <=90 */
    Integer softDeleteRetentionDays();

    /** @return whether protection against purge is enabled for this managed HSM */
    boolean isPurgeProtectionEnabled();

    /** @return whether the resource is being created or is being recovered from a deleted resource */
    CreateMode createMode();

    /** @return rules governing the accessibility of the key vault from specific network locations */
    MhsmNetworkRuleSet networkRuleSet();

    /** @return provisioning state */
    ProvisioningState provisioningState();

    /** @return the Key Vault key API entry point */
    Keys keys();
}
