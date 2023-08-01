// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;

/** Entry point to disk encryption set management API. */
public interface DiskEncryptionSets
    extends SupportsCreating<DiskEncryptionSet.DefinitionStages.Blank>,
        SupportsListingByResourceGroup<DiskEncryptionSet>,
        SupportsGettingByResourceGroup<DiskEncryptionSet>,
        SupportsGettingById<DiskEncryptionSet>,
        SupportsDeletingByResourceGroup,
        SupportsDeletingById {
}
