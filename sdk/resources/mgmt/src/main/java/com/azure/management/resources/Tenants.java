/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.models.TenantIdDescriptionInner;

/**
 * Entry point to tenant management API.
 */
@Fluent
public interface Tenants extends
        SupportsListing<TenantIdDescriptionInner> {
}
