/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.azure.management.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.HasManager;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.storage.implementation.StorageManager;
import com.azure.management.storage.inner.UsageInner;
import com.azure.management.storage.inner.UsagesClient;

/** Entry point for storage resource usage management API. */
@Fluent
public interface Usages extends SupportsListing<UsageInner>, HasInner<UsagesClient>, HasManager<StorageManager> {
}
