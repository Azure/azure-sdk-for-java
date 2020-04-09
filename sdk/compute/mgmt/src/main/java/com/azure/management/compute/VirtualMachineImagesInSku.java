// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.compute;
import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to virtual machine SKU images.
 */
@Fluent
public interface VirtualMachineImagesInSku extends SupportsListing<VirtualMachineImage> {
}
