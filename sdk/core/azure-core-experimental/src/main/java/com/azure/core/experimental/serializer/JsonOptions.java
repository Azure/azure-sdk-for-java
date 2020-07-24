// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

/**
 * The json serializer options.
 */
public class JsonOptions {
    private boolean includeNull;

    /**
     * The option of taking nulls when serialize.
     *
     * @param includeNull The boolean indicate whether to serialize null or not.
     * @return The {@link JsonOptions} object itself.
     */
    public JsonOptions setIncludeNull(boolean includeNull) {
        this.includeNull = includeNull;
        return this;
    }

    /**
     * Check whether serialize nulls set to true.
     *
     * @return true if set serialize nulls, otherwise false.
     */
    public boolean isNullIncluded() {
        return includeNull;
    }


}
