// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

public enum MigrateCollectionDirective {
    /**
     * Move to SSD
     */
    Thaw,

    /**
     * Move to HDD
     */
    Freeze
}
