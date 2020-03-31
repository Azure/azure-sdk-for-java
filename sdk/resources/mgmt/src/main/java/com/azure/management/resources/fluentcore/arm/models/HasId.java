/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.resources.fluentcore.arm.models;

import com.azure.core.annotation.Fluent;

/**
 * An interface representing a model that has a resource group name.
 */
@Fluent
public interface HasId {
    /**
     * @return the resource ID string
     */
    String id();
}
