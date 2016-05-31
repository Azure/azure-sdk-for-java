/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.TenantIdDescriptionInner;

/**
 * An immutable client-side representation of an Azure tenant.
 */
public interface Tenant extends
        Indexable,
        Wrapper<TenantIdDescriptionInner> {

    /**
     * @return a UUID of the tenant
     */
    String tenantId();
}
