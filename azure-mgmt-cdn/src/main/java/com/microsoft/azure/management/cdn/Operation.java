/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.cdn.implementation.OperationInner;

/**
 * Operation that CDN service supports.
 */
@LangDefinition
public class Operation {
    private OperationInner inner;

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
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String name() {
        return this.inner.name();
    }

    /**
     * Get the provider value.
     *
     * @return the provider value
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
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
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
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
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String type() {
        if (this.inner.display() == null) {
            return null;
        }
        return this.inner.display().operation();
    }

}
