// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

/**
 * The ResourceNamerFactory to generate ResourceNamer.
 */
public class ResourceNamerFactory {
    /**
     * Factory method to generate instance of ResourceNamer.
     * @param name prefix for the names.
     * @return instance of ResourceNamer
     */
    public ResourceNamer createResourceNamer(String name) {
        return new ResourceNamer(name);
    }
}
