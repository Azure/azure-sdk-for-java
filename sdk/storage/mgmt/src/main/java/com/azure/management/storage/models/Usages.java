// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.storage.StorageManager;
import com.azure.management.storage.fluent.UsagesClient;
import com.azure.management.storage.fluent.inner.UsageInner;

/** Entry point for storage resource usage management API. */
@Fluent
public interface Usages extends SupportsListing<UsageInner>, HasInner<UsagesClient>, HasManager<StorageManager> {
}
