// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.UsagesClient;
import com.azure.resourcemanager.storage.fluent.inner.UsageInner;

/** Entry point for storage resource usage management API. */
@Fluent
public interface Usages extends SupportsListing<UsageInner>, HasInner<UsagesClient>, HasManager<StorageManager> {
}
