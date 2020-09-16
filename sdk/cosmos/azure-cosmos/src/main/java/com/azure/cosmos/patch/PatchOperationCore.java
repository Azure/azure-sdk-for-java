// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.patch;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.patch.implementation.PatchOperationType;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class PatchOperationCore<TResource>  extends PatchOperation<TResource> {

    private String path;

    /**
     * Initializes a new instance of the {@link PatchOperationCore} class.
     *
     * @param operationType Specifies the type of Patch operation
     * @param path          Specifies the path to target location.
     * @param value         Specifies the value to be used
     */
    PatchOperationCore(PatchOperationType operationType, String path, TResource value) {
        super(operationType, value);

        checkArgument(StringUtils.isNotEmpty(path), "path empty %s", path);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
