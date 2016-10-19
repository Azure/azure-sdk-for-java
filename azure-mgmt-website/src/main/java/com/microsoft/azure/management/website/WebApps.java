/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;

/**
 * Entry point for storage accounts management API.
 */
public interface WebApps extends
        SupportsCreating<WebApp.DefinitionStages.Blank>,
        SupportsDeleting,
        SupportsListingByGroup<WebApp>,
        SupportsGettingByGroup<WebApp>,
        SupportsGettingById<WebApp>,
        SupportsDeletingByGroup {
}