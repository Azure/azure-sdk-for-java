// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingByRegion;

/** Entry point for network resource usage management API. */
@Fluent
public interface NetworkUsages extends SupportsListingByRegion<NetworkUsage> {
}
