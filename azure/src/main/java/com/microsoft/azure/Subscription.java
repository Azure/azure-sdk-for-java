/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.storage.StorageAccounts;

import java.io.IOException;

public interface Subscription {
    ResourceGroups resourceGroups() throws IOException, CloudException;
    StorageAccounts storageAccounts() throws IOException, CloudException;
}
