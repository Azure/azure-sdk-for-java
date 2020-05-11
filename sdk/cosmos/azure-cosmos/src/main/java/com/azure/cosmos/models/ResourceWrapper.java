// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Resource;

/**
 * Represents a base class has a resource.
 */
public abstract class ResourceWrapper {

    /** @return Resource. */
    abstract Resource getResource();

    /** @return Id of the resource. */
    public String getId() { return this.getResource().getId(); }

    /** @return Resource id of the resource. */
    public String getResourceId() { return this.getResource().getResourceId(); }
}
