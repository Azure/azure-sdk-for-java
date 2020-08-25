/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

/**
 * A type contains subset of ARM envelop properties in {@link com.microsoft.azure.Resource} namely id, name and type.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_7_0)
public interface NestedResource
        extends
        Indexable {
    /**
     * @return resource id.
     */
    String id();
    /**
     * @return the resource name.
     */
    String name();
    /**
     *
     * @return the resource type.
     */
    String type();
}
