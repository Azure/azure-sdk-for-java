/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.VirtualNetworks;
import com.microsoft.azure.management.resources.collection.ResourceGroups;
import com.microsoft.azure.management.storage.StorageAccounts;

public interface Subscription {
    VirtualMachines virtualMachines();

    ResourceGroups resourceGroups();

    StorageAccounts storageAccounts();

    VirtualNetworks virtualNetworks();
}
