/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;

public interface AzureAuthenticated {
    ResourceGroups resourceGroups();
    GenericResources genericResources();
    StorageAccounts storageAccounts();
    Usages usages();
}
