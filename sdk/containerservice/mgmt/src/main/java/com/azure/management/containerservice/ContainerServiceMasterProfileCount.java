// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.containerservice;

import com.azure.core.annotation.Fluent;

/** The minimum valid number of master nodes. */
@Fluent
public enum ContainerServiceMasterProfileCount {
    /** Enum value Min. */
    MIN(1),

    /** Enum value Mid. */
    MID(3),

    /** Enum value Max. */
    MAX(5);

    /** The count for a ContainerServiceMasterProfileCount instance. */
    private int count;

    ContainerServiceMasterProfileCount(int count) {
        this.count = count;
    }

    /** @return the count. */
    public int count() {
        return this.count;
    }
}
