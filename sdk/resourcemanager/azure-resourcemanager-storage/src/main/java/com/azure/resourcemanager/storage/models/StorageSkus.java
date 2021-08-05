// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.storage.StorageManager;

/** Entry point to storage service SKUs. */
public interface StorageSkus extends SupportsListing<StorageSku>, HasManager<StorageManager> {
}
