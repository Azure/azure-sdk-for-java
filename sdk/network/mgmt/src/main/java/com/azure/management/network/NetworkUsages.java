/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.network;


import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.collection.SupportsListingByRegion;

/**
 * Entry point for network resource usage management API.
 */
@Fluent
public interface NetworkUsages extends SupportsListingByRegion<NetworkUsage> {
}
