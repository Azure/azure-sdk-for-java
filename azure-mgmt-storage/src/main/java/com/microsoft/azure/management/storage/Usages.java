/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point for storage resource usage management API.
 */
@LangDefinition(ContainerName="~/")
public interface Usages extends SupportsListing<StorageUsage> {
}
