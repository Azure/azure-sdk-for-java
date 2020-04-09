// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

/**
 * Entry point for compute resource usage management API.
 */
@Fluent
public interface ComputeUsages extends SupportsListingByRegion<ComputeUsage> {
}
