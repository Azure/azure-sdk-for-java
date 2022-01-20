// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;

/** Entry point to virtual machine image offer SKUs. */
@Fluent
public interface VirtualMachineSkus extends SupportsListing<VirtualMachineSku> {
}
