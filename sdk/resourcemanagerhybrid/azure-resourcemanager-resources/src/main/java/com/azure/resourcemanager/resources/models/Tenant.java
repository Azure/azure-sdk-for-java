// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluent.models.TenantIdDescriptionInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/**
 * An immutable client-side representation of an Azure tenant.
 */
@Fluent
public interface Tenant extends Indexable, HasInnerModel<TenantIdDescriptionInner> {
    /**
     * @return the UUID of the tenant
     */
    String tenantId();
}
