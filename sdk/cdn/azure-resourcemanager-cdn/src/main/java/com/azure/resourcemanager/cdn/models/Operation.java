// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.resourcemanager.cdn.fluent.models.OperationInner;

/**
 * Operation that CDN service supports.
 */
public class Operation {
    private final OperationInner inner;

    /**
     * Construct Operation object from server response object.
     *
     * @param inner server response object containing supported operation description.
     */
    public Operation(OperationInner inner) {
        this.inner = inner;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.inner.name();
    }

    /**
     * Get the provider value.
     *
     * @return the provider value
     */
    public String provider() {
        if (this.inner.display() == null) {
            return null;
        }
        return this.inner.display().provider();
    }

    /**
     * Get the resource value.
     *
     * @return the resource value
     */
    public String resource() {
        if (this.inner.display() == null) {
            return null;
        }
        return this.inner.display().resource();
    }

    /**
     * Get the operation value.
     *
     * @return the operation value
     */
    public String type() {
        if (this.inner.display() == null) {
            return null;
        }
        return this.inner.display().operation();
    }

}
