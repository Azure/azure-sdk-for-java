// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

/**
 * The json serializer options.
 */
public class JsonOptions {
    private boolean serializeNulls;

    /**
     * The option of taking nulls when serialize.
     *
     * @return The {@link JsonOptions} object itself.
     */
    public JsonOptions includeNulls() {
        serializeNulls = true;
        return this;
    }

    /**
     * Check whether serialize nulls set to true.
     *
     * @return true if set serialize nulls, otherwise false.
     */
    public boolean isNullIncluded() {
        return serializeNulls;
    }
}
