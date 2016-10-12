/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An interface representing a model that has a name.
 */
@Fluent
public interface HasName {
    /**
     * @return the name of the resource
     */
    String name();
}
