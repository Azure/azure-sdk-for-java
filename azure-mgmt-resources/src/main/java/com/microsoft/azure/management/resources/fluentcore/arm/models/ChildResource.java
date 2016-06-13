/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.resources.fluentcore.arm.models;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

/**
 * Base interface used by child resources.
 */
public interface ChildResource extends Indexable {
    /**
     * @return the name of the child resource
     */
    String name();
}
