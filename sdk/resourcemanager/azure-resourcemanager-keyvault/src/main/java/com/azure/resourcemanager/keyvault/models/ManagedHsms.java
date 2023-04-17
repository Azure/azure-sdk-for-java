// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;

/** Entry point for managed HSM management API. */
@Fluent
public interface ManagedHsms
    extends SupportsListingByResourceGroup<ManagedHsm>,
    SupportsGettingByResourceGroup<ManagedHsm>,
    SupportsGettingById<ManagedHsm>,
    SupportsDeletingByResourceGroup,
    SupportsDeletingById,
    HasManager<KeyVaultManager> {
}
