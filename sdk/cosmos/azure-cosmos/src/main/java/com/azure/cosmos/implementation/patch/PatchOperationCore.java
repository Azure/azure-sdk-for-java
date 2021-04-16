// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.patch;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * @param <T> The type of value for this patch operation.
 */
public final class PatchOperationCore<T> extends PatchOperation {

    private final String path;
    private final T resource;

    /**
     * Initializes a new instance of the {@link PatchOperationCore} class.
     *
     * @param operationType Specifies the type of Update operation
     * @param path          Specifies the path to target location.
     * @param value         Specifies the value to be used
     */
    public PatchOperationCore(PatchOperationType operationType, String path, T value) {
        super(operationType);

        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);
        this.path = path;
        this.resource = value;
    }

    String getPath() {
        return path;
    }

    T getResource() {
        return resource;
    }
}
