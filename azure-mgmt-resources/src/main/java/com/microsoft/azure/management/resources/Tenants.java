/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;

/**
 * Entry point to tenant management API.
 */
public interface Tenants extends
        SupportsListing<Tenant> {
}
