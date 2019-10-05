// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

public enum ItemType {
    NoValue(0x0), Null(0x1), Boolean(0x2), Number(0x4), String(0x5);

    private final int val;

    ItemType(int val) {
        this.val = val;
    }

    public int getVal() {
        return this.val;
    }
}
