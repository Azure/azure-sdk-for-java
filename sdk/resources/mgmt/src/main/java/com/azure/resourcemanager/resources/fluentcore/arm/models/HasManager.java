// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.resources.fluentcore.arm.models;

import com.azure.core.annotation.Fluent;

/**
 * An interface representing a model that exposes a management client.
 *
 * @param <ManagerT> the manager client type
 */
@Fluent
public interface HasManager<ManagerT> {
    /**
     * @return the manager client of this resource type
     */
    ManagerT manager();
}
