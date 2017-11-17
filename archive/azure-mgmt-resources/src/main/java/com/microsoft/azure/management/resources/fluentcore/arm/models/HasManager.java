/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An interface representing a model that exposes a management client.
 * @param <ManagerT> the manager client type
 */
@Fluent
public interface HasManager<ManagerT> {
    /**
     * @return the manager client of this resource type
     */
    ManagerT manager();
}
