/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.implementation;

import com.microsoft.azure.management.resources.implementation.ResourceManager;

/**
 * Base class for Azure resource managers.
 */
public abstract class Manager {

    private final ResourceManager resourceManager;

    protected Manager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    protected ResourceManager resourceManager() {
        return this.resourceManager;
    }
}
