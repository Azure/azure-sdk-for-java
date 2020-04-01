/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage;

import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.storage.implementation.StorageManager;
import com.azure.management.storage.models.SkusInner;

/**
 * Entry point to storage service SKUs.
 */
public interface StorageSkus
        extends
        SupportsListing<StorageSku>,
        HasInner<SkusInner>,
        HasManager<StorageManager> {
}