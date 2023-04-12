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

import java.time.OffsetDateTime;
import java.util.List;

/** An immutable client-side representation of an Azure Managed Hardware Security Module. */
@Fluent
public interface ManagedHsm
    extends GroupableResource<KeyVaultManager, ManagedHsmInner>, Refreshable<ManagedHsm>,
    SupportsListingPrivateLinkResource,
    SupportsUpdatingPrivateEndpointConnection {

    /** @return the AAD tenant ID that should be used for authenticating requests to the managed HSM */
    String tenantId();

    /** @return Managed HSM SKU */
    ManagedHsmSku sku();

    /**
     * When a managed HSM is created, the requestor also provides a list of data plane administrators (all security principals are supported).
     * Only these administrators are able to access the managed HSM data plane to perform key operations and
     * manage data plane role assignments (Managed HSM local RBAC).
     * @return initial administrators object ids for this managed hsm pool
     */
    List<String> initialAdminObjectIds();

    /** @return the URI of the managed hsm pool for performing operations on keys */
    String hsmUri();

    /** @return whether the 'soft delete' functionality is enabled for this managed HSM */
    boolean isSoftDeleteEnabled();

    /** @return softDelete data retention days. It accepts value between 7 and 90 (both included) */
    Integer softDeleteRetentionInDays();

    /** @return whether protection against purge is enabled for this managed HSM */
    boolean isPurgeProtectionEnabled();

    /** @return rules governing the accessibility of the key vault from specific network locations */
    MhsmNetworkRuleSet networkRuleSet();

    /** @return the Key Vault key API entry point */
    Keys keys();

    /** @return the scheduled purge date in UTC */
    OffsetDateTime scheduledPurgeDate();

    /** @return whether data plane traffic coming from public networks is allowed while private endpoint is enabled */
    PublicNetworkAccess publicNetworkAccess();
}
