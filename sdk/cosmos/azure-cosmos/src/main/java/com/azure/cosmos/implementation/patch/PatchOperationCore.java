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

    private final String from;
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
        this.from = null;
    }

    public PatchOperationCore(PatchOperationType operationType, String from, String path) {
        super(operationType);

        checkArgument(StringUtils.isNotEmpty(from), "path empty %s", from);
        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);
        this.from = from;
        this.path = path;
        this.resource = null;
    }

    public PatchOperationCore(PatchOperationType operationType, String path) {
        super(operationType);

        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);
        this.path = path;
        this.from = null;
        this.resource = null;
    }

    public String getPath() {
        return path;
    }

    public String getFrom() {
        return from;
    }

    public T getResource() {
        return resource;
    }
}
