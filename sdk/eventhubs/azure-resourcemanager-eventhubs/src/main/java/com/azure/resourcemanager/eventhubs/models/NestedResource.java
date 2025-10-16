// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.eventhubs.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;

/**
 * A type contains subset of ARM envelop properties
 * in {@link com.azure.core.management.Resource} namely id, name and type.
 */
@Fluent
public interface NestedResource extends Indexable {
    /**
     * Gets resource id.
     *
     * @return resource id.
     */
    String id();

    /**
     * Gets the resource name.
     *
     * @return the resource name.
     */
    String name();

    /**
     * Gets the resource type.
     *
     * @return the resource type.
     */
    String type();
}
