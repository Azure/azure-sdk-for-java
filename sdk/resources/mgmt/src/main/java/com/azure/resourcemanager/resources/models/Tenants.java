// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import com.azure.resourcemanager.resources.fluent.inner.TenantIdDescriptionInner;

/**
 * Entry point to tenant management API.
 */
@Fluent
public interface Tenants extends
        SupportsListing<TenantIdDescriptionInner> {
}
